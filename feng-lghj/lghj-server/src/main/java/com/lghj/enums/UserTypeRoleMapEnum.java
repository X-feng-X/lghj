package com.lghj.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * userType 与角色表的映射枚举
 * 确保 userType 的值和 role_id 精准对应
 */
@Getter
public enum UserTypeRoleMapEnum {
    // userType = 1 → 普通用户（role_id=1）
    NORMAL_USER((short) 1, 1, "普通用户"),
    // userType = 2 → 机构用户（role_id=2）
    ORG_USER((short) 2, 2, "机构用户"),
    // userType = 3 → 超级管理员（role_id=3）
    SUPER_ADMIN((short) 3, 3, "超级管理员"),
    // userType = 4 → 系统管理员（role_id=4）
    SYSTEM_ADMIN((short) 3, 4, "系统管理员"),
    // userType = 5 → 运营管理员（role_id=5）
    OPERATION_ADMIN((short) 3, 5, "运营管理员"),
    // userType = 6 → 内容管理员（role_id=6）
    CONTENT_ADMIN((short) 3, 6, "内容管理员");

    // userType 的值
    private final Short userType;
    // 对应的角色表 ID
    private final Integer roleId;
    // 角色名称
    private final String roleName;

    // 构造器
    UserTypeRoleMapEnum(Short userType, Integer roleId, String roleName) {
        this.userType = userType;
        this.roleId = roleId;
        this.roleName = roleName;
    }

    // 根据 userType 获取对应的枚举项
    public static Integer getByUserType(Short userType) {
        for (UserTypeRoleMapEnum item : values()) {
            if (item.getUserType().equals(userType)) {
                return item.getRoleId();
            }
        }
        return null; // 无匹配时返回null，后续抛出异常
    }

    // 判读角色是否存在
    public static boolean isAdminRole(Integer roleId) {
        if (roleId == null) {
            return false;
        }

        return Arrays.stream(values())
                .anyMatch(item -> item.getRoleId().equals(roleId)
                        && !item.getRoleId().equals(1)
                        && !item.getRoleId().equals(2));
    }
}