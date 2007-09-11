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
