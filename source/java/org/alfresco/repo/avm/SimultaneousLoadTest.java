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
