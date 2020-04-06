/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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

import static org.alfresco.rest.rm.community.util.CommonTestUtils.generateTestPrefix;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.alfresco.dataprep.ContentActions;
import org.alfresco.rest.core.search.SearchRequestBuilder;
import org.alfresco.rest.rm.community.base.BaseRMRestTest;
import org.alfresco.rest.search.RestRequestQueryModel;
import org.alfresco.rest.search.SearchResponse;
import org.alfresco.rest.v0.UserTrashcanAPI;
import org.alfresco.test.AlfrescoTest;
import org.alfresco.utility.Utility;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.SiteModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class contains the tests for v1 Search API for documents using AFTS query
 *
 * @author Roxana Lucanu
 * @since 2.6.3
 */
public class SearchDocumentsV1AftsTest extends BaseRMRestTest
{
    private static final String SEARCH_TERM = generateTestPrefix(SearchDocumentsV1AftsTest.class);
    private SiteModel collaborationSite;
    private FileModel fileModel;
    private RestRequestQueryModel queryModel;

    @Autowired
    private UserTrashcanAPI userTrashcanAPI;

    @Autowired
    private ContentActions contentActions;

    /**
     * Create a collaboration site and some documents in it.
     */
    @BeforeClass (alwaysRun = true)
    public void setupSearchAPI() throws Exception
    {
        STEP("Create a collaboration site");
        collaborationSite = dataSite.usingAdmin().createPrivateRandomSite();

        STEP("Create 10 documents with name ending with SEARCH_TERM");
        for (int i = 0; ++i <= 10; )
        {
            fileModel = new FileModel("Doc" + i + SEARCH_TERM, FileType.UNDEFINED);
            fileModel = dataContent.usingAdmin().usingSite(collaborationSite).createContent(fileModel);
        }
        queryModel = new RestRequestQueryModel();
        queryModel.setLanguage("afts");
        queryModel.setQuery("cm:name:*" + SEARCH_TERM);

        //do a cmis query to wait for solr indexing
        Utility.sleep(5000, 80000, () ->
        {
            SearchRequestBuilder sqlRequest = new SearchRequestBuilder().setQueryBuilder(queryModel)
                                                                        .setPagingBuilder(new SearchRequestBuilder().setPagination(100, 0))
                                                                        .setFieldsBuilder(asList("id", "name"));
            SearchResponse searchResponse = getRestAPIFactory().getSearchAPI(null).search(sqlRequest);
            assertEquals(searchResponse.getPagination().getTotalItems().intValue(), 10,
                "Total number of items is not 10, got  " + searchResponse.getPagination().getTotalItems() + " total items");
        });
    }

    /**
     * Given some documents having a common term in the name
     * When executing the search query with paging
     * And setting the skipCount and maxItems to reach the number of total items
     * Then hasMoreItems will be set to false
     */
    @Test
    @AlfrescoTest (jira = "RM-7145")
    public void searchWhenMaxItemsReached() throws Exception
    {
        final SearchRequestBuilder sqlRequest = new SearchRequestBuilder().setQueryBuilder(queryModel)
                                                                          .setPagingBuilder(new SearchRequestBuilder().setPagination(5, 5))
                                                                          .setFieldsBuilder(asList("id", "name"));

        SearchResponse searchResponse = getRestAPIFactory().getSearchAPI(null).search(sqlRequest);
        assertEquals(searchResponse.getPagination().getCount(), 5);
        assertEquals(searchResponse.getPagination().getSkipCount(), 5);
        assertFalse(searchResponse.getPagination().isHasMoreItems());
        assertEquals(searchResponse.getEntries().size(), 5);
    }

    /**
     * Given some documents ending with a particular text
     * When executing the search query with paging
     * And setting skipCount and maxItems to exceed the number of total items
     * Then hasMoreItems will be set to false
     */
    @Test
    public void searchWhenTotalItemsExceed() throws Exception
    {
        final SearchRequestBuilder sqlRequest = new SearchRequestBuilder().setQueryBuilder(queryModel)
                                                                          .setPagingBuilder(new SearchRequestBuilder().setPagination(5, 6))
                                                                          .setFieldsBuilder(asList("id", "name"));

        SearchResponse searchResponse = getRestAPIFactory().getSearchAPI(null).search(sqlRequest);
        assertEquals(searchResponse.getPagination().getCount(), 4);
        assertEquals(searchResponse.getPagination().getSkipCount(), 6);
        assertFalse(searchResponse.getPagination().isHasMoreItems());
        assertEquals(searchResponse.getEntries().size(), 4);
    }

    /**
     * Given some documents ending with a particular text
     * When executing the search query with paging
     * And setting skipCount and maxItems under the number of total items
     * Then hasMoreItems will be set to true
     */
    @Test
    public void searchResultsUnderTotalItems() throws Exception
    {
        final SearchRequestBuilder sqlRequest = new SearchRequestBuilder().setQueryBuilder(queryModel)
                                                                          .setPagingBuilder(new SearchRequestBuilder().setPagination(4, 5))
                                                                          .setFieldsBuilder(asList("id", "name"));

        SearchResponse searchResponse = getRestAPIFactory().getSearchAPI(null).search(sqlRequest);
        assertEquals(searchResponse.getPagination().getCount(), 4);
        assertEquals(searchResponse.getPagination().getSkipCount(), 5);
        assertTrue(searchResponse.getPagination().isHasMoreItems());
        assertEquals(searchResponse.getEntries().size(), 4);
    }

}
