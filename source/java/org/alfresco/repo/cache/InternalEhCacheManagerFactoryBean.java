/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Properties;

import net.sf.ehcache.CacheManager;

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.CacheProvider;
import org.hibernate.cache.EhCache;
import org.hibernate.cache.EhCacheProvider;
import org.hibernate.cache.Timestamper;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.ResourceUtils;

/**
 * Alfresco-specific cache manager factory.
 * <p>
 * The purpose of this bean is to provide a common point from which the system-wide
 * EHCache <code>CacheManager</code> singleton is created.  Hibernate and Spring
 * will all pick up the same <code>CacheManager</code> instance.  It then becomes
 * possible to initialise this instance in whichever way we require, provided it
 * is done in a well-known (non-configurable) way.
 * <p>
 * For Alfresco purposes, there are two files that are looked for:
 * <ul>
 *   <li><b>classpath:alfresco/extension/ehcache-custom.xml</b>, which will take precedence</li>
 *   <li><b>classpath:alfresco/ehcache-default.xml</b>, which is the default shipped with Alfresco</li>
 * </ul>
 * <p>
 * The EHCache static singleton instance is used but ensuring that all access to the
 * instance goes through the required initialization code first.
 * <p>
 * TODO: Provide mixing of config so that cache definitions in the custom file override
 *       those in the default file
 * 
 * @see #getInstance()
 * 
 * @author Derek Hulley
 */
public class InternalEhCacheManagerFactoryBean implements FactoryBean, CacheProvider
{
    public static final String CUSTOM_CONFIGURATION_FILE = "classpath:alfresco/extension/ehcache-custom.xml";
    public static final String DEFAULT_CONFIGURATION_FILE = "classpath:alfresco/ehcache-default.xml";
    
    private static Log logger = LogFactory.getLog(InternalEhCacheManagerFactoryBean.class);
    
    /** keep track of the singleton status to avoid work */
    private static boolean initialized;
    /** the <code>CacheManager</code> */
    private static CacheManager cacheManager;
    /** used to ensure that the existing Hibernate logic is maintained */
    private static EhCacheProvider hibernateEhCacheProvider = new EhCacheProvider();
    
    /**
     * Default constructor required by Hibernate.  In fact, we anticipate several
     * instances of this class to be created.
     */
    public InternalEhCacheManagerFactoryBean()
    {
    }
    
    /**
     * News up the singleton cache manager according to the rules set out
     * in the class comments.
     */
    private static synchronized void initCacheManager()
    {
        if (initialized)
        {
            return;
        }
        try
        {
            boolean defaultLocation = false;
            try
            {
                URL configUrl = ResourceUtils.getURL(CUSTOM_CONFIGURATION_FILE);
                InternalEhCacheManagerFactoryBean.cacheManager = new CacheManager(configUrl);
            }
            catch (FileNotFoundException e)
            {
                // try the alfresco default
                URL configUrl = ResourceUtils.getURL(DEFAULT_CONFIGURATION_FILE);
                if (configUrl == null)
                {
                    throw new AlfrescoRuntimeException("Missing default cache config: " + DEFAULT_CONFIGURATION_FILE);
                }
                InternalEhCacheManagerFactoryBean.cacheManager = new CacheManager(configUrl);
                defaultLocation = true;
            }
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("Created EHCache CacheManager instance: \n" +
                        "   configuration: " + (defaultLocation ? DEFAULT_CONFIGURATION_FILE : CUSTOM_CONFIGURATION_FILE));
            }
            initialized = true;
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException("EHCache configuration failed", e);
        }
    }
    
    /**
     * @return Returns the properly initialized instance for Alfresco internal use
     * 
     * @see #initCacheManager()
     */
    public static CacheManager getInstance()
    {
        if (!InternalEhCacheManagerFactoryBean.initialized)
        {
            InternalEhCacheManagerFactoryBean.initCacheManager();
        }
        return InternalEhCacheManagerFactoryBean.cacheManager;
    }

    public Cache buildCache(String regionName, Properties properties) throws CacheException
    {
        CacheManager manager = InternalEhCacheManagerFactoryBean.getInstance();
        try
        {
            net.sf.ehcache.Cache cache = manager.getCache(regionName);
            if (cache == null)
            {
                logger.info("Using default cache configuration: " + regionName);
                manager.addCache(regionName);
                cache = manager.getCache(regionName);
                logger.debug("Started EHCache region: " + regionName);
            }
            return new EhCache(cache);
        }
        catch (net.sf.ehcache.CacheException e)
        {
            throw new CacheException(e);
        }
    }

    /**
     * @see #hibernateEhCacheProvider
     */
    public boolean isMinimalPutsEnabledByDefault()
    {
        return false;
    }

    /**
     * @see #hibernateEhCacheProvider
     */
    public long nextTimestamp()
    {
        return Timestamper.next();
    }

    /**
     * @see #initCacheManager()
     */
    public void start(Properties properties) throws CacheException
    {
        InternalEhCacheManagerFactoryBean.initCacheManager();
    }

    /**
     * @see #initCacheManager()
     */
    public void stop()
    {
        InternalEhCacheManagerFactoryBean.getInstance().shutdown();
    }

    /**
     * @return Returns the singleton cache manager
     * 
     * @see #initCacheManager()
     */
    public Object getObject() throws Exception
    {
        return InternalEhCacheManagerFactoryBean.getInstance();
    }

    /**
     * @return Returns the singleton cache manager type
     */
    public Class getObjectType()
    {
        return CacheManager.class;
    }

    /**
     * @return Returns true always
     */
    public boolean isSingleton()
    {
        return true;
    }
}
