package com.lghj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lghj.pojo.entity.SimAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SimAccountMapper extends BaseMapper<SimAccount> {

    /**
     * 使用行锁查询账户信息，防止并发操作导致的资金超扣
     *
     * @param userId 用户ID
     * @return 账户信息
     */
    @Select("SELECT * FROM sim_account WHERE user_id = #{userId} AND is_deleted = 0 FOR UPDATE")
    SimAccount selectForUpdate(Long userId);

}

