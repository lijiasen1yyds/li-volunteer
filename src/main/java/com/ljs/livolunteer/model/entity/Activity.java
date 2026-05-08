package com.ljs.livolunteer.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 活动实体类
 *
 * @TableName activity
 */
@Data
@TableName("activity")
public class
Activity implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 活动标题
     */
    private String title;

    /**
     * 活动描述
     */
    private String description;

    /**
     * 活动封面图片URL
     */
    private String coverImage;

    /**
     * 活动类别: community-社区帮扶 competition-赛事保障 environment-环保宣传 education-教育支持 other-其他
     */
    private String category;

    /**
     * 活动地点
     */
    private String location;

    /**
     * 活动开始时间
     */
    private Date startTime;

    /**
     * 活动结束时间
     */
    private Date endTime;

    /**
     * 报名截止时间
     */
    private Date registrationDeadline;

    /**
     * 最大参与人数(0表示不限)
     */
    private Integer maxParticipants;

    /**
     * 当前报名通过人数
     */
    private Integer currentParticipants;

    /**
     * 可获得志愿时长(小时)
     */
    private BigDecimal volunteerHours;

    /**
     * 组织者ID(关联user表)
     */
    private Long organizerId;

    /**
     * 活动状态: 0-待审核 1-已发布 2-审核拒绝 3-进行中 4-已完成 5-已取消
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
