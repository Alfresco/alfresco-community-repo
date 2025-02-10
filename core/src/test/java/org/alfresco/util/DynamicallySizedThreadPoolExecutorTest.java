/*
 * Copyright (C) 2005-2025 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.util;

import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;

/**
 * Tests for our instance of {@link java.util.concurrent.ThreadPoolExecutor}
 * 
 * @author Nick Burch
 */
public class DynamicallySizedThreadPoolExecutorTest extends TestCase
{

    private static final Duration MAX_WAIT_TIMEOUT = Duration.ofSeconds(1);
    private static final Log logger = LogFactory.getLog(DynamicallySizedThreadPoolExecutorTest.class);
    private static final int DEFAULT_KEEP_ALIVE_TIME = 90;

    @Override
    protected void setUp() throws Exception
    {
        SleepUntilAllWake.reset();
    }

    public void testUpToCore()
    {
        DynamicallySizedThreadPoolExecutor exec = createInstance(5,10, DEFAULT_KEEP_ALIVE_TIME);

        assertEquals(0, exec.getPoolSize());
        exec.execute(new SleepUntilAllWake());
        exec.execute(new SleepUntilAllWake());
        assertEquals(2, exec.getPoolSize());
        exec.execute(new SleepUntilAllWake());
        exec.execute(new SleepUntilAllWake());
        assertEquals(4, exec.getPoolSize());
        exec.execute(new SleepUntilAllWake());
        assertEquals(5, exec.getPoolSize());
        
        SleepUntilAllWake.wakeAll();
        waitForPoolSizeEquals(exec, 5);
        assertEquals(5, exec.getPoolSize());
    }

    public void testPastCoreButNotHugeQueue()
    {
        DynamicallySizedThreadPoolExecutor exec = createInstance(5,10, DEFAULT_KEEP_ALIVE_TIME);

        assertEquals(0, exec.getPoolSize());
        assertEquals(0, exec.getQueue().size());
        exec.execute(new SleepUntilAllWake());
        exec.execute(new SleepUntilAllWake());
        exec.execute(new SleepUntilAllWake());
        exec.execute(new SleepUntilAllWake());
        exec.execute(new SleepUntilAllWake());
        assertEquals(5, exec.getPoolSize());
        assertEquals(0, exec.getQueue().size());
        
        // Need to hit max pool size before it adds more
        exec.execute(new SleepUntilAllWake());
        exec.execute(new SleepUntilAllWake());
        exec.execute(new SleepUntilAllWake());
        exec.execute(new SleepUntilAllWake());
        exec.execute(new SleepUntilAllWake());
        assertEquals(5, exec.getPoolSize());
        assertEquals(5, exec.getQueue().size());
        
        exec.execute(new SleepUntilAllWake());
        exec.execute(new SleepUntilAllWake());
        assertEquals(5, exec.getPoolSize());
        assertEquals(7, exec.getQueue().size());
        
        SleepUntilAllWake.wakeAll();
        waitForPoolSizeEquals(exec, 5);
        assertEquals(5, exec.getPoolSize());
    }
    
    public void testToExpandQueue() throws Exception
    {
        DynamicallySizedThreadPoolExecutor exec = createInstance(2,4,5);

        assertEquals(0, exec.getPoolSize());
        assertEquals(0, exec.getQueue().size());
        exec.execute(new SleepUntilAllWake());
        exec.execute(new SleepUntilAllWake());
        assertEquals(2, exec.getPoolSize());
        assertEquals(0, exec.getQueue().size());
        
        exec.execute(new SleepUntilAllWake());
        exec.execute(new SleepUntilAllWake());
        exec.execute(new SleepUntilAllWake());
        assertEquals(2, exec.getPoolSize());
        assertEquals(3, exec.getQueue().size());
        
        // Next should add one
        exec.execute(new SleepUntilAllWake());
        waitForPoolSizeEquals(exec, 3); // Let the new thread spin up
        assertEquals(3, exec.getPoolSize());
        assertEquals(3, exec.getQueue().size());

        // And again
        exec.execute(new SleepUntilAllWake());
        waitForPoolSizeEquals(exec, 4); // Let the new thread spin up
        assertEquals(4, exec.getPoolSize());
        assertEquals(3, exec.getQueue().size());
        
        // But no more will be added, as we're at max
        exec.execute(new SleepUntilAllWake());
        exec.execute(new SleepUntilAllWake());
        exec.execute(new SleepUntilAllWake());
        assertEquals(4, exec.getPoolSize());
        assertEquals(6, exec.getQueue().size());
        
        SleepUntilAllWake.wakeAll();
        Thread.sleep(100);

        // All threads still running, as 5 second timeout
        assertEquals(4, exec.getPoolSize());
    }

    private DynamicallySizedThreadPoolExecutor createInstance(int corePoolSize, int maximumPoolSize, int keepAliveTime)
    {
        // We need a thread factory
        TraceableThreadFactory threadFactory = new TraceableThreadFactory();
        threadFactory.setThreadDaemon(true);
        threadFactory.setThreadPriority(Thread.NORM_PRIORITY);

        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();

        return new DynamicallySizedThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                workQueue,
                threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    private void waitForPoolSizeEquals(DynamicallySizedThreadPoolExecutor exec, int expectedSize)
    {
        await().atMost(MAX_WAIT_TIMEOUT).until(() -> exec.getPoolSize() == expectedSize);
    }

    public static class SleepUntilAllWake implements Runnable
    {
        private static ConcurrentMap<String, Thread> sleeping = new ConcurrentHashMap<String, Thread>();
        private static boolean allAwake = false;

        @Override
        public void run()
        {
            if(allAwake) return;
            
            // Track us, and wait for the bang
            logger.debug("Adding thread: " + Thread.currentThread().getName());
            sleeping.put(Thread.currentThread().getName(), Thread.currentThread());
            try
            {
                Thread.sleep(30*1000);
                System.err.println("Warning - Thread finished sleeping without wake!");
            }
            catch(InterruptedException e)
            {
                logger.debug("Interrupted thread: " + Thread.currentThread().getName());
            }
        }
        
        public static void wakeAll()
        {
            allAwake = true;
            for(Entry<String, Thread> t : sleeping.entrySet())
            {
                logger.debug("Interrupting thread: " + t.getKey());
                t.getValue().interrupt();
            }
        }
        public static void reset()
        {
            logger.debug("Resetting.");
            allAwake = false;
            sleeping.clear();
        }
    }
}
