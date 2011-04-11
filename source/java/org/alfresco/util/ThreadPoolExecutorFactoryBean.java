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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.alfresco.error.AlfrescoRuntimeException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Factory for {@link java.util.concurrent.ThreadPoolExecutor} instances,
 * which cannot easily be constructed using constructor injection.  This instance
 * also allows the setting of the thread-specific properties that would otherwise
 * require setting a <code>ThreadFactory</code>.
 * <p>
 * This factory provides the a singleton instance of the pool.
 * <p>
 * Defaults are:
 * <ul>
 *   <li><b>{@link #setCorePoolSize(int) corePoolSize}: </b>
 *          20</li>
 *   <li><b>{@link #setMaximumPoolSize(int) maximumPoolSize}: </b>
 *          Equal to the {@link #setCorePoolSize(int)} at the time of instance creation</li>
 *   <li><b>{@link #setKeepAliveTime(int) keepAliveTime}: </b>
 *          90 seconds</li>
 *   <li><b>{@link #setThreadPriority(int) threadPriority}: </b>
 *          5 (NORM)</li>
 *   <li><b>{@link #setThreadDaemon(boolean) threadDaemon}: </b>
 *          true</li>
 *   <li><b>{@link #setWorkQueueSize(int) workQueueSize}: </b>
 *          -1 or less (No upper bound)</li>
 *   <li><b>{@link #setRejectedExecutionHandler(RejectedExecutionHandler) rejectedExecutionHandler: </b>
 *          <code>ThreadPoolExecutor.CallerRunsPolicy</code></li>
 * </ul>
 * 
 * @author Derek Hulley
 */
public class ThreadPoolExecutorFactoryBean implements FactoryBean, InitializingBean
{
    private static final int DEFAULT_CORE_POOL_SIZE = 20;
    private static final int DEFAULT_MAXIMUM_POOL_SIZE = -1;        // -1 is a sign that it must match the core pool size
    private static final int DEFAULT_KEEP_ALIVE_TIME = 90;          // seconds
    private static final int DEFAULT_THREAD_PRIORITY = Thread.NORM_PRIORITY;
    private static final boolean DEFAULT_THREAD_DAEMON = Boolean.TRUE;
    private static final int DEFAULT_WORK_QUEUE_SIZE = -1;
    private static final RejectedExecutionHandler DEFAULT_REJECTED_EXECUTION_HANDLER = new ThreadPoolExecutor.CallerRunsPolicy();
    private String poolName = "";
    
    private int corePoolSize;
    private int maximumPoolSize;
    private int keepAliveTime;
    private int threadPriority;
    private boolean threadDaemon;
    private int workQueueSize;
    private RejectedExecutionHandler rejectedExecutionHandler;
    /** the instance that will be given out by the factory */
    private DynamicallySizedThreadPoolExecutor instance;
    
    /**
     * Constructor setting default properties:
     */
    public ThreadPoolExecutorFactoryBean()
    {
        corePoolSize = DEFAULT_CORE_POOL_SIZE;
        maximumPoolSize = DEFAULT_MAXIMUM_POOL_SIZE;
        keepAliveTime = DEFAULT_KEEP_ALIVE_TIME;
        threadPriority = DEFAULT_THREAD_PRIORITY;
        threadDaemon = DEFAULT_THREAD_DAEMON;
        workQueueSize = DEFAULT_WORK_QUEUE_SIZE;
        rejectedExecutionHandler = DEFAULT_REJECTED_EXECUTION_HANDLER;
    }
    
    /**
     * The number of threads to keep in the pool, even if idle.
     * 
     * @param corePoolSize core thread count
     */
    public void setCorePoolSize(int corePoolSize)
    {
        this.corePoolSize = corePoolSize;
    }

    /**
     * The maximum number of threads to keep in the pool
     * 
     * @param maximumPoolSize the maximum number of threads in the pool
     */
    public void setMaximumPoolSize(int maximumPoolSize)
    {
        this.maximumPoolSize = maximumPoolSize;
    }

    /**
     * The time (in seconds) to keep non-core idle threads in the pool
     * 
     * @param keepAliveTime time to stay idle in seconds
     */
    public void setKeepAliveTime(int keepAliveTime)
    {
        this.keepAliveTime = keepAliveTime;
    }

    /**
     * The priority that all threads must have on the scale of 1 to 10,
     * where 1 has the lowest priority and 10 has the highest priority.
     * 
     * @param threadPriority    the thread priority
     */
    public void setThreadPriority(int threadPriority)
    {
        this.threadPriority = threadPriority;
    }

    /**
     * Set whether the threads run as daemon threads or not.
     * 
     * @param threadDaemon      <tt>true</tt> to run as daemon
     */
    public void setThreadDaemon(boolean threadDaemon)
    {
        this.threadDaemon = threadDaemon;
    }

    /**
     * The maximum number of queued work instances to keep before blocking
     * against further adds.
     * 
     * @param size      the queue size before blocks, or <b>-1</b> default
     *                  to indicate no upper bound
     */
    public void setWorkQueueSize(int workQueueSize)
    {
        this.workQueueSize = workQueueSize;
    }
    
    /**
     * The optional handler for when tasks cannot be submitted to the queue.
     * The default is the <code>CallerRunsPolicy</code>.
     * 
     * @param rejectedExecutionHandler      the handler to use
     */
    public void setRejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler)
    {
        this.rejectedExecutionHandler = rejectedExecutionHandler;
    }

    public void afterPropertiesSet() throws Exception
    {
        // if the maximum pool size has not been set, change it to match the core pool size
        if (maximumPoolSize == DEFAULT_MAXIMUM_POOL_SIZE)
        {
            maximumPoolSize = corePoolSize;
        }
        
        // We need a thread factory
        TraceableThreadFactory threadFactory = new TraceableThreadFactory();
        threadFactory.setThreadDaemon(threadDaemon);
        threadFactory.setThreadPriority(threadPriority);
        
        if(poolName.length() > 0) 
        {
        	threadFactory.setNamePrefix(poolName);
        }
        
        BlockingQueue<Runnable> workQueue;
        if (workQueueSize < 0)
        {
            // We can have an unlimited queue, as we have a sensible thread pool!
            workQueue = new LinkedBlockingQueue<Runnable>();
        }
        else
        {
            // Use an array one for consistent performance on a small queue size
            workQueue = new ArrayBlockingQueue<Runnable>(workQueueSize);
        }
        
        // construct the instance
        instance = new DynamicallySizedThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                workQueue,
                threadFactory,
                rejectedExecutionHandler);
    }

    /**
     * @return Returns true always.
     */
    public boolean isSingleton()
    {
        return false;
    }

    /**
     * @return Returns the singleton {@link ThreadPoolExecutor instance}.
     */
    public Object getObject() throws Exception
    {
        if (instance == null)
        {
            throw new AlfrescoRuntimeException("The ThreadPoolExecutor instance has not been created");
        }
        return instance;
    }

    /**
     * @see ThreadPoolExecutor
     */
    public Class getObjectType()
    {
        return DynamicallySizedThreadPoolExecutor.class;
    }
    
    public String getPoolName() 
    {
    	return this.poolName;
    }
    
    public void setPoolName(String poolName)
    {
    	this.poolName = poolName;
    }
}
