package be.urpi.software.modular.core.test.rest.spring.config;

import be.urpi.software.modular.core.rest.spring.config.CoreRestConfiguration;
import be.urpi.software.modular.core.test.rest.service.HelloService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@Import(value = {CoreRestConfiguration.class})
@EnableWebMvc
public class TestWebConfiguration extends WebMvcConfigurerAdapter {
    @Bean(name = "helloService")
    HelloService helloService() {
        return new HelloService();
    }
}
