/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
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
