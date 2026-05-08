package com.ljs.livolunteer.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ljs.livolunteer.constant.CommonConstant;
import com.ljs.livolunteer.exception.ErrorCode;
import com.ljs.livolunteer.exception.ThrowUtils;
import com.ljs.livolunteer.mapper.CheckInRecordMapper;
import com.ljs.livolunteer.mapper.VolunteerHoursMapper;
import com.ljs.livolunteer.model.dto.volunteerhours.VolunteerHoursQueryRequest;
import com.ljs.livolunteer.model.dto.volunteerhours.VolunteerHoursReviewRequest;
import com.ljs.livolunteer.model.entity.Activity;
import com.ljs.livolunteer.model.entity.CheckInRecord;
import com.ljs.livolunteer.model.entity.User;
import com.ljs.livolunteer.model.entity.VolunteerHours;
import com.ljs.livolunteer.model.enums.CheckInStatusEnum;
import com.ljs.livolunteer.model.enums.VolunteerHoursStatusEnum;
import com.ljs.livolunteer.model.vo.VolunteerHoursVO;
import com.ljs.livolunteer.service.ActivityService;
import com.ljs.livolunteer.service.UserService;
import com.ljs.livolunteer.service.VolunteerHoursService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ljs.livolunteer.constant.UserConstant.ADMIN_ROLE;

/**
 * 志愿时长认证服务实现
 */
