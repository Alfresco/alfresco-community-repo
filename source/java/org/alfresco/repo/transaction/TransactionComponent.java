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
