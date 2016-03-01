 
package org.alfresco.repo.security.permissions.processor;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;


/**
 * Permission Veto Interface
 * 
 * @author Roy Wetherall
 * @since 2.4.a
 */
public interface PermissionPreProcessor
{
	/**
	 * Process permission.
	 * 
	 * @param  accessStatus			current access status
	 * @param  nodeRef				node reference
	 * @param  perm					permission
	 * @return {@link AccessStatus}
	 */
	AccessStatus process(NodeRef nodeRef, String perm);

}
