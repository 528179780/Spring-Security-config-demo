package com.sufu.blog.server.controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

/**
 * 获取验证码接口
 * @author sufu
 * @date 2021/1/26
 */
@RestController
public class CaptchaController {
    @Autowired
    private DefaultKaptcha kaptchaUtil;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    @RequestMapping("/captcha")
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response){
        // 定义response输出类型为image/jpeg类型
        response.setDateHeader("Expires", 0);
        // Set standard HTTP/1.1 no-cache headers.
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        // Set IE extended HTTP/1.1 no-cache headers (use addHeader).
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        // Set standard HTTP/1.0 no-cache header.
        response.setHeader("Pragma", "no-cache");
        // return a jpeg
        response.setContentType("image/jpeg");
        // 生成验证码
        String captchaValue = kaptchaUtil.createText();
        // 生成一个uuid作为code-id
        String captchaId = UUID.randomUUID().toString();
        response.setHeader("Captcha-id", captchaId);
        // 将uuid作为键保存到redis 超时时间为1s
        redisTemplate.opsForValue().set(captchaId, captchaValue, Duration.ofMinutes(1));
        // 生成img
        BufferedImage image = kaptchaUtil.createImage(captchaValue);
        try(ServletOutputStream outputStream = response.getOutputStream()) {
            // 输出流输出图片，格式为jpg
            ImageIO.write(image, "jpg", outputStream);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
