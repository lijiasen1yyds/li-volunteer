package com.ljs.livolunteer.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 活动报名实体类
 *
 * @TableName activity_registration
 */
@Data
@TableName("activity_registration")
public class ActivityRegistration implements Serializable {

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
     * 报名用户ID(关联user表)
     */
    private Long userId;

    /**
     * 报名状态: 0-待审核 1-已通过 2-已拒绝 3-已取消
     */
    private Integer status;

    /**
     * 审核意见
     */
    private String reviewMessage;

    /**
     * 审核人ID(关联user表)
     */
    private Long reviewerId;

    /**
     * 审核时间
     */
    private Date reviewTime;

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
