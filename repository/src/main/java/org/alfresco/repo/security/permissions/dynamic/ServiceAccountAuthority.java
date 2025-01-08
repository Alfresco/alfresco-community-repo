/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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
package org.alfresco.repo.security.permissions.dynamic;

import java.util.Optional;
import java.util.Set;

import org.alfresco.repo.serviceaccount.ServiceAccountRegistry;
import org.alfresco.repo.security.permissions.DynamicAuthority;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.PropertyCheck;
import org.springframework.beans.factory.InitializingBean;

/**
 * This class represents a dynamic authority for service accounts in the system.
 *
 * @author Jamal Kaabi-Mofrad
 */
public class ServiceAccountAuthority implements DynamicAuthority, InitializingBean
{
    private String authority;
    private ServiceAccountRegistry serviceAccountRegistry;

    public void setAuthority(String authority)
    {
        this.authority = authority;
    }

    public void setServiceAccountRegistry(ServiceAccountRegistry serviceAccountRegistry)
    {
        this.serviceAccountRegistry = serviceAccountRegistry;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "authority", authority);
        PropertyCheck.mandatory(this, "serviceAccountRegistry", serviceAccountRegistry);
    }

    @Override
    public boolean hasAuthority(NodeRef nodeRef, String userName)
    {
        Optional<String> role = serviceAccountRegistry.getServiceAccountRole(userName);
        return role.isPresent() && role.get().equals(this.getAuthority());
    }

    @Override
    public String getAuthority()
    {
        return this.authority;
    }

    @Override
    public Set<PermissionReference> requiredFor()
    {
        return null;
    }
}