/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
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
