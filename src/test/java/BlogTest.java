import com.sufu.blog.server.BlogServeApplication;
import com.sufu.blog.server.config.security.JwtTokenUtil;
import com.sufu.blog.server.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;

import java.util.Date;
import java.util.HashMap;

/**
 * @author sufu
 * @date 2021/1/24
 */
@SpringBootTest(classes = BlogServeApplication.class)
public class BlogTest {
    @Autowired
    UserService userService;
    @Autowired
    RedisTemplate<String,Object> redisTemplate;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    JwtTokenUtil jwtTokenUtil;
    @Test
    public void testUserService(){
        System.out.println(userService.loadUserByUsername("sufu"));
    }
    @Test
    public void testRedis(){
//        Object sufu = redisTemplate.opsForValue().get("sufu");
//        System.out.println(sufu);
        Date date = new Date(1611886389692L);
        System.out.println(date);
    }
    @Test
    public void testPasswordEncoder(){
        System.out.println(passwordEncoder.encode("123456"));
    }
    /**
     * 测试jwt解析过期的token是会抛出异常还是得到空值
     **/
    @Test
    public void testJwt(){
        HashMap<String, Object> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put("test", "test");
        String tokenByClaim = jwtTokenUtil.getTokenByClaim(objectObjectHashMap, 1L);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            System.out.println(jwtTokenUtil.getUsernameByToken(tokenByClaim));
        }catch (ExpiredJwtException e){
            System.out.println(e.getMessage());
        }

    }
}
