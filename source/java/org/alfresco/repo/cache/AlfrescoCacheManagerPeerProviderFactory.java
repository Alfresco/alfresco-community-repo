/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
