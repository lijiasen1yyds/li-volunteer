package com.ljs.livolunteer.model.enums;

import lombok.Getter;
import org.springframework.util.ObjectUtils;

/**
 * 活动状态枚举
 */
@Getter
public enum ActivityStatusEnum {

    PENDING("待审核", 0),
    PUBLISHED("已发布", 1),
    REJECTED("审核拒绝", 2),
    IN_PROGRESS("进行中", 3),
    COMPLETED("已完成", 4),
    CANCELLED("已取消", 5);

    private final String text;

    private final int value;

    ActivityStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     */
    public static ActivityStatusEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (ActivityStatusEnum statusEnum : ActivityStatusEnum.values()) {
            if (statusEnum.value == value) {
                return statusEnum;
            }
        }
        return null;
    }
}
