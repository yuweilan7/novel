package io.github.xxyopen.novel.core.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.xxyopen.novel.core.auth.AuthStrategy;
import io.github.xxyopen.novel.core.auth.UserHolder;
import io.github.xxyopen.novel.core.common.exception.BusinessException;
import io.github.xxyopen.novel.core.common.resp.RestResp;
import io.github.xxyopen.novel.core.constant.ApiRouterConsts;
import io.github.xxyopen.novel.core.constant.SystemConfigConsts;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 认证授权 拦截器：为了注入其它的 Spring beans，需要通过 @Component 注解将该拦截器注册到 Spring 上下文
 *
 * @author xiongxiaoyang
 * @date 2022/5/18
 */

/**
 * 假设当前系统的 API URL 前缀为 /api/v1，一个链接为 /api/v1/systemA/getUserInfo。
 * 当该链接被请求时，Spring MVC 会调用该系统中所有符合 /api/v1/** 匹配规则的拦截器。
 * 在这个系统中，有两个拦截器需要被执行：Token 解析拦截器和认证授权拦截器。
 *
 * 首先，Token 解析拦截器的 preHandle() 函数会被调用。该函数从 HTTP Header 中获取 JWT，
 * 然后从请求 URI 中解析出系统名称，进而确定使用哪种认证策略进行认证。
 * 在这个例子中，系统名称是 systemA，则使用 SystemAAuthStrategy 进行认证。
 *
 * 接着，认证授权拦截器的 preHandle() 函数会被调用。
 * 该函数会从 HTTP Header 中获取 JWT，然后对 JWT 进行解析，
 * 校验用户的身份和权限是否满足当前请求所需的要求。如果校验通过，那么请求将被允许继续执行。
 * 如果校验不通过，那么请求将被拒绝，返回相应的错误信息。
 *
 * 因此，在这个例子中，Token 解析拦截器会在认证授权拦截器之前被执行，
 * 用于解析 JWT 并确定使用哪种认证策略。认证授权拦截器则在 Token 解析拦截器之后被执行，
 * 用于对用户的身份和权限进行认证和授权。
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final Map<String, AuthStrategy> authStrategy;

    private final ObjectMapper objectMapper;

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
        Object handler) throws Exception {
        // 获取登录 JWT
        String token = request.getHeader(SystemConfigConsts.HTTP_AUTH_HEADER_NAME);

        // 获取请求的 URI
        /**
         * 比如请求的URL为http://example.com:8080/path/to/resource?param1=value1&param2=value2#anchor，
         * 则request.getRequestURI()返回的字符串就是/path/to/resource
         */
        String requestUri = request.getRequestURI();

        // 根据请求的 URI 得到认证策略
        String subUri = requestUri.substring(ApiRouterConsts.API_URL_PREFIX.length() + 1);
        String systemName = subUri.substring(0, subUri.indexOf("/"));
        String authStrategyName = String.format("%sAuthStrategy", systemName);

        // 开始认证
        try {
            /**
             * 主要是Front和Author开头的链接校验,Admin还没有,其实就这三种
             */
            authStrategy.get(authStrategyName).auth(token, requestUri);
            return HandlerInterceptor.super.preHandle(request, response, handler);
        } catch (BusinessException exception) {
            // 认证失败
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                objectMapper.writeValueAsString(RestResp.fail(exception.getErrorCodeEnum())));
            return false;
        }
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
        ModelAndView modelAndView) throws Exception {
        // 清理当前线程保存的用户数据
        UserHolder.clear();
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }
}
