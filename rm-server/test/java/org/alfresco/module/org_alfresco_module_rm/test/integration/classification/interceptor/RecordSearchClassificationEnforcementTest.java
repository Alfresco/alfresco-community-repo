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

import static org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService.ROLE_ADMIN;
import static org.alfresco.util.GUID.generate;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.search.RecordsManagementSearchParameters;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

/**
 * Enforcement of classification when searching records in the file plan
 *
 * @author Tuna Aksoy
 * @since 2.4.a
 */
public class RecordSearchClassificationEnforcementTest extends SearchClassificationEnforcementTestBase
{
    public void testUserWithNoSecurityClearance()
    {
        /**
         * Given that a test user without security clearance exists
         * and the test user is added to the RM Admin role
         * and a category, a folder and two records are created in the file plan
         * and one of the records is classified with the highest security level
         *
         * When I search for the records as admin
         * Then I will see both records
         *
         * When I search for the records as the test user
         * Then I will only see the unclassified record
         */
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef category;
            private NodeRef folder;
            private NodeRef record1;
            private NodeRef record2;
            private String searchQuery = generate();
            private List<NodeRef> resultsForAdmin;
            private List<NodeRef> resultsForTestUser;

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
                record1 = utils.createRecord(folder, searchQuery + generate());
                record2 = utils.createRecord(folder, searchQuery + generate());

                contentClassificationService.classifyContent(propertiesDTO1, record1);
            }

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#when()
             */
            @Override
            public void when() throws Exception
            {
                resultsForAdmin = searchAsAdmin(searchQuery);
                resultsForTestUser = searchAsTestUser(searchQuery);
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
                        assertTrue(resultsForAdmin.contains(record1));
                        assertTrue(resultsForAdmin.contains(record2));

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
                        assertTrue(resultsForTestUser.contains(record2));

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
         * When I search for the records as admin
         * The I will see all three records
         *
         * When I search for the records as the test user
         * Then I will see the unclassified document
         * and the document with the mid-level classification
         * and I won't be able to see the document with the classification greater than my clearance level
         */
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef category;
            private NodeRef folder;
            private NodeRef record1;
            private NodeRef record2;
            private NodeRef record3;
            private String searchQuery = generate();
            private List<NodeRef> resultsForAdmin;
            private List<NodeRef> resultsForTestUser;

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#given()
             */
            @Override
            public void given() throws Exception
            {
                testUser = generate();
                createPerson(testUser);
                filePlanRoleService.assignRoleToAuthority(filePlan, ROLE_ADMIN, testUser);
                securityClearanceService.setUserSecurityClearance(testUser, SECRET_ID);

                category = filePlanService.createRecordCategory(filePlan, generate());
                folder = recordFolderService.createRecordFolder(category, generate());
                record1 = utils.createRecord(folder, searchQuery + generate());
                record2 = utils.createRecord(folder, searchQuery + generate());
                record3 = utils.createRecord(folder, searchQuery + generate());

                contentClassificationService.classifyContent(propertiesDTO1, record1);
                contentClassificationService.classifyContent(propertiesDTO2, record2);
            }

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#when()
             */
            @Override
            public void when() throws Exception
            {
                resultsForAdmin = searchAsAdmin(searchQuery);
                resultsForTestUser = searchAsTestUser(searchQuery);
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
                        assertTrue(resultsForAdmin.contains(record1));
                        assertTrue(resultsForAdmin.contains(record2));
                        assertTrue(resultsForAdmin.contains(record3));

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
                        assertTrue(resultsForTestUser.contains(record2));
                        assertTrue(resultsForTestUser.contains(record3));

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
         * When I search for the records as admin
         * The I will see all three records
         *
         * When I search for the records as the test user
         * The I will see all three records
         */
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef category;
            private NodeRef folder;
            private NodeRef record1;
            private NodeRef record2;
            private NodeRef record3;
            private String searchQuery = generate();
            private List<NodeRef> resultsForAdmin;
            private List<NodeRef> resultsForTestUser;

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#given()
             */
            @Override
            public void given() throws Exception
            {
                testUser = generate();
                createPerson(testUser);
                filePlanRoleService.assignRoleToAuthority(filePlan, ROLE_ADMIN, testUser);
                securityClearanceService.setUserSecurityClearance(testUser, TOP_SECRET_ID);

                category = filePlanService.createRecordCategory(filePlan, generate());
                folder = recordFolderService.createRecordFolder(category, generate());
                record1 = utils.createRecord(folder, searchQuery + generate());
                record2 = utils.createRecord(folder, searchQuery + generate());
                record3 = utils.createRecord(folder, searchQuery + generate());

                contentClassificationService.classifyContent(propertiesDTO1, record1);
                contentClassificationService.classifyContent(propertiesDTO2, record2);
            }

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#when()
             */
            @Override
            public void when() throws Exception
            {
                resultsForAdmin = searchAsAdmin(searchQuery);
                resultsForTestUser = searchAsTestUser(searchQuery);
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
                        assertTrue(resultsForAdmin.contains(record1));
                        assertTrue(resultsForAdmin.contains(record2));
                        assertTrue(resultsForAdmin.contains(record3));

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
                        assertTrue(resultsForTestUser.contains(record1));
                        assertTrue(resultsForTestUser.contains(record2));
                        assertTrue(resultsForTestUser.contains(record3));

                        return null;
                    }
                }, testUser);
            }
        });
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.integration.classification.interceptor.SearchClassificationEnforcementTestBase#search(java.lang.String)
     */
    @Override
    protected List<NodeRef> search(String searchQuery)
    {
        String query = "cm:name:" + searchQuery + "*";
        RecordsManagementSearchParameters searchParameters = new RecordsManagementSearchParameters();
        searchParameters.setIncludeUndeclaredRecords(true);
        List<Pair<NodeRef, NodeRef>> result = rmSearchService.search(siteId, query, searchParameters);

        List<NodeRef> filteredResult = new ArrayList<>();
        for (Pair<NodeRef, NodeRef> pair : result)
        {
            filteredResult.add(pair.getSecond());
        }

        return filteredResult;
    }
}
