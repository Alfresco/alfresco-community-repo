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

package org.alfresco.repo.avm.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.alfresco.service.cmr.avm.AVMException;
import org.alfresco.service.cmr.avm.AVMService;

/**
 * This takes a filesystem directory path and a repository path and name
 * and bulk loads recursively from the filesystem.
 * @author britt
 */
public class BulkLoader
{
    private AVMService fService;
    
    /**
     * Create a new one.
     */
    public BulkLoader()
    {
    }

    /**
     * Set the AVMService.
     * @param service
     */
    public void setAvmService(AVMService service)
    {
        fService = service;
    }
    
    /**
     * Recursively load content.
     * @param fsPath The path in the filesystem.
     * @param repPath
     */
    public void recursiveLoad(String fsPath, String repPath)
    {
        File file = new File(fsPath);
        String name = file.getName();
        if (file.isDirectory())
        {
            fService.createDirectory(repPath, name);
            String[] children = file.list();
            String baseName = repPath.endsWith("/") ? repPath + name : repPath + "/" + name;
            for (String child : children)
            {
                recursiveLoad(fsPath + "/" + child, baseName);
            }
        }
        else
        {
            try
            {
                InputStream in = new FileInputStream(file);
                OutputStream out = fService.createFile(repPath, name);
                byte[] buff = new byte[8192];
                int read = 0;
                while ((read = in.read(buff)) != -1)
                {
                    out.write(buff, 0, read);
                }
                out.close();
                in.close();
            }
            catch (IOException e)
            {
                e.printStackTrace(System.err);
                throw new AVMException("I/O Error");
            }
        }
    }
}
