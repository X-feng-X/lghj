package com.lghj.controller.internal;

import com.lghj.pojo.dto.Result;
import com.lghj.service.ISimTradeProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/sim-trade")
@RequiredArgsConstructor
public class SimTradeProfileController {

    private final ISimTradeProfileService simTradeProfileService;

    @Value("${lghj.internal-api.token:}")
    private String internalApiToken;

    @GetMapping("/profile")
    public Result queryProfile(@RequestParam Long userId,
                               @RequestHeader(value = "X-Internal-Token", required = false) String token) {
        if (StringUtils.hasText(internalApiToken) && !internalApiToken.equals(token)) {
            return Result.error("internal token invalid");
        }
        return Result.success(simTradeProfileService.queryProfile(userId));
    }
}
