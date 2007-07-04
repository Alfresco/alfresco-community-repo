/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A thread factory that spawns threads that are statically visible.  Each factory uses a unique
 * thread group.  All the groups that have been used can be fetched using
 * {@link #getActiveThreadGroups()}, allowing iteration of the the threads in the group.
 * 
 * @since 2.1
 * @author Derek Hulley
 */
public class TraceableThreadFactory implements ThreadFactory
{
    private static final AtomicInteger factoryNumber = new AtomicInteger(1);
    private static List<ThreadGroup> activeThreadGroups = Collections.synchronizedList(new ArrayList<ThreadGroup>(1));
    
    /**
     * Get a list of thread groups registered by the factory.
     * 
     * @return      Returns a snapshot of thread groups
     */
    public static List<ThreadGroup> getActiveThreadGroups()
    {
        return activeThreadGroups;
    }
    
    private final ThreadGroup group;
    private final String namePrefix;
    private final AtomicInteger threadNumber;
    private boolean threadDaemon;
    private int threadPriority;
    

    TraceableThreadFactory()
    {
        this.group = new ThreadGroup("TraceableThreadGroup-" + factoryNumber.getAndIncrement());
        TraceableThreadFactory.activeThreadGroups.add(this.group);
        
        this.namePrefix = "TraceableThread-" + factoryNumber.getAndIncrement() + "-thread-";
        this.threadNumber = new AtomicInteger(1);
    }

    /**
     * @param daemon            <tt>true</tt> if all threads created must be daemon threads
     */
    public void setThreadDaemon(boolean daemon)
    {
        this.threadDaemon = daemon;
    }

    /**
     * 
     * @param threadPriority    the threads priority from 1 (lowest) to 10 (highest)
     */
    public void setThreadPriority(int threadPriority)
    {
        this.threadPriority = threadPriority;
    }

    public Thread newThread(Runnable r)
    {
        Thread thread = new Thread(
                group,
                r,
                namePrefix + threadNumber.getAndIncrement(),
                0);
        thread.setDaemon(threadDaemon);
        thread.setPriority(threadPriority);
        
        return thread;
    }
}
