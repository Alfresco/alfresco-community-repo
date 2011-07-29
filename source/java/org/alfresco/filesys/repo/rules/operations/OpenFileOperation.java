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
package org.alfresco.filesys.repo.rules.operations;

import org.alfresco.filesys.repo.rules.Operation;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Create File Operation.
 * <p>
 * Create a file with the given name.
 */
public class OpenFileOperation implements Operation
{
    private String name;
    private boolean writeAccess = false;
    private boolean truncate = false;
    private String path;
    private NodeRef rootNode;
    
    /**
     * 
     * @param name the name of the file to open
     * @param writeAccess if true open the file in read/write
     * @param rootNode
     * @param path the full path/name to open
     */
    public OpenFileOperation(String name, boolean writeAccess, boolean truncate, NodeRef rootNode, String path)
    {
        this.name = name;
        this.rootNode = rootNode;
        this.truncate = truncate;
        this.path = path;
        this.writeAccess = writeAccess;
    }

    public String getName()
    {
        return name;
    }
    
    public String getPath()
    {
        return path;
    }
    
    public NodeRef getRootNodeRef()
    {
        return rootNode;
    }
    
    public boolean isWriteAccess()
    {
        return writeAccess;
    }
    
    public boolean isTruncate()
    {
        return writeAccess;
    }
    
    public String toString()
    {
        return "OpenFileOperation: " + name;
    }
    
    public int hashCode()
    {
        return name.hashCode();
    }
    
    public boolean equals(Object o)
    {
        if(o instanceof OpenFileOperation)
        {
            OpenFileOperation c = (OpenFileOperation)o;
            if(name.equals(c.getName()))
            {
                return true;
            }
        }
        return false;
    }
}
