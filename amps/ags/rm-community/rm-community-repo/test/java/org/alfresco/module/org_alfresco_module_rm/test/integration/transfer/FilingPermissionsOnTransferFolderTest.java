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

package org.alfresco.module.org_alfresco_module_rm.test.integration.transfer;

import static org.alfresco.module.org_alfresco_module_rm.action.impl.CompleteEventAction.PARAM_EVENT_NAME;
import static org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService.ROLE_RECORDS_MANAGER;
import static org.alfresco.module.org_alfresco_module_rm.test.util.CommonRMTestUtils.DEFAULT_DISPOSITION_AUTHORITY;
import static org.alfresco.module.org_alfresco_module_rm.test.util.CommonRMTestUtils.DEFAULT_DISPOSITION_INSTRUCTIONS;
import static org.alfresco.module.org_alfresco_module_rm.test.util.CommonRMTestUtils.DEFAULT_EVENT_NAME;
import static org.alfresco.repo.security.authentication.AuthenticationUtil.getAdminUserName;
import static org.alfresco.repo.security.authentication.AuthenticationUtil.runAs;
import static org.alfresco.repo.site.SiteModel.SITE_CONSUMER;
import static org.alfresco.service.cmr.security.AccessStatus.ALLOWED;
import static org.alfresco.util.GUID.generate;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.action.impl.CompleteEventAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.CutOffAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.TransferAction;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Test case which shows that the user who did not create a transfer folder will
 * be able to see and action on it if he gets filing permission on the transfer folder.
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class FilingPermissionsOnTransferFolderTest extends BaseRMTestCase
{
    // Test users
    private String testUser1 = null;
    private String testUser2 = null;

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#isUserTest()
     */
    @Override
    protected boolean isUserTest()
    {
        return true;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#setupTestUsersImpl(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void setupTestUsersImpl(NodeRef filePlan)
    {
        super.setupTestUsersImpl(filePlan);

        // Create test users
        testUser1 = generate();
        createPerson(testUser1);
        testUser2 = generate();
        createPerson(testUser2);

        // Join the RM site
        siteService.setMembership(siteId, testUser1, SITE_CONSUMER);
        siteService.setMembership(siteId, testUser2, SITE_CONSUMER);

        // Add the test users to RM Records Manager role
        filePlanRoleService.assignRoleToAuthority(filePlan, ROLE_RECORDS_MANAGER, testUser1);
        filePlanRoleService.assignRoleToAuthority(filePlan, ROLE_RECORDS_MANAGER, testUser2);
    }

    public void testFilingPermissionsOnTransferFolder()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(testUser1)
        {
            // Records folder
            private NodeRef recordsFolder = null;

            // Transfer folder
            private NodeRef transferFolder = null;

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#given()
             */
            @Override
            public void given()
            {
                runAs(new RunAsWork<Void>()
                {
                    public Void doWork()
                    {
                        // Create category
                        NodeRef category = filePlanService.createRecordCategory(filePlan, generate());

                        // Give filing permissions for the test users on the category
                        filePlanPermissionService.setPermission(category, testUser1, FILING);
                        filePlanPermissionService.setPermission(category, testUser2, FILING);

                        // Create disposition schedule
                        utils.createDispositionSchedule(category, DEFAULT_DISPOSITION_INSTRUCTIONS, DEFAULT_DISPOSITION_AUTHORITY, false, true, true);

                        // Create folder
                        recordsFolder = recordFolderService.createRecordFolder(category, generate());

                        // Make eligible for cut off
                        Map<String, Serializable> params = new HashMap<>(1);
                        params.put(PARAM_EVENT_NAME, DEFAULT_EVENT_NAME);
                        rmActionService.executeRecordsManagementAction(recordsFolder, CompleteEventAction.NAME, params);

                        // Cut off folder
                        rmActionService.executeRecordsManagementAction(recordsFolder, CutOffAction.NAME);

                        return null;
                    }
                }, getAdminUserName());

                // FIXME: This step should be executed in "when()".
                // See RM-3931
                transferFolder = (NodeRef) rmActionService.executeRecordsManagementAction(recordsFolder, TransferAction.NAME).getValue();
            }

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#when()
             */
            @Override
            public void when()
            {
                // FIXME: If the transfer step is executed here the test fails. See RM-3931
                //transferFolder = (NodeRef) rmActionService.executeRecordsManagementAction(recordsFolder, TransferAction.NAME).getValue();

                // Give testUser2 filing permissions on transfer folder
                filePlanPermissionService.setPermission(transferFolder, testUser2, FILING);
            }

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#then()
             */
            @Override
            public void then()
            {
                // Check transfer folder
                assertNotNull(transferFolder);

                // testUser1 should have read permissions on the transfers container
                assertEquals(ALLOWED, permissionService.hasPermission(transfersContainer, READ_RECORDS));

                // Check if testUser1 has filing permissions on the transfer folder
                assertEquals(ALLOWED, permissionService.hasPermission(transferFolder, FILING));

                runAs(new RunAsWork<Void>()
                {
                    public Void doWork()
                    {
                        // Check transfer folder
                        assertNotNull(transferFolder);

                        // testUser2 should have read permissions on the transfers container
                        assertEquals(ALLOWED, permissionService.hasPermission(transfersContainer, READ_RECORDS));

                        // Check if testUser2 has read permissions on the transfer folder
                        assertEquals(ALLOWED, permissionService.hasPermission(transferFolder, READ_RECORDS));

                        // Check if testUser2 has filing permissions on the transfer folder
                        assertEquals(ALLOWED,  permissionService.hasPermission(transferFolder, FILING));

                        // FIXME: Should be able to execute the action. Failing intermittently. See RM-3931
                        // Execute transfer complete action as testUser2 who has filing permissions on the transfer folder
                        // rmActionService.executeRecordsManagementAction(transferFolder, TransferCompleteAction.NAME);

                        return null;
                    }
                }, testUser2);
            }
        });
    }
}
