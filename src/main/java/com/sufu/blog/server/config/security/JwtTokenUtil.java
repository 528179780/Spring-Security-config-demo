package com.sufu.blog.server.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * token工具类
 * @author sufu
 * @date 2021/1/24
 */
@Component
public class JwtTokenUtil {
    private static final String CLAIM_KEY_USERNAME = "sub";
    private static final String CLAIM_KEY_CREATED = "created";
    @Value("${jwt.tokenHead}")
    public  String tokenHead;
    @Value("${jwt.tokenHeader}")
    public String tokenHeader;
    @Value("${jwt.secret}")
    public String secret;
    @Value("${jwt.expiration}")
    public Long expiration;


    /**
     * 根据用户生成Token，token中只存username
     * @param userDetails 用户实体类
     * @return java.lang.String 生成的Token
     **/
    public String getTokenByUserDetails(UserDetails userDetails){
        Map<String,Object> claim = new HashMap<>(2);
        claim.put(CLAIM_KEY_USERNAME, userDetails.getUsername());
        claim.put(CLAIM_KEY_CREATED, new Date());
        return getTokenByClaim(claim);
    }

    /**
     * 根据载荷 私钥 过期时间生成Token
     * @param claim 载荷，存放过期时间和用户名
     * @return java.lang.String 生成的Token
     **/
    public String getTokenByClaim(Map<String,Object> claim){
        return tokenHead+" "+Jwts.builder()
                .setClaims(claim)
                .setExpiration(getExpirationTime())
                .signWith(SignatureAlgorithm.HS512,secret)
                .compact();
    }

    /**
     * 根据 expiration 生成过期时间
     * @return java.util.Date 过期时间
     **/
    private Date getExpirationTime() {
        return new Date(System.currentTimeMillis()+expiration);
    }
    /**
     * 根据Token获取username
     * @param token token
     * @return java.lang.String 用户名
     **/
    public String getUsernameByToken(String token){
        Claims claims =getClaimFromToken(token);
        return claims.getSubject();
    }
    /**
     * 根据token 获取载荷
     * @param token token
     * @return io.jsonwebtoken.Claims 载荷
     **/
    private Claims getClaimFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }
    /**
     * 判断token是否有效
     * @param token token
     * @return boolean true 如果有效
     **/
    public boolean isTokenAvailable(String token,UserDetails userDetails){
        return getUsernameByToken(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
    /**
     * 判断token是否过期
     * @param token token
     * @return boolean true 如果token过期了
     **/
    public boolean isTokenExpired(String token) {
        return getClaimFromToken(token).getExpiration().before(new Date());
    }
    /**
     * 判断Token是否可以刷新，如果未超时就可以刷新
     * @param token token
     * @return boolean true 如果token可以刷新
     **/
    public boolean isTokenRefreshable(String token){
        return isTokenExpired(token);
    }
    /**
     * 刷新token 将当前时间改为token的创建时间
     * @param token token
     * @return java.lang.String 刷新之后的token
     **/
    public String refreshToken(String token){
        Claims claim = getClaimFromToken(token);
        claim.put(CLAIM_KEY_CREATED, new Date());
        return getTokenByClaim(claim);
    }


    /**
     * 根据 给定的秒数 生成过期时间，用做测试
     * @return java.util.Date 过期时间
     **/
    private Date getExpirationTime(long seconds) {
        return new Date(System.currentTimeMillis()+seconds);
    }

    /**
     * 根据载荷 私钥 过期时间生成Token,用做测试
     * @param claim 载荷，存放过期时间和用户名
     * @return java.lang.String 生成的Token
     **/
    public String getTokenByClaim(Map<String,Object> claim,long seconds){
        return Jwts.builder()
                .setClaims(claim)
                .setExpiration(getExpirationTime(seconds))
                .signWith(SignatureAlgorithm.HS512,secret)
                .compact();
    }
}
