/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
	private List<PermissionPreProcessor> permissionPreProcessors = new ArrayList<>();
	
	/** permission post-processors */
	private List<PermissionPostProcessor> permissionPostProcessors = new ArrayList<>();
	
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
	 * @return {@link List}&lt;{@link PermissionPreProcessor}&gt;	list of permission pre-processors
	 */
	public List<PermissionPreProcessor> getPermissionPreProcessors()
	{
		return permissionPreProcessors;
	}	
	
	/**
	 * Get a list of the registered permission post-processors.
	 * 
	 * @return &lt;{@link List}&gt;{@link PermissionPreProcessor} list of permission post-processors
	 */
	public List<PermissionPostProcessor> getPermissionPostProcessors() 
	{
		return permissionPostProcessors;
	}
}
