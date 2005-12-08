/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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

    public UserTransaction getNonPropagatingUserTransaction()
    {
        return txn;
    }

    public UserTransaction getUserTransaction()
    {
        return txn;
    }
    
    public UserTransaction getUserTransaction(boolean readonly)
    {
        return txn;
    }
}
