package com.ljs.livolunteer.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ljs.livolunteer.constant.CommonConstant;
import com.ljs.livolunteer.exception.ErrorCode;
import com.ljs.livolunteer.exception.ThrowUtils;
import com.ljs.livolunteer.mapper.ActivityEvaluationMapper;
import com.ljs.livolunteer.model.dto.evaluation.EvaluationAddRequest;
import com.ljs.livolunteer.model.dto.evaluation.EvaluationQueryRequest;
import com.ljs.livolunteer.model.dto.evaluation.EvaluationUpdateRequest;
import com.ljs.livolunteer.model.entity.Activity;
import com.ljs.livolunteer.model.entity.ActivityEvaluation;
import com.ljs.livolunteer.model.entity.ActivityRegistration;
import com.ljs.livolunteer.model.entity.User;
import com.ljs.livolunteer.model.enums.ActivityStatusEnum;
import com.ljs.livolunteer.model.enums.RegistrationStatusEnum;
import com.ljs.livolunteer.model.vo.ActivityEvaluationVO;
import com.ljs.livolunteer.service.ActivityEvaluationService;
import com.ljs.livolunteer.service.ActivityRegistrationService;
import com.ljs.livolunteer.service.ActivityService;
import com.ljs.livolunteer.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ljs.livolunteer.constant.UserConstant.ADMIN_ROLE;

/**
 * 活动评价服务实现
 */
