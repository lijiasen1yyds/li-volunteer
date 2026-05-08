package com.ljs.livolunteer.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ljs.livolunteer.model.dto.registration.RegistrationQueryRequest;
import com.ljs.livolunteer.model.dto.registration.RegistrationReviewRequest;
import com.ljs.livolunteer.model.entity.ActivityRegistration;
import com.ljs.livolunteer.model.vo.ActivityRegistrationVO;

/**
 * 活动报名服务
 */
public interface ActivityRegistrationService extends IService<ActivityRegistration> {

    /**
     * 构建查询条件
     */
    QueryWrapper<ActivityRegistration> getQueryWrapper(RegistrationQueryRequest registrationQueryRequest);

    /**
     * 单条报名记录转 VO
     */
    ActivityRegistrationVO getRegistrationVO(ActivityRegistration activityRegistration);

    /**
     * 分页报名记录转 VO
     */
    Page<ActivityRegistrationVO> getRegistrationVOPage(Page<ActivityRegistration> registrationPage);
    /**
     * 取消报名
     */
    boolean cancelRegistration(long id);

    /**
     * 添加报名
     */
    Long addRegistration(long activityId);

    /**
     * 审核报名
     */
    boolean reviewRegistration(RegistrationReviewRequest registrationReviewRequest);
}
