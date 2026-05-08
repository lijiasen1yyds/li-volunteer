package com.ljs.livolunteer.controller;

import cn.hutool.core.io.FileUtil;
import com.ljs.livolunteer.common.BaseResponse;
import com.ljs.livolunteer.common.ResultUtils;
import com.ljs.livolunteer.exception.BusinessException;
import com.ljs.livolunteer.exception.ErrorCode;
import com.ljs.livolunteer.exception.ThrowUtils;
import com.ljs.livolunteer.manager.CosManager;
import com.ljs.livolunteer.model.entity.Activity;
import com.ljs.livolunteer.model.entity.User;
import com.ljs.livolunteer.model.enums.UserRoleEnum;
import com.ljs.livolunteer.service.ActivityService;
import com.ljs.livolunteer.service.UserService;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.CIUploadResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传接口
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    /**
     * 允许上传的图片后缀
     */
    private static final List<String> ALLOW_IMAGE_SUFFIX = Arrays.asList("jpg", "jpeg", "png", "webp");

    /**
     * 最大文件大小: 2MB
     */
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;

    @Resource
    private CosManager cosManager;

    @Resource
    private UserService userService;

    @Resource
    private ActivityService activityService;

    /**
     * 上传活动封面图片
     *
     * @param file       图片文件
     * @param activityId 活动 ID
     * @return 图片访问 URL
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadImage(@RequestPart("file") MultipartFile file,
                                            @RequestParam("activityId") Long activityId) {
        // 需要登录
        User loginUser = userService.getLoginUser();

        // 校验活动是否存在
        ThrowUtils.throwIf(activityId == null || activityId <= 0, ErrorCode.PARAMS_ERROR);
        Activity activity = activityService.getById(activityId);
        ThrowUtils.throwIf(activity == null, ErrorCode.NOT_FOUND_ERROR, "活动不存在");

        // 仅本人或管理员可上传封面
        if (!activity.getOrganizerId().equals(loginUser.getId())
                && !UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR);
        }

        // 校验文件
        validateFile(file);

        // 生成存储路径：activity/cover/yyyy-MM-dd/uuid.后缀
        String originalFilename = file.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = String.format("activity/cover/%s/%s.%s", datePath, UUID.randomUUID(), suffix);

        // 通过数据万象上传到 COS，获取图片信息
        File tempFile = null;
        String url;
        try {
            tempFile = File.createTempFile("upload_", "." + suffix);
            file.transferTo(tempFile);
            CIUploadResult ciUploadResult = cosManager.putPictureObject(key, tempFile);
            // 从数据万象返回结果中解析图片 URL
            ImageInfo imageInfo = ciUploadResult.getOriginalInfo().getImageInfo();
            // 拼接完整访问 URL
            url = cosManager.getObjectUrl(key);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        } finally {
            if (tempFile != null) {
                boolean deleted = tempFile.delete();
                if (!deleted) {
                    log.warn("临时文件删除失败: {}", tempFile.getAbsolutePath());
                }
            }
        }

        // 更新活动封面图片
        Activity updateActivity = new Activity();
        updateActivity.setId(activityId);
        updateActivity.setCoverImage(url);
        boolean updated = activityService.updateById(updateActivity);
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "更新活动封面失败");

        return ResultUtils.success(url);
    }

    /**
     * 校验上传文件
     */
    private void validateFile(MultipartFile file) {
        ThrowUtils.throwIf(file == null || file.isEmpty(), ErrorCode.PARAMS_ERROR, "文件不能为空");
        ThrowUtils.throwIf(file.getSize() > MAX_FILE_SIZE, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2MB");

        String suffix = FileUtil.getSuffix(file.getOriginalFilename());
        ThrowUtils.throwIf(!ALLOW_IMAGE_SUFFIX.contains(suffix.toLowerCase()),
                ErrorCode.PARAMS_ERROR, "仅支持 jpg、jpeg、png、webp 格式的图片");
    }
}
