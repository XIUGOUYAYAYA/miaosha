package org.miaosha.miaosha.common.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    // 密钥，项目上线建议放到yml配置文件，不要硬编码
    private static final String SECRET = "miaosha-secret-key-20260921-miaoshaproject-256bits-long-secret-string123";
    // 生成HS256加密密钥
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes());
    // token过期时间：120分钟，单位毫秒
    private static final long EXPIRE_TIME = 1000 * 60 * 120;


    /**
     * 根据userId、用户角色生成JWT token（登录使用重载方法）
     * @param userId 用户ID
     * @param role 用户角色 0普通用户 1管理员
     * @return token字符串
     */
    public String generateToken(Long userId, Byte role) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + EXPIRE_TIME);
        Map<String, Object> claims = new HashMap<>();
        if (role != null) {
            claims.put("role", role);
        }
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId.toString())
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(KEY)
                .compact();
    }

    /**
     * 从token解析获取userId
     * @param token token字符串
     * @return 用户id
     */
    public Long getUserIdByToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Long.valueOf(claims.getSubject());
    }

    /**
     * 从token解析获取用户角色
     * @param token token字符串
     * @return 角色Byte：0普通用户，1管理员；无role返回null
     */
    public Byte getRoleByToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("role", Byte.class);
    }

    /**
     * 校验token合法性：签名、是否过期
     * @param token token
     * @return true合法；false非法/过期
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            // token过期
        } catch (MalformedJwtException | SignatureException e) {
            // token被篡改、格式错误
        } catch (Exception e) {
            // 其他异常
        }
        return false;
    }
}
