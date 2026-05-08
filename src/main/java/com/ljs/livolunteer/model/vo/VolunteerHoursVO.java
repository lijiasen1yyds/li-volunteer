package com.ljs.livolunteer.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 志愿时长认证视图对象
 */
@Data
public class VolunteerHoursVO implements Serializable {

    /**
     * 认证记录ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 认证时长(小时)
     */
    private BigDecimal hours;

    /**
     * 认证状态
     */
    private Integer status;

    /**
     * 认证人ID
     */
    private Long certifierId;

    /**
     * 认证时间
     */
    private Date certifyTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 活动标题
     */
    private String activityTitle;

    /**
     * 认证人名称
     */
    private String certifierName;

    private static final long serialVersionUID = 1L;
}
