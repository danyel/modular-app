package be.urpi.software.modular.core.rest.api.service.manager.factory;

import be.urpi.software.modular.core.rest.api.service.RestService;
import be.urpi.software.modular.core.rest.api.service.exception.RestServiceNameNotFoundException;
import be.urpi.software.modular.core.rest.api.service.manager.RestServiceManager;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static java.lang.Boolean.TRUE;

public class RestServiceManagerBeanFactory implements FactoryBean<RestServiceManager>, ApplicationContextAware {
    private final RestServiceManagerBean restServiceManagerBean = new RestServiceManagerBean();
    private ApplicationContext applicationContext;

    void loadBeanOfTypeRestService() {
        final Map<String, RestService> beansOfType = applicationContext.getBeansOfType(RestService.class);
        restServiceManagerBean.loadBeans(beansOfType);
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public RestServiceManager getObject() throws Exception {
        loadBeanOfTypeRestService();
        return restServiceManagerBean;
    }

    @Override
    public Class<?> getObjectType() {
        return RestServiceManager.class;
    }

    @Override
    public boolean isSingleton() {
        return TRUE;
    }

    private static class RestServiceManagerBean implements RestServiceManager {
        Map<String, RestService> restServiceRegistry = newHashMap();

        void loadBeans(final Map<String, RestService> beansOfType) {
            restServiceRegistry.clear();
            restServiceRegistry.putAll(beansOfType);
        }

        @SuppressWarnings("unchecked")
        @Override
        public RestService locateByName(final String restServiceName) throws RestServiceNameNotFoundException {
            if (restServiceRegistry.containsKey(restServiceName)) {
                return restServiceRegistry.get(restServiceName);
            }
            throw RestServiceNameNotFoundException.forServiceName(restServiceName);
        }
    }
}
