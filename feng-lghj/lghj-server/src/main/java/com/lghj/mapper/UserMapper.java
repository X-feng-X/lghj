package com.lghj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lghj.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

}
