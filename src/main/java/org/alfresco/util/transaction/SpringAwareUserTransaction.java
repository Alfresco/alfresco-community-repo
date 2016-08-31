/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.util.transaction;

import java.lang.reflect.Method;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.alfresco.error.StackTraceUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * A <code>UserTransaction</code> that will allow the thread using it to participate
 * in transactions that are normally only begun and committed by the <b>SpringFramework</b>
 * transaction aware components.
 * <p>
 * Client code can use this class directly, but should be very careful to handle the exception
 * conditions with the appropriate <code>finally</code> blocks and <code>rollback</code> code.
 * It is recommended that clients use this class indirectly via an instance of the
 * {@link org.alfresco.repo.transaction.RetryingTransactionHelper}.
 * <p>
 * This class is thread-safe in that it will detect multithreaded access and throw
 * exceptions.  Therefore </b>do not use on multiple threads</b>.  Instances should be
 * used only for the duration of the required user transaction and then discarded.
 * Any attempt to reuse an instance will result in failure.
 * <p>
 * Nested user transaction are allowed.
 * <p>
 * <b>Logging:</b><br/>
 * To dump exceptions during commits, turn debugging on for this class.<br/>
 * To log leaked transactions i.e. a begin() is not matched by a commit() or rollback(),
 * add <i>.trace</i> to the usual classname-based debug category and set to WARN log
 * level. This will log the first detection of a leaked transaction and automatically enable
 * transaction call stack logging for subsequent leaked transactions.  To enforce
 * call stack logging from the start set the <i>.trace</i> log level to DEBUG. Call stack
 * logging will hamper performance but is useful when it appears that something is eating
 * connections or holding onto resources - usually a sign that client code hasn't handled all
 * possible exception conditions.
 * 
 * @see org.springframework.transaction.PlatformTransactionManager
 * @see org.springframework.transaction.support.DefaultTransactionDefinition
 * 
 * @author Derek Hulley
 */
