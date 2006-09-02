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
