package com.ljs.livolunteer.model.dto.registration;

import lombok.Data;

import java.io.Serializable;

/**
 * 活动报名请求
 */
@Data
public class RegistrationAddRequest implements Serializable {

    /**
     * 活动ID（必填）
     */
    private Long activityId;

    private static final long serialVersionUID = 1L;
}
