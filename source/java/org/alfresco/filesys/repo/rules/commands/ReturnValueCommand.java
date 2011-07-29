package org.alfresco.filesys.repo.rules.commands;

import org.alfresco.filesys.repo.rules.Command;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;

/**
 * Return a specified value to the executor
 */
public class ReturnValueCommand implements Command
{

    Object returnValue;
    public ReturnValueCommand(Object returnValue)
    {
        this.returnValue = returnValue;
    }
    
    public Object getReturnValue()
    {
        return returnValue;
    }
    
    @Override
    public TxnReadState getTransactionRequired()
    {
        
        return TxnReadState.TXN_NONE;
    }
}