@Slf4j
@Service
public class ActivityEvaluationServiceImpl extends ServiceImpl<ActivityEvaluationMapper, ActivityEvaluation>
        implements ActivityEvaluationService {

    @Resource
    private UserService userService;

    @Resource
    private ActivityService activityService;

    @Resource
    private ActivityRegistrationService activityRegistrationService;

    @Override
    public QueryWrapper<ActivityEvaluation> getQueryWrapper(EvaluationQueryRequest evaluationQueryRequest) {
        ThrowUtils.throwIf(evaluationQueryRequest == null, ErrorCode.PARAMS_ERROR);

        Long activityId = evaluationQueryRequest.getActivityId();
        Long userId = evaluationQueryRequest.getUserId();
        Integer minRating = evaluationQueryRequest.getMinRating();
        Integer maxRating = evaluationQueryRequest.getMaxRating();
        String sortField = evaluationQueryRequest.getSortField();
        String sortOrder = evaluationQueryRequest.getSortOrder();

        QueryWrapper<ActivityEvaluation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(activityId != null, "activityId", activityId);
        queryWrapper.eq(userId != null, "userId", userId);
        queryWrapper.ge(minRating != null, "rating", minRating);
        queryWrapper.le(maxRating != null, "rating", maxRating);

        // 排序
        queryWrapper.orderBy(StrUtil.isNotBlank(sortField),
                CommonConstant.SORT_ORDER_ASC.equals(sortOrder), sortField);

        return queryWrapper;
    }

    @Override
    public ActivityEvaluationVO getEvaluationVO(ActivityEvaluation activityEvaluation) {
        if (activityEvaluation == null) {
            return null;
        }
        ActivityEvaluationVO vo = new ActivityEvaluationVO();
        BeanUtil.copyProperties(activityEvaluation, vo);

        // 填充用户信息
        Long userId = activityEvaluation.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            if (user != null) {
                vo.setUserName(user.getUserName());
                vo.setUserAvatar(user.getUserAvatar());
            }
        }

        // 填充活动标题
        Long activityId = activityEvaluation.getActivityId();
        if (activityId != null) {
            Activity activity = activityService.getById(activityId);
            if (activity != null) {
                vo.setActivityTitle(activity.getTitle());
            }
        }

        return vo;
    }

    @Override
    public Page<ActivityEvaluationVO> getEvaluationVOPage(Page<ActivityEvaluation> evaluationPage) {
        List<ActivityEvaluation> evaluationList = evaluationPage.getRecords();
        Page<ActivityEvaluationVO> voPage = new Page<>(evaluationPage.getCurrent(),
                evaluationPage.getSize(), evaluationPage.getTotal());

        if (evaluationList == null || evaluationList.isEmpty()) {
            return voPage;
        }

        // 批量查询用户信息
        Set<Long> userIds = evaluationList.stream()
                .map(ActivityEvaluation::getUserId)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 批量查询活动信息
        Set<Long> activityIds = evaluationList.stream()
                .map(ActivityEvaluation::getActivityId)
                .collect(Collectors.toSet());
        Map<Long, Activity> activityMap = activityService.listByIds(activityIds).stream()
                .collect(Collectors.toMap(Activity::getId, activity -> activity));

        // 转换为 VO
        List<ActivityEvaluationVO> voList = evaluationList.stream().map(evaluation -> {
            ActivityEvaluationVO vo = new ActivityEvaluationVO();
            BeanUtil.copyProperties(evaluation, vo);

            User user = userMap.get(evaluation.getUserId());
            if (user != null) {
                vo.setUserName(user.getUserName());
                vo.setUserAvatar(user.getUserAvatar());
            }

            Activity activity = activityMap.get(evaluation.getActivityId());
            if (activity != null) {
                vo.setActivityTitle(activity.getTitle());
            }

            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public Long addEvaluation(EvaluationAddRequest evaluationAddRequest) {
        ThrowUtils.throwIf(evaluationAddRequest == null, ErrorCode.PARAMS_ERROR);

        Long activityId = evaluationAddRequest.getActivityId();
        Integer rating = evaluationAddRequest.getRating();
        String content = evaluationAddRequest.getContent();

        // 校验参数
        ThrowUtils.throwIf(activityId == null || activityId <= 0, ErrorCode.PARAMS_ERROR, "活动ID不合法");
        ThrowUtils.throwIf(rating == null || rating < 1 || rating > 5, ErrorCode.PARAMS_ERROR, "评分必须在1-5之间");
        ThrowUtils.throwIf(StrUtil.isBlank(content), ErrorCode.PARAMS_ERROR, "评价内容不能为空");
        ThrowUtils.throwIf(content.length() > 1024, ErrorCode.PARAMS_ERROR, "评价内容不能超过1024个字符");

        // 校验活动存在且状态为已完成
        Activity activity = activityService.getById(activityId);
        ThrowUtils.throwIf(activity == null, ErrorCode.NOT_FOUND_ERROR, "活动不存在");
        ThrowUtils.throwIf(ActivityStatusEnum.COMPLETED.getValue() != activity.getStatus(),
                ErrorCode.PARAMS_ERROR, "只有已完成的活动才能评价");

        User loginUser = userService.getLoginUser();

        // 校验用户有该活动的已通过报名记录
        QueryWrapper<ActivityRegistration> regQueryWrapper = new QueryWrapper<>();
        regQueryWrapper.eq("activityId", activityId)
                .eq("userId", loginUser.getId())
                .eq("status", RegistrationStatusEnum.APPROVED.getValue());
        ActivityRegistration registration = activityRegistrationService.getOne(regQueryWrapper);
        ThrowUtils.throwIf(registration == null, ErrorCode.PARAMS_ERROR, "您未报名该活动或报名未通过审核");

        // 校验未重复评价
        QueryWrapper<ActivityEvaluation> evalQueryWrapper = new QueryWrapper<>();
        evalQueryWrapper.eq("activityId", activityId)
                .eq("userId", loginUser.getId());
        ActivityEvaluation existingEvaluation = this.getOne(evalQueryWrapper);
        ThrowUtils.throwIf(existingEvaluation != null, ErrorCode.PARAMS_ERROR, "您已评价过该活动，请勿重复评价");

        // 保存评价记录
        ActivityEvaluation evaluation = new ActivityEvaluation();
        evaluation.setActivityId(activityId);
        evaluation.setUserId(loginUser.getId());
        evaluation.setRating(rating);
        evaluation.setContent(content);

        boolean result = this.save(evaluation);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "评价失败");
        return evaluation.getId();
    }

    @Override
    public boolean updateEvaluation(EvaluationUpdateRequest evaluationUpdateRequest) {
        ThrowUtils.throwIf(evaluationUpdateRequest == null, ErrorCode.PARAMS_ERROR);

        Long id = evaluationUpdateRequest.getId();
        Integer rating = evaluationUpdateRequest.getRating();
        String content = evaluationUpdateRequest.getContent();

        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR, "评价ID不合法");
        ThrowUtils.throwIf(rating == null || rating < 1 || rating > 5, ErrorCode.PARAMS_ERROR, "评分必须在1-5之间");
        ThrowUtils.throwIf(StrUtil.isBlank(content), ErrorCode.PARAMS_ERROR, "评价内容不能为空");
        ThrowUtils.throwIf(content.length() > 1024, ErrorCode.PARAMS_ERROR, "评价内容不能超过1024个字符");

        // 校验评价记录存在
        ActivityEvaluation evaluation = this.getById(id);
        ThrowUtils.throwIf(evaluation == null, ErrorCode.NOT_FOUND_ERROR, "评价不存在");

        // 校验是本人的评价
        User loginUser = userService.getLoginUser();
        ThrowUtils.throwIf(!evaluation.getUserId().equals(loginUser.getId()),
                ErrorCode.NOT_AUTH_ERROR, "只能修改自己的评价");

        // 更新评价
        ActivityEvaluation updateEvaluation = new ActivityEvaluation();
        updateEvaluation.setId(id);
        updateEvaluation.setRating(rating);
        updateEvaluation.setContent(content);

        boolean result = this.updateById(updateEvaluation);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "修改评价失败");
        return true;
    }

    @Override
    public boolean deleteEvaluation(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR, "评价ID不合法");

        // 校验评价记录存在
        ActivityEvaluation evaluation = this.getById(id);
        ThrowUtils.throwIf(evaluation == null, ErrorCode.NOT_FOUND_ERROR, "评价不存在");

        // 校验是本人的评价或管理员
        User loginUser = userService.getLoginUser();
        boolean isAdmin = ADMIN_ROLE.equals(loginUser.getUserRole());
        ThrowUtils.throwIf(!evaluation.getUserId().equals(loginUser.getId()) && !isAdmin,
                ErrorCode.NOT_AUTH_ERROR, "只能删除自己的评价");

        // 逻辑删除
        boolean result = this.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "删除评价失败");
        return true;
    }
}
