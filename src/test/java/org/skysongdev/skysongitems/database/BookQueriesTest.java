package org.skysongdev.skysongitems.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.plugin.PluginMock;
import org.skysongdev.skysongitems.testsupport.DirectExecutorService;
import org.skysongdev.skysongitems.testsupport.InMemoryBooks;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/** Covers where book queries run, in which order, and how their results come back. */
class BookQueriesTest extends MockBukkitTest {
    private PluginMock plugin;
    private BookDescriptionRepository repository;
    private final List<Object> results = Collections.synchronizedList(new ArrayList<>());

    @BeforeEach
    void setUp() throws SQLException {
        plugin = MockBukkit.createMockPlugin();
        repository = InMemoryBooks.emptyRepository();
    }

    @Test
    void queryRunsOnTheWorkerThreadAndItsResultArrivesOnTheMainThread() {
        BookQueries queries = BookQueries.onWorkerThread(plugin, () -> repository);
        Thread mainThread = Thread.currentThread();
        AtomicReference<Thread> queryThread = new AtomicReference<>();
        AtomicReference<Thread> resultThread = new AtomicReference<>();

        queries.submit(books -> {
                    queryThread.set(Thread.currentThread());
                    return books.findLatestId();
                },
                id -> {
                    resultThread.set(Thread.currentThread());
                    results.add(id);
                },
                e -> fail(e));
        queries.shutdown();

        assertTrue(results.isEmpty(), "the result is only handed back on a server tick");
        server.getScheduler().performOneTick();

        assertNotEquals(mainThread, queryThread.get());
        assertEquals(plugin.getName() + "-database", queryThread.get().getName());
        assertSame(mainThread, resultThread.get());
        assertEquals(List.of(0), results);
    }

    @Test
    void sqlExceptionIsHandedToTheErrorCallbackOnTheNextTick() {
        BookQueries queries = new BookQueries(plugin, () -> repository, new DirectExecutorService());
        SQLException failure = new SQLException("Database down");

        queries.submit(books -> {
                    throw failure;
                },
                result -> fail("No result was expected"),
                results::add);

        assertTrue(results.isEmpty(), "the error is only handed back on a server tick");
        server.getScheduler().performOneTick();

        assertEquals(List.of(failure), results);
    }

    @Test
    void queriesRunOneAfterAnotherInSubmissionOrder() {
        BookQueries queries = BookQueries.onWorkerThread(plugin, () -> repository);
        List<String> queryOrder = Collections.synchronizedList(new ArrayList<>());

        queries.submit(books -> {
                    books.create(new BookDescriptions(0, "Slow write", ""));
                    pause();
                    queryOrder.add("write");
                    return "write";
                }, results::add, e -> fail(e));
        queries.submit(books -> {
                    queryOrder.add("read");
                    return books.findById(1).getName();
                }, results::add, e -> fail(e));
        queries.shutdown();
        server.getScheduler().performOneTick();

        assertEquals(List.of("write", "read"), queryOrder);
        assertEquals(List.of("write", "Slow write"), results);
    }

    @Test
    void eachQueryUsesTheRepositoryInUseWhenItWasSubmitted() throws SQLException {
        BookDescriptionRepository replacement = InMemoryBooks.emptyRepository();
        AtomicReference<BookDescriptionRepository> current = new AtomicReference<>(repository);
        BookQueries queries = BookQueries.onWorkerThread(plugin, current::get);
        CountDownLatch release = new CountDownLatch(1);

        // Holds the worker so both queries below are queued before either one runs.
        queries.submit(books -> {
                    awaitRelease(release);
                    return null;
                }, ignored -> { }, e -> fail(e));
        queries.submit(books -> books, results::add, e -> fail(e));
        current.set(replacement);
        queries.submit(books -> books, results::add, e -> fail(e));
        release.countDown();
        queries.shutdown();
        server.getScheduler().performOneTick();

        assertEquals(List.of(repository, replacement), results);
    }

    @Test
    void nothingIsHandedBackOnceThePluginIsDisabled() {
        BookQueries queries = new BookQueries(plugin, () -> repository, new DirectExecutorService());
        server.getPluginManager().disablePlugin(plugin);

        queries.submit(books -> 1, results::add, results::add);
        server.getScheduler().performOneTick();

        assertTrue(results.isEmpty());
    }

    @Test
    void shutdownWaitsForQueriesAlreadySubmitted() {
        BookQueries queries = BookQueries.onWorkerThread(plugin, () -> repository);
        AtomicBoolean finished = new AtomicBoolean();

        queries.submit(books -> {
                    pause();
                    finished.set(true);
                    return null;
                }, ignored -> { }, e -> fail(e));
        queries.shutdown();

        assertTrue(finished.get());
    }

    private static void awaitRelease(CountDownLatch release) {
        try {
            release.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Long enough for a query submitted next to overtake this one if queries ran in parallel. */
    private static void pause() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
