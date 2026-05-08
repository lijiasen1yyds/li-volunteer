package com.ljs.livolunteer.model.dto.registration;

import com.ljs.livolunteer.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 报名查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RegistrationQueryRequest extends PageRequest implements Serializable {

    /**
     * 按活动筛选
     */
    private Long activityId;

    /**
     * 按用户筛选
     */
    private Long userId;

    /**
     * 按状态筛选
     */
    private Integer status;

    private static final long serialVersionUID = 1L;
}
