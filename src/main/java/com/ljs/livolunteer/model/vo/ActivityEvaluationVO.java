package com.ljs.livolunteer.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 活动评价视图对象
 */
@Data
public class ActivityEvaluationVO implements Serializable {

    /**
     * 评价ID
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
     * 评分(1-5)
     */
    private Integer rating;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 活动标题
     */
    private String activityTitle;

    private static final long serialVersionUID = 1L;
}
