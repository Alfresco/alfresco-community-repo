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
 * Create File Operation.
 * <p>
 * Create a file with the given name.
 */
public class CreateFileOperation implements Operation
{
    private String name;
    private NodeRef rootNodeRef;
    private String path;
    private long allocationSize;
    boolean isHidden;
    
    public CreateFileOperation(String name, NodeRef rootNodeRef, String path, long allocationSize, boolean isHidden)
    {
        this.name = name;
        this.rootNodeRef = rootNodeRef;
        this.path = path;
        this.allocationSize = allocationSize;
        this.isHidden = isHidden;
    }

    public String getName()
    {
        return name;
    }
    
    public String toString()
    {
        return "CreateFileOperation: " + name;
    }
    
    public String getPath()
    {
        return path;
    }
    
    public NodeRef getRootNodeRef()
    {
        return rootNodeRef;
    }
    
    public int hashCode()
    {
        return name.hashCode();
    }
    
    public boolean equals(Object o)
    {
        if(o instanceof CreateFileOperation)
        {
            CreateFileOperation c = (CreateFileOperation)o;
            if(name.equals(c.getName()))
            {
                return true;
            }
        }
        return false;
    }

    public void setAllocationSize(long allocationSize)
    {
        this.allocationSize = allocationSize;
    }

    public long getAllocationSize()
    {
        return allocationSize;
    }
    
    public boolean isHidden()
    {
        return isHidden;
    }
}
