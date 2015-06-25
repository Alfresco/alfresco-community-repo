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
import static org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService.ROLE_USER;
import static org.alfresco.util.GUID.generate;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Enforcement of classification when browsing records in the file plan
 *
 * @author Tuna Aksoy
 * @since 3.0
 */
public class RecordClassificationEnforcementTest extends BaseRMTestCase
{
    private static final String LEVEL1 = "level1";
    private static final String LEVEL2 = "level2";
    private static final String REASON = "Test Reason 1";

    public void testUserWithNoSecurityClearance()
    {
        /**
         * Given that a test user without security clearance exists
         * and the test user is added to the RM Users role
         * and a category, a folder and two records are created in the file plan
         *
         * When the test user is given read permissions on the category
         * and one of the records is classified with the highest security level
         *
         *
         * Then as the admin user I will see both records
         * and as the test user I will only see the unclassified record
         */
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private String myUser;
            private NodeRef category;
            private NodeRef folder;
            private NodeRef record1;
            private NodeRef record2;

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
                record1 = utils.createRecord(folder, generate());
                record2 = utils.createRecord(folder, generate());
            }

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#when()
             */
            @Override
            public void when() throws Exception
            {
                filePlanPermissionService.setPermission(category, myUser, READ_RECORDS);
                contentClassificationService.classifyContent(LEVEL1, generate(), newHashSet(REASON), record1);
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
                        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(folder);
                        assertEquals(2, childAssocs.size());

                        List<NodeRef> recordList = newArrayList(record1, record2);
                        assertTrue(recordList.contains(childAssocs.get(0).getChildRef()));
                        assertTrue(recordList.contains(childAssocs.get(1).getChildRef()));

                        return null;
                    }
                });

                doTestInTransaction(new Test<Void>()
                {
                    @Override
                    public Void run()
                    {
                        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(folder);
                        assertEquals(1, childAssocs.size());
                        assertEquals(record2, childAssocs.get(0).getChildRef());

                        return null;
                    }
                }, myUser);
            }
        });
    }

    public void testUserWithMidlevelSecurityClearance()
    {
        /**
         * Given that a test user with mid-level security clearance exists
         * and the test user is added to the RM Users role
         * and a category, a folder and three records are created in the file plan
         *
         * When the test user is given read permissions on the category
         * and one of the records is classified with the highest security level
         * and another record is classified with the mid-level security level
         *
         * Then as the admin user I will see all three records
         * and as the test user I will see the unclassified record
         * and the record with the mid-level classification
         * and I won't be able to see the record with the classification greater than my clearance level
         */
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private String myUser;
            private NodeRef category;
            private NodeRef folder;
            private NodeRef record1;
            private NodeRef record2;
            private NodeRef record3;

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#given()
             */
            @Override
            public void given() throws Exception
            {
                myUser = generate();
                createPerson(myUser);
                filePlanRoleService.assignRoleToAuthority(filePlan, ROLE_USER, myUser);
                securityClearanceService.setUserSecurityClearance(myUser, LEVEL2);

                category = filePlanService.createRecordCategory(filePlan, generate());
                folder = recordFolderService.createRecordFolder(category, generate());
                record1 = utils.createRecord(folder, generate());
                record2 = utils.createRecord(folder, generate());
                record3 = utils.createRecord(folder, generate());
            }

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#when()
             */
            @Override
            public void when() throws Exception
            {
                filePlanPermissionService.setPermission(category, myUser, READ_RECORDS);
                contentClassificationService.classifyContent(LEVEL1, generate(), newHashSet(REASON), record1);
                contentClassificationService.classifyContent(LEVEL2, generate(), newHashSet(REASON), record2);
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
                        List<ChildAssociationRef> childAssociationRefs = nodeService.getChildAssocs(folder);
                        assertNotNull(childAssociationRefs);
                        assertEquals(3, childAssociationRefs.size());

                        ArrayList<NodeRef> docs = newArrayList(record1, record2, record3);
                        assertTrue(docs.contains(childAssociationRefs.get(0).getChildRef()));
                        assertTrue(docs.contains(childAssociationRefs.get(1).getChildRef()));
                        assertTrue(docs.contains(childAssociationRefs.get(2).getChildRef()));

                        return null;
                    }
                });

                doTestInTransaction(new Test<Void>()
                {
                    @Override
                    public Void run()
                    {
                        List<ChildAssociationRef> childAssociationRefs = nodeService.getChildAssocs(folder);
                        assertNotNull(childAssociationRefs);
                        assertEquals(2, childAssociationRefs.size());

                        ArrayList<NodeRef> docs = newArrayList(record2, record3);
                        assertTrue(docs.contains(childAssociationRefs.get(0).getChildRef()));
                        assertTrue(docs.contains(childAssociationRefs.get(1).getChildRef()));

                        return null;
                    }
                }, myUser);
            }
        });
    }

    public void testUseWithHighestLevelSecurityClearance()
    {
        /**
         * Given that a test user with highest level security clearance exists
         * and the test user is added to the RM Users role
         * and a category, a folder and three records are created in the file plan
         *
         * When the test user is given read permissions on the category
         * and one of the records is classified with the highest security level
         * and another record is classified with the mid-level security level
         *
         * Then as the admin user I will see all three records
         * and as the test user I will see all three records
         */
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private String myUser;
            private NodeRef category;
            private NodeRef folder;
            private NodeRef record1;
            private NodeRef record2;
            private NodeRef record3;

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#given()
             */
            @Override
            public void given() throws Exception
            {
                myUser = generate();
                createPerson(myUser);
                filePlanRoleService.assignRoleToAuthority(filePlan, ROLE_USER, myUser);
                securityClearanceService.setUserSecurityClearance(myUser, LEVEL1);

                category = filePlanService.createRecordCategory(filePlan, generate());
                folder = recordFolderService.createRecordFolder(category, generate());
                record1 = utils.createRecord(folder, generate());
                record2 = utils.createRecord(folder, generate());
                record3 = utils.createRecord(folder, generate());
            }

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#when()
             */
            @Override
            public void when() throws Exception
            {
                filePlanPermissionService.setPermission(category, myUser, READ_RECORDS);
                contentClassificationService.classifyContent(LEVEL1, generate(), newHashSet(REASON), record1);
                contentClassificationService.classifyContent(LEVEL2, generate(), newHashSet(REASON), record2);
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
                        List<ChildAssociationRef> childAssociationRefs = nodeService.getChildAssocs(folder);
                        assertNotNull(childAssociationRefs);
                        assertEquals(3, childAssociationRefs.size());

                        ArrayList<NodeRef> docs = newArrayList(record1, record2, record3);
                        assertTrue(docs.contains(childAssociationRefs.get(0).getChildRef()));
                        assertTrue(docs.contains(childAssociationRefs.get(1).getChildRef()));
                        assertTrue(docs.contains(childAssociationRefs.get(2).getChildRef()));

                        return null;
                    }
                });

                doTestInTransaction(new Test<Void>()
                {
                    @Override
                    public Void run()
                    {
                        List<ChildAssociationRef> childAssociationRefs = nodeService.getChildAssocs(folder);
                        assertNotNull(childAssociationRefs);
                        assertEquals(3, childAssociationRefs.size());

                        ArrayList<NodeRef> docs = newArrayList(record1, record2, record3);
                        assertTrue(docs.contains(childAssociationRefs.get(0).getChildRef()));
                        assertTrue(docs.contains(childAssociationRefs.get(1).getChildRef()));
                        assertTrue(docs.contains(childAssociationRefs.get(2).getChildRef()));

                        return null;
                    }
                }, myUser);
            }
        });
    }
}
