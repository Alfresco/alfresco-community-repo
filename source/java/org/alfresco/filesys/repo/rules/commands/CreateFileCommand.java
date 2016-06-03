package org.alfresco.filesys.repo.rules.commands;

import java.util.List;

import org.alfresco.filesys.repo.rules.Command;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * CreateFile command
 */
public class CreateFileCommand implements Command
{
    private String name;
    private NodeRef rootNode;
    private String path;
    private long allocationSize;
    private boolean isHidden;
    
    public CreateFileCommand(String name, NodeRef rootNode, String path, long allocationSize, boolean isHidden)
    {
        this.name = name;
        this.path = path;
        this.rootNode = rootNode;
        this.allocationSize = allocationSize;
        this.isHidden = isHidden;
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
    
    public boolean isHidden()
    {
    	return isHidden;
    }
    
}
