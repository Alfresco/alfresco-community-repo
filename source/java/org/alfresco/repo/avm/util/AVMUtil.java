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

import org.alfresco.service.cmr.avm.AVMBadArgumentException;
import org.alfresco.service.cmr.avm.AVMException;


/**
 * Helper methods and constants related to AVM (not WCM-specific)
 * 
 * @author janv
 */
public class AVMUtil
{
    /**
     * Utility to get AVM store name from AVM path, for example "foo:/bar/baz" returns "foo"
     * 
     * @param avmPath
     * @return
     */
    public static String getStoreName(String avmPath)
    {
        int i = avmPath.indexOf(AVM_STORE_SEPARATOR_CHAR);
        if (i == -1)
        {
            throw new AVMBadArgumentException("path " + avmPath + " does not contain a store");
        }
        return avmPath.substring(0, i);
    }
    
    /**
     * Utility to split an AVM path, for example "foo:/bar/baz", into its AVM repository store name ("foo") and path ("/bar/baz") parts.
     * 
     * @param path  The fully qualified path.
     * @return The store name and the store relative path.
     */
    public static String[] splitPath(String path)
    {
        String[] pathParts = path.split(AVM_STORE_SEPARATOR);
        if (pathParts.length != 2)
        {
            throw new AVMBadArgumentException("Invalid path: " + path);
        }
        return pathParts;
    }
    
    /**
     * Split a path into its parent path and its base name. If the store root path is passed, then return [null, ""].
     * 
     * @param path The initial AVM path.
     * 
     * @return An array of 2 Strings containing the parent AVM path (or null) and the base
     * name.
     */
    public static String[] splitBase(String path)
    {
        path = path.replaceAll("/+", AVM_PATH_SEPARATOR);
        while (path.endsWith(AVM_PATH_SEPARATOR) && !path.endsWith(AVM_STORE_PATH_SEPARATOR))
        {
            // ends with "/" not ":/"
            path = path.substring(0, path.length() - 1);
        }
        if (path.endsWith(AVM_STORE_PATH_SEPARATOR))
        {
            // end with ":/"
            return new String[] { null, "" };
        }
        int off = path.lastIndexOf(AVM_PATH_SEPARATOR);
        if (off == -1)
        {
            throw new AVMException("Invalid Path: " + path);
        }
        String [] decomposed = new String[2];
        decomposed[0] = path.substring(0, off);
        if (decomposed[0].charAt(decomposed[0].length()-1) == AVM_STORE_SEPARATOR_CHAR)
        {
            decomposed[0] = decomposed[0] + AVM_PATH_SEPARATOR_CHAR;
        }
        decomposed[1] = path.substring(off + 1);
        return decomposed;
    }
    
    public static String buildAVMPath(String storeName, String storeRelativePath)
    {
        // note: assumes storeRelativePath is not null and does not contain ':', although will add leading slash (if missing)
        StringBuilder builder = new StringBuilder();
        builder.append(storeName).append(AVMUtil.AVM_STORE_SEPARATOR_CHAR);
        if ((storeRelativePath.length() == 0) || (storeRelativePath.charAt(0) != AVM_PATH_SEPARATOR_CHAR))
        {
            builder.append(AVM_PATH_SEPARATOR_CHAR);
        }
        builder.append(storeRelativePath);
        return builder.toString();
    }
    
    public static String extendAVMPath(String path, String name)
    {
        if (path.endsWith(AVM_PATH_SEPARATOR))
        {
            return path + name;
        }
        else
        {
            return path + AVM_PATH_SEPARATOR_CHAR + name;
        }
    }
    
    public static String normalizePath(String path)
    {
        path = path.replaceAll("/+", AVM_PATH_SEPARATOR);
        path = path.replaceAll("/$", "");
        return path;
    }
    
    public static String addLeadingSlash(String relativePath)
    {
        if ((relativePath.length() == 0) || (relativePath.charAt(0) != AVM_PATH_SEPARATOR_CHAR))
        {
            relativePath = AVM_PATH_SEPARATOR_CHAR + relativePath;
        }
        
        return relativePath;
    }
    
    public static final char AVM_PATH_SEPARATOR_CHAR = '/';
    public static final String AVM_PATH_SEPARATOR = AVM_PATH_SEPARATOR_CHAR+"";
    
    public static final char AVM_STORE_SEPARATOR_CHAR = ':';
    public static final String AVM_STORE_SEPARATOR = AVM_STORE_SEPARATOR_CHAR+""; 
    
    private static final String AVM_STORE_PATH_SEPARATOR = AVM_STORE_SEPARATOR_CHAR+AVM_PATH_SEPARATOR;
    
    public static final String INITIAL_SNAPSHOT = "Initial Empty Version.";
}
