/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.service.transaction;

import javax.transaction.UserTransaction;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.NotAuditable;
import org.alfresco.service.PublicService;

/**
 * Contract for retrieving access to a user transaction.
 * <p>
 * Note that the implementation of the {@link javax.transaction.UserTransaction}
 * is not able to provide the full set of status codes available on the
 * {@link javax.transaction.Status} class.
 * 
 * @author David Caruana
 */
public interface TransactionService
{
    /**
     * Determine if the repository has been put into read only mode.
     * This is independent of the current user.
     * 
     * @return          <tt>true</tt> if the repository is allowed to perform
     *                  write operations
     */
    public boolean getAllowWrite();
    
    /**
     * Determine if ALL user transactions will be read-only.  The 'System'
     * user is always allowed to write.
     * 
     * @return Returns true if all transactions are read-only AND the current
     *         user is not the 'System' user.
     */
    @NotAuditable
    public boolean isReadOnly();
    
    /**
     * Gets a user transaction that supports transaction propagation.
     * This is like the EJB <b>REQUIRED</b> transaction attribute.
     * 
     * @return the user transaction
     */
    @NotAuditable
    UserTransaction getUserTransaction();
    
    /**
     * Gets a user transaction that supports transaction propagation.
     * This is like the EJB <b>REQUIRED</b> transaction attribute.
     * 
     * @param readOnly     Set true for a READONLY transaction instance, false otherwise.
     *      Note that it is not <i>always</i> possible to force a write transaction if the
     *      system is in read-only mode.
     * @return the user transaction
     */
    @NotAuditable
    UserTransaction getUserTransaction(boolean readOnly);
    
    /**
     * Gets a user transaction that supports transaction propagation.
     * This is like the EJB <b>REQUIRED</b> transaction attribute.
     * 
     * @param readOnly     Set true for a READONLY transaction instance, false otherwise.
     * @param ignoreSystemReadOnly     <tt>true</tt> to force the read-only flag to be respected regardless
     *                  of the system read-only mode.
     * @return the user transaction
     */
    @NotAuditable
    UserTransaction getUserTransaction(boolean readOnly, boolean ignoreSystemReadOnly);
    
    /**
     * Gets a user transaction that ensures a new transaction is created.
     * Any enclosing transaction is not propagated.
     * This is like the EJB <b>REQUIRES_NEW</b> transaction attribute -
     * when the transaction is started, the current transaction will be
     * suspended and a new one started.
     * 
     * @return Returns a non-propagating user transaction
     */
    @NotAuditable
    UserTransaction getNonPropagatingUserTransaction();
    
    /**
     * Gets a user transaction that ensures a new transaction is created.
     * Any enclosing transaction is not propagated.
     * This is like the EJB <b>REQUIRES_NEW</b> transaction attribute -
     * when the transaction is started, the current transaction will be
     * suspended and a new one started.
     * 
     * @param readOnly Set true for a READONLY transaction instance, false otherwise.
     *      Note that it is not <i>always</i> possible to force a write transaction if the
     *      system is in read-only mode.
     * @return Returns a non-propagating user transaction
     */
    @NotAuditable
    UserTransaction getNonPropagatingUserTransaction(boolean readOnly);
    
    /**
     * Gets a user transaction that ensures a new transaction is created.
     * Any enclosing transaction is not propagated.
     * This is like the EJB <b>REQUIRES_NEW</b> transaction attribute -
     * when the transaction is started, the current transaction will be
     * suspended and a new one started.
     * 
     * @param readOnly  Set true for a READONLY transaction instance, false otherwise.
     * @param ignoreSystemReadOnly     <tt>true</tt> to force the read-only flag to be respected regardless
     *                  of the system read-only mode.
     * @return Returns a non-propagating user transaction
     */
    @NotAuditable
    UserTransaction getNonPropagatingUserTransaction(boolean readOnly, boolean ignoreSystemReadOnly);
    
    /**
     * Get the standard instance of the helper object that supports transaction retrying.
     * 
     * @return
     *      Returns a helper object that executes units of work transactionally.  The helper
     *      can be reused or altered as required.
     */
    @NotAuditable
    RetryingTransactionHelper getRetryingTransactionHelper();
}
