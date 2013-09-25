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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.impl.FileToAction;
import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.springframework.util.StringUtils;

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
    private static final String PATH_BAD = "monkey/rmfolder";
    private static final String PATH_CREATE = "rmcontainer/newrmfolder";

    private static final String PATH_SUB1 = "rmcontainer/${node.cm:title}";

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
        initRecord();

        doTestInTransaction(new Test<Void>()
        {
            public Void run()
            {
                NodeRef unfiledContainer = filePlanService.getUnfiledContainer(filePlan);
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(unfiledContainer, RMPermissionModel.FILING));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(rmFolder, RMPermissionModel.FILING));

                Capability capability = capabilityService.getCapability("FileUnfiledRecords");
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

    private void initRecord()
    {
        doTestInTransaction(new Test<Void>()
        {
            public Void run()
            {
                nodeService.setProperty(dmDocument, ContentModel.PROP_TITLE, "mytestvalue");
                
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
    }

    public void testFileToPath()
    {
        initRecord();

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
        initRecord();

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

    public void testCreate() throws Exception
    {
        initRecord();
        createRecord(PATH_CREATE, "newrmfolder");
    }

    public void testCreateSub() throws Exception
    {
        initRecord();
        createRecord(PATH_SUB1, "mytestvalue", "rmcontainer/mytestvalue");
    }

    private void createRecord(String path, String name)
    {
        createRecord(path, name, path);
    }

    private void createRecord(final String path, final String name, final String resolvedPath)
    {
        doTestInTransaction(new Test<Void>()
        {
            public Void run() throws Exception
            {
                String[] pathValues = StringUtils.tokenizeToStringArray(resolvedPath, "/");

                // show the folder doesn't exist to begin with
                FileInfo createdRecordFolder = fileFolderService.resolveNamePath(filePlan, new ArrayList<String>(Arrays.asList(pathValues)), false);
                assertNull(createdRecordFolder);

                // set parameters
                Map<String, Serializable> params = new HashMap<String, Serializable>(1);
                params.put(FileToAction.PARAM_PATH, path);
                params.put(FileToAction.PARAM_CREATE_RECORD_FOLDER, true);

                // execute file-to action
                actionService.executeRecordsManagementAction(dmDocument, FileToAction.NAME, params);

                // show the folder has now been created
                createdRecordFolder = fileFolderService.resolveNamePath(filePlan, new ArrayList<String>(Arrays.asList(pathValues)), false);
                assertNotNull(createdRecordFolder);
                assertEquals(name, createdRecordFolder.getName());
                NodeRef createdRecordFolderNodeRef = createdRecordFolder.getNodeRef();

                // check things have gone according to plan
                assertTrue(recordService.isRecord(dmDocument));
                assertTrue(recordService.isFiled(dmDocument));

                // is the record folder the primary parent of the filed record
                NodeRef parent = nodeService.getPrimaryParent(dmDocument).getParentRef();
                assertEquals(createdRecordFolderNodeRef, parent);

                return null;
            }
        }, rmAdminName);
    }

    public void failureTests() throws Exception
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

        doTestInTransaction(new FailureTest
        (
            "Path is invalid and record create not set."
        )
        {
            @Override
            public void run() throws Exception
            {
                // set parameters
                Map<String, Serializable> params = new HashMap<String, Serializable>(1);
                params.put(FileToAction.PARAM_PATH, PATH_BAD);

                // execute file-to action
                actionService.executeRecordsManagementAction(dmDocument, FileToAction.NAME, params);

            }
        });

        doTestInTransaction(new FailureTest
        (
            "Path is for a new folder, but create not set."
        )
        {
            @Override
            public void run() throws Exception
            {
                // set parameters
                Map<String, Serializable> params = new HashMap<String, Serializable>(1);
                params.put(FileToAction.PARAM_PATH, PATH_CREATE);

                // execute file-to action
                actionService.executeRecordsManagementAction(dmDocument, FileToAction.NAME, params);

            }
        });

    }

}
