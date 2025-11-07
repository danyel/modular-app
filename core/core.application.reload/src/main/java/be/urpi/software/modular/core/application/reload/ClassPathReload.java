package be.urpi.software.modular.core.application.reload;

import be.urpi.software.modular.core.watcher.file.FileWatchAble;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

@SuppressWarnings("unused")
@Slf4j
public class ClassPathReload implements FileWatchAble, ApplicationContextAware {
    private final String destination;
    private File destinationDirectory;
    private ApplicationContext applicationContext;

    public ClassPathReload(String destination) {
        this.destination = destination;
    }

    @Override
    public File getFile() {
        return destinationDirectory;
    }

    @Override
    public WatchEvent.Kind<Path>[] on() {
        //noinspection unchecked
        return (WatchEvent.Kind<Path>[]) new WatchEvent.Kind[]{ENTRY_MODIFY};
    }

    @Override
    public void afterPropertiesSet() {
        checkNotNull(destination);
        destinationDirectory = new File(destination);
        checkState(destinationDirectory.exists());
        checkState(destinationDirectory.isDirectory());
    }

    @Override
    public void doOnChange() {
        for (File file : Objects.requireNonNullElse(destinationDirectory.listFiles(), new File[0])) {
            log.debug("ClassPathReloader: {}", file.getAbsolutePath());
            ClassPathUtil.refresh(applicationContext, file);
        }
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
