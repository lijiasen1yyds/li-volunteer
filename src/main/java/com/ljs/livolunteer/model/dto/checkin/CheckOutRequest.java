package com.ljs.livolunteer.model.dto.checkin;

import lombok.Data;

import java.io.Serializable;

/**
 * 签退请求
 */
@Data
public class CheckOutRequest implements Serializable {

    /**
     * 活动ID
     */
    private Long activityId;

    private static final long serialVersionUID = 1L;
}
