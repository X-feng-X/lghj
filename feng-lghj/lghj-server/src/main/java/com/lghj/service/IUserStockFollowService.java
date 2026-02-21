package com.lghj.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lghj.pojo.entity.UserStockFollow;
import com.lghj.pojo.vo.StockFollowVO;

import java.util.List;

public interface IUserStockFollowService extends IService<UserStockFollow> {

    /**
     * 添加自选股
     * @param userId 用户ID
     * @param symbol 股票代码
     * @return 成功返回true
     */
    boolean addFollow(Long userId, String symbol);

    /**
     * 取消关注
     * @param userId 用户ID
     * @param symbol 股票代码
     * @return 成功返回true
     */
    boolean removeFollow(Long userId, String symbol);

    /**
     * 查询自选股列表（带行情）
     * @param userId 用户ID
     * @return 自选股VO列表
     */
    List<StockFollowVO> getUserFollowList(Long userId);
}
