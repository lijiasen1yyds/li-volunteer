package com.ljs.livolunteer.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ljs.livolunteer.model.dto.checkin.CheckInQueryRequest;
import com.ljs.livolunteer.model.entity.CheckInRecord;
import com.ljs.livolunteer.model.vo.CheckInRecordVO;

import java.util.Date;

/**
 * 签到记录服务
 */
public interface CheckInRecordService extends IService<CheckInRecord> {

    /**
     * 构造查询条件
     */
    QueryWrapper<CheckInRecord> getQueryWrapper(CheckInQueryRequest checkInQueryRequest);

    /**
     * 获取签到记录视图对象（单条）
     */
    CheckInRecordVO getCheckInRecordVO(CheckInRecord checkInRecord);

    /**
     * 获取签到记录视图对象（分页）
     */
    Page<CheckInRecordVO> getCheckInRecordVOPage(Page<CheckInRecord> checkInRecordPage);

    /**
     * 志愿者签到
     *
     * @param activityId 活动ID
     * @return 签到记录ID
     */
    Long checkIn(Long activityId);

    /**
     * 志愿者签退
     *
     * @param activityId 活动ID
     * @return 是否成功
     */
    boolean checkOut(Long activityId);

    /**
     * 活动结束时，自动为未签退的志愿者执行签退并生成志愿时长认证记录
     *
     * @param activityId     活动ID
     * @param activityEndTime 活动结束时间（作为签退时间）
     */
    void autoCheckOutByActivity(Long activityId, Date activityEndTime);
}
