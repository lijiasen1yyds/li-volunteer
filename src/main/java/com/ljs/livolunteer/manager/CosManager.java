package com.ljs.livolunteer.manager;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIUploadResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.ljs.livolunteer.config.CosClientConfig;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

/**
 * COS 对象存储操作管理器
 */
@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    /**
     * 上传文件到 COS
     *
     * @param key  文件在 COS 中的路径（如 /activity/cover/2026-03-13/xxx.jpg）
     * @param file 本地临时文件
     * @return PutObjectResult
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(
                cosClientConfig.getBucket(), key, file
        );
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传图片到 COS（附带数据万象处理）
     *
     * @param key  文件在 COS 中的路径
     * @param file 本地临时文件
     * @return 数据万象处理结果
     */
    public CIUploadResult putPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(
                cosClientConfig.getBucket(), key, file
        );
        // 数据万象图片处理：获取图片基本信息（宽高、格式等）
        PicOperations picOperations = new PicOperations();
        picOperations.setIsPicInfo(1);
        putObjectRequest.setPicOperations(picOperations);
        PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
        return putObjectResult.getCiUploadResult();
    }

    /**
     * 获取文件的完整访问 URL
     *
     * @param key 文件在 COS 中的路径
     * @return 完整 URL
     */
    public String getObjectUrl(String key) {
        return cosClientConfig.getHost() + key;
    }
}
