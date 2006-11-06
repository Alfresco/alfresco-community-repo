/*
 * Copyright (C) 2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */

package org.alfresco.repo.avm;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.avm.util.BulkLoader;

/**
 * This is a stress test for the AVM repository.
 * @author britt
 */
public class AVMStressTest extends AVMServiceTestBase
{
    /**
     * Test N threads
     */
    public void testNThreads()
    {
//        try
//        {
//            int nCopies = 1;
//            int nThreads = 1;
//            BulkLoader loader = new BulkLoader();
//            loader.setAvmService(fService);
//            long start = System.currentTimeMillis();
//            for (int i = 0; i < nCopies; i++)
//            {
//                fService.createDirectory("main:/", "" + i);
//                loader.recursiveLoad("source", "main:/" + i);
//                fService.createSnapshot("main", null, null);
//            }
//            System.out.println("Load time: " + (System.currentTimeMillis() - start));
//            List<AVMTester> testers = new ArrayList<AVMTester>();
//            List<Thread> threads = new ArrayList<Thread>();
//            for (int i = 0; i < nThreads; i++)
//            {
//                AVMTester tester
//                    = new AVMTester(800,      // create file.
//                                    20,        // create dir,
//                                    0,        // rename
//                                    2,         // create layered dir
//                                    5,         // create layered file
//                                    10,        // remove node
//                                    20,        // modify file.
//                                    3200,        // read file
//                                    10,        // snapshot
//                                    1000,      // # ops
//                                    fService);   
//                tester.refresh();
//                Thread thread = new Thread(tester);
//                testers.add(tester);
//                threads.add(thread);
//            }
//            for (Thread thread : threads)
//            {
//                thread.start();
//            }
//            int exited = 0;
//            // long sampStart = System.currentTimeMillis();
//            while (exited != nThreads)
//            {
//                try
//                {
//                    Thread.sleep(2000);
//                    for (int i = 0; i < nThreads; i++)
//                    {
//                        if (threads.get(i) == null)
//                        {
//                            continue;
//                        }
//                        threads.get(i).join(1000);
//                        if (!threads.get(i).isAlive())
//                        {
//                            threads.set(i, null);
//                            if (testers.get(i).getError())
//                            {
//                                for (AVMTester tester : testers)
//                                {
//                                    tester.setExit();
//                                }
//                                fail();
//                            }
//                            exited++;
//                        }
//                    }
//                }
//                catch (InterruptedException e)
//                {
//                    // Do nothing.
//                }
//            }
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace(System.err);
//            fail();
//        }
    }
}
