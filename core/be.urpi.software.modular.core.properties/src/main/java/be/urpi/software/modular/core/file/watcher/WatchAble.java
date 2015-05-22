package be.urpi.software.modular.core.file.watcher;

import org.springframework.beans.factory.InitializingBean;

import java.io.File;
import java.io.IOException;

public interface WatchAble extends InitializingBean {
    File getFile() throws IOException;

    void doOnChange() throws IOException;

    void doOnStart() throws IOException;
}
