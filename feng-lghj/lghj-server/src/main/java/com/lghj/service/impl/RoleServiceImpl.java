package com.lghj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lghj.mapper.RoleMapper;
import com.lghj.pojo.entity.Role;
import com.lghj.service.IRoleService;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl  extends ServiceImpl<RoleMapper, Role> implements IRoleService {
}
