package com.lghj.interceptor;

import com.lghj.constant.JwtClaimsConstant;
import com.lghj.context.BaseContext;
import com.lghj.enums.ErrorEnum;
import com.lghj.exception.BusinessException;
import com.lghj.properties.JwtProperties;
import com.lghj.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * jwt令牌校验的拦截器（优化不通过逻辑，精准抛错、规范日志）
 */
@Component
@Slf4j
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    @Resource
    private JwtProperties jwtProperties;

    /**
     * 校验jwt
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 判断当前拦截到的是Controller的方法还是其他资源（非接口方法直接放行）
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 从请求头中获取令牌（提取请求URL，用于日志记录）
        String requestUrl = request.getRequestURI();
        String token = request.getHeader(jwtProperties.getAdminTokenName());
        log.info("开始校验JWT令牌，请求URL：{}，令牌：{}", requestUrl, desensitizeToken(token));

        // 先判断token是否为空（提前拦截，避免无效解析）
        if (token == null || token.trim().isEmpty()) {
            log.error("JWT校验失败：请求头中未携带令牌，请求URL：{}", requestUrl);
            // 设置HTTP响应状态码：401 Unauthorized（未授权）
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            throw new BusinessException(ErrorEnum.NO_LOGIN, "未登录，请先登录后再访问");
        }

        // 校验令牌（捕获JWT相关专属异常，精准区分错误类型）
        try {
            // 解析JWT令牌
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            short userType = Short.parseShort(claims.get(JwtClaimsConstant.USER_TYPE).toString());

            // 校验管理员权限（仅管理员可访问，该逻辑保留）
            if (userType != 3) {
                log.error("JWT校验失败：无管理员权限，用户ID：{}，请求URL：{}", userId, requestUrl);
                // 设置HTTP响应状态码：403 Forbidden（禁止访问）
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                throw new BusinessException(ErrorEnum.NO_PERMISSION, "无管理员权限，无法访问该接口");
            }

            // 令牌校验通过，将用户ID存入ThreadLocal（便于后续业务获取）
            BaseContext.setCurrentId(userId);
            log.info("JWT校验成功，用户ID：{}，请求URL：{}", userId, requestUrl);
            return true;
        } catch (JwtException e) {
            // 专属捕获：JWT相关异常（token过期、无效、签名错误等）
            log.error("JWT校验失败：令牌无效或已过期，请求URL：{}，异常信息：{}", requestUrl, e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            throw new BusinessException(ErrorEnum.NO_LOGIN, "登录状态已过期，请重新登录");
        } catch (NumberFormatException e) {
            // 专属捕获：用户ID/userType格式转换异常（令牌被篡改）
            log.error("JWT校验失败：令牌内容被篡改，请求URL：{}，异常信息：{}", requestUrl, e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            throw new BusinessException(ErrorEnum.NO_LOGIN, "令牌无效，无法访问");
        } catch (BusinessException e) {
            // 捕获自身抛出的权限异常（不覆盖，直接向上抛出，让全局异常处理器处理）
            throw e;
        } catch (Exception e) {
            // 兜底捕获：其他未知异常
            log.error("JWT校验失败：未知异常，请求URL：{}，异常信息：{}", requestUrl, e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw new BusinessException(ErrorEnum.SYSTEM_ERROR, "系统内部异常，无法访问");
        }
    }

    /**
     * 辅助方法：令牌脱敏（避免日志泄露完整token，仅保留前8位和后8位）
     */
    private String desensitizeToken(String token) {
        if (token == null || token.length() <= 16) {
            return "******";
        }
        return token.substring(0, 8) + "******" + token.substring(token.length() - 8);
    }
}