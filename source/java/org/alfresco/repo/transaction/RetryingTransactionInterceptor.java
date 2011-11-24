/*
* Copyright (C) 2005-2010 Alfresco Software Limited.
*
* This file is part of Alfresco
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
*/
package org.alfresco.repo.transaction;

import java.lang.reflect.Method;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.transaction.TransactionService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.interceptor.TransactionAttribute;

/**
 * @author Dmitry Velichkevich
 */
public class RetryingTransactionInterceptor extends TransactionAspectSupport implements MethodInterceptor
{
    private TransactionService transactionService;

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public Object invoke(final MethodInvocation target) throws Throwable
    {
        if ((null != target) && (null != target.getThis()) && (null != target.getMethod()))
        {
            final Method method = target.getMethod();
            final TransactionAttribute txnAttr = getTransactionAttributeSource().getTransactionAttribute(method, target.getThis().getClass());
            if (null != txnAttr && txnAttr.getPropagationBehavior() != TransactionAttribute.PROPAGATION_SUPPORTS)
            {
                RetryingTransactionCallback<Object> txnCallback = new RetryingTransactionCallback<Object>()
                {
                    @Override
                    public Object execute() throws Throwable
                    {
                        return target.proceed();
                    }
                };
                return transactionService.getRetryingTransactionHelper().doInTransaction(
                        txnCallback,
                        txnAttr.isReadOnly(),
                        (TransactionAttribute.PROPAGATION_REQUIRES_NEW == txnAttr.getPropagationBehavior()));
            }
            else
            {
                return target.proceed();
            }
        }
        throw new AlfrescoRuntimeException("Invalid or undefined MethodInvocation instance: " + target.getMethod());
    }
}
