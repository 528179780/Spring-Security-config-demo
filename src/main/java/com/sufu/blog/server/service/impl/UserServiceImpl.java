package com.sufu.blog.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sufu.blog.server.entity.User;
import com.sufu.blog.server.mapper.UserMapper;
import com.sufu.blog.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author sufu
 * @date 2021/1/23
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        return userMapper.selectOne(new QueryWrapper<User>().eq("username", s));
    }
    @Override
    public boolean isUsernameExisted(String username){
        return userMapper.selectCount(new QueryWrapper<User>().eq("username", username)) != 0;
    }

    @Override
    public int insert(User user) {
        return userMapper.insert(user);
    }
}
