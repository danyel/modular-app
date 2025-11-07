package com.github.test.spring.config;

import be.urpi.software.modular.core.rest.spring.config.CoreRestAutoConfiguration;
import com.github.test.service.HelloService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration
@Import(value = {CoreRestAutoConfiguration.class})
@ComponentScan("com.github.test.service")
class TestWebConfiguration implements WebMvcConfigurer {
    @Bean(name = "helloService")
    HelloService helloService() {
        return new HelloService();
    }
}
