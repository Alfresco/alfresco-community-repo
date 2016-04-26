package org.alfresco.filesys.repo.rules.commands;

import org.alfresco.filesys.repo.rules.Command;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;

/**
 * Callback command.
 * 
 * Makes a callback when executed.
 */
public class CallbackCommand implements Command
{
    public CallbackCommand()
    {
    }

    @Override
    public TxnReadState getTransactionRequired()
    {
        return TxnReadState.TXN_NONE;
    }
}
