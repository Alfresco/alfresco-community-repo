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

package org.alfresco.repo.avm.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;

/**
 * Testing utility class. Reads a tree recursively.
 * @author britt
 */
public class BulkReader
{
    private AVMService fService;
    
    public BulkReader()
    {
    }
    
    public void setAvmService(AVMService service)
    {
        fService = service;
    }
    
    public void recursiveFutz(String store, String path, int futz)
    {
        List<String> paths = new ArrayList<String>();
        recursiveRead(path, paths);
        Random random = new Random(System.currentTimeMillis());
        int futzed = 0;
        while (futzed < futz)
        {
            String futzPath = paths.get(random.nextInt(paths.size()));
            AVMNodeDescriptor desc = fService.lookup(-1, futzPath);
            if (desc.isFile())
            {
                try
                {
                    fService.getFileOutputStream(futzPath).close();
                }
                catch (IOException e)
                {
                    // Do nothing.
                }
                futzed++;
            }
        }
        fService.createSnapshot(store, null, null);
    }
    
    public void recursiveRead(String path, List<String> paths)
    {
        AVMNodeDescriptor desc = fService.lookup(-1, path);
        paths.add(desc.getPath());
        if (desc.isFile())
        {
            InputStream in = fService.getFileInputStream(desc);
            try
            {
                byte[] buff = new byte[8192];
                while (in.read(buff) != -1);
                in.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return;
        }
        Map<String, AVMNodeDescriptor> listing = fService.getDirectoryListing(desc);
        for (Map.Entry<String, AVMNodeDescriptor> entry : listing.entrySet())
        {
            recursiveRead(entry.getValue().getPath(), paths);
        }
    }
}
