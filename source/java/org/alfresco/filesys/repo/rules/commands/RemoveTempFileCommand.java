package org.alfresco.filesys.repo.rules.commands;

import org.alfresco.filesys.repo.TempNetworkFile;
import org.alfresco.filesys.repo.rules.Command;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Remove a temporary file
 */
public class RemoveTempFileCommand implements Command
{
   
    private TempNetworkFile networkFile;
    
    public RemoveTempFileCommand(TempNetworkFile file)
    {
        this.networkFile = file;
    }

    @Override
    public TxnReadState getTransactionRequired()
    {
        return TxnReadState.TXN_NONE;
    }
    
    public TempNetworkFile getNetworkFile()
    {
        return networkFile;
    }
}
