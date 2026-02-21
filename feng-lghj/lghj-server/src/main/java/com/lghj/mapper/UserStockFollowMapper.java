package com.lghj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lghj.pojo.entity.UserStockFollow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserStockFollowMapper extends BaseMapper<UserStockFollow> {

    /**
     * 查询用户关注的股票代码列表
     */
    @Select("SELECT symbol FROM user_stock_follow WHERE user_id = #{userId}")
    List<String> selectSymbolsByUserId(@Param("userId") Long userId);
}
