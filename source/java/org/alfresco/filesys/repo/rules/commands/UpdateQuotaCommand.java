package org.alfresco.filesys.repo.rules.commands;

import org.alfresco.filesys.repo.rules.Command;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Update the user's quota.
 */
public class UpdateQuotaCommand implements Command
{
   
    public UpdateQuotaCommand()
    {
    }

    @Override
    public TxnReadState getTransactionRequired()
    {
        return TxnReadState.TXN_NONE;
    }
}
