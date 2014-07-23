/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts;

import org.alfresco.repo.cache.AbstractMTAsynchronouslyRefreshedCache;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.extensions.webscripts.Registry;

/**
 * Asynchronously refreshed cache for repository webscripts.
 * <p/>
 * This does not stop gratuitous calls to <i>refresh</i> but will ensure that, once an instance has been created,
 * a version of the registry is returned even if it is slighly out of date.  This can be changed so that it waits
 * for reset but is probably not required.
 * 
 * @author Derek Hulley
 * @since 4.2.0
 */
public class RegistryAsynchronouslyRefreshedCache extends  AbstractMTAsynchronouslyRefreshedCache<Registry> implements InitializingBean
{
    private static Log logger = LogFactory.getLog(RegistryAsynchronouslyRefreshedCache.class);
    
    private ObjectFactory<Registry> registryFactory;
    private RetryingTransactionHelper retryingTransactionHelper;

    /**
     * @param registryFactory               factory for web script registries
     */
    public void setRegistryFactory(ObjectFactory<Registry> registryFactory)
    {
        this.registryFactory = registryFactory;
    }
    
    /**
     * @param retryingTransactionHelper     the retryingTransactionHelper to set
     */
    public void setRetryingTransactionHelper(RetryingTransactionHelper retryingTransactionHelper)
    {
        this.retryingTransactionHelper = retryingTransactionHelper;
    }

    @Override
    protected Registry buildCache(final String tenantId)
    {
        return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Registry>()
        {
            @Override
            public Registry execute() throws Throwable
            {
                return doBuildCache(tenantId);
            }
        }, true, false);
    }

    /**
     * This method is thread safe as per contract of {@link #buildCache(String)}.
     */
    private Registry doBuildCache(String tenantId)
    {
        Registry registry = registryFactory.getObject();
        registry.reset();
        logger.info("Fetching web script registry for tenant " + tenantId);
        return registry;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "registryFactory", registryFactory);
        PropertyCheck.mandatory(this, "retryingTransactionHelper", retryingTransactionHelper);
        super.afterPropertiesSet();
    }
}
