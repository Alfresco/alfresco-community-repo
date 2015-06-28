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
package org.alfresco.module.org_alfresco_module_rm.test.integration.classification.interceptor;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService.ROLE_ADMIN;
import static org.alfresco.util.GUID.generate;

import java.util.List;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Enforcement of classification when browsing records in the file plan
 *
 * @author Tuna Aksoy
 * @since 3.0
 */
public class RecordBrowseClassificationEnforcementTest extends BrowseClassificationEnforcementTestBase
{
    public void testUserWithNoSecurityClearance()
    {
        /**
         * Given that a test user without security clearance exists
         * and the test user is added to the RM Admin role
         * and a category, a folder and two records are created in the file plan
         * and one of the records is classified with the highest security level
         *
         * When I browse the file plan as admin
         * Then I will see both documents
         *
         * When I browse the file plan as the test user
         * Then I will only see the unclassified record
         */
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef category;
            private NodeRef folder;
            private NodeRef record1;
            private NodeRef record2;
            private List<ChildAssociationRef> resultsForAdmin;
            private List<ChildAssociationRef> resultsForTestUser;

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#given()
             */
            @Override
            public void given() throws Exception
            {
                testUser = generate();
                createPerson(testUser);
                filePlanRoleService.assignRoleToAuthority(filePlan, ROLE_ADMIN, testUser);

                category = filePlanService.createRecordCategory(filePlan, generate());
                folder = recordFolderService.createRecordFolder(category, generate());
                record1 = utils.createRecord(folder, generate());
                record2 = utils.createRecord(folder, generate());

                contentClassificationService.classifyContent(LEVEL1, generate(), newHashSet(REASON), record1);
            }

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#when()
             */
            @Override
            public void when() throws Exception
            {
                resultsForAdmin = browseAsAdmin(folder);
                resultsForTestUser = browseAsTestUser(folder);
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
                        assertNotNull(resultsForAdmin);
                        assertEquals(2, resultsForAdmin.size());

                        List<NodeRef> records = newArrayList(record1, record2);
                        assertTrue(records.contains(resultsForAdmin.get(0).getChildRef()));
                        assertTrue(records.contains(resultsForAdmin.get(1).getChildRef()));

                        return null;
                    }
                });

                doTestInTransaction(new Test<Void>()
                {
                    @Override
                    public Void run()
                    {
                        assertNotNull(resultsForTestUser);
                        assertEquals(1, resultsForTestUser.size());
                        assertEquals(record2, resultsForTestUser.get(0).getChildRef());

                        return null;
                    }
                }, testUser);
            }
        });
    }

    public void testUserWithMidlevelSecurityClearance()
    {
        /**
         * Given that a test user with mid-level security clearance exists
         * and the test user is added to the RM Admin role
         * and a category, a folder and three records are created in the file plan
         * and one of the records is classified with the highest security level
         * and another record is classified with the mid-level security level
         *
         * When I browse the file plan as admin
         * Then I will see all three records
         *
         * When I browse the file plan as the test user
         * Then I will see the unclassified record
         * and the record with the mid-level classification
         * and I won't be able to see the record with the classification greater than my clearance level
         */
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef category;
            private NodeRef folder;
            private NodeRef record1;
            private NodeRef record2;
            private NodeRef record3;
            private List<ChildAssociationRef> resultsForAdmin;
            private List<ChildAssociationRef> resultsForTestUser;

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#given()
             */
            @Override
            public void given() throws Exception
            {
                testUser = generate();
                createPerson(testUser);
                filePlanRoleService.assignRoleToAuthority(filePlan, ROLE_ADMIN, testUser);
                securityClearanceService.setUserSecurityClearance(testUser, LEVEL2);

                category = filePlanService.createRecordCategory(filePlan, generate());
                folder = recordFolderService.createRecordFolder(category, generate());
                record1 = utils.createRecord(folder, generate());
                record2 = utils.createRecord(folder, generate());
                record3 = utils.createRecord(folder, generate());

                contentClassificationService.classifyContent(LEVEL1, generate(), newHashSet(REASON), record1);
                contentClassificationService.classifyContent(LEVEL2, generate(), newHashSet(REASON), record2);
            }

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#when()
             */
            @Override
            public void when() throws Exception
            {
                resultsForAdmin = browseAsAdmin(folder);
                resultsForTestUser = browseAsTestUser(folder);
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
                        assertNotNull(resultsForAdmin);
                        assertEquals(3, resultsForAdmin.size());

                        List<NodeRef> records = newArrayList(record1, record2, record3);
                        assertTrue(records.contains(resultsForAdmin.get(0).getChildRef()));
                        assertTrue(records.contains(resultsForAdmin.get(1).getChildRef()));
                        assertTrue(records.contains(resultsForAdmin.get(2).getChildRef()));

                        return null;
                    }
                });

                doTestInTransaction(new Test<Void>()
                {
                    @Override
                    public Void run()
                    {
                        assertNotNull(resultsForTestUser);
                        assertEquals(2, resultsForTestUser.size());

                        List<NodeRef> records = newArrayList(record2, record3);
                        assertTrue(records.contains(resultsForTestUser.get(0).getChildRef()));
                        assertTrue(records.contains(resultsForTestUser.get(1).getChildRef()));

                        return null;
                    }
                }, testUser);
            }
        });
    }

    public void testUseWithHighestLevelSecurityClearance()
    {
        /**
         * Given that a test user with highest level security clearance exists
         * and the test user is added to the RM Admin role
         * and a category, a folder and three records are created in the file plan
         * and one of the records is classified with the highest security level
         * and another record is classified with the mid-level security level
         *
         * When I browse the file plan as admin
         * The I will see all three records
         *
         * When I browse the file plan as the test user
         * The I will see all three records
         */
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef category;
            private NodeRef folder;
            private NodeRef record1;
            private NodeRef record2;
            private NodeRef record3;
            private List<ChildAssociationRef> resultsForAdmin;
            private List<ChildAssociationRef> resultsForTestUser;

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#given()
             */
            @Override
            public void given() throws Exception
            {
                testUser = generate();
                createPerson(testUser);
                filePlanRoleService.assignRoleToAuthority(filePlan, ROLE_ADMIN, testUser);
                securityClearanceService.setUserSecurityClearance(testUser, LEVEL1);

                category = filePlanService.createRecordCategory(filePlan, generate());
                folder = recordFolderService.createRecordFolder(category, generate());
                record1 = utils.createRecord(folder, generate());
                record2 = utils.createRecord(folder, generate());
                record3 = utils.createRecord(folder, generate());

                contentClassificationService.classifyContent(LEVEL1, generate(), newHashSet(REASON), record1);
                contentClassificationService.classifyContent(LEVEL2, generate(), newHashSet(REASON), record2);
            }

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#when()
             */
            @Override
            public void when() throws Exception
            {
                resultsForAdmin = browseAsAdmin(folder);
                resultsForTestUser = browseAsTestUser(folder);
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
                        assertNotNull(resultsForAdmin);
                        assertEquals(3, resultsForAdmin.size());

                        List<NodeRef> records = newArrayList(record1, record2, record3);
                        assertTrue(records.contains(resultsForAdmin.get(0).getChildRef()));
                        assertTrue(records.contains(resultsForAdmin.get(1).getChildRef()));
                        assertTrue(records.contains(resultsForAdmin.get(2).getChildRef()));

                        return null;
                    }
                });

                doTestInTransaction(new Test<Void>()
                {
                    @Override
                    public Void run()
                    {
                        assertNotNull(resultsForTestUser);
                        assertEquals(3, resultsForTestUser.size());

                        List<NodeRef> records = newArrayList(record1, record2, record3);
                        assertTrue(records.contains(resultsForTestUser.get(0).getChildRef()));
                        assertTrue(records.contains(resultsForTestUser.get(1).getChildRef()));
                        assertTrue(records.contains(resultsForTestUser.get(2).getChildRef()));

                        return null;
                    }
                }, testUser);
            }
        });
    }
}
