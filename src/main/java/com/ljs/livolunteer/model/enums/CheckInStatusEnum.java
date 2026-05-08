package com.ljs.livolunteer.model.enums;

import lombok.Getter;

/**
 * 签到状态枚举
 */
@Getter
public enum CheckInStatusEnum {

    CHECKED_IN("已签到", 0),
    CHECKED_OUT("已签退", 1);

    private final String text;

    private final int value;

    CheckInStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     */
    public static CheckInStatusEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (CheckInStatusEnum statusEnum : CheckInStatusEnum.values()) {
            if (statusEnum.value == value) {
                return statusEnum;
            }
        }
        return null;
    }
}
