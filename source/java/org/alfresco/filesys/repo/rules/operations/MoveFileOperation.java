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



