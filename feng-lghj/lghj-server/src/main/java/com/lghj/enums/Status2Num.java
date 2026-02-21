package com.lghj.enums;

import lombok.Getter;

/**
 * 状态枚举类
 */
@Getter
public enum Status2Num {

    DISABLED((short) 0, "禁用"),
    NORMAL((short) 1, "正常");

    private Short code;
    private String name;

    Status2Num(Short code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getName(Short code) {
        if (code == null) {
            return null;
        }
        for (Status2Num info : Status2Num.values()) {
            if (info.getCode().equals(code)) {
                return info.getName();
            }
        }
        return null;
    }
}
