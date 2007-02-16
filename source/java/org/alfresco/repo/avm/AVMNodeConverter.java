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

import org.alfresco.service.cmr.avm.AVMException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.Pair;
import org.apache.log4j.Logger;

/**
 * Utility for going back and forth between the AVM world and
 * the <code>StoreRef</code>, <code>NodeRef</code> world.
 * @author britt
 */
public class AVMNodeConverter
{
    private static Logger fgLogger = Logger.getLogger(AVMNodeConverter.class);
    
    /**
     * Get a NodeRef corresponding to the given path and version.
     * @param version The version id.
     * @param avmPath The AVM path.
     * @return A NodeRef with AVM info stuffed inside.
     */
    public static NodeRef ToNodeRef(int version, String avmPath)
    {
        String [] pathParts = avmPath.split(":");
        if (pathParts.length != 2)
        {
            throw new AVMException("Malformed AVM Path: " + avmPath);
        }
        while (pathParts[1].endsWith("/") && pathParts[1].length() > 1)
        {
            pathParts[1] = pathParts[1].substring(0, pathParts[1].length() - 1);
        }
        StoreRef storeRef = ToStoreRef(pathParts[0]);
        String translated = version + pathParts[1];
        translated = translated.replaceAll("/+", ";");
        return new NodeRef(storeRef, translated);
    }
    
    /**
     * Get a StoreRef that corresponds to a given AVMStore name.
     * @param avmStore The name of the AVMStore.
     * @return A working StoreRef.
     */
    public static StoreRef ToStoreRef(String avmStore)
    {
        return new StoreRef(StoreRef.PROTOCOL_AVM, avmStore);
    }
    
    /**
     * Convert a NodeRef into a version, AVMPath pair.
     * @param nodeRef The NodeRef to convert.
     * @return An Integer, String array.
     */
    public static Pair<Integer, String> ToAVMVersionPath(NodeRef nodeRef)
    {
        StoreRef store = nodeRef.getStoreRef();
        String translated = nodeRef.getId();
        translated = translated.replace(';', '/');
        int off = translated.indexOf("/");
        if (off == -1)
        {
            fgLogger.error(translated);
            throw new AVMException("Bad Node Reference: " + nodeRef.getId());
        }
        int version = Integer.parseInt(translated.substring(0, off));
        String path = translated.substring(off);
        return new Pair<Integer, String>(version, store.getIdentifier() + ":" + path);
    }

    /**
     * Extend an already valid AVM path by one more component.
     * @param path The starting AVM path.
     * @param name The name to add to it.
     * @return The extended path.
     */
    public static String ExtendAVMPath(String path, String name)
    {
        if (path.endsWith("/"))
        {
            return path + name;
        }
        else
        {
            return path + "/" + name;
        }
    }

    /**
     * Split a path into its parent path and its base name.
     * @param path The initial AVM path.
     * @return An array of 2 Strings containing the parent path and the base
     * name.
     */
    public static String [] SplitBase(String path)
    {
        path = path.replaceAll("/+", "/");
        while (path.endsWith("/") && !path.endsWith(":/"))
        {
            path = path.substring(0, path.length() - 1);
        }
        if (path.endsWith(":/"))
        {
           return new String[] { null, "" };
        }
        int off = path.lastIndexOf("/");
        if (off == -1)
        {
            throw new AVMException("Invalid Path: " + path);
        }
        String [] decomposed = new String[2];
        decomposed[0] = path.substring(0, off);
        if (decomposed[0].endsWith(":"))
        {
            decomposed[0] = decomposed[0] + "/";
        }
        decomposed[1] = path.substring(off + 1);
        return decomposed;
    }
}
