package com.ljs.livolunteer.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ljs.livolunteer.model.entity.Activity;
import com.ljs.livolunteer.model.enums.ActivityStatusEnum;
import com.ljs.livolunteer.service.ActivityService;
import com.ljs.livolunteer.service.CheckInRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 活动状态自动流转定时任务
 */
@Slf4j
@Component
public class ActivityStatusJob {

    @Resource
    private ActivityService activityService;

    @Resource
    private CheckInRecordService checkInRecordService;

    /**
     * 每分钟执行一次，自动流转活动状态
     */
    @Scheduled(fixedRate = 60000)
    public void updateActivityStatus() {
        Date now = new Date();

        // 已发布 -> 进行中：startTime <= now
        LambdaUpdateWrapper<Activity> toInProgress = new LambdaUpdateWrapper<>();
        toInProgress.eq(Activity::getStatus, ActivityStatusEnum.PUBLISHED.getValue())
                .le(Activity::getStartTime, now)
                .set(Activity::getStatus, ActivityStatusEnum.IN_PROGRESS.getValue());
        boolean updated1 = activityService.update(toInProgress);
        if (updated1) {
            log.info("活动状态流转：已发布 -> 进行中，执行成功");
        }

        // 进行中 -> 已完成：endTime <= now
        // 先查出即将完成的活动，用于自动签退
        QueryWrapper<Activity> completingQuery = new QueryWrapper<>();
        completingQuery.eq("status", ActivityStatusEnum.IN_PROGRESS.getValue())
                .le("endTime", now);
        List<Activity> completingActivities = activityService.list(completingQuery);

        if (completingActivities != null && !completingActivities.isEmpty()) {
            // 自动签退未签退的志愿者并生成志愿时长认证记录
            for (Activity activity : completingActivities) {
                try {
                    checkInRecordService.autoCheckOutByActivity(activity.getId(), activity.getEndTime());
                } catch (Exception e) {
                    log.error("活动 {} 自动签退失败: {}", activity.getId(), e.getMessage());
                }
            }

            // 更新活动状态为已完成
            LambdaUpdateWrapper<Activity> toCompleted = new LambdaUpdateWrapper<>();
            toCompleted.eq(Activity::getStatus, ActivityStatusEnum.IN_PROGRESS.getValue())
                    .le(Activity::getEndTime, now)
                    .set(Activity::getStatus, ActivityStatusEnum.COMPLETED.getValue());
            boolean updated2 = activityService.update(toCompleted);
            if (updated2) {
                log.info("活动状态流转：进行中 -> 已完成，执行成功");
            }
        }
    }
}
