package com.lghj.utils;

import com.lghj.enums.Sex2Num;
import com.lghj.enums.Status2Num;
import com.lghj.enums.UserType2Num;

/**
 * 枚举转换工具类
 */
public class EnumConvertUtil {
    // 转换用户状态
    public static String convertState(Short state) {
        String name = Status2Num.getName(state);
        return name == null ? "未知状态" : name;
    }

    // 转换用户类型
    public static String convertUserType(Short userType) {
        String name = UserType2Num.getName(userType);
        return name == null ? "未知状态" : name;
    }

    // 转换用户性别
    public static String convertSex(Short sex) {
        String name = Sex2Num.getName(sex);
        return name == null ? "未知状态" : name;
    }
}
