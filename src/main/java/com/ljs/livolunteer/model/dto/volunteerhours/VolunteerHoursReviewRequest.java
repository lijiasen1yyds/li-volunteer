package com.ljs.livolunteer.model.dto.volunteerhours;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 志愿时长认证审核请求
 */
@Data
public class VolunteerHoursReviewRequest implements Serializable {

    /**
     * 认证记录ID
     */
    private Long id;

    /**
     * 审核状态: 1-通过 2-拒绝
     */
    private Integer status;

    /**
     * 最终认证时长(小时)，审核通过时可选
     */
    private BigDecimal hours;

    /**
     * 认证备注
     */
    private String remark;

    private static final long serialVersionUID = 1L;
}
