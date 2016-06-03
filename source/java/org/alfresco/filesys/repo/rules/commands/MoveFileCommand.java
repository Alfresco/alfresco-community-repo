package org.alfresco.filesys.repo.rules.commands;

import org.alfresco.filesys.repo.rules.Command;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.service.cmr.repository.NodeRef;

public class MoveFileCommand implements Command
{
    
    private String from;
    private String to;
    private NodeRef rootNode;
    private String fromPath;
    private String toPath;
    private boolean isMoveAsSystem = false;
    
    public MoveFileCommand(String from, String to, NodeRef rootNode, String fromPath, String toPath)
    {
        this.from = from;
        this.to = to;
        this.rootNode = rootNode;
        this.fromPath = fromPath;
        this.toPath = toPath;
    }

    // ALF-16257: in shuffle scenarios if user has insufficient permissions rename should be done as System
    public MoveFileCommand(String from, String to, NodeRef rootNode, String fromPath, String toPath, boolean moveAsSystem)
    {
        this(from, to, rootNode, fromPath, toPath);
        this.isMoveAsSystem = moveAsSystem;
    }

    public String getFrom()
    {
        return from;
    }
    
    public String getTo()
    {
        return to;
    }


    @Override
    public TxnReadState getTransactionRequired()
    {
        return TxnReadState.TXN_READ_WRITE;
    }


    public void setRootNode(NodeRef rootNode)
    {
        this.rootNode = rootNode;
    }


    public NodeRef getRootNode()
    {
        return rootNode;
    }


    public void setFromPath(String fromPath)
    {
        this.fromPath = fromPath;
    }


    public String getFromPath()
    {
        return fromPath;
    }


    public void setToPath(String toPath)
    {
        this.toPath = toPath;
    }


    public String getToPath()
    {
        return toPath;
    }

    public boolean isMoveAsSystem()
    {
        return isMoveAsSystem;
    }
}
