package org.alfresco.repo.security.permissions.processor.impl;

import org.alfresco.repo.security.permissions.processor.PermissionProcessorRegistry;

/**
 * Commonality found in both pre and post permission processor implementations.
 * 
 * @author Roy Wetherall
 * @since 2.4.a
 */
/*package*/ abstract class PermissionProcessorBaseImpl 
{
	/** permission processor registry */
	private PermissionProcessorRegistry permissionProcessorRegistry;
	
	/**
	 * @param PermissionProcessorRegistry	permission processor registry
	 */
	public void setPermissionProcessorRegistry(PermissionProcessorRegistry permissionProcessorRegistry) 
	{
		this.permissionProcessorRegistry = permissionProcessorRegistry;
	}
	
	/**
	 * @return {@link PermissionProcessorRegistry}	permission processor registry
	 */
	protected PermissionProcessorRegistry getPermissionProcessorRegistry() 
	{
		return permissionProcessorRegistry;
	}
}
