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
import com.ljs.livolunteer.model.dto.checkin.CheckInQueryRequest;
import com.ljs.livolunteer.model.entity.Activity;
import com.ljs.livolunteer.model.entity.ActivityRegistration;
import com.ljs.livolunteer.model.entity.CheckInRecord;
import com.ljs.livolunteer.model.entity.User;
import com.ljs.livolunteer.model.enums.ActivityStatusEnum;
import com.ljs.livolunteer.model.enums.CheckInStatusEnum;
import com.ljs.livolunteer.model.enums.RegistrationStatusEnum;
import com.ljs.livolunteer.model.vo.CheckInRecordVO;
import com.ljs.livolunteer.service.ActivityRegistrationService;
import com.ljs.livolunteer.service.ActivityService;
import com.ljs.livolunteer.service.CheckInRecordService;
import com.ljs.livolunteer.service.UserService;
import com.ljs.livolunteer.service.VolunteerHoursService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 签到记录服务实现
 */
@Slf4j
@Service
public class CheckInRecordServiceImpl extends ServiceImpl<CheckInRecordMapper, CheckInRecord>
        implements CheckInRecordService {

    @Resource
    private UserService userService;

    @Resource
    private ActivityService activityService;

    @Resource
    private ActivityRegistrationService activityRegistrationService;

    @Resource
    private VolunteerHoursService volunteerHoursService;

    @Override
    public QueryWrapper<CheckInRecord> getQueryWrapper(CheckInQueryRequest checkInQueryRequest) {
        ThrowUtils.throwIf(checkInQueryRequest == null, ErrorCode.PARAMS_ERROR);

        Long activityId = checkInQueryRequest.getActivityId();
        Long userId = checkInQueryRequest.getUserId();
        Integer status = checkInQueryRequest.getStatus();
        String sortField = checkInQueryRequest.getSortField();
        String sortOrder = checkInQueryRequest.getSortOrder();

        QueryWrapper<CheckInRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(activityId != null, "activityId", activityId);
        queryWrapper.eq(userId != null, "userId", userId);
        queryWrapper.eq(status != null, "status", status);
        queryWrapper.orderBy(StrUtil.isNotBlank(sortField),
                CommonConstant.SORT_ORDER_ASC.equals(sortOrder), sortField);

        return queryWrapper;
    }

    @Override
    public CheckInRecordVO getCheckInRecordVO(CheckInRecord checkInRecord) {
        if (checkInRecord == null) {
            return null;
        }
        CheckInRecordVO vo = new CheckInRecordVO();
        BeanUtil.copyProperties(checkInRecord, vo);

        Long userId = checkInRecord.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            if (user != null) {
                vo.setUserName(user.getUserName());
            }
        }

        Long activityId = checkInRecord.getActivityId();
        if (activityId != null) {
            Activity activity = activityService.getById(activityId);
            if (activity != null) {
                vo.setActivityTitle(activity.getTitle());
            }
        }

        return vo;
    }

    @Override
    public Page<CheckInRecordVO> getCheckInRecordVOPage(Page<CheckInRecord> checkInRecordPage) {
        List<CheckInRecord> recordList = checkInRecordPage.getRecords();
        Page<CheckInRecordVO> voPage = new Page<>(checkInRecordPage.getCurrent(),
                checkInRecordPage.getSize(), checkInRecordPage.getTotal());

        if (recordList == null || recordList.isEmpty()) {
            return voPage;
        }

        Set<Long> userIds = recordList.stream()
                .map(CheckInRecord::getUserId)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        Set<Long> activityIds = recordList.stream()
                .map(CheckInRecord::getActivityId)
                .collect(Collectors.toSet());
        Map<Long, Activity> activityMap = activityService.listByIds(activityIds).stream()
                .collect(Collectors.toMap(Activity::getId, activity -> activity));

        List<CheckInRecordVO> voList = recordList.stream().map(record -> {
            CheckInRecordVO vo = new CheckInRecordVO();
            BeanUtil.copyProperties(record, vo);

            User user = userMap.get(record.getUserId());
            if (user != null) {
                vo.setUserName(user.getUserName());
            }

            Activity activity = activityMap.get(record.getActivityId());
            if (activity != null) {
                vo.setActivityTitle(activity.getTitle());
            }

            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public Long checkIn(Long activityId) {
        Activity activity = activityService.getById(activityId);
        ThrowUtils.throwIf(activity == null, ErrorCode.NOT_FOUND_ERROR, "活动不存在");
        ThrowUtils.throwIf(ActivityStatusEnum.IN_PROGRESS.getValue() != activity.getStatus(),
                ErrorCode.PARAMS_ERROR, "只有进行中的活动才能签到");

        User loginUser = userService.getLoginUser();

        QueryWrapper<ActivityRegistration> regQueryWrapper = new QueryWrapper<>();
        regQueryWrapper.eq("activityId", activityId)
                .eq("userId", loginUser.getId())
                .eq("status", RegistrationStatusEnum.APPROVED.getValue());
        ActivityRegistration registration = activityRegistrationService.getOne(regQueryWrapper);
        ThrowUtils.throwIf(registration == null, ErrorCode.PARAMS_ERROR, "您未报名该活动或报名未通过审核");

        QueryWrapper<CheckInRecord> checkInQueryWrapper = new QueryWrapper<>();
        checkInQueryWrapper.eq("activityId", activityId)
                .eq("userId", loginUser.getId());
        CheckInRecord existingRecord = this.getOne(checkInQueryWrapper);
        ThrowUtils.throwIf(existingRecord != null, ErrorCode.PARAMS_ERROR, "您已签到过该活动，请勿重复签到");

        CheckInRecord checkInRecord = new CheckInRecord();
        checkInRecord.setActivityId(activityId);
        checkInRecord.setUserId(loginUser.getId());
        checkInRecord.setRegistrationId(registration.getId());
        checkInRecord.setCheckInTime(new Date());
        checkInRecord.setStatus(CheckInStatusEnum.CHECKED_IN.getValue());

        boolean result = this.save(checkInRecord);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "签到失败");
        return checkInRecord.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkOut(Long activityId) {
        User loginUser = userService.getLoginUser();

        QueryWrapper<CheckInRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("activityId", activityId)
                .eq("userId", loginUser.getId());
        CheckInRecord checkInRecord = this.getOne(queryWrapper);
        ThrowUtils.throwIf(checkInRecord == null, ErrorCode.NOT_FOUND_ERROR, "未找到签到记录");

        ThrowUtils.throwIf(CheckInStatusEnum.CHECKED_IN.getValue() != checkInRecord.getStatus(),
                ErrorCode.PARAMS_ERROR, "当前状态不允许签退，您可能已经签退过");

        Date checkOutTime = new Date();
        long durationMillis = checkOutTime.getTime() - checkInRecord.getCheckInTime().getTime();
        BigDecimal actualHours = BigDecimal.valueOf(durationMillis)
                .divide(BigDecimal.valueOf(3600000), 2, RoundingMode.HALF_UP);

        CheckInRecord updateRecord = new CheckInRecord();
        updateRecord.setId(checkInRecord.getId());
        updateRecord.setCheckOutTime(checkOutTime);
        updateRecord.setActualHours(actualHours);
        updateRecord.setStatus(CheckInStatusEnum.CHECKED_OUT.getValue());

        boolean result = this.updateById(updateRecord);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "签退失败");

        volunteerHoursService.savePendingVolunteerHours(activityId, loginUser.getId(), actualHours);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoCheckOutByActivity(Long activityId, Date activityEndTime) {
        // 查询该活动下所有未签退的签到记录
        QueryWrapper<CheckInRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("activityId", activityId)
                .eq("status", CheckInStatusEnum.CHECKED_IN.getValue());
        List<CheckInRecord> uncheckedOutRecords = this.list(queryWrapper);

        if (uncheckedOutRecords == null || uncheckedOutRecords.isEmpty()) {
            return;
        }

        for (CheckInRecord record : uncheckedOutRecords) {
            // 用活动结束时间作为签退时间
            long durationMillis = activityEndTime.getTime() - record.getCheckInTime().getTime();
            BigDecimal actualHours = BigDecimal.valueOf(durationMillis)
                    .divide(BigDecimal.valueOf(3600000), 2, RoundingMode.HALF_UP);

            // 更新签到记录为已签退
            CheckInRecord updateRecord = new CheckInRecord();
            updateRecord.setId(record.getId());
            updateRecord.setCheckOutTime(activityEndTime);
            updateRecord.setActualHours(actualHours);
            updateRecord.setStatus(CheckInStatusEnum.CHECKED_OUT.getValue());
            this.updateById(updateRecord);

            // 生成志愿时长认证记录
            volunteerHoursService.savePendingVolunteerHours(activityId, record.getUserId(), actualHours);
        }

        log.info("活动 {} 自动签退 {} 名志愿者", activityId, uncheckedOutRecords.size());
    }
}
