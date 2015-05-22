package be.urpi.software.modular.core.properties;

import be.urpi.software.modular.core.file.watcher.WatchAble;

import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class WatchAbleApplicationProperties extends Properties implements WatchAble {
    private Resource resource;

    public synchronized void load(final Resource resource) throws IOException {
        this.resource = resource;
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
    public void afterPropertiesSet() throws Exception {
        checkNotNull(resource);
        checkArgument(resource.exists());
    }
}
