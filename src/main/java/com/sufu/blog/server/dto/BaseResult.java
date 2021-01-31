package com.sufu.blog.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author sufu
 * @date 2021/1/24
 */
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResult {
    private Integer code;
    private String msg;
    private Object data;
    public static BaseResult success(String msg){
        return new BaseResult(200, msg, null);
    }
    public static BaseResult success(Integer code,String msg){
        return new BaseResult(code, msg, null);
    }
    public static BaseResult success(String msg,Object data){
        return new BaseResult(200, msg, data);
    }

    public static BaseResult fail(){
        return new BaseResult(400, "请求失败", null);
    }
    public static BaseResult fail(Integer code){
        return new BaseResult(code, null, null);
    }
    public static BaseResult fail(Integer code,String msg){
        return new BaseResult(code, msg, null);
    }
    public static BaseResult fail(String msg,Object data){
        return new BaseResult(400, msg, data);
    }
}
