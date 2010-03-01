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

public class AuditSourceImpl implements AuditSource
{
    /**
     * The surrogate key
     */
    private Long id;

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

    public Long getId()
    {
        return id;
    }

    protected void setId(Long id)
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
