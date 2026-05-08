package com.ljs.livolunteer.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 活动评价实体类
 *
 * @TableName activity_evaluation
 */
@Data
@TableName("activity_evaluation")
public class ActivityEvaluation implements Serializable {

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
     * 评价用户ID(关联user表)
     */
    private Long userId;

    /**
     * 评分(1-5)
     */
    private Integer rating;

    /**
     * 评价内容
     */
    private String content;

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
