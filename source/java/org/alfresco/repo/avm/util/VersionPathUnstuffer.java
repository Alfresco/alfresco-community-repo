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

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

/**
 * Takes a String representation of a list of Version/Paths (created
 * by VersionPathStuffer) and gives you back a list of Version/Paths
 * @author britt
 */
public final class VersionPathUnstuffer 
{
    /**
     * The unpacked version paths.
     */
    private List<Pair<Integer, String>> fVersionPaths;
    
    /**
     * Construct one and in the process unstuff the String.
     */
    public VersionPathUnstuffer(String stuffed)
    {
        fVersionPaths = new ArrayList<Pair<Integer, String>>();
        String[] versionPaths = stuffed.split(";");
        for (String path : versionPaths)
        {
            String [] pathVersion = path.split("@");
            Pair<Integer, String> item = 
                new Pair<Integer, String>(new Integer(pathVersion[1]),
                                          pathVersion[0]);
            fVersionPaths.add(item);
        }
    }
    
    /**
     * Get the raw list of Version/Paths.
     */
    public List<Pair<Integer, String>> getVersionPaths()
    {
        return fVersionPaths;
    }
    
    /**
     * Get the Version/Paths as NodeRefs.
     */
    public List<NodeRef> getNodeRefs()
    {
        List<NodeRef> result = new ArrayList<NodeRef>();
        for (Pair<Integer, String> item : fVersionPaths)
        {
            result.add(AVMNodeConverter.ToNodeRef(item.getFirst(), item.getSecond()));   
        }
        return result;
    }
}
