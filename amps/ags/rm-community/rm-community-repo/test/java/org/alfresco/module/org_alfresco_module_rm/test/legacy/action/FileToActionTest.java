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

package org.alfresco.module.org_alfresco_module_rm.test.legacy.action;

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
import org.alfresco.repo.security.authentication.AuthenticationUtil;
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
    private static final String PATH = "rmContainer/rmFolder";
    private static final String PATH2 = "/rmContainer/rmFolder";
    private static final String PATH_BAD = "monkey/rmFolder";
    private static final String PATH_CREATE = "rmContainer/newRmFolder";
    private static final String LONG_PATH_CREATE = "/rmContainer/one/two/three/four/newRmFolder";

    private static final String PATH_SUB1 = "rmContainer/${node.cm:title}";

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
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            @Override
            public void given() throws Exception
            {
                initRecord();

                NodeRef unfiledContainer = filePlanService.getUnfiledContainer(filePlan);
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(unfiledContainer, RMPermissionModel.FILING));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(rmFolder, RMPermissionModel.FILING));

                Capability capability = capabilityService.getCapability("FileUnfiledRecords");
                assertNotNull(capability);
                assertEquals(AccessStatus.ALLOWED, capability.hasPermission(dmDocument));
            }

            @Override
            public void when() throws Exception
            {
                // set parameters
                Map<String, Serializable> params = new HashMap<>(1);
                params.put(FileToAction.PARAM_DESTINATION_RECORD_FOLDER, rmFolder);

                // execute file-to action
                rmActionService.executeRecordsManagementAction(dmDocument, FileToAction.NAME, params);
            }

            @Override
            public void then() throws Exception
            {
                // check things have gone according to plan
                assertTrue(recordService.isRecord(dmDocument));
                assertTrue(recordService.isFiled(dmDocument));

                // is the record folder the primary parent of the filed record
                NodeRef parent = nodeService.getPrimaryParent(dmDocument).getParentRef();
                assertEquals(rmFolder, parent);
            }
        });
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

                return null;
            }

            @Override
            public void test(Void result) throws Exception 
            {
                AuthenticationUtil.runAs(() ->
                {
                    // check things have gone according to plan
                    assertTrue(recordService.isRecord(dmDocument));
                    assertFalse(recordService.isFiled(dmDocument));

                    // is the unfiled container the primary parent of the filed
                    // record
                    NodeRef parent = nodeService.getPrimaryParent(dmDocument).getParentRef();
                    assertEquals(filePlanService.getUnfiledContainer(filePlan), parent);

                    return null;
                }, AuthenticationUtil.getAdminUserName());
            }
            
        }, dmCollaborator);
    }

    public void testFileToPath()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            @Override
            public void given() throws Exception
            {
                initRecord();
            }

            @Override
            public void when() throws Exception
            {
                // set parameters
                Map<String, Serializable> params = new HashMap<>(1);
                params.put(FileToAction.PARAM_PATH, PATH);

                // execute file-to action
                rmActionService.executeRecordsManagementAction(dmDocument, FileToAction.NAME, params);
            }

            @Override
            public void then() throws Exception
            {
                // check things have gone according to plan
                assertTrue(recordService.isRecord(dmDocument));
                assertTrue(recordService.isFiled(dmDocument));

                // is the record folder the primary parent of the filed record
                NodeRef parent = nodeService.getPrimaryParent(dmDocument).getParentRef();
                assertEquals(rmFolder, parent);
            }
        });
    }

    public void testFileToPath2()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            @Override
            public void given() throws Exception
            {
                initRecord();
            }

            @Override
            public void when() throws Exception
            {
                // set parameters
                Map<String, Serializable> params = new HashMap<>(1);
                params.put(FileToAction.PARAM_PATH, PATH2);

                // execute file-to action
                rmActionService.executeRecordsManagementAction(dmDocument, FileToAction.NAME, params);
            }

            @Override
            public void then() throws Exception
            {
                // check things have gone according to plan
                assertTrue(recordService.isRecord(dmDocument));
                assertTrue(recordService.isFiled(dmDocument));

                // is the record folder the primary parent of the filed record
                NodeRef parent = nodeService.getPrimaryParent(dmDocument).getParentRef();
                assertEquals(rmFolder, parent);
            }
        });
    }

    public void testCreate() throws Exception
    {
        initRecord();
        createRecord(PATH_CREATE, "newRmFolder");
    }

    public void testCreateSub() throws Exception
    {
        initRecord();
        createRecord(PATH_SUB1, "mytestvalue", "rmContainer/mytestvalue");
    }

    public void testCreatePath() throws Exception
    {
        initRecord();
        createRecord(LONG_PATH_CREATE, "newRmFolder", "rmContainer/one/two/three/four/newRmFolder");
    }

    private void createRecord(String path, String name)
    {
        createRecord(path, name, path);
    }

    private void createRecord(final String path, final String name, final String resolvedPath)
    {
        final String[] pathValues = StringUtils.tokenizeToStringArray(resolvedPath, "/");

        // set parameters
        Map<String, Serializable> params = new HashMap<>(1);
        params.put(FileToAction.PARAM_PATH, path);
        params.put(FileToAction.PARAM_CREATE_RECORD_PATH, true);

        doTestInTransaction(new Test<Void>()
        {
            public Void run() throws Exception
            {
                // show the folder doesn't exist to begin with
                FileInfo createdRecordFolder = fileFolderService.resolveNamePath(filePlan, new ArrayList<>(Arrays.asList(pathValues)), false);
                assertNull(createdRecordFolder);

                // set parameters
                Map<String, Serializable> params = new HashMap<>(1);
                params.put(FileToAction.PARAM_PATH, path);
                params.put(FileToAction.PARAM_CREATE_RECORD_PATH, true);

                return null;
            }
        }, ADMIN_USER);

        doTestInTransaction(new Test<Void>()
        {
            public Void run() throws Exception
            {
                // execute file-to action
                rmActionService.executeRecordsManagementAction(dmDocument, FileToAction.NAME, params);
                return null;
            }
        });

        doTestInTransaction(new Test<Void>()
        {
            public Void run() throws Exception
            {
                // show the folder has now been created
            	FileInfo createdRecordFolder = fileFolderService.resolveNamePath(filePlan, new ArrayList<>(Arrays.asList(pathValues)), false);
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
        }, ADMIN_USER);
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
                Map<String, Serializable> params = new HashMap<>(1);
                params.put(FileToAction.PARAM_PATH, PATH_BAD);

                // execute file-to action
                rmActionService.executeRecordsManagementAction(dmDocument, FileToAction.NAME, params);

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
                Map<String, Serializable> params = new HashMap<>(1);
                params.put(FileToAction.PARAM_PATH, PATH_CREATE);

                // execute file-to action
                rmActionService.executeRecordsManagementAction(dmDocument, FileToAction.NAME, params);

            }
        });

    }

}
