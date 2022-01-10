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
package org.alfresco.rest.rm.community.search;

import static java.util.Arrays.asList;

import static org.alfresco.rest.rm.community.model.user.UserRoles.ROLE_RM_MANAGER;
import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.alfresco.rest.core.search.SearchRequestBuilder;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.user.UserPermissions;
import org.alfresco.rest.search.RestRequestQueryModel;
import org.alfresco.rest.search.SearchResponse;
import org.alfresco.rest.v0.UserTrashcanAPI;
import org.alfresco.rest.v0.service.RoleService;
import org.alfresco.utility.Utility;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class contains the tests for v1 Search API  with records with CMIS query
 */
public class SearchRecordsV1CmisTests extends BaseRMRestTest
{
    private static final String SEARCH_TERM = generateTestPrefix(SearchRecordsV1CmisTests.class);
    private SiteModel collaborationSite;
    private UserModel nonRMUser, rmUser;
    private FileModel fileModel;
    private RestRequestQueryModel queryModel;

    @Autowired
    private UserTrashcanAPI userTrashcanAPI;

    @Autowired
    private RoleService roleService;

    /**
     * Create a collaboration site and some in place records.
     */
    @BeforeClass (alwaysRun = true)
    public void setupSearchRecordsV1Cmis() throws Exception
    {
        STEP("Create a collaboration site");
        collaborationSite = dataSite.usingAdmin().createPrivateRandomSite();

        STEP("Create a site manager user for the collaboration site");
        nonRMUser = getDataUser().createRandomTestUser();
        getDataUser().addUserToSite(nonRMUser, collaborationSite, UserRole.SiteManager);

        STEP("Create an rm user");
        rmUser = getDataUser().createRandomTestUser();

        STEP("Create 10 documents and declare as records");
        for (int i = 0; ++i <= 10; )
        {
            fileModel = new FileModel(String.format("%s.%s", "Record" + SEARCH_TERM + i, FileType.TEXT_PLAIN.extension));
            fileModel = dataContent.usingUser(nonRMUser).usingSite(collaborationSite).createContent(fileModel);
            getRestAPIFactory().getFilesAPI(nonRMUser).declareAsRecord(fileModel.getNodeRefWithoutVersion());
        }
        STEP("Create record folder and some records ");
        RecordCategoryChild recordFolder = createCategoryFolderInFilePlan();
        roleService.assignUserPermissionsOnCategoryAndRMRole(rmUser, recordFolder.getId(),
                UserPermissions.PERMISSION_READ_RECORDS, ROLE_RM_MANAGER.roleId);
        for (int i = 0; ++i <= 10; )
        {
            createElectronicRecord(recordFolder.getId(), "Record" + SEARCH_TERM + i);
        }

        queryModel = new RestRequestQueryModel();
        queryModel.setQuery("select * from cmis:document WHERE cmis:name LIKE 'Record" + SEARCH_TERM + "%'");
        queryModel.setLanguage("cmis");

        //wait for solr indexing
        Utility.sleep(1000, 80000, () ->
        {
            SearchRequestBuilder sqlRequest = new SearchRequestBuilder().setQueryBuilder(queryModel)
                                                                        .setPagingBuilder(new SearchRequestBuilder().setPagination(100, 0))
                                                                        .setFieldsBuilder(asList("id", "name"));
            SearchResponse searchResponse = getRestAPIFactory().getSearchAPI(null).search(sqlRequest);
            assertEquals(searchResponse.getPagination().getTotalItems().intValue(), 20,
                    "Total number of items is not retrieved yet");
        });
    }

    /**
     * Given some documents with names starting with a particular test
     * When executing the search query with paging
     * And setting the skipCount and maxItems to reach the number of total items
     * Then hasMoreItems will be set to false
     */
    @Test
    public void searchWhenTotalItemsReach()
    {
        final SearchRequestBuilder sqlRequest = new SearchRequestBuilder().setQueryBuilder(queryModel)
                                                                          .setPagingBuilder(new SearchRequestBuilder().setPagination(5, 15))
                                                                          .setFieldsBuilder(asList("id", "name"));

        SearchResponse searchResponse = getRestAPIFactory().getSearchAPI(rmUser).search(sqlRequest);
        assertEquals(searchResponse.getPagination().getCount(), 5, "Expected maxItems to be five");
        assertEquals(searchResponse.getPagination().getSkipCount(), 15, "Expected skip count to be fifteen");
        assertFalse(searchResponse.getPagination().isHasMoreItems(), "Expected hasMoreItems to be false");
        assertEquals(searchResponse.getEntries().size(), 5, "Expected total entries to be five");
    }

