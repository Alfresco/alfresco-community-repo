/*
 * Copyright (C) 2006 Alfresco, Inc.
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

package org.alfresco.repo.avm;

import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.service.transaction.TransactionService;

/**
 * This is a helper class that knows how to issue identifiers.
 * @author britt
 */
public class Issuer
{
    /**
     * The next number to issue.
     */
    private long fNext;
    
    /**
     * The name of this issuer.
     */
    private String fName;
    
    /**
     * The transaction service.
     */
    private TransactionService fTransactionService;
    
    /**
     * Default constructor.
     */
    public Issuer()
    {
    }
    
    /**
     * Set the name of this issuer. For Spring.
     * @param name The name to set.
     */
    public void setName(String name)
    {
        fName = name;
    }
    
    public void setTransactionService(TransactionService transactionService)
    {
        fTransactionService = transactionService;
    }

    /**
     * After the database is up, get our value.
     */
    public void init()
    {
        class TxnWork implements TransactionUtil.TransactionWork<Long>
        {
            public Long doWork() throws Exception
            {
                return AVMDAOs.Instance().fIssuerDAO.getIssuerValue(fName);
            }
        }
        Long result = TransactionUtil.executeInUserTransaction(fTransactionService,
                                                               new TxnWork(),
                                                               true);
        if (result == null)
        {
            fNext = 0L;
        }
        else
        {
            fNext = result + 1L;
        }
    }
    
    /**
     * Issue the next number.
     * @return A serial number.
     */
    public synchronized long issue()
    {
        return fNext++;
    }
}
