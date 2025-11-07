package be.urpi.software.modular.core.filesystem.configuration;

import be.urpi.software.modular.core.application.reload.ClassPathReloader;
import be.urpi.software.modular.core.application.reload.ClassPathSourceDirectory;
import be.urpi.software.modular.core.watcher.file.FileWatchAble;
import be.urpi.software.modular.core.watcher.file.ThreadFileWatcher;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;

import java.io.IOException;

@AutoConfiguration
@Setter
@Slf4j
class FileWatcherAutoConfiguration implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Bean
    FileWatchAble classPathReload(@Value("${module.reload.destination}") String destinationFolder) {
        FileWatchAble classPathReload = new ClassPathReloader(destinationFolder);
        log.info("Configuring classPath watcher {}", destinationFolder);
        ThreadFileWatcher threadDirectoryWatcher = new ThreadFileWatcher(classPathReload);
        threadDirectoryWatcher.start();
        return classPathReload;
    }

    @Bean
    FileWatchAble sourceDirectoryToWatch(@Value("${module.reload.source}") String source, @Value("${module.reload.destination}") String destination) {
        FileWatchAble sourceDirectoryToWatch = new ClassPathSourceDirectory(source, destination);
        log.info("Configuring directory watcher {}", source);
        ThreadFileWatcher threadDirectoryWatcher = new ThreadFileWatcher(sourceDirectoryToWatch);
        threadDirectoryWatcher.start();
        return sourceDirectoryToWatch;
    }

    @EventListener(ApplicationStartedEvent.class)
    void onApplicationEvent(ApplicationStartedEvent ignoredEvent) throws IOException {
        FileWatchAble classPathReload = applicationContext.getBean("classPathReload", FileWatchAble.class);
        FileWatchAble sourceDirectoryToWatch = applicationContext.getBean("sourceDirectoryToWatch", FileWatchAble.class);
        sourceDirectoryToWatch.doOnChange();
        classPathReload.doOnChange();
    }
}
