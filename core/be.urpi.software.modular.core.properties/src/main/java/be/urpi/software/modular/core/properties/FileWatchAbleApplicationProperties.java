package be.urpi.software.modular.core.properties;

import be.urpi.software.modular.core.watcher.file.FileWatchAble;

import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class FileWatchAbleApplicationProperties extends Properties implements FileWatchAble {
    private Resource resource;

    public synchronized void load(final Resource resource) throws IOException {
        this.resource = resource;
        load(resource.getInputStream());
    }

    public void setResource(final Resource resource) throws IOException {
        this.resource = resource;
        load(resource.getInputStream());
    }

    @Override
    public File getFile() throws IOException {
        return resource.getFile();
    }

    @Override
    public void doOnChange() throws IOException {
        load(resource.getInputStream());
    }

    @Override
    public void doOnStart() throws IOException {
        load(resource.getInputStream());
    }

    @Override
    public WatchEvent.Kind<Path> on() {
        return ENTRY_MODIFY;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        checkNotNull(resource);
        checkArgument(resource.exists());
    }
}
