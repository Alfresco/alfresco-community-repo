 
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
        Set<Role> result = new HashSet<Role>(roles.size());
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
