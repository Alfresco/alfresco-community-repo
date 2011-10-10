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

/**
 * Listener for Alfresco-specific transaction callbacks.
 *
 * @see org.alfresco.repo.transaction.AlfrescoTransactionSupport
 * 
 * @author Derek Hulley
 */
public interface TransactionListener
{
    /**
     * @deprecated      No longer supported
     */
    void flush();
    
    /**
     * Called before a transaction is committed.
     * <p>
     * All transaction resources are still available.
     * 
     * @param readOnly true if the transaction is read-only
     */
    void beforeCommit(boolean readOnly);
    
    /**
     * Invoked before transaction commit/rollback.  Will be called after
     * {@link #beforeCommit(boolean) } even if {@link #beforeCommit(boolean)}
     * failed.
     * <p>
     * All transaction resources are still available.
     */
    void beforeCompletion();
    
    /**
     * Invoked after transaction commit.
     * <p>
     * Any exceptions generated here will only be logged and will have no effect
     * on the state of the transaction.
     * <p>
     * Although all transaction resources are still available, this method should
     * be used only for cleaning up resources after a commit has occured.
     */
    void afterCommit();

    /**
     * Invoked after transaction rollback.
     * <p>
     * Any exceptions generated here will only be logged and will have no effect
     * on the state of the transaction.
     * <p>
     * Although all transaction resources are still available, this method should
     * be used only for cleaning up resources after a rollback has occured.
     */
    void afterRollback();
}
