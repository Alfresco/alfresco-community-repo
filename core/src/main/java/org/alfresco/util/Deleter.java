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

package org.alfresco.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility to delete a file or directory recursively.
 * @author britt
 */
public class Deleter
{
    private static final Log log = LogFactory.getLog(Deleter.class);
    
    /**
     * Delete by path.
     * @param path
     */
    public static void Delete(String path)
    {
        File toDelete = new File(path);
        Delete(toDelete);
    }
    
    /**
     * Delete by File.
     * @param toDelete
     */
    public static void Delete(File toDelete)
    {
        if (toDelete.isDirectory())
        {
            File[] listing = toDelete.listFiles();
            for (File file : listing)
            {
                Delete(file);
            }
        }
        toDelete.delete();
    }
    
    
    /**
     * Recursively deletes the parents of the specified file stopping when <code>rootDir</code> is reached.
     * The file itself must have been deleted before calling this method - since only empty
     * directories can be deleted.
     * <p>
     * For example: <code>deleteEmptyParents(new File("/tmp/a/b/c/d.txt"), "/tmp/a")</code>
     * <p>
     * Will delete directories c and b assuming that they are both empty. It will leave /tmp/a even if it is
     * empty as this is the <code>rootDir</code>
     * 
     * @param file     The path of the file whose parent directories should be deleted.
     * @param rootDir  Top level directory where deletion should stop. <strong>Must be the canonical path
     *                 to ensure correct comparisons.</strong>
     */
    public static void deleteEmptyParents(File file, String rootDir)
    {
        File parent = file.getParentFile();
        boolean deleted = false;
        do
        {
            try
            {
                if (parent.isDirectory() && !parent.getCanonicalPath().equals(rootDir))
                {
                    // Only an empty directory will successfully be deleted.
                    deleted = parent.delete();
                }
            }
            catch (IOException error)
            {
                log.error("Unable to construct canonical path for " + parent.getAbsolutePath());
                break;
            }
            
            parent = parent.getParentFile();
        }
        while(deleted);
    }
    
    /**
     * Same behaviour as for {@link Deleter#deleteEmptyParents(File, String)} but with the
     * <code>rootDir</code> parameter specified as a {@link java.io.File} object.
     * 
     * @see Deleter#deleteEmptyParents(File, String)
     * @param file
     * @param rootDir
     */
    public static void deleteEmptyParents(File file, File rootDir)
    {
        try
        {
            deleteEmptyParents(file, rootDir.getCanonicalPath());
        }
        catch (IOException e)
        {
            String msg = "Unable to convert rootDir to canonical form [rootDir=" + rootDir + "]";
            throw new RuntimeException(msg, e);
        }
    }
}
