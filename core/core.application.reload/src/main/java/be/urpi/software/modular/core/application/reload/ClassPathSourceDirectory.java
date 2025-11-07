package be.urpi.software.modular.core.application.reload;

import be.urpi.software.modular.core.watcher.directory.DirectoryWatchAble;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

@SuppressWarnings("unused")
@Slf4j
public class ClassPathSourceDirectory implements DirectoryWatchAble, ApplicationContextAware {
    private final String source;
    private final String destination;
    private File sourceDirectory;
    private File destinationDirectory;
    private ApplicationContext applicationContext;

    public ClassPathSourceDirectory(String source, String destination) {
        this.source = source;
        this.destination = destination;
    }

    @Override
    public File getFile() {
        return sourceDirectory;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void doOnStart() throws IOException {
        // already checked can never be null after afterPropertiesSet() is executed
        for (File file : sourceDirectory.listFiles()) {
            if (file.isFile() && !file.isDirectory()) {
                log.debug("Deleting file: {}", file.getAbsolutePath());
                FileUtils.forceDelete(file);
            }
        }
    }

    @Override
    public WatchEvent.Kind<Path>[] on() {
        //noinspection unchecked
        return (WatchEvent.Kind<Path>[]) new WatchEvent.Kind[]{ENTRY_MODIFY};
    }

    @Override
    public void afterPropertiesSet() {
        checkNotNull(source);
        checkNotNull(destination);
        sourceDirectory = new File(source);
        destinationDirectory = new File(destination);
        checkState(sourceDirectory.exists());
        checkState(destinationDirectory.exists());
    }

    @Override
    public void doOnChange(File file) throws IOException {
        String fileName = file.getName();
        File sourceFile = new File(sourceDirectory, fileName);
        File destinationFile = new File(destinationDirectory, fileName);
        FileUtils.copyFile(sourceFile, destinationFile);
        ClassPathUtil.refresh(applicationContext, destinationFile);
        FileUtils.delete(sourceFile);
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
