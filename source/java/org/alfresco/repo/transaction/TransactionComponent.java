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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.transaction;

import javax.transaction.UserTransaction;

import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.transaction.SpringAwareUserTransaction;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;

/**
 * Default implementation of Transaction Service
 * 
 * @author David Caruana
 */
public class TransactionComponent implements TransactionService
{
    private PlatformTransactionManager transactionManager;
    private boolean readOnly = false;
    
    /**
     * Set the transaction manager to use
     * 
     * @param transactionManager platform transaction manager
     */
    public void setTransactionManager(PlatformTransactionManager transactionManager)
    {
        this.transactionManager = transactionManager;
    }

    /**
     * Set the read-only mode for all generated transactions.
     * 
     * @param allowWrite false if all transactions must be read-only
     */
    public void setAllowWrite(boolean allowWrite)
    {
        this.readOnly = !allowWrite;
    }
    
    public boolean isReadOnly()
    {
        return readOnly;
    }

    /**
     * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_REQUIRED
     */
    public UserTransaction getUserTransaction()
    {
        SpringAwareUserTransaction txn = new SpringAwareUserTransaction(
                transactionManager,
                this.readOnly,
                TransactionDefinition.ISOLATION_DEFAULT,
                TransactionDefinition.PROPAGATION_REQUIRED,
                TransactionDefinition.TIMEOUT_DEFAULT);
        return txn;
    }
    
    /**
     * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_REQUIRED
     */
    public UserTransaction getUserTransaction(boolean readOnly)
    {
        SpringAwareUserTransaction txn = new SpringAwareUserTransaction(
                transactionManager,
                (readOnly | this.readOnly),
                TransactionDefinition.ISOLATION_DEFAULT,
                TransactionDefinition.PROPAGATION_REQUIRED,
                TransactionDefinition.TIMEOUT_DEFAULT);
        return txn;
    }

    /**
     * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_REQUIRES_NEW
     */
    public UserTransaction getNonPropagatingUserTransaction()
    {
        SpringAwareUserTransaction txn = new SpringAwareUserTransaction(
                transactionManager,
                this.readOnly,
                TransactionDefinition.ISOLATION_DEFAULT,
                TransactionDefinition.PROPAGATION_REQUIRES_NEW,
                TransactionDefinition.TIMEOUT_DEFAULT);
        return txn;
    }

    /**
     * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_REQUIRES_NEW
     */
    public UserTransaction getNonPropagatingUserTransaction(boolean readOnly)
    {
        SpringAwareUserTransaction txn = new SpringAwareUserTransaction(
                transactionManager,
                (readOnly | this.readOnly),
                TransactionDefinition.ISOLATION_DEFAULT,
                TransactionDefinition.PROPAGATION_REQUIRES_NEW,
                TransactionDefinition.TIMEOUT_DEFAULT);
        return txn;
    }
}
