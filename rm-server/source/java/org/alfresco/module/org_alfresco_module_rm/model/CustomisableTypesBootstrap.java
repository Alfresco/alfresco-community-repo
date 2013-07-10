/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.model;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Bootstrap bean that indicates that the specified types or aspects are
 * 
 * @author Roy Wetherall
 */
public class CustomisableTypesBootstrap 
{
	/** Records management admin service */
	private RecordsManagementAdminService recordsManagementAdminService;
	
	/** Namespace service */
	private NamespaceService namespaceService;
	
	/** List of types and aspects to register as customisable */
	private List<String> customisable;
	
	/**
	 * @param recordsManagementAdminService	records management admin service
	 */
	public void setRecordsManagementAdminService(RecordsManagementAdminService recordsManagementAdminService) 
	{
		this.recordsManagementAdminService = recordsManagementAdminService;
	}
	
	/**
	 * @param namespaceService	namespace service
	 */
	public void setNamespaceService(NamespaceService namespaceService) 
	{
		this.namespaceService = namespaceService;
	}
	
	/**
	 * @param customizable	list of types and aspects to register as customisable
	 */
	public void setCustomisable(List<String> customisable)
	{
		this.customisable = customisable;
	}
	
	/**
	 * Bean initialisation method
	 */
	public void init()
	{
		for (String customType : customisable) 
		{
			QName customTypeQName = QName.createQName(customType, namespaceService);
			recordsManagementAdminService.makeCustomisable(customTypeQName);
		}
	}
}
