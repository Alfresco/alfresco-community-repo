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
package org.alfresco.repo.transaction;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.extensions.surf.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class containing transactions helper methods and interfaces.
 * 
 * @deprecated  Use a {@link RetryingTransactionHelper} instance
 * 
 * @author Derek Hulley
 */
public class TransactionUtil
{
    private static Log logger = LogFactory.getLog(TransactionUtil.class);

    /**
     * Transaction work interface.
     * <p>
     * This interface encapsulates a unit of work that should be done within a
     * transaction.
     * 
     * @deprecated
     * @see RetryingTransactionHelper.RetryingTransactionCallback
     */
    public interface TransactionWork<Result>
    {
        /**
         * Method containing the work to be done in the user transaction.
         * 
         * @return Return the result of the operation
         */
        Result doWork() throws Throwable;
    }

    /**
     * Flush transaction.
     */
    public static void flush()
    {
        AlfrescoTransactionSupport.flush();
    }
    
    /**
     * Execute the transaction work in a user transaction
     * 
     * @param transactionService the transaction service
     * @param transactionWork the transaction work
     * 
     * @throws java.lang.RuntimeException if the transaction was rolled back
     * 
     * @deprecated  Use a {@link RetryingTransactionHelper} instance
     */
    public static <R> R executeInUserTransaction(
            TransactionService transactionService,
            TransactionWork<R> transactionWork)
    {
        return executeInTransaction(transactionService, transactionWork, false, false);
    }

    /**
     * Execute the transaction work in a user transaction.
     * Any current transaction will be continued.
     * 
     * @param transactionService the transaction service
     * @param transactionWork the transaction work
     * @param readOnly true if the transaction should be read-only
     * 
     * @throws java.lang.RuntimeException if the transaction was rolled back
     * 
     * @deprecated  Use a {@link RetryingTransactionHelper} instance
     */
    public static <R> R executeInUserTransaction(
            TransactionService transactionService,
            TransactionWork<R> transactionWork,
            boolean readOnly)
    {
        return executeInTransaction(transactionService, transactionWork, false, readOnly);
    }

    /**
     * Execute the transaction work in a <b>writable</b>, non-propagating user transaction.
     * Any current transaction will be suspended a new one started.
     * 
     * @param transactionService the transaction service
     * @param transactionWork the transaction work
     * 
     * @throws java.lang.RuntimeException if the transaction was rolled back
     * 
     * @deprecated  Use a {@link RetryingTransactionHelper} instance
     */
    public static <R> R executeInNonPropagatingUserTransaction(
            TransactionService transactionService,
            TransactionWork<R> transactionWork)
    {
        return executeInTransaction(transactionService, transactionWork, true, false);
    }

    /**
     * Execute the transaction work in a non-propagating user transaction.
     * Any current transaction will be suspended a new one started.
     * 
     * @param transactionService the transaction service
     * @param transactionWork the transaction work
     * @param readOnly true if the transaction should be read-only
     * 
     * @throws java.lang.RuntimeException if the transaction was rolled back
     * 
     * @deprecated  Use a {@link RetryingTransactionHelper} instance
     */
    public static <R> R executeInNonPropagatingUserTransaction(
            TransactionService transactionService,
            TransactionWork<R> transactionWork,
            boolean readOnly)
    {
        return executeInTransaction(transactionService, transactionWork, true, readOnly);
    }

    /**
     * Execute the transaction work in a user transaction of a specified type
     * 
     * @param transactionService the transaction service
     * @param transactionWork the transaction work
     * @param nonPropagatingUserTransaction indicates whether the transaction
     *        should be non propigating or not
     * @param readOnly true if the transaction should be read-only
     * 
     * @throws java.lang.RuntimeException if the transaction was rolled back
     */
    private static <R> R executeInTransaction(
            TransactionService transactionService,
            TransactionWork<R> transactionWork,
            boolean nonPropagatingUserTransaction,
            boolean readOnly)
    {
        ParameterCheck.mandatory("transactionWork", transactionWork);

        R result = null;

        // Get the right type of user transaction
        UserTransaction txn = null;
        if (nonPropagatingUserTransaction == true)
        {
            txn = transactionService.getNonPropagatingUserTransaction();
        }
        else
        {
            txn = transactionService.getUserTransaction(readOnly);
        }

        try
        {
            // Begin the transaction, do the work and then commit the
            // transaction
            txn.begin();
            result = transactionWork.doWork();
            // rollback or commit
            if (txn.getStatus() == Status.STATUS_MARKED_ROLLBACK)
            {
                // something caused the transaction to be marked for rollback
                txn.rollback();
            }
            else
            {
                // transaction should still commit
                txn.commit();
            }
        }
        catch (RollbackException exception)
        {
            // commit failed
            throw new AlfrescoRuntimeException(
                    "Unexpected rollback exception: \n" + exception.getMessage(),
                    exception);
        }
        catch (Throwable exception)
        {
            try
            {
                // Roll back the exception
                txn.rollback();
            }
            catch (Throwable rollbackException)
            {
                // just dump the exception - we are already in a failure state
                logger.error("Error rolling back transaction", rollbackException);
            }

            // Re-throw the exception
            if (exception instanceof RuntimeException)
            {
                throw (RuntimeException) exception;
            }
            else
            {
                throw new RuntimeException("Error during execution of transaction.", exception);
            }
        }

        return result;
    }
}