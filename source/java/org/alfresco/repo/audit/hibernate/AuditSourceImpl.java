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

    public static AuditSourceImpl getApplicationSource(Session session, String application)
    {
        Query query = session.getNamedQuery(HibernateAuditDAO.QUERY_AUDIT_APP_SOURCE);
        query.setParameter(HibernateAuditDAO.QUERY_AUDIT_APP_SOURCE_APP, application);
        return (AuditSourceImpl) query.uniqueResult();
    }

    public static AuditSourceImpl getApplicationSource(Session session, String application, String service,
            String method)
    {
        Query query = session.getNamedQuery(HibernateAuditDAO.QUERY_AUDIT_METHOD_SOURCE);
        query.setParameter(HibernateAuditDAO.QUERY_AUDIT_APP_SOURCE_APP, application);
        query.setParameter(HibernateAuditDAO.QUERY_AUDIT_APP_SOURCE_SER, service);
        query.setParameter(HibernateAuditDAO.QUERY_AUDIT_APP_SOURCE_MET, method);
        return (AuditSourceImpl) query.uniqueResult();
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
