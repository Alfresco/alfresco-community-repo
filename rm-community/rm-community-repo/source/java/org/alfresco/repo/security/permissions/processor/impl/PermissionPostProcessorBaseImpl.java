package org.alfresco.repo.security.permissions.processor.impl;

import org.alfresco.repo.security.permissions.processor.PermissionPostProcessor;

/**
 * Permission post processor base implementation.
 * <p>
 * Helper class that can be extended when providing a custom permission
 * post processor implementation.
 * 
 * @author Roy Wetherall
 * @since 2.4.a
 */
public abstract class PermissionPostProcessorBaseImpl extends PermissionProcessorBaseImpl
													  implements PermissionPostProcessor 
{
	/**
	 * Init method to add this permission extensions to the registry
	 */
	public void init()
	{
		getPermissionProcessorRegistry().addPermissionPostProcessor(this);
	}
}
