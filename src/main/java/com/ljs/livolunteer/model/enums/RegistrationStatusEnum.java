package com.ljs.livolunteer.model.enums;

import lombok.Getter;

/**
 * 报名状态枚举
 */
@Getter
public enum RegistrationStatusEnum {

    PENDING("待审核", 0),
    APPROVED("已通过", 1),
    REJECTED("已拒绝", 2),
    CANCELLED("已取消", 3);

    private final String text;

    private final int value;

    RegistrationStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     */
    public static RegistrationStatusEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (RegistrationStatusEnum statusEnum : RegistrationStatusEnum.values()) {
            if (statusEnum.value == value) {
                return statusEnum;
            }
        }
        return null;
    }
}
