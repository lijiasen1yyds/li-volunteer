package com.ljs.livolunteer.model.enums;

import lombok.Getter;

/**
 * 活动类别枚举
 */
@Getter
public enum ActivityCategoryEnum {

    COMMUNITY("社区帮扶", "community"),
    COMPETITION("赛事保障", "competition"),
    ENVIRONMENT("环保宣传", "environment"),
    EDUCATION("教育支持", "education"),
    OTHER("其他", "other");

    private final String text;

    private final String value;

    ActivityCategoryEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     */
    public static ActivityCategoryEnum getEnumByValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        for (ActivityCategoryEnum categoryEnum : ActivityCategoryEnum.values()) {
            if (categoryEnum.value.equals(value)) {
                return categoryEnum;
            }
        }
        return null;
    }
}
