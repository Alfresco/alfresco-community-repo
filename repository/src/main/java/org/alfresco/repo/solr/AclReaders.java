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
package org.alfresco.repo.solr;

import java.util.Set;

import org.alfresco.repo.tenant.TenantService;

/**
 * Bean for SOLR ACL readers.
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class AclReaders
{
    private Long aclId;
    private Set<String> readers;
    private Set<String> denied;
    private long aclChangeSetId;
    private String tenantDomain = TenantService.DEFAULT_DOMAIN;

    @Override
    public String toString()
    {
        return "AclReaders [aclId=" + aclId + ", readers=" + readers + ", denied=" + denied + ", aclChangeSetId=" + aclChangeSetId + ", tenantDomain=" + tenantDomain + "]";
    }

    public Long getAclId()
    {
        return aclId;
    }

    public void setAclId(Long aclId)
    {
        this.aclId = aclId;
    }

    public Set<String> getReaders()
    {
        return readers;
    }

    public void setReaders(Set<String> aclReaders)
    {
        this.readers = aclReaders;
    }

    public Set<String> getDenied()
    {
        return denied;
    }

    public void setDenied(Set<String> denied)
    {
        this.denied = denied;
    }

    public long getAclChangeSetId()
    {
        return aclChangeSetId;
    }

    public void setAclChangeSetId(long aclChangeSetId)
    {
        this.aclChangeSetId = aclChangeSetId;
    }

    public String getTenantDomain()
    {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain)
    {
        this.tenantDomain = tenantDomain;
    }
}
