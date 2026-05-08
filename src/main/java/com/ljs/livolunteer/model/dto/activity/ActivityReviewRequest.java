package com.ljs.livolunteer.model.dto.activity;

import lombok.Data;

import java.io.Serializable;

/**
 * 活动审核请求
 */
@Data
public class ActivityReviewRequest implements Serializable {

    /**
     * 活动 ID
     */
    private Long id;

    /**
     * 审核结果：1=通过，2=拒绝
     */
    private Integer status;

    /**
     * 审核意见（拒绝时必填）
     */
    private String reviewMessage;

    private static final long serialVersionUID = 1L;
}
