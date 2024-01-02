/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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

import java.util.Optional;
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

/**
 * Enqueuing event sender allows to create asynchronously the RepoEvent offloading the work to a ThreadPool but
 * at the same time it preserves the order of the events.
 */
public class EnqueuingEventSender extends DirectEventSender
{
    protected static final Log LOGGER = LogFactory.getLog(EnqueuingEventSender.class);

    protected Executor enqueueThreadPoolExecutor;
    protected Executor dequeueThreadPoolExecutor;
    protected BlockingQueue<EventInMaking> queue = new LinkedBlockingQueue<>();
    protected Runnable listener = createListener();

    @Override
    public void afterPropertiesSet()
    {
        super.afterPropertiesSet();
        PropertyCheck.mandatory(this, "enqueueThreadPoolExecutor", enqueueThreadPoolExecutor);
        PropertyCheck.mandatory(this, "dequeueThreadPoolExecutor", dequeueThreadPoolExecutor);
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
     * @param eventProducer Callback function that creates an event.
     */
    @Override
    public void accept(Callable<Optional<RepoEvent<?>>> eventProducer)
    {
        EventInMaking eventInMaking = new EventInMaking(eventProducer);
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
        return () -> {
            try
            {
                while (!Thread.interrupted())
                {
                    try
                    {
                        queue.take().getEventWhenReady().ifPresent(event -> event2MessageProducer.send(event));
                    }
                    catch (Exception e)
                    {
                        LOGGER.error("Unexpected error while dequeuing and sending repository event " + e);
                    }
                }
            }
            finally
            {
                LOGGER.warn("Unexpected: rescheduling the listener thread.");
                dequeueThreadPoolExecutor.execute(listener);
            }
        };

    }

    /**
     * Simple class that makes events and allows to retrieve them when ready
     */
    private static class EventInMaking
    {
        private final Callable<Optional<RepoEvent<?>>> maker;
        private volatile RepoEvent<?> event;
        private final CountDownLatch latch;

        public EventInMaking(Callable<Optional<RepoEvent<?>>> maker)
        {
            this.maker = maker;
            this.latch = new CountDownLatch(1);
        }

        public void make() throws Exception
        {
            try
            {
                event = maker.call().orElse(null);
            }
            finally
            {
                latch.countDown();
            }
        }

        public Optional<RepoEvent<?>> getEventWhenReady() throws InterruptedException
        {
            latch.await(30, TimeUnit.SECONDS);
            return Optional.ofNullable(event);
        }

        @Override
        public String toString()
        {
            return maker.toString();
        }
    }
}
