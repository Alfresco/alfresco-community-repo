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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.repo.avm;

import org.alfresco.repo.avm.util.BulkLoader;
import org.alfresco.repo.avm.util.BulkReader;

/**
 * Test of scaling out to large numbers of files.
 * @author britt
 */
public class AVMScaleTestP extends AVMServiceTestBase
{
    public void testScaleA()
    {
        testScaling(1,
                    getSourceFolder() + "/org/alfresco/repo/avm/actions", // relative from .../repository
                    1);
    }
    
    public void testScaleB()
    {
        testScaling(2,
                    getSourceFolder() + "/org/alfresco/repo/avm", // relative from .../repository
                    2);
    }

    /*
    public void xtestScaleZ()
    {
        testScaling(250,         
                    "/Users/britt/hibernate-3.1",
                    10);
    }
    */
    
    /**
     * Do the scale test
     * 
     * @param n             Number of bulkloads to do
     * @param fsPath        The path in the filesystem to load (tree of stuff) from
     * @param futzCount     The number of post snapshot modifications to make after each load
     */
    private void testScaling(int n, String fsPath, int futzCount)
    {
        try
        {
            BulkLoader loader = new BulkLoader();
            loader.setAvmService(fService);
            loader.setPropertyCount(50);
            BulkReader reader = new BulkReader();
            reader.setAvmService(fService);
            long lastTime = System.currentTimeMillis();
            for (int i = 0; i < n; i++)
            {
                System.out.println("Round " + (i + 1));
                fService.createStore("store" + i);
                loader.recursiveLoad(fsPath, "store" + i + ":/");
                fService.createSnapshot("store" + i, null, null);
                long now = System.currentTimeMillis();
                System.out.println("Load Time: " + (now - lastTime) + "ms");
                lastTime = now;
                reader.recursiveFutz("store" + i, "store" + i + ":/", futzCount);
                now = System.currentTimeMillis();
                System.out.println("Read Time: " + (now - lastTime) + "ms");
                System.out.flush();
                lastTime = now;
            }
        }
        finally
        {
            for (int i = 0; i < n; i++)
            {
                if (fService.getStore("store" + i) != null)
                {
                    fService.purgeStore("store" + i);
                }
            }
        }
    }
}
