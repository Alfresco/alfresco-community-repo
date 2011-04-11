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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import javax.transaction.UserTransaction;

import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.VmShutdownListener;
import org.alfresco.util.transaction.SpringAwareUserTransaction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    private static VmShutdownListener shutdownListener = new VmShutdownListener("TransactionService");

    private PlatformTransactionManager transactionManager;
    private AuthenticationContext authenticationContext;
    private int maxRetries = -1;
    private int minRetryWaitMs = -1;
    private int maxRetryWaitMs = -1;
    private int retryWaitIncrementMs = -1;
    
    private static final Log logger = LogFactory.getLog(TransactionServiceImpl.class);

    // SysAdmin cache - used to cluster certain configuration parameters
    private SysAdminParams sysAdminParams;
    
    // Veto for allow write
    private Set<QName> writeVeto = new HashSet<QName>();
    private final QName generalVetoName = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "GeneralVeto");
    
    private ReadLock vetoReadLock;
    private WriteLock vetoWriteLock;
    
    /**
     * Construct defaults
     */
    public TransactionServiceImpl()
    {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        vetoReadLock = lock.readLock();
        vetoWriteLock = lock.writeLock();
    }

    /**
     * Set the transaction manager to use
     */
    public void setTransactionManager(PlatformTransactionManager transactionManager)
    {
        this.transactionManager = transactionManager;
    }

    /**
     * Sets the authentication context.
     * 
     * @param authenticationContext
     *            the authentication context
     */
    public void setAuthenticationContext(AuthenticationContext authenticationContext)
    {
        this.authenticationContext = authenticationContext;
    }

    public void setSysAdminParams(SysAdminParams sysAdminParams)
    {
        this.sysAdminParams = sysAdminParams;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getAllowWrite()
    {
        vetoReadLock.lock();
        try
        {
            return writeVeto.isEmpty();
        }
        finally
        {
            vetoReadLock.unlock();
        }
    }
    
    /**
     * Set the read-only mode for all generated transactions.
     * <p>
     * Intended for use by spring configuration only.   Alfresco code should call the method which 
     * specifies a veto name.
     * 
     * @param allowWrite        false if all transactions must be read-only
     */
    public void setAllowWrite(boolean allowWrite)
    {
        setAllowWrite(allowWrite, generalVetoName);
    }
    
    /**
     * Set the read-only mode for all generated transactions.
     * <p>
     * By default read/write transactions are allowed however vetos may be applied that make the 
     * transactions read only.   
     * <p>
     * Prevent writes by calling allowWrite with false and a given veto name.
     * <p>
     * The veto is removed by calling allowWrite with true for the given veto name
     * when all vetos are removed then read/write transactions are allowed.
     * 
     * @param allowWrite
     *            false if all transactions must be read-only
     * @param nameOfVeto
     *             the name of the veto           
     */
    public void setAllowWrite(boolean allowWrite, QName nameOfVeto)
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("setAllowWrite:" + allowWrite + ", name of veto:" + nameOfVeto);
        }
        vetoWriteLock.lock();
        try
        {
            if(allowWrite)
            {
                writeVeto.remove(nameOfVeto);
            }
            else
            {
                writeVeto.add(nameOfVeto);
            }
        }
        finally
        {
            vetoWriteLock.unlock();
        }
    }   

    /**
     * {@inheritDoc}
     */
    public boolean isReadOnly()
    {
        if (shutdownListener.isVmShuttingDown())
        {
            return true;
        }
        vetoReadLock.lock();
        try
        {
            if (this.authenticationContext.isCurrentUserTheSystemUser())
            {
                return false;
            }
            else
            {
                return !writeVeto.isEmpty() || !this.sysAdminParams.getAllowWrite();
            }
        }
        finally
        {
            vetoReadLock.unlock();
        }
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
        return getUserTransaction(false, false);
    }

    /**
     * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_REQUIRED
     */
    public UserTransaction getUserTransaction(boolean readOnly)
    {
        return getUserTransaction(readOnly, false);
    }

    /**
     * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_REQUIRED
     */
    public UserTransaction getUserTransaction(boolean readOnly, boolean ignoreSystemReadOnly)
    {
        if (ignoreSystemReadOnly)
        {
            SpringAwareUserTransaction txn = new SpringAwareUserTransaction(
                    transactionManager,
                    (readOnly),
                    TransactionDefinition.ISOLATION_DEFAULT, TransactionDefinition.PROPAGATION_REQUIRED,
                    TransactionDefinition.TIMEOUT_DEFAULT);
            return txn;
        }
        else
        {
            SpringAwareUserTransaction txn = new SpringAwareUserTransaction(
                    transactionManager,
                    (readOnly | isReadOnly()),
                    TransactionDefinition.ISOLATION_DEFAULT, TransactionDefinition.PROPAGATION_REQUIRED,
                    TransactionDefinition.TIMEOUT_DEFAULT);
            return txn;
        }
    }

    /**
     * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_REQUIRES_NEW
     */
    public UserTransaction getNonPropagatingUserTransaction()
    {
        return getNonPropagatingUserTransaction(false, false);
    }

    /**
     * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_REQUIRES_NEW
     */
    public UserTransaction getNonPropagatingUserTransaction(boolean readOnly)
    {
        return getNonPropagatingUserTransaction(readOnly, false);
    }

    /**
     * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_REQUIRES_NEW
     */
    @Override
    public UserTransaction getNonPropagatingUserTransaction(boolean readOnly, boolean ignoreSystemReadOnly)
    {
        if (ignoreSystemReadOnly)
        {
            SpringAwareUserTransaction txn = new SpringAwareUserTransaction(
                    transactionManager,
                    (readOnly),
                    TransactionDefinition.ISOLATION_DEFAULT, TransactionDefinition.PROPAGATION_REQUIRES_NEW,
                    TransactionDefinition.TIMEOUT_DEFAULT);
            return txn;
        }
        else
        {
            SpringAwareUserTransaction txn = new SpringAwareUserTransaction(
                    transactionManager,
                    (readOnly | isReadOnly()),
                    TransactionDefinition.ISOLATION_DEFAULT, TransactionDefinition.PROPAGATION_REQUIRES_NEW,
                    TransactionDefinition.TIMEOUT_DEFAULT);
            return txn;
        }
    }

    /**
     * Creates a new helper instance. It can be reused or customized by the client code: each instance is new and
     * initialized afresh.
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
