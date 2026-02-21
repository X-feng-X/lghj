package com.lghj.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lghj.pojo.dto.PageResult;
import com.lghj.pojo.dto.UserDTO;
import com.lghj.pojo.dto.UserQueryDTO;
import com.lghj.pojo.dto.UserUpdateDTO;
import com.lghj.pojo.entity.User;
import com.lghj.pojo.vo.UserVO;

public interface IUserService extends IService<User> {
    PageResult<UserVO> queryUserPage(Long current, Long size, UserQueryDTO userQueryDTO);

    boolean addUser(UserDTO userDTO);

    boolean removeUser(Long id);

    UserVO getUserById(Long id);

    boolean updateUser(UserUpdateDTO userUpdateDTO);

    boolean changeUserStatus(Long id, Short status);
}
