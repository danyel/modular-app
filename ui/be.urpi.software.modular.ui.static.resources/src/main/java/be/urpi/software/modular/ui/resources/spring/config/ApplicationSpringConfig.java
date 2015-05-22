package be.urpi.software.modular.ui.resources.spring.config;

import be.urpi.software.modular.core.properties.FileWatchAbleApplicationProperties;
import be.urpi.software.modular.core.properties.factory.ApplicationPropertiesFactoryBean;
import be.urpi.software.modular.core.watcher.file.FileWatcher;
import be.urpi.software.modular.core.watcher.file.ThreadFileWatcher;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

@Configuration
@ImportResource(value = {"classpath:/be/urpi/software/modular/ui/static/resources/static-resources-ctx.xml"})
@ComponentScan(basePackages = "be.urpi.software.modular.ui.resources")
@EnableWebMvc
public class ApplicationSpringConfig extends WebMvcConfigurerAdapter {
    @Bean(name = "applicationProperties")
    ApplicationPropertiesFactoryBean applicationProperties() {
        return new ApplicationPropertiesFactoryBean();
    }

    @Bean(name = "applicationPropertiesFileWatcher", destroyMethod = "stopThread", initMethod = "startThread")
    FileWatcher applicationPropertiesFileWatcher(final FileWatchAbleApplicationProperties applicationProperties) throws Exception {
        return new ThreadFileWatcher(applicationProperties);
    }

    @Override
    public void configureMessageConverters(final List<HttpMessageConverter<?>> converters) {
        super.configureMessageConverters(converters);
        converters.add(jsonMessageConverter());
    }

    MappingJackson2HttpMessageConverter jsonMessageConverter() {
        return new MappingJackson2HttpMessageConverter();
    }
}
