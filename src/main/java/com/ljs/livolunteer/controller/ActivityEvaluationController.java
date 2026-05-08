package com.ljs.livolunteer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ljs.livolunteer.annotation.AuthCheck;
import com.ljs.livolunteer.common.BaseResponse;
import com.ljs.livolunteer.common.ResultUtils;
import com.ljs.livolunteer.exception.ErrorCode;
import com.ljs.livolunteer.exception.ThrowUtils;
import com.ljs.livolunteer.model.dto.evaluation.EvaluationAddRequest;
import com.ljs.livolunteer.model.dto.evaluation.EvaluationQueryRequest;
import com.ljs.livolunteer.model.dto.evaluation.EvaluationUpdateRequest;
import com.ljs.livolunteer.model.entity.ActivityEvaluation;
import com.ljs.livolunteer.model.entity.User;
import com.ljs.livolunteer.model.vo.ActivityEvaluationVO;
import com.ljs.livolunteer.service.ActivityEvaluationService;
import com.ljs.livolunteer.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static com.ljs.livolunteer.constant.UserConstant.*;

/**
 * 活动评价接口
 */
@Slf4j
@RestController
@RequestMapping("/evaluation")
public class ActivityEvaluationController {

    @Resource
    private ActivityEvaluationService activityEvaluationService;

    @Resource
    private UserService userService;

    // region 增删改

    /**
     * 添加评价
     */
    @AuthCheck(mustRole = VOLUNTEER_ROLE)
    @PostMapping("/add")
    public BaseResponse<Long> addEvaluation(@RequestBody EvaluationAddRequest evaluationAddRequest) {
        ThrowUtils.throwIf(evaluationAddRequest == null, ErrorCode.PARAMS_ERROR);
        Long evaluationId = activityEvaluationService.addEvaluation(evaluationAddRequest);
        return ResultUtils.success(evaluationId);
    }

    /**
     * 修改评价（仅本人）
     */
    @AuthCheck
    @PostMapping("/update")
    public BaseResponse<Boolean> updateEvaluation(@RequestBody EvaluationUpdateRequest evaluationUpdateRequest) {
        ThrowUtils.throwIf(evaluationUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        boolean result = activityEvaluationService.updateEvaluation(evaluationUpdateRequest);
        return ResultUtils.success(result);
    }

    /**
     * 删除评价（本人或管理员）
     */
    @AuthCheck
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteEvaluation(@RequestBody long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        boolean result = activityEvaluationService.deleteEvaluation(id);
        return ResultUtils.success(result);
    }

    // endregion

    // region 查询

    /**
     * 获取评价详情
     */
    @GetMapping("/get/vo")
    public BaseResponse<ActivityEvaluationVO> getEvaluationVOById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        ActivityEvaluation evaluation = activityEvaluationService.getById(id);
        ThrowUtils.throwIf(evaluation == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(activityEvaluationService.getEvaluationVO(evaluation));
    }

    /**
     * 分页查询评价（按活动）
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<ActivityEvaluationVO>> listEvaluationVOByPage(
            @RequestBody EvaluationQueryRequest evaluationQueryRequest) {
        ThrowUtils.throwIf(evaluationQueryRequest == null, ErrorCode.PARAMS_ERROR);

        long current = evaluationQueryRequest.getCurrent();
        long size = evaluationQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "每页条数不能超过20");

        Page<ActivityEvaluation> evaluationPage = activityEvaluationService.page(
                new Page<>(current, size),
                activityEvaluationService.getQueryWrapper(evaluationQueryRequest)
        );
        return ResultUtils.success(activityEvaluationService.getEvaluationVOPage(evaluationPage));
    }

    /**
     * 查看我的评价列表
     */
    @AuthCheck
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<ActivityEvaluationVO>> listMyEvaluationVOByPage(
            @RequestBody EvaluationQueryRequest evaluationQueryRequest) {
        ThrowUtils.throwIf(evaluationQueryRequest == null, ErrorCode.PARAMS_ERROR);

        // 强制设置 userId 为当前用户
        User loginUser = userService.getLoginUser();
        evaluationQueryRequest.setUserId(loginUser.getId());

        long current = evaluationQueryRequest.getCurrent();
        long size = evaluationQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "每页条数不能超过20");

        Page<ActivityEvaluation> evaluationPage = activityEvaluationService.page(
                new Page<>(current, size),
                activityEvaluationService.getQueryWrapper(evaluationQueryRequest)
        );
        return ResultUtils.success(activityEvaluationService.getEvaluationVOPage(evaluationPage));
    }

    // endregion
}
