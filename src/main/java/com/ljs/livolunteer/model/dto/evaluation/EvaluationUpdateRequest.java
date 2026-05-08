package com.ljs.livolunteer.model.dto.evaluation;

import lombok.Data;

import java.io.Serializable;

/**
 * 活动评价修改请求
 */
@Data
public class EvaluationUpdateRequest implements Serializable {

    /**
     * 评价ID
     */
    private Long id;

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
