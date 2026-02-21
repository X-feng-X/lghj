package com.lghj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lghj.pojo.entity.Blog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BlogMapper  extends BaseMapper<Blog> {
}
