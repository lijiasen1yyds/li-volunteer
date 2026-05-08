package com.ljs.livolunteer.model.enums;

import lombok.Getter;

/**
 * 志愿时长认证状态枚举
 */
@Getter
public enum VolunteerHoursStatusEnum {

    PENDING("待认证", 0),
    CERTIFIED("已认证", 1),
    REJECTED("认证拒绝", 2);

    private final String text;

    private final int value;

    VolunteerHoursStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     */
    public static VolunteerHoursStatusEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (VolunteerHoursStatusEnum statusEnum : VolunteerHoursStatusEnum.values()) {
            if (statusEnum.value == value) {
                return statusEnum;
            }
        }
        return null;
    }
}
