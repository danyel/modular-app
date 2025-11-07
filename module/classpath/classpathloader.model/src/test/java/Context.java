import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class Context implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    @SuppressWarnings("unused")
    public Object getBean(String bean) {
        return applicationContext.getBean(bean);
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
