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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
        int m = 192;             // How many multiples of content to start with.
        long runTime = 3600000;  // 6 hours.
        fService.purgeStore("main");
        BulkLoader loader = new BulkLoader();
        loader.setAvmService(fService);
        for (int i = 0; i < m; i++)
        {
            fService.createStore("d" + i);
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
