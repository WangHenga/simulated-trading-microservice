package com.cffex.simulatedtradingmodel.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {

    private static final String SECRET_KEY = "yourSecretKeyasfasdfasdfasnjfadladjfisodfoiasffaosiddfjsfsamfasdfoiasdfasjifnaifdasofsdfaaiofjasfasdnfoisadfsajdoifsadnfas"; // 密钥，需要保密

    public static String generateToken(Integer userId, Long expire) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);

        Date now = new Date();
        Date expiration = new Date(now.getTime() + expire);

        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(expiration)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public static Integer getUserIdFromToken(String token) {
        Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
        return (Integer) claims.get("userId");
    }

    // TODO 其他方法，如验证token是否有效等
}
