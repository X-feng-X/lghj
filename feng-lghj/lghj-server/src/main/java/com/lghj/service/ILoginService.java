package com.lghj.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lghj.pojo.dto.LoginDTO;
import com.lghj.pojo.entity.User;

public interface ILoginService extends IService<User> {
    User login(LoginDTO loginDTO);
}
