package com.ljs.livolunteer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ljs.livolunteer.annotation.AuthCheck;
import com.ljs.livolunteer.common.BaseResponse;
import com.ljs.livolunteer.common.ResultUtils;
import com.ljs.livolunteer.exception.ErrorCode;
import com.ljs.livolunteer.exception.ThrowUtils;
import com.ljs.livolunteer.model.dto.volunteerhours.VolunteerHoursQueryRequest;
import com.ljs.livolunteer.model.dto.volunteerhours.VolunteerHoursReviewRequest;
import com.ljs.livolunteer.model.entity.Activity;
import com.ljs.livolunteer.model.entity.User;
import com.ljs.livolunteer.model.entity.VolunteerHours;
import com.ljs.livolunteer.model.vo.VolunteerHoursVO;
import com.ljs.livolunteer.service.ActivityService;
import com.ljs.livolunteer.service.UserService;
import com.ljs.livolunteer.service.VolunteerHoursService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static com.ljs.livolunteer.constant.UserConstant.ADMIN_ROLE;
import static com.ljs.livolunteer.constant.UserConstant.ORGANIZER_ROLE;

/**
 * 志愿时长认证接口
 */
@Slf4j
@RestController
@RequestMapping("/volunteerHours")
public class VolunteerHoursController {

    @Resource
    private VolunteerHoursService volunteerHoursService;

    @Resource
    private UserService userService;

    @Resource
    private ActivityService activityService;

    /**
     * 获取志愿时长认证详情
     */
    @AuthCheck
    @GetMapping("/get/vo")
    public BaseResponse<VolunteerHoursVO> getVolunteerHoursVOById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);

        VolunteerHours volunteerHours = volunteerHoursService.getById(id);
        ThrowUtils.throwIf(volunteerHours == null, ErrorCode.NOT_FOUND_ERROR);

        User loginUser = userService.getLoginUser();
        boolean isAdmin = ADMIN_ROLE.equals(loginUser.getUserRole());
        if (!isAdmin && !volunteerHours.getUserId().equals(loginUser.getId())) {
            Activity activity = activityService.getById(volunteerHours.getActivityId());
            ThrowUtils.throwIf(activity == null, ErrorCode.NOT_FOUND_ERROR, "关联活动不存在");
            ThrowUtils.throwIf(!activity.getOrganizerId().equals(loginUser.getId()),
                    ErrorCode.NOT_AUTH_ERROR, "无权查看该认证记录");
        }

        return ResultUtils.success(volunteerHoursService.getVolunteerHoursVO(volunteerHours));
    }

    /**
     * 审核志愿时长认证
     */
    @AuthCheck(mustRole = ORGANIZER_ROLE)
    @PostMapping("/review")
    public BaseResponse<Boolean> reviewVolunteerHours(@RequestBody VolunteerHoursReviewRequest reviewRequest) {
        ThrowUtils.throwIf(reviewRequest == null, ErrorCode.PARAMS_ERROR);
        boolean result = volunteerHoursService.reviewVolunteerHours(reviewRequest);
        return ResultUtils.success(result);
    }

    /**
     * 分页查询志愿时长认证列表（组织者仅可查看自己活动）
     */
    @AuthCheck(mustRole = ORGANIZER_ROLE)
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<VolunteerHoursVO>> listVolunteerHoursVOByPage(
            @RequestBody VolunteerHoursQueryRequest volunteerHoursQueryRequest) {
        ThrowUtils.throwIf(volunteerHoursQueryRequest == null, ErrorCode.PARAMS_ERROR);

        long current = volunteerHoursQueryRequest.getCurrent();
        long size = volunteerHoursQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "每页条数不能超过20");

        User loginUser = userService.getLoginUser();
        boolean isAdmin = ADMIN_ROLE.equals(loginUser.getUserRole());
        if (!isAdmin) {
            Long activityId = volunteerHoursQueryRequest.getActivityId();
            ThrowUtils.throwIf(activityId == null || activityId <= 0,
                    ErrorCode.PARAMS_ERROR, "请指定活动ID");

            Activity activity = activityService.getById(activityId);
            ThrowUtils.throwIf(activity == null, ErrorCode.NOT_FOUND_ERROR, "活动不存在");
            ThrowUtils.throwIf(!activity.getOrganizerId().equals(loginUser.getId()),
                    ErrorCode.NOT_AUTH_ERROR, "只能查看自己创建的活动认证记录");
        }

        Page<VolunteerHours> volunteerHoursPage = volunteerHoursService.page(
                new Page<>(current, size),
                volunteerHoursService.getQueryWrapper(volunteerHoursQueryRequest)
        );
        return ResultUtils.success(volunteerHoursService.getVolunteerHoursVOPage(volunteerHoursPage));
    }

    /**
     * 当前用户查看自己的志愿时长认证记录
     */
    @AuthCheck
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<VolunteerHoursVO>> listMyVolunteerHoursVOByPage(
            @RequestBody VolunteerHoursQueryRequest volunteerHoursQueryRequest) {
        ThrowUtils.throwIf(volunteerHoursQueryRequest == null, ErrorCode.PARAMS_ERROR);

        User loginUser = userService.getLoginUser();
        volunteerHoursQueryRequest.setUserId(loginUser.getId());

        long current = volunteerHoursQueryRequest.getCurrent();
        long size = volunteerHoursQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "每页条数不能超过20");

        Page<VolunteerHours> volunteerHoursPage = volunteerHoursService.page(
                new Page<>(current, size),
                volunteerHoursService.getQueryWrapper(volunteerHoursQueryRequest)
        );
        return ResultUtils.success(volunteerHoursService.getVolunteerHoursVOPage(volunteerHoursPage));
    }
}
