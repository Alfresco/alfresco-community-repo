package org.alfresco.filesys.repo.rules.commands;

import java.util.List;

import org.alfresco.filesys.repo.rules.Command;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * CopyContent command
 * 
 * Copy just the content from one node to another
 *
 */
public class CopyContentCommand implements Command
{
    
    private String from;
    private String to;
    private NodeRef rootNode;
    private String fromPath;
    private String toPath;
    
    public CopyContentCommand(String from, String to, NodeRef rootNode, String fromPath, String toPath)
    {
        this.from = from;
        this.to = to;
        this.rootNode = rootNode;
        this.fromPath = fromPath;
        this.toPath = toPath;
    }
    
    public String getTo()
    {
        return from;
    }
 
    public String getFrom()
    {
        return from;
    }
    
    public NodeRef getRootNode()
    {
        return rootNode;
    }
    
    public String getFromPath()
    {
        return fromPath;
    }
    
    public String getToPath()
    {
        return toPath;
    }

    @Override
    public TxnReadState getTransactionRequired()
    {
        return TxnReadState.TXN_READ_WRITE;
    }
}
