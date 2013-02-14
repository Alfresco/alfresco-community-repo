/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.test.action;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.action.impl.FileToAction;
import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;

/**
 * FileTo action unit test
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class FileToActionTest extends BaseRMTestCase
{
    private static final String PATH = "rmcontainer/rmfolder";
    private static final String PATH2 = "/rmcontainer/rmfolder";
    
    protected ActionService dmActionService;

    @Override
    protected void initServices()
    {
        super.initServices();
        dmActionService = (ActionService) applicationContext.getBean("ActionService");
    }
    
    @Override
    protected boolean isCollaborationSiteTest()
    {
        return true;
    }
    
    @Override
    protected boolean isUserTest()
    {
        return true;
    }
    
    public void testFileToNodeRef()
    {
        doTestInTransaction(new Test<Void>()
        {
            public Void run()
            {
                // create record from document
                recordService.createRecord(filePlan, dmDocument);
                
                // check things have gone according to plan
                assertTrue(recordService.isRecord(dmDocument));
                assertFalse(recordService.isFiled(dmDocument));
                
                // is the unfiled container the primary parent of the filed record
                NodeRef parent = nodeService.getPrimaryParent(dmDocument).getParentRef();
                assertEquals(filePlanService.getUnfiledContainer(filePlan), parent);
                
                return null;
            }
        }, dmCollaborator);
        
        doTestInTransaction(new Test<Void>()
        {
            public Void run()
            {
                NodeRef unfiledContainer = filePlanService.getUnfiledContainer(filePlan);                
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(unfiledContainer, RMPermissionModel.FILING));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(rmFolder, RMPermissionModel.FILING));
                
                Capability capability = capabilityService.getCapability("FileToRecords");
                assertNotNull(capability);
                assertEquals(AccessStatus.ALLOWED, capability.hasPermission(dmDocument));
                
                // set parameters
                Map<String, Serializable> params = new HashMap<String, Serializable>(1);
                params.put(FileToAction.PARAM_DESTINATION_RECORD_FOLDER, rmFolder);
                
                // execute file-to action
                actionService.executeRecordsManagementAction(dmDocument, FileToAction.NAME, params);
                
                // check things have gone according to plan
                assertTrue(recordService.isRecord(dmDocument));
                assertTrue(recordService.isFiled(dmDocument));
                
                // is the record folder the primary parent of the filed record
                NodeRef parent = nodeService.getPrimaryParent(dmDocument).getParentRef();
                assertEquals(rmFolder, parent);
                
                return null;
            }
        }, rmAdminName);
        
    }
    
    public void testFileToPath()
    {
        doTestInTransaction(new Test<Void>()
            {
            public Void run()
            {
                // create record from document
                recordService.createRecord(filePlan, dmDocument);
                
                // check things have gone according to plan
                assertTrue(recordService.isRecord(dmDocument));
                assertFalse(recordService.isFiled(dmDocument));
                
                // is the unfiled container the primary parent of the filed record
                NodeRef parent = nodeService.getPrimaryParent(dmDocument).getParentRef();
                assertEquals(filePlanService.getUnfiledContainer(filePlan), parent);
                
                return null;
            }
        }, dmCollaborator);
        
        doTestInTransaction(new Test<Void>()
        {
            public Void run()
            {
                // set parameters
                Map<String, Serializable> params = new HashMap<String, Serializable>(1);
                params.put(FileToAction.PARAM_PATH, PATH);
                
                // execute file-to action
                actionService.executeRecordsManagementAction(dmDocument, FileToAction.NAME, params);
                
                // check things have gone according to plan
                assertTrue(recordService.isRecord(dmDocument));
                assertTrue(recordService.isFiled(dmDocument));
                
                // is the record folder the primary parent of the filed record
                NodeRef parent = nodeService.getPrimaryParent(dmDocument).getParentRef();
                assertEquals(rmFolder, parent);
                
                return null;
            }
        }, rmAdminName);        
    }
    
    public void testFileToPath2()
    {
        doTestInTransaction(new Test<Void>()
            {
            public Void run()
            {
                // create record from document
                recordService.createRecord(filePlan, dmDocument);
                
                // check things have gone according to plan
                assertTrue(recordService.isRecord(dmDocument));
                assertFalse(recordService.isFiled(dmDocument));
                
                // is the unfiled container the primary parent of the filed record
                NodeRef parent = nodeService.getPrimaryParent(dmDocument).getParentRef();
                assertEquals(filePlanService.getUnfiledContainer(filePlan), parent);
                
                return null;
            }
        }, dmCollaborator);
        
        doTestInTransaction(new Test<Void>()
        {
            public Void run()
            {
                // set parameters
                Map<String, Serializable> params = new HashMap<String, Serializable>(1);
                params.put(FileToAction.PARAM_PATH, PATH2);
                
                // execute file-to action
                actionService.executeRecordsManagementAction(dmDocument, FileToAction.NAME, params);
                
                // check things have gone according to plan
                assertTrue(recordService.isRecord(dmDocument));
                assertTrue(recordService.isFiled(dmDocument));
                
                // is the record folder the primary parent of the filed record
                NodeRef parent = nodeService.getPrimaryParent(dmDocument).getParentRef();
                assertEquals(rmFolder, parent);
                
                return null;
            }
        }, rmAdminName);        
    }

}
