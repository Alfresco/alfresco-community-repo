/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.repo.domain.control;

import java.sql.Savepoint;

/**
 * DAO services for database control statements.  It is sometimes necessary to
 * issue control statements on a database connection; these are not usually
 * supported in the ANSI SQL standard.
 * 
 * @author Derek Hulley
 * @since 3.2SP1
 */
public interface ControlDAO
{
    /**
     * Begin batching prepared statements for later execution.
     * 
     * @see #executeBatch()
     */
    public void startBatch();
    
    /**
     * Execute statements that were queued for batching.
     * 
     * @see #startBatch()
     */
    public void executeBatch();
    
    /**
     * Create a "Save Point" in the current transaction, for later selective rollback.
     * Creation should be accompanied by a matching {@link #rollbackToSavepoint(String)}
     * or {@link #releaseSavepoint(String)} using the same name.
     * 
     * @param savepoint             the name of the save point
     * @return                      Returns the handle to the savepoint or <tt>null</tt> if the
     *                              implementation does not support it
     */
    public Savepoint createSavepoint(String savepoint);

    /**
     * Roll back to a previously-created "Save Point", discarding any intervening
     * changes to the current transaction.
     * 
     * @param savepoint             a previously-created savepoint
     * 
     * @see #createSavepoint(String)
     */
    public void rollbackToSavepoint(Savepoint savepoint);

    /**
     * Remove a previously-created "Save Point", writing any intervening updates
     * into the current transaction.
     * 
     * @param savepoint             the name of the save point
     * 
     * @see #createSavepoint(String)
     */
    public void releaseSavepoint(Savepoint savepoint);
}
