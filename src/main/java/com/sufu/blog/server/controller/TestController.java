package com.sufu.blog.server.controller;

import com.sufu.blog.server.dto.BaseResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author sufu
 * @date 2021/1/24
 */
@RestController
public class TestController {

    @GetMapping("/hello")
    @PreAuthorize("hasRole('ROLE_admin')")
    public BaseResult hello(){
        return BaseResult.success(200, "hello");
    }
}