    @Test
    public void searchWhenTotalItemsReachWithNonRM()
    {
        final SearchRequestBuilder sqlRequest = new SearchRequestBuilder().setQueryBuilder(queryModel)
                                                                          .setPagingBuilder(new SearchRequestBuilder().setPagination(5, 5))
                                                                          .setFieldsBuilder(asList("id", "name"));

        SearchResponse searchResponse = getRestAPIFactory().getSearchAPI(nonRMUser).search(sqlRequest);
        assertEquals(searchResponse.getPagination().getCount(), 5, "Expected maxItems to be five");
        assertEquals(searchResponse.getPagination().getSkipCount(), 5, "Expected skip count to be five");
        assertFalse(searchResponse.getPagination().isHasMoreItems(), "Expected hasMoreItems to be false");
        assertEquals(searchResponse.getEntries().size(), 5, "Expected total entries to be five");
    }

    /**
     * Given some documents with names starting with a particular text
     * When executing the search query with paging
     * And setting skipCount and maxItems to exceed the number of total items
     * Then hasMoreItems will be set to false
     */
    @Test
    public void searchWhenTotalItemsExceedRMUser()
    {
        final SearchRequestBuilder sqlRequest = new SearchRequestBuilder().setQueryBuilder(queryModel)
                                                                          .setPagingBuilder(new SearchRequestBuilder().setPagination(5, 16))
                                                                          .setFieldsBuilder(asList("id", "name"));

        SearchResponse searchResponse = getRestAPIFactory().getSearchAPI(rmUser).search(sqlRequest);
        assertEquals(searchResponse.getPagination().getCount(), 4, "Expected maxItems to be four");
        assertEquals(searchResponse.getPagination().getSkipCount(), 16, "Expected skip count to be sixteen");
        assertFalse(searchResponse.getPagination().isHasMoreItems(), "Expected hasMoreItems to be false");
        assertEquals(searchResponse.getEntries().size(), 4, "Expected total entries to be four");
    }

    @Test
    public void searchWhenTotalItemsExceedNonRMUser()
    {
        final SearchRequestBuilder sqlRequest = new SearchRequestBuilder().setQueryBuilder(queryModel)
                                                                          .setPagingBuilder(new SearchRequestBuilder().setPagination(5, 6))
                                                                          .setFieldsBuilder(asList("id", "name"));

        SearchResponse searchResponse = getRestAPIFactory().getSearchAPI(nonRMUser).search(sqlRequest);
        assertEquals(searchResponse.getPagination().getCount(), 4, "Expected maxItems to be four");
        assertEquals(searchResponse.getPagination().getSkipCount(), 6, "Expected skip count to be six");
        assertFalse(searchResponse.getPagination().isHasMoreItems(), "Expected hasMoreItems to be false");
        assertEquals(searchResponse.getEntries().size(), 4, "Expected total entries to be four");
    }

    /**
     * Given some documents ending with a particular text
     * When executing the search query with paging
     * And setting skipCount and maxItems under the number of total items
     * Then hasMoreItems will be set to true
     */
    @Test
    public void searchResultsUnderTotalItemsRMUser()
    {
        final SearchRequestBuilder sqlRequest = new SearchRequestBuilder().setQueryBuilder(queryModel)
                                                                          .setPagingBuilder(new SearchRequestBuilder().setPagination(4, 15))
                                                                          .setFieldsBuilder(asList("id", "name"));

        SearchResponse searchResponse = getRestAPIFactory().getSearchAPI(rmUser).search(sqlRequest);
        assertEquals(searchResponse.getPagination().getCount(), 4, "Expected maxItems to be four");
        assertEquals(searchResponse.getPagination().getSkipCount(), 15, "Expected skip count to be fifteen");
        assertTrue(searchResponse.getPagination().isHasMoreItems(), "Expected hasMoreItems to be true");
        assertEquals(searchResponse.getEntries().size(), 4, "Expected total entries to be four");
    }

    @Test
    public void searchResultsUnderTotalItemsNonRMUser()
    {
        final SearchRequestBuilder sqlRequest = new SearchRequestBuilder().setQueryBuilder(queryModel)
                                                                          .setPagingBuilder(new SearchRequestBuilder().setPagination(4, 5))
                                                                          .setFieldsBuilder(asList("id", "name"));

        SearchResponse searchResponse = getRestAPIFactory().getSearchAPI(nonRMUser).search(sqlRequest);
        assertEquals(searchResponse.getPagination().getCount(), 4, "Expected maxItems to be four");
        assertEquals(searchResponse.getPagination().getSkipCount(), 5, "Expected skip count to be five");
        assertTrue(searchResponse.getPagination().isHasMoreItems(), "Expected hasMoreItems to be true");
        assertEquals(searchResponse.getEntries().size(), 4, "Expected total entries to be four");
    }

    @AfterClass (alwaysRun = true)
    public void tearDown()
    {
        dataSite.usingAdmin().deleteSite(collaborationSite);
        userTrashcanAPI.emptyTrashcan(getAdminUser().getUsername(), getAdminUser().getPassword());
    }
}
