/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.repo.transaction;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * A this advice wrapper around the {@link RetryingTransactionHelper}.
 *
 * @author Derek Hulley
 */
public class RetryingTransactionAdvice implements MethodInterceptor
{
    private RetryingTransactionHelper txnHelper;
    private boolean readOnly;
    private boolean requiresNew;

    public RetryingTransactionAdvice()
    {
        readOnly = false;
        requiresNew = false;
    }

    public void setTxnHelper(RetryingTransactionHelper txnHelper)
    {
        this.txnHelper = txnHelper;
    }

    public void setReadOnly(boolean readOnly)
    {
        this.readOnly = readOnly;
    }

    public void setRequiresNew(boolean requiresNew)
    {
        this.requiresNew = requiresNew;
    }

    public Object invoke(final MethodInvocation methodInvocation) throws Throwable
    {
        // Just call the helper
        RetryingTransactionCallback<Object> txnCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                return methodInvocation.proceed();
            }
        };
        return txnHelper.doInTransaction(txnCallback, readOnly, requiresNew);
    }
}
