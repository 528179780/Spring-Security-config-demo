package com.sufu.blog.server.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sufu.blog.server.dto.LoginParamsDTO;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 处理登录的过滤器
 * @author sufu
 * @date 2021/1/28
 */
public class JwtUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if(!HttpMethod.POST.toString().equals(request.getMethod())){
            throw new AuthenticationServiceException("仅支持POST请求登录!");
        }
        try {
            LoginParamsDTO loginParamsDTO = new ObjectMapper().readValue(request.getInputStream(), LoginParamsDTO.class);
            Assert.notNull(loginParamsDTO.getUsername(), "username 不能为空");
            Assert.notNull(loginParamsDTO.getPassword(),"password 不能为空");
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(loginParamsDTO.getUsername(), loginParamsDTO.getPassword());
            setDetails(request, authenticationToken);
            return this.getAuthenticationManager().authenticate(authenticationToken);
        } catch (IOException|IllegalArgumentException e) {
            throw new AuthenticationServiceException("请输入正确的请求格式", e);
        }
    }
}
