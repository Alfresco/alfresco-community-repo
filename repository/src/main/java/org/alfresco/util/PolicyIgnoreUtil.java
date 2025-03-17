/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
package org.alfresco.util;

import java.util.Collections;
import java.util.Set;

import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Utility class which checks whether a node is in a store on which policies should not be applied(e.g. archive://SpacesStore)
 * 
 * @author cpopa
 *
 */
public class PolicyIgnoreUtil
{
    private TenantService tenantService;
    private Set<String> storesToIgnorePolicies = Collections.emptySet();

    public void setStoresToIgnorePolicies(Set<String> storesToIgnorePolicies)
    {
        this.storesToIgnorePolicies = storesToIgnorePolicies;
    }

    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    /**
     * Checks whether the node is in a store on which policies should not be applied.
     * 
     * @param nodeRef
     *            node to check if the policy can be run or not
     * @return true if the nodeRef is part of a store which should be ignored when invoking policies(e.g. archive://SpacesStore)
     */
    public boolean ignorePolicy(NodeRef nodeRef)
    {
        return (storesToIgnorePolicies.contains(tenantService.getBaseName(nodeRef.getStoreRef()).toString()));
    }
}
