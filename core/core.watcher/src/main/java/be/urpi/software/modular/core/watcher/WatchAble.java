package be.urpi.software.modular.core.watcher;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent;

public interface WatchAble {
    File getFile();

    WatchEvent.Kind<Path>[] on();

    void checkState() throws WatchAbleException;
}
