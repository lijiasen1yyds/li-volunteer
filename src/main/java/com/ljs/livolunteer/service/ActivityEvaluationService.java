package com.ljs.livolunteer.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ljs.livolunteer.model.dto.evaluation.EvaluationAddRequest;
import com.ljs.livolunteer.model.dto.evaluation.EvaluationQueryRequest;
import com.ljs.livolunteer.model.dto.evaluation.EvaluationUpdateRequest;
import com.ljs.livolunteer.model.entity.ActivityEvaluation;
import com.ljs.livolunteer.model.vo.ActivityEvaluationVO;

/**
 * 活动评价服务
 */
public interface ActivityEvaluationService extends IService<ActivityEvaluation> {

    /**
     * 构造查询条件
     */
    QueryWrapper<ActivityEvaluation> getQueryWrapper(EvaluationQueryRequest evaluationQueryRequest);

    /**
     * 获取评价视图对象（单条）
     */
    ActivityEvaluationVO getEvaluationVO(ActivityEvaluation activityEvaluation);

    /**
     * 获取评价视图对象（分页）
     */
    Page<ActivityEvaluationVO> getEvaluationVOPage(Page<ActivityEvaluation> evaluationPage);

    /**
     * 添加评价
     *
     * @param evaluationAddRequest 添加请求
     * @return 评价ID
     */
    Long addEvaluation(EvaluationAddRequest evaluationAddRequest);

    /**
     * 修改评价（仅本人）
     *
     * @param evaluationUpdateRequest 修改请求
     * @return 是否成功
     */
    boolean updateEvaluation(EvaluationUpdateRequest evaluationUpdateRequest);

    /**
     * 删除评价（本人或管理员）
     *
     * @param id 评价ID
     * @return 是否成功
     */
    boolean deleteEvaluation(long id);
}
