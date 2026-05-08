package com.ljs.livolunteer.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 活动视图对象
 */
@Data
public class ActivityVO implements Serializable {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 活动标题
     */
    private String title;

    /**
     * 活动描述
     */
    private String description;

    /**
     * 活动封面图片URL
     */
    private String coverImage;

    /**
     * 活动类别
     */
    private String category;

    /**
     * 活动地点
     */
    private String location;

    /**
     * 活动开始时间
     */
    private Date startTime;

    /**
     * 活动结束时间
     */
    private Date endTime;

    /**
     * 报名截止时间
     */
    private Date registrationDeadline;

    /**
     * 最大参与人数
     */
    private Integer maxParticipants;

    /**
     * 当前报名通过人数
     */
    private Integer currentParticipants;

    /**
     * 可获得志愿时长(小时)
     */
    private BigDecimal volunteerHours;

    /**
     * 组织者ID
     */
    private Long organizerId;

    /**
     * 组织者名称
     */
    private String organizerName;

    /**
     * 活动状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;
}
