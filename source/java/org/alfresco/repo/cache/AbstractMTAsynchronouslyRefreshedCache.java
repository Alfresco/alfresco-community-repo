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
package org.alfresco.repo.cache;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;

/**
 * The base implementation for Multi-tenant asynchronously refreshed cache.  Currently supports one value per tenant. 
 * 
 * Implementors just need to provide buildCache(String tennantId)
 * 
 * @author Andy
 * @since 4.1.3
 */
public abstract class AbstractMTAsynchronouslyRefreshedCache<T> 
    extends org.alfresco.util.cache.AbstractAsynchronouslyRefreshedCache<T> 
	implements AsynchronouslyRefreshedCache<T>, InitializingBean
{
    
    private static Log logger = LogFactory.getLog(AbstractMTAsynchronouslyRefreshedCache.class);

    private TenantService tenantService;

    /**
     * @param tenantService
     *            the tenantService to set
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }


    @Override
    public T get()
    {
        String tenantId = tenantService.getCurrentUserDomain();
        return get(tenantId);
    }

    public void forceInChangesForThisUncommittedTransaction()
    {
        String tenantId = tenantService.getCurrentUserDomain();
        forceInChangesForThisUncommittedTransaction(tenantId);
    }

    @Override
    public void refresh()
    {
        String tenantId = tenantService.getCurrentUserDomain();
        refresh(tenantId);
    }


    @Override
    public boolean isUpToDate()
    {
       String tenantId = tenantService.getCurrentUserDomain();
       return isUpToDate(tenantId);
    } 

    /**
     * Build the cache entry for the specific tenant.
     * This method is called in a thread-safe manner i.e. it is only ever called by a single
     * thread.
     */
    protected abstract T buildCache(String tenantId);
    

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "tenantService", tenantService);
        super.afterPropertiesSet();
    }
}
