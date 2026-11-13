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

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Test;

import org.alfresco.repo.transaction.InMemoryTransactionHelper;

/**
 * Sends of {@link TransactionAwareEventProducer} with and without deferred sends. Each test runs on its own thread, so the per-thread deferred mode never leaks into other tests.
 */
public class TransactionAwareEventProducerDeferredSendsTest
{
    private static final long TIMEOUT_SECONDS = 60;

    private final InMemoryTransactionHelper transactions = new InMemoryTransactionHelper();
    private final TestBroker broker = new TestBroker();
    private final TransactionAwareEventProducer eventProducer = broker.eventProducer();
    private final ExecutorService threads = Executors.newCachedThreadPool();

    @After
    public void tearDown()
    {
        threads.shutdownNow();
    }

    @Test
    public void withoutDeferredSendsMessagesAreSentAfterCommitAndNotAfterRollback() throws Exception
    {
        onOwnThread(() -> {
            transactions.doInTransaction(() -> {
                eventProducer.send("test:queue", "committed");
                assertEquals(List.of(), broker.received());
                return null;
            });
            assertEquals(List.of("committed"), broker.delivered());

            assertThrows(IllegalStateException.class, () -> transactions.doInTransaction(() -> {
                eventProducer.send("test:queue", "rolled back");
                throw new IllegalStateException("roll back");
            }));
            assertEquals(List.of("committed"), broker.receivedBodies());
            return null;
        });
    }

    @Test
    public void deferredMessagesOfCommittedTransactionsAreSentInOrderOutsideAnyTransaction() throws Exception
    {
        onOwnThread(() -> {
            TransactionAwareEventProducer.enableDeferredSendsOnThisThread();
            send("a");
            send("b", "c");
            assertThrows(IllegalStateException.class, () -> transactions.doInTransaction(() -> {
                eventProducer.send("test:queue", "rolled back");
                throw new IllegalStateException("roll back");
            }));
            assertEquals(List.of(), broker.received());

            TransactionAwareEventProducer.sendAndDisableDeferredSendsOnThisThread();
            assertEquals(List.of("a", "b", "c"), broker.delivered());
            broker.received().forEach(send -> assertTrue(send.toString(), send.outsideTransaction()));

            // Deferred sends are off again: the next commit sends at once.
            send("d");
            assertEquals(List.of("a", "b", "c", "d"), broker.delivered());
            return null;
        });
    }

    @Test
    public void failedDeferredSendDoesNotStopTheOthers() throws Exception
    {
        broker.reject("a");
        onOwnThread(() -> {
            TransactionAwareEventProducer.enableDeferredSendsOnThisThread();
            send("a", "b");
            send("c");
            TransactionAwareEventProducer.sendAndDisableDeferredSendsOnThisThread();
            return null;
        });
        assertEquals(List.of("a", "b", "c"), broker.receivedBodies());
        assertEquals(List.of("b", "c"), broker.delivered());
    }

    @Test
    public void deferredSendsAffectOnlyTheThreadThatEnabledThem() throws Exception
    {
        onOwnThread(() -> {
            TransactionAwareEventProducer.enableDeferredSendsOnThisThread();
            onOwnThread(() -> send("other thread"));
            assertEquals(List.of("other thread"), broker.delivered());

            send("deferred");
            assertEquals(List.of("other thread"), broker.delivered());
            TransactionAwareEventProducer.sendAndDisableDeferredSendsOnThisThread();
            assertEquals(List.of("other thread", "deferred"), broker.delivered());
            return null;
        });
    }

    @Test
    public void deferredSendsMustBeEnabledFirst() throws Exception
    {
        onOwnThread(() -> {
            DeferredSends<String> deferredSends = new DeferredSends<>();
            assertFalse(deferredSends.isDeferredSendsEnabledOnThisThread());
            assertThrows(IllegalStateException.class, () -> deferredSends.addToDeferred(List.of("a")));
            assertThrows(IllegalStateException.class, deferredSends::consumeAndDisableDeferredSends);

            deferredSends.enableDeferredSendsOnThisThread();
            assertTrue(deferredSends.isDeferredSendsEnabledOnThisThread());
            deferredSends.addToDeferred(List.of("a", "b"));
            deferredSends.addToDeferred(List.of("c"));
            assertEquals(List.of("a", "b", "c"), deferredSends.consumeAndDisableDeferredSends());
            assertFalse(deferredSends.isDeferredSendsEnabledOnThisThread());
            return null;
        });
    }

    private Void send(String... messages)
    {
        return transactions.doInTransaction(() -> {
            for (String message : messages)
            {
                eventProducer.send("test:queue", message);
            }
            return null;
        });
    }

    private <T> T onOwnThread(Callable<T> work) throws Exception
    {
        return threads.submit(work).get(TIMEOUT_SECONDS, SECONDS);
    }
}
