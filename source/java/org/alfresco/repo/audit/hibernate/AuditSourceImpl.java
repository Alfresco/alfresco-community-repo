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

public class AuditSourceImpl implements AuditSource
{
    /**
     * The surrogate key
     */
    private long id;

    /**
     * The auditing application (System for method audits)
     */
    private String application;

    /**
     * The audited service
     */
    private String service;

    /**
     * The audited method
     */
    private String method;

    public AuditSourceImpl()
    {
        super();
    }

    public String getApplication()
    {
        return application;
    }

    public void setApplication(String application)
    {
        this.application = application;
    }

    public long getId()
    {
        return id;
    }

    protected void setId(long id)
    {
        this.id = id;
    }

    public String getMethod()
    {
        return method;
    }

    public void setMethod(String method)
    {
        this.method = method;
    }

    public String getService()
    {
        return service;
    }

    public void setService(String service)
    {
        this.service = service;
    }

    public static AuditSource getApplicationSource(Session session, String application)
    {
        Query query = session.getNamedQuery(HibernateAuditDAO.QUERY_AUDIT_APP_SOURCE);
        query.setParameter(HibernateAuditDAO.QUERY_AUDIT_APP_SOURCE_APP, application);
        return (AuditSource) query.uniqueResult();
    }

    public static AuditSource getApplicationSource(Session session, String application, String service,
            String method)
    {
        Query query = session.getNamedQuery(HibernateAuditDAO.QUERY_AUDIT_METHOD_SOURCE);
        query.setParameter(HibernateAuditDAO.QUERY_AUDIT_APP_SOURCE_APP, application);
        query.setParameter(HibernateAuditDAO.QUERY_AUDIT_APP_SOURCE_SER, service);
        query.setParameter(HibernateAuditDAO.QUERY_AUDIT_APP_SOURCE_MET, method);
        return (AuditSource) query.uniqueResult();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof AuditSourceImpl))
        {
            return false;
        }
        AuditSourceImpl other = (AuditSourceImpl) o;
        return EqualsHelper.nullSafeEquals(this.application, other.application)
                && EqualsHelper.nullSafeEquals(this.service, other.service)
                && EqualsHelper.nullSafeEquals(this.method, other.method);
    }

    @Override
    public int hashCode()
    {
        int hash = application.hashCode();
        if(service != null)
        {
            hash = (hash * 37) + service.hashCode();
        }
        if(method != null)
        {
            hash = (hash * 37) + method.hashCode();
        }
        return hash;
    }

}
