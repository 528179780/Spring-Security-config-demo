package com.sufu.blog.server.exception;

import com.sufu.blog.server.dto.BaseResult;
import com.sufu.blog.server.util.ResponseUtil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 全局异常捕获
 * @author sufu
 * @date 2021/1/29
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 处理AccessDeniedException，这里的异常一般是由 @PreAuthorize("hasRole('ROLE_XXXX')")注解的方法访问异常抛出的
     * 不能由spring security 的accessDeniedHandler处理
     * @param resp response对象
     **/
    @ExceptionHandler(AccessDeniedException.class)
    public void accessDeniedExceptionHandler(HttpServletResponse resp) throws IOException {
        ResponseUtil.doRestResponse(resp, 403, BaseResult.fail(403, "没有访问权限，请联系管理员"));
    }
}
