/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
            throw new AlfrescoRuntimeException("A transaction has not be started for method " + methodName + " on " + className);
        }
        return methodInvocation.proceed();
    }
}
