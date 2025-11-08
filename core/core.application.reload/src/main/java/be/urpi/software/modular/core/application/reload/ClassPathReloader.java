package be.urpi.software.modular.core.application.reload;

import be.urpi.software.modular.core.jar.util.JarFileUtil;
import be.urpi.software.modular.core.properties.ApplicationPropertiesWatcher;
import be.urpi.software.modular.core.properties.ModularProperties;
import be.urpi.software.modular.core.properties.SpringContextType;
import be.urpi.software.modular.core.watcher.file.FileWatchAble;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * This class will listen to the destination directory that has been set via the spring property module.reload.destination.
 * <p>
 * If there are new files it will be picked upped and it will make a child context and populate the parent context with the bean definitions of the child context
 *
 * @author danyel
 */
@SuppressWarnings("unused")
@Slf4j
public class ClassPathReloader implements FileWatchAble, ApplicationContextAware {
    private final File destinationDirectory;
    private GenericApplicationContext applicationContext;

    public ClassPathReloader(String destination) {
        destinationDirectory = new File(destination);
    }

    @Override
    public File getFile() {
        return destinationDirectory;
    }

    @Override
    public WatchEvent.Kind<Path>[] on() {
        //noinspection unchecked
        return (WatchEvent.Kind<Path>[]) new WatchEvent.Kind[]{ENTRY_CREATE, ENTRY_MODIFY};
    }

    @Override
    public void checkState() {
        Preconditions.checkState(destinationDirectory.exists());
        Preconditions.checkState(destinationDirectory.isDirectory());
    }

    @Override
    public void doOnChange() throws IOException {
        List<File> files = Arrays.asList(Objects.requireNonNullElse(destinationDirectory.listFiles(), new File[0]));
        log.debug("How much files in directory: {}", Strings.join(files.stream().map(File::getName).toList(), ','));
        for (File file : files) {
            log.debug("ClassPathReloader: {}", file.getAbsolutePath());
            InputStream inputStream = JarFileUtil.getInputStream(applicationContext, file, "META-INF/modular.properties");
            ModularProperties modularProperties = new ModularProperties();
            modularProperties.load(inputStream);
            ApplicationPropertiesWatcher applicationPropertiesWatcher = new ApplicationPropertiesWatcher(modularProperties, applicationContext, file);
            applicationPropertiesWatcher.checkState();
            applicationPropertiesWatcher.doOnChange();
            if (modularProperties.getType() == SpringContextType.JAVA) {
                ApplicationContextUtil.refresh(modularProperties, applicationContext, file);
            } else {
                ApplicationContextUtil.refreshXml(modularProperties, applicationContext, file);
            }
        }
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        if (applicationContext instanceof GenericApplicationContext genericApplicationContext) {
            this.applicationContext = genericApplicationContext;
        }
    }
}
