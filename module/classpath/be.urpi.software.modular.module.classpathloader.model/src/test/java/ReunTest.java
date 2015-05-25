import be.urpi.software.modular.module.classpathloader.model.ClassPathLoaderModelConfiguration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import javax.annotation.Resource;
import java.util.Properties;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ClassPathLoaderModelConfiguration.class, loader = AnnotationConfigContextLoader.class)
public class ReunTest {
    @Resource(name = "applicationPropertiesFile")
    private Properties properties;
    @Resource
    private Context applicationContext;

    @Test
    public void testName() {
        while (true) {
            try {
                final Object helloService = applicationContext.getBean("helloService");

                if (helloService != null) {
                    System.out.println("found");
                }
            } catch (NoSuchBeanDefinitionException e) {
                System.out.println("d");
            }
        }
    }
}
