package be.urpi.software.modular.core.watcher.directory;

import be.urpi.software.modular.core.watcher.WatchAble;

import java.io.File;
import java.io.IOException;

public interface DirectoryWatchAble extends WatchAble {
    void doOnChange(File file) throws IOException;
}
