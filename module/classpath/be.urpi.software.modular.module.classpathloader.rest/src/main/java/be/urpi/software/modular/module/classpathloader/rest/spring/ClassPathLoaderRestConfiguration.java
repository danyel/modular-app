package be.urpi.software.modular.module.classpathloader.rest.spring;

import be.urpi.software.modular.module.classpathloader.model.ClassPathLoaderModelConfiguration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@Import(value = ClassPathLoaderModelConfiguration.class)
@EnableWebMvc
public class ClassPathLoaderRestConfiguration extends WebMvcConfigurerAdapter {
}
