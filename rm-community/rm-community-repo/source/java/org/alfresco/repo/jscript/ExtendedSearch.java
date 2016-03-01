 
package org.alfresco.repo.jscript;

import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.ParameterCheck;

/**
 * Extended jscript search implementation.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class ExtendedSearch extends Search
{
    /**
     * Extended to take into account record read permission check.
     * 
     * @see org.alfresco.repo.jscript.Search#findNode(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public ScriptNode findNode(NodeRef ref)
    {
        ParameterCheck.mandatory("ref", ref);       
        if (this.services.getNodeService().exists(ref) &&
            (this.services.getPermissionService().hasPermission(ref, PermissionService.READ) == AccessStatus.ALLOWED ||
             this.services.getPermissionService().hasPermission(ref, RMPermissionModel.READ_RECORDS) == AccessStatus.ALLOWED))
        {
            return new ScriptNode(ref, this.services, getScope());
        }
        return null;
    }

}
