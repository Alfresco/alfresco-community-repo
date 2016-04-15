/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.filesys.repo.rules.operations;

import org.alfresco.filesys.repo.rules.Operation;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Rename a file within the same directory
 */
public class MoveFileOperation implements Operation
{ 
    private String from;
    private String to;
    private String fromPath;
    private String toPath;
    NodeRef rootNodeRef;
    
    /**
     * 
     * @param from name of file from
     * @param to name of file to
     * @param fromPath full path of from
     * @param toPath full path of to
     * @param rootNodeRef
     */
    public MoveFileOperation(String from, String to, String fromPath, String toPath, NodeRef rootNodeRef)
    {
        this.from = from;
        this.to = to;
        this.fromPath = fromPath;
        this.toPath = toPath;
        this.rootNodeRef = rootNodeRef;
    }

    
    public String getFrom()
    {
        return from;
    }
    
    public String getTo()
    {
        return to;
    }
    
    public String getToPath()
    {
        return toPath;
    }
    
    public String getFromPath()
    {
        return fromPath;
    }
    
    public NodeRef getRootNodeRef()
    {
        return rootNodeRef;
    }
    
    public String toString()
    {
        return "MoveFileOperation: from " + fromPath + " to "+ toPath;
    }
    
    public int hashCode()
    {
        return fromPath.hashCode();
    }
    
    public boolean equals(Object o)
    {
        if(o instanceof MoveFileOperation)
        {
            MoveFileOperation r = (MoveFileOperation)o;
            if(fromPath.equals(r.getFromPath()) && toPath.equals(r.getToPath()))
            {
                return true;
            }
        }
        return false;
    }
}



