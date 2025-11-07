package be.urpi.software.modular.core.watcher;

public interface Watcher<WA extends WatchAble> {
    void stopThread();

    void startThread();

    boolean isActive();
}
