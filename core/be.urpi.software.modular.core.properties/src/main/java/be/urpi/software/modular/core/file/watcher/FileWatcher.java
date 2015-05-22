package be.urpi.software.modular.core.file.watcher;

public interface FileWatcher {
    void stopThread();

    void startThread();

    boolean isActive();
}
