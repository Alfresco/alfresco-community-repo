/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2026 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.repo.util;

import static java.util.concurrent.TimeUnit.SECONDS;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.rawevents.TestBroker;
import org.alfresco.repo.rawevents.TransactionAwareEventProducer;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.InMemoryTransactionHelper;

public class PostTxnCallbackSchedulerTest
{
    private static final long TIMEOUT_SECONDS = 60;
    private static final Duration TIMEOUT = Duration.ofSeconds(TIMEOUT_SECONDS);
    private static final String DISPATCH_THREAD_PREFIX = "rendition-dispatch-";

    private final InMemoryTransactionHelper transactions = new InMemoryTransactionHelper();
    private final ExecutorService requestThreads = Executors.newCachedThreadPool();
    private final TestBroker broker = new TestBroker();
    private final TransactionAwareEventProducer eventProducer = broker.eventProducer();
    private final List<CountDownLatch> latches = new CopyOnWriteArrayList<>();
    private final List<String> runs = new CopyOnWriteArrayList<>();
    private PostTxnCallbackScheduler scheduler;

    @BeforeClass
    public static void initAuthenticationUtil() throws Exception
    {
        new AuthenticationUtil().afterPropertiesSet();
    }

    @After
    public void tearDown()
    {
        broker.start();
        latches.forEach(CountDownLatch::countDown);
        if (scheduler != null)
        {
            scheduler.destroy();
        }
        requestThreads.shutdownNow();
    }

    @Test
    public void callbackRunsAfterCommitOnADispatchThreadInItsOwnTransaction() throws Exception
    {
        startScheduler(1);
        AtomicReference<String> requestTransaction = new AtomicReference<>();
        AtomicReference<String> callbackTransaction = new AtomicReference<>();
        AtomicReference<String> callbackThread = new AtomicReference<>();
        CountDownLatch done = latch();

        commit(() -> {
            requestTransaction.set(AlfrescoTransactionSupport.getTransactionId());
            schedule("a", () -> {
                callbackThread.set(Thread.currentThread().getName());
                callbackTransaction.set(AlfrescoTransactionSupport.getTransactionId());
                done.countDown();
            });
        });

        awaitLatch(done);
        assertTrue(callbackThread.get(), callbackThread.get().startsWith(DISPATCH_THREAD_PREFIX));
        assertNotNull(callbackTransaction.get());
        assertNotEquals(requestTransaction.get(), callbackTransaction.get());
    }

    @Test
    public void callbackRunsAsTheUserAndTenantThatScheduledIt() throws Exception
    {
        startScheduler(1);
        AtomicReference<String> user = new AtomicReference<>();
        AtomicReference<String> tenant = new AtomicReference<>();
        CountDownLatch done = latch();

        // The transaction commits as bob, but alice in tenant acme.example scheduled the callback.
        submitAndWait(() -> AuthenticationUtil.runAs(() -> transactions.doInTransaction(() -> TenantUtil.runAsUserTenant(() -> {
            schedule("a", () -> {
                user.set(AuthenticationUtil.getRunAsUser());
                tenant.set(TenantUtil.getCurrentDomain());
                done.countDown();
            });
            return null;
        }, "alice", "acme.example")), "bob"));

        awaitLatch(done);
        assertEquals("alice", user.get());
        assertEquals("acme.example", tenant.get());
    }

    @Test
    public void callbackRunsAsSystemWhenNoUserIsAuthenticated() throws Exception
    {
        startScheduler(1);
        AtomicReference<String> user = new AtomicReference<>();
        CountDownLatch done = latch();

        commit(() -> schedule("a", () -> {
            user.set(AuthenticationUtil.getRunAsUser());
            done.countDown();
        }));

        awaitLatch(done);
        assertEquals(AuthenticationUtil.getSystemUserName(), user.get());
    }

    @Test
    public void committingThreadDoesNotWaitForTheCallback() throws Exception
    {
        startScheduler(1);
        CountDownLatch started = latch();
        CountDownLatch release = latch();
        CountDownLatch finished = latch();

        // commit() fails if the committing thread waits for the callback, which cannot finish before release.
        commit(() -> schedule("slow", () -> {
            started.countDown();
            awaitQuietly(release);
            finished.countDown();
        }));

        awaitLatch(started);
        assertEquals(1, finished.getCount());
        assertEquals(1, scheduler.getActiveCount());
        release.countDown();
        awaitLatch(finished);
    }

    @Test
    public void callbackDoesNotRunWhenTheTransactionRollsBack() throws Exception
    {
        startScheduler(1);
        CountDownLatch sentinel = latch();

        ExecutionException e = assertThrows(ExecutionException.class, () -> submitAndWait(() -> transactions.doInTransaction(() -> {
            schedule("rolled-back", () -> runs.add("rolled-back"));
            throw new IllegalStateException("roll back");
        })));
        assertEquals("roll back", e.getCause().getMessage());

        // One worker runs callbacks in order, so the sentinel runs after anything queued before it.
        commit(() -> schedule("sentinel", sentinel::countDown));
        awaitLatch(sentinel);
        assertEquals(List.of(), runs);
        assertEquals(0, scheduler.getPendingCount());
    }

    @Test
    public void queuedCallbackIsReplacedByANewerOneWithTheSameId() throws Exception
    {
        startScheduler(1);
        CountDownLatch release = occupyTheOnlyWorker();
        CountDownLatch sentinel = latch();

        commit(() -> schedule("x", () -> runs.add("x v1")));
        commit(() -> schedule("x", () -> runs.add("x v2")));
        commit(() -> schedule("y", () -> {
            runs.add("y");
            sentinel.countDown();
        }));
        assertEquals(2, scheduler.getPendingCount());

        release.countDown();
        awaitLatch(sentinel);
        assertEquals(List.of("x v2", "y"), runs);
    }

    @Test
    public void firstCallbackForAnIdWinsWithinOneTransaction() throws Exception
    {
        startScheduler(1);
        CountDownLatch bothRan = new CountDownLatch(2);
        latches.add(bothRan);
        CountDownLatch sentinel = latch();

        commit(() -> {
            schedule("x", () -> {
                runs.add("x v1");
                bothRan.countDown();
            });
            schedule("x", () -> runs.add("x v2"));
            schedule("y", () -> {
                runs.add("y");
                bothRan.countDown();
            });
        });
        awaitLatch(bothRan);
        commit(() -> schedule("sentinel", sentinel::countDown));
        awaitLatch(sentinel);

        assertEquals(Set.of("x v1", "y"), Set.copyOf(runs));
        assertEquals(2, runs.size());
    }

    @Test
    public void failingCallbackDoesNotStopLaterCallbacks() throws Exception
    {
        startScheduler(1);
        CountDownLatch next = latch();

        commit(() -> schedule("fails", () -> {
            throw new IllegalStateException("callback failed");
        }));
        commit(() -> schedule("next", next::countDown));

        awaitLatch(next);
    }

    @Test
    public void messagesAreSentOnlyAfterTheCallbackTransactionHasFinished() throws Exception
    {
        startScheduler(1);

        commit(() -> schedule("a", () -> {
            eventProducer.send("test:queue", "m1");
            eventProducer.send("test:queue", "m2");
        }));

        await().atMost(TIMEOUT).until(() -> broker.delivered().size() == 2);
        assertEquals(List.of("m1", "m2"), broker.delivered());
        assertSentOnDispatchThreadsOutsideTransactions(broker.received());
    }

    @Test
    public void unreachableBrokerHoldsOnlyTheWorkersNotTheCommittingThreads() throws Exception
    {
        startScheduler(2);
        broker.stop();

        // Each commit() fails instead of hanging if the committing thread waits for the broker.
        for (int i = 0; i < 10; i++)
        {
            String message = "m" + i;
            commit(() -> schedule(message, () -> eventProducer.send("test:queue", message)));
        }

        await().atMost(TIMEOUT).until(() -> broker.received().size() == 2);
        assertEquals(2, scheduler.getActiveCount());
        assertEquals(8, scheduler.getPendingCount());
        assertEquals(List.of(), broker.delivered());

        broker.start();
        await().atMost(TIMEOUT).until(() -> broker.delivered().size() == 10);
        assertEquals(IntStream.range(0, 10).mapToObj(i -> "m" + i).collect(Collectors.toSet()), Set.copyOf(broker.delivered()));
        assertSentOnDispatchThreadsOutsideTransactions(broker.received());
        await().atMost(TIMEOUT).until(() -> scheduler.getActiveCount() == 0);
        assertEquals(0, scheduler.getPendingCount());
    }

