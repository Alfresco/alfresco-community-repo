/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.encryption;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;

/**
 * Loads key resources (key store and key store passwords) from the Spring classpath.
 * 
 * @since 4.0
 *
 */
public class SpringKeyResourceLoader implements KeyResourceLoader, ApplicationContextAware
{
    /**
     * The application context might not be available, in which case the usual URL
     * loading is used.
     */
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }
    
    /**
     * Helper method to switch between application context resource loading or
     * simpler current classloader resource loading.
     */
    private InputStream getSafeInputStream(String location)
    {
        try
        {
            final InputStream is;
            if (applicationContext != null)
            {
                Resource resource = applicationContext.getResource(location);
                if (resource.exists())
                {
                    is = new BufferedInputStream(resource.getInputStream());
                }
                else
                {
                    // Fall back to conventional loading
                    File file = ResourceUtils.getFile(location);
                    if (file.exists())
                    {
                        is = new BufferedInputStream(new FileInputStream(file));
                    }
                    else
                    {
                        is = null;
                    }
                }
            }
            else
            {
                // Load conventionally (i.e. we are in a unit test)
                File file = ResourceUtils.getFile(location);
                if (file.exists())
                {
                    is = new BufferedInputStream(new FileInputStream(file));
                }
                else
                {
                    is = null;
                }
            }

            return is;
        }
        catch (IOException e) 
        {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getKeyStore(String keyStoreLocation)
    {
        if (keyStoreLocation == null)
        {
            return null;
        }
        return getSafeInputStream(keyStoreLocation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Properties loadKeyMetaData(String keyMetaDataFileLocation) throws IOException
    {
        if (keyMetaDataFileLocation == null)
        {
            return null;
        }

        try
        {
            InputStream is = getSafeInputStream(keyMetaDataFileLocation);
            if (is == null)
            {
                return null;
            }
            else
            {
                try
                {
                    Properties p = new Properties();
                    p.load(is);
                    return p;
                }
                finally
                {
                    try { is.close(); } catch (Throwable e) {}
                }
            }
        }
        catch(FileNotFoundException e)
        {
            return null;
        }
    }
}
