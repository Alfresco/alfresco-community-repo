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
package org.alfresco.repo.search.transaction;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

public class SimpleTransactionManager implements TransactionManager
{
    private static SimpleTransactionManager manager = new SimpleTransactionManager();

    private int timeout;

    private SimpleTransactionManager()
    {
        super();
    }

    public static SimpleTransactionManager getInstance()
    {
        return manager;
    }

    public void begin() throws NotSupportedException, SystemException
    {
        SimpleTransaction.begin();

    }

    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
            SecurityException, IllegalStateException, SystemException
    {
        SimpleTransaction transaction = getTransactionChecked();
        transaction.commit();
    }

    public int getStatus() throws SystemException
    {
        SimpleTransaction transaction = getTransactionChecked();
        return transaction.getStatus();
    }

    public SimpleTransaction getTransaction() throws SystemException
    {
        return SimpleTransaction.getTransaction();
    }

    private SimpleTransaction getTransactionChecked() throws SystemException, IllegalStateException
    {
        SimpleTransaction tx = SimpleTransaction.getTransaction();
        if (tx == null)
        {
            throw new IllegalStateException("The thread is not bound to a transaction.");
        }
        return tx;
    }

    public void resume(Transaction tx) throws InvalidTransactionException, IllegalStateException, SystemException
    {
        if (!(tx instanceof SimpleTransaction))
        {
            throw new IllegalStateException("Transaction must be a SimpleTransaction to resume");
        }
        SimpleTransaction.resume((SimpleTransaction) tx);
    }

    public void rollback() throws IllegalStateException, SecurityException, SystemException
    {
        SimpleTransaction transaction = getTransactionChecked();
        transaction.rollback();
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException
    {
        SimpleTransaction transaction = getTransactionChecked();
        transaction.setRollbackOnly();
    }

    public void setTransactionTimeout(int timeout) throws SystemException
    {
        this.timeout = timeout;
    }

    public SimpleTransaction suspend() throws SystemException
    {
        return SimpleTransaction.suspend();
    }

}
