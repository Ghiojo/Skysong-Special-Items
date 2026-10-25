package org.skysongdev.skysongitems.database;

import org.bukkit.plugin.Plugin;

import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * Runs book queries off the server's main thread, so a slow or unreachable database
 * no longer freezes the server.
 *
 * <p>Every query runs on one worker thread, one after another in the order it was
 * submitted, just as when they all ran on the main thread: a book saved by one command
 * is always stored before a later command reads it. The result, or the
 * {@link SQLException}, is then handed back on the main thread on the next tick, where
 * it is safe to message players and change their items.
 */
public final class BookQueries {
    private static final long SHUTDOWN_TIMEOUT_SECONDS = 10;

    private final Plugin plugin;
    private final Supplier<BookDescriptionRepository> repository;
    private final ExecutorService worker;

    /**
     * @param repository gives the repository in use when a query is submitted; it is
     *                   called on the main thread, so it may read the plugin's state
     * @param worker     runs the queries; it should have a single thread to keep their order
     */
    public BookQueries(Plugin plugin, Supplier<BookDescriptionRepository> repository, ExecutorService worker) {
        this.plugin = plugin;
        this.repository = repository;
        this.worker = worker;
    }

    /** Book queries for this plugin, run on a dedicated worker thread. */
    public static BookQueries onWorkerThread(Plugin plugin, Supplier<BookDescriptionRepository> repository) {
        return new BookQueries(plugin, repository, Executors.newSingleThreadExecutor(task -> {
            Thread thread = new Thread(task, plugin.getName() + "-database");
            thread.setDaemon(true);
            thread.setUncaughtExceptionHandler((failed, error) ->
                    plugin.getLogger().log(Level.SEVERE, "Unexpected error in a database query", error));
            return thread;
        }));
    }

    /**
     * Runs {@code query} on the worker thread, then passes its result to
     * {@code onResult}, or its exception to {@code onError}, on the main thread.
     * Nothing is passed back once the plugin is disabled.
     */
    public <T> void submit(Query<T> query, Consumer<? super T> onResult, Consumer<SQLException> onError) {
        BookDescriptionRepository books = repository.get();
        worker.execute(() -> {
            try {
                T result = query.run(books);
                onMainThread(() -> onResult.accept(result));
            } catch (SQLException e) {
                onMainThread(() -> onError.accept(e));
            }
        });
    }

    /**
     * Stops taking queries and waits up to ten seconds for the ones already
     * submitted, so books being saved when the server stops are not lost.
     */
    public void shutdown() {
        worker.shutdown();
        try {
            if (!worker.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                plugin.getLogger().warning("Database queries were still running when the plugin stopped");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void onMainThread(Runnable task) {
        if (plugin.isEnabled()) {
            plugin.getServer().getScheduler().runTask(plugin, task);
        }
    }

    /** Work done with the book repository on the worker thread. */
    @FunctionalInterface
    public interface Query<T> {
        T run(BookDescriptionRepository books) throws SQLException;
    }
}
