/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
