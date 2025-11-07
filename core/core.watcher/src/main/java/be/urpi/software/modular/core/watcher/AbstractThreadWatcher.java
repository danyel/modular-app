package be.urpi.software.modular.core.watcher;

import be.urpi.software.modular.core.watcher.directory.DirectoryWatchAble;
import be.urpi.software.modular.core.watcher.file.FileWatchAble;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.Boolean.TRUE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
public abstract class AbstractThreadWatcher<WA extends WatchAble> extends Thread implements Watcher<WA>, InitializingBean {
    private final WA watchAble;
    private final AtomicBoolean stop = new AtomicBoolean(false);
    private final WatchService watchService;

    protected AbstractThreadWatcher(WA watchAble) throws WatchAbleException {
        checkNotNull(watchAble);
        try {
            watchService = FileSystems.getDefault().newWatchService();
            this.watchAble = watchAble;
            log.info("Starting watcher {}", watchAble.getFile().getAbsolutePath());
            watchAble.afterPropertiesSet();
        } catch (Exception exception) {
            throw new WatchAbleException(exception);
        }
    }

    @Override
    public synchronized void stopThread() {
        stop.set(TRUE);
    }

    @Override
    public void start() {
        log.info("Starting watcher");
        startThread();
        super.start();
    }

    @Override
    public synchronized void startThread() {
        if (watchService != null) {
            try {
                log.debug("Starting the thread {}", watchAble.getFile().getAbsolutePath());
                doOnStart();
                Path path = watchAble instanceof FileWatchAble ? getFile().toPath().getParent() : getFile().toPath();
                log.debug("Registering {} on the watch service", path.getFileName());
                path.register(watchService, on());
            } catch (IOException e) {
                throw new WatchAbleException(e);
            }
        }
    }

    @Override
    public boolean isActive() {
        return !stop.get();
    }

    @Override
    public void run() throws WatchAbleException {
        log.debug("Running {}", getClass().getSimpleName());
        while (isActive()) {
            try {
                // every 20 seconds
                WatchKey key = watchService.poll(20, SECONDS);
                if (key == null) {
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == OVERFLOW) continue;

                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path filename = ev.context();

                    if (watchAble instanceof FileWatchAble fileWatchAble &&
                            filename.toString().equals(getFile().getName())) {
                        fileWatchAble.doOnChange();
                    }

                    if (watchAble instanceof DirectoryWatchAble directoryWatchAble) {
                        directoryWatchAble.doOnChange(filename.toFile());
                    }
                }

                boolean valid = key.reset();
                if (!valid) break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Watcher error", e);
                break;
            }
        }
    }

    File getFile() throws IOException {
        return watchAble.getFile();
    }

    void doOnStart() throws IOException {
        watchAble.doOnStart();
    }

    WatchEvent.Kind<Path>[] on() {
        return watchAble.on();
    }

    @Override
    public void afterPropertiesSet() {
        checkState(watchAble instanceof FileWatchAble || watchAble instanceof DirectoryWatchAble);
    }
}
