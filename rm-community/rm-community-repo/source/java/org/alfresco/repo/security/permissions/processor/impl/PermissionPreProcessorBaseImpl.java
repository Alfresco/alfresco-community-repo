 
package org.alfresco.repo.security.permissions.processor.impl;

import org.alfresco.repo.security.permissions.processor.PermissionPreProcessor;

/**
 * Permission pre-processor base implementation.
 * <p>
 * Helper class that can be extended when providing a custom permission
 * pre-processor implementation.
 * 
 * @author Roy Wetherall
 * @since 2.4.a
 */
public abstract class PermissionPreProcessorBaseImpl extends PermissionProcessorBaseImpl
													 implements PermissionPreProcessor 
{
	/**
	 * Init method to add this permission extensions to the registry
	 */
	public void init()
	{
		getPermissionProcessorRegistry().addPermissionPreProcessor(this);
	}
}
