import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class Context implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    public Object getBean(String bean) {
        return applicationContext.getBean(bean);
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
