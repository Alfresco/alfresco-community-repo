/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.security;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.capability.Capability;

/**
 * Records management role class
 *
 * @author Roy Wetherall
 * 
 * @deprecated As of release 2.1, see {@link org.alfresco.module.org_alfresco_module_rm.role.Role}
 */
public class Role extends org.alfresco.module.org_alfresco_module_rm.role.Role
{
    /**
     * Compatibility method
     */
    public static Role toRole(org.alfresco.module.org_alfresco_module_rm.role.Role role)
    {
        return new Role(role.getName(), role.getDisplayLabel(), role.getCapabilities(), role.getRoleGroupName());
    }
    
    /**
     * Compatibility method
     */
    public static Set<Role> toRoleSet(Set<org.alfresco.module.org_alfresco_module_rm.role.Role> roles)
    {
        Set<Role> result = new HashSet<>(roles.size());
        for (org.alfresco.module.org_alfresco_module_rm.role.Role role : roles)
        {
            result.add(Role.toRole(role));
        }
        return result;
    }
    
    /**
     * Constructor
     */
    @Deprecated
    public Role(String name, String displayLabel, Set<Capability> capabilities, String roleGroupName)
    {
        super(name, displayLabel, capabilities, roleGroupName);
    }
}
