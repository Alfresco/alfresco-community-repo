/*
 * Copyright (C) 2005-2016 Alfresco Software Limited.
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

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.junit.Assert;

/**
 * Test class for GUID generation
 * 
 * @author Andreea Dragoi
 *
 */
public class GuidTest extends TestCase
{
    class GuidRunner implements Runnable
    {
        @Override
        public void run()
        {
            GUID.generate();
        }
    }
    
    /**
     * Tests the improvement added by using a SecureRandom pool when generating GUID's 
     */
    public void testGuid()
    {
        // warm-up (to pre-init the secureRandomArray)
        GUID.generate();

        List<Thread> threads = new ArrayList<>();
        int n = 30;
        
        for (int i = 0; i < n; i++)
        {
            Thread thread = new Thread(new GuidRunner());
            threads.add(thread);
            thread.start();
        }
        
        Set<String> blocked = new HashSet<String>();
        Set<String> terminated = new HashSet<String>();

        int maxItemsBlocked = 0;

        while (terminated.size() != n)
        {
            for (Thread current : threads)
            {
                State state = current.getState();
                String name = current.getName();

                if (state == State.BLOCKED)
                {
                    if (!blocked.contains(name))
                    {
                        blocked.add(name);
                        maxItemsBlocked = blocked.size() > maxItemsBlocked ? blocked.size() : maxItemsBlocked;
                    }
                }
                else // not BLOCKED, eg. RUNNABLE, TERMINATED, ...
                {
                    blocked.remove(name);
                    if (state == State.TERMINATED && !terminated.contains(name))
                    {
                        terminated.add(name);
                    }
                }
            }
        }
        
        //worst case scenario : max number of threads blocked at a moment = number of threads - 2 ( usually ~5 for 30 threads)
        //the implementation without RandomSecure pool reaches constantly (number of threads - 1) max blocked threads  
        Assert.assertTrue("Exceeded number of blocked threads : " + maxItemsBlocked, maxItemsBlocked < n-2);
    }

}

