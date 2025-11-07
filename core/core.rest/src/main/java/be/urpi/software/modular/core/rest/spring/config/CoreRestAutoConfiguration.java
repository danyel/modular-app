package be.urpi.software.modular.core.rest.spring.config;

import be.urpi.software.modular.core.service.manager.RestServiceManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "be.urpi.software.modular.core.rest.controller")
public class CoreRestAutoConfiguration {
    @Bean(name = RestServiceManager.BEAN_NAME)
    RestServiceManager restServiceManager(ApplicationContext applicationContext) {
        return RestServiceManager.getInstance(applicationContext);
    }
}