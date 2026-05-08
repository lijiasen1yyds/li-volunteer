package com.ljs.livolunteer.model.dto.activity;

import com.ljs.livolunteer.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询活动请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ActivityQueryRequest extends PageRequest implements Serializable {

    /**
     * 活动标题（模糊搜索）
     */
    private String title;

    /**
     * 活动类别
     */
    private String category;

    /**
     * 活动状态
     */
    private Integer status;

    /**
     * 组织者ID
     */
    private Long organizerId;

    private static final long serialVersionUID = 1L;
}
