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
package org.alfresco.repo.audit;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A class to read the audit configuration from the class path
 * 
 * @author Andy Hind
 */
public class AuditConfigurationImpl implements AuditConfiguration
{
    private static Log logger = LogFactory.getLog(AuditConfigurationImpl.class);

    private String config;
    
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

    public InputStream getInputStream()
    {
        InputStream is = null;
        try
        {
            is = new FileInputStream(getPath());
        }
        catch (FileNotFoundException e)
        {
            if (logger.isWarnEnabled())
            {
                logger.warn("File not found: " + getPath());
            }
        }
        return is;
    }
    
    public String getPath()
    {
        return this.getClass().getClassLoader().getResource(config).getPath();
    }
}
