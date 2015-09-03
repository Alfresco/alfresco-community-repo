/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
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
