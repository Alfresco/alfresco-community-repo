package org.alfresco.filesys.repo.rules.commands;

import java.util.List;

import org.alfresco.filesys.repo.OpenFileMode;
import org.alfresco.filesys.repo.rules.Command;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Open File command
 */
public class OpenFileCommand implements Command
{
    private String name;
    private OpenFileMode mode = OpenFileMode.READ_ONLY;
    private boolean truncate = false;
    private String path;
    private NodeRef rootNode;
    
    /**
     * 
     * @param name
     * @param mode
     * @param truncate
     * @param rootNode
     * @param path
     */
    public OpenFileCommand(String name, OpenFileMode mode, boolean truncate, NodeRef rootNode, String path)
    {
        this.name = name;
        this.mode = mode;
        this.truncate = truncate;
        this.rootNode = rootNode;
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
        return rootNode;
    }
    
    public OpenFileMode getMode()
    {
        return mode;
    }
    
    public boolean isTruncate()
    {
        return truncate;
    }

    @Override
    public TxnReadState getTransactionRequired()
    {
        return TxnReadState.TXN_READ_ONLY;
    }
}
