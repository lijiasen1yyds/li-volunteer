package com.ljs.livolunteer.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ljs.livolunteer.annotation.AuthCheck;
import com.ljs.livolunteer.common.BaseResponse;
import static com.ljs.livolunteer.constant.UserConstant.*;
import com.ljs.livolunteer.common.DeleteRequest;
import com.ljs.livolunteer.common.ResultUtils;
import com.ljs.livolunteer.exception.BusinessException;
import com.ljs.livolunteer.exception.ErrorCode;
import com.ljs.livolunteer.exception.ThrowUtils;
import com.ljs.livolunteer.manager.CosManager;
import com.ljs.livolunteer.model.dto.activity.ActivityAddRequest;
import com.ljs.livolunteer.model.dto.activity.ActivityQueryRequest;
import com.ljs.livolunteer.model.dto.activity.ActivityReviewRequest;
import com.ljs.livolunteer.model.dto.activity.ActivityUpdateRequest;
import com.ljs.livolunteer.model.enums.ActivityStatusEnum;
import com.ljs.livolunteer.model.entity.Activity;
import com.ljs.livolunteer.model.entity.User;
import com.ljs.livolunteer.model.vo.ActivityVO;
import com.ljs.livolunteer.service.ActivityService;
import com.ljs.livolunteer.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 活动接口
 */
@Slf4j
@RestController
@RequestMapping("/activity")
public class ActivityController {

    @Resource
    private ActivityService activityService;

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

    private static final List<String> ALLOW_IMAGE_SUFFIX = Arrays.asList("jpg", "jpeg", "png", "webp");

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;

    // region 增删改查

