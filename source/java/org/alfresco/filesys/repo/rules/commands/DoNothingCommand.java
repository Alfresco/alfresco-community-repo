package org.alfresco.filesys.repo.rules.commands;

import org.alfresco.filesys.repo.rules.Command;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Do Nothing Command,  Does what it says on the tin!
 */
public class DoNothingCommand implements Command
{
   
    public DoNothingCommand()
    {
    }

    @Override
    public TxnReadState getTransactionRequired()
    {
        return TxnReadState.TXN_NONE;
    }
}
