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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * A wrapper that checks that a transaction is present.
 *
 * @author Derek Hulley
 * @since 2.2.1
 */
public class CheckTransactionAdvice implements MethodInterceptor
{
    public CheckTransactionAdvice()
    {
    }
    public Object invoke(final MethodInvocation methodInvocation) throws Throwable
    {
        // Just check for any transaction
        if (AlfrescoTransactionSupport.getTransactionReadState() == TxnReadState.TXN_NONE)
        {
            String methodName = methodInvocation.getMethod().getName();
            String className = methodInvocation.getMethod().getDeclaringClass().getName();
            throw new AlfrescoRuntimeException(
                    "A transaction has not be started for method '" + methodName + "' on " + className);
        }
        return methodInvocation.proceed();
    }
}
