package com.example.commonutil.jwt;

import io.jsonwebtoken.*;
import org.springframework.util.StringUtils;

import java.util.Date;

public class JwtHelper {

    private static long tokenExpiration = 365 * 24 * 60 * 60 * 1000;
    private static String tokenSignKey = "123456";

    // 根据用户 id 和用户名称， 生成token的字符串
    public static String createToken(Long userId, String username) {
        String token = Jwts.builder()
                .setSubject("AUTH-USER")
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration))
                .claim("userId", userId)
                .claim("username", username)
                .signWith(SignatureAlgorithm.HS512, tokenSignKey)
                .compressWith(CompressionCodecs.GZIP)
                .compact();
        return token;
    }

    public static Long getUserId(String token) {
        try {
            if (StringUtils.isEmpty(token)) return null;

            Claims claims=parseJwt(token);
            Integer userId = (Integer) claims.get("userId");
            return userId.longValue();
        } catch (Exception e) {

            return null;
        }
    }

    public static String getUsername(String token) {
        try {
            if (StringUtils.isEmpty(token)) return "";

            Claims claims=parseJwt(token);
            return (String) claims.get("username");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Claims parseJwt(String token){
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(tokenSignKey) // 设置标识名
                    .parseClaimsJws(token)  //解析token
                    .getBody();
        } catch (ExpiredJwtException e) {
            claims = e.getClaims();
        }
        return claims;
    }


    public static void main(String[] args) {
        String token = JwtHelper.createToken(2L, "wangwu");
        System.out.println(token);
        String username = JwtHelper.getUsername(token);
        Long userId = JwtHelper.getUserId(token);

        System.out.println("username = " + username);
        System.out.println("userId = " + userId);
    }

}
