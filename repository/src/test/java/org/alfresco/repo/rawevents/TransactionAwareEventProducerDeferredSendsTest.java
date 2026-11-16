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
package org.alfresco.repo.rawevents;

import static java.util.concurrent.TimeUnit.SECONDS;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.junit.After;
import org.junit.Test;

import org.alfresco.repo.transaction.InMemoryTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;

/**
 * Sends of {@link TransactionAwareEventProducer} with and without {@link TransactionAwareEventProducer#doInTransactionThenSend}. Each test runs on its own thread, so the per-thread deferred mode never leaks into other tests.
 */
public class TransactionAwareEventProducerDeferredSendsTest
{
    private static final long TIMEOUT_SECONDS = 60;

    private final InMemoryTransactionHelper transactions = new InMemoryTransactionHelper();
    private final StubBroker broker = new StubBroker();
    private final TransactionAwareEventProducer eventProducer = broker.eventProducer(transactions.transactionService());
    private final ExecutorService threads = Executors.newCachedThreadPool();

    @After
    public void tearDown()
    {
        threads.shutdownNow();
    }

    @Test
    public void withoutDeferredSendsMessagesAreSentAfterCommitAndNotAfterRollback() throws Exception
    {
        RetryingTransactionCallback<Void> sendBeforeCommit = () -> {
            send("committed");
            assertEquals(List.of(), broker.received());
            return null;
        };
        RetryingTransactionCallback<Void> sendThenRollBack = () -> {
            send("rolled back");
            throw new IllegalStateException("roll back");
        };
        Callable<Void> commitThenRollBack = () -> {
            transactions.doInTransaction(sendBeforeCommit);
            assertEquals(List.of("committed"), broker.delivered());
            assertThrows(IllegalStateException.class, () -> transactions.doInTransaction(sendThenRollBack));
            return null;
        };

        onOwnThread(commitThenRollBack);

        assertEquals(List.of("committed"), broker.receivedBodies());
    }

    @Test
    public void messagesAreSentInOrderAfterTheTransactionOutsideAnyTransaction() throws Exception
    {
        RetryingTransactionCallback<String> sendThree = () -> {
            send("a", "b");
            send("c");
            return "result";
        };
        Callable<String> deferThenCommit = () -> {
            String value = doInTransactionThenSend(sendThree);
            broker.received().forEach(send -> assertTrue(send.toString(), send.outsideTransaction()));

            // Deferred sends are off again: the next commit sends at once.
            commit("d");
            assertEquals(List.of("a", "b", "c", "d"), broker.delivered());
            return value;
        };

        assertEquals("result", onOwnThread(deferThenCommit));
    }

    @Test
    public void committedNestedTransactionsAreSentEvenIfTheTransactionFails() throws Exception
    {
        RetryingTransactionCallback<Void> commitInnerThenRollBack = () -> {
            commitNew("inner");
            send("rolled back");
            throw new IllegalStateException("roll back");
        };
        Callable<Void> failThenCommit = () -> {
            assertThrows(IllegalStateException.class, () -> doInTransactionThenSend(commitInnerThenRollBack));
            commit("next");
            return null;
        };

        onOwnThread(failThenCommit);

        assertEquals(List.of("inner", "next"), broker.delivered());
    }

    @Test
    public void messagesOfANestedRequiresNewTransactionAreDeferredToo() throws Exception
    {
        List<String> sentDuringOuter = new ArrayList<>();
        RetryingTransactionCallback<Void> outerWithInner = () -> {
            send("outer");
            commitNew("inner");
            sentDuringOuter.addAll(broker.receivedBodies());
            return null;
        };

        doInTransactionThenSendOnOwnThread(outerWithInner);

        assertEquals(List.of(), sentDuringOuter);
        assertEquals(List.of("inner", "outer"), broker.delivered());
        broker.received().forEach(send -> assertTrue(send.toString(), send.outsideTransaction()));
    }

