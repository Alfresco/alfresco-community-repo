/*
 * #%L
 * Alfresco Search Services E2E Test
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.rest.search;

import org.alfresco.rest.model.body.RestNodeLockBodyModel;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.hamcrest.Matchers;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.reverse;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.*;
import static org.testng.AssertJUnit.assertEquals;

/**
 * Search end point Public API test.
 */
public class SearchTest extends AbstractSearchServicesE2ETest
{
    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        searchServicesDataPreparation();
        waitForContentIndexing(file4.getContent(), true);
    }

    @Test
    public void searchOnIndexedData() throws Exception
    {
        SearchResponse nodes =  query("cm:content:" + unique_searchString);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        nodes.assertThat().entriesListIsNotEmpty();
        
        SearchNodeModel entity = nodes.getEntryByIndex(0);
        // MNT-25404: search/score is only populated when highlighting is requested
        if (entity.getSearch() != null)
        {
            entity.assertThat().field("search").contains("score");
            entity.getSearch().assertThat().field("score").isNotNull();
        }
        Assert.assertEquals(entity.getName(),"pangram.txt");
    }
    
    @Test
    public void searchNonIndexedData()
    {        
        SearchResponse nodes =  query("yeti");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        nodes.assertThat().entriesListIsEmpty();
    }

    @Test
    @TestRail(section = {TestGroup.REST_API, TestGroup.SEARCH}, executionType = ExecutionType.REGRESSION,
              description = "Checks its possible to include the original request in the response")
    public void searchWithRequest()
    {
        SearchRequest query = new SearchRequest();
        RestRequestQueryModel queryReq = new RestRequestQueryModel();
        queryReq.setQuery("fox");
        query.setQuery(queryReq);
        query.setIncludeRequest(true);

        SearchResponse response = query(query);
        restClient.assertStatusCodeIs(HttpStatus.OK);

        response.getContext().assertThat().field("request").isNotEmpty();
    }

    @Test(groups={TestGroup.CONFIG_ENABLED_CASCADE_TRACKER})
    @TestRail(section = {TestGroup.REST_API, TestGroup.SEARCH}, executionType = ExecutionType.REGRESSION,
            description = "Tests a search request containing a sort clause.")
    public void searchWithOneSortClause()
    {
        // Tests the ascending order first
        List<String> expectedOrder = asList("alfresco.docx", "cars.PDF", "pangram.txt");

        SearchRequest searchRequest = createQuery("cm_name:alfresco\\.docx cm_name:cars\\.PDF cm_name:pangram\\.txt");
        searchRequest.addSortClause("FIELD", "name", true);

        RestRequestFilterQueryModel filters = new RestRequestFilterQueryModel();
        filters.setQuery("SITE:'" + testSite.getId() + "'");
        searchRequest.setFilterQueries(filters);

        SearchResponse responseWithAscendingOrder = query(searchRequest);

        restClient.assertStatusCodeIs(HttpStatus.OK);

        assertEquals(
                expectedOrder,
                responseWithAscendingOrder.getEntries().stream()
                    .map(SearchNodeModel::getModel)
                    .map(SearchNodeModel::getName)
                    .collect(Collectors.toList()));

        // Reverts the expected order...
        reverse(expectedOrder);

        // ...and test the descending order
        searchRequest.getSort().clear();
        searchRequest.addSortClause("FIELD", "name", false);

        SearchResponse responseWithDescendingOrder = query(searchRequest);
        assertEquals(
                expectedOrder,
                responseWithDescendingOrder.getEntries().stream()
                        .map(SearchNodeModel::getModel)
                        .map(SearchNodeModel::getName)
                        .collect(Collectors.toList()));
    }

    /**
     * Tests the query execution with two sort clauses.
     * The first clause has always the same value for all matches so the test makes sure the request is correctly
     * processed and the returned order is determined by the second clause.
     */
    @Test(groups={TestGroup.CONFIG_ENABLED_CASCADE_TRACKER})
    @TestRail(section = {TestGroup.REST_API, TestGroup.SEARCH}, executionType = ExecutionType.REGRESSION,
            description = "Tests a search request containing a sort clause.")
    public void searchWithTwoSortClauses()
    {
        // Tests the ascending order first
        List<String> expectedOrder = asList("alfresco.docx", "cars.PDF", "pangram.txt");

        SearchRequest searchRequest = createQuery("cm_name:alfresco\\.docx cm_name:cars\\.PDF cm_name:pangram\\.txt");
        searchRequest.addSortClause("FIELD", "name", true);
        searchRequest.addSortClause("FIELD", "createdByUser.id", true);

        RestRequestFilterQueryModel filters = new RestRequestFilterQueryModel();
        filters.setQuery("SITE:'" + testSite.getId() + "'");
        searchRequest.setFilterQueries(filters);

        SearchResponse responseWithAscendingOrder = query(searchRequest);

        restClient.assertStatusCodeIs(HttpStatus.OK);

        assertEquals(
                expectedOrder,
                responseWithAscendingOrder.getEntries().stream()
                        .map(SearchNodeModel::getModel)
                        .map(SearchNodeModel::getName)
                        .collect(Collectors.toList()));

        // Reverts the expected order...
        reverse(expectedOrder);

        // ...and test the descending order
        searchRequest.getSort().clear();
        searchRequest.addSortClause("FIELD", "name", false);
        searchRequest.addSortClause("FIELD", "createdByUser.id", true);

        SearchResponse responseWithDescendingOrder = query(searchRequest);
        assertEquals(
                expectedOrder,
                responseWithDescendingOrder.getEntries().stream()
                        .map(SearchNodeModel::getModel)
                        .map(SearchNodeModel::getName)
                        .collect(Collectors.toList()));
    }

    @Test(groups = { TestGroup.ACS_61n })
    @TestRail(section = {
        TestGroup.REST_API, TestGroup.SEARCH,
        TestGroup.ACS_61n }, executionType = ExecutionType.REGRESSION, description = "Checks the \"include\" request parameter support the 'permissions' option")
    public void searchQuery_includePermissions_shouldReturnNodeWithPermissionsInformation()
    {
        String query = "fox";
        String include = "permissions";

        SearchRequest retrievalQueryIncludingPermissionsInformation = createQuery(query);
        retrievalQueryIncludingPermissionsInformation.setInclude(singletonList(include));

        query(retrievalQueryIncludingPermissionsInformation);

        restClient.assertStatusCodeIs(HttpStatus.OK);
        restClient.onResponse().assertThat().body("list.entries[0].entry.permissions", notNullValue());

        SearchRequest retrievalQueryNotIncludingLockInformation = createQuery(query);

        query(retrievalQueryNotIncludingLockInformation);

        restClient.assertStatusCodeIs(HttpStatus.OK);
        restClient.onResponse().assertThat().body("list.entries[0].entry.permissions", nullValue());
    }

    @Test(groups = { TestGroup.ACS_61n }, enabled = false)
    @TestRail(section = {TestGroup.REST_API, TestGroup.SEARCH, TestGroup.ACS_61n  }, executionType = ExecutionType.REGRESSION,
            description = "Checks the \"include\" request parameter support the 'isLocked' option")
    public void searchQuery_includeIsLocked_shouldReturnNodeWithLockInformation() throws Exception {
        String query = "fox";
        String include = "isLocked";

        SearchRequest retrievalQueryIncludingLockInformation = createQuery(query);
        retrievalQueryIncludingLockInformation.setInclude(singletonList(include));

        query(retrievalQueryIncludingLockInformation);
        
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restClient.onResponse().assertThat().body("list.entries[0].entry.isLocked", equalTo(false));

        RestNodeLockBodyModel lockBodyModel = new RestNodeLockBodyModel();
        lockBodyModel.setLifetime("EPHEMERAL");
        lockBodyModel.setTimeToExpire(20);
        lockBodyModel.setType("FULL");
        restClient.authenticateUser(testUser).withCoreAPI().usingNode(file).lockNode(lockBodyModel);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        
        query(retrievalQueryIncludingLockInformation);

        restClient.assertStatusCodeIs(HttpStatus.OK);
        restClient.onResponse().assertThat().body("list.entries[0].entry.isLocked", equalTo(true));
        
        SearchRequest retrievalQueryNotIncludingLockInformation = createQuery(query);

        query(retrievalQueryNotIncludingLockInformation);

        restClient.assertStatusCodeIs(HttpStatus.OK);
        restClient.onResponse().assertThat().body("list.entries[0].entry.isLocked", nullValue());
    }

    @Test(groups = { TestGroup.ACS_61n })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SEARCH, TestGroup.ACS_61n  }, executionType = ExecutionType.REGRESSION,
            description = "Checks the \"include\" request parameter does not support the 'notValid' option")
    public void searchQuery_includeInvalid_shouldReturnBadResponse()
    {
        String query = "fox";
        String notValidInclude = "notValid";
        SearchRequest permissionsRetrieval = createQuery(query);
        permissionsRetrieval.setInclude(singletonList(notValidInclude));
        
        query(permissionsRetrieval);
        
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST);
        restClient.onResponse()
                .assertThat()
                .body("error.briefSummary", containsString("An invalid argument was received "+notValidInclude));
    }

    // Test that when fields parameter is set, only restricted fields appear in the response
    @Test
    public void searchWithFields()
    {
        SearchRequest query = new SearchRequest();
        RestRequestQueryModel queryReq = new RestRequestQueryModel();
        queryReq.setQuery("alfresco");
        query.setQuery(queryReq);

        // Restrict to fields: parentId
        List<String> fields = new ArrayList<>();
        fields.add("parentId");
        query.setFields(fields);

        query(query);
        restClient.assertStatusCodeIs(HttpStatus.OK);

        // Only Field parentId is included in the response
        restClient.onResponse().assertThat().body("list.entries.entry[0].parentId", Matchers.notNullValue());
        // Usual Fields such as 'name, id' aren't included in the response
        restClient.onResponse().assertThat().body("list.entries.entry[0].name", Matchers.nullValue());
        restClient.onResponse().assertThat().body("list.entries.entry[0].id", Matchers.nullValue());
    }
    
    @Test
    public void searchSpecialCharacters() throws Exception
    {
        // Create a file with Special Characters
        String specialCharfileName = "è¥äæ§ç§-åæ.pdf";
        FileModel file = new FileModel(specialCharfileName, "è¥äæ§ç§-åæ¬¯¸" + "è¥äæ§ç§-åæ¬¯¸", "è¥äæ§ç§-åæ¬¯¸", FileType.TEXT_PLAIN,
                "Text file with Special Characters: " + specialCharfileName);
        dataContent.usingUser(testUser).usingSite(testSite).createContent(file);

        waitForIndexing(file.getName(), true);

        // Search
        SearchRequest searchReq = createQuery("name:'" + specialCharfileName + "'");
        SearchResponse nodes = query(searchReq);

        restClient.assertStatusCodeIs(HttpStatus.OK);
        nodes.assertThat().entriesListIsNotEmpty();

        restClient.onResponse().assertThat().body("list.entries.entry[0].name", Matchers.equalToIgnoringCase(specialCharfileName));
    }
}
