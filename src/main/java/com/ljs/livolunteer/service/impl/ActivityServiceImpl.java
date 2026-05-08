package com.ljs.livolunteer.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ljs.livolunteer.constant.CommonConstant;
import com.ljs.livolunteer.exception.BusinessException;
import com.ljs.livolunteer.exception.ErrorCode;
import com.ljs.livolunteer.exception.ThrowUtils;
import com.ljs.livolunteer.mapper.ActivityMapper;
import com.ljs.livolunteer.model.dto.activity.ActivityQueryRequest;
import com.ljs.livolunteer.model.entity.Activity;
import com.ljs.livolunteer.model.entity.User;
import com.ljs.livolunteer.model.vo.ActivityVO;
import com.ljs.livolunteer.service.ActivityService;
import com.ljs.livolunteer.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 活动服务实现
 */
@Slf4j
@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements ActivityService {

    @Resource
    private UserService userService;

    @Override
    public void validActivity(Activity activity, boolean add) {
        ThrowUtils.throwIf(activity == null, ErrorCode.PARAMS_ERROR);

        String title = activity.getTitle();
        String description = activity.getDescription();

        // 创建时必须有标题、开始时间、结束时间
        if (add) {
            ThrowUtils.throwIf(StrUtil.isBlank(title), ErrorCode.PARAMS_ERROR, "活动标题不能为空");
            ThrowUtils.throwIf(activity.getStartTime() == null, ErrorCode.PARAMS_ERROR, "活动开始时间不能为空");
            ThrowUtils.throwIf(activity.getEndTime() == null, ErrorCode.PARAMS_ERROR, "活动结束时间不能为空");
        }

        // 通用校验
        if (StrUtil.isNotBlank(title)) {
            ThrowUtils.throwIf(title.length() > 256, ErrorCode.PARAMS_ERROR, "活动标题过长");
        }
        if (StrUtil.isNotBlank(description)) {
            ThrowUtils.throwIf(description.length() > 10000, ErrorCode.PARAMS_ERROR, "活动描述过长");
        }
        if (activity.getStartTime() != null && activity.getEndTime() != null) {
            ThrowUtils.throwIf(activity.getStartTime().after(activity.getEndTime()),
                    ErrorCode.PARAMS_ERROR, "活动开始时间不能晚于结束时间");
        }
    }

    @Override
    public QueryWrapper<Activity> getQueryWrapper(ActivityQueryRequest activityQueryRequest) {
        ThrowUtils.throwIf(activityQueryRequest == null, ErrorCode.PARAMS_ERROR);

        String title = activityQueryRequest.getTitle();
        String category = activityQueryRequest.getCategory();
        Integer status = activityQueryRequest.getStatus();
        Long organizerId = activityQueryRequest.getOrganizerId();
        String sortField = activityQueryRequest.getSortField();
        String sortOrder = activityQueryRequest.getSortOrder();

        QueryWrapper<Activity> queryWrapper = new QueryWrapper<>();
        // 标题模糊搜索
        queryWrapper.like(StrUtil.isNotBlank(title), "title", title);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(status != null, "status", status);
        queryWrapper.eq(organizerId != null, "organizerId", organizerId);

        // 排序
        queryWrapper.orderBy(StrUtil.isNotBlank(sortField),
                CommonConstant.SORT_ORDER_ASC.equals(sortOrder), sortField);

        return queryWrapper;
    }

    @Override
    public ActivityVO getActivityVO(Activity activity) {
        if (activity == null) {
            return null;
        }
        ActivityVO activityVO = new ActivityVO();
        BeanUtil.copyProperties(activity, activityVO);

        // 填充组织者名称
        Long organizerId = activity.getOrganizerId();
        if (organizerId != null) {
            User organizer = userService.getById(organizerId);
            if (organizer != null) {
                activityVO.setOrganizerName(organizer.getUserName());
            }
        }
        return activityVO;
    }

    @Override
    public Page<ActivityVO> getActivityVOPage(Page<Activity> activityPage) {
        List<Activity> activityList = activityPage.getRecords();
        Page<ActivityVO> activityVOPage = new Page<>(activityPage.getCurrent(), activityPage.getSize(), activityPage.getTotal());

        if (activityList == null || activityList.isEmpty()) {
            return activityVOPage;
        }

        // 批量查询组织者信息
        Set<Long> organizerIds = activityList.stream()
                .map(Activity::getOrganizerId)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = userService.listByIds(organizerIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 转换为VO
        List<ActivityVO> activityVOList = activityList.stream().map(activity -> {
            ActivityVO activityVO = new ActivityVO();
            BeanUtil.copyProperties(activity, activityVO);
            User organizer = userMap.get(activity.getOrganizerId());
            if (organizer != null) {
                activityVO.setOrganizerName(organizer.getUserName());
            }
            return activityVO;
        }).collect(Collectors.toList());

        activityVOPage.setRecords(activityVOList);
        return activityVOPage;
    }
}
