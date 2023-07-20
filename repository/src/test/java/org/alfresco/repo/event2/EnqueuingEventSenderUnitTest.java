/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
package org.alfresco.repo.event2;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.alfresco.repo.event.v1.model.RepoEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class EnqueuingEventSenderUnitTest
{
    private EnqueuingEventSender eventSender;

    private Event2MessageProducer bus;
    private ExecutorService enqueuePool;
    private ExecutorService dequeuePool;
    private List<RepoEvent<?>> recordedEvents;
    private Map<String, RepoEvent<?>> events;

    @Before
    public void setup() 
    {
        eventSender = new EnqueuingEventSender();

        enqueuePool = newThreadPool();
        eventSender.setEnqueueThreadPoolExecutor(enqueuePool);
        dequeuePool = newThreadPool();
        eventSender.setDequeueThreadPoolExecutor(dequeuePool);

        bus = mock(Event2MessageProducer.class);
        eventSender.setEvent2MessageProducer(bus);

        events = new HashMap<>();

        setupEventsRecorder();
    }

    @After
    public void teardown() 
    {
        enqueuePool.shutdown();
        dequeuePool.shutdown();
    }

    private void setupEventsRecorder()
    {
        recordedEvents = new CopyOnWriteArrayList<>();

        Mockito.doAnswer(new Answer<Void>()
        {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable
            {
                RepoEvent<?> event = invocation.getArgument(0, RepoEvent.class);
                recordedEvents.add(event);
                return null;
            }
        }).when(bus).send(any());
    }

    @Test
    public void shouldReceiveSingleQuickMessage() throws Exception 
    {
        eventSender.accept(messageWithDelay("A", 55l));

        sleep(150l);

        assertEquals(1, recordedEvents.size());
        assertEquals("A", recordedEvents.get(0).getId());
    }

    @Test
    public void shouldNotReceiveEventsWhenMessageIsNull() throws Exception 
    {
        eventSender.accept(() -> { return null; });

        sleep(150l);

        assertEquals(0, recordedEvents.size());
    }

    @Test
    public void shouldReceiveMultipleMessagesPreservingOrderScenarioOne() throws Exception {
        eventSender.accept(messageWithDelay("A", 0l));
        eventSender.accept(messageWithDelay("B", 100l));
        eventSender.accept(messageWithDelay("C", 200l));

        sleep(450l);

        assertEquals(3, recordedEvents.size());
        assertEquals("A", recordedEvents.get(0).getId());
        assertEquals("B", recordedEvents.get(1).getId());
        assertEquals("C", recordedEvents.get(2).getId());
    }

    @Test
    public void shouldReceiveMultipleMessagesPreservingOrderScenarioTwo() throws Exception
    {
        eventSender.accept(messageWithDelay("A", 300l));
        eventSender.accept(messageWithDelay("B", 150l));
        eventSender.accept(messageWithDelay("C", 0l));

        sleep(950l);

        assertEquals(3, recordedEvents.size());
        assertEquals("A", recordedEvents.get(0).getId());
        assertEquals("B", recordedEvents.get(1).getId());
        assertEquals("C", recordedEvents.get(2).getId());
    }

    @Test
    public void shouldReceiveMultipleMessagesPreservingOrderEvenWhenMakerPoisoned() throws Exception
    {
        eventSender.accept(messageWithDelay("A", 300l));
        eventSender.accept(() -> {throw new RuntimeException("Boom! (not to worry, this is a test)");});
        eventSender.accept(messageWithDelay("B", 55l));
        eventSender.accept(messageWithDelay("C", 0l));

        sleep(950l);

        assertEquals(3, recordedEvents.size());
        assertEquals("A", recordedEvents.get(0).getId());
        assertEquals("B", recordedEvents.get(1).getId());
        assertEquals("C", recordedEvents.get(2).getId());
    }
    
    @Test
    public void shouldReceiveMultipleMessagesPreservingOrderEvenWhenSenderPoisoned() throws Exception
    {
        Callable<Optional<RepoEvent<?>>> makerB = messageWithDelay("B", 55l);
        RepoEvent<?> messageB = makerB.call().get();
        doThrow(new RuntimeException("Boom! (not to worry, this is a test)")).when(bus).send(messageB);
        eventSender.accept(messageWithDelay("A", 300l));
        eventSender.accept(makerB);
        eventSender.accept(messageWithDelay("C", 0l));

        sleep(950l);

        assertEquals(2, recordedEvents.size());
        assertEquals("A", recordedEvents.get(0).getId());
        assertEquals("C", recordedEvents.get(1).getId());
    }

    @Test
    public void shouldReceiveMultipleMessagesPreservingOrderEvenWhenMakerPoisonedWithError() throws Exception
    {
        eventSender.accept(messageWithDelay("A", 300l));
        eventSender.accept(() -> {throw new OutOfMemoryError("Boom! (not to worry, this is a test)");});
        eventSender.accept(messageWithDelay("B", 55l));
        eventSender.accept(messageWithDelay("C", 0l));

        sleep(950l);

        assertEquals(3, recordedEvents.size());
        assertEquals("A", recordedEvents.get(0).getId());
        assertEquals("B", recordedEvents.get(1).getId());
        assertEquals("C", recordedEvents.get(2).getId());
    }
    
    @Test
    public void shouldReceiveMultipleMessagesPreservingOrderEvenWhenSenderPoisonedWithError() throws Exception
    {
        Callable<Optional<RepoEvent<?>>> makerB = messageWithDelay("B", 55l);
        RepoEvent<?> messageB = makerB.call().get();
        doThrow(new OutOfMemoryError("Boom! (not to worry, this is a test)")).when(bus).send(messageB);
        eventSender.accept(messageWithDelay("A", 300l));
        eventSender.accept(makerB);
        eventSender.accept(messageWithDelay("C", 0l));

        sleep(950l);

        assertEquals(2, recordedEvents.size());
        assertEquals("A", recordedEvents.get(0).getId());
        assertEquals("C", recordedEvents.get(1).getId());
    }

    private Callable<Optional<RepoEvent<?>>> messageWithDelay(String id, long delay)
    {
        return new Callable<Optional<RepoEvent<?>>>()
        {
            @Override
            public Optional<RepoEvent<?>> call() throws Exception
            {
                if (delay != 0)
                {
                    sleep(delay);
                }
                return Optional.of(newRepoEvent(id));
            }

            @Override
            public String toString()
            {
                return id;
            }
        };
    }
    
    private RepoEvent<?> newRepoEvent(String id)
    {
        RepoEvent<?> ev = events.get(id);
        if (ev != null)
            return ev;
        
        ev = mock(RepoEvent.class);
        when(ev.getId()).thenReturn(id);
        when(ev.toString()).thenReturn(id);
        events.put(id, ev);

        return ev;
    }

    public static ExecutorService newThreadPool() 
    {
        return new ThreadPoolExecutor(2, Integer.MAX_VALUE,
                                      60L, TimeUnit.SECONDS,
                                      new SynchronousQueue<Runnable>());
    }

    public static final Executor SYNC_EXECUTOR_SAME_THREAD = new Executor()
    {
        @Override
        public void execute(Runnable command)
        {
            command.run();
        }
    };

    public static final Executor SYNC_EXECUTOR_NEW_THREAD = new Executor()
    {
        @Override
        public void execute(Runnable command)
        {
            Thread t = new Thread(command);
            t.start();
            try
            {
                t.join();
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
        }
    };
}
