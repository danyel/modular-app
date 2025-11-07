package be.urpi.software.modular.core.service.manager.factory;

import be.urpi.software.modular.core.service.RestService;
import be.urpi.software.modular.core.service.exception.RestServiceNameNotFoundException;
import be.urpi.software.modular.core.service.manager.RestServiceManager;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class RestServiceManagerBeanFactory implements FactoryBean<RestServiceManager>, ApplicationContextAware {
    private final RestServiceManagerBean restServiceManagerBean = new RestServiceManagerBean();
    @Setter
    private ApplicationContext applicationContext;
    @Getter
    private final boolean singleton = true;
    @Getter
    private final Class<?> objectType = RestServiceManager.class;

    @Override
    public RestServiceManager getObject() {
        restServiceManagerBean.load(applicationContext);
        return restServiceManagerBean;
    }

    private static class RestServiceManagerBean implements RestServiceManager {
        @SuppressWarnings("rawtypes")
        Map<String, RestService> restServiceRegistry = newHashMap();

        @SuppressWarnings("rawtypes")
        @Override
        public void load(ApplicationContext applicationContext) {
            Map<String, RestService> beansOfType = applicationContext.getBeansOfType(RestService.class);
            restServiceRegistry.clear();
            restServiceRegistry.putAll(beansOfType);
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        public RestService locateByName(final String restServiceName) throws RestServiceNameNotFoundException {
            if (restServiceRegistry.containsKey(restServiceName)) {
                return restServiceRegistry.get(restServiceName);
            }
            throw RestServiceNameNotFoundException.forServiceName(restServiceName);
        }
    }
}
