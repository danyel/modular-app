package be.urpi.software.modular.core.rest.spring.config;

import be.urpi.software.modular.core.rest.api.service.manager.RestServiceManager;
import be.urpi.software.modular.core.rest.api.service.manager.factory.RestServiceManagerBeanFactory;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "be.urpi.software.modular.core.rest.controller")
public class CoreRestConfiguration {
    @Bean(name = "restServiceManager")
    RestServiceManager restServiceManager(final ApplicationContext applicationContext) throws Exception {
        final RestServiceManagerBeanFactory restServiceManagerBeanFactory = new RestServiceManagerBeanFactory();
        restServiceManagerBeanFactory.setApplicationContext(applicationContext);
        return restServiceManagerBeanFactory.getObject();
    }
}
