package be.urpi.software.modular.core.rest.api.service.manager.factory;

import be.urpi.software.modular.core.rest.api.service.RestService;
import be.urpi.software.modular.core.rest.api.service.exception.RestServiceNameNotFoundException;
import be.urpi.software.modular.core.rest.api.service.manager.RestServiceManager;
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

    void loadBeanOfTypeRestService() {
        restServiceManagerBean.loadBeans(applicationContext.getBeansOfType(RestService.class));
    }

    @Override
    public RestServiceManager getObject() throws Exception {
        loadBeanOfTypeRestService();
        return restServiceManagerBean;
    }

    private static class RestServiceManagerBean implements RestServiceManager {
        @SuppressWarnings("rawtypes")
        Map<String, RestService> restServiceRegistry = newHashMap();

        @SuppressWarnings("rawtypes")
        void loadBeans(final Map<String, RestService> beansOfType) {
            restServiceRegistry.clear();
            restServiceRegistry.putAll(beansOfType);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public RestService locateByName(final String restServiceName) throws RestServiceNameNotFoundException {
            if (restServiceRegistry.containsKey(restServiceName)) {
                return restServiceRegistry.get(restServiceName);
            }
            throw RestServiceNameNotFoundException.forServiceName(restServiceName);
        }
    }
}
