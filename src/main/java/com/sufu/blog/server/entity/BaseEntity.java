package com.sufu.blog.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 基本实体抽象类 定义了公共字段
 * @author sufu
 * @date 2021/1/23
 */
@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {
    /**
     * 雪花算法生成id
     **/
    @TableId(type = IdType.ASSIGN_ID)
    @Id
    protected Long id;
    /**
     * 乐观锁控制，通过mybatis plus插件实现
     **/
    @Version
    @Column(columnDefinition = "int default 1")
    protected Integer version;
    /**
     * 是否已经删除（逻辑删除）
     **/
    @TableLogic
    protected boolean deleted = false;
    /**
     * 在创建的时候自动填充
     **/
    @TableField(fill = FieldFill.INSERT)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    protected LocalDateTime createTime;
    /**
     * 更新的时候自动填充
     **/
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    protected LocalDateTime updateTime;
}
