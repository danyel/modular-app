package be.urpi.software.modular.core.application.reload;

import be.urpi.software.modular.core.watcher.directory.DirectoryWatchAble;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.WatchEvent;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class ClassPathReload implements DirectoryWatchAble {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassPathReload.class);
    private String source;
    private String destination;
    private File sourceDirectory;
    private File destinationDirectory;

    public ClassPathReload() {
    }

    public ClassPathReload(final String source,
                           final String destination) {
        this.source = source;
        this.destination = destination;
    }

    @Override
    public File getFile() throws IOException {
        return sourceDirectory;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void doOnStart() throws IOException {
        // all ready checked can never be null after afterPropertiesSet() is executed
        for (final File file : destinationDirectory.listFiles()) {
            FileUtils.forceDelete(file);
        }
    }

    @Override
    public WatchEvent.Kind<Path> on() {
        return ENTRY_MODIFY;
    }

    public void setSource(final String source) {
        this.source = source;
    }

    public void setDestination(final String destination) {
        this.destination = destination;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        checkNotNull(source);
        checkNotNull(destination);
        sourceDirectory = new File(source);
        destinationDirectory = new File(destination);
        checkState(sourceDirectory.exists());
        checkState(destinationDirectory.exists());
    }

    @Override
    public void doOnChange(final File file) throws IOException {
        final String fileName = file.getName();
        final File sourceFile = new File(sourceDirectory, fileName);
        final File destinationFile = new File(destinationDirectory, fileName);
        FileUtils.copyFile(sourceFile, destinationFile);
        URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Method m;
        try {
            m = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});

            m.setAccessible(true);
            m.invoke(urlClassLoader, destinationFile.toURI().toURL());
            String cp = System.getProperty("java.class.path");
            if (cp != null) {
                cp += File.pathSeparatorChar + destinationFile.getCanonicalPath();
            } else {
                cp = destinationFile.toURI().getPath();
            }
            System.setProperty("java.class.path", cp);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            LOGGER.error(e.getMessage());
        }
    }
}
