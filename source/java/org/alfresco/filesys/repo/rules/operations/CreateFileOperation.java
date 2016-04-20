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
