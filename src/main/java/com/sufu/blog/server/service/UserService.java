package com.sufu.blog.server.service;

import com.sufu.blog.server.entity.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * @author sufu
 * @date 2021/1/24
 */
public interface UserService extends UserDetailsService {
    /**
     * 检查用户名是否可用
     * @param username 用户名
     * @return boolean true 如果用户名可用
     **/
    boolean isUsernameExisted(String username);
    /**
     * 插入一条用户数据
     * @param user 用户实体类
     * @return int 返回受影响的行数
     **/
    int insert(User user);
}
