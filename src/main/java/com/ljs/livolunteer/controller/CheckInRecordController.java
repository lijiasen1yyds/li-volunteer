package com.ljs.livolunteer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ljs.livolunteer.annotation.AuthCheck;
import com.ljs.livolunteer.common.BaseResponse;
import com.ljs.livolunteer.common.ResultUtils;
import com.ljs.livolunteer.exception.ErrorCode;
import com.ljs.livolunteer.exception.ThrowUtils;
import com.ljs.livolunteer.model.dto.checkin.CheckInQueryRequest;
import com.ljs.livolunteer.model.dto.checkin.CheckInRequest;
import com.ljs.livolunteer.model.dto.checkin.CheckOutRequest;
import com.ljs.livolunteer.model.entity.Activity;
import com.ljs.livolunteer.model.entity.CheckInRecord;
import com.ljs.livolunteer.model.entity.User;
import com.ljs.livolunteer.model.vo.CheckInRecordVO;
import com.ljs.livolunteer.service.ActivityService;
import com.ljs.livolunteer.service.CheckInRecordService;
import com.ljs.livolunteer.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static com.ljs.livolunteer.constant.UserConstant.*;

/**
 * 签到接口
 */
@Slf4j
@RestController
@RequestMapping("/checkIn")
public class CheckInRecordController {

    @Resource
    private CheckInRecordService checkInRecordService;

    @Resource
    private ActivityService activityService;

    @Resource
    private UserService userService;

    // region 签到与签退

    /**
     * 志愿者签到
     */
    @AuthCheck(mustRole = VOLUNTEER_ROLE)
    @PostMapping("/do")
    public BaseResponse<Long> doCheckIn(@RequestBody CheckInRequest checkInRequest) {
        ThrowUtils.throwIf(checkInRequest == null, ErrorCode.PARAMS_ERROR);
        Long activityId = checkInRequest.getActivityId();
        ThrowUtils.throwIf(activityId == null || activityId <= 0, ErrorCode.PARAMS_ERROR, "活动ID不合法");
        Long checkInRecordId = checkInRecordService.checkIn(activityId);
        return ResultUtils.success(checkInRecordId);
    }

    /**
     * 志愿者签退
     */
    @AuthCheck(mustRole = VOLUNTEER_ROLE)
    @PostMapping("/doCheckOut")
    public BaseResponse<Boolean> doCheckOut(@RequestBody CheckOutRequest checkOutRequest) {
        ThrowUtils.throwIf(checkOutRequest == null, ErrorCode.PARAMS_ERROR);
        Long activityId = checkOutRequest.getActivityId();
        ThrowUtils.throwIf(activityId == null || activityId <= 0, ErrorCode.PARAMS_ERROR, "活动ID不合法");
        boolean result = checkInRecordService.checkOut(activityId);
        return ResultUtils.success(result);
    }

    // endregion

    // region 查询

    /**
     * 获取签到详情
     */
    @AuthCheck
    @GetMapping("/get/vo")
    public BaseResponse<CheckInRecordVO> getCheckInRecordVOById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        CheckInRecord checkInRecord = checkInRecordService.getById(id);
        ThrowUtils.throwIf(checkInRecord == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(checkInRecordService.getCheckInRecordVO(checkInRecord));
    }

    /**
     * 组织者查看活动签到列表（组织者只能查看自己活动 / 管理员可查看所有）
     */
    @AuthCheck(mustRole = ORGANIZER_ROLE)
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<CheckInRecordVO>> listCheckInRecordVOByPage(
            @RequestBody CheckInQueryRequest checkInQueryRequest) {
        ThrowUtils.throwIf(checkInQueryRequest == null, ErrorCode.PARAMS_ERROR);

        long current = checkInQueryRequest.getCurrent();
        long size = checkInQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "每页条数不能超过20");

        User loginUser = userService.getLoginUser();
        boolean isAdmin = ADMIN_ROLE.equals(loginUser.getUserRole());

        // 组织者只能查看自己创建的活动的签到记录
        if (!isAdmin) {
            Long activityId = checkInQueryRequest.getActivityId();
            ThrowUtils.throwIf(activityId == null || activityId <= 0, ErrorCode.PARAMS_ERROR, "请指定活动ID");

            Activity activity = activityService.getById(activityId);
            ThrowUtils.throwIf(activity == null, ErrorCode.NOT_FOUND_ERROR, "活动不存在");
            ThrowUtils.throwIf(!activity.getOrganizerId().equals(loginUser.getId()),
                    ErrorCode.NOT_AUTH_ERROR, "只能查看自己创建的活动的签到记录");
        }

        Page<CheckInRecord> checkInRecordPage = checkInRecordService.page(
                new Page<>(current, size),
                checkInRecordService.getQueryWrapper(checkInQueryRequest)
        );
        return ResultUtils.success(checkInRecordService.getCheckInRecordVOPage(checkInRecordPage));
    }

    /**
     * 用户查看自己的签到记录
     */
    @AuthCheck
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<CheckInRecordVO>> listMyCheckInRecordVOByPage(
            @RequestBody CheckInQueryRequest checkInQueryRequest) {
        ThrowUtils.throwIf(checkInQueryRequest == null, ErrorCode.PARAMS_ERROR);

        // 强制设置 userId 为当前用户
        User loginUser = userService.getLoginUser();
        checkInQueryRequest.setUserId(loginUser.getId());

        long current = checkInQueryRequest.getCurrent();
        long size = checkInQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "每页条数不能超过20");

        Page<CheckInRecord> checkInRecordPage = checkInRecordService.page(
                new Page<>(current, size),
                checkInRecordService.getQueryWrapper(checkInQueryRequest)
        );
        return ResultUtils.success(checkInRecordService.getCheckInRecordVOPage(checkInRecordPage));
    }

    // endregion
}
