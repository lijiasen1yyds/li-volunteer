package com.ljs.livolunteer.model.dto.activity;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 更新活动请求
 */
@Data
public class ActivityUpdateRequest implements Serializable {

    /**
     * 活动ID
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
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /**
     * 活动结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /**
     * 报名截止时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date registrationDeadline;

    /**
     * 最大参与人数(0表示不限)
     */
    private Integer maxParticipants;

    /**
     * 可获得志愿时长(小时)
     */
    private BigDecimal volunteerHours;

    private static final long serialVersionUID = 1L;
}
