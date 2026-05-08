package com.ljs.livolunteer.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ljs.livolunteer.annotation.AuthCheck;
import com.ljs.livolunteer.common.BaseResponse;
import com.ljs.livolunteer.common.DeleteRequest;
import com.ljs.livolunteer.common.ResultUtils;
import static com.ljs.livolunteer.constant.UserConstant.*;
import com.ljs.livolunteer.exception.BusinessException;
import com.ljs.livolunteer.exception.ErrorCode;
import com.ljs.livolunteer.exception.ThrowUtils;
import com.ljs.livolunteer.model.dto.registration.RegistrationAddRequest;
import com.ljs.livolunteer.model.dto.registration.RegistrationQueryRequest;
import com.ljs.livolunteer.model.dto.registration.RegistrationReviewRequest;
import com.ljs.livolunteer.model.entity.Activity;
import com.ljs.livolunteer.model.entity.ActivityRegistration;
import com.ljs.livolunteer.model.entity.User;
import com.ljs.livolunteer.model.enums.ActivityStatusEnum;
import com.ljs.livolunteer.model.enums.RegistrationStatusEnum;
import com.ljs.livolunteer.model.vo.ActivityRegistrationVO;
import com.ljs.livolunteer.service.ActivityRegistrationService;
import com.ljs.livolunteer.service.ActivityService;
import com.ljs.livolunteer.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 活动报名接口
 */
@Slf4j
@RestController
@RequestMapping("/registration")
public class ActivityRegistrationController {

    @Resource
    private ActivityRegistrationService activityRegistrationService;

    @Resource
    private ActivityService activityService;

    @Resource
    private UserService userService;

    // region 报名与取消

    /**
     * 用户报名活动
     */
    @AuthCheck(mustRole = "volunteer")
    @PostMapping("/add")
    public BaseResponse<Long> addRegistration(@RequestBody RegistrationAddRequest registrationAddRequest) {
        ThrowUtils.throwIf(registrationAddRequest == null, ErrorCode.PARAMS_ERROR);
        Long activityId = registrationAddRequest.getActivityId();
        ThrowUtils.throwIf(activityId == null || activityId <= 0, ErrorCode.PARAMS_ERROR, "活动ID不合法");
        long registrationId = activityRegistrationService.addRegistration(activityId);
        return ResultUtils.success(registrationId);
    }

    /**
     * 用户取消报名
     */
    @AuthCheck
    @PostMapping("/cancel")
    public BaseResponse<Boolean> cancelRegistration(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        boolean result = activityRegistrationService.cancelRegistration(deleteRequest.getId());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "取消报名失败");
        return ResultUtils.success(result);
    }

    // endregion

    // region 审核

    /**
     * 审核报名（组织者/管理员）
     */
    @AuthCheck(mustRole = ORGANIZER_ROLE)
    @PostMapping("/review")
    public BaseResponse<Boolean> reviewRegistration(@RequestBody RegistrationReviewRequest reviewRequest) {
        ThrowUtils.throwIf(reviewRequest == null, ErrorCode.PARAMS_ERROR);
        boolean result = activityRegistrationService.reviewRegistration(reviewRequest);

        return ResultUtils.success(result);
    }

    // endregion

    // region 查询

    /**
     * 获取报名详情
     */
    @AuthCheck
    @GetMapping("/get/vo")
    public BaseResponse<ActivityRegistrationVO> getRegistrationVOById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        ActivityRegistration registration = activityRegistrationService.getById(id);
        ThrowUtils.throwIf(registration == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(activityRegistrationService.getRegistrationVO(registration));
    }

    /**
     * 按活动分页查询报名列表（组织者查看自己活动的报名 / 管理员可查看所有）
     */
    @AuthCheck(mustRole = ORGANIZER_ROLE)
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<ActivityRegistrationVO>> listRegistrationVOByPage(
            @RequestBody RegistrationQueryRequest registrationQueryRequest) {
        ThrowUtils.throwIf(registrationQueryRequest == null, ErrorCode.PARAMS_ERROR);

        long current = registrationQueryRequest.getCurrent();
        long size = registrationQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "每页条数不能超过20");

        User loginUser = userService.getLoginUser();
        boolean isAdmin = ADMIN_ROLE.equals(loginUser.getUserRole());

        // 组织者只能查看自己创建的活动的报名
        if (!isAdmin) {
            Long activityId = registrationQueryRequest.getActivityId();
            ThrowUtils.throwIf(activityId == null || activityId <= 0, ErrorCode.PARAMS_ERROR, "请指定活动ID");

            Activity activity = activityService.getById(activityId);
            ThrowUtils.throwIf(activity == null, ErrorCode.NOT_FOUND_ERROR, "活动不存在");
            ThrowUtils.throwIf(!activity.getOrganizerId().equals(loginUser.getId()),
                    ErrorCode.NOT_AUTH_ERROR, "只能查看自己创建的活动的报名");
        }

        Page<ActivityRegistration> registrationPage = activityRegistrationService.page(
                new Page<>(current, size),
                activityRegistrationService.getQueryWrapper(registrationQueryRequest)
        );
        return ResultUtils.success(activityRegistrationService.getRegistrationVOPage(registrationPage));
    }

    /**
     * 用户查看自己的报名列表
     */
    @AuthCheck
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<ActivityRegistrationVO>> listMyRegistrationVOByPage(
            @RequestBody RegistrationQueryRequest registrationQueryRequest) {
        ThrowUtils.throwIf(registrationQueryRequest == null, ErrorCode.PARAMS_ERROR);

        // 强制设置 userId 为当前用户
        User loginUser = userService.getLoginUser();
        registrationQueryRequest.setUserId(loginUser.getId());

        long current = registrationQueryRequest.getCurrent();
        long size = registrationQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "每页条数不能超过20");

        Page<ActivityRegistration> registrationPage = activityRegistrationService.page(
                new Page<>(current, size),
                activityRegistrationService.getQueryWrapper(registrationQueryRequest)
        );
        return ResultUtils.success(activityRegistrationService.getRegistrationVOPage(registrationPage));
    }

    // endregion
}
