package be.urpi.software.modular.core.watcher;

import be.urpi.software.modular.core.watcher.directory.DirectoryWatchAble;
import be.urpi.software.modular.core.watcher.file.FileWatchAble;
import org.springframework.beans.factory.InitializingBean;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.Boolean.TRUE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public abstract class AbstractThreadWatcher<WA extends WatchAble> extends Thread implements Watcher<WA>, InitializingBean {
    private final WA watchAble;
    private final AtomicBoolean stop = new AtomicBoolean(false);

    protected AbstractThreadWatcher( WA watchAble) throws WatchAbleException {
        checkNotNull(watchAble);
        try {
            watchAble.afterPropertiesSet();
        } catch (final Exception exception) {
            throw new WatchAbleException(exception);
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
            throw new WatchAbleException(e);
        }
    }

    @Override
    public boolean isActive() {
        return !stop.get();
    }

    @Override
    public void run() {
        try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
            Path path = watchAble instanceof FileWatchAble ? getFile().toPath().getParent() : getFile().toPath();
            path.register(watcher, on());

            while (isActive()) {
                try {
                    WatchKey key = watcher.poll(25, MILLISECONDS);
                    if (key == null) {
                        Thread.yield();
                        continue;
                    }
                    for (WatchEvent<?> polledWatchEvent : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = polledWatchEvent.kind();

                        @SuppressWarnings("unchecked") final WatchEvent<Path> pathWatchEvent = (WatchEvent<Path>) polledWatchEvent;
                        Path filename = pathWatchEvent.context();

                        if (kind == OVERFLOW) {
                            Thread.yield();
                            continue;
                        } else if (kind == on()) {
                            if (watchAble instanceof FileWatchAble fileWatchAble && filename.toString().equals(getFile().getName())) {
                                fileWatchAble.doOnChange();
                            }

                            if (watchAble instanceof DirectoryWatchAble directoryWatchAble) {
                                directoryWatchAble.doOnChange(filename.toFile());
                            }
                        }
                        boolean valid = key.reset();
                        if (!valid) {
                            break;
                        }
                    }
                    Thread.yield();
                } catch (final InterruptedException e) {
                    return;
                }
            }
        } catch (final Throwable e) {
            throw new WatchAbleException(e);
        }
    }

    File getFile() throws IOException {
        return watchAble.getFile();
    }

    void doOnStart() throws IOException {
        watchAble.doOnStart();
    }

    WatchEvent.Kind<Path> on() {
        return watchAble.on();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        checkState(watchAble instanceof FileWatchAble || watchAble instanceof DirectoryWatchAble);
    }
}
