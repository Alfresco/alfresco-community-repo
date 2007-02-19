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

import java.io.IOException;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;

import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

/**
 * This is virtually a copy of the Springframework version, with the exception
 * that it uses the newer constructors for the <code>EHCacheManager</code>
 * instances.
 * 
 * @author Derek Hulley
 */
public class EhCacheManagerFactoryBean implements FactoryBean, InitializingBean, DisposableBean
{
    protected final Log logger = LogFactory.getLog(EhCacheManagerFactoryBean.class);

    private Resource configLocation;
    private CacheManager cacheManager;

    /**
     * 
     * @param configLocation a resource location using the <b>file:</b> or <b>classpath:</b> prefix
     */
    public void setConfigLocation(Resource configLocation)
    {
        this.configLocation = configLocation;
    }

    public void afterPropertiesSet() throws IOException, CacheException
    {
        PropertyCheck.mandatory(this, "configLocation", configLocation);
        
        logger.info("Initializing EHCache CacheManager");
        this.cacheManager = new CacheManager(this.configLocation.getURL());
    }

    public Object getObject()
    {
        return this.cacheManager;
    }

    public Class getObjectType()
    {
        return (this.cacheManager != null ? this.cacheManager.getClass() : CacheManager.class);
    }

    public boolean isSingleton()
    {
        return true;
    }

    public void destroy()
    {
        logger.info("Shutting down EHCache CacheManager");
        this.cacheManager.shutdown();
    }
}
