package be.urpi.software.modular.core.watcher.file;

import be.urpi.software.modular.core.watcher.WatchAble;

import java.io.File;
import java.io.IOException;

public interface FileWatchAble extends WatchAble {
    void doOnChange(File file) throws IOException;
}
