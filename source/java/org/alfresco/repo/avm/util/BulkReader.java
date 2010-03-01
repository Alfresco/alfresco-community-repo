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
