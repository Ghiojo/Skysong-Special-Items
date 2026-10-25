package org.skysongdev.skysongitems.testsupport;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Runs each task straight away on the thread that submits it, so tests of code that
 * queues work on another thread can check the outcome without waiting.
 */
public final class DirectExecutorService extends AbstractExecutorService {
    private boolean shutdown;

    @Override
    public void execute(Runnable command) {
        if (shutdown) {
            throw new RejectedExecutionException("The executor is shut down");
        }
        command.run();
    }

    @Override
    public void shutdown() {
        shutdown = true;
    }

    @Override
    public List<Runnable> shutdownNow() {
        shutdown = true;
        return List.of();
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public boolean isTerminated() {
        return shutdown;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) {
        return true;
    }
}
