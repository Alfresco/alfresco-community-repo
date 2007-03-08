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
package org.alfresco.web.api.framework;

import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.web.api.framework.APIDescription.RequiredTransaction;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * API Service Transaction
 * 
 * @author davidc
 */
public class ServiceTransaction implements MethodInterceptor
{
    // Logger
    protected static final Log logger = LogFactory.getLog(ServiceTransaction.class);

    // dependencies
    private TransactionService transactionService;
    

    /**
     * Sets the transaction service
     * 
     * @param transactionService
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
    
    
    /* (non-Javadoc)
     * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    public Object invoke(final MethodInvocation invocation)
        throws Throwable
    {
        APIService service = (APIService)invocation.getThis();
        final APIDescription description = service.getDescription();

        // encapsulate service call within transaction
        TransactionUtil.TransactionWork<Object> work = new TransactionUtil.TransactionWork<Object>()
        {
            public Object doWork() throws Throwable
            {
                if (logger.isDebugEnabled())
                    logger.debug("Begin transaction: " + description.getRequiredTransaction());
                
                Object retVal = invocation.proceed();
                
                if (logger.isDebugEnabled())
                    logger.debug("End transaction: " + description.getRequiredTransaction());
                
                return retVal;
            }        
        };
        
        // execute call within transaction
        Object retVal;
        if (description.getRequiredTransaction() == RequiredTransaction.required)
        {
            retVal = TransactionUtil.executeInUserTransaction(transactionService, work); 
        }
        else
        {
            retVal = TransactionUtil.executeInNonPropagatingUserTransaction(transactionService, work); 
        }
        return retVal;
    }

}
