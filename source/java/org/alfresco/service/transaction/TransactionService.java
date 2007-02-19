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
package org.alfresco.service.transaction;

import javax.transaction.UserTransaction;

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
@PublicService
public interface TransactionService
{
    /**
     * Determine if ALL user transactions will be read-only.
     * 
     * @return Returns true if all transactions are read-only.
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
     * @return Returns a non-gating user transaction
     */
    @NotAuditable
    UserTransaction getNonPropagatingUserTransaction(boolean readOnly);
}
