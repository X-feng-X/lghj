package com.lghj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lghj.constant.StatusConstant;
import com.lghj.enums.ErrorEnum;
import com.lghj.enums.UserType2Num;
import com.lghj.exception.BusinessException;
import com.lghj.mapper.UserMapper;
import com.lghj.pojo.dto.LoginDTO;
import com.lghj.pojo.dto.RegisterDTO;
import com.lghj.pojo.entity.User;
import com.lghj.service.ILoginService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class LoginServiceImpl extends ServiceImpl<UserMapper, User> implements ILoginService {

    @Override
    public User login(LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .select(User::getId, User::getPassword, User::getUserType, User::getStatus, User::getUsername)
                .eq(User::getUsername, username);
        User user = baseMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException(ErrorEnum.USER_NOT_FOUND);
        }

        if (!password.equals(user.getPassword())) {
            throw new BusinessException(ErrorEnum.PASSWORD_ERROR);
        }

        if (Objects.equals(user.getStatus(), StatusConstant.DISABLE)) {
            throw new BusinessException(ErrorEnum.ACCOUNT_LOCKED);
        }

        return user;
    }

    @Override
    public void register(RegisterDTO registerDTO) {
        String username = registerDTO.getUsername();

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username);
        Integer count = Math.toIntExact(baseMapper.selectCount(wrapper));
        if (count > 0) {
            throw new BusinessException(ErrorEnum.USERNAME_EXIST);
        }

        User user = User.builder()
                .username(username)
                .password(registerDTO.getPassword())
                .nickName(registerDTO.getNickName() != null ? registerDTO.getNickName() : username)
                .email(registerDTO.getEmail())
                .phone(registerDTO.getPhone())
                .sex(registerDTO.getSex())
                .userType(UserType2Num.COMMON.getCode())
                .status(StatusConstant.ENABLE)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted((short) 0)
                .build();

        int result = baseMapper.insert(user);
        if (result <= 0) {
            throw new BusinessException(ErrorEnum.USER_SAVE_FAIL);
        }
    }
}
