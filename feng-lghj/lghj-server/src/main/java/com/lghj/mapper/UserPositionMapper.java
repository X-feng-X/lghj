package com.lghj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lghj.pojo.entity.UserPosition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserPositionMapper extends BaseMapper<UserPosition> {

    /**
     * 使用行锁查询持仓信息，防止并发操作导致的超卖
     * @param userId 用户ID
     * @param symbol 股票代码
     * @return 持仓信息
     */
    @Select("SELECT * FROM user_position WHERE user_id = #{userId} AND symbol = #{symbol} AND is_deleted = 0 FOR UPDATE")
    UserPosition selectForUpdate(Long userId, String symbol);

}

