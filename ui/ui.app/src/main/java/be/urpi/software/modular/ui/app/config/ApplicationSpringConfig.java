package be.urpi.software.modular.ui.app.config;

import be.urpi.software.modular.core.properties.FileWatchAbleApplicationProperties;
import be.urpi.software.modular.core.properties.factory.ApplicationPropertiesFactoryBean;
import be.urpi.software.modular.core.watcher.file.FileWatcher;
import be.urpi.software.modular.core.watcher.file.ThreadFileWatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@ComponentScan(basePackages = "be.urpi.software.modular.ui.resources")
@EnableWebMvc
public class ApplicationSpringConfig implements WebMvcConfigurer {
    @Bean(name = "applicationProperties")
    ApplicationPropertiesFactoryBean applicationProperties() {
        return new ApplicationPropertiesFactoryBean();
    }

    @Bean(name = "applicationPropertiesFileWatcher", destroyMethod = "stopThread", initMethod = "startThread")
    FileWatcher applicationPropertiesFileWatcher(FileWatchAbleApplicationProperties applicationProperties) {
        return new ThreadFileWatcher(applicationProperties);
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(jsonMessageConverter());
    }

    MappingJackson2HttpMessageConverter jsonMessageConverter() {
        return new MappingJackson2HttpMessageConverter();
    }
}