    /**
     * 创建活动（可同时上传封面图片）
     */
    @AuthCheck(mustRole = ORGANIZER_ROLE)
    @PostMapping("/add")
    public BaseResponse<Long> addActivity(  ActivityAddRequest activityAddRequest,
                                          @RequestPart(value = "file", required = false) MultipartFile file) {
        ThrowUtils.throwIf(activityAddRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取当前登录用户
        User loginUser = userService.getLoginUser();

        Activity activity = new Activity();
        BeanUtil.copyProperties(activityAddRequest, activity);
        // 设置组织者为当前用户
        activity.setOrganizerId(loginUser.getId());

        // 如果上传了封面图片，通过数据万象上传并解析 URL
        if (file != null && !file.isEmpty()) {
            String coverUrl = uploadCoverImage(file);
            activity.setCoverImage(coverUrl);
        }

        // 参数校验
        activityService.validActivity(activity, true);

        boolean result = activityService.save(activity);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建活动失败");
        return ResultUtils.success(activity.getId());
    }

    /**
     * 删除活动（管理员直接删除，组织者将活动状态改为已取消）
     */
    @AuthCheck
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteActivity(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);

        long id = deleteRequest.getId();

        // 判断活动是否存在
        Activity oldActivity = activityService.getById(id);
        ThrowUtils.throwIf(oldActivity == null, ErrorCode.NOT_FOUND_ERROR);

        User loginUser = userService.getLoginUser();
        String userRole = loginUser.getUserRole();

        if (ADMIN_ROLE.equals(userRole)) {
            // 管理员直接删除
            boolean result = activityService.removeById(id);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "删除活动失败");
        } else if (ORGANIZER_ROLE.equals(userRole)) {
            // 组织者只能删除自己创建的活动
            ThrowUtils.throwIf(!oldActivity.getOrganizerId().equals(loginUser.getId()),
                    ErrorCode.NOT_AUTH_ERROR, "只能删除自己创建的活动");
            // 将活动状态改为已取消(5)
            Activity updateActivity = new Activity();
            updateActivity.setId(id);
            updateActivity.setStatus(ActivityStatusEnum.CANCELLED.getValue());
            boolean result = activityService.updateById(updateActivity);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "删除活动失败");
        } else {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR);
        }

        return ResultUtils.success(true);
    }

    /**
     * 更新活动（可同时更新封面图片）
     */
    @AuthCheck(mustRole = ADMIN_ROLE)
    @PostMapping("/update")
    public BaseResponse<Boolean> updateActivity(  ActivityUpdateRequest activityUpdateRequest,
                                                @RequestPart(value = "file", required = false) MultipartFile file) {
        ThrowUtils.throwIf(activityUpdateRequest == null || activityUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);

        // 判断活动是否存在
        Activity oldActivity = activityService.getById(activityUpdateRequest.getId());
        ThrowUtils.throwIf(oldActivity == null, ErrorCode.NOT_FOUND_ERROR);

        Activity activity = new Activity();
        BeanUtil.copyProperties(activityUpdateRequest, activity);

        // 如果上传了新封面图片，通过数据万象上传并解析 URL
        if (file != null && !file.isEmpty()) {
            String coverUrl = uploadCoverImage(file);
            activity.setCoverImage(coverUrl);
        }

        // 参数校验
        activityService.validActivity(activity, false);

        boolean result = activityService.updateById(activity);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新活动失败");
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取活动（封装类）
     */
    @GetMapping("/get/vo")
    public BaseResponse<ActivityVO> getActivityVOById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Activity activity = activityService.getById(id);
        ThrowUtils.throwIf(activity == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(activityService.getActivityVO(activity));
    }

    /**
     * 分页获取活动列表（封装类）
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<ActivityVO>> listActivityVOByPage(@RequestBody ActivityQueryRequest activityQueryRequest) {
        ThrowUtils.throwIf(activityQueryRequest == null, ErrorCode.PARAMS_ERROR);

        long current = activityQueryRequest.getCurrent();
        long size = activityQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "每页条数不能超过20");

        Page<Activity> activityPage = activityService.page(
                new Page<>(current, size),
                activityService.getQueryWrapper(activityQueryRequest)
        );
        return ResultUtils.success(activityService.getActivityVOPage(activityPage));
    }

    /**
     * 分页获取当前用户创建的活动列表
     */
    @AuthCheck
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<ActivityVO>> listMyActivityVOByPage(@RequestBody ActivityQueryRequest activityQueryRequest) {
        ThrowUtils.throwIf(activityQueryRequest == null, ErrorCode.PARAMS_ERROR);

        User loginUser = userService.getLoginUser();
        activityQueryRequest.setOrganizerId(loginUser.getId());

        long current = activityQueryRequest.getCurrent();
        long size = activityQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "每页条数不能超过20");

        Page<Activity> activityPage = activityService.page(
                new Page<>(current, size),
                activityService.getQueryWrapper(activityQueryRequest)
        );
        return ResultUtils.success(activityService.getActivityVOPage(activityPage));
    }

    // endregion

    // region 审核

    /**
     * 审核活动（仅管理员）
     */
    @AuthCheck(mustRole = ADMIN_ROLE)
    @PostMapping("/review")
    public BaseResponse<Boolean> reviewActivity(@RequestBody ActivityReviewRequest activityReviewRequest) {
        ThrowUtils.throwIf(activityReviewRequest == null, ErrorCode.PARAMS_ERROR);

        Long id = activityReviewRequest.getId();
        Integer status = activityReviewRequest.getStatus();
        String reviewMessage = activityReviewRequest.getReviewMessage();

        // 参数校验
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR, "活动 ID 不合法");
        ThrowUtils.throwIf(status == null || (status != ActivityStatusEnum.PUBLISHED.getValue()
                        && status != ActivityStatusEnum.REJECTED.getValue()),
                ErrorCode.PARAMS_ERROR, "审核状态只能为通过或拒绝");
        // 拒绝时审核意见不能为空
        if (ActivityStatusEnum.REJECTED.getValue() == status) {
            ThrowUtils.throwIf(reviewMessage == null || reviewMessage.trim().isEmpty(),
                    ErrorCode.PARAMS_ERROR, "拒绝时必须填写审核意见");
        }

        // 校验活动是否存在
        Activity activity = activityService.getById(id);
        ThrowUtils.throwIf(activity == null, ErrorCode.NOT_FOUND_ERROR, "活动不存在");

        // 只有待审核的活动才能被审核
        ThrowUtils.throwIf(ActivityStatusEnum.PENDING.getValue() != activity.getStatus(),
                ErrorCode.PARAMS_ERROR, "该活动当前状态不允许审核");

        // 获取当前管理员
        User loginUser = userService.getLoginUser();

        // 更新审核信息
        Activity updateActivity = new Activity();
        updateActivity.setId(id);
        updateActivity.setStatus(status);
        updateActivity.setReviewMessage(reviewMessage);
        updateActivity.setReviewerId(loginUser.getId());
        updateActivity.setReviewTime(new Date());

        boolean result = activityService.updateById(updateActivity);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "审核操作失败");
        return ResultUtils.success(true);
    }

    // endregion

    /**
     * 上传封面图片到 COS（数据万象），返回图片访问 URL
     */
    private String uploadCoverImage(MultipartFile file) {
        // 校验文件
        ThrowUtils.throwIf(file.getSize() > MAX_FILE_SIZE, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2MB");
        String suffix = FileUtil.getSuffix(file.getOriginalFilename());
        ThrowUtils.throwIf(!ALLOW_IMAGE_SUFFIX.contains(suffix.toLowerCase()),
                ErrorCode.PARAMS_ERROR, "仅支持 jpg、jpeg、png、webp 格式的图片");

        // 生成存储路径
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = String.format("/activity/cover/%s/%s.%s", datePath, UUID.randomUUID(), suffix);

        // 通过数据万象上传
        File tempFile = null;
        try {
            tempFile = File.createTempFile("upload_", "." + suffix);
            file.transferTo(tempFile);
            cosManager.putPictureObject(key, tempFile);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "封面图片上传失败");
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
        return cosManager.getObjectUrl(key);
    }
}
