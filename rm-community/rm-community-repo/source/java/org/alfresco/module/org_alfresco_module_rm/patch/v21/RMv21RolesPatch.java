package org.alfresco.module.org_alfresco_module_rm.patch.v21;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.role.Role;
import org.alfresco.repo.security.authority.RMAuthority;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.springframework.beans.factory.BeanNameAware;

/**
 * Adds the existing rm roles to a new zone "APP.RM"
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
@SuppressWarnings("deprecation")
public class RMv21RolesPatch extends RMv21PatchComponent implements BeanNameAware
{
    /** file plan service */
    private FilePlanService filePlanService;

    /** file plan role service */
    private FilePlanRoleService filePlanRoleService;

    /** authority service */
    private AuthorityService authorityService;

    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    public void setFilePlanRoleService(FilePlanRoleService filePlanRoleService)
    {
        this.filePlanRoleService = filePlanRoleService;
    }

    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.patch.compatibility.ModulePatchComponent#executePatch()
     */
    @Override
    protected void executePatch()
    {
        Set<NodeRef> filePlans = filePlanService.getFilePlans();

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug(" ... updating " + filePlans.size() + " file plans");
        }

        for (NodeRef filePlan : filePlans)
        {
            boolean parentAddedToZone = false;
            Set<Role> roles = filePlanRoleService.getRoles(filePlan);
            for (Role role : roles)
            {
                String roleGroupName = role.getRoleGroupName();
                if (!authorityService.getAuthorityZones(roleGroupName).contains(RMAuthority.ZONE_APP_RM))
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug(" ... updating " + roleGroupName + " in file plan " + filePlan.toString());
                    }

                    addAuthorityToZone(roleGroupName);
                    if (!parentAddedToZone)
                    {
                        String allRolesGroupName = filePlanRoleService.getAllRolesContainerGroup(filePlan);
                        addAuthorityToZone(allRolesGroupName);
                        parentAddedToZone = true;
                    }
                }
            }
        }
    }

    private void addAuthorityToZone(String roleGroupName)
    {
        authorityService.addAuthorityToZones(roleGroupName, new HashSet<String>(Arrays.asList(RMAuthority.ZONE_APP_RM)));
    }
}
