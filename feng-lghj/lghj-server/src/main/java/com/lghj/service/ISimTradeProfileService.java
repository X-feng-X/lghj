package com.lghj.service;

import com.lghj.pojo.vo.SimTradeProfileVO;

public interface ISimTradeProfileService {

    SimTradeProfileVO queryProfile(Long userId);
}
