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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is an instance of {@link java.util.concurrent.ThreadPoolExecutor} which 
 * behaves how one would expect it to, even when faced with an unlimited
 * queue. Unlike the default {@link java.util.concurrent.ThreadPoolExecutor}, it
 * will add new Threads up to {@link #setMaximumPoolSize(int) maximumPoolSize}
 * when there is lots of pending work, rather than only when the queue is full
 * (which it often never will be, especially for unlimited queues)
 * 
 * @author Nick Burch
 */
public class DynamicallySizedThreadPoolExecutor extends ThreadPoolExecutor
{
    private static Log logger = LogFactory.getLog(DynamicallySizedThreadPoolExecutor.class);
    
    private final ReentrantLock lock = new ReentrantLock();
    private int realCorePoolSize;
    
    public DynamicallySizedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
            BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
        this.realCorePoolSize = corePoolSize;
    }

    public DynamicallySizedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
            BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        this.realCorePoolSize = corePoolSize;
    }

    public DynamicallySizedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
            BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        this.realCorePoolSize = corePoolSize;
    }

    public DynamicallySizedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
            BlockingQueue<Runnable> workQueue)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        this.realCorePoolSize = corePoolSize;
    }
    
    @Override
    public void setCorePoolSize(int corePoolSize)
    {
        this.realCorePoolSize = corePoolSize;
        super.setCorePoolSize(corePoolSize);
    }

    @Override
    public void execute(Runnable command)
    {
        // Do we want to add another thread?
        int threadCount = getPoolSize();
        if(logger.isDebugEnabled())
        {
            logger.debug("Current pool size is " + threadCount + ", real core=" + realCorePoolSize +  
                    ", current core=" + getCorePoolSize() + ", max=" + getMaximumPoolSize());
        }
        
        if(threadCount < getMaximumPoolSize())
        {
            // We're not yet at the full thread count
            
            // Does the queue size warrant adding one?
            // (If there are more than the maximum pool size of jobs pending,
            //  it's time to add another thread)
            int queueSize = getQueue().size() + 1;// New job not yet added
            if(queueSize >= getMaximumPoolSize())
            {
                lock.lock();
                int currentCoreSize = getCorePoolSize();
                if(currentCoreSize < getMaximumPoolSize()) 
                {
                    super.setCorePoolSize(currentCoreSize+1);
                    
                    if(logger.isInfoEnabled())
                    {
                        logger.info("Increased pool size to " + getCorePoolSize() + " from " +
                                currentCoreSize + " due to queue size of " + queueSize);
                    }
                }
                lock.unlock();
            }
        }
        
        // Now run the actual work
        super.execute(command);
    }
     
    @Override
    protected void afterExecute(Runnable r, Throwable t)
    {
        // If the queue is looking empty, allow the pool to
        //  get rid of idle threads when it wants to
        int threadCount = getPoolSize();
        if(threadCount == getMaximumPoolSize() && threadCount > realCorePoolSize) 
        {
            int queueSize = getQueue().size();
            int currentCoreSize = getCorePoolSize();
            if(queueSize < 2 && currentCoreSize > realCorePoolSize)
            {
                // Almost out of work, allow the pool to reduce threads when
                //  required. Double checks the sizing inside a lock to avoid
                //  race conditions taking us below the real core size.
                lock.lock();
                currentCoreSize = getCorePoolSize();
                if(currentCoreSize > realCorePoolSize) 
                {
                    super.setCorePoolSize(currentCoreSize-1);
                    
                    if(logger.isInfoEnabled())
                    {
                        logger.info("Decreased pool size to " + getCorePoolSize() + " from " +
                                currentCoreSize + " (real core size is " + realCorePoolSize + 
                                ") due to queue size of " + queueSize);
                    }
                }
                lock.unlock();
            }
        }
    }
}
