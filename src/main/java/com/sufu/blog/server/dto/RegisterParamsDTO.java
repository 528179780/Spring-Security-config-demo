package com.sufu.blog.server.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 注册参数类
 * @author sufu
 * @date 2021/1/29
 */
@Data
public class RegisterParamsDTO {
    String username;
    String password;
    String sex;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    LocalDateTime birthday;
    String email;
}
