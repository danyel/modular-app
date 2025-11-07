package be.urpi.software.modular.core.service.manager;

import be.urpi.software.modular.core.service.RestService;
import be.urpi.software.modular.core.service.exception.RestServiceNameNotFoundException;
import be.urpi.software.modular.core.service.manager.factory.RestServiceManagerBeanFactory;
import org.springframework.context.ApplicationContext;

public interface RestServiceManager {
    String BEAN_NAME = "restServiceManager";

    <S, E> RestService<S, E> locateByName(String restServiceName) throws RestServiceNameNotFoundException;

    void load(ApplicationContext applicationContext);

    static RestServiceManager getInstance(ApplicationContext applicationContext) {
        RestServiceManagerBeanFactory restServiceManagerBeanFactory = new RestServiceManagerBeanFactory();
        restServiceManagerBeanFactory.setApplicationContext(applicationContext);
        return restServiceManagerBeanFactory.getObject();
    }
}
