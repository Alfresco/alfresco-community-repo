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
package org.alfresco.service.transaction;

import javax.transaction.UserTransaction;

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
     * Determine if ALL user transactions will be read-only.
     * 
     * @return Returns true if all transactions are read-only.
     */
    public boolean isReadOnly();
    
    /**
     * Gets a user transaction that supports transaction propagation.
     * This is like the EJB <b>REQUIRED</b> transaction attribute.
     * 
     * @return the user transaction
     */
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
    UserTransaction getNonPropagatingUserTransaction(boolean readOnly);
}
