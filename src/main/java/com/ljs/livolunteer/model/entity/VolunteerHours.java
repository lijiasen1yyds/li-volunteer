package com.ljs.livolunteer.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 志愿时长认证实体类
 *
 * @TableName volunteer_hours
 */
@Data
@TableName("volunteer_hours")
public class VolunteerHours implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 认证时长(小时)
     */
    private BigDecimal hours;

    /**
     * 认证状态: 0-待认证 1-已认证 2-认证拒绝
     */
    private Integer status;

    /**
     * 认证人ID
     */
    private Long certifierId;

    /**
     * 认证时间
     */
    private Date certifyTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除: 0-未删除 1-已删除
     */
    @TableLogic
    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}
