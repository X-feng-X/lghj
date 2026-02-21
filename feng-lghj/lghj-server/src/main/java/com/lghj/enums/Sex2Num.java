package com.lghj.enums;

import lombok.Getter;

/**
 * 性别转换类
 */
@Getter
public enum Sex2Num {

    MAN((short) 1, "男"),
    WOMAN((short) 2, "女");

    private Short code;
    private String name;

    Sex2Num(Short code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getName(Short code) {
        if (code == null) {
            return null;
        }
        for (Sex2Num info : Sex2Num.values()) {
            if (info.getCode().equals(code)) {
                return info.getName();
            }
        }
        return null;
    }
}
