/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.rest.api.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class AuditEntry
{

    private Long id;
    private String auditApplicationId;
    protected UserInfo createdByUser;
    protected Date createdAt;
    protected Map<String, Serializable> values;

    public AuditEntry()
    {

    }

    public AuditEntry(Long id, String auditApplicationId, UserInfo createdByUser, Date createdAt, Map<String, Serializable> values2)
    {
        this.id = id;
        this.auditApplicationId = auditApplicationId;
        this.createdByUser = createdByUser;
        this.createdAt = createdAt;
        this.values = values2;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getAuditApplicationId()
    {
        return auditApplicationId;
    }

    public void setAuditApplicationId(String auditApplicationId)
    {
        this.auditApplicationId = auditApplicationId;
    }

    public UserInfo getCreatedByUser()
    {
        return createdByUser;
    }

    public void setCreatedByUser(UserInfo createdByUser)
    {
        this.createdByUser = createdByUser;
    }

    public Date getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt)
    {
        this.createdAt = createdAt;
    }

    public Map<String, Serializable> getValues()
    {
        return values;
    }

    public void setValues(Map<String, Serializable> values)
    {
        this.values = values;
    }

}
