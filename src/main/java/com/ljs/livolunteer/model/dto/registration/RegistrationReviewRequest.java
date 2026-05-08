package com.ljs.livolunteer.model.dto.registration;

import lombok.Data;

import java.io.Serializable;

/**
 * 报名审核请求
 */
@Data
public class RegistrationReviewRequest implements Serializable {

    /**
     * 报名记录ID（必填）
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
