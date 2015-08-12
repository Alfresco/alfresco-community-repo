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
package org.alfresco.repo.security.permissions.veto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Roy Wetherall
 * @since 3.0.a
 */
public class PermissionVetoRegistry 
{
	/** list of vetos to apply */
	private List<PermissionVeto> permissionVetos = new ArrayList<PermissionVeto>();
	
	/**
	 * @param permissionVeto permission veto
	 */
	public void addPermissionVeto(PermissionVeto permissionVeto)
	{
		permissionVetos.add(permissionVeto);
	}
	
	/**
	 * @return {@link List}<{@link PermissionVeto}>	list of permission vetos
	 */
	public List<PermissionVeto> getPermissionVetos()
	{
		return permissionVetos;
	}	
}
