package com.ljs.livolunteer.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.ljs.livolunteer.config.CosClientConfig;
import com.ljs.livolunteer.exception.BusinessException;
import com.ljs.livolunteer.exception.ErrorCode;
import com.ljs.livolunteer.exception.ThrowUtils;
import com.ljs.livolunteer.manager.CosManager;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class FileActivityUpload {
    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;


    public void processSource(Object inputSource, File file) throws IOException {
        String fileUrl = (String) inputSource;
        //下载文件到临时文件
        HttpUtil.downloadFile(fileUrl, file);
    }


    public String getOriginalFilename(Object inputSource) {
        String fileUrl = (String) inputSource;
        String originalFilename = FileUtil.mainName(fileUrl);
        return originalFilename;
    }

    public void validPicture(Object inputSource) {
        String fileUrl = (String) inputSource;
        //1.校验参数为空
        ThrowUtils.throwIf(fileUrl == null, new BusinessException(ErrorCode.PARAMS_ERROR, "图片Url不能为空"));
        //2、校验Url格式
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片Url格式错误");
        }
        //3.校验Url协议
        ThrowUtils.throwIf(!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://"),
                ErrorCode.PARAMS_ERROR, "仅支持HTTP活HTttps");
        //4、发送HEAD请求,检验文件是否存在
        HttpResponse response = null;
        try {
            response = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
            //如果为正常返回，无需执行其他判断
            if (response.getStatus() != HttpStatus.HTTP_OK) {
                return;
            }
            //若正常返回则需要判断文件类型
            String contentType = response.header("Content-Type");
            if (StrUtil.isNotBlank(contentType)) {
                //允许图片列表为
                final List<String> ALLOW_FORMAT_LIST = Arrays.asList("image/jpeg", "image/png",
                        "image/jpg", "image/webp");
                ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(contentType),
                        ErrorCode.PARAMS_ERROR, "仅支持JPG、PNG、JPEG、WEBP格式");
            }
            //5.校验文件大小
            String contentLength = response.header("Content-Length");
            if (StrUtil.isNotBlank(contentLength)) {
                //定义文件大小为2MB
                final long ONE_M = 1024 * 1024;
                ThrowUtils.throwIf(Long.parseLong(contentLength) > 2 * ONE_M,
                        ErrorCode.PARAMS_ERROR, "文件大小不能超过2M");
            }
        } finally {
            if (response != null) {
                response.close();
            }
        }

    }
}
