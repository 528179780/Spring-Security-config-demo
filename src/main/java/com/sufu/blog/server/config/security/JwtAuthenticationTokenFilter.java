package com.sufu.blog.server.config.security;

import com.sufu.blog.server.dto.BaseResult;
import com.sufu.blog.server.util.ResponseUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 每一次访问都需要设置认证
 * @author sufu
 * @date 2021/1/28
 */
public class JwtAuthenticationTokenFilter extends SecurityContextPersistenceFilter {
    /**
     * 如果该值存在，说明已经拦截过，不再拦截
     **/
    static final String FILTER_APPLIED = "__spring_security_jatf_applied";
    @Value("${jwt.tokenHeader}")
    private String tokenHeader;
    @Value("${jwt.tokenHead}")
    private  String tokenHead;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // 保证这个拦截器每次请求只拦截一次
        if (request.getAttribute(FILTER_APPLIED) != null) {
            chain.doFilter(request, response);
            return;
        }
        // 没有拦截过则添加属性到request 中，表示已经被该拦截器拦截过了
        request.setAttribute(FILTER_APPLIED,Boolean.TRUE);
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String header = req.getHeader(tokenHeader);
        if(header!=null && header.startsWith(tokenHead)){
            // 请求中Token存在且以指定head开头
            String token = header.substring(tokenHead.length());
            String username;
            try {
                // 从Token中获取用户名 如果过期了会报ExpiredException,
                username = jwtTokenUtil.getUsernameByToken(token);
            }catch (JwtException e){
                // 解析token的异常处理
                BaseResult result = BaseResult.fail(401);
                if(e instanceof ExpiredJwtException){
                    result.setMsg("当前Token已经过期，请重新登录。");
                }else {
                    result.setMsg("当前Token无效，请重新登录。");
                }
                ResponseUtil.doRestResponse(resp, 401, result);
                return;
            }
            if(SecurityContextHolder.getContext().getAuthentication()==null){
                // 未认证 尝试从redis中获取用户来认证
                UserDetails userDetails = (UserDetails) redisTemplate.opsForValue().get(username);
                if(userDetails == null){
                    // Token没过期 但是redis中没有这个登录的用户，说明超过30分钟，该key被删除了，重新查数据库，然后放入redis
                    userDetails = userDetailsService.loadUserByUsername(username);
                    redisTemplate.opsForValue().set(userDetails.getUsername(), userDetails);
                }
                // 设置用户对象
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        // 继续接下来的拦截器链
        chain.doFilter(req, resp);
        // 请求完成之后，清除Context，清除请求中的FILTER_APPLIED属性
        SecurityContextHolder.clearContext();
        req.removeAttribute(FILTER_APPLIED);
    }
}
