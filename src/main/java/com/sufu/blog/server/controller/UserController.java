package com.sufu.blog.server.controller;

import com.sufu.blog.server.dto.BaseResult;
import com.sufu.blog.server.dto.RegisterParamsDTO;
import com.sufu.blog.server.entity.User;
import com.sufu.blog.server.service.UserService;
import com.sufu.blog.server.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author sufu
 * @date 2021/1/23
 */
@Controller
public class UserController {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserService userService;
    @PostMapping("/register")
    public void register(RegisterParamsDTO paramsDTO, HttpServletResponse resp) throws IOException {
        User user = new User();
        user.setUsername(paramsDTO.getUsername());
        user.setPassword(passwordEncoder.encode(paramsDTO.getPassword()));
        user.setSex(paramsDTO.getSex());
        user.setEmail(paramsDTO.getEmail());
        user.setBirthday(paramsDTO.getBirthday());
        int insert = userService.insert(user);
        BaseResult result;
        if(insert == 0){
            result = BaseResult.success(201,"注册成功！");
            ResponseUtil.doRestResponse(resp, HttpServletResponse.SC_CREATED, result);
        }else {
            result = BaseResult.fail(400,"注册失败！请重试");
            ResponseUtil.doRestResponse(resp, HttpServletResponse.SC_BAD_REQUEST, result);
        }
    }
    @GetMapping("/username/exist")
    public void testIfUsernameExist(String username,HttpServletResponse resp) throws IOException {
        BaseResult result;
        if (username == null){
            result = BaseResult.fail(400,"请携带username参数查询");
        }else {
            boolean usernameExisted = userService.isUsernameExisted(username);
            Map<String, Boolean> responseData= new HashMap<>(1);
            responseData.put("existed", usernameExisted);
            result = BaseResult.success("查询成功",responseData);
        }
        ResponseUtil.doRestResponse(resp,200,result);
    }
}
