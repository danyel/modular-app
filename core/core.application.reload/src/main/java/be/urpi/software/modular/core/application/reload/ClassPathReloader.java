package be.urpi.software.modular.core.application.reload;

import be.urpi.software.modular.core.properties.ApplicationPropertiesWatcher;
import be.urpi.software.modular.core.watcher.file.FileWatchAble;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
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
    private ApplicationContext applicationContext;

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
    public void doOnChange() {
        for (File file : Objects.requireNonNullElse(destinationDirectory.listFiles(), new File[0])) {
            log.debug("ClassPathReloader: {}", file.getAbsolutePath());
            ApplicationPropertiesWatcher applicationPropertiesWatcher = new ApplicationPropertiesWatcher(applicationContext, file);
            applicationPropertiesWatcher.checkState();
            applicationPropertiesWatcher.doOnChange();
            ApplicationContextUtil.refresh(applicationContext, file);
        }
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
