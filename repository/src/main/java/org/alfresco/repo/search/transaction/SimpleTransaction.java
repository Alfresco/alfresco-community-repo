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

import java.io.UnsupportedEncodingException;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.xa.XAResource;

import org.alfresco.util.GUID;

public class SimpleTransaction implements XidTransaction
{
    private static final int DEFAULT_TIMEOUT = 600;

    private boolean isRollBackOnly;

    private int timeout;

    public static final int FORMAT_ID = 12321;

    private static final String CHAR_SET = "UTF-8";

    private byte[] globalTransactionId;

    private byte[] branchQualifier;

    // This is the transactoin id
    private String guid;

    private static ThreadLocal<SimpleTransaction> transaction = new ThreadLocal<SimpleTransaction>();

    private SimpleTransaction(int timeout)
    {
        super();
        this.timeout = timeout;
        guid = GUID.generate();
        try
        {
            globalTransactionId = guid.getBytes(CHAR_SET);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new XidException(e);
        }
        branchQualifier = new byte[0];
    }

    private SimpleTransaction()
    {
        this(DEFAULT_TIMEOUT);
    }

    public static SimpleTransaction getTransaction()
    {
        return (SimpleTransaction) transaction.get();
    }

    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
            SecurityException, SystemException
    {
        try
        {
            if (isRollBackOnly)
            {
                throw new RollbackException("Commit failed: Transaction marked for rollback");
            }

        }
        finally
        {
            transaction.set(null);
        }
    }

    public boolean delistResource(XAResource arg0, int arg1) throws IllegalStateException, SystemException
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public boolean enlistResource(XAResource arg0) throws RollbackException, IllegalStateException, SystemException
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public int getStatus() throws SystemException
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public void registerSynchronization(Synchronization arg0) throws RollbackException, IllegalStateException,
            SystemException
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public void rollback() throws IllegalStateException, SystemException
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException
    {
        isRollBackOnly = true;
    }

    /*
     * Support for suspend, resume and begin.
     */

    /* package */static SimpleTransaction suspend()
    {
        SimpleTransaction tx = getTransaction();
        transaction.set(null);
        return tx;
    }

    /* package */static void begin() throws NotSupportedException
    {
        if (getTransaction() != null)
        {
            throw new NotSupportedException("Nested transactions are not supported");
        }
        transaction.set(new SimpleTransaction());
    }

    /* package */static void resume(SimpleTransaction tx)
    {
        if (getTransaction() != null)
        {
            throw new IllegalStateException("A transaction is already associated with the thread");
        }
        transaction.set((SimpleTransaction) tx);
    }

    public String getGUID()
    {
        return guid;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof SimpleTransaction))
        {
            return false;
        }
        SimpleTransaction other = (SimpleTransaction) o;
        return this.getGUID().equals(other.getGUID());
    }

    public int hashCode()
    {
        return getGUID().hashCode();
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer(128);
        buffer.append("Simple Transaction GUID = " + getGUID());
        return buffer.toString();
    }

    public int getFormatId()
    {
        return FORMAT_ID;
    }

    public byte[] getGlobalTransactionId()
    {
        return globalTransactionId;
    }

    public byte[] getBranchQualifier()
    {
        return branchQualifier;
    }
}
