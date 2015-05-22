import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Properties;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = {"classpath:META-INF/context/library-properties-ctx.xml", "classpath:module-classpathloader-ctx.xml"})
public class ReunTest {
    @Resource(name = "applicationPropertiesFile")
    private Properties properties;

    @Test
    public void testName() throws Exception {
        while (true) {

        }
    }
}
