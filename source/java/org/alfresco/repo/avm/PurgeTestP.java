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

import org.alfresco.repo.avm.util.BulkLoader;

/**
 * Test the purge thread.
 * @author britt
 */
public class PurgeTestP extends AVMServiceTestBase
{
    /**
     * Test purging a version.
     */
    public void testPurgeVersion()
    {
        try
        {
            setupBasicTree();
            BulkLoader loader = new BulkLoader();
            loader.setAvmService(fService);
            long start = System.currentTimeMillis();
            loader.recursiveLoad("source/web", "main:/");
            System.err.println("Load time: " + (System.currentTimeMillis() - start) + "ms");
            fService.createSnapshot("main", null, null);
            System.err.println("Load time + snapshot: " + (System.currentTimeMillis() - start) + "ms");
            fService.purgeVersion(2, "main");
            fReaper.activate();
            while (fReaper.isActive())
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
            setupBasicTree();
            BulkLoader loader = new BulkLoader();
            loader.setAvmService(fService);
            long start = System.currentTimeMillis();
            loader.recursiveLoad("source", "main:/");
            System.err.println("Load time: " + (System.currentTimeMillis() - start) + "ms");
            fService.createSnapshot("main", null, null);
            System.err.println("Load time + snapshot: " + (System.currentTimeMillis() - start) + "ms");
            fService.removeNode("main:/source/java/org/alfresco", "repo");
            fService.createSnapshot("main", null, null);
            fService.purgeVersion(2, "main");
            fReaper.activate();
            while (fReaper.isActive())
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
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }    

    /**
     * Test purging an entire store.
     */
    public void testPurgeStore()
    {
        try
        {
            setupBasicTree();
            BulkLoader loader = new BulkLoader();
            loader.setAvmService(fService);
            long start = System.currentTimeMillis();
            loader.recursiveLoad("source", "main:/");
            System.err.println("Load time: " + (System.currentTimeMillis() - start) + "ms");
            fService.createSnapshot("main", null, null);
            System.err.println("Load time + snapshot: " + (System.currentTimeMillis() - start) + "ms");
            fService.createLayeredDirectory("main:/source", "main:/", "layer");
            fService.removeNode("main:/layer/java/org/alfresco", "repo");
            fService.createFile("main:/layer/java/org/alfresco", "goofy").close();
            fService.createSnapshot("main", null, null);
            fService.purgeStore("main");
            fReaper.activate();
            while (fReaper.isActive())
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
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }
}
