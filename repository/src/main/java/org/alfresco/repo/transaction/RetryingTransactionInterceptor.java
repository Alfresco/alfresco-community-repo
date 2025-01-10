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

import java.lang.reflect.Method;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.transaction.TransactionService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.interceptor.TransactionAttribute;

/**
 * @author Dmitry Velichkevich
 */
public class RetryingTransactionInterceptor extends TransactionAspectSupport implements MethodInterceptor
{
    private TransactionService transactionService;
    private List<Class<?>> extraExceptions;

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setExtraExceptions(List<Class<?>> extraExceptions)
    {
        this.extraExceptions = extraExceptions;
    }

    public Object invoke(final MethodInvocation invocation) throws Throwable
    {
        if ((null != invocation) && (null != invocation.getThis()) && (null != invocation.getMethod()))
        {
            final Method method = invocation.getMethod();
            final TransactionAttribute txnAttr = getTransactionAttributeSource().getTransactionAttribute(
                    method, invocation.getThis().getClass());

            final TransactionManager tm = determineTransactionManager(txnAttr, null);

            if (tm != null && !(tm instanceof PlatformTransactionManager))
            {
                throw new IllegalStateException("Specified transaction manager is not a PlatformTransactionManager: " + tm);
            }

            final PlatformTransactionManager ptm = (PlatformTransactionManager) tm;
            @SuppressWarnings("deprecation")
            final String joinpointIdentification = methodIdentification(invocation.getMethod(), invocation.getThis().getClass());
            final int propagationBehaviour = txnAttr.getPropagationBehavior();
            try
            {
                if (null != txnAttr && propagationBehaviour != TransactionAttribute.PROPAGATION_SUPPORTS)
                {
                    RetryingTransactionHelper retryingTransactionHelper = transactionService.getRetryingTransactionHelper();
                    retryingTransactionHelper.setExtraExceptions(extraExceptions);

                    return retryingTransactionHelper.doInTransaction(
                            new RetryingTransactionCallback<Object>()
                            {
                                public Object execute()
                                {
                                    TransactionInfo txInfo = createTransactionIfNecessary(ptm, TransactionAttribute.PROPAGATION_REQUIRES_NEW == txnAttr
                                            .getPropagationBehavior() ? null : txnAttr,
                                            joinpointIdentification);
                                    try
                                    {
                                        return invocation.proceed();
                                    }
                                    catch (RuntimeException e)
                                    {
                                        completeTransactionAfterThrowing(txInfo, e);
                                        throw e;
                                    }
                                    catch (Throwable e)
                                    {
                                        // Wrap non-runtime exceptions so that they can be preserved
                                        completeTransactionAfterThrowing(txInfo, e);
                                        throw new WrapperException(e);
                                    }
                                    finally
                                    {
                                        cleanupTransactionInfo(txInfo);
                                    }
                                }
                            },
                            txnAttr.isReadOnly(),
                            (TransactionAttribute.PROPAGATION_REQUIRES_NEW == propagationBehaviour));
                }
                else
                {
                    return invocation.proceed();
                }
            }
            catch (WrapperException e)
            {
                throw e.getCause();
            }
        }
        throw new AlfrescoRuntimeException("Invalid undefined MethodInvocation instance");
    }

    public static class WrapperException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;

        public WrapperException(Throwable cause)
        {
            super(cause);
        }
    }
}
