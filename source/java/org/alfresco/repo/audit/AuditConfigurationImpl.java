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

import java.io.InputStream;

import org.springframework.beans.factory.InitializingBean;

/**
 * A class to read the audit configuration from the class path
 * 
 * @author Andy Hind
 */
public class AuditConfigurationImpl implements InitializingBean, AuditConfiguration
{

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


    /* (non-Javadoc)
     * @see org.alfresco.repo.audit.AuditConfiguration#getInputStream()
     */
    public InputStream getInputStream()
    {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(config);
        return is;
    }

    public void afterPropertiesSet() throws Exception
    {
        // Read and set up the audit configuration
        
    }
    
}
