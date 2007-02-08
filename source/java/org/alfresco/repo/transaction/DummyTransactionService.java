/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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

    public boolean isReadOnly()
    {
        return false;
    }

    public void setReadOnly(boolean readOnly)
    {
    }

    public UserTransaction getUserTransaction()
    {
        return txn;
    }
    
    public UserTransaction getUserTransaction(boolean readOnly)
    {
        return txn;
    }

    public UserTransaction getNonPropagatingUserTransaction()
    {
        return txn;
    }

    public UserTransaction getNonPropagatingUserTransaction(boolean readOnly)
    {
        return txn;
    }
}
