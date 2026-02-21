package com.lghj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lghj.mapper.UserRoleMapper;
import com.lghj.pojo.entity.UserRole;
import com.lghj.service.IUserRoleService;
import org.springframework.stereotype.Service;

@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements IUserRoleService {
}
