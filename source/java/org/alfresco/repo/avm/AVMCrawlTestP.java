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
 * Another performance test that runs simultaneous crawlers that 
 * do operations with locality of reference.
 * @author britt
 */
public class AVMCrawlTestP extends AVMServiceTestBase
{
    /**
     * Do the crawl test.
     */
    public void testCrawl()
    {
        int n = 4;              // Number of Threads.
        int m = 16;             // How many multiples of content to start with.
        long runTime = 3600000;  // 6 hours.
        fService.purgeAVMStore("main");
        BulkLoader loader = new BulkLoader();
        loader.setAvmService(fService);
        for (int i = 0; i < m; i++)
        {
            fService.createAVMStore("d" + i);
            loader.recursiveLoad("source", "d" + i + ":/");
            fService.createSnapshot("d" + i, null, null);
        }
        long startTime = System.currentTimeMillis();
        List<AVMCrawler> crawlers = new ArrayList<AVMCrawler>();
        List<Thread> threads = new ArrayList<Thread>();
        for (int i = 0; i < n; i++)
        {
            crawlers.add(new AVMCrawler(fService));
            threads.add(new Thread(crawlers.get(i)));
            threads.get(i).start();
        }
        while (true)
        {
            try
            {
                Thread.sleep(5000);
                // Check that none of the crawlers has errored out.
                for (AVMCrawler crawler : crawlers)
                {
                    if (crawler.getError())
                    {
                        for (AVMCrawler craw : crawlers)
                        {
                            craw.setDone();
                        }
                        for (Thread thread : threads)
                        {
                            try
                            {
                                thread.join();
                            }
                            catch (InterruptedException ie)
                            {
                                // Do nothing.
                            }
                        }
                        fail();
                    }
                }
            }
            catch (InterruptedException ie)
            {
                // Do nothing.
            }
            long now = System.currentTimeMillis();
            if (now - startTime > runTime)
            {
                break;
            }
        }
        for (AVMCrawler crawler : crawlers)
        {
            crawler.setDone();
        }
        for (Thread thread : threads)
        {
            try
            {
                thread.join();
            }
            catch (InterruptedException ie)
            {
                // Do nothing.
            }
        }
        long ops = 0L;
        for (AVMCrawler crawler : crawlers)
        {
            ops += crawler.getOpCount();
        }
        long time = System.currentTimeMillis() - startTime;
        System.out.println("Ops/Sec: " + (ops * 1000L / time));
    }
}
