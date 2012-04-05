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
package org.alfresco.module.org_alfresco_module_rm.test.system;

import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.test.util.TestUtilities;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.util.BaseSpringTest;

/**
 * 
 * 
 * @author Roy Wetherall
 */
public class DODDataLoadSystemTest extends BaseSpringTest 
{    
	private NodeService nodeService;
	private AuthenticationComponent authenticationComponent;
	private ImporterService importer;
    private PermissionService permissionService;
    private SearchService searchService;
    private RecordsManagementService rmService;
    private RecordsManagementActionService rmActionService;
	
	@Override
	protected void onSetUpInTransaction() throws Exception 
	{
		super.onSetUpInTransaction();

		// Get the service required in the tests
		this.nodeService = (NodeService)this.applicationContext.getBean("NodeService");
		this.authenticationComponent = (AuthenticationComponent)this.applicationContext.getBean("authenticationComponent");
		this.importer = (ImporterService)this.applicationContext.getBean("ImporterService");
		this.permissionService = (PermissionService)this.applicationContext.getBean("PermissionService");
		searchService = (SearchService)applicationContext.getBean("SearchService");
		rmService = (RecordsManagementService)applicationContext.getBean("RecordsManagementService");
        rmActionService = (RecordsManagementActionService)applicationContext.getBean("RecordsManagementActionService");
		
		
		// Set the current security context as admin
		this.authenticationComponent.setCurrentUser(AuthenticationUtil.getSystemUserName());		
	}

    public void testSetup()
    {
        // NOOP
    }
    
	public void testLoadFilePlanData()
	{
	    TestUtilities.loadFilePlanData(applicationContext);
	    
	    setComplete();
        endTransaction();
	}
}
