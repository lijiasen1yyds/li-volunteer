package com.ljs.livolunteer.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ljs.livolunteer.model.dto.activity.ActivityQueryRequest;
import com.ljs.livolunteer.model.entity.Activity;
import com.ljs.livolunteer.model.vo.ActivityVO;

/**
 * 活动服务接口
 */
public interface ActivityService extends IService<Activity> {

    /**
     * 校验活动参数
     *
     * @param activity 活动实体
     * @param add      是否为创建操作
     */
    void validActivity(Activity activity, boolean add);

    /**
     * 获取查询条件
     *
     * @param activityQueryRequest 查询请求
     * @return QueryWrapper
     */
    QueryWrapper<Activity> getQueryWrapper(ActivityQueryRequest activityQueryRequest);

    /**
     * 获取活动视图对象
     *
     * @param activity 活动实体
     * @return 活动VO
     */
    ActivityVO getActivityVO(Activity activity);

    /**
     * 分页获取活动视图对象
     *
     * @param activityPage 活动分页
     * @return 活动VO分页
     */
    Page<ActivityVO> getActivityVOPage(Page<Activity> activityPage);
}
