import com.sufu.blog.server.BlogServeApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 测试根据实体类自动生成数据库表，启动项目的时候自动创建
 * @author sufu
 * @date 2021/1/23
 */
@SpringBootTest(classes = BlogServeApplication.class)
public class JpaAutoCreateTableTest {
    @Test
    public void create(){
        System.out.println("success！");
    }
}
