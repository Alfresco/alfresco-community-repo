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

import org.alfresco.repo.avm.util.BulkLoader;
import org.alfresco.test_category.LegacyCategory;
import org.junit.experimental.categories.Category;

/**
 * This tests loading content simultaneously.
 * @author britt
 */
@Category(LegacyCategory.class)
public class SimultaneousLoadTest extends AVMServiceTestBase
{
    /*
    public void xtestSimulLoadA() throws Throwable
    {
        testSimultaneousLoad(1,1);
    }
    */
    
    public void testSimulLoadB() throws Throwable
    {
        testSimultaneousLoad(5,3);
    }
    
    /**
     * Test loading content simultaneously.
     */
    private void testSimultaneousLoad(int n, int m) throws Throwable
    {
        try
        {
            fReaper.setActiveBaseSleep(60000);
            for (int i = 0; i < n; i++)
            {
                fService.createDirectory("main:/", "d" + i);
            }
            fService.createSnapshot("main", null, null);
            Thread [] threads = new Thread[n];
            for (int i = 0; i < n; i++)
            {
                //Loader loader = new Loader("/Users/britt/stuff/" + i, "main:/d" + i, m);
                Loader loader = new Loader("source/java/org/alfresco/repo/avm/actions", "main:/d" + i, m);
                
                threads[i] = new Thread(loader);
                threads[i].start();
            }
            for (int i = 0; i < n; i++)
            {
                threads[i].join();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
    }
    
    private class Loader implements Runnable
    {
        /**
         * The BulkLoader.
         */
        private BulkLoader fLoader;
        
        /**
         * The source directory.
         */
        private String fSource;
        
        /**
         * The destination path.
         */
        private String fDestination;
        
        /**
         * The number of copies of stuff to make serially.
         */
        private int fCount;
        
        /**
         * Set up.
         * @param source Source directory.
         * @param destination Destination path.
         */
        public Loader(String source, String destination, int count)
        {
            fLoader = new BulkLoader();
            fLoader.setAvmService(fService);
            fSource = source;
            fDestination = destination;
            fCount = count;
        }
        
        public void run()
        {
            for (int i = 0; i < fCount; i++)
            {
                fService.createDirectory(fDestination, "" + i);
                fLoader.recursiveLoad(fSource, fDestination + "/" + i);
            }
        }
    }
}
