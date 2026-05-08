package com.ljs.livolunteer.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ljs.livolunteer.constant.CommonConstant;
import com.ljs.livolunteer.exception.ErrorCode;
import com.ljs.livolunteer.exception.ThrowUtils;
import com.ljs.livolunteer.mapper.ActivityRegistrationMapper;
import com.ljs.livolunteer.model.dto.registration.RegistrationQueryRequest;
import com.ljs.livolunteer.model.dto.registration.RegistrationReviewRequest;
import com.ljs.livolunteer.model.entity.Activity;
import com.ljs.livolunteer.model.entity.ActivityRegistration;
import com.ljs.livolunteer.model.entity.User;
import com.ljs.livolunteer.model.enums.ActivityStatusEnum;
import com.ljs.livolunteer.model.enums.RegistrationStatusEnum;
import com.ljs.livolunteer.model.vo.ActivityRegistrationVO;
import com.ljs.livolunteer.service.ActivityRegistrationService;
import com.ljs.livolunteer.service.ActivityService;
import com.ljs.livolunteer.service.UserService;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ljs.livolunteer.constant.UserConstant.ADMIN_ROLE;

/**
 * 活动报名服务实现
 */
@Slf4j
@Service
public class ActivityRegistrationServiceImpl extends ServiceImpl<ActivityRegistrationMapper, ActivityRegistration>
        implements ActivityRegistrationService {

    @Resource
    private UserService userService;

    @Resource
    private ActivityService activityService;

    @Override
    public QueryWrapper<ActivityRegistration> getQueryWrapper(RegistrationQueryRequest registrationQueryRequest) {
        ThrowUtils.throwIf(registrationQueryRequest == null, ErrorCode.PARAMS_ERROR);

        Long activityId = registrationQueryRequest.getActivityId();
        Long userId = registrationQueryRequest.getUserId();
        Integer status = registrationQueryRequest.getStatus();
        String sortField = registrationQueryRequest.getSortField();
        String sortOrder = registrationQueryRequest.getSortOrder();

        QueryWrapper<ActivityRegistration> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(activityId != null, "activityId", activityId);
        queryWrapper.eq(userId != null, "userId", userId);
        queryWrapper.eq(status != null, "status", status);

        // 排序
        queryWrapper.orderBy(StrUtil.isNotBlank(sortField),
                CommonConstant.SORT_ORDER_ASC.equals(sortOrder), sortField);

        return queryWrapper;
    }

    @Override
    public ActivityRegistrationVO getRegistrationVO(ActivityRegistration activityRegistration) {
        if (activityRegistration == null) {
            return null;
        }
        ActivityRegistrationVO vo = new ActivityRegistrationVO();
        BeanUtil.copyProperties(activityRegistration, vo);

        // 填充用户昵称
        Long userId = activityRegistration.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            if (user != null) {
                vo.setUserName(user.getUserName());
            }
        }

        // 填充活动标题
        Long activityId = activityRegistration.getActivityId();
        if (activityId != null) {
            Activity activity = activityService.getById(activityId);
            if (activity != null) {
                vo.setActivityTitle(activity.getTitle());
            }
        }

        return vo;
    }

    @Override
    public Page<ActivityRegistrationVO> getRegistrationVOPage(Page<ActivityRegistration> registrationPage) {
        List<ActivityRegistration> registrationList = registrationPage.getRecords();
        Page<ActivityRegistrationVO> voPage = new Page<>(registrationPage.getCurrent(),
                registrationPage.getSize(), registrationPage.getTotal());

        if (registrationList == null || registrationList.isEmpty()) {
            return voPage;
        }

        // 批量查询用户信息
        Set<Long> userIds = registrationList.stream()
                .map(ActivityRegistration::getUserId)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 批量查询活动信息
        Set<Long> activityIds = registrationList.stream()
                .map(ActivityRegistration::getActivityId)
                .collect(Collectors.toSet());
        Map<Long, Activity> activityMap = activityService.listByIds(activityIds).stream()
                .collect(Collectors.toMap(Activity::getId, activity -> activity));

        // 转换为 VO
        List<ActivityRegistrationVO> voList = registrationList.stream().map(registration -> {
            ActivityRegistrationVO vo = new ActivityRegistrationVO();
            BeanUtil.copyProperties(registration, vo);

            User user = userMap.get(registration.getUserId());
            if (user != null) {
                vo.setUserName(user.getUserName());
            }

            Activity activity = activityMap.get(registration.getActivityId());
            if (activity != null) {
                vo.setActivityTitle(activity.getTitle());
            }

            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public boolean cancelRegistration(long id) {
        // 校验报名记录存在
        ActivityRegistration registration = this.getById(id);
        ThrowUtils.throwIf(registration == null, ErrorCode.NOT_FOUND_ERROR, "报名记录不存在");

        // 校验属于当前用户
        User loginUser = userService.getLoginUser();
        ThrowUtils.throwIf(!registration.getUserId().equals(loginUser.getId()),
                ErrorCode.NOT_AUTH_ERROR, "只能取消自己的报名");

        // 校验状态为待审核
        ThrowUtils.throwIf(RegistrationStatusEnum.PENDING.getValue() != registration.getStatus(),
                ErrorCode.PARAMS_ERROR, "只有待审核的报名可以取消");

        // 更新状态为已取消
        ActivityRegistration updateRegistration = new ActivityRegistration();
        updateRegistration.setId(id);
        updateRegistration.setStatus(RegistrationStatusEnum.CANCELLED.getValue());

        boolean result = this.updateById(updateRegistration);
        return true;
    }

    @Override
    public Long addRegistration(long activityId) {
        // 校验活动存在
        Activity activity = activityService.getById(activityId);
        ThrowUtils.throwIf(activity == null, ErrorCode.NOT_FOUND_ERROR, "活动不存在");

        // 校验活动状态为已发布
        ThrowUtils.throwIf(ActivityStatusEnum.PUBLISHED.getValue() != activity.getStatus(),
                ErrorCode.PARAMS_ERROR, "该活动当前不可报名");

        // 校验未过报名截止时间
        if (activity.getRegistrationDeadline() != null) {
            ThrowUtils.throwIf(new Date().after(activity.getRegistrationDeadline()),
                    ErrorCode.PARAMS_ERROR, "已超过报名截止时间");
        }

        // 校验未达最大参与人数
        if (activity.getMaxParticipants() != null && activity.getMaxParticipants() > 0) {
            int currentParticipants = activity.getCurrentParticipants() != null ? activity.getCurrentParticipants() : 0;
            ThrowUtils.throwIf(currentParticipants >= activity.getMaxParticipants(),
                    ErrorCode.PARAMS_ERROR, "报名人数已满");
        }

        User loginUser = userService.getLoginUser();

        // 检查用户是否已经报名过该活动
        QueryWrapper<ActivityRegistration> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("activityId", activityId)
                .eq("userId", loginUser.getId());
        ActivityRegistration existingRegistration = this.getOne(queryWrapper);
        if (existingRegistration != null) {
            if (existingRegistration.getStatus().equals(RegistrationStatusEnum.CANCELLED.getValue()) || existingRegistration.getStatus().equals(RegistrationStatusEnum.PENDING.getValue())) {
                existingRegistration.setStatus(RegistrationStatusEnum.PENDING.getValue());
                this.updateById(existingRegistration);
                return existingRegistration.getId();
            }
        } else {
            // 创建报名记录
            ActivityRegistration registration = new ActivityRegistration();
            registration.setActivityId(activityId);
            registration.setUserId(loginUser.getId());
            registration.setStatus(RegistrationStatusEnum.PENDING.getValue());

            boolean result = this.save(registration);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "报名失败");
            return registration.getId();
        }
        return null;
    }
    @Override
    public boolean reviewRegistration(RegistrationReviewRequest reviewRequest) {
        Long id = reviewRequest.getId();
        Integer status = reviewRequest.getStatus();
        String reviewMessage = reviewRequest.getReviewMessage();

        // 参数校验
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR, "报名记录ID不合法");
        ThrowUtils.throwIf(status == null || (status != RegistrationStatusEnum.APPROVED.getValue()
                        && status != RegistrationStatusEnum.REJECTED.getValue()),
                ErrorCode.PARAMS_ERROR, "审核状态只能为通过或拒绝");

        // 拒绝时审核意见必填
        if (RegistrationStatusEnum.REJECTED.getValue() == status) {
            ThrowUtils.throwIf(reviewMessage == null || reviewMessage.trim().isEmpty(),
                    ErrorCode.PARAMS_ERROR, "拒绝时必须填写审核意见");
        }

        // 校验报名记录存在
        ActivityRegistration registration = this.getById(id);
        ThrowUtils.throwIf(registration == null, ErrorCode.NOT_FOUND_ERROR, "报名记录不存在");

        // 校验状态为待审核
        ThrowUtils.throwIf(RegistrationStatusEnum.PENDING.getValue() != registration.getStatus(),
                ErrorCode.PARAMS_ERROR, "该报名当前状态不允许审核");

        // 校验当前用户是该活动的组织者或管理员
        User loginUser = userService.getLoginUser();
        Activity activity = activityService.getById(registration.getActivityId());
        ThrowUtils.throwIf(activity == null, ErrorCode.NOT_FOUND_ERROR, "关联活动不存在");

        boolean isAdmin = ADMIN_ROLE.equals(loginUser.getUserRole());
        boolean isOrganizer = activity.getOrganizerId().equals(loginUser.getId());
        ThrowUtils.throwIf(!isAdmin && !isOrganizer, ErrorCode.NOT_AUTH_ERROR, "只有活动组织者或管理员可以审核");

        // 更新审核信息
        ActivityRegistration updateRegistration = new ActivityRegistration();
        updateRegistration.setId(id);
        updateRegistration.setStatus(status);
        updateRegistration.setReviewMessage(reviewMessage);
        updateRegistration.setReviewerId(loginUser.getId());
        updateRegistration.setReviewTime(new Date());

        boolean result = this.updateById(updateRegistration);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "审核操作失败");

        // 通过时：activity 的 currentParticipants + 1
        if (RegistrationStatusEnum.APPROVED.getValue() == status) {
            Activity updateActivity = new Activity();
            updateActivity.setId(activity.getId());
            int currentParticipants = activity.getCurrentParticipants() != null ? activity.getCurrentParticipants() : 0;
            updateActivity.setCurrentParticipants(currentParticipants + 1);
            activityService.updateById(updateActivity);
        }
        return true;
    }
}