@Slf4j
@Service
public class VolunteerHoursServiceImpl extends ServiceImpl<VolunteerHoursMapper, VolunteerHours>
        implements VolunteerHoursService {

    @Resource
    private UserService userService;

    @Resource
    private ActivityService activityService;

    @Resource
    private CheckInRecordMapper checkInRecordMapper;

    @Override
    public QueryWrapper<VolunteerHours> getQueryWrapper(VolunteerHoursQueryRequest volunteerHoursQueryRequest) {
        ThrowUtils.throwIf(volunteerHoursQueryRequest == null, ErrorCode.PARAMS_ERROR);

        Long activityId = volunteerHoursQueryRequest.getActivityId();
        Long userId = volunteerHoursQueryRequest.getUserId();
        Integer status = volunteerHoursQueryRequest.getStatus();
        String sortField = volunteerHoursQueryRequest.getSortField();
        String sortOrder = volunteerHoursQueryRequest.getSortOrder();

        QueryWrapper<VolunteerHours> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(activityId != null, "activityId", activityId);
        queryWrapper.eq(userId != null, "userId", userId);
        queryWrapper.eq(status != null, "status", status);
        queryWrapper.orderBy(StrUtil.isNotBlank(sortField),
                CommonConstant.SORT_ORDER_ASC.equals(sortOrder), sortField);
        return queryWrapper;
    }

    @Override
    public VolunteerHoursVO getVolunteerHoursVO(VolunteerHours volunteerHours) {
        if (volunteerHours == null) {
            return null;
        }
        VolunteerHoursVO vo = new VolunteerHoursVO();
        BeanUtil.copyProperties(volunteerHours, vo);

        Long userId = volunteerHours.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            if (user != null) {
                vo.setUserName(user.getUserName());
            }
        }

        Long activityId = volunteerHours.getActivityId();
        if (activityId != null) {
            Activity activity = activityService.getById(activityId);
            if (activity != null) {
                vo.setActivityTitle(activity.getTitle());
            }
        }

        Long certifierId = volunteerHours.getCertifierId();
        if (certifierId != null) {
            User certifier = userService.getById(certifierId);
            if (certifier != null) {
                vo.setCertifierName(certifier.getUserName());
            }
        }

        return vo;
    }

    @Override
    public Page<VolunteerHoursVO> getVolunteerHoursVOPage(Page<VolunteerHours> volunteerHoursPage) {
        List<VolunteerHours> volunteerHoursList = volunteerHoursPage.getRecords();
        Page<VolunteerHoursVO> voPage = new Page<>(volunteerHoursPage.getCurrent(),
                volunteerHoursPage.getSize(), volunteerHoursPage.getTotal());

        if (volunteerHoursList == null || volunteerHoursList.isEmpty()) {
            return voPage;
        }

        Set<Long> userIds = volunteerHoursList.stream()
                .map(VolunteerHours::getUserId)
                .collect(Collectors.toSet());
        Set<Long> certifierIds = volunteerHoursList.stream()
                .map(VolunteerHours::getCertifierId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        Set<Long> allUserIds = new HashSet<>(userIds);
        allUserIds.addAll(certifierIds);
        Map<Long, User> userMap = userService.listByIds(allUserIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        Set<Long> activityIds = volunteerHoursList.stream()
                .map(VolunteerHours::getActivityId)
                .collect(Collectors.toSet());
        Map<Long, Activity> activityMap = activityService.listByIds(activityIds).stream()
                .collect(Collectors.toMap(Activity::getId, activity -> activity));

        List<VolunteerHoursVO> voList = volunteerHoursList.stream().map(volunteerHours -> {
            VolunteerHoursVO vo = new VolunteerHoursVO();
            BeanUtil.copyProperties(volunteerHours, vo);

            User user = userMap.get(volunteerHours.getUserId());
            if (user != null) {
                vo.setUserName(user.getUserName());
            }

            Activity activity = activityMap.get(volunteerHours.getActivityId());
            if (activity != null) {
                vo.setActivityTitle(activity.getTitle());
            }

            User certifier = userMap.get(volunteerHours.getCertifierId());
            if (certifier != null) {
                vo.setCertifierName(certifier.getUserName());
            }

            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean savePendingVolunteerHours(Long activityId, Long userId, BigDecimal actualHours) {
        ThrowUtils.throwIf(activityId == null || activityId <= 0, ErrorCode.PARAMS_ERROR, "活动ID不合法");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不合法");
        ThrowUtils.throwIf(actualHours == null || actualHours.compareTo(BigDecimal.ZERO) < 0,
                ErrorCode.PARAMS_ERROR, "实际服务时长不合法");

        Activity activity = activityService.getById(activityId);
        ThrowUtils.throwIf(activity == null, ErrorCode.NOT_FOUND_ERROR, "活动不存在");

        BigDecimal pendingHours = calculateAllowedHours(activity, actualHours);

        QueryWrapper<VolunteerHours> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("activityId", activityId)
                .eq("userId", userId);
        VolunteerHours existingRecord = this.getOne(queryWrapper);

        if (existingRecord == null) {
            VolunteerHours volunteerHours = new VolunteerHours();
            volunteerHours.setActivityId(activityId);
            volunteerHours.setUserId(userId);
            volunteerHours.setHours(pendingHours);
            volunteerHours.setStatus(VolunteerHoursStatusEnum.PENDING.getValue());
            boolean result = this.save(volunteerHours);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "生成志愿时长认证记录失败");
            return true;
        }

        ThrowUtils.throwIf(existingRecord.getStatus() != VolunteerHoursStatusEnum.PENDING.getValue(),
                ErrorCode.OPERATION_ERROR, "该活动的志愿时长认证记录已存在");

        VolunteerHours updateVolunteerHours = new VolunteerHours();
        updateVolunteerHours.setId(existingRecord.getId());
        updateVolunteerHours.setHours(pendingHours);
        updateVolunteerHours.setRemark(null);
        updateVolunteerHours.setCertifierId(null);
        updateVolunteerHours.setCertifyTime(null);

        boolean result = this.updateById(updateVolunteerHours);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新志愿时长认证记录失败");
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reviewVolunteerHours(VolunteerHoursReviewRequest reviewRequest) {
        ThrowUtils.throwIf(reviewRequest == null, ErrorCode.PARAMS_ERROR);

        Long id = reviewRequest.getId();
        Integer status = reviewRequest.getStatus();
        BigDecimal reviewHours = reviewRequest.getHours();
        String remark = reviewRequest.getRemark();

        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR, "认证记录ID不合法");
        ThrowUtils.throwIf(status == null || (status != VolunteerHoursStatusEnum.CERTIFIED.getValue()
                        && status != VolunteerHoursStatusEnum.REJECTED.getValue()),
                ErrorCode.PARAMS_ERROR, "认证状态只能为通过或拒绝");
        ThrowUtils.throwIf(reviewHours != null && reviewHours.compareTo(BigDecimal.ZERO) < 0,
                ErrorCode.PARAMS_ERROR, "认证时长不能小于0");

        if (VolunteerHoursStatusEnum.REJECTED.getValue() == status) {
            ThrowUtils.throwIf(StrUtil.isBlank(remark), ErrorCode.PARAMS_ERROR, "拒绝认证时必须填写备注");
        }

        VolunteerHours volunteerHours = this.getById(id);
        ThrowUtils.throwIf(volunteerHours == null, ErrorCode.NOT_FOUND_ERROR, "认证记录不存在");
        ThrowUtils.throwIf(volunteerHours.getStatus() != VolunteerHoursStatusEnum.PENDING.getValue(),
                ErrorCode.PARAMS_ERROR, "当前认证记录状态不允许审核");

        Activity activity = activityService.getById(volunteerHours.getActivityId());
        ThrowUtils.throwIf(activity == null, ErrorCode.NOT_FOUND_ERROR, "关联活动不存在");

        User loginUser = userService.getLoginUser();
        boolean isAdmin = ADMIN_ROLE.equals(loginUser.getUserRole());
        boolean isOrganizer = activity.getOrganizerId().equals(loginUser.getId());
        ThrowUtils.throwIf(!isAdmin && !isOrganizer,
                ErrorCode.NOT_AUTH_ERROR, "只有活动组织者或管理员可以认证志愿时长");

        BigDecimal approvedHours = volunteerHours.getHours();
        if (VolunteerHoursStatusEnum.CERTIFIED.getValue() == status) {
            CheckInRecord checkInRecord = getCheckedOutRecord(volunteerHours.getActivityId(), volunteerHours.getUserId());
            ThrowUtils.throwIf(checkInRecord == null || checkInRecord.getActualHours() == null,
                    ErrorCode.OPERATION_ERROR, "未找到已签退的签到记录，无法认证时长");

            BigDecimal maxAllowedHours = calculateAllowedHours(activity, checkInRecord.getActualHours());
            approvedHours = reviewHours == null ? approvedHours : reviewHours.setScale(2, RoundingMode.HALF_UP);

            ThrowUtils.throwIf(approvedHours == null || approvedHours.compareTo(BigDecimal.ZERO) <= 0,
                    ErrorCode.PARAMS_ERROR, "认证时长必须大于0");
            ThrowUtils.throwIf(approvedHours.compareTo(maxAllowedHours) > 0,
                    ErrorCode.PARAMS_ERROR, "认证时长不能超过可认证时长");
        }

        VolunteerHours updateVolunteerHours = new VolunteerHours();
        updateVolunteerHours.setId(id);
        updateVolunteerHours.setStatus(status);
        updateVolunteerHours.setRemark(remark);
        updateVolunteerHours.setCertifierId(loginUser.getId());
        updateVolunteerHours.setCertifyTime(new Date());
        if (VolunteerHoursStatusEnum.CERTIFIED.getValue() == status) {
            updateVolunteerHours.setHours(approvedHours);
        }

        boolean result = this.updateById(updateVolunteerHours);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "认证志愿时长失败");

        if (VolunteerHoursStatusEnum.CERTIFIED.getValue() == status) {
            User user = userService.getById(volunteerHours.getUserId());
            ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "认证用户不存在");

            BigDecimal totalHours = user.getTotalVolunteerHours() == null
                    ? BigDecimal.ZERO : user.getTotalVolunteerHours();
            User updateUser = new User();
            updateUser.setId(user.getId());
            updateUser.setTotalVolunteerHours(totalHours.add(approvedHours));

            boolean updateUserResult = userService.updateById(updateUser);
            ThrowUtils.throwIf(!updateUserResult, ErrorCode.OPERATION_ERROR, "更新用户总志愿时长失败");
        }

        return true;
    }

    private CheckInRecord getCheckedOutRecord(Long activityId, Long userId) {
        QueryWrapper<CheckInRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("activityId", activityId)
                .eq("userId", userId)
                .eq("status", CheckInStatusEnum.CHECKED_OUT.getValue());
        return checkInRecordMapper.selectOne(queryWrapper);
    }

    private BigDecimal calculateAllowedHours(Activity activity, BigDecimal actualHours) {
        BigDecimal safeActualHours = actualHours == null
                ? BigDecimal.ZERO : actualHours.setScale(2, RoundingMode.HALF_UP);
        BigDecimal activityHours = activity.getVolunteerHours();
        if (activityHours == null || activityHours.compareTo(BigDecimal.ZERO) <= 0) {
            return safeActualHours;
        }
        return safeActualHours.min(activityHours.setScale(2, RoundingMode.HALF_UP));
    }
}
