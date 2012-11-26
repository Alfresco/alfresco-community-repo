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
package org.alfresco.module.org_alfresco_module_rm.test.service;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.dm.CreateRecordAction;
import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.security.ExtendedReaderDynamicAuthority;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.site.SiteServiceImpl;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.util.GUID;

/**
 * Record service implementation unit test.
 * 
 * @author Roy Wetherall
 */
public class RecordServiceTestImpl extends BaseRMTestCase
{
    protected static final String COLLABORATION_SITE_ID = "collab-site-id";
    
    protected ActionService dmActionService;
    protected TaggingService taggingService;
    protected PermissionService dmPermissionService;
    
    protected SiteInfo collaborationSite;
    protected NodeRef documentLibrary;
    protected NodeRef dmFolder;
    protected NodeRef dmDocument;
    
    protected String dmUserName;
    protected NodeRef dmUserPerson;
    
    @Override
    protected void initServices()
    {
        super.initServices();
        
        dmActionService = (ActionService)applicationContext.getBean("ActionService");
        taggingService = (TaggingService)applicationContext.getBean("TaggingService");
        dmPermissionService = (PermissionService)applicationContext.getBean("PermissionService");
    }
    
    @Override
    protected boolean isUserTest()
    {
        return true;
    }
    
    @Override
    protected void setupTestData()
    {
        super.setupTestData();
        
        doTestInTransaction(new Test<Void>()
        {
            public Void run()
            {
                setupCollaborationSiteTestDataImpl();
                return null;
            }                      
        }, 
        AuthenticationUtil.getSystemUserName());   
    }
    
    protected void setupCollaborationSiteTestDataImpl()
    {
        // create collaboration site
        collaborationSite = siteService.createSite("preset", COLLABORATION_SITE_ID, "title", "description", SiteVisibility.PRIVATE);
        documentLibrary = SiteServiceImpl.getSiteContainer(
                                COLLABORATION_SITE_ID, 
                                SiteService.DOCUMENT_LIBRARY,
                                true,
                                siteService,
                                transactionService,
                                taggingService);
        
        assertNotNull("Collaboration site document library component was not successfully created.", documentLibrary);
        
        // create a folder and documents
        dmFolder = fileFolderService.create(documentLibrary, "collabFolder", ContentModel.TYPE_FOLDER).getNodeRef();
        dmDocument = fileFolderService.create(dmFolder, "collabDocument.txt", ContentModel.TYPE_CONTENT).getNodeRef();
    }
    
    @Override
    protected void setupTestUsersImpl(NodeRef filePlan)
    {
        super.setupTestUsersImpl(filePlan);
        
        dmUserName = GUID.generate();
        dmUserPerson = createPerson(dmUserName);
        siteService.setMembership(COLLABORATION_SITE_ID, dmUserName, SiteModel.SITE_COLLABORATOR);
    }
    
    @Override
    protected void tearDownImpl()
    {
        super.tearDownImpl();        
        siteService.deleteSite(COLLABORATION_SITE_ID);
    }
    
    public void testCreateRecordAction()
    {
        doTestInTransaction(new Test<Void>()
        {
            public Void run()
            {
                assertEquals(AccessStatus.DENIED, dmPermissionService.hasPermission(dmDocument, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, dmPermissionService.hasPermission(filePlan, RMPermissionModel.VIEW_RECORDS));
                
                Action action = dmActionService.createAction(CreateRecordAction.NAME);
                dmActionService.executeAction(action, dmDocument);
                
                return null;
            }
            
            public void test(Void result) throws Exception 
            {
                assertEquals(AccessStatus.ALLOWED, dmPermissionService.hasPermission(dmDocument, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.ALLOWED, dmPermissionService.hasPermission(filePlan, RMPermissionModel.VIEW_RECORDS));
                
                assertTrue(rmService.isRecord(dmDocument));
                
                // 
                Capability createCapability = capabilityService.getCapability("Create");
                assertNotNull(createCapability);
                createCapability.evaluate(dmDocument);
                
                
                
            };
        }, 
        dmUserName);  
        
        doTestInTransaction(new Test<Void>()
        {
            public Void run()
            {

                return null;
            }            
        }); 
    }
        
}
