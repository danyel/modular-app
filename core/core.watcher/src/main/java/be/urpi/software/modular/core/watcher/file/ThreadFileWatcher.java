package be.urpi.software.modular.core.watcher.file;

import be.urpi.software.modular.core.watcher.AbstractThreadWatcher;
import be.urpi.software.modular.core.watcher.WatchAbleException;

public class ThreadFileWatcher extends AbstractThreadWatcher<FileWatchAble> implements FileWatcher {
    public ThreadFileWatcher(FileWatchAble fileWatchAble) throws WatchAbleException {
        super(fileWatchAble);
    }
}
