package be.urpi.software.modular.core.file.watcher;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Throwables.propagate;
import static java.lang.Boolean.TRUE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ThreadFileWatcher extends Thread implements FileWatcher {
    private final WatchAble watchAble;
    private AtomicBoolean stop = new AtomicBoolean(false);

    public ThreadFileWatcher(final WatchAble watchAble) throws Exception {
        Preconditions.checkNotNull(watchAble);
        watchAble.afterPropertiesSet();
        this.watchAble = watchAble;
    }

    @Override
    public synchronized void stopThread() {
        stop.set(TRUE);
    }

    @Override
    public synchronized void startThread() {
        try {
            doOnStart();
            super.start();
        } catch (final IOException e) {
            throw propagate(e);
        }
    }

    @Override
    public boolean isActive() {
        return !stop.get();
    }

    @Override
    public void run() {
        try (final WatchService watcher = FileSystems.getDefault().newWatchService()) {
            final Path path = getFile().toPath().getParent();
            path.register(watcher, ENTRY_MODIFY);

            while (isActive()) {
                WatchKey key;
                try {
                    key = watcher.poll(25, MILLISECONDS);
                } catch (final InterruptedException e) {
                    return;
                }
                if (key == null) {
                    Thread.yield();
                    continue;
                }

                for (final WatchEvent<?> polledWatchEvent : key.pollEvents()) {
                    final WatchEvent.Kind<?> kind = polledWatchEvent.kind();

                    @SuppressWarnings("unchecked")
                    final WatchEvent<Path> pathWatchEvent = (WatchEvent<Path>) polledWatchEvent;
                    final Path filename = pathWatchEvent.context();

                    if (kind == OVERFLOW) {
                        Thread.yield();
                        continue;
                    } else if (kind == ENTRY_MODIFY && filename.toString().equals(getFile().getName())) {
                        doOnChange();
                    }
                    final boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                }
                Thread.yield();
            }
        } catch (final Throwable e) {
            throw propagate(e);
        }
    }

    File getFile() throws IOException {
        return watchAble.getFile();
    }

    void doOnChange() throws IOException {
        watchAble.doOnChange();
    }

    void doOnStart() throws IOException {
        watchAble.doOnStart();
    }
}
