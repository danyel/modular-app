package be.urpi.software.modular.core.application.reload;

import be.urpi.software.modular.core.watcher.directory.DirectoryWatchAble;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class ClassPathReload implements DirectoryWatchAble {
    @Override
    public File getFile() throws IOException {
        return null;
    }

    @Override
    public void doOnChange() throws IOException {

    }

    @Override
    public void doOnStart() throws IOException {

    }

    @Override
    public WatchEvent.Kind<Path> on() {
        return ENTRY_MODIFY;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
