import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sufu.blog.server.BlogServeApplication;
import com.sufu.blog.server.entity.User;
import com.sufu.blog.server.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

/**
 * @author sufu
 * @date 2021/1/23
 */
@SpringBootTest(classes = BlogServeApplication.class)
public class MybatisPlusTest {
    @Autowired
    private UserMapper userMapper;
    /**
     * 测试插入
     **/
    @Test
    public void testInsert(){
        User user = new User();
        user.setUsername("sufu");
        user.setPassword("123456");
        user.setBirthday(LocalDateTime.now());
        user.setEmail("528179780@qq.com");
        user.setSex("男性");
        int insert = userMapper.insert(user);
        System.out.println("插入成功，受影响的行数： "+insert);
    }
    /**
     * 测试逻辑删除
     * @author sufu
     * @date 2021/1/23 21:56
     **/
    @Test
    public void testRemove(){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", "sufu");
        int delete = userMapper.delete(queryWrapper);
        System.out.println("受影响的行数： "+delete);
    }
    /**
     * 测试乐观锁
     **/
    @Test
    public void testOptimisticLocker(){
        testInsert();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", "sufu");
        // 先查询出User
        User user = userMapper.selectOne(queryWrapper);
        user.setUsername("sufu-1");
        // 模拟被其他线程先更新一次
        int i = userMapper.updateById(user);
        System.out.println("先更新：受影响行数："+i+" version"+user.getVersion());
        // 会自动更新version 因此这里将version设置回来
        user.setVersion(1);
        // 本线程提交更新
        int result = userMapper.updateById(user);
        if(result == 1){
            System.out.println("本线程更新：受影响行数："+result+" version"+user.getVersion());
        }else {
            System.out.println("更新失败");
        }

    }
}
