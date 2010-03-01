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
package org.alfresco.repo.audit.hibernate;

import org.alfresco.util.EqualsHelper;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.InitializingBean;

public class AuditConfigImpl implements AuditConfig, InitializingBean
{
    /**
     * The hibernate generated internal key.
     */
    private Long id;

    /**
     * The URL to the content that contains the configuration file
     */
    private String configURL;

    public AuditConfigImpl()
    {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditContig#getConfigURL()
     */
    public String getConfigURL()
    {
        return configURL;
    }

    public void setConfigURL(String configURL)
    {
        this.configURL = configURL;
    }

    
    
    public void afterPropertiesSet() throws Exception
    {
        // Read the audit configuration
        
        
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.audit.hibernate.AuditContig#getId()
     */
    public Long getId()
    {
        return id;
    }

    /**
     * Internal setter for hibernate.
     * 
     * @param id
     */

    @SuppressWarnings("unused")
    private void setId(Long id)
    {
        this.id = id;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if(!(o instanceof AuditConfigImpl))
        {
            return false;
        }
        AuditConfigImpl other = (AuditConfigImpl)o;
        return EqualsHelper.nullSafeEquals(this.configURL, other.configURL);
    }

    @Override
    public int hashCode()
    {
        return configURL == null ? 0 : configURL.hashCode(); 
    }
    
    
}
