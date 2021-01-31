package com.sufu.blog.server.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * @author sufu
 * @date 2021/1/23
 */

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Entity(name = "t_user")
@TableName("t_user")
public class User extends BaseEntity implements UserDetails, Serializable {
    /**
     * 用户名唯一 且不为空 长度为32以内
     **/
    @Column(length = 32,unique = true,nullable = false)
    private String username;
    /**
     * 密码 不为空
     **/
    @Column(length = 64,nullable = false)
    private String password;
    @Column(length = 2)
    private String sex;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime birthday;
    private String email;
    @Transient
    @TableField(exist = false)
    private List<Role> roles = new ArrayList<>();
    /**
     * 是否过期
     **/
    private boolean expired = false;
    /**
     * 是否锁定
     **/
    private boolean locked = false;
    /**
     * 是否启用
     **/
    private boolean enabled = true;


    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (Role role : roles) {
            grantedAuthorities.add(new SimpleGrantedAuthority(role.getName()));
        }
        return grantedAuthorities;
    }
    public Collection<? extends GrantedAuthority> setAuthorities(List<SimpleGrantedAuthority> grantedAuthorities) {
        return grantedAuthorities;
    }


    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return !expired;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    /**
     * 当前密码是否过期
     * @return boolean true if credentials not expired(密码没有过期)
     **/
    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
