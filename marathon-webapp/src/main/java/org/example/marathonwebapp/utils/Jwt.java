package org.example.marathonwebapp.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class Jwt {
    //签名信息
    private static String secret = "change-me-in-local-env";
    private static long expireSeconds = 86400;

    @Value("${jwt.secret:change-me-in-local-env}")
    public void setSecret(String configuredSecret) {
        Jwt.secret = configuredSecret;
    }

    @Value("${jwt.expire-seconds:86400}")
    public void setExpireSeconds(long configuredExpireSeconds) {
        Jwt.expireSeconds = configuredExpireSeconds;
    }

    // 生成token
    public static String createToken(String account,Long userId,Integer type) {
        JwtBuilder builder = Jwts.builder();
        return builder
                // 设置头部
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS256")
                // 设置载荷
                .claim("account", account)
                .claim("userId", userId)
                .claim("type",type)
                .setId(UUID.randomUUID().toString())
                // 设置过期时间
                .setExpiration(new Date(System.currentTimeMillis() + expireSeconds * 1000))
                // 签名 指定加密算法和签名内容
                .signWith(SignatureAlgorithm.HS256, secret)
                // 将三部分拼起来生成token
                .compact();
    }

    // 解析token 返回权限
    public Integer parseToken(String token) {
        return parseType(token);
    }

    public static Integer parseType(String token) {
        return (Integer) parseClaims(token).get("type");
    }

    public static Long parseUserId(String token) {
        Object userId = parseClaims(token).get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        if (userId instanceof Long) {
            return (Long) userId;
        }
        return Long.valueOf(String.valueOf(userId));
    }

    public static Claims parseClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    //校验token是否合法
    public static boolean checkToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