    @Test
    public void failedSendDoesNotStopTheOtherSends() throws Exception
    {
        startScheduler(1);
        broker.reject("bad");

        commit(() -> schedule("a", () -> {
            eventProducer.send("test:queue", "bad");
            eventProducer.send("test:queue", "good");
        }));
        commit(() -> schedule("b", () -> eventProducer.send("test:queue", "next")));

        await().atMost(TIMEOUT).until(() -> broker.delivered().size() == 2);
        assertEquals(List.of("good", "next"), broker.delivered());
    }

    @Test
    public void threadPoolSizeMustBeAtLeastOne()
    {
        for (int threadPoolSize : new int[]{0, -1})
        {
            PostTxnCallbackScheduler invalid = new PostTxnCallbackScheduler();
            invalid.setTransactionService(transactions.transactionService());
            invalid.setThreadPoolSize(threadPoolSize);
            assertThrows(AlfrescoRuntimeException.class, invalid::init);
            assertEquals(0, invalid.getActiveCount());
        }
    }

    @Test
    public void commitAfterShutdownDropsTheCallbackWithoutFailing() throws Exception
    {
        startScheduler(1);
        scheduler.destroy();

        commit(() -> schedule("late", () -> runs.add("late")));

        assertEquals(0, scheduler.getPendingCount());
        assertEquals(List.of(), runs);
    }

    private void startScheduler(int threadPoolSize)
    {
        scheduler = new PostTxnCallbackScheduler();
        scheduler.setTransactionService(transactions.transactionService());
        scheduler.setThreadPoolSize(threadPoolSize);
        scheduler.init();
    }

    /** Blocks the only worker, so callbacks committed afterwards wait in the queue. Count down the result to free it. */
    private CountDownLatch occupyTheOnlyWorker() throws Exception
    {
        CountDownLatch started = latch();
        CountDownLatch release = latch();
        commit(() -> schedule("blocker", () -> {
            started.countDown();
            awaitQuietly(release);
        }));
        awaitLatch(started);
        return release;
    }

    private void schedule(String id, Runnable callback)
    {
        scheduler.scheduleRendition(() -> {
            callback.run();
            return null;
        }, id);
    }

    /** Runs the work in a transaction on another thread, like a request thread, and waits for the commit to return. */
    private void commit(Runnable work) throws Exception
    {
        submitAndWait(() -> transactions.doInTransaction(() -> {
            work.run();
            return null;
        }));
    }

    private void submitAndWait(Runnable task) throws Exception
    {
        Future<?> future = requestThreads.submit(task);
        try
        {
            future.get(TIMEOUT_SECONDS, SECONDS);
        }
        catch (TimeoutException e)
        {
            fail("The committing thread did not return within " + TIMEOUT_SECONDS + " s");
        }
    }

    private CountDownLatch latch()
    {
        CountDownLatch latch = new CountDownLatch(1);
        latches.add(latch);
        return latch;
    }

    private static void awaitLatch(CountDownLatch latch) throws InterruptedException
    {
        assertTrue("Timed out after " + TIMEOUT_SECONDS + " s", latch.await(TIMEOUT_SECONDS, SECONDS));
    }

    private static void awaitQuietly(CountDownLatch latch)
    {
        try
        {
            latch.await(TIMEOUT_SECONDS, SECONDS);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
    }

    private static void assertSentOnDispatchThreadsOutsideTransactions(List<TestBroker.Send> sends)
    {
        for (TestBroker.Send send : sends)
        {
            assertTrue(send.toString(), send.thread().startsWith(DISPATCH_THREAD_PREFIX));
            assertTrue(send.toString(), send.outsideTransaction());
        }
    }
}
