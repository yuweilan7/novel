package io.github.xxyopen.novel.core.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * JWT 工具类
 *
 * @author xiongxiaoyang
 * @date 2022/5/17
 */

/**
 * JWT (JSON Web Token) 是一种基于 JSON 的开放标准 (RFC 7519)，用于在网络应用中传递声明，
 * 用于在身份验证和授权过程中验证信息的真实性。
 * 在实际开发中，通常使用 JWT 来进行用户认证和授权。
 * 生成 JWT 后，可以将其作为请求的一部分，发送给服务端，服务端通过校验 JWT 来识别用户身份和权限。
 * JWT 具有轻量级、自包含、可扩展性、安全性等特点。
 * 在上述代码中，generateToken 方法用于根据用户 ID 和系统标识生成 JWT，
 * 而 parseToken 方法用于解析 JWT 并返回用户 ID。这些方法可以用于实现用户身份验证和授权。
 */
@ConditionalOnProperty("novel.jwt.secret")
@Component
@Slf4j
public class JwtUtils {

    /**
     * 注入JWT加密密钥
     */
    @Value("${novel.jwt.secret}")
    private String secret;

    /**
     * 定义系统标识头常量
     */
    private static final String HEADER_SYSTEM_KEY = "systemKeyHeader";

    /**
     * 根据用户ID生成JWT
     *
     * @param uid       用户ID
     * @param systemKey 系统标识
     * @return JWT
     */
    /**
     * Jwts.builder()：用于创建JWT构建器对象。
     * .setHeaderParam(HEADER_SYSTEM_KEY, systemKey)：在JWT header中添加一个键值对，表示此JWT是哪个系统发出的。
     * .setSubject(uid.toString())：将用户ID作为JWT的Subject，表示这个JWT是代表哪个用户的。
     * .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))：用指定的算法和密钥对JWT进行签名。
     * .compact()：将JWT编码为字符串。
     */
    public String generateToken(Long uid, String systemKey) {
        return Jwts.builder()
            .setHeaderParam(HEADER_SYSTEM_KEY, systemKey)
            .setSubject(uid.toString())
            .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
            .compact();
    }

    /**
     * 解析JWT返回用户ID
     *
     * @param token     JWT
     * @param systemKey 系统标识
     * @return 用户ID
     */
    public Long parseToken(String token, String systemKey) {
        Jws<Claims> claimsJws;
        try {
            claimsJws = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token);
            // OK, we can trust this JWT
            // 判断该 JWT 是否属于指定系统
            if (Objects.equals(claimsJws.getHeader().get(HEADER_SYSTEM_KEY), systemKey)) {
                return Long.parseLong(claimsJws.getBody().getSubject());
            }
        } catch (JwtException e) {
            log.warn("JWT解析失败:{}", token);
            // don't trust the JWT!
        }
        return null;
    }

}
