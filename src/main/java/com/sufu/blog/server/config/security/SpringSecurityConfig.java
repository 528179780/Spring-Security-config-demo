package com.sufu.blog.server.config.security;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.sufu.blog.server.dto.BaseResult;
import com.sufu.blog.server.service.UserService;
import com.sufu.blog.server.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;


/**
 * @author sufu
 * @date 2021/1/19
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled=true)
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    @Value("${jwt.tokenHead}")
    private String tokenHead;
    @Autowired
    private JwtTokenUtil tokenUtil;

    /**
     * 配置userDetailService和密码加密器
     **/
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder());
    }
    @Bean
    public JwtUsernamePasswordAuthenticationFilter jwtUsernamePasswordAuthenticationFilter() throws Exception {
        JwtUsernamePasswordAuthenticationFilter authenticationFilter = new JwtUsernamePasswordAuthenticationFilter();
        // 设置登陆成功处理
        authenticationFilter.setAuthenticationSuccessHandler((req,resp,auth)->{
            UserDetails loggedUserDetails = (UserDetails) auth.getPrincipal();
            // 将已经登录的用户放入redis做缓存，持续时间为30分钟，结合jwt中的过期时间控制登录
            redisTemplate.opsForValue().set(loggedUserDetails.getUsername(), loggedUserDetails, Duration.ofMinutes(30));
            // 校验成功，返回token
            resp.setContentType("application/json; charset=UTF-8");
            String token = tokenUtil.getTokenByUserDetails(loggedUserDetails);
            Map<String,String> res = new HashMap<>(2);
            res.put("Token-Head",tokenHead);
            res.put("Token",token);
            BaseResult result = BaseResult.success("登陆成功", res);
            PrintWriter writer = resp.getWriter();
            writer.write(new ObjectMapper().writeValueAsString(result));
            writer.flush();
            writer.close();
        });
        authenticationFilter.setAuthenticationFailureHandler((req,resp,e)->{
            resp.setContentType("application/json; charset=UTF-8");
            resp.setStatus(400);
            PrintWriter writer = resp.getWriter();
            BaseResult respBean = BaseResult.fail(400);
            if (e instanceof BadCredentialsException) {
                respBean.setMsg("用户名或者密码输入错误，请重新输入!");
            } else if (e instanceof CredentialsExpiredException) {
                respBean.setMsg("密码过期，请联系管理员!");
            } else if (e instanceof AccountExpiredException) {
                respBean.setMsg("账户过期，请联系管理员!");
            } else if (e instanceof DisabledException) {
                respBean.setMsg("账户被禁用，请联系管理员!");
            } else if (e instanceof LockedException) {
                respBean.setMsg("账户被锁定，请联系管理员!");
            }else if(e instanceof AuthenticationServiceException){
                respBean.setMsg(e.getMessage());
            }
            writer.write(new ObjectMapper().writeValueAsString(respBean));
            writer.flush();
            writer.close();
        });
        authenticationFilter.setAuthenticationManager(authenticationManagerBean());
        authenticationFilter.setFilterProcessesUrl("/login");
        return authenticationFilter;
    }


    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    /**
     * 该配置下的路径不走拦截器，不能放login请求，这样的话 不会走拦截器，做处理的拦截
     **/
    @Override
    public void configure(WebSecurity web) {
        web.ignoring()
                .antMatchers("/captcha","/favicon.ico");
    }



    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf()
                .disable()
                // 基于Token 不需要session
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/login")
                .permitAll()
                .and()
                .headers()
                .cacheControl();
        http.logout().logoutSuccessHandler((req,resp,auth)->{
            resp.setContentType("application/json; charset=UTF-8");
            PrintWriter writer = resp.getWriter();
            writer.write(new ObjectMapper().writeValueAsString(BaseResult.success("注销成功")));
            writer.flush();
            writer.close();
        })
        .permitAll();
        // 访问被拒绝的处理器 这里配置的accessDeniedHandler只能捕获 以http配置的全权限要求不足抛出的异常
        // 注解@PreAuthorize配置的权限访问抛出的异常用全局异常捕获来处理
        http.exceptionHandling().accessDeniedHandler((req,resp,e)->{
            BaseResult result = BaseResult.fail(HttpServletResponse.SC_FORBIDDEN, "拒绝访问，权限不足");
            ResponseUtil.doRestResponse(resp, HttpServletResponse.SC_FORBIDDEN, result);
        }).authenticationEntryPoint((req, resp, e) -> {
            BaseResult result = BaseResult.fail(HttpServletResponse.SC_BAD_REQUEST, "认证失败！请重新登录");
            ResponseUtil.doRestResponse(resp, HttpServletResponse.SC_BAD_REQUEST, result);
        });
        http.addFilterAt(jwtUsernamePasswordAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }
    @Bean
    JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter(){
        return new JwtAuthenticationTokenFilter();
    }
}
