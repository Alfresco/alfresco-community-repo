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
package org.alfresco.module.org_alfresco_module_rm.test.integration.issue;

import static com.google.common.collect.Sets.newHashSet;
import static org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService.ROLE_USER;
import static org.alfresco.util.GUID.generate;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Integration test for RM-2260
 *
 * @author Tuna Aksoy
 * @since 3.0
 */
public class RM2260Test extends BaseRMTestCase
{
    private static final String LEVEL = "level1";
    private static final String REASON = "Test Reason 1";

    public void testClassifiyingContentAsNonAdminUser()
    {
        /**
         * Given that a user (assigned to an RM role) exists
         * When filing permissions on a root category and the security clearance for that user are set
         * Then the user should be able to classify a record within a folder which is within the given category
         */
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            String myUser;
            NodeRef category;
            NodeRef folder;
            NodeRef record;

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#given()
             */
            @Override
            public void given() throws Exception
            {
                myUser = generate();
                createPerson(myUser);
                filePlanRoleService.assignRoleToAuthority(filePlan, ROLE_USER, myUser);

                category = filePlanService.createRecordCategory(filePlan, generate());
                folder = recordFolderService.createRecordFolder(category, generate());
                record = utils.createRecord(folder, generate());
            }

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#when()
             */
            @Override
            public void when() throws Exception
            {
                filePlanPermissionService.setPermission(category, myUser, FILING);
                securityClearanceService.setUserSecurityClearance(myUser, LEVEL);
            }

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#then()
             */
            @Override
            public void then() throws Exception
            {
                doTestInTransaction(new Test<Void>()
                {
                    @Override
                    public Void run()
                    {
                        contentClassificationService.classifyContent(LEVEL, generate(), newHashSet(REASON), record);
                        return null;
                    }
                }, myUser);
            }
        });
    }
}
