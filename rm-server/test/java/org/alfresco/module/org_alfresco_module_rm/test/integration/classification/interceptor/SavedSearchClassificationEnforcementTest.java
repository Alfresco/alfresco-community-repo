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
import static org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService.ROLE_ADMIN;
import static org.alfresco.module.org_alfresco_module_rm.search.SavedSearchDetails.QUERY;
import static org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
import static org.alfresco.service.cmr.search.SearchService.LANGUAGE_FTS_ALFRESCO;
import static org.alfresco.util.GUID.generate;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.search.RecordsManagementSearchParameters;
import org.alfresco.module.org_alfresco_module_rm.search.SavedSearchDetails;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchParameters;
import org.json.JSONObject;

/**
 * Integration test for saved searches with classification enforcement
 *
 * @author Tuna Aksoy
 * @since 2.4.a
 */
public class SavedSearchClassificationEnforcementTest extends SearchClassificationEnforcementTestBase
{
    public void testSavedSearchWithClassificationEnforcement()
    {
        /**
         * Given that a test user with mid-level security clearance exists
         * and the test user is added to the RM Admin role
         * and a category, a folder and five records are created in the file plan
         * and two of the records are classified with the highest security level
         * and another record is classified with the mid-level security level
         *
         * When I view the results of the saved search as admin
         * The I will see all five records
         *
         * When I view the results of the saved search as the test user
         * Then I will see the unclassified documents
         * and the document with the mid-level classification
         * and I won't be able to see the documents with the classification greater than my clearance level
         */
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef category;
            private NodeRef folder;
            private NodeRef record1;
            private NodeRef record2;
            private NodeRef record3;
            private NodeRef record4;
            private NodeRef record5;
            private String savedSearchName = generate();
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
                record4 = utils.createRecord(folder, searchQuery + generate());
                record5 = utils.createRecord(folder, searchQuery + generate());

                RecordsManagementSearchParameters searchParameters = new RecordsManagementSearchParameters();
                searchParameters.setIncludeUndeclaredRecords(true);
                rmSearchService.saveSearch(siteId, savedSearchName, generate(), searchQuery + "*", searchParameters, true);

                contentClassificationService.classifyContent(propertiesDTO1, record1);
                contentClassificationService.classifyContent(propertiesDTO2, record3);
                contentClassificationService.classifyContent(propertiesDTO1, record5);
            }

            /**
             * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase.BehaviourDrivenTest#when()
             */
            @Override
            public void when() throws Exception
            {
                SavedSearchDetails savedSearchDetails = rmSearchService.getSavedSearch(siteId, savedSearchName);
                JSONObject jsonObject = new JSONObject(savedSearchDetails.toJSONString());
                String query = (String) jsonObject.get(QUERY);

                resultsForAdmin = searchAsAdmin(query);
                resultsForTestUser = searchAsTestUser(query);
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
                        assertEquals(5, resultsForAdmin.size());
                        assertTrue(resultsForAdmin.contains(record1));
                        assertTrue(resultsForAdmin.contains(record2));
                        assertTrue(resultsForAdmin.contains(record3));
                        assertTrue(resultsForAdmin.contains(record4));
                        assertTrue(resultsForAdmin.contains(record5));

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
                        assertTrue(resultsForTestUser.contains(record2));
                        assertTrue(resultsForTestUser.contains(record3));
                        assertTrue(resultsForTestUser.contains(record4));

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
        searchParameters.setQuery(searchQuery);
        searchParameters.setLanguage(LANGUAGE_FTS_ALFRESCO);
        searchParameters.addStore(STORE_REF_WORKSPACE_SPACESSTORE);
        searchParameters.setMaxItems(MAX_VALUE);
        searchParameters.setNamespace(RM_URI);
        searchParameters.addQueryTemplate("keywords", "%(cm:name cm:title cm:description TEXT)");
        return searchService.query(searchParameters).getNodeRefs();
    }
}
