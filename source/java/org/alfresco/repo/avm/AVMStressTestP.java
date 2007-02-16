/*
 * Copyright (C) 2006 Alfresco, Inc.
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
 * http://www.alfresco.com/legal/licensing" */

package org.alfresco.repo.avm;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.avm.util.BulkLoader;

/**
 * This is a stress test for the AVM repository.
 * @author britt
 */
public class AVMStressTestP extends AVMServiceTestBase
{
    /**
     * Test N threads
     */
    public void testNThreads()
    {
        try
        {
            int nCopies = 8;
            int nThreads = 4;
            BulkLoader loader = new BulkLoader();
            loader.setAvmService(fService);
            long start = System.currentTimeMillis();
            for (int i = 0; i < nCopies; i++)
            {
                fService.createDirectory("main:/", "" + i);
                loader.recursiveLoad("source", "main:/" + i);
                fService.createSnapshot("main", null, null);
            }
            System.out.println("Load time: " + (System.currentTimeMillis() - start));
            List<AVMTester> testers = new ArrayList<AVMTester>();
            List<Thread> threads = new ArrayList<Thread>();
            for (int i = 0; i < nThreads; i++)
            {
                AVMTester tester
                    = new AVMTester(400,      // create file.
                                    20,        // create dir,
                                    5,        // rename
                                    5,         // create layered dir
                                    5,         // create layered file
                                    10,        // remove node
                                    20,        // modify file.
                                    3200,        // read file
                                    10,        // snapshot
                                    40000,      // # ops
                                    fService);   
                tester.refresh();
                Thread thread = new Thread(tester);
                testers.add(tester);
                threads.add(thread);
            }
            for (Thread thread : threads)
            {
                thread.start();
            }
            int exited = 0;
            // long sampStart = System.currentTimeMillis();
            while (exited != nThreads)
            {
                try
                {
                    Thread.sleep(2000);
                    for (int i = 0; i < nThreads; i++)
                    {
                        if (threads.get(i) == null)
                        {
                            continue;
                        }
                        threads.get(i).join(1000);
                        if (!threads.get(i).isAlive())
                        {
                            threads.set(i, null);
                            if (testers.get(i).getError())
                            {
                                for (AVMTester tester : testers)
                                {
                                    tester.setExit();
                                }
                                fail();
                            }
                            exited++;
                        }
                    }
                }
                catch (InterruptedException e)
                {
                    // Do nothing.
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }
}