    @Test
    public void nestedCallJoinsTheOuterOne() throws Exception
    {
        List<String> sentDuringOuter = new ArrayList<>();
        RetryingTransactionCallback<Void> inner = () -> commitNew("b");
        RetryingTransactionCallback<Void> outer = () -> {
            send("a");
            doInTransactionThenSend(inner);
            send("c");
            sentDuringOuter.addAll(broker.receivedBodies());
            return null;
        };

        doInTransactionThenSendOnOwnThread(outer);

        assertEquals(List.of(), sentDuringOuter);
        assertEquals(List.of("b", "a", "c"), broker.delivered());
    }

    @Test
    public void failedDeferredSendDoesNotStopTheOthers() throws Exception
    {
        broker.reject("a");
        RetryingTransactionCallback<Void> sendThree = () -> {
            send("a", "b", "c");
            return null;
        };

        doInTransactionThenSendOnOwnThread(sendThree);

        assertEquals(List.of("a", "b", "c"), broker.receivedBodies());
        assertEquals(List.of("b", "c"), broker.delivered());
    }

    @Test
    public void deferredSendsAffectOnlyTheThreadThatDefersThem() throws Exception
    {
        List<String> deliveredDuringOuter = new ArrayList<>();
        RetryingTransactionCallback<Void> commitOnBothThreads = () -> {
            commitOnAnotherThread("other thread");
            commitNew("deferred");
            deliveredDuringOuter.addAll(broker.delivered());
            return null;
        };

        doInTransactionThenSendOnOwnThread(commitOnBothThreads);

        assertEquals(List.of("other thread"), deliveredDuringOuter);
        assertEquals(List.of("other thread", "deferred"), broker.delivered());
    }

    @Test
    public void sendsCanOnlyBeDeferredInsideRun() throws Exception
    {
        DeferredSends<String> deferredSends = new DeferredSends<>();
        List<List<String>> sent = new ArrayList<>();
        Supplier<String> deferThree = () -> {
            assertTrue(deferredSends.isDeferring());
            deferredSends.defer(List.of("a", "b"));
            deferredSends.defer(List.of("c"));
            return "result";
        };
        Callable<String> deferOnlyInsideRun = () -> {
            assertFalse(deferredSends.isDeferring());
            assertThrows(IllegalStateException.class, () -> deferredSends.defer(List.of("a")));
            String value = deferredSends.run(deferThree, sent::add);
            assertFalse(deferredSends.isDeferring());
            return value;
        };

        assertEquals("result", onOwnThread(deferOnlyInsideRun));
        assertEquals(List.of(List.of("a", "b", "c")), sent);
    }

    private <R> R doInTransactionThenSend(RetryingTransactionCallback<R> callback)
    {
        return eventProducer.doInTransactionThenSend(callback);
    }

    private <R> R doInTransactionThenSendOnOwnThread(RetryingTransactionCallback<R> callback) throws InterruptedException, ExecutionException, TimeoutException
    {
        return onOwnThread(() -> doInTransactionThenSend(callback));
    }

    /** Sends in the current transaction. */
    private void send(String... messages)
    {
        for (String message : messages)
        {
            eventProducer.send("test:queue", message);
        }
    }

    /** Sends in a transaction of its own, or in the current one if there is one. */
    private Void commit(String... messages)
    {
        return transactions.runInTransaction(() -> send(messages));
    }

    /** Sends in a new transaction that commits at once, also inside another one. */
    private Void commitNew(String... messages)
    {
        return transactions.doInTransaction(() -> {
            send(messages);
            return null;
        }, false, true);
    }

    private void commitOnAnotherThread(String message)
    {
        CompletableFuture.runAsync(() -> commit(message), threads).orTimeout(TIMEOUT_SECONDS, SECONDS).join();
    }

    private <T> T onOwnThread(Callable<T> work) throws InterruptedException, ExecutionException, TimeoutException
    {
        return threads.submit(work).get(TIMEOUT_SECONDS, SECONDS);
    }
}
