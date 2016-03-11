package org.alfresco.module.org_alfresco_module_rm.patch.v22;

import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.patch.AbstractModulePatch;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.role.Role;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;

/**
 * Removes the in-place groups from the all roles group.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public class RMv22RemoveInPlaceRolesFromAllPatch extends AbstractModulePatch
{
    /** file plan service */
    private FilePlanService filePlanService;
    
    /** file plan role service */
    private FilePlanRoleService filePlanRoleService;
    
    /** authority service */
    private AuthorityService authorityService;
    
    /**
     * @param filePlanService   file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }
    
    /**
     * @param filePlanRoleService   file plan role service
     */
    public void setFilePlanRoleService(FilePlanRoleService filePlanRoleService)
    {
        this.filePlanRoleService = filePlanRoleService;
    }
    
    /**
     * @param authorityService  authority service
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.patch.AbstractModulePatch#applyInternal()
     */
    @Override
    public void applyInternal()
    {
        // get all file plans
        Set<NodeRef> filePlans = filePlanService.getFilePlans();        
        for (NodeRef filePlan : filePlans)
        {
            Role extendedReaders = filePlanRoleService.getRole(filePlan, FilePlanRoleService.ROLE_EXTENDED_READERS);
            Role extendedWriters = filePlanRoleService.getRole(filePlan, FilePlanRoleService.ROLE_EXTENDED_WRITERS);
            
            // remove extended readers and writers roles from the all roles group
            String allRolesGroup = filePlanRoleService.getAllRolesContainerGroup(filePlan);              
            Set<String> members = authorityService.getContainedAuthorities(null, allRolesGroup, true);
            if (members.contains(extendedReaders.getRoleGroupName()))
            {
                authorityService.removeAuthority(allRolesGroup, extendedReaders.getRoleGroupName());
            }
            if (members.contains(extendedWriters.getRoleGroupName()))
            {
                authorityService.removeAuthority(allRolesGroup, extendedWriters.getRoleGroupName());
            }
        }
    }
}
