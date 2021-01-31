package com.sufu.blog.server.dto;

import lombok.Data;

/**
 * 登录参数对象
 * @author sufu
 * @date 2021/1/25
 */
@Data
public class LoginParamsDTO {
    /**
     * 用户名
     **/
    private String username;
    /**
     * 密码
     **/
    private String password;
    /**
     * 验证码id，请求验证码的时候提交的id
     **/
    private String captchaId;
    /**
     * 验证码 非必须，提示之后必须
     **/
    private String captchaCode;
}
