package be.urpi.software.modular.core.watcher;

import be.urpi.software.modular.core.watcher.file.FileWatchAble;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Boolean.TRUE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
public class ThreadWatcher extends Thread implements Watcher<FileWatchAble> {
    private final LinkedList<FileWatchAble> watchAbles;
    private final AtomicBoolean stop = new AtomicBoolean(false);
    private final WatchService watchService;
    @Setter
    private File observablePath;

    public ThreadWatcher(LinkedList<FileWatchAble> watchAbles) throws WatchAbleException {
        checkNotNull(watchAbles);
        try {
            watchService = FileSystems.getDefault().newWatchService();
            this.watchAbles = watchAbles;
            for (FileWatchAble wa : watchAbles) {
                wa.checkState();
            }
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
                log.debug("Starting the thread {}", observablePath.getAbsolutePath());
                Path path = observablePath.toPath();
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
                WatchKey key = watchService.poll(5, SECONDS);
                if (key == null) {
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == OVERFLOW) continue;

                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path filename = ev.context();
                    for (FileWatchAble watchAble : watchAbles) {
                        log.debug("File Watchable: {} and for folder: {}", watchAble.getClass().getSimpleName(), filename.toFile().getAbsolutePath());
                        watchAble.doOnChange(filename.toFile());
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

    WatchEvent.Kind<Path>[] on() {
        return watchAbles.getFirst().on();
    }
}
