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
import org.hibernate.cache.EhCacheProvider;
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
                CacheManager.create(configUrl);
            }
            catch (FileNotFoundException e)
            {
                // try the alfresco default
                URL configUrl = ResourceUtils.getURL(DEFAULT_CONFIGURATION_FILE);
                CacheManager.create(configUrl);   // this file MUST be present
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
        initCacheManager();
        return CacheManager.getInstance();
    }

    /**
     * @see #hibernateEhCacheProvider
     */
    public Cache buildCache(String regionName, Properties properties) throws CacheException
    {
        initCacheManager();
        return hibernateEhCacheProvider.buildCache(regionName, properties);
    }

    /**
     * @see #hibernateEhCacheProvider
     */
    public boolean isMinimalPutsEnabledByDefault()
    {
        return hibernateEhCacheProvider.isMinimalPutsEnabledByDefault();
    }

    /**
     * @see #hibernateEhCacheProvider
     */
    public long nextTimestamp()
    {
        return hibernateEhCacheProvider.nextTimestamp();
    }

    /**
     * @see #initCacheManager()
     * @see #hibernateEhCacheProvider
     */
    public void start(Properties properties) throws CacheException
    {
        initCacheManager();
        hibernateEhCacheProvider.start(properties);
    }

    /**
     * @see #initCacheManager()
     * @see #hibernateEhCacheProvider
     */
    public void stop()
    {
        initCacheManager();
        hibernateEhCacheProvider.stop();
    }

    /**
     * @return Returns the singleton cache manager
     * 
     * @see #initCacheManager()
     */
    public Object getObject() throws Exception
    {
        initCacheManager();
        return CacheManager.getInstance();
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
