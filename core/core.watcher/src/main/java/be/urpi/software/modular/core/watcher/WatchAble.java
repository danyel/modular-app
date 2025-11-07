package be.urpi.software.modular.core.watcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;

public interface WatchAble {
    File getFile() throws IOException;

    default void doOnStart() {
    }

    WatchEvent.Kind<Path>[] on();

    void checkState() throws WatchAbleException;
}
