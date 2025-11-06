package be.urpi.software.modular.core.application.reload;

import be.urpi.software.modular.core.watcher.directory.DirectoryWatchAble;
import org.apache.commons.io.FileUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.WatchEvent;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class ClassPathReload implements DirectoryWatchAble, ApplicationContextAware {
    @Value("${module.reload.source}")
    private String source;
    @Value("${module.reload.destination}")
    private String destination;
    private File sourceDirectory;
    private File destinationDirectory;
    private ApplicationContext applicationContext;

    @Override
    public File getFile() {
        return sourceDirectory;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void doOnStart() throws IOException {
        // already checked can never be null after afterPropertiesSet() is executed
        for (File file : sourceDirectory.listFiles()) {
            FileUtils.forceDelete(file);
        }
    }

    @Override
    public WatchEvent.Kind<Path> on() {
        return ENTRY_MODIFY;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
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
        URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        ClassPathUtil.setOnClassPath(destinationFile);
        if (applicationContext instanceof AnnotationConfigWebApplicationContext annotationConfigWebApplicationContext) {
            annotationConfigWebApplicationContext.refresh();
        }
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
