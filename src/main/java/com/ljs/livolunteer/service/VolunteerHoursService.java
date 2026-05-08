package com.ljs.livolunteer.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ljs.livolunteer.model.dto.volunteerhours.VolunteerHoursQueryRequest;
import com.ljs.livolunteer.model.dto.volunteerhours.VolunteerHoursReviewRequest;
import com.ljs.livolunteer.model.entity.VolunteerHours;
import com.ljs.livolunteer.model.vo.VolunteerHoursVO;

import java.math.BigDecimal;

/**
 * 志愿时长认证服务
 */
public interface VolunteerHoursService extends IService<VolunteerHours> {

    /**
     * 构造查询条件
     */
    QueryWrapper<VolunteerHours> getQueryWrapper(VolunteerHoursQueryRequest volunteerHoursQueryRequest);

    /**
     * 获取志愿时长认证视图对象（单条）
     */
    VolunteerHoursVO getVolunteerHoursVO(VolunteerHours volunteerHours);

    /**
     * 获取志愿时长认证视图对象（分页）
     */
    Page<VolunteerHoursVO> getVolunteerHoursVOPage(Page<VolunteerHours> volunteerHoursPage);

    /**
     * 在签退后生成待认证记录
     *
     * @param activityId 活动ID
     * @param userId 用户ID
     * @param actualHours 实际签到时长
     * @return 是否成功
     */
    boolean savePendingVolunteerHours(Long activityId, Long userId, BigDecimal actualHours);

    /**
     * 审核志愿时长认证
     *
     * @param reviewRequest 审核请求
     * @return 是否成功
     */
    boolean reviewVolunteerHours(VolunteerHoursReviewRequest reviewRequest);
}
