package org.alfresco.filesys.repo.rules;

import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;

/**
 * A Command is something that must be done.   Commands are higher level 
 * than Operations.    So a rule returns a command or set of commands to 
 * implement an operation.
 */
public interface Command
{
    /**
     * Is a transaction required to run this command?
     */
    TxnReadState getTransactionRequired();
    

}
