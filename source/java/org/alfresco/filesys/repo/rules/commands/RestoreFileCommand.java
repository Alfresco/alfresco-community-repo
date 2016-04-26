package org.alfresco.filesys.repo.rules.commands;

import org.alfresco.filesys.repo.rules.Command;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Restore File Command
 */
public class RestoreFileCommand implements Command
{
    private String name;
    private NodeRef rootNode;
    private String path;
    private long allocationSize;
    private NodeRef originalNodeRef;
    
    public RestoreFileCommand(String name, NodeRef rootNode, String path, long allocationSize, NodeRef originalNodeRef)
    {
        this.name = name;
        this.path = path;
        this.rootNode = rootNode;
        this.allocationSize = allocationSize;
        this.originalNodeRef = originalNodeRef;
    }

    public String getName()
    {
        return name;
    }
    
    public NodeRef getRootNode()
    {
        return rootNode;
    }
    
    public String getPath()
    {
        return path;
    }
    
    public NodeRef getOriginalNodeRef()
    {
        return originalNodeRef;
    }

    @Override
    public TxnReadState getTransactionRequired()
    {
        return TxnReadState.TXN_READ_WRITE;
    }

    public void setAllocationSize(long allocationSize)
    {
        this.allocationSize = allocationSize;
    }

    public long getAllocationSize()
    {
        return allocationSize;
    }
    
}
