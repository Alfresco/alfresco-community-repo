/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.transaction;

import javax.transaction.UserTransaction;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.transaction.SpringAwareUserTransaction;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;

/**
 * Default implementation of Transaction Service.
 * <p>
 * Default retry behaviour: see {@link RetryingTransactionHelper#RetryingTransactionHelper()}
 * 
 * @author David Caruana
 */
public class TransactionServiceImpl implements TransactionService
{
    private PlatformTransactionManager transactionManager;
    private int maxRetries = -1;
    private int minRetryWaitMs = -1;
    private int maxRetryWaitMs = -1;
    private int retryWaitIncrementMs = -1;
    
    // SysAdmin cache - used to cluster certain JMX operations
    private SimpleCache<String, Object> sysAdminCache;
    private final static String KEY_SYSADMIN_ALLOW_WRITE = "sysAdminCache.txAllowWrite";
    
    
    /**
     * Set the transaction manager to use
     * 
     * @param transactionManager platform transaction manager
     */
    public void setTransactionManager(PlatformTransactionManager transactionManager)
    {
        this.transactionManager = transactionManager;
    }
    
    public void setSysAdminCache(SimpleCache<String, Object> sysAdminCache)
    {
        this.sysAdminCache = sysAdminCache;
    }

    /**
     * Set the read-only mode for all generated transactions.
     * 
     * @param allowWrite false if all transactions must be read-only
     */
    public void setAllowWrite(boolean allowWrite)
    {
    	sysAdminCache.put(KEY_SYSADMIN_ALLOW_WRITE, allowWrite);
    }
    
    public boolean isReadOnly()
    {
    	Boolean allowWrite = (Boolean)sysAdminCache.get(KEY_SYSADMIN_ALLOW_WRITE);
    	return (allowWrite == null ? false : ! allowWrite);
    }
    
    /**
     * @see RetryingTransactionHelper#setMaxRetries(int)
     */
    public void setMaxRetries(int maxRetries)
    {
        this.maxRetries = maxRetries;
    }

    /**
     * @see RetryingTransactionHelper#setMinRetryWaitMs(int)
     */
    public void setMinRetryWaitMs(int minRetryWaitMs)
    {
        this.minRetryWaitMs = minRetryWaitMs;
    }

    /**
     * @see RetryingTransactionHelper#setMaxRetryWaitMs(int)
     */
    public void setMaxRetryWaitMs(int maxRetryWaitMs)
    {
        this.maxRetryWaitMs = maxRetryWaitMs;
    }

    /**
     * @see RetryingTransactionHelper#setRetryWaitIncrementMs(int)
     */
    public void setRetryWaitIncrementMs(int retryWaitIncrementMs)
    {
        this.retryWaitIncrementMs = retryWaitIncrementMs;
    }

    /**
     * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_REQUIRED
     */
    public UserTransaction getUserTransaction()
    {
        SpringAwareUserTransaction txn = new SpringAwareUserTransaction(
                transactionManager,
                isReadOnly(),
                TransactionDefinition.ISOLATION_DEFAULT,
                TransactionDefinition.PROPAGATION_REQUIRED,
                TransactionDefinition.TIMEOUT_DEFAULT);
        return txn;
    }
    
    /**
     * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_REQUIRED
     */
    public UserTransaction getUserTransaction(boolean readOnly)
    {
        SpringAwareUserTransaction txn = new SpringAwareUserTransaction(
                transactionManager,
                (readOnly | isReadOnly()),
                TransactionDefinition.ISOLATION_DEFAULT,
                TransactionDefinition.PROPAGATION_REQUIRED,
                TransactionDefinition.TIMEOUT_DEFAULT);
        return txn;
    }

    /**
     * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_REQUIRES_NEW
     */
    public UserTransaction getNonPropagatingUserTransaction()
    {
        SpringAwareUserTransaction txn = new SpringAwareUserTransaction(
                transactionManager,
                isReadOnly(),
                TransactionDefinition.ISOLATION_DEFAULT,
                TransactionDefinition.PROPAGATION_REQUIRES_NEW,
                TransactionDefinition.TIMEOUT_DEFAULT);
        return txn;
    }

    /**
     * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_REQUIRES_NEW
     */
    public UserTransaction getNonPropagatingUserTransaction(boolean readOnly)
    {
        SpringAwareUserTransaction txn = new SpringAwareUserTransaction(
                transactionManager,
                (readOnly | isReadOnly()),
                TransactionDefinition.ISOLATION_DEFAULT,
                TransactionDefinition.PROPAGATION_REQUIRES_NEW,
                TransactionDefinition.TIMEOUT_DEFAULT);
        return txn;
    }

    /**
     * Creates a new helper instance.  It can be reused or customized by the client code:
     * each instance is new and initialized afresh.
     */
    public RetryingTransactionHelper getRetryingTransactionHelper()
    {
        RetryingTransactionHelper helper = new RetryingTransactionHelper();
        helper.setTransactionService(this);
        helper.setReadOnly(isReadOnly());
        if (maxRetries >= 0)
        {
            helper.setMaxRetries(maxRetries);
        }
        if (minRetryWaitMs > 0)
        {
            helper.setMinRetryWaitMs(minRetryWaitMs);
        }
        if (maxRetryWaitMs > 0)
        {
            helper.setMaxRetryWaitMs(maxRetryWaitMs);
        }
        if (retryWaitIncrementMs > 0)
        {
            helper.setRetryWaitIncrementMs(retryWaitIncrementMs);
        }
        return helper;
    }
}
