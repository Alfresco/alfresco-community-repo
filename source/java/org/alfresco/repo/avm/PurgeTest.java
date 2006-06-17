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

import org.alfresco.repo.avm.util.BulkLoad;

/**
 * Test the purge thread.
 * @author britt
 */
public class PurgeTest extends AVMServiceTestBase
{
    /**
     * Test purging a version.
     */
    public void testPurgeVersion()
    {
        try
        {
            OrphanReaper reaper = new OrphanReaper();
            reaper.init();
            setupBasicTree();
            BulkLoad loader = new BulkLoad(fService);
            long start = System.currentTimeMillis();
            loader.recursiveLoad("source", "main:/");
            System.err.println("Load time: " + (System.currentTimeMillis() - start) + "ms");
            fService.createSnapshot("main");
            System.err.println("Load time + snapshot: " + (System.currentTimeMillis() - start) + "ms");
            fService.purgeVersion(2, "main");
            reaper.activate();
            while (reaper.isActive())
            {
                try
                {
                    Thread.sleep(2000);
                }
                catch (InterruptedException e)
                {
                    // Do nothing.
                }
            }
            reaper.shutDown();
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Test purging a version that's not the latest.
     */
    public void testPurgeOlderVersion()
    {
        try
        {
            OrphanReaper reaper = new OrphanReaper();
            reaper.init();
            setupBasicTree();
            BulkLoad loader = new BulkLoad(fService);
            long start = System.currentTimeMillis();
            loader.recursiveLoad("source", "main:/");
            System.err.println("Load time: " + (System.currentTimeMillis() - start) + "ms");
            fService.createSnapshot("main");
            System.err.println("Load time + snapshot: " + (System.currentTimeMillis() - start) + "ms");
            fService.removeNode("main:/source/java/org/alfresco", "repo");
            fService.createSnapshot("main");
            fService.purgeVersion(2, "main");
            reaper.activate();
            while (reaper.isActive())
            {
                try
                {
                    Thread.sleep(2000);
                }
                catch (InterruptedException e)
                {
                    // Do nothing.
                }
            }
            reaper.shutDown();
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }    

    /**
     * Test purging an entire repository.
     */
    public void testPurgeRepository()
    {
        try
        {
            OrphanReaper reaper = new OrphanReaper();
            reaper.init();
            setupBasicTree();
            BulkLoad loader = new BulkLoad(fService);
            long start = System.currentTimeMillis();
            loader.recursiveLoad("source", "main:/");
            System.err.println("Load time: " + (System.currentTimeMillis() - start) + "ms");
            fService.createSnapshot("main");
            System.err.println("Load time + snapshot: " + (System.currentTimeMillis() - start) + "ms");
            fService.createLayeredDirectory("main:/source", "main:/", "layer");
            fService.removeNode("main:/layer/java/org/alfresco", "repo");
            fService.createFile("main:/layer/java/org/alfresco", "goofy").close();
            fService.createSnapshot("main");
            fService.purgeRepository("main");
            reaper.activate();
            while (reaper.isActive())
            {
                try
                {
                    Thread.sleep(2000);
                }
                catch (InterruptedException e)
                {
                    // Do nothing.
                }
            }
            reaper.shutDown();
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }
}
