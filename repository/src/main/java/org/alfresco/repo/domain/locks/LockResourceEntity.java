/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.domain.locks;

/**
 * Entity bean for <b>alf_lock_resource</b> table.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class LockResourceEntity
{
    private Long id;
    private Long version;
    private Long qnameNamespaceId;
    private String qnameLocalName;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getVersion()
    {
        return version;
    }

    public void setVersion(Long version)
    {
        this.version = version;
    }

    /**
     * @return                  Returns the ID of the namespace that the lock belongs to
     */
    public Long getQnameNamespaceId()
    {
        return qnameNamespaceId;
    }

    /**
     * @param namespaceId       the ID of the namespace that the lock belongs to
     */
    public void setQnameNamespaceId(Long namespaceId)
    {
        this.qnameNamespaceId = namespaceId;
    }

    /**
     * @return                  Returns the lock qualified name localname
     */
    public String getQnameLocalName()
    {
        return qnameLocalName;
    }

    /**
     * @param qnameLocalName    the lock qualified name localname
     */
    public void setQnameLocalName(String qnameLocalName)
    {
        this.qnameLocalName = qnameLocalName;
    }
}
