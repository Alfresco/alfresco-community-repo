/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
    
    public AuditConfigurationImpl()
    {
        super();
    }
    
    public void setConfig(String config)
    {
        this.config = config;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.audit.getInputStream#getInputStream()
     */
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
