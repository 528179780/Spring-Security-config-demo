package com.sufu.blog.server.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

/**
 * 角色
 * @author sufu
 * @date 2021/1/24
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Entity(name = "t_role")
@TableName("t_role")
public class Role extends BaseEntity{
    private String name;
    private String nameZh;
}
