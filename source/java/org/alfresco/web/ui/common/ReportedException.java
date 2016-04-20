package org.alfresco.web.ui.common;

import javax.transaction.UserTransaction;

import org.alfresco.repo.transaction.RetryingTransactionHelper;

/**
 * Unchecked exception wrapping an already-reported exception.  The dialog code can use this to
 * detect whether or not to report further to the user.
 * 
 * @author Derek Hulley
 * @since 3.1
 */
public class ReportedException extends RuntimeException
{
    private static final long serialVersionUID = -4179045854462002741L;

    public ReportedException(Throwable e)
    {
        super(e);
    }
    
    /**
     * Throws the given exception if we are still in an active transaction,
     * this ensures that we cross the transaction boundary and thus cause
     * the transaction to rollback.
     * 
     * @param error The error to be thrown
     */
    public static void throwIfNecessary(Throwable error)
    {
       if (error != null)
       {
          UserTransaction txn = RetryingTransactionHelper.getActiveUserTransaction();
          if (txn != null)
          {
             throw new ReportedException(error);
          }
       }
    }
}
