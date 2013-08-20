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

import net.sf.acegisecurity.Authentication;

import org.alfresco.repo.avm.util.BulkLoader;
import org.alfresco.repo.security.authentication.AuthenticationUtil;


/**
 * Another performance test that runs simultaneous crawlers that 
 * do operations with locality of reference.
 * @author britt
 */
public class AVMCrawlTestP extends AVMServiceTestBase
{
    /*
    public void xtestCrawlA()
    {
        testCrawl(1,
                  getSourceFolder() + "/org/alfresco/repo/avm/actions", // relative from .../repository
                  1,
                  30000); // 30 secs
    }
    */
    
    public void testCrawlB()
    {
        testCrawl(2,
                  getSourceFolder() + "/org/alfresco/repo/avm/actions", // relative from .../repository
                  2,
                  30000); // 30 secs
    }
    
    /*
    public void xtestCrawlC()
    {
        testCrawl(10,
                  getSourceFolder() + "/org/alfresco/repo/avm", // relative from .../repository
                  2,
                  60000); // 1 min
    }
    */
    
    /*
    public void xtestCrawlZ()
    {
        testCrawl(8,         
                  "source", // relative from .../repository
                  2,         
                  28800000); // 8 hours
    }
    */

    /**
     * Do the crawl test
     * 
     * @param n             Number of threads
     * @param fsPath        The path in the filesystem to load (tree of stuff) from
     * @param m             How many multiples of content to start with
     * @param runTime       Min run time (in msecs)
     */
    private void testCrawl(int n, String fsPath, int m, long runTime)
    {
        try
        {
            Authentication authentication = AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.SYSTEM_USER_NAME);
            try
            {
                if (m < 1)
                {
                    fail("Must have at least one 1 copy of content");
                }
                
                BulkLoader loader = new BulkLoader();
                loader.setAvmService(fService);
                for (int i = 0; i < m; i++)
                {
                    fService.createStore("d" + i);
                    loader.recursiveLoad(fsPath, "d" + i + ":/");
                    fService.createSnapshot("d" + i, null, null);
                }
                long startTime = System.currentTimeMillis();
                List<AVMCrawler> crawlers = new ArrayList<AVMCrawler>();
                List<Thread> threads = new ArrayList<Thread>();
                for (int i = 0; i < n; i++)
                {
                    crawlers.add(new AVMCrawler(fService, authentication));
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
                                //fail();
                                System.err.println("Crawler error");
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
                int errorCnt = 0;
                for (AVMCrawler crawler : crawlers)
                {
                    ops += crawler.getOpCount();
                    errorCnt += (crawler.getError() ? 1 : 0);
                }
    
                long time = System.currentTimeMillis() - startTime;
                System.out.println("Ops/Sec: " + (ops * 1000L / time));
                
                if (errorCnt > 0)
                {
                	StringBuffer errorStack = new StringBuffer();
                	errorStack.append("Crawler errors: ").append(errorCnt).append(" out of ").append(crawlers.size()).append(" are in error state");
                	
                	for (AVMCrawler crawler : crawlers)
                    {
                       if (crawler.getError())
                       {
                    	   errorStack.append("\n\n").append(crawler.getErrorStackTrace());
                       }
                    }
                	
                	fail(errorStack.toString());
                }
            }
            finally
            {
                for (int i = 0; i < m; i++)
                {
                    if (fService.getStore("d" + i) != null)
                    {
                        fService.purgeStore("d" + i);
                    }
                }
            }
        }
        finally
        {
            AuthenticationUtil.clearCurrentSecurityContext();
        }
    }
}
