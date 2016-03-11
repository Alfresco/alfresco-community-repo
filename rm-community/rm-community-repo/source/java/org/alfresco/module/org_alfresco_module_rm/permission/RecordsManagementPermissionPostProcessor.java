package org.alfresco.module.org_alfresco_module_rm.permission;

import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.security.permissions.processor.impl.PermissionPostProcessorBaseImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;

/**
 * Records management permission post processor.
 * 
 * @author Roy Wetherall
 * @since 2.4.a
 */
public class RecordsManagementPermissionPostProcessor extends PermissionPostProcessorBaseImpl 
{
	/** node service */
	private NodeService nodeService;
	public void setNodeService(NodeService nodeService) {this.nodeService=nodeService;}
	
	/** permission service */
	private PermissionService permissionService;
	public void setPermissionService(PermissionService permissionService) {this.permissionService=permissionService;}
	
	/**
	 * @see org.alfresco.repo.security.permissions.processor.PermissionPostProcessor#process(org.alfresco.service.cmr.security.AccessStatus, org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
	 */
	@Override
	public AccessStatus process(AccessStatus accessStatus, NodeRef nodeRef, String perm) 
	{
		AccessStatus result = accessStatus;
		if (AccessStatus.DENIED.equals(accessStatus) &&
            nodeService.hasAspect(nodeRef, RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT))
		{        
			// if read denied on rm artifact
	        if (PermissionService.READ.equals(perm))
	        {
	        	// check for read record
	            result = permissionService.hasPermission(nodeRef, RMPermissionModel.READ_RECORDS);
	        }
	        // if write deinied on rm artificat
	        else if (PermissionService.WRITE.equals(perm))
	        {
	        	// check for file record
	        	result = permissionService.hasPermission(nodeRef, RMPermissionModel.FILE_RECORDS);
	        }
		}
		
		return result;
	
	}

}
