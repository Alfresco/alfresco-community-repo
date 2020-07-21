/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.opencmis;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.chemistry.opencmis.commons.spi.Holder;

/**
 * The interceptor wraps {@link Holder} class in {@link TransactionAwareHolder}.
 * This is designed specifically for CMIS Service.
 *
 * @author alex.mukha
 */
public class CMISTransactionAwareHolderInterceptor implements MethodInterceptor
{
    @Override
    @SuppressWarnings("unchecked")
    public Object invoke(MethodInvocation invocation) throws Throwable
    {
        Class<?>[] parameterTypes = invocation.getMethod().getParameterTypes();
        Object[] arguments = invocation.getArguments();
        for (int i = 0; i < parameterTypes.length; i++)
        {
            if (Holder.class.isAssignableFrom(parameterTypes[i]) && arguments[i] != null)
            {
                TransactionAwareHolder txnHolder = new TransactionAwareHolder(((Holder) arguments[i]));
                arguments[i] = txnHolder;
            }
        }
        return invocation.proceed();
    }
}
