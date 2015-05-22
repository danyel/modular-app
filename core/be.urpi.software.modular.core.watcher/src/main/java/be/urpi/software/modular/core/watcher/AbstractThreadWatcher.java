package be.urpi.software.modular.core.watcher;

import be.urpi.software.modular.core.watcher.directory.DirectoryWatchAble;
import be.urpi.software.modular.core.watcher.file.FileWatchAble;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;
import static java.lang.Boolean.TRUE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public abstract class AbstractThreadWatcher<WA extends WatchAble> extends Thread implements Watcher<WA> {
    private final WA watchAble;
    private AtomicBoolean stop = new AtomicBoolean(false);

    protected AbstractThreadWatcher(final WA watchAble) throws WatchAbleException {
        checkNotNull(watchAble);
        try {
            watchAble.afterPropertiesSet();
        } catch (final Exception exception) {
            propagate(new WatchAbleException(exception));
        }
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
            final Path path = watchAble instanceof FileWatchAble ? getFile().toPath().getParent() : getFile().toPath();
            path.register(watcher, on());

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
                    } else if (kind == on()) {
                        if (watchAble instanceof FileWatchAble && filename.toString().equals(getFile().getName())) {
                            doOnChange();
                        }

                        if (watchAble instanceof DirectoryWatchAble) {
                            doOnChange();
                        }
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

    WatchEvent.Kind<Path> on(){
        return watchAble.on();
    }
}
