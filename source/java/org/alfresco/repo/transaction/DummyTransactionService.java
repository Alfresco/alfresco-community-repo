package org.alfresco.repo.transaction;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.alfresco.service.transaction.TransactionService;

/**
 * Simple implementation of the transaction service that serve up
 * entirely useless user transactions.  It is useful within the context
 * of some tests.
 * 
 * @author Derek Hulley
 */
public class DummyTransactionService implements TransactionService
{
    private UserTransaction txn = new UserTransaction()
    {
        public void begin() {};
        public void commit() {};
        public int getStatus() {return Status.STATUS_NO_TRANSACTION;};
        public void rollback() {};
        public void setRollbackOnly() {};
        public void setTransactionTimeout(int arg0) {};
    };

    @Override
    public boolean getAllowWrite()
    {
        return true;
    }

    @Override
    public boolean isReadOnly()
    {
        return false;
    }

    @Override
    public UserTransaction getUserTransaction()
    {
        return txn;
    }
    
    @Override
    public UserTransaction getUserTransaction(boolean readOnly)
    {
        return txn;
    }

    @Override
    public UserTransaction getUserTransaction(boolean readOnly, boolean ignoreSystemReadOnly)
    {
        return txn;
    }

    @Override
    public UserTransaction getNonPropagatingUserTransaction()
    {
        return txn;
    }

    @Override
    public UserTransaction getNonPropagatingUserTransaction(boolean readOnly)
    {
        return txn;
    }

    @Override
    public UserTransaction getNonPropagatingUserTransaction(boolean readOnly, boolean ignoreSystemReadOnly)
    {
        return txn;
    }

    public RetryingTransactionHelper getRetryingTransactionHelper()
    {
        RetryingTransactionHelper helper = new RetryingTransactionHelper();
        helper.setMaxRetries(20);
        helper.setTransactionService(this);
        helper.setReadOnly(false);
        return helper;
    }
}
