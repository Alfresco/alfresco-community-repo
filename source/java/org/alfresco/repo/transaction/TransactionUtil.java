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

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class containing transactions helper methods and interfaces.
 * 
 * @author Roy Wetherall
 */
public class TransactionUtil
{
    private static Log logger = LogFactory.getLog(TransactionUtil.class);

    /**
     * Transaction work interface.
     * <p>
     * This interface encapsulates a unit of work that should be done within a
     * transaction.
     */
    public interface TransactionWork<Result>
    {
        /**
         * Method containing the work to be done in the user transaction.
         * 
         * @return Return the result of the operation
         */
        Result doWork() throws Exception;
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
     * @param ignoreException indicates whether errors raised in the work are
     *        ignored or re-thrown
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
                    "Unexpected rollback of exception: \n" + exception.getMessage(),
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