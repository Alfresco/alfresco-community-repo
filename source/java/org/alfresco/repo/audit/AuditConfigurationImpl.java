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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.audit;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * A class to read the audit configuration from the class path
 * 
 * @author Andy Hind
 */
public class AuditConfigurationImpl implements AuditConfiguration, ResourceLoaderAware
{
    private static Log logger = LogFactory.getLog(AuditConfigurationImpl.class);
    private static long STARTUP_TIME = System.currentTimeMillis();

    private String config;
    
    private ResourceLoader resourceLoader;
    
    /**
     * Default constructor
     *
     */
    public AuditConfigurationImpl()
    {
        super();
    }
    
    /**
     * Set the audit config
     * 
     * @param config
     */
    public void setConfig(String config)
    {
        this.config = config;
    }

    private Resource getResource()
    {
        return this.resourceLoader.getResource(config);
    }

    public InputStream getInputStream()
    {
        try
        {
            return getResource().getInputStream();
        }
        catch (IOException e)
        {
            logger.warn("Unable to resolve " + config + " as input stream", e);
            return null;
        }
    }
            
    /*
     * (non-Javadoc)
     * @see
     * org.springframework.context.ResourceLoaderAware#setResourceLoader(org.springframework.core.io.ResourceLoader)
     */
    public void setResourceLoader(ResourceLoader resourceLoader)
    {
        this.resourceLoader = resourceLoader;
    }

    public long getLastModified()
    {
        try 
        {
            return getResource().getFile().lastModified();
        }
        catch (IOException e) 
        {
            // Not all resources can be resolved to files on the filesystem. If this is the case, just return the time
            // the server was last started
            return STARTUP_TIME;
        }
    }
}
