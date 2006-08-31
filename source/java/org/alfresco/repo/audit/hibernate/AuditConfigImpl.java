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
    public static AuditConfigImpl getLatestConfig(Session session)
    {
        Query query = session.getNamedQuery(HibernateAuditDAO.QUERY_LAST_AUDIT_CONFIG);
        return (AuditConfigImpl) query.uniqueResult();
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
