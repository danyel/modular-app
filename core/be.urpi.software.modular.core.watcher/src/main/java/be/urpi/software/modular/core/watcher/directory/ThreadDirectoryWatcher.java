package be.urpi.software.modular.core.watcher.directory;

import be.urpi.software.modular.core.watcher.AbstractThreadWatcher;
import be.urpi.software.modular.core.watcher.WatchAbleException;

public class ThreadDirectoryWatcher extends AbstractThreadWatcher<DirectoryWatchAble> implements DirectoryWatcher {
    public ThreadDirectoryWatcher(final DirectoryWatchAble directoryWatchAble) throws WatchAbleException {
        super(directoryWatchAble);
    }
}
