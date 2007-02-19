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
    private long id;

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
    public long getId()
    {
        return id;
    }

    /**
     * Internal setter for hibernate.
     * 
     * @param id
     */

    @SuppressWarnings("unused")
    private void setId(long id)
    {
        this.id = id;
    }

    /**
     * Helper method to get the latest audit config
     */
    public static AuditConfig getLatestConfig(Session session)
    {
        Query query = session.getNamedQuery(HibernateAuditDAO.QUERY_LAST_AUDIT_CONFIG);
        return (AuditConfig) query.uniqueResult();
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
