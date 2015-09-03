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

import static java.lang.Integer.MAX_VALUE;
import static org.alfresco.repo.site.SiteModel.SITE_MANAGER;
import static org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
import static org.alfresco.service.cmr.search.SearchService.LANGUAGE_FTS_ALFRESCO;
import static org.alfresco.util.GUID.generate;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchParameters;

/**
 * Tests for enforcement of classification when searching documents in the document library
 *
 * @author Tuna Aksoy
 * @since 2.4.a
 */
public class DocumentSearchClassificationEnforcementTest extends SearchClassificationEnforcementTestBase
{
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
         * and one of the documents is classified with the highest security level
         *
         * When I search for the documents as admin
         * Then I will see both documents
         *
         * When I search for the documents as the test user
         * Then I will only see the unclassified document
         */
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef folder;
            private NodeRef doc1;
            private NodeRef doc2;
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
                siteService.setMembership(collabSiteId, testUser, SITE_MANAGER);

                folder = fileFolderService.create(documentLibrary, generate(), TYPE_FOLDER).getNodeRef();
                doc1 = fileFolderService.create(folder, searchQuery + generate(), TYPE_CONTENT).getNodeRef();
                doc2 = fileFolderService.create(folder, searchQuery + generate(), TYPE_CONTENT).getNodeRef();

                contentClassificationService.classifyContent(propertiesDTO1, doc1);
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
                        assertTrue(resultsForAdmin.contains(doc1));
                        assertTrue(resultsForAdmin.contains(doc2));

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
                        assertTrue(resultsForTestUser.contains(doc2));

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
         * and three documents are created in the document library
         * and one of the documents is classified with the highest security level
         * and another document is classified with the mid-level security level
         *
         * When I search for the documents as admin
         * Then I will see all three documents
         *
         * When I search for the documents as the test user
         * Then I will see the unclassified document
         * and the document with the mid-level classification
         * and I won't be able to see the document with the classification greater than my clearance level
         */
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef folder;
            private NodeRef doc1;
            private NodeRef doc2;
            private NodeRef doc3;
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
                siteService.setMembership(collabSiteId, testUser, SITE_MANAGER);
                securityClearanceService.setUserSecurityClearance(testUser, LEVEL2);

                folder = fileFolderService.create(documentLibrary, generate(), TYPE_FOLDER).getNodeRef();
                doc1 = fileFolderService.create(folder, searchQuery + generate(), TYPE_CONTENT).getNodeRef();
                doc2 = fileFolderService.create(folder, searchQuery + generate(), TYPE_CONTENT).getNodeRef();
                doc3 = fileFolderService.create(folder, searchQuery + generate(), TYPE_CONTENT).getNodeRef();

                contentClassificationService.classifyContent(propertiesDTO1, doc1);
                contentClassificationService.classifyContent(propertiesDTO2, doc2);
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
                        assertTrue(resultsForAdmin.contains(doc1));
                        assertTrue(resultsForAdmin.contains(doc2));
                        assertTrue(resultsForAdmin.contains(doc3));

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
                        assertTrue(resultsForTestUser.contains(doc2));
                        assertTrue(resultsForTestUser.contains(doc3));

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
         * and three documents are created in the document library
         * and one of the documents is classified with the highest security level
         * and another document is classified with the mid-level security level
         *
         * When I search for the documents as admin
         * Then I will see all three documents
         *
         * When I search for the documents as the test user
         * Then I will see all three documents
         */
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef folder;
            private NodeRef doc1;
            private NodeRef doc2;
            private NodeRef doc3;
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
                siteService.setMembership(collabSiteId, testUser, SITE_MANAGER);
                securityClearanceService.setUserSecurityClearance(testUser, LEVEL1);

                folder = fileFolderService.create(documentLibrary, generate(), TYPE_FOLDER).getNodeRef();
                doc1 = fileFolderService.create(folder, searchQuery + generate(), TYPE_CONTENT).getNodeRef();
                doc2 = fileFolderService.create(folder, searchQuery + generate(), TYPE_CONTENT).getNodeRef();
                doc3 = fileFolderService.create(folder, searchQuery + generate(), TYPE_CONTENT).getNodeRef();

                contentClassificationService.classifyContent(propertiesDTO1, doc1);
                contentClassificationService.classifyContent(propertiesDTO2, doc2);
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
                        assertTrue(resultsForAdmin.contains(doc1));
                        assertTrue(resultsForAdmin.contains(doc2));
                        assertTrue(resultsForAdmin.contains(doc3));

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
                        assertTrue(resultsForTestUser.contains(doc1));
                        assertTrue(resultsForTestUser.contains(doc2));
                        assertTrue(resultsForTestUser.contains(doc3));

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
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.setQuery("cm:name:" + searchQuery + "*");
        searchParameters.setLanguage(LANGUAGE_FTS_ALFRESCO);
        searchParameters.addStore(STORE_REF_WORKSPACE_SPACESSTORE);
        searchParameters.setMaxItems(MAX_VALUE);
        return searchService.query(searchParameters).getNodeRefs();
    }
}
