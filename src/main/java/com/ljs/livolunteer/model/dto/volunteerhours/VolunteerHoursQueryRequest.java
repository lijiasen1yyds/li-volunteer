package com.ljs.livolunteer.model.dto.volunteerhours;

import com.ljs.livolunteer.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 志愿时长认证查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class VolunteerHoursQueryRequest extends PageRequest implements Serializable {

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 认证状态
     */
    private Integer status;

    private static final long serialVersionUID = 1L;
}
