package be.urpi.software.modular.core.properties.factory;

import be.urpi.software.modular.core.properties.WatchAbleApplicationProperties;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;

import static com.google.common.base.Throwables.propagate;
import static java.lang.System.getProperty;
import static org.springframework.util.StringUtils.isEmpty;

public class ApplicationPropertiesFactoryBean implements FactoryBean<WatchAbleApplicationProperties> {
    private final ClassPathResource defaultApplicationConfigurationLocation = new ClassPathResource("classpath:/be/urpi/software/modular/ui/static/resources/ApplicationConfiguration.properties");

    public WatchAbleApplicationProperties getObject() throws Exception {
        final String property = getProperty("application.properties.location");
        Resource resource = null;

        if (isEmpty(property) && defaultApplicationConfigurationLocation.exists()) {
            resource = defaultApplicationConfigurationLocation;
        } else if (!isEmpty(property)) {
            final FileSystemResource fileSystemResource = new FileSystemResource(property);

            if (fileSystemResource.exists()) {
                resource = fileSystemResource;
            }
        }

        if (resource != null) {
            final WatchAbleApplicationProperties properties = new WatchAbleApplicationProperties();
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
        return WatchAbleApplicationProperties.class;
    }

    public boolean isSingleton() {
        return true;
    }
}
