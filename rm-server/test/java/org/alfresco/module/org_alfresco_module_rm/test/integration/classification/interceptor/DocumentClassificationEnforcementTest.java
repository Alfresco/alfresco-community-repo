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
import static org.alfresco.repo.site.SiteModel.SITE_MANAGER;
import static org.alfresco.util.GUID.generate;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Tests for enforcement of classification when browsing documents in the document library
 *
 * @author Tuna Aksoy
 * @since 3.0
 */
public class DocumentClassificationEnforcementTest extends BaseRMTestCase
{
    private static final String LEVEL1 = "level1";
    private static final String LEVEL2 = "level2";
    private static final String REASON = "Test Reason 1";

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#isCollaborationSiteTest()
     */
    @Override
    protected boolean isCollaborationSiteTest()
    {
        return true;
    }

    public void testUserWithNoSecurityClearance()
    {
        /**
         * Given that a test user without security clearance exists
         * and two documents are created in the document library
         *
         * When one of the documents is classified with the highest security level
         *
         * Then as the admin user I will see both documents
         * and as the test user I will only see the unclassified document
         */
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private String myUser;
            private NodeRef folder;
            private NodeRef doc1;
            private NodeRef doc2;

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#given()
             */
            @Override
            public void given() throws Exception
            {
                myUser = generate();
                createPerson(myUser);
                siteService.setMembership(collabSiteId, myUser, SITE_MANAGER);

                folder = fileFolderService.create(documentLibrary, generate(), TYPE_FOLDER).getNodeRef();
                doc1 = fileFolderService.create(folder, generate(), TYPE_CONTENT).getNodeRef();
                doc2 = fileFolderService.create(folder, generate(), TYPE_CONTENT).getNodeRef();
            }

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#when()
             */
            @Override
            public void when() throws Exception
            {
                contentClassificationService.classifyContent(LEVEL1, generate(), newHashSet(REASON), doc1);
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
                        assertEquals(2, childAssociationRefs.size());

                        ArrayList<NodeRef> docs = newArrayList(doc1, doc2);
                        assertTrue(docs.contains(childAssociationRefs.get(0).getChildRef()));
                        assertTrue(docs.contains(childAssociationRefs.get(1).getChildRef()));

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
                        assertEquals(1, childAssociationRefs.size());
                        assertEquals(doc2, childAssociationRefs.get(0).getChildRef());

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
         * and three documents are created in the document library
         *
         * When one of the documents is classified with the highest security level
         * and another document is classified with the mid-level security level
         *
         * Then as the admin user I will see all three documents
         * and as the test user I will see the unclassified document
         * and the document with the mid-level classification
         * and I won't be able to see the document with the classification greater than my clearance level
         */
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private String myUser;
            private NodeRef folder;
            private NodeRef doc1;
            private NodeRef doc2;
            private NodeRef doc3;

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#given()
             */
            @Override
            public void given() throws Exception
            {
                myUser = generate();
                createPerson(myUser);
                siteService.setMembership(collabSiteId, myUser, SITE_MANAGER);
                securityClearanceService.setUserSecurityClearance(myUser, LEVEL2);

                folder = fileFolderService.create(documentLibrary, generate(), TYPE_FOLDER).getNodeRef();
                doc1 = fileFolderService.create(folder, generate(), TYPE_CONTENT).getNodeRef();
                doc2 = fileFolderService.create(folder, generate(), TYPE_CONTENT).getNodeRef();
                doc3 = fileFolderService.create(folder, generate(), TYPE_CONTENT).getNodeRef();
            }

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#when()
             */
            @Override
            public void when() throws Exception
            {
                contentClassificationService.classifyContent(LEVEL1, generate(), newHashSet(REASON), doc1);
                contentClassificationService.classifyContent(LEVEL2, generate(), newHashSet(REASON), doc2);
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

                        ArrayList<NodeRef> docs = newArrayList(doc1, doc2, doc3);
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

                        ArrayList<NodeRef> docs = newArrayList(doc2, doc3);
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
         * and three documents are created in the document library
         *
         * When one of the documents is classified with the highest security level
         * and another document is classified with the mid-level security level
         *
         * Then as the admin user I will see all three documents
         * and as the test user I will see all three documents
         */
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private String myUser;
            private NodeRef folder;
            private NodeRef doc1;
            private NodeRef doc2;
            private NodeRef doc3;

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#given()
             */
            @Override
            public void given() throws Exception
            {
                myUser = generate();
                createPerson(myUser);
                siteService.setMembership(collabSiteId, myUser, SITE_MANAGER);
                securityClearanceService.setUserSecurityClearance(myUser, LEVEL1);

                folder = fileFolderService.create(documentLibrary, generate(), TYPE_FOLDER).getNodeRef();
                doc1 = fileFolderService.create(folder, generate(), TYPE_CONTENT).getNodeRef();
                doc2 = fileFolderService.create(folder, generate(), TYPE_CONTENT).getNodeRef();
                doc3 = fileFolderService.create(folder, generate(), TYPE_CONTENT).getNodeRef();
            }

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#when()
             */
            @Override
            public void when() throws Exception
            {
                contentClassificationService.classifyContent(LEVEL1, generate(), newHashSet(REASON), doc1);
                contentClassificationService.classifyContent(LEVEL2, generate(), newHashSet(REASON), doc2);
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

                        ArrayList<NodeRef> docs = newArrayList(doc1, doc2, doc3);
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

                        ArrayList<NodeRef> docs = newArrayList(doc1, doc2, doc3);
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
