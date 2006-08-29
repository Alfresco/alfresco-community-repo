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
