package be.urpi.software.modular.core.application.reload;

import be.urpi.software.modular.core.watcher.file.FileWatchAble;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * This watchable will only check if there are jar files located in the directory that will be configured by spring.
 * If there are jar files it will go from this directory to another.
 * The spring configuration:
 * - module.reload.source: this is the directory where the jars will be stored or uploaded
 * - module.reload.destination: that is the location where we are going to move the file to.
 *
 * @author danyel
 */
@SuppressWarnings({"unused", "ClassCanBeRecord"})
@Slf4j
public class ClassPathSourceDirectory implements FileWatchAble {
    private final File sourceDirectory;
    private final File destinationDirectory;

    public ClassPathSourceDirectory(String source, String destination) {
        this.sourceDirectory = new File(source);
        this.destinationDirectory = new File(destination);
    }

    @Override
    public File getFile() {
        return sourceDirectory;
    }

    @Override
    public WatchEvent.Kind<Path>[] on() {
        //noinspection unchecked
        return (WatchEvent.Kind<Path>[]) new WatchEvent.Kind[]{ENTRY_MODIFY};
    }

    @Override
    public void checkState() {
        Preconditions.checkState(sourceDirectory.exists());
        Preconditions.checkState(destinationDirectory.exists());
    }

    @Override
    public void doOnChange(File file) throws IOException {
        File jarFile = new File(sourceDirectory, file.getName());
        if (jarFile.getName().endsWith(".jar")) {
            File sourceFile = new File(sourceDirectory, jarFile.getName());
            File destinationFile = new File(destinationDirectory, jarFile.getName());
            log.debug("Changing file {} from {} to {}", jarFile.getName(), sourceFile.getAbsolutePath(), destinationFile.getAbsolutePath());
            FileUtils.copyFile(sourceFile, destinationFile);
            log.debug("Deleting file {}", sourceFile.getAbsolutePath());
            FileUtils.forceDelete(sourceFile);
        }
    }
}
