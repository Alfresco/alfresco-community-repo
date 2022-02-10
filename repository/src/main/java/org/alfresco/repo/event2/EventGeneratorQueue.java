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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/*
 * This queue allows to create asynchronously the RepoEvent offloading the work to a ThreadPool but
 * at the same time it preserves the order of the events
 */
public class EventGeneratorQueue implements InitializingBean
{
	protected static final Log LOGGER = LogFactory.getLog(EventGeneratorQueue.class);
    
    protected Executor enqueueThreadPoolExecutor;
    protected Executor dequeueThreadPoolExecutor;
    protected Event2MessageProducer event2MessageProducer;
    protected BlockingQueue<EventInMaking> queue = new LinkedBlockingQueue<>();
    protected Runnable listener = createListener();

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "enqueueThreadPoolExecutor", enqueueThreadPoolExecutor);
        PropertyCheck.mandatory(this, "dequeueThreadPoolExecutor", dequeueThreadPoolExecutor);
        PropertyCheck.mandatory(this, "event2MessageProducer", event2MessageProducer);
    }

    public void setEvent2MessageProducer(Event2MessageProducer event2MessageProducer)
    {
        this.event2MessageProducer = event2MessageProducer;
    }
    
    public void setEnqueueThreadPoolExecutor(Executor enqueueThreadPoolExecutor)
    {
        this.enqueueThreadPoolExecutor = enqueueThreadPoolExecutor;
    }
    
    public void setDequeueThreadPoolExecutor(Executor dequeueThreadPoolExecutor)
    {
        this.dequeueThreadPoolExecutor = dequeueThreadPoolExecutor;
        dequeueThreadPoolExecutor.execute(listener);
    }

    /**
     * Procedure to enqueue the callback functions that creates an event.
     * @param maker Callback function that creates an event.
     */
    public void accept(Callable<RepoEvent<?>> maker)
    {
        EventInMaking eventInMaking = new EventInMaking(maker);
        queue.offer(eventInMaking);
        enqueueThreadPoolExecutor.execute(() -> {
            try
            {
                eventInMaking.make();
            }
            catch (Exception e)
            {
                LOGGER.error("Unexpected error while enqueuing maker function for repository event" + e);
            }
        });
    }

    /**
     * Create listener task in charge of dequeuing and sending events ready to be sent.
     * @return The task in charge of dequeuing and sending events ready to be sent.
     */
    private Runnable createListener()
    {
        return new Runnable()
        {
            @Override
            public void run()
            {
                try 
                {
                    while (!Thread.interrupted())
                    {
                        try
                        {
                            EventInMaking eventInMaking = queue.take();
                            RepoEvent<?> event = eventInMaking.getEventWhenReady();
                            if (event != null)
                            {
                                event2MessageProducer.send(event);
                            }
                        }
                        catch (Exception e)
                        {
                            LOGGER.error("Unexpected error while dequeuing and sending repository event" + e);
                        }
                    }
                }
                finally
                {
                    LOGGER.warn("Unexpected: rescheduling the listener thread.");
                    dequeueThreadPoolExecutor.execute(listener);
                }
            }
        };

    }

    /*
     * Simple class that makes events and allows to retrieve them when ready
     */
    private static class EventInMaking
    {
        private Callable<RepoEvent<?>> maker;
        private volatile RepoEvent<?> event;
        private CountDownLatch latch;
        
        public EventInMaking(Callable<RepoEvent<?>> maker)
        {
            this.maker = maker;
            this.latch = new CountDownLatch(1);
        }
        
        public void make() throws Exception
        {
            try
            {
                event = maker.call();
            }
            finally 
            {
                latch.countDown();
            }
        }
        
        public RepoEvent<?> getEventWhenReady() throws InterruptedException
        {
            latch.await(30, TimeUnit.SECONDS);
            return event;
        }
        
        @Override
        public String toString()
        {
            return maker.toString();
        }
    }

}
