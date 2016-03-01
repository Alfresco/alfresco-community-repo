 
package org.alfresco.module.org_alfresco_module_rm.patch.common;

import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.patch.AbstractModulePatch;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;

/**
 * Abstract implementation of capability patch.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public abstract class CapabilityPatch extends AbstractModulePatch
{
    /** File plan service */
    private FilePlanService filePlanService;

    /** authority service */
    private AuthorityService authorityService;
    
    /** permission service */
    private PermissionService permissionService;
    
    /**
     * @param authorityService authority service
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    
    /**
     * @param permissionService permission service
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    /**
     * @param filePlanService   file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * Helper method to get the file plans
     *
     * @return Set of file plan node references
     */
    protected Set<NodeRef> getFilePlans()
    {
        return filePlanService.getFilePlans();
    }

    /**
     * Adds a new capability to the specified roles.
     *
     * @param filePlan          file plan
     * @param capabilityName    capability name
     * @param roles             roles
     */
    protected void addCapability(NodeRef filePlan, String capabilityName, String ... roles)
    {
        for (String role : roles)
        {
            String fullRoleName = role + filePlan.getId();
            String roleAuthority = authorityService.getName(AuthorityType.GROUP, fullRoleName);
            if (roleAuthority == null)
            {
                throw new AlfrescoRuntimeException("Role " + role + " does not exist.");
            }
            else
            {
                permissionService.setPermission(filePlan, roleAuthority, capabilityName, true);
            }
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.patch.AbstractModulePatch#applyInternal()
     */
    @Override
    public void applyInternal()
    {
        Set<NodeRef> filePlans = getFilePlans();

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("  ... updating " + filePlans.size() + " file plans");
        }

        for (NodeRef filePlan : filePlans)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("  ... updating file plan " + filePlan.toString());
            }
            
            // apply the capability patch to each file plan
            applyCapabilityPatch(filePlan);
        }
    }
    
    @Override
    public void apply()
    {
        setTxnReadOnly(false);
        setTxnRequiresNew(true);
        super.apply();
    }
    
    protected abstract void applyCapabilityPatch(NodeRef filePlan);
}
