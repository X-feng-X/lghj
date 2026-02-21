package com.lghj.enums;

import lombok.Getter;

/**
 * 用户身份标识转换类
 */
@Getter
public enum UserType2Num {
    COMMON((short) 1, "普通用户"),
    INSTITUTIONAL((short) 2, "机构用户"),
    ADMINISTRATOR((short) 3, "管理员");

    private Short code;
    private String name;

    UserType2Num(Short code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getName(Short code) {
        if (code == null) {
            return null;
        }
        for (UserType2Num info : UserType2Num.values()) {
            if (info.getCode().equals(code)) {
                return info.getName();
            }
        }
        return null;
    }
}
