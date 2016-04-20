package org.alfresco.filesys.repo.rules.operations;

import org.alfresco.filesys.repo.rules.Operation;
import org.alfresco.service.cmr.repository.NodeRef;

public class DeleteFileOperation implements Operation
{
    
    private String name;
    private NodeRef rootNodeRef;
    private String path;
    
    /**
     * Delete File Operation
     * @param name of file
     * @param rootNodeRef root node ref
     * @param path path + name of file to delete
     */
    public DeleteFileOperation(String name, NodeRef rootNodeRef, String path)
    {
        this.name = name;
        this.rootNodeRef = rootNodeRef;
        this.path = path;
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
        return rootNodeRef;
    }
    
    public String toString()
    {
        return "DeleteFileOperation: " + name;
    }
    
    public int hashCode()
    {
        return name.hashCode();
    }
    
    public boolean equals(Object o)
    {
        if(o instanceof DeleteFileOperation)
        {
            DeleteFileOperation c = (DeleteFileOperation)o;
            if(name.equals(c.getName()))
            {
                return true;
            }
        }
        return false;
    }

}
