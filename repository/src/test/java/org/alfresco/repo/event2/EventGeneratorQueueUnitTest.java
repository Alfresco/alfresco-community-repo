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

public class EventGeneratorQueueUnitTest
{
    private EventGeneratorQueue queue;

    private Event2MessageProducer bus;
    private ExecutorService enqueuePool;
    private ExecutorService dequeuePool;
    private List<RepoEvent<?>> recordedEvents;
    private Map<String, RepoEvent<?>> events;

    @Before
    public void setup() 
    {
        queue = new EventGeneratorQueue();

        enqueuePool = newThreadPool();
        queue.setEnqueueThreadPoolExecutor(enqueuePool);
        dequeuePool = newThreadPool();
        queue.setDequeueThreadPoolExecutor(dequeuePool);

        bus = mock(Event2MessageProducer.class);
        queue.setEvent2MessageProducer(bus);

        events = new HashMap<>();

        setupEventsRecorder();
    }

    @After
    public void teardown() 
    {
        enqueuePool.shutdown();
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
        queue.accept(messageWithDelay("A", 55l));

        sleep(150l);

        assertEquals(1, recordedEvents.size());
        assertEquals("A", recordedEvents.get(0).getId());
    }

    @Test
    public void shouldNotReceiveEventsWhenMessageIsNull() throws Exception 
    {
        queue.accept(() -> { return null; });

        sleep(150l);

        assertEquals(0, recordedEvents.size());
    }

    @Test
    public void shouldReceiveMultipleMessagesPreservingOrderScenarioOne() throws Exception {
        queue.accept(messageWithDelay("A", 0l));
        queue.accept(messageWithDelay("B", 100l));
        queue.accept(messageWithDelay("C", 200l));

        sleep(450l);

        assertEquals(3, recordedEvents.size());
        assertEquals("A", recordedEvents.get(0).getId());
        assertEquals("B", recordedEvents.get(1).getId());
        assertEquals("C", recordedEvents.get(2).getId());
    }

    @Test
    public void shouldReceiveMultipleMessagesPreservingOrderScenarioTwo() throws Exception
    {
        queue.accept(messageWithDelay("A", 300l));
        queue.accept(messageWithDelay("B", 150l));
        queue.accept(messageWithDelay("C", 0l));

        sleep(950l);

        assertEquals(3, recordedEvents.size());
        assertEquals("A", recordedEvents.get(0).getId());
        assertEquals("B", recordedEvents.get(1).getId());
        assertEquals("C", recordedEvents.get(2).getId());
    }

    @Test
    public void shouldReceiveMultipleMessagesPreservingOrderEvenWhenMakerPoisoned() throws Exception
    {
        queue.accept(messageWithDelay("A", 300l));
        queue.accept(() -> {throw new RuntimeException("Boom! (not to worry, this is a test)");});
        queue.accept(messageWithDelay("B", 55l));
        queue.accept(messageWithDelay("C", 0l));

        sleep(950l);

        assertEquals(3, recordedEvents.size());
        assertEquals("A", recordedEvents.get(0).getId());
        assertEquals("B", recordedEvents.get(1).getId());
        assertEquals("C", recordedEvents.get(2).getId());
    }
    
    @Test
    public void shouldReceiveMultipleMessagesPreservingOrderEvenWhenSenderPoisoned() throws Exception
    {
        Callable<RepoEvent<?>> makerB = messageWithDelay("B", 55l);
        RepoEvent<?> messageB = makerB.call();
        doThrow(new RuntimeException("Boom! (not to worry, this is a test)")).when(bus).send(messageB);
        queue.accept(messageWithDelay("A", 300l));
        queue.accept(makerB);
        queue.accept(messageWithDelay("C", 0l));

        sleep(950l);

        assertEquals(2, recordedEvents.size());
        assertEquals("A", recordedEvents.get(0).getId());
        assertEquals("C", recordedEvents.get(1).getId());
    }

    @Test
    public void shouldReceiveMultipleMessagesPreservingOrderEvenWhenMakerPoisonedWithError() throws Exception
    {
        queue.accept(messageWithDelay("A", 300l));
        queue.accept(() -> {throw new OutOfMemoryError("Boom! (not to worry, this is a test)");});
        queue.accept(messageWithDelay("B", 55l));
        queue.accept(messageWithDelay("C", 0l));

        sleep(950l);

        assertEquals(3, recordedEvents.size());
        assertEquals("A", recordedEvents.get(0).getId());
        assertEquals("B", recordedEvents.get(1).getId());
        assertEquals("C", recordedEvents.get(2).getId());
    }
    
    @Test
    public void shouldReceiveMultipleMessagesPreservingOrderEvenWhenSenderPoisonedWithError() throws Exception
    {
        Callable<RepoEvent<?>> makerB = messageWithDelay("B", 55l);
        RepoEvent<?> messageB = makerB.call();
        doThrow(new OutOfMemoryError("Boom! (not to worry, this is a test)")).when(bus).send(messageB);
        queue.accept(messageWithDelay("A", 300l));
        queue.accept(makerB);
        queue.accept(messageWithDelay("C", 0l));

        sleep(950l);

        assertEquals(2, recordedEvents.size());
        assertEquals("A", recordedEvents.get(0).getId());
        assertEquals("C", recordedEvents.get(1).getId());
    }

    private Callable<RepoEvent<?>> messageWithDelay(String id, long delay)
    {
        Callable<RepoEvent<?>> res = new Callable<RepoEvent<?>>() {

            @Override
            public RepoEvent<?> call() throws Exception
            {
                if(delay != 0)
                {
                    sleep(delay); 
                }
                return newRepoEvent(id);
            } 
            
            @Override
            public String toString()
            {
                return id;
            }
        };
        return res;
    }
    
    private RepoEvent<?> newRepoEvent(String id)
    {
        RepoEvent<?> ev = events.get(id);
        if (ev!=null)
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
