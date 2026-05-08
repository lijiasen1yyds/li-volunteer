package com.ljs.livolunteer.model.dto.evaluation;

import lombok.Data;

import java.io.Serializable;

/**
 * 活动评价添加请求
 */
@Data
public class EvaluationAddRequest implements Serializable {

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 评分(1-5)
     */
    private Integer rating;

    /**
     * 评价内容
     */
    private String content;

    private static final long serialVersionUID = 1L;
}
