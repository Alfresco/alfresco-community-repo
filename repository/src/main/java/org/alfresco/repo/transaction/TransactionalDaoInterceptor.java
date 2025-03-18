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

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.InitializingBean;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Utility class that ensures that a <tt>NodeDaoService</tt> has been registered with the current transaction.
 * <p>
 * It is designed to act as a <b>postInterceptor</b> on the <tt>NodeDaoService</tt>'s {@link org.springframework.transaction.interceptor.TransactionProxyFactoryBean}.
 * 
 * @author Derek Hulley
 */
public class TransactionalDaoInterceptor implements MethodInterceptor, InitializingBean
{
    private TransactionalDao daoService;

    /**
     * @param daoService
     *            the <tt>NodeDaoService</tt> to register
     */
    public void setDaoService(TransactionalDao daoService)
    {
        this.daoService = daoService;
    }

    /**
     * Checks that required values have been injected
     */
    public void afterPropertiesSet() throws Exception
    {
        if (daoService == null)
        {
            throw new AlfrescoRuntimeException("TransactionalDao is required: " + this);
        }
    }

    public Object invoke(MethodInvocation invocation) throws Throwable
    {
        AlfrescoTransactionSupport.bindDaoService(daoService);
        // propogate the call
        return invocation.proceed();
    }
}
