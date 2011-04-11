/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

import java.util.ArrayList;
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
    // We need logging, so load that if we don't have the full repo already
    static {
        if(! ApplicationContextHelper.isContextLoaded())
        {
            ApplicationContextHelper.getApplicationContext(new String[] {
                    "classpath:alfresco/logging-context.xml"
            });
        }
    }
    private static Log logger = LogFactory.getLog(DynamicallySizedThreadPoolExecutorTest.class);
    
    private ThreadPoolExecutorFactoryBean factory;
    
    @Override
    protected void setUp() throws Exception
    {
        factory = new ThreadPoolExecutorFactoryBean();
        SleepUntilAllWake.reset();
    }

    public void testUpToCore() throws Exception
    {
        factory.setCorePoolSize(5);
        factory.setMaximumPoolSize(10);
        factory.afterPropertiesSet();
        
        DynamicallySizedThreadPoolExecutor exec = 
            (DynamicallySizedThreadPoolExecutor)factory.getObject();
        
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
        Thread.sleep(100);
        assertEquals(5, exec.getPoolSize());
    }

    public void testPastCoreButNotHugeQueue() throws Exception
    {
        factory.setCorePoolSize(5);
        factory.setMaximumPoolSize(10);
        factory.afterPropertiesSet();
        
        DynamicallySizedThreadPoolExecutor exec = 
            (DynamicallySizedThreadPoolExecutor)factory.getObject();
        
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
        Thread.sleep(100);
        assertEquals(5, exec.getPoolSize());
    }
    
    public void testToExpandQueue() throws Exception
    {
        factory.setCorePoolSize(2);
        factory.setMaximumPoolSize(4);
        factory.setKeepAliveTime(1);
        factory.afterPropertiesSet();
        
        DynamicallySizedThreadPoolExecutor exec = 
            (DynamicallySizedThreadPoolExecutor)factory.getObject();
        
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
        Thread.sleep(20); // Let the new thread spin up
        assertEquals(3, exec.getPoolSize());
        assertEquals(3, exec.getQueue().size());

        // And again
        exec.execute(new SleepUntilAllWake());
        Thread.sleep(20); // Let the new thread spin up
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
        
        // All threads still running, as 1 second timeout
        assertEquals(4, exec.getPoolSize());
    }

    public void testToExpandThenContract() throws Exception
    {
        factory.setCorePoolSize(2);
        factory.setMaximumPoolSize(4);
        factory.setKeepAliveTime(1);
        factory.afterPropertiesSet();
        
        DynamicallySizedThreadPoolExecutor exec = 
            (DynamicallySizedThreadPoolExecutor)factory.getObject();
        exec.setKeepAliveTime(30, TimeUnit.MILLISECONDS);
        
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
        Thread.sleep(20); // Let the new thread spin up
        assertEquals(3, exec.getPoolSize());
        assertEquals(3, exec.getQueue().size());

        // And again
        exec.execute(new SleepUntilAllWake());
        Thread.sleep(20); // Let the new thread spin up
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
        
        // Wait longer than the timeout without any work, which should
        //  let all the extra threads go away
        // (Depending on how closely your JVM follows the specification,
        //  we may fall back to the core size which is correct, or we
        //  may go to zero which is wrong, but hey, it's the JVM...)
        logger.debug("Core pool size is " + exec.getCorePoolSize());
        logger.debug("Current pool size is " + exec.getPoolSize());
        logger.debug("Queue size is " + exec.getQueue().size());
        assertTrue(
                "Pool size should be 0-2 as everything is idle, was " + exec.getPoolSize(),
                exec.getPoolSize() >= 0
        );
        assertTrue(
                "Pool size should be 0-2 as everything is idle, was " + exec.getPoolSize(),
                exec.getPoolSize() <= 2
        );
        
        SleepUntilAllWake.reset();
        
        // Add 2 new jobs, will stay/ go to at 2 threads
        assertEquals(0, exec.getQueue().size());
        exec.execute(new SleepUntilAllWake());
        exec.execute(new SleepUntilAllWake());
        
        // Let the idle threads grab them, then check
        Thread.sleep(20);
        assertEquals(2, exec.getPoolSize());
        assertEquals(0, exec.getQueue().size());
        
        // 3 more, still at 2 threads
        exec.execute(new SleepUntilAllWake());
        exec.execute(new SleepUntilAllWake());
        exec.execute(new SleepUntilAllWake());
        assertEquals(2, exec.getPoolSize());
        assertEquals(3, exec.getQueue().size());
        
        // And again wait for it all
        SleepUntilAllWake.wakeAll();
        Thread.sleep(100);
        assertEquals(2, exec.getPoolSize());

        
        // Now decrease the overall pool size
        // Will rise and fall to there now
        exec.setCorePoolSize(1);
        
        // Run a quick job, to ensure that the
        //  "can I kill one yet" logic is applied
        SleepUntilAllWake.reset();
        exec.execute(new SleepUntilAllWake());
        SleepUntilAllWake.wakeAll();
        
        Thread.sleep(100);
        assertEquals(1, exec.getPoolSize());
        assertEquals(0, exec.getQueue().size());
        
        SleepUntilAllWake.reset();
        
        
        // Push enough on to go up to 4 active threads
        exec.execute(new SleepUntilAllWake());
        exec.execute(new SleepUntilAllWake());
        exec.execute(new SleepUntilAllWake());
        exec.execute(new SleepUntilAllWake());
        exec.execute(new SleepUntilAllWake());
        exec.execute(new SleepUntilAllWake());
        exec.execute(new SleepUntilAllWake());
        exec.execute(new SleepUntilAllWake());
        exec.execute(new SleepUntilAllWake());
        exec.execute(new SleepUntilAllWake());
        
        Thread.sleep(20); // Let the new threads spin up
        assertEquals(4, exec.getPoolSize());
        assertEquals(6, exec.getQueue().size());
        
        // Wait for them all to finish, should drop back to 1 now
        // (Or zero, if your JVM can't read the specification...)
        SleepUntilAllWake.wakeAll();
        Thread.sleep(100);
        assertTrue(
                "Pool size should be 0 or 1 as everything is idle, was " + exec.getPoolSize(),
                exec.getPoolSize() >= 0
        );
        assertTrue(
                "Pool size should be 0 or 1 as everything is idle, was " + exec.getPoolSize(),
                exec.getPoolSize() <= 1
        );
    }
    
    public static class SleepUntilAllWake implements Runnable
    {
        private static ArrayList<Thread> sleeping = new ArrayList<Thread>();
        private static boolean allAwake = false;

        @Override
        public void run()
        {
            if(allAwake) return;
            
            // Track us, and wait for the bang
            sleeping.add(Thread.currentThread());
            try
            {
                Thread.sleep(30*1000);
                System.err.println("Warning - Thread finished sleeping without wake!");
            } catch(InterruptedException e) {}
        }
        
        public static void wakeAll()
        {
            allAwake = true;
            for(Thread t : sleeping) {
                t.interrupt();
            }
        }
        public static void reset()
        {
            allAwake = false;
            sleeping.clear();
        }
    }
}