public class SpringAwareUserTransaction
        extends TransactionAspectSupport
        implements UserTransaction, TransactionAttributeSource, TransactionAttribute
{
    /*
     * There is some extra work in here to perform safety checks against the thread ID.
     * This is because this class doesn't operate in an environment that guarantees that the
     * thread coming into the begin() method is the same as the thread forcing commit() or
     * rollback().
     */
        
    private static final long serialVersionUID = 3762538897183224373L;


    private static final String NAME = "UserTransaction";
    
    private static final Log logger = LogFactory.getLog(SpringAwareUserTransaction.class);
    
    
    /*
     * Leaked Transaction Logging
     */
    private static final Log traceLogger = LogFactory.getLog(SpringAwareUserTransaction.class.getName() + ".trace");
    private static volatile boolean isCallStackTraced = false;
    
    static
    {
        if (traceLogger.isDebugEnabled())
        {
            isCallStackTraced = true;
            traceLogger.warn("Logging of transaction call stack is enforced and will affect performance");
        }
    }
    
    
    static boolean isCallStackTraced()
    {
        return isCallStackTraced;
    }
    
    /** stores whether begin() & commit()/rollback() methods calls are balanced */ 
    private boolean isBeginMatched = true;
    /** stores the begin() call stack when auto tracing */
    private StackTraceElement[] beginCallStack;

    
    private boolean readOnly;
    private int isolationLevel;
    private int propagationBehaviour;
    private int timeout;
    
    /** Stores the user transaction current status as affected by explicit operations */
    private int internalStatus = Status.STATUS_NO_TRANSACTION;
    /** the transaction information used to check for mismatched begin/end */
    private TransactionInfo internalTxnInfo;
    /** keep the thread that the transaction was started on to perform thread safety checks */
    private long threadId = Long.MIN_VALUE;
    /** make sure that we clean up the thread transaction stack properly */
    private boolean finalized = false;
    
    /**
     * Creates a user transaction that defaults to {@link TransactionDefinition#PROPAGATION_REQUIRED}.
     * 
     * @param transactionManager the transaction manager to use
     * @param readOnly true to force a read-only transaction
     * @param isolationLevel one of the
     *      {@link TransactionDefinition#ISOLATION_DEFAULT TransactionDefinition.ISOLATION_XXX}
     *      constants
     * @param propagationBehaviour one of the
     *      {@link TransactionDefinition#PROPAGATION_MANDATORY TransactionDefinition.PROPAGATION__XXX}
     *      constants
     * @param timeout the transaction timeout in seconds.
     * 
     * @see TransactionDefinition#getTimeout()
     */
    public SpringAwareUserTransaction(
            PlatformTransactionManager transactionManager,
            boolean readOnly,
            int isolationLevel,
            int propagationBehaviour,
            int timeout)
    {
        super();
        setTransactionManager(transactionManager);
        setTransactionAttributeSource(this);
        this.readOnly = readOnly;
        this.isolationLevel = isolationLevel;
        this.propagationBehaviour = propagationBehaviour;
        this.timeout = timeout;
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(256);
        sb.append("UserTransaction")
          .append("[object=").append(super.toString())
          .append(", status=").append(internalStatus)
          .append("]");
        return sb.toString();
    }

    /**
     * This class carries all the information required to fullfil requests about the transaction
     * attributes.  It acts as a source of the transaction attributes.
     * 
     * @return Return <code>this</code> instance
     */
    public TransactionAttribute getTransactionAttribute(Method method, Class<?> targetClass)
    {
        return this;
    }        
    
    /**
     * Return a qualifier value associated with this transaction attribute. This is not used as the transaction manager
     * has been selected for us.
     * 
     * @return null always
     */
    public String getQualifier()
    {
        return null;
    }

    /**
     * The {@link UserTransaction } must rollback regardless of the error.  The
     * {@link #rollback() rollback} behaviour is implemented by simulating a caught
     * exception.  As this method will always return <code>true</code>, the rollback
     * behaviour will be to rollback the transaction or mark it for rollback.
     * 
     * @return Returns true always
     */
    public boolean rollbackOn(Throwable ex)
    {
        return true;
    }
    
    /**
     * @see #NAME
     */
    public String getName()
    {
        return NAME;
    }

    public boolean isReadOnly()
    {
        return readOnly;
    }

    public int getIsolationLevel()
    {
        return isolationLevel;
    }

    public int getPropagationBehavior()
    {
        return propagationBehaviour;
    }

    public int getTimeout()
    {
        return timeout;
    }

    /**
     * Implementation required for {@link UserTransaction}.
     */
    public void setTransactionTimeout(int timeout) throws SystemException
    {
        if (internalStatus != Status.STATUS_NO_TRANSACTION)
        {
            throw new RuntimeException("Can only set the timeout before begin");
        }
        this.timeout = timeout;
    }

    /**
     * Gets the current transaction info, or null if none exists.
     * <p>
     * A check is done to ensure that the transaction info on the stack is exactly
     * the same instance used when this transaction was started.
     * The internal status is also checked against the transaction info.
     * These checks ensure that the transaction demarcation is done correctly and that
     * thread safety is adhered to.
     * 
     * @return Returns the current transaction
     */
    private TransactionInfo getTransactionInfo()
    {
        // a few quick self-checks
        if (threadId < 0 && internalStatus != Status.STATUS_NO_TRANSACTION)
        {
            throw new RuntimeException("Transaction has been started but there is no thread ID");
        }
        else if (threadId >= 0 && internalStatus == Status.STATUS_NO_TRANSACTION)
        {
            throw new RuntimeException("Transaction has not been started but a thread ID has been recorded");
        }
        
        TransactionInfo txnInfo = null;
        try
        {
            txnInfo = TransactionAspectSupport.currentTransactionInfo();
            // we are in a transaction
        }
        catch (NoTransactionException e)
        {
            // No transaction.  It is possible that the transaction threw an exception during commit.
        }
        // perform checks for active transactions
        if (internalStatus == Status.STATUS_ACTIVE)
        {
            if (Thread.currentThread().getId() != threadId)
            {
                // the internally stored transaction info (retrieved in begin()) should match the info
                // on the thread
                throw new RuntimeException("UserTransaction may not be accessed by multiple threads");
            }
            else if (txnInfo == null)
            {
                // internally we recorded a transaction starting, but there is nothing on the thread
                throw new RuntimeException("Transaction boundaries have been made to overlap in the stack");
            }
            else if (txnInfo != internalTxnInfo)
            {
                // the transaction info on the stack isn't the one we started with
                throw new RuntimeException("UserTransaction begin/commit mismatch");
            }
        }
        return txnInfo;
    }

    /**
     * This status is a combination of the internal status, as recorded during explicit operations,
     * and the status provided by the Spring support.
     * 
     * @see Status
     */
    public synchronized int getStatus() throws SystemException
    {
        TransactionInfo txnInfo = getTransactionInfo();
        
        // if the txn info is null, then we are outside a transaction
        if (txnInfo == null)
        {
            return internalStatus;      // this is checked in getTransactionInfo
        }

        // normally the internal status is correct, but we only need to double check
        // for the case where the transaction was marked for rollback, or rolledback
        // in a deeper transaction
        TransactionStatus txnStatus = txnInfo.getTransactionStatus();
        if (internalStatus == Status.STATUS_ROLLEDBACK)
        {
            // explicitly rolled back at some point
            return internalStatus;
        }
        else if (txnStatus.isRollbackOnly())
        {
            // marked for rollback at some point in the stack
            return Status.STATUS_MARKED_ROLLBACK;
        }
        else
        {
            // just rely on the internal status
            return internalStatus;
        }
    }

    public synchronized void setRollbackOnly() throws IllegalStateException, SystemException
    {
        // just a check
        TransactionInfo txnInfo = getTransactionInfo();

        int status = getStatus();
        // check the status
        if (status == Status.STATUS_MARKED_ROLLBACK)
        {
            // this is acceptable
        }
        else if (status == Status.STATUS_NO_TRANSACTION)
        {
            throw new IllegalStateException("The transaction has not been started yet");
        }
        else if (status == Status.STATUS_ROLLING_BACK || status == Status.STATUS_ROLLEDBACK)
        {
            throw new IllegalStateException("The transaction has already been rolled back");
        }
        else if (status == Status.STATUS_COMMITTING || status == Status.STATUS_COMMITTED)
        {
            throw new IllegalStateException("The transaction has already been committed");
        }
        else if (status != Status.STATUS_ACTIVE)
        {
            throw new IllegalStateException("The transaction is not active: " + status);
        }

        // mark for rollback
        txnInfo.getTransactionStatus().setRollbackOnly();
        // make sure that we record the fact that we have been marked for rollback
        internalStatus = Status.STATUS_MARKED_ROLLBACK;
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Set transaction status to rollback only: " + this);
        }
    }
    
    /**
     * @throws NotSupportedException if an attempt is made to reuse this instance
     */
    public synchronized void begin() throws NotSupportedException, SystemException
    {
        // make sure that the status and info align - the result may or may not be null
        @SuppressWarnings("unused")
        TransactionInfo txnInfo = getTransactionInfo();
        if (internalStatus != Status.STATUS_NO_TRANSACTION)
        {
            throw new NotSupportedException("The UserTransaction may not be reused");
        }
        
        // check 
        
        if( (propagationBehaviour != TransactionDefinition.PROPAGATION_REQUIRES_NEW))
        {
            if(!readOnly && 
                    TransactionSynchronizationManager.isSynchronizationActive() &&  
                    TransactionSynchronizationManager.isCurrentTransactionReadOnly()
            )
            {
                throw new IllegalStateException("Nested writable transaction in a read only transaction");
            }
        }
        
        // begin a transaction
        try
        {
            internalTxnInfo = createTransactionIfNecessary(
                (Method) null,
                (Class<?>) null);  // super class will just pass nulls back to us
        }
        catch (CannotCreateTransactionException e)
        {
            throw new ConnectionPoolException("The DB connection pool is depleted.", e);
        }
        
        internalStatus = Status.STATUS_ACTIVE;
        threadId = Thread.currentThread().getId();
        
        // Record that transaction details now that begin was successful
        isBeginMatched = false;
        if (isCallStackTraced)
        {
            // get the stack trace
            Exception e = new Exception();
            e.fillInStackTrace();
            beginCallStack = e.getStackTrace();
        }

        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Began user transaction: " + this);
        }
    }
    
    /**
     * @throws IllegalStateException if a transaction was not started
     */
    public synchronized void commit()
            throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
            SecurityException, IllegalStateException, SystemException
    {
        // perform checks
        TransactionInfo txnInfo = getTransactionInfo();

        int status = getStatus();
        // check the status
        if (status == Status.STATUS_NO_TRANSACTION)
        {
            throw new IllegalStateException("The transaction has not yet begun");
        }
        else if (status == Status.STATUS_ROLLING_BACK || status == Status.STATUS_ROLLEDBACK)
        {
            throw new RollbackException("The transaction has already been rolled back");
        }
        else if (status == Status.STATUS_MARKED_ROLLBACK)
        {
            throw new RollbackException("The transaction has already been marked for rollback");
        }
        else if (status == Status.STATUS_COMMITTING || status == Status.STATUS_COMMITTED)
        {
            throw new IllegalStateException("The transaction has already been committed");
        }
        else if (status != Status.STATUS_ACTIVE || txnInfo == null)
        {
            throw new IllegalStateException("No user transaction is active");
        }
            
        if (!finalized)
        {
            try
            {
                // the status seems correct - we can try a commit
                commitTransactionAfterReturning(txnInfo);
            }
            catch (Throwable e)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Transaction didn't commit", e);
                }
                // commit failed
                internalStatus = Status.STATUS_ROLLEDBACK;
                RollbackException re = new RollbackException("Transaction didn't commit: " + e.getMessage());
                // Stick the originating reason for failure into the exception.
                re.initCause(e);
                throw re;
            }
            finally
            {
                // make sure that we clean up the stack
                cleanupTransactionInfo(txnInfo);
                finalized = true;
                // clean up leaked transaction logging
                isBeginMatched = true;
                beginCallStack = null;
            }
        }
        
        // regardless of whether the transaction was finally committed or not, the status
        // as far as UserTransaction is concerned should be 'committed'
        
        // keep track that this UserTransaction was explicitly committed
        internalStatus = Status.STATUS_COMMITTED;
        
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Committed user transaction: " + this);
        }
    }

    public synchronized void rollback()
            throws IllegalStateException, SecurityException, SystemException
    {
        // perform checks
        TransactionInfo txnInfo = getTransactionInfo();
        
        int status = getStatus();
        // check the status
        if (status == Status.STATUS_ROLLING_BACK || status == Status.STATUS_ROLLEDBACK)
        {
            throw new IllegalStateException("The transaction has already been rolled back");
        }
        else if (status == Status.STATUS_COMMITTING || status == Status.STATUS_COMMITTED)
        {
            throw new IllegalStateException("The transaction has already been committed");
        }
        else if (txnInfo == null)
        {
            throw new IllegalStateException("No user transaction is active");
        }
    
        if (!finalized)
        {
            try
            {
                // force a rollback by generating an exception that will trigger a rollback
                completeTransactionAfterThrowing(txnInfo, new Exception());
            }
            finally
            {
                // make sure that we clean up the stack
                cleanupTransactionInfo(txnInfo);
                finalized = true;
                // clean up leaked transaction logging
                isBeginMatched = true;
                beginCallStack = null;
            }
        }

        // the internal status notes that we were specifically rolled back 
        internalStatus = Status.STATUS_ROLLEDBACK;
        
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Rolled back user transaction: " + this);
        }
    }
    
    @Override
    protected void completeTransactionAfterThrowing(TransactionInfo txInfo, Throwable ex)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Exception attempting to pass transaction boundaries.", ex);
        }
        super.completeTransactionAfterThrowing(txInfo, ex);
    }

    @Override
    protected String methodIdentification(Method method)
    {
        // note: override for debugging purposes - this method called by Spring
        return NAME;
    }
    
    @Override
    protected void finalize() throws Throwable
    {
        if (!isBeginMatched)
        {
            if (isCallStackTraced)
            {
                if (beginCallStack == null)
                {
                    traceLogger.error("UserTransaction being garbage collected without a commit() or rollback(). " + 
                                      "NOTE: Prior to transaction call stack logging.");
                }
                else
                {
                    StringBuilder sb = new StringBuilder(1024);
                    StackTraceUtil.buildStackTrace(
                            "UserTransaction being garbage collected without a commit() or rollback().",
                            beginCallStack,
                            sb,
                            -1);
                    traceLogger.error(sb);
                }
            }
            else
            {
                traceLogger.error("Detected first UserTransaction which is being garbage collected without a commit() or rollback()");
                traceLogger.error("Logging of transaction call stack is now enabled and will affect performance");
                isCallStackTraced = true;
            }
        }
    }
}
