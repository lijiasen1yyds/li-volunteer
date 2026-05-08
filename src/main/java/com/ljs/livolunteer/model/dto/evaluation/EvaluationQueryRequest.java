package com.ljs.livolunteer.model.dto.evaluation;

import com.ljs.livolunteer.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 活动评价查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class EvaluationQueryRequest extends PageRequest implements Serializable {

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 最低评分
     */
    private Integer minRating;

    /**
     * 最高评分
     */
    private Integer maxRating;

    private static final long serialVersionUID = 1L;
}
