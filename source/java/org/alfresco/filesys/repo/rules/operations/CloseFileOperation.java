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
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Close File Operation.
 * <p>
 * Close a file with the given name.
 */
public class CloseFileOperation implements Operation
{
    private String name;
    private NodeRef rootNodeRef;
    private String path;
    
    private NetworkFile networkFile;
    boolean deleteOnClose;
    boolean force;
    
    public CloseFileOperation(String name, NetworkFile networkFile, NodeRef rootNodeRef, String path, boolean deleteOnClose, boolean force)
    {
        this.name = name;
        this.networkFile = networkFile;
        this.rootNodeRef = rootNodeRef;
        this.path = path;
        this.deleteOnClose = deleteOnClose;
        this.force = force;
    }

    public String getName()
    {
        return name;
    }
    
    public NodeRef getRootNodeRef()
    {
        return rootNodeRef;
    }
    
    public String getPath()
    {
        return path;
    }
    
    public NetworkFile getNetworkFile()
    {
        return networkFile;
    }
    
    public String toString()
    {
        return "CloseFileOperation: " + name;
    }
    
    public int hashCode()
    {
        return name.hashCode();
    }
    
    public boolean isDeleteOnClose()
    {
        return deleteOnClose;
    }
    
    public boolean isForce()
    {
        return force;
    }
    
    public boolean equals(Object o)
    {
        if(o instanceof CloseFileOperation)
        {
            CloseFileOperation c = (CloseFileOperation)o;
            if(name.equals(c.getName()))
            {
                return true;
            }
        }
        return false;
    }
}
