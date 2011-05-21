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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

package org.alfresco.repo.avm;

import org.alfresco.repo.avm.util.AVMUtil;
import org.alfresco.service.cmr.avm.AVMException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility for going back and forth between the AVM world and
 * the <code>StoreRef</code>, <code>NodeRef</code> world.
 * @author britt
 */
public class AVMNodeConverter
{
    private static Log fgLogger = LogFactory.getLog(AVMNodeConverter.class);
    
    /**
     * Get a NodeRef corresponding to the given path and version.
     * @param version The version id.
     * @param avmPath The AVM path.
     * @return A NodeRef with AVM info stuffed inside.
     */
    public static NodeRef ToNodeRef(int version, String avmPath)
    {
        String [] pathParts = AVMUtil.splitPath(avmPath);
        while (pathParts[1].endsWith(AVMUtil.AVM_PATH_SEPARATOR) && pathParts[1].length() > 1)
        {
            pathParts[1] = pathParts[1].substring(0, pathParts[1].length() - 1);
        }
        StoreRef storeRef = ToStoreRef(pathParts[0]);
        String translated = version + pathParts[1];
        translated = translated.replaceAll("/+", "|");
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
        
        if (translated.indexOf('|') != -1)
        {
            // we assume that this is the new style avm path
            translated = translated.replace('|', AVMUtil.AVM_PATH_SEPARATOR_CHAR);
        }
        else
        {
            // this is the old style avm path
            translated = translated.replace(';', AVMUtil.AVM_PATH_SEPARATOR_CHAR);
        }
        int off = translated.indexOf(AVMUtil.AVM_PATH_SEPARATOR_CHAR);
        if (off == -1)
        {
            fgLogger.error(translated);
            throw new AVMException("Bad Node Reference: " + nodeRef.getId());
        }
        int version = Integer.parseInt(translated.substring(0, off));
        String path = translated.substring(off);
        return new Pair<Integer, String>(version, AVMUtil.buildAVMPath(store.getIdentifier(), path));
    }
    
    /**
     * Extend an already valid AVM path by one more component.
     * @param path The starting AVM path.
     * @param name The name to add to it.
     * @return The extended path.
     */
    public static String ExtendAVMPath(String path, String name)
    {
        return AVMUtil.extendAVMPath(path, name);
    }
    
    /**
     * Split a path into its parent path and its base name.
     * @param path The initial AVM path.
     * @return An array of 2 Strings containing the parent path and the base
     * name.
     */
    public static String[] SplitBase(String path)
    {
        return AVMUtil.splitBase(path);
    }
    
    /**
     * Normalize an AVM path.
     * @param path The incoming path.
     * @return The normalized path.
     * 
     * @deprecated see org.alfresco.repo.avm.util.AVMUtil.normalizePath
     */
    public static String NormalizePath(String path)
    {
        return AVMUtil.normalizePath(path);
    }
}
