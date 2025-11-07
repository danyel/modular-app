package be.urpi.software.modular.core.watcher;

import org.springframework.beans.factory.InitializingBean;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;

public interface WatchAble extends InitializingBean {
    File getFile() throws IOException;

    default void doOnStart() throws IOException {
    }

    WatchEvent.Kind<Path>[] on();
}
