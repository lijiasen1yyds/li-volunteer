package com.ljs.livolunteer.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 活动报名视图对象
 */
@Data
public class ActivityRegistrationVO implements Serializable {

    /**
     * 报名ID
     */
    private Long id;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 报名用户ID
     */
    private Long userId;

    /**
     * 报名状态: 0-待审核 1-已通过 2-已拒绝 3-已取消
     */
    private Integer status;

    /**
     * 审核意见
     */
    private String reviewMessage;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 报名用户昵称
     */
    private String userName;

    /**
     * 活动标题
     */
    private String activityTitle;

    private static final long serialVersionUID = 1L;
}
