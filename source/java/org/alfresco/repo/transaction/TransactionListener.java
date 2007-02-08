/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
     * Allows the listener to flush any consuming resources.  This mechanism is
     * used primarily during long-lived transactions to ensure that system resources
     * are not used up.
     * <p>
     * This method must not be used for implementing business logic.
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
     * Any exceptions generated here will cause the transaction to rollback.
     * <p>
     * All transaction resources are still available.
     */
    void beforeCompletion();
    
    /**
     * Invoked after transaction commit.
     * <p>
     * Any exceptions generated here will cause the transaction to rollback.
     * <p>
     * Although all transaction resources are still available, this method should
     * be used only for cleaning up resources after a commit has occured.
     */
    void afterCommit();

    /**
     * Invoked after transaction rollback.
     * <p>
     * Although all transaction resources are still available, this method should
     * be used only for cleaning up resources after a rollback has occured.
     */
    void afterRollback();
}
