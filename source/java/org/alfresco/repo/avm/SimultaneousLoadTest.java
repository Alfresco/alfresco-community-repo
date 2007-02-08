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

import org.alfresco.repo.avm.util.BulkLoader;

/**
 * This tests loading content simultaneously.
 * @author britt
 */
public class SimultaneousLoadTest extends AVMServiceTestBase
{
    /**
     * Test loading content simultaneously.
     */
    public void testSimultaneousLoad()
    {
//        try
//        {
//            int n = 1;
//            int m = 1;
//            fReaper.setInactiveBaseSleep(60000);
//            for (int i = 0; i < n; i++)
//            {
//                fService.createDirectory("main:/", "d" + i);
//            }
//            fService.createSnapshot("main", null, null);
//            Thread [] threads = new Thread[n];
//            for (int i = 0; i < n; i++)
//            {
//                Loader loader = new Loader("/Users/britt/stuff/" + i, "main:/d" + i, m);
//                threads[i] = new Thread(loader);
//                threads[i].start();
//            }
//            for (int i = 0; i < n; i++)
//            {
//                threads[i].join();
//            }
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace(System.err);
//            fail();
//        }
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
