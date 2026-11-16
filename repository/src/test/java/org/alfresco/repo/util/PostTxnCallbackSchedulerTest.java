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
import org.alfresco.repo.rawevents.StubBroker;
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
    private final StubBroker broker = new StubBroker();
    private final TransactionAwareEventProducer eventProducer = broker.eventProducer(transactions.transactionService());
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
        Runnable callback = () -> {
            callbackThread.set(Thread.currentThread().getName());
            callbackTransaction.set(AlfrescoTransactionSupport.getTransactionId());
            done.countDown();
        };
        Runnable request = () -> {
            requestTransaction.set(AlfrescoTransactionSupport.getTransactionId());
            schedule("a", callback);
        };

        commit(request);

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
        Runnable callback = () -> {
            user.set(AuthenticationUtil.getRunAsUser());
            tenant.set(TenantUtil.getCurrentDomain());
            done.countDown();
        };
        Runnable scheduleAsAliceInAcme = () -> TenantUtil.runAsUserTenant(() -> schedule("a", callback), "alice", "acme.example");

        // The transaction commits as bob, but alice in tenant acme.example scheduled the callback.
        submitAndWait(() -> AuthenticationUtil.runAs(() -> transactions.runInTransaction(scheduleAsAliceInAcme), "bob"));

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
        Runnable callback = () -> {
            user.set(AuthenticationUtil.getRunAsUser());
            done.countDown();
        };

        scheduleAndCommit("a", callback);

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
        Runnable slowCallback = () -> {
            started.countDown();
            awaitQuietly(release);
            finished.countDown();
        };

        // scheduleAndCommit() fails if the committing thread waits for the callback, which cannot finish before release.
        scheduleAndCommit("slow", slowCallback);

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
        Runnable scheduleThenRollBack = () -> {
            schedule("rolled-back", () -> runs.add("rolled-back"));
            throw new IllegalStateException("roll back");
        };

        ExecutionException e = assertThrows(ExecutionException.class, () -> commit(scheduleThenRollBack));
        assertEquals("roll back", e.getCause().getMessage());

        // One worker runs callbacks in order, so the sentinel runs after anything queued before it.
        scheduleAndCommit("sentinel", sentinel::countDown);
        awaitLatch(sentinel);
        assertEquals(List.of(), runs);
        assertEquals(0, scheduler.getPendingCount());
    }

    @Test
    public void queuedCallbackIsReplacedByANewerOneWithTheSameId() throws Exception
    {
        startScheduler(1);
        CountDownLatch blockerStarted = latch();
        CountDownLatch release = latch();
        CountDownLatch sentinel = latch();
        Runnable blocker = () -> {
            blockerStarted.countDown();
            awaitQuietly(release);
        };
        Runnable last = () -> {
            runs.add("y");
            sentinel.countDown();
        };

        // The blocker occupies the only worker, so the callbacks committed after it wait in the queue.
        scheduleAndCommit("blocker", blocker);
        awaitLatch(blockerStarted);
        scheduleAndCommit("x", () -> runs.add("x v1"));
        scheduleAndCommit("x", () -> runs.add("x v2"));
        scheduleAndCommit("y", last);
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
        Runnable first = () -> {
            runs.add("x v1");
            bothRan.countDown();
        };
        Runnable second = () -> runs.add("x v2");
        Runnable other = () -> {
            runs.add("y");
            bothRan.countDown();
        };
        Runnable request = () -> {
            schedule("x", first);
            schedule("x", second);
            schedule("y", other);
        };

        commit(request);
        awaitLatch(bothRan);
        scheduleAndCommit("sentinel", sentinel::countDown);
        awaitLatch(sentinel);

        assertEquals(Set.of("x v1", "y"), Set.copyOf(runs));
        assertEquals(2, runs.size());
    }

    @Test
    public void workerSendsNormallyAfterAFailingCallback() throws Exception
    {
        startScheduler(1);
        List<String> threads = new CopyOnWriteArrayList<>();
        Runnable failing = () -> {
            threads.add(Thread.currentThread().getName());
            eventProducer.send("test:queue", "rolled back");
            throw new IllegalStateException("callback failed");
        };
        Runnable next = () -> {
            threads.add(Thread.currentThread().getName());
            eventProducer.send("test:queue", "next");
        };

        scheduleAndCommit("fails", failing);
        scheduleAndCommit("next", next);

        await().atMost(TIMEOUT).until(() -> broker.delivered().size() == 1);
        assertEquals(List.of("next"), broker.delivered());
        assertEquals(2, threads.size());
        assertEquals(threads.get(0), threads.get(1));
    }

    @Test
    public void messagesAreSentOnlyAfterTheCallbackTransactionHasFinished() throws Exception
    {
        startScheduler(1);
        Runnable twoSends = () -> {
            eventProducer.send("test:queue", "m1");
            eventProducer.send("test:queue", "m2");
        };

        scheduleAndCommit("a", twoSends);

        await().atMost(TIMEOUT).until(() -> broker.delivered().size() == 2);
        assertEquals(List.of("m1", "m2"), broker.delivered());
        assertSentOnDispatchThreadsOutsideTransactions(broker.received());
    }

    @Test
    public void unreachableBrokerHoldsOnlyTheWorkersNotTheCommittingThreads() throws Exception
    {
        startScheduler(2);
        broker.stop();

        // Each scheduleAndCommit() fails instead of hanging if the committing thread waits for the broker.
        for (int i = 0; i < 10; i++)
        {
            String message = "m" + i;
            scheduleAndCommit(message, () -> eventProducer.send("test:queue", message));
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
        Runnable badThenGood = () -> {
            eventProducer.send("test:queue", "bad");
            eventProducer.send("test:queue", "good");
        };

        scheduleAndCommit("a", badThenGood);
        scheduleAndCommit("b", () -> eventProducer.send("test:queue", "next"));

        await().atMost(TIMEOUT).until(() -> broker.delivered().size() == 2);
        assertEquals(List.of("good", "next"), broker.delivered());
    }

    @Test
    public void threadPoolSizeMustBeAtLeastOne()
    {
        for (int threadPoolSize : new int[]{0, -1})
        {
            PostTxnCallbackScheduler invalid = new PostTxnCallbackScheduler();
            invalid.setTransactionAwareEventProducer(eventProducer);
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

        scheduleAndCommit("late", () -> runs.add("late"));

        assertEquals(0, scheduler.getPendingCount());
        assertEquals(List.of(), runs);
    }

    private void startScheduler(int threadPoolSize)
    {
        scheduler = new PostTxnCallbackScheduler();
        scheduler.setTransactionAwareEventProducer(eventProducer);
        scheduler.setThreadPoolSize(threadPoolSize);
        scheduler.init();
    }

    /** A request thread schedules the callback in a transaction and commits it. */
    private void scheduleAndCommit(String id, Runnable callback) throws InterruptedException, ExecutionException
    {
        commit(() -> schedule(id, callback));
    }

    /** Returns null, for callers that need a value. */
    private Void schedule(String id, Runnable callback)
    {
        scheduler.scheduleRendition(() -> {
            callback.run();
            return null;
        }, id);
        return null;
    }

    /** Runs the work in a transaction on another thread, like a request thread, and waits for the commit to return. */
    private void commit(Runnable work) throws InterruptedException, ExecutionException
    {
        submitAndWait(() -> transactions.runInTransaction(work));
    }

    private void submitAndWait(Runnable task) throws InterruptedException, ExecutionException
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

    private static void assertSentOnDispatchThreadsOutsideTransactions(List<StubBroker.Send> sends)
    {
        for (StubBroker.Send send : sends)
        {
            assertTrue(send.toString(), send.thread().startsWith(DISPATCH_THREAD_PREFIX));
            assertTrue(send.toString(), send.outsideTransaction());
        }
    }
}
