package com.lghj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lghj.constant.StatusConstant;
import com.lghj.enums.ErrorEnum;
import com.lghj.exception.BusinessException;
import com.lghj.mapper.UserMapper;
import com.lghj.pojo.dto.LoginDTO;
import com.lghj.pojo.entity.User;
import com.lghj.service.ILoginService;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class LoginServiceImpl extends ServiceImpl<UserMapper, User> implements ILoginService {

    /**
     * 用户登录
     */
    @Override
    public User login(LoginDTO loginDTO) {
        // 从登录数据传输对象中获取用户名和密码
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();

        // 根据用户名查询数据库中的数据
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .select(User::getId, User::getPassword, User::getUserType, User::getStatus)
                .eq(User::getUsername, username);
        User user = baseMapper.selectOne(wrapper);

        // 处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (user == null) {
            //账号不存在
            throw new BusinessException(ErrorEnum.USER_NOT_FOUND);
        }

        // 密码比对
        // 对前端传过来的明文密码进行md5加密处理
//        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(user.getPassword())) {
            //密码错误
            throw new BusinessException(ErrorEnum.PASSWORD_ERROR);
        }

        if (Objects.equals(user.getStatus(), StatusConstant.DISABLE)) {
            //账号被锁定
            throw new BusinessException(ErrorEnum.ACCOUNT_LOCKED);
        }

        // 返回实体对象
        return user;
    }
}
