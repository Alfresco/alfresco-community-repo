/*
 * Copyright (C) 2005 Alfresco, Inc.
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
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.InitializingBean;

/**
 * Utility class that ensures that a <tt>NodeDaoService</tt> has been registered
 * with the current transaction.
 * <p>
 * It is designed to act as a <b>postInterceptor</b> on the <tt>NodeDaoService</tt>'s
 * {@link org.springframework.transaction.interceptor.TransactionProxyFactoryBean}. 
 * 
 * @author Derek Hulley
 */
public class TransactionalDaoInterceptor implements MethodInterceptor, InitializingBean
{
    private TransactionalDao daoService;

    /**
     * @param daoService the <tt>NodeDaoService</tt> to register
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
