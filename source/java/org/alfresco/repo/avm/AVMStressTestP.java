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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

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
    /*
    public void xtestStressA() throws Throwable
    {
        testNThreads(   1,  // nThreads
                        "source/java/org/alfresco/repo/avm/actions", // relative dir to load from (.../repository)
                        1,  // nCopies
                        1,  // create file
                        1,  // create dir
                        0,  // rename
                        0,  // create layered dir
                        0,  // create layered file
                        0,  // remove node
                        0,  // modify file
                        50,  // read file
                        0,  // snapshot
                        100); // # ops (for each thread)
    }
    */
    
    public void testStressB() throws Throwable
    {
        testNThreads(   2,  // nThreads
                        "source/java/org/alfresco/repo/avm/actions", // relative dir to load from (.../repository)
                        1,  // nCopies
                        10,  // create file
                        2,  // create dir
                        2,  // rename
                        2,  // create layered dir  // TODO pending ETWOTWO-715 (is 2 in 2.1.x)
                        2,  // create layered file // TODO pending ETWOTWO-715 (is 2 in 2.1.x)
                        5,  // remove node
                        10,  // modify file
                        50,  // read file
                        5,  // snapshot
                        200); // # ops (for each thread)
    }
    
    /*
    public void xtestStressZ()
    {
        testNThreads(   4,      // nThreads
                        "source", // relative dir to load from (.../repository)
                        8,      // nCopies
                        400,    // create file
                        20,     // create dir
                        5,      // rename
                        5,      // create layered dir
                        5,      // create layered file
                        10,     // remove node
                        20,     // modify file
                        3200,   // read file
                        10,     // snapshot
                        40000); // # ops
    }
    */
    
    /**
     * Test N threads
     */
    private void testNThreads(int nThreads, 
                              String fsPath,
                              int nCopies,
                              int createFile,
                              int createDir,
                              int rename,
                              int createLayeredDir,
                              int createLayeredFile,
                              int removeNode,
                              int modifyFile,
                              int readFile,
                              int snapshot,
                              int opCount) throws Throwable
    {
        try
        {
            BulkLoader loader = new BulkLoader();
            loader.setAvmService(fService);
            long start = System.currentTimeMillis();
            for (int i = 0; i < nCopies; i++)
            {
                fService.createDirectory("main:/", "" + i);
                loader.recursiveLoad(fsPath, "main:/" + i);
                fService.createSnapshot("main", null, null);
            }
            System.out.println("Load time: " + (System.currentTimeMillis() - start));
            List<AVMTester> testers = new ArrayList<AVMTester>();
            List<Thread> threads = new ArrayList<Thread>();
            for (int i = 0; i < nThreads; i++)
            {
                AVMTester tester
                    = new AVMTester(createFile,
                                createDir,
                                rename,
                                createLayeredDir,
                                createLayeredFile,
                                removeNode,
                                modifyFile,
                                readFile,
                                snapshot,
                                opCount,
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
                                //fail();
                                System.err.println("Stress tester error");
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
            
            int errorCnt = 0;
            for (AVMTester tester : testers)
            {
                errorCnt += (tester.getError() ? 1 : 0);
            }
            
            if (errorCnt > 0)
            {
            	StringBuffer errorStack = new StringBuffer();
            	errorStack.append("Stress tester errors: ").append(errorCnt).append(" out of ").append(testers.size()).append(" are in error state");
            	
            	for (AVMTester tester : testers)
                {
                   if (tester.getError())
                   {
                	   errorStack.append("\n\n").append(tester.getErrorStackTrace());
                   }
                }
            	
            	fail(errorStack.toString());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
    }
}
