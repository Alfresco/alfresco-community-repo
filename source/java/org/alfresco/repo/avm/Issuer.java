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
 * http://www.alfresco.com/legal/licensing" */

package org.alfresco.repo.avm;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
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
    public void initialize()
    {
        class TxnWork implements RetryingTransactionCallback<Long>
        {
            public Long execute() throws Exception
            {
                return AVMDAOs.Instance().fIssuerDAO.getIssuerValue(fName);
            }
        }
        Long result = fTransactionService.getRetryingTransactionHelper().doInTransaction(new TxnWork(), true);
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
