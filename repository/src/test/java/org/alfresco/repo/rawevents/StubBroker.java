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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.mockito.quality.Strictness;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import org.alfresco.repo.transaction.AlfrescoTransactionSupport;

/**
 * Stands in for the message broker behind a {@link ProducerTemplate}. While {@link #stop() stopped}, sends block like they do when ActiveMQ is unreachable. Records the thread and transaction state of every send. No network is involved.
 */
public class StubBroker
{
    private static final long MAX_OUTAGE_MINUTES = 5;

    private final List<Send> received = new CopyOnWriteArrayList<>();
    private final List<String> delivered = new CopyOnWriteArrayList<>();
    private final Set<String> rejected = new CopyOnWriteArraySet<>();
    private final ProducerTemplate producer = mock(ProducerTemplate.class, withSettings().strictness(Strictness.LENIENT));
    private volatile CountDownLatch up = new CountDownLatch(0);

    public StubBroker()
    {
        doAnswer(invocation -> {
            String body = String.valueOf((Object) invocation.getArgument(2));
            Map<String, Object> headers = invocation.getArgument(3);
            received.add(new Send(body, headers, Thread.currentThread().getName(),
                    TransactionSynchronizationManager.isActualTransactionActive(),
                    AlfrescoTransactionSupport.getTransactionId(),
                    !TransactionSynchronizationManager.getResourceMap().isEmpty()));
            CountDownLatch latch = up;
            if (!latch.await(MAX_OUTAGE_MINUTES, TimeUnit.MINUTES))
            {
                throw new IllegalStateException("The stub broker was not started again");
            }
            if (rejected.contains(body))
            {
                throw new IllegalStateException("The stub broker rejected " + body);
            }
            delivered.add(body);
            return null;
        }).when(producer).sendBodyAndHeaders(anyString(), any(ExchangePattern.class), any(), anyMap());
    }

    public ProducerTemplate producer()
    {
        return producer;
    }

    /** @return a new {@link TransactionAwareEventProducer} that sends to this broker. */
    public TransactionAwareEventProducer eventProducer()
    {
        TransactionAwareEventProducer eventProducer = new TransactionAwareEventProducer();
        eventProducer.setProducer(producer);
        return eventProducer;
    }

    /** From now on sends wait until {@link #start()}. */
    public void stop()
    {
        up = new CountDownLatch(1);
    }

    /** Releases waiting sends. Safe to call more than once. */
    public void start()
    {
        up.countDown();
    }

    /** Sends with this body fail once the broker is up. */
    public void reject(String body)
    {
        rejected.add(body);
    }

    public List<Send> received()
    {
        return List.copyOf(received);
    }

    public List<String> receivedBodies()
    {
        return received.stream().map(Send::body).collect(Collectors.toList());
    }

    public List<String> delivered()
    {
        return List.copyOf(delivered);
    }

    /** A send as seen by the broker, with the state of the sending thread. */
    public record Send(String body, Map<String, Object> headers, String thread, boolean transactionActive, String alfrescoTransactionId, boolean resourcesBound)
    {
        /** The sending thread holds no transaction and no transaction-bound resource such as a DB connection. */
        public boolean outsideTransaction()
        {
            return !transactionActive && alfrescoTransactionId == null && !resourcesBound;
        }
    }
}
