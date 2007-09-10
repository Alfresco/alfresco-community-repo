/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing
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
    public void testScaling()
    {
        int n = 4; // The number of BulkLoads to do.
        int futzCount = 10; // The number of post snapshot modifications to make after each load.
        String load = "/Users/britt/hibernate-3.1"; // The tree of stuff to load.
        BulkLoader loader = new BulkLoader();
        loader.setAvmService(fService);
        loader.setPropertyCount(5);
        BulkReader reader = new BulkReader();
        reader.setAvmService(fService);
        long lastTime = System.currentTimeMillis();
        for (int i = 0; i < n; i++)
        {
            System.out.println("Round " + (i + 1));
            fService.createStore("store" + i);
            loader.recursiveLoad(load, "store" + i + ":/");
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
}
