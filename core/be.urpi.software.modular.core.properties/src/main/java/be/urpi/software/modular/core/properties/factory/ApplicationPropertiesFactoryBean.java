package be.urpi.software.modular.core.properties.factory;

import be.urpi.software.modular.core.properties.FileWatchAbleApplicationProperties;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;

import static com.google.common.base.Throwables.propagate;
import static java.lang.System.getProperty;
import static org.springframework.util.StringUtils.isEmpty;

public class ApplicationPropertiesFactoryBean implements FactoryBean<FileWatchAbleApplicationProperties> {
    private final ClassPathResource defaultApplicationConfigurationLocation = new ClassPathResource("classpath:/be/urpi/software/modular/ui/static/resources/ApplicationConfiguration.properties");

    public FileWatchAbleApplicationProperties getObject() throws Exception {
        final String property = getProperty("application.properties.location");
        Resource resource = null;

        if (isEmpty(property) && defaultApplicationConfigurationLocation.exists()) {
            resource = defaultApplicationConfigurationLocation;
        } else if (!isEmpty(property)) {
            final Resource givenResource = "classpath:".startsWith(property) ?
                    new ClassPathResource(property.substring("classpath:".length())) :
                    new FileSystemResource(property);

            if (givenResource.exists()) {
                resource = givenResource;
            }
        }

        if (resource != null) {
            final FileWatchAbleApplicationProperties properties = new FileWatchAbleApplicationProperties();
            try {
                properties.load(resource);
                return properties;
            } catch (final IOException e) {
                throw propagate(e);
            }
        } else {
            throw propagate(new IOException(String.format("File not found: %s", isEmpty(property) ?
                    defaultApplicationConfigurationLocation.getFilename() :
                    property)));
        }
    }

    public Class<?> getObjectType() {
        return FileWatchAbleApplicationProperties.class;
    }

    public boolean isSingleton() {
        return true;
    }
}
