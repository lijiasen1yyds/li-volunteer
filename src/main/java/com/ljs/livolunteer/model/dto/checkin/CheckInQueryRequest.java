package com.ljs.livolunteer.model.dto.checkin;

import com.ljs.livolunteer.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 签到记录查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CheckInQueryRequest extends PageRequest implements Serializable {

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 签到状态: 0-已签到 1-已签退
     */
    private Integer status;

    private static final long serialVersionUID = 1L;
}
