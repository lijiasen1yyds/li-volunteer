package com.ljs.livolunteer.model.dto.checkin;

import lombok.Data;

import java.io.Serializable;

/**
 * 签到请求
 */
@Data
public class CheckInRequest implements Serializable {

    /**
     * 活动ID
     */
    private Long activityId;

    private static final long serialVersionUID = 1L;
}
