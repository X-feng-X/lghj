package com.lghj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lghj.enums.*;
import com.lghj.exception.BusinessException;
import com.lghj.mapper.UserMapper;
import com.lghj.pojo.dto.PageResult;
import com.lghj.pojo.dto.UserDTO;
import com.lghj.pojo.dto.UserQueryDTO;
import com.lghj.pojo.dto.UserUpdateDTO;
import com.lghj.pojo.entity.User;
import com.lghj.pojo.entity.UserRole;
import com.lghj.pojo.vo.UserVO;
import com.lghj.service.IRoleService;
import com.lghj.service.IUserRoleService;
import com.lghj.service.IUserService;
import com.lghj.utils.EnumConvertUtil;
import com.lghj.utils.PageResultConvertUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final IRoleService RoleService;
    private final IUserRoleService userRoleService;

    /**
     * 用户分页条件查询
     */
    @Override
    public PageResult<UserVO> queryUserPage(Long current, Long size, UserQueryDTO userQueryDTO) {
        // 构建分页对象
        IPage<User> userPage = new Page<>(current, size);

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .select(User::getId, User::getUsername, User::getPassword, User::getEmail, User::getPhone, User::getSex,
                        User::getUserType, User::getStatus, User::getCreateTime, User::getUpdateTime, User::getCreateUser,
                        User::getUpdateUser, User::getIsDeleted)
                .orderByDesc(User::getUpdateTime);

        if (userQueryDTO != null) {
            wrapper.like(StringUtils.hasText(userQueryDTO.getUsername()), User::getUsername, userQueryDTO.getUsername())
                    .like(StringUtils.hasText(userQueryDTO.getEmail()), User::getEmail, userQueryDTO.getEmail())
                    .like(StringUtils.hasText(userQueryDTO.getPhone()), User::getPhone, userQueryDTO.getPhone())
                    .eq(userQueryDTO.getSex() != null, User::getSex, userQueryDTO.getSex())
                    .eq(userQueryDTO.getUserType() != null, User::getUserType, userQueryDTO.getUserType())
                    .eq(userQueryDTO.getStatus() != null, User::getStatus, userQueryDTO.getStatus())
                    .eq(userQueryDTO.getIsDeleted() != null, User::getIsDeleted, userQueryDTO.getIsDeleted())
                    .ge(userQueryDTO.getBegin() != null, User::getUpdateTime, userQueryDTO.getBegin())
                    .le(userQueryDTO.getEnd() != null, User::getUpdateTime, userQueryDTO.getEnd());
        }

        // 执行分页查询
        IPage<User> userIPage = baseMapper.selectPage(userPage, wrapper);

        PageResult<UserVO> pageResult = PageResultConvertUtil.convert(userIPage, user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);

            userVO.setStatus(EnumConvertUtil.convertState(user.getStatus()));
            userVO.setUserType(EnumConvertUtil.convertUserType(user.getUserType()));
            userVO.setSex(EnumConvertUtil.convertSex(user.getSex()));
            return userVO;
        });

        return pageResult;
    }

    /**
     * 新增用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务注解：异常时自动回滚
    public boolean addUser(UserDTO userDTO) {
        // 兜底参数校验（防止 Controller 校验遗漏，提高 Service 层独立性）
        if (userDTO == null) {
            throw new BusinessException(ErrorEnum.PARAM_NULL);
        }
        if (!StringUtils.hasText(userDTO.getUsername())) {
            throw new BusinessException(ErrorEnum.USERNAME_EMPTY);
        }

        // 业务校验：用户名去重（核心业务逻辑，避免重复数据）
        Integer usernameCount = baseMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, userDTO.getUsername()));
        if (usernameCount > 0) {
            log.error("新增用户失败：用户名 {} 已存在", userDTO.getUsername());
            throw new RuntimeException("新增用户失败，用户名已存在");
        }

        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        Short userType = userDTO.getUserType(); // 提取 userType，避免多次调用，方便 null 校验
        // 默认 userType 为普通用户（1，若前端未传递）
        if (userType == null) {
            userType = 1;
        }

        // 插入用户表
        boolean userSaveSuccess = this.save(user);
        if (!userSaveSuccess) {
            throw new BusinessException(ErrorEnum.USER_SAVE_FAIL);
        }
        Integer roleId = null;
        Long newUserId = user.getId();

        // 插入用户-角色对应表
        if (userType == 3) {
            // 判断是否是管理员角色
            if (UserTypeRoleMapEnum.isAdminRole(userDTO.getRoleName())) {
                roleId = userDTO.getRoleName();
            } else {
                throw new BusinessException(ErrorEnum.PARAM_INVALID, "管理员角色不存在");
            }
        } else {
            roleId = UserTypeRoleMapEnum.getByUserType(userDTO.getUserType());
            if (roleId == null) {
                throw new BusinessException(ErrorEnum.PARAM_INVALID, "userType 不合法（仅支持1/2/3）");
            }
        }
        UserRole userRole = UserRole.builder()
                .userId(newUserId)
                .roleId(roleId)
                .build();
        userRoleService.save(userRole);
        log.info("用户-角色关联表插入成功，用户ID：{}，角色ID：{}", newUserId, roleId);

        return true;
    }

    /**
     * 删除用户
     */
    @Override
    public boolean removeUser(Long id) {
        // 参数校验
        if (id == null) {
            throw new BusinessException(ErrorEnum.PARAM_NULL);
        }

        // 查询用户是否存在
        User existingUser = baseMapper.selectById(id);
        if (existingUser == null) {
            throw new BusinessException(ErrorEnum.USER_NOT_EXIST);
        }

        // 删除用户表记录（逻辑删除）
        baseMapper.deleteById(id);

        // 删除用户-角色关联表记录
        LambdaQueryWrapper<UserRole> queryWrapper = new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, id); // 按 userId 匹配，而非关联表主键

        // 批量删除关联表记录
        boolean roleRelationDeleteSuccess = userRoleService.remove(queryWrapper);
        if (!roleRelationDeleteSuccess) {
            throw new BusinessException(ErrorEnum.SYSTEM_ERROR, "用户-角色关联记录删除失败");
        }

        log.info("用户 {} 及对应的关联角色记录删除成功", id);
        return true;
    }

    /**
     * 根据id查询用户
     */
    @Override
    public UserVO getUserById(Long id) {
        // 1. 调用MP的getById方法，查询用户实体
        User user = baseMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorEnum.USER_NOT_EXIST);
        }

        // 实体转换
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);

        // 补充枚举转换
        String name = Status2Num.getName(user.getStatus());
        userVO.setStatus(name == null ? "未知状态" : name);
        String userType = UserType2Num.getName(user.getUserType());
        userVO.setUserType(userType == null ? "未知状态" : userType);
        String sex = Sex2Num.getName(user.getSex());
        userVO.setSex(sex == null ? "未知状态" : sex);

        return userVO;
    }

    /**
     * 修改用户信息
     */
    @Override
    public boolean updateUser(UserUpdateDTO userUpdateDTO) {
        // 校验参数（ID不能为空）
        if (userUpdateDTO.getId() == null) {
            throw new RuntimeException("用户ID不能为空");
        }

        // 查询用户是否存在
        User existingUser = baseMapper.selectById(userUpdateDTO.getId());
        if (existingUser == null) {
            throw new BusinessException(ErrorEnum.USER_NOT_EXIST);
        }

        Short userType = userUpdateDTO.getUserType();
        Integer roleId = userUpdateDTO.getRoleName();
        if (userType != null && roleId != null) {
            if (userType == 3) {
                // 判断是否是管理员角色
                if (UserTypeRoleMapEnum.isAdminRole(userUpdateDTO.getRoleName())) {
                    roleId = userUpdateDTO.getRoleName();
                } else {
                    throw new BusinessException(ErrorEnum.PARAM_INVALID, "管理员角色不存在");
                }
            } else {
                roleId = UserTypeRoleMapEnum.getByUserType(userUpdateDTO.getUserType());
                if (roleId == null) {
                    throw new BusinessException(ErrorEnum.PARAM_INVALID, "userType 不合法（仅支持1/2/3）");
                }
            }
            LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<UserRole>()
                    .select(UserRole::getId)
                    .eq(UserRole::getUserId, userUpdateDTO.getId());
            UserRole userRole = userRoleService.getOne(wrapper);
            userRole.setRoleId(roleId);
            boolean roleUpdateSuccess = userRoleService.updateById(userRole);
            if (!roleUpdateSuccess) {
                throw new BusinessException(ErrorEnum.SYSTEM_ERROR, "用户-角色关联记录更新失败");
            }
        }

        // 转换实体类
        User user = new User();
        BeanUtils.copyProperties(userUpdateDTO, user);

        user.setUpdateTime(LocalDateTime.now());

        // 修改用户（返回是否修改成功）
        return baseMapper.updateById(user) > 0;
    }

    /**
     * 启用、禁用用户账号
     */
    @Override
    public boolean changeUserStatus(Long id, Short status) {
        // 1. 校验参数
        if (id == null || status == null) {
            throw new RuntimeException("用户ID和状态不能为空");
        }
        if (status != 0 && status != 1) {
            throw new RuntimeException("状态只能是0（禁用）或1（启用）");
        }

        // 2. 查询用户是否存在
        User existingUser = baseMapper.selectById(id);
        if (existingUser == null) {
            throw new RuntimeException("用户不存在，无法修改状态");
        }

        // 3. 构建要更新的实体（只更新userStatus字段）
        User user = new User();
        user.setId(id);
        user.setStatus(status);

        // 4. 更新状态
        return baseMapper.updateById(user) > 0;
    }
}

