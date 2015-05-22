package be.urpi.software.modular.core.watcher.directory;

import be.urpi.software.modular.core.watcher.AbstractThreadWatcher;
import be.urpi.software.modular.core.watcher.WatchAbleException;

import java.util.concurrent.atomic.AtomicBoolean;

public class ThreadDirectoryWatcher extends AbstractThreadWatcher<DirectoryWatchAble> implements DirectoryWatcher {
    private AtomicBoolean stop = new AtomicBoolean(false);

    public ThreadDirectoryWatcher(final DirectoryWatchAble directoryWatchAble) throws WatchAbleException {
        super(directoryWatchAble);
    }
}
