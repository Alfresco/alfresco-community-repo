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
