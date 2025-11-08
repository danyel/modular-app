package be.urpi.software.modular.core.properties;

import be.urpi.software.modular.core.jar.util.JarFileUtil;
import be.urpi.software.modular.core.watcher.WatchAbleException;
import be.urpi.software.modular.core.watcher.file.FileWatchAble;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.List;
import java.util.Properties;
import java.util.function.Predicate;

@Slf4j
public class ApplicationPropertiesWatcher implements FileWatchAble {
    private static final String propertyName = "module.name";
    private final ConfigurableEnvironment environment;
    private final ApplicationContext applicationContext;
    private final File jarFile;
    private final ModularProperties modularProperties;

    public ApplicationPropertiesWatcher(ModularProperties modularProperties, ApplicationContext applicationContext, File jarFile) {
        this.jarFile = jarFile;
        this.modularProperties = modularProperties;
        this.applicationContext = applicationContext;
        this.environment = applicationContext instanceof ConfigurableApplicationContext configurableApplicationContext
                ? configurableApplicationContext.getEnvironment()
                : null;
    }

    @Override
    public void doOnChange() {
        try {
            if (environment != null) {
                Predicate<String> filter = fileName -> {
                    String yamlFile = "modular-%s.yaml".formatted(modularProperties.getName());
                    String propertiesFile = "modular-%s.properties".formatted(modularProperties.getName());
                    String ymlFile = "modular-%s.yml".formatted(modularProperties.getName());
                    return fileName.contains(propertiesFile) || fileName.contains(ymlFile) || fileName.contains(yamlFile);
                };
                List<String> entries = JarFileUtil.getEntries(jarFile, filter);
                if (!entries.isEmpty()) {
                    if (entries.size() > 1) {
                        String message = "Multiple entries found for file: " + Strings.join(entries, ',');
                        log.error(message);
                        throw new WatchAbleException(new IllegalStateException(message));
                    }
                    String entry = entries.getFirst();
                    Properties modularApplicationProperties = new Properties();
                    modularApplicationProperties.load(JarFileUtil.getInputStream(applicationContext, jarFile, entry));
                    if (!environment.getPropertySources().contains(entry)) {
                        environment.getPropertySources().addFirst(new PropertiesPropertySource(entry, modularApplicationProperties));
                    }
                }
            }
        } catch (IOException e) {
            // do nothing
        }
    }

    @Override
    public File getFile() {
        return jarFile;
    }

    @Override
    public WatchEvent.Kind<Path>[] on() {
        // this does not matter
        //noinspection unchecked
        return (WatchEvent.Kind<Path>[]) new WatchEvent.Kind[0];
    }

    @Override
    public void checkState() throws WatchAbleException {
        Preconditions.checkState(jarFile.exists());
    }
}
