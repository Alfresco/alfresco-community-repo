package org.alfresco.filesys.repo.rules.commands;

import org.alfresco.filesys.repo.rules.Command;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Open File command
 */
public class CloseFileCommand implements Command
{
    private String name;
    private String path;
    private NodeRef rootNode;
  
    private NetworkFile networkFile;
    
    public CloseFileCommand(String name, NetworkFile networkFile, NodeRef rootNode, String path)
    {
        this.name = name;
        this.networkFile = networkFile;
        this.rootNode = rootNode;
        this.path = path;
    }

    public String getName()
    {
        return name;
    }
    
    public NetworkFile getNetworkFile()
    {
        return networkFile;
    }
    
    public NodeRef getRootNodeRef()
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
}
