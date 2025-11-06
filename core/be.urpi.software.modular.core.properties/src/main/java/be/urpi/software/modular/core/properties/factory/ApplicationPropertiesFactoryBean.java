package be.urpi.software.modular.core.properties.factory;

import be.urpi.software.modular.core.properties.FileWatchAbleApplicationProperties;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;

import static java.lang.System.getProperty;
import static org.springframework.util.StringUtils.hasText;

public class ApplicationPropertiesFactoryBean implements FactoryBean<FileWatchAbleApplicationProperties>, EnvironmentAware {
    private final static Logger log = LoggerFactory.getLogger(ApplicationPropertiesFactoryBean.class);
    public static final String JAR_DIRECTORY = "application.modules";
    private final ClassPathResource defaultApplicationConfigurationLocation = new ClassPathResource("classpath:/be/urpi/software/modular/ui/static/resources/ApplicationConfiguration.properties");
    @Getter
    private final boolean singleton = true;
    @Getter
    private final Class<?> objectType = FileWatchAbleApplicationProperties.class;
    @Setter
    private Environment environment;

    public FileWatchAbleApplicationProperties getObject() throws Exception {
        String property = environment.getProperty(JAR_DIRECTORY);
        Resource resource = null;

        if (hasText(property) && defaultApplicationConfigurationLocation.exists()) {
            resource = defaultApplicationConfigurationLocation;
        } else if (property != null) {
            Resource givenResource = property.startsWith("classpath:") ? new ClassPathResource(property.substring("classpath:".length())) : new FileSystemResource(property);

            if (givenResource.exists()) {
                resource = givenResource;
            }
        }

        if (resource != null) {
            log.debug("Loading application configuration from {}", resource.getFile().getAbsolutePath());
            FileWatchAbleApplicationProperties properties = new FileWatchAbleApplicationProperties();
            properties.load(resource);
            return properties;
        } else {
            throw new IOException(String.format("File not found: %s", hasText(property) ? defaultApplicationConfigurationLocation.getFilename() : property));
        }
    }
}
