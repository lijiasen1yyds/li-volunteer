package com.ljs.livolunteer.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 签到记录实体类
 *
 * @TableName check_in_record
 */
@Data
@TableName("check_in_record")
public class CheckInRecord implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 活动ID(关联activity表)
     */
    private Long activityId;

    /**
     * 用户ID(关联user表)
     */
    private Long userId;

    /**
     * 报名记录ID(关联activity_registration表)
     */
    private Long registrationId;

    /**
     * 签到时间
     */
    private Date checkInTime;

    /**
     * 签退时间
     */
    private Date checkOutTime;

    /**
     * 实际服务时长(小时, 根据签到签退自动计算)
     */
    private BigDecimal actualHours;

    /**
     * 签到状态: 0-已签到 1-已签退
     */
    private Integer status;

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
