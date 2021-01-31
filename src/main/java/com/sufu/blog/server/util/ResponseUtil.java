package com.sufu.blog.server.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sufu.blog.server.dto.BaseResult;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 做response的工具类
 * @author sufu
 * @date 2021/1/28
 */
public class ResponseUtil {
    public static void doRestResponse(HttpServletResponse response,Integer responseCode,BaseResult result) throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(responseCode);
        PrintWriter writer = response.getWriter();
        writer.write(new ObjectMapper().writeValueAsString(result));
        writer.flush();
        writer.close();
    }
}
