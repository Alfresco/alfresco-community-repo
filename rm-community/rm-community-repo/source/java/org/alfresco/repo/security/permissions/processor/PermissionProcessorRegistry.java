package org.alfresco.repo.security.permissions.processor;

import java.util.ArrayList;
import java.util.List;

/**
 * Permission Processor Registry
 * 
 * @author Roy Wetherall
 * @since 2.4.a
 */
public class PermissionProcessorRegistry 
{
	/** permission pre-processors */
	private List<PermissionPreProcessor> permissionPreProcessors = new ArrayList<PermissionPreProcessor>();
	
	/** permission post-processors */
	private List<PermissionPostProcessor> permissionPostProcessors = new ArrayList<PermissionPostProcessor>();
	
	/**
	 * Add a permission pre-processor.
	 * 
	 * @param permissionPreProcessor permission pre-processor
	 */
	public void addPermissionPreProcessor(PermissionPreProcessor permissionPreProcessor)
	{
		permissionPreProcessors.add(permissionPreProcessor);
	}
	
	/**
	 * Add a permission post-processor.
	 * 
	 * @param permissionPostProcessor	permission post-processor
	 */
	public void addPermissionPostProcessor(PermissionPostProcessor permissionPostProcessor)
	{
		permissionPostProcessors.add(permissionPostProcessor);
	}
	
	/**
	 * Get a list of the registered permission pre-processors.
	 * 
	 * @return {@link List}<{@link PermissionPreProcessor}>	list of permission pre-processors
	 */
	public List<PermissionPreProcessor> getPermissionPreProcessors()
	{
		return permissionPreProcessors;
	}	
	
	/**
	 * Get a list of the registered permission post-processors.
	 * 
	 * @return <{@link List}>{@link PermissionPreProcessor} list of permission post-processors
	 */
	public List<PermissionPostProcessor> getPermissionPostProcessors() 
	{
		return permissionPostProcessors;
	}
}
