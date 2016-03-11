package org.alfresco.repo.security.permissions.impl;

import java.util.Set;

import org.alfresco.service.cmr.security.PermissionService;

/**
 * Extended Permission Service Interface used in RM.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public interface ExtendedPermissionService extends PermissionService
{
	/**
	 * Get a set of all the authorities that have write access.
	 * 
	 * @param  aclId							acl id
	 * @return {@link Set}<{@link String}>		set of authorities with write access
	 */
    Set<String> getWriters(Long aclId);
}
