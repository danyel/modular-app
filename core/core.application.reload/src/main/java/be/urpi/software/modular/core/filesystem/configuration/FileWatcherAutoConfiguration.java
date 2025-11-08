package be.urpi.software.modular.core.filesystem.configuration;

import be.urpi.software.modular.core.application.reload.ClassPathReloader;
import be.urpi.software.modular.core.application.reload.ClassPathSourceDirectory;
import be.urpi.software.modular.core.watcher.ThreadWatcher;
import be.urpi.software.modular.core.watcher.file.FileWatchAble;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@AutoConfiguration
@Setter
@Slf4j
class FileWatcherAutoConfiguration implements ApplicationContextAware {
    private ApplicationContext applicationContext;
    private @Value("${module.reload.source}") String source;
    private @Value("${module.reload.destination}") String destination;

    @Bean
    FileWatchAble classPathReload() {
        FileWatchAble classPathReload = new ClassPathReloader(destination);
        log.info("Configuring classPath watcher {}", destination);
        return classPathReload;
    }

    @Bean
    FileWatchAble sourceDirectoryToWatch() {
        FileWatchAble sourceDirectoryToWatch = new ClassPathSourceDirectory(source, destination);
        log.info("Configuring directory watcher {}", source);
        return sourceDirectoryToWatch;
    }

    @Bean
    ThreadWatcher threadFileWatcher(@Autowired FileWatchAble sourceDirectoryToWatch, @Autowired FileWatchAble classPathReload) {
        ThreadWatcher threadDirectoryWatcher = new ThreadWatcher(new LinkedList<>(List.of(sourceDirectoryToWatch, classPathReload)));
        threadDirectoryWatcher.setObservablePath(sourceDirectoryToWatch().getFile());
        threadDirectoryWatcher.start();
        return threadDirectoryWatcher;
    }

    @EventListener(ApplicationStartedEvent.class)
    void onApplicationEvent(ApplicationStartedEvent ignoredEvent) throws IOException {
        FileWatchAble classPathReload = applicationContext.getBean("classPathReload", FileWatchAble.class);
        FileWatchAble sourceDirectoryToWatch = applicationContext.getBean("sourceDirectoryToWatch", FileWatchAble.class);
        for (File file : Objects.requireNonNullElse(new File(source).listFiles(), new File[0])) {
            sourceDirectoryToWatch.doOnChange(file);
        }
        for (File file : Objects.requireNonNullElse(new File(destination).listFiles(), new File[0])) {
            classPathReload.doOnChange(file);
        }
    }
}
