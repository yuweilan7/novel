package io.github.xxyopen.novel.core.interceptor;

import io.github.xxyopen.novel.core.auth.UserHolder;
import io.github.xxyopen.novel.core.constant.SystemConfigConsts;
import io.github.xxyopen.novel.core.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Token 解析拦截器
 *
 * @author xiongxiaoyang
 * @date 2022/5/27
 */

/**
 * 一般情况下，Token解析拦截器会先于认证授权拦截器运行。这是因为在进行认证授权之前，
 * 需要先解析请求中携带的Token，从而确定请求中包含的用户身份信息。
 * 在实际应用中，通常将Token解析拦截器放置在认证授权拦截器的前面。
 * 这样可以保证在认证授权拦截器中使用用户身份信息时，这些信息已经被正确解析并存储到相关的上下文中。
 */
@Component
@RequiredArgsConstructor
public class TokenParseInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
        Object handler) throws Exception {
        // 获取登录 JWT
        String token = request.getHeader(SystemConfigConsts.HTTP_AUTH_HEADER_NAME);
        if (StringUtils.hasText(token)) {
            // 解析 token 并保存
            UserHolder.setUserId(jwtUtils.parseToken(token, SystemConfigConsts.NOVEL_FRONT_KEY));
        }
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
        ModelAndView modelAndView) throws Exception {
        // 清理当前线程保存的用户数据
        UserHolder.clear();
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }
}
