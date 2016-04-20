package org.alfresco.filesys.repo.rules.commands;

import org.alfresco.filesys.repo.rules.Command;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Remove file with CONTENT_NO_ASPECT on error
 */
public class RemoveNoContentFileOnError implements Command
{
    private String name;
    private String path;
    private NodeRef rootNode;
  
    private NetworkFile networkFile;
    
    public RemoveNoContentFileOnError(String name, NodeRef rootNode, String path)
    {
        this.name = name;
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
        return TxnReadState.TXN_NONE;
    }
}
