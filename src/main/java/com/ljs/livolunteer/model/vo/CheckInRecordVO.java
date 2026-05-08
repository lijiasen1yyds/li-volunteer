package com.ljs.livolunteer.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 签到记录视图对象
 */
@Data
public class CheckInRecordVO implements Serializable {

    /**
     * 签到记录ID
     */
    private Long id;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 报名记录ID
     */
    private Long registrationId;

    /**
     * 签到时间
     */
    private Date checkInTime;

    /**
     * 签退时间
     */
    private Date checkOutTime;

    /**
     * 实际服务时长(小时)
     */
    private BigDecimal actualHours;

    /**
     * 签到状态: 0-已签到 1-已签退
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 活动标题
     */
    private String activityTitle;

    private static final long serialVersionUID = 1L;
}
