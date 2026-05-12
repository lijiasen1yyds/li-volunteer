package com.ljs.livolunteer.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ljs.livolunteer.constant.CommonConstant;
import com.ljs.livolunteer.constant.UserConstant;
import com.ljs.livolunteer.exception.BusinessException;
import com.ljs.livolunteer.exception.ErrorCode;
import com.ljs.livolunteer.exception.ThrowUtils;
import com.ljs.livolunteer.mapper.ActivityMapper;
import com.ljs.livolunteer.mapper.ActivityRegistrationMapper;
import com.ljs.livolunteer.mapper.CheckInRecordMapper;
import com.ljs.livolunteer.mapper.UserMapper;
import com.ljs.livolunteer.mapper.VolunteerHoursMapper;
import com.ljs.livolunteer.model.dto.user.UserQueryRequest;
import com.ljs.livolunteer.model.entity.Activity;
import com.ljs.livolunteer.model.entity.ActivityRegistration;
import com.ljs.livolunteer.model.entity.CheckInRecord;
import com.ljs.livolunteer.model.entity.User;
import com.ljs.livolunteer.model.entity.VolunteerHours;
import com.ljs.livolunteer.model.enums.ActivityStatusEnum;
import com.ljs.livolunteer.model.enums.RegistrationStatusEnum;
import com.ljs.livolunteer.model.enums.VolunteerHoursStatusEnum;
import com.ljs.livolunteer.model.vo.LoginUserVO;
import com.ljs.livolunteer.model.vo.UserStatsVO;
import com.ljs.livolunteer.model.vo.UserVO;
import com.ljs.livolunteer.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final String SALT = "ljs_volunteer";

    @Resource
    private ActivityRegistrationMapper activityRegistrationMapper;

    @Resource
    private ActivityMapper activityMapper;

    @Resource
    private VolunteerHoursMapper volunteerHoursMapper;

    @Resource
    private CheckInRecordMapper checkInRecordMapper;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 参数校验
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword, checkPassword),
                ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userAccount.length() < 4,
                ErrorCode.PARAMS_ERROR, "账号长度不能少于4位");
        ThrowUtils.throwIf(userPassword.length() < 8,
                ErrorCode.PARAMS_ERROR, "密码长度不能少于8位");
        ThrowUtils.throwIf(!userPassword.equals(checkPassword),
                ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");

        // 2. 检查账号是否已存在
        synchronized (userAccount.intern()) {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = this.count(queryWrapper);
            ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "账号已存在");

            // 3. 加密密码
            String encryptPassword = encryptPassword(userPassword);

            // 4. 插入新用户
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setUserName("用户" + RandomUtil.randomNumbers(6));
            boolean saved = this.save(user);
            ThrowUtils.throwIf(!saved, ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            return user.getId();
        }
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword) {
        // 1. 参数校验
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword),
                ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userAccount.length() < 4,
                ErrorCode.PARAMS_ERROR, "账号长度不能少于4位");
        ThrowUtils.throwIf(userPassword.length() < 8,
                ErrorCode.PARAMS_ERROR, "密码长度不能少于8位");

        // 2. 加密密码并查询用户
        String encryptPassword = encryptPassword(userPassword);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.getOne(queryWrapper);
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "账号或密码错误");

        // 3. 检查用户状态
        ThrowUtils.throwIf(user.getUserStatus() != 0, ErrorCode.FORBIDDEN_ERROR, "账号已被禁用");

        // 4. SA-Token 登录
        StpUtil.login(user.getId());
        log.info("用户登录成功, userId = {}", user.getId());

        return this.getLoginUserVO(user);
    }

    @Override
    public boolean userLogout() {
        StpUtil.checkLogin();
        StpUtil.logout();
        return true;
    }

    @Override
    public User getLoginUser() {
        Object userIdObj = StpUtil.getLoginId();
        long userId = Long.parseLong(userIdObj.toString());
        User user = this.getById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR, "用户不存在");
        ThrowUtils.throwIf(user.getUserStatus() != 0, ErrorCode.FORBIDDEN_ERROR, "账号已被禁用");
        return user;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollectionUtils.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userPhone = userQueryRequest.getUserPhone();
        String studentId = userQueryRequest.getStudentId();
        String college = userQueryRequest.getCollege();
        String userRole = userQueryRequest.getUserRole();
        Integer userStatus = userQueryRequest.getUserStatus();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.eq(StrUtil.isNotBlank(userPhone), "userPhone", userPhone);
        queryWrapper.eq(StrUtil.isNotBlank(studentId), "studentId", studentId);
        queryWrapper.eq(StrUtil.isNotBlank(college), "college", college);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.eq(userStatus != null, "userStatus", userStatus);

        queryWrapper.orderBy(StrUtil.isNotBlank(sortField),
                CommonConstant.SORT_ORDER_ASC.equals(sortOrder), sortField);

        return queryWrapper;
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserConstant.ADMIN_ROLE.equals(user.getUserRole());
    }

    @Override
    public String encryptPassword(String userPassword) {
        return DigestUtil.md5Hex(SALT + userPassword);
    }

    @Override
    public UserStatsVO getMyStats() {
        User loginUser = this.getLoginUser();
        Long userId = loginUser.getId();
        String role = loginUser.getUserRole();

        UserStatsVO vo = new UserStatsVO();
        vo.setUserId(userId);
        vo.setUserRole(role);
        vo.setTotalVolunteerHours(loginUser.getTotalVolunteerHours() == null
                ? BigDecimal.ZERO : loginUser.getTotalVolunteerHours());

        boolean isOrganizer = UserConstant.ORGANIZER_ROLE.equals(role);
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(role);

        // ===== 志愿者视角统计（所有角色都返回，方便前端复用） =====
        fillVolunteerStats(vo, userId);

        // ===== 组织者视角统计（仅 organizer / admin） =====
        if (isOrganizer || isAdmin) {
            fillOrganizerStats(vo, userId, isAdmin);
        } else {
            vo.setOrganizedActivityCount(0L);
            vo.setOrganizedCompletedCount(0L);
            vo.setOrganizedInProgressCount(0L);
            vo.setPendingReviewCount(0L);
        }

        return vo;
    }

    /**
     * 统计志愿者视角字段
     */
    private void fillVolunteerStats(UserStatsVO vo, Long userId) {
        // 我参加的活动数(报名已通过)
        QueryWrapper<ActivityRegistration> approvedWrapper = new QueryWrapper<>();
        approvedWrapper.eq("userId", userId)
                .eq("status", RegistrationStatusEnum.APPROVED.getValue());
        Long joinedCount = activityRegistrationMapper.selectCount(approvedWrapper);
        vo.setJoinedActivityCount(joinedCount == null ? 0L : joinedCount);

        // 我的报名总数(不含已取消)
        QueryWrapper<ActivityRegistration> totalWrapper = new QueryWrapper<>();
        totalWrapper.eq("userId", userId)
                .ne("status", RegistrationStatusEnum.CANCELLED.getValue());
        Long regTotal = activityRegistrationMapper.selectCount(totalWrapper);
        vo.setRegistrationTotalCount(regTotal == null ? 0L : regTotal);

        // 我的待审核数 = 报名待审核 + 志愿时长待认证
        QueryWrapper<ActivityRegistration> pendingRegWrapper = new QueryWrapper<>();
        pendingRegWrapper.eq("userId", userId)
                .eq("status", RegistrationStatusEnum.PENDING.getValue());
        Long pendingReg = activityRegistrationMapper.selectCount(pendingRegWrapper);

        QueryWrapper<VolunteerHours> pendingHoursWrapper = new QueryWrapper<>();
        pendingHoursWrapper.eq("userId", userId)
                .eq("status", VolunteerHoursStatusEnum.PENDING.getValue());
        Long pendingHours = volunteerHoursMapper.selectCount(pendingHoursWrapper);

        vo.setMyPendingCount((pendingReg == null ? 0L : pendingReg)
                + (pendingHours == null ? 0L : pendingHours));

        // 我的签到次数
        QueryWrapper<CheckInRecord> checkInWrapper = new QueryWrapper<>();
        checkInWrapper.eq("userId", userId);
        Long checkInCount = checkInRecordMapper.selectCount(checkInWrapper);
        vo.setCheckInCount(checkInCount == null ? 0L : checkInCount);

        // 我已完成的活动数 = 我已通过报名的活动中，活动状态为已完成的数量
        vo.setCompletedActivityCount(countMyCompletedActivities(userId));
    }

    /**
     * 统计我已通过报名的活动里，活动状态为已完成的数量
     */
    private Long countMyCompletedActivities(Long userId) {
        QueryWrapper<ActivityRegistration> regWrapper = new QueryWrapper<>();
        regWrapper.select("activityId")
                .eq("userId", userId)
                .eq("status", RegistrationStatusEnum.APPROVED.getValue());
        List<ActivityRegistration> regs = activityRegistrationMapper.selectList(regWrapper);
        if (CollectionUtils.isEmpty(regs)) {
            return 0L;
        }
        List<Long> activityIds = regs.stream()
                .map(ActivityRegistration::getActivityId)
                .collect(Collectors.toList());

        QueryWrapper<Activity> activityWrapper = new QueryWrapper<>();
        activityWrapper.in("id", activityIds)
                .eq("status", ActivityStatusEnum.COMPLETED.getValue());
        Long count = activityMapper.selectCount(activityWrapper);
        return count == null ? 0L : count;
    }

    /**
     * 统计组织者视角字段。admin 视为可看全部活动。
     */
    private void fillOrganizerStats(UserStatsVO vo, Long userId, boolean isAdmin) {
        QueryWrapper<Activity> baseWrapper = new QueryWrapper<>();
        if (!isAdmin) {
            baseWrapper.eq("organizerId", userId);
        }
        Long organizedTotal = activityMapper.selectCount(baseWrapper);
        vo.setOrganizedActivityCount(organizedTotal == null ? 0L : organizedTotal);

        QueryWrapper<Activity> completedWrapper = new QueryWrapper<>();
        if (!isAdmin) {
            completedWrapper.eq("organizerId", userId);
        }
        completedWrapper.eq("status", ActivityStatusEnum.COMPLETED.getValue());
        Long completed = activityMapper.selectCount(completedWrapper);
        vo.setOrganizedCompletedCount(completed == null ? 0L : completed);

        QueryWrapper<Activity> inProgressWrapper = new QueryWrapper<>();
        if (!isAdmin) {
            inProgressWrapper.eq("organizerId", userId);
        }
        inProgressWrapper.eq("status", ActivityStatusEnum.IN_PROGRESS.getValue());
        Long inProgress = activityMapper.selectCount(inProgressWrapper);
        vo.setOrganizedInProgressCount(inProgress == null ? 0L : inProgress);

        // 待我审核数 = 我活动中待审核报名 + 待认证志愿时长
        // admin 直接看全表
        Long pendingReg;
        Long pendingHours;
        if (isAdmin) {
            QueryWrapper<ActivityRegistration> regWrapper = new QueryWrapper<>();
            regWrapper.eq("status", RegistrationStatusEnum.PENDING.getValue());
            pendingReg = activityRegistrationMapper.selectCount(regWrapper);

            QueryWrapper<VolunteerHours> hoursWrapper = new QueryWrapper<>();
            hoursWrapper.eq("status", VolunteerHoursStatusEnum.PENDING.getValue());
            pendingHours = volunteerHoursMapper.selectCount(hoursWrapper);
        } else {
            // organizer：先取自己活动 id，再查关联的待审核数据
            QueryWrapper<Activity> idWrapper = new QueryWrapper<>();
            idWrapper.select("id").eq("organizerId", userId);
            List<Activity> myActivities = activityMapper.selectList(idWrapper);
            if (CollectionUtils.isEmpty(myActivities)) {
                pendingReg = 0L;
                pendingHours = 0L;
            } else {
                List<Long> activityIds = myActivities.stream()
                        .map(Activity::getId).collect(Collectors.toList());

                QueryWrapper<ActivityRegistration> regWrapper = new QueryWrapper<>();
                regWrapper.in("activityId", activityIds)
                        .eq("status", RegistrationStatusEnum.PENDING.getValue());
                pendingReg = activityRegistrationMapper.selectCount(regWrapper);

                QueryWrapper<VolunteerHours> hoursWrapper = new QueryWrapper<>();
                hoursWrapper.in("activityId", activityIds)
                        .eq("status", VolunteerHoursStatusEnum.PENDING.getValue());
                pendingHours = volunteerHoursMapper.selectCount(hoursWrapper);
            }
        }
        vo.setPendingReviewCount((pendingReg == null ? 0L : pendingReg)
                + (pendingHours == null ? 0L : pendingHours));
    }
}
