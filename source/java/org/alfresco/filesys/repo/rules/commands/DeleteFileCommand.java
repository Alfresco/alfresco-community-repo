package org.alfresco.filesys.repo.rules.commands;

import org.alfresco.filesys.repo.rules.Command;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Delete File command
 */
public class DeleteFileCommand implements Command
{
  private String name;
  private NodeRef rootNode;
  private String path;
    
  /**
   * 
   * @param name name of file
   * @param rootNode root node
   * @param path full path of file
   */
    public DeleteFileCommand(String name, NodeRef rootNode, String path)
    {
        this.name = name;
        this.rootNode = rootNode;
        this.path = path;
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
}
