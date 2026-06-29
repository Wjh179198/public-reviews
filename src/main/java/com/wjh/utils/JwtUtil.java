package com.wjh.utils;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Jwts;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;


public class JwtUtil {

    public static String createToken (String secretKey, long ttlMillis, Map<String, Object> claims) {

        // 生成JWT的时间
        long expMillis = System.currentTimeMillis() + ttlMillis;
        Date exp = new Date(expMillis);

        //指定签名算法
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        return Jwts.builder()
                .setClaims(claims)
                .signWith(signatureAlgorithm, secretKey.getBytes(StandardCharsets.UTF_8))
                .setExpiration(exp)
                .compact();
    }

    public static Map<String, Object> parseToken (String token, String secretKey) {
        return Jwts.parser()
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(token)
                .getBody();
    }
}
