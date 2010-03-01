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
package org.alfresco.repo.cache;

import java.util.Properties;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.distribution.CacheManagerPeerProvider;
import net.sf.ehcache.distribution.CacheManagerPeerProviderFactory;
import net.sf.ehcache.distribution.RMICacheManagerPeerProviderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Alfresco's <tt>CacheManagerPeerProviderFactory</tt> that defers to the community or
 * enterprise factories.
 * 
 * @author Derek Hulley
 * @since 3.1
 */
public class AlfrescoCacheManagerPeerProviderFactory extends CacheManagerPeerProviderFactory
{
    private static Log logger = LogFactory.getLog(AlfrescoCacheManagerPeerProviderFactory.class);
    
    @Override
    public CacheManagerPeerProvider createCachePeerProvider(CacheManager cacheManager, Properties properties)
    {
        CacheManagerPeerProviderFactory factory = null;
        try
        {
            @SuppressWarnings("unchecked")
            Class clazz = Class.forName("org.alfresco.enterprise.repo.cache.jgroups.JGroupsRMICacheManagerPeerProvider$Factory");
            factory = (CacheManagerPeerProviderFactory) clazz.newInstance();
        }
        catch (ClassNotFoundException e)
        {
            // Entirely expected if the Enterprise-level code is not present
        }
        catch (Throwable e)
        {
            logger.error("Failed to instantiate JGroupsRMICacheManagerPeerProvider factory.", e);
        }
        finally
        {
            if (factory == null)
            {
                // Use EHCache's default implementation
                factory = new RMICacheManagerPeerProviderFactory();
            }
        }
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Using peer provider factory: " + factory.getClass().getName());
        }
        
        return factory.createCachePeerProvider(cacheManager, properties);
    }

}
