/*
 * #%L
 * Alfresco Search Services E2E Test
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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

import org.alfresco.cmis.CmisWrapper;
import org.alfresco.dataprep.SiteService.Visibility;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.exception.EmptyJsonResponseException;
import org.alfresco.utility.LogFactory;
import org.alfresco.utility.TasProperties;
import org.alfresco.utility.Utility;
import org.alfresco.utility.data.DataContent;
import org.alfresco.utility.data.DataSite;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.*;
import org.alfresco.utility.network.ServerHealth;
import org.hamcrest.Matchers;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import java.util.Set;
import java.util.stream.Collectors;

import static java.util.List.of;

/**
 * Abstract base class for Search API E2E tests running against ElasticSearch.
 * Migrated from alfresco-search-services test infrastructure into community repo.
 * Combines AbstractE2EFunctionalTest + AbstractSearchServicesE2ETest from the original repo.
 */
@ContextConfiguration("classpath:alfresco-restapi-context.xml")
public abstract class AbstractSearchServicesE2ETest extends AbstractTestNGSpringContextTests
{
    private static final Logger LOGGER = LogFactory.getLogger();

    private static final String SEARCH_DATA_SAMPLE_FOLDER = "FolderSearch";
    private static final int MAX_ATTEMPTS_TO_RETRY_QUERY = 10;
    private static final int MAX_WAIT_IN_SECONDS_BEFORE_RETRY_QUERY = 5;
    private static final int MAX_ATTEMPTS_TO_READ_RESPONSE = 10;
    private static final int MAX_WAIT_IN_SECONDS_BEFORE_REREAD_RESPONSE = 2;

    /** The number of retries that a query will be tried before giving up. */
    protected static final int SEARCH_MAX_ATTEMPTS = 120;

    @Autowired
    protected RestWrapper restClient;

    @Autowired
    protected DataContent dataContent;

    @Autowired
    protected DataUser dataUser;

    @Autowired
    protected DataSite dataSite;

    @Autowired
    protected CmisWrapper cmisApi;

    @Autowired
    protected ServerHealth serverHealth;

    @Autowired
    protected TasProperties properties;

    protected UserModel testUser;
    protected UserModel adminUserModel;

    protected SiteModel testSite;

    protected static String unique_searchString;

    protected FileModel file, file2, file3, file4;
    protected FolderModel folder;

    @BeforeClass(alwaysRun = true)
    public void setup()
    {
        serverHealth.assertServerIsOnline();

        adminUserModel = dataUser.getAdminUser();
        testUser = dataUser.createRandomTestUser("UserSearch");

        testSite = new SiteModel(RandomData.getRandomName("SiteSearch"));
        testSite.setVisibility(Visibility.PRIVATE);

        testSite = dataSite.usingUser(testUser).createSite(testSite);

        unique_searchString = testSite.getTitle().replace("SiteSearch", "Unique");
    }

    /**
     * Creates the standard test data structure used across search tests:
     * <pre>
     * |- FolderSearch
     * |-- pangram.txt
     * |-- cars.PDF
     * |-- alfresco.docx
     * |-- &lt;uniqueFileName&gt;.ODT
     * </pre>
     */
    public void searchServicesDataPreparation()
    {
        folder = new FolderModel(SEARCH_DATA_SAMPLE_FOLDER);
        dataContent.usingUser(testUser).usingSite(testSite).createFolder(folder);

        String title = "Title: " + unique_searchString;
        String description = "Description: File is created for search tests by Author: " + unique_searchString + " . ";

        file = new FileModel("pangram.txt", "pangram" + title, description, FileType.TEXT_PLAIN,
                description + " The quick brown fox jumps over the lazy dog");

        file2 = new FileModel("cars.PDF", "cars", description, FileType.TEXT_PLAIN,
                "The landrover discovery is not a sports car");

        file3 = new FileModel("alfresco.docx", "alfresco", "alfresco", FileType.TEXT_PLAIN,
                "Alfresco text file for search ");

        file4 = new FileModel(unique_searchString + ".ODT", "uniquee" + title, description, FileType.TEXT_PLAIN,
                "Unique text file for search ");

        of(file, file2, file3, file4).forEach(
                f -> dataContent.usingUser(testUser).usingSite(testSite).usingResource(folder).createContent(f));

        waitForMetadataIndexing(file4.getName(), true);
    }

    /**
     * Creates a file with provided text content and waits for it to be indexed.
     */
    protected FileModel createFileWithProvidedText(String filename, String providedText)
    {
        String title = "Title: File containing " + providedText;
        String description = "Description: Contains provided string: " + providedText;
        FileModel uniqueFile = new FileModel(filename, title, description, FileType.TEXT_PLAIN,
                "The content " + providedText + " is a provided string");
        dataContent.usingUser(testUser).usingSite(testSite).usingResource(folder).createContent(uniqueFile);
        waitForContentIndexing(providedText, true);
        return uniqueFile;
    }

    /**
     * Deploys a custom content model from the given classpath path.
     */
    public boolean deployCustomModel(String path)
    {
        boolean modelDeployed = false;
        if ((path != null) && (path.endsWith("-model.xml")))
        {
            try
            {
                dataContent.usingAdmin().deployContentModel(path);
                modelDeployed = true;
            }
            catch (Exception e)
            {
                LOGGER.warn("Error Loading Custom Model", e);
            }
        }
        return modelDeployed;
    }

    /**
     * Executes a search request authenticated as testUser.
     */
    protected SearchResponse query(SearchRequest query)
    {
        return restClient.authenticateUser(testUser).withSearchAPI().search(query);
    }

    /**
     * Executes a plain string search as admin user.
     */
    public SearchResponse query(String queryString)
    {
        return queryAsUser(dataUser.getAdminUser(), queryString);
    }

    /**
     * Executes a search with highlight as testUser.
     */
    protected SearchResponse query(RestRequestQueryModel queryReq, RestRequestHighlightModel highlight)
    {
        SearchRequest query = new SearchRequest(queryReq);
        query.setHighlight(highlight);
        return restClient.authenticateUser(testUser).withSearchAPI().search(query);
    }

    /**
     * Executes a search as the given user using a plain query string.
     */
    protected SearchResponse queryAsUser(UserModel user, String queryString)
    {
        SearchRequest searchRequest = new SearchRequest();
        RestRequestQueryModel queryModel = new RestRequestQueryModel();
        queryModel.setQuery(queryString);
        searchRequest.setQuery(queryModel);
        return restClient.authenticateUser(user).withSearchAPI().search(searchRequest);
    }

    /**
     * Executes a search as the given user with pagination.
     */
    protected SearchResponse queryAsUser(UserModel user, RestRequestQueryModel queryModel, Pagination paging)
    {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setQuery(queryModel);
        if (paging != null)
        {
            searchRequest.setPaging(paging);
        }
        return restClient.authenticateUser(user).withSearchAPI().search(searchRequest);
    }

    /**
     * Queries until the response entries list is not empty, retrying up to the configured limit.
     */
    protected SearchResponse queryUntilResponseEntriesListNotEmpty(UserModel user, String queryString)
    {
        SearchResponse response = queryUntilStatusIsOk(user, queryString);
        if (restClient.getStatusCode().matches(String.valueOf(HttpStatus.OK.value())))
        {
            for (int readAttempts = 0; readAttempts < MAX_ATTEMPTS_TO_READ_RESPONSE; readAttempts++)
            {
                if (!response.isEmpty())
                {
                    return response;
                }
                Utility.waitToLoopTime(MAX_WAIT_IN_SECONDS_BEFORE_REREAD_RESPONSE,
                        "Re-reading empty response. Retry Attempt: " + (readAttempts + 1));
            }
        }
        return response;
    }

    private SearchResponse queryUntilStatusIsOk(UserModel user, String queryString)
    {
        for (int queryAttempts = 0; queryAttempts < MAX_ATTEMPTS_TO_RETRY_QUERY - 1; queryAttempts++)
        {
            try
            {
                SearchResponse response = queryAsUser(user, queryString);
                if (restClient.getStatusCode().matches(String.valueOf(HttpStatus.OK.value())))
                {
                    return response;
                }
                Utility.waitToLoopTime(MAX_WAIT_IN_SECONDS_BEFORE_RETRY_QUERY,
                        "Re-trying query for valid status code. Retry Attempt: " + (queryAttempts + 1));
            }
            catch (EmptyJsonResponseException ignore)
            {
            }
        }
        return queryAsUser(user, queryString);
    }

    /**
     * Builds a SearchRequest from a plain query term.
     */
    protected SearchRequest createQuery(String term)
    {
        SearchRequest query = new SearchRequest();
        RestRequestQueryModel queryReq = new RestRequestQueryModel();
        queryReq.setQuery(term);
        query.setQuery(queryReq);
        return query;
    }

    /**
     * Waits for content to be indexed using the cm:content field.
     */
    public boolean waitForContentIndexing(String userQuery, boolean expectedInResults)
    {
        return waitForIndexing("cm:content", userQuery, expectedInResults);
    }

    /**
     * Waits for metadata (name field) to be indexed.
     */
    public boolean waitForMetadataIndexing(String userQuery, boolean expectedInResults)
    {
        return waitForIndexing("name", userQuery, expectedInResults);
    }

    /**
     * Waits for a query to return expected results, using the query as-is.
     */
    public boolean waitForIndexing(String userQuery, boolean expectedInResults)
    {
        return waitForIndexing(null, userQuery, expectedInResults);
    }

    private boolean waitForIndexing(String fieldName, String userQuery, boolean expectedInResults)
    {
        String query = (fieldName == null) ? userQuery : String.format("%s:'%s'", fieldName, userQuery);
        return isContentInSearchResults(query, null, expectedInResults);
    }

    /**
     * Polls the search API until the expected content appears or is absent from results.
     */
    public boolean isContentInSearchResults(String userQuery, String contentToFind, boolean expectedInResults)
    {
        String contentName = (contentToFind == null) ? "" : contentToFind;
        SearchRequest searchRequest = createQuery(userQuery);
        final int ignoreRuntimeExceptionThreshold = SEARCH_MAX_ATTEMPTS / 10 + 1;

        for (int searchCount = 0; searchCount < SEARCH_MAX_ATTEMPTS; searchCount++)
        {
            try
            {
                if (expectedInResults == isContentFoundWithRequest(searchRequest, contentName))
                {
                    return true;
                }
            }
            catch (EmptyJsonResponseException ignore)
            {
            }
            catch (RuntimeException runtimeException)
            {
                if (searchCount > ignoreRuntimeExceptionThreshold)
                {
                    throw runtimeException;
                }
                else
                {
                    LOGGER.warn("Ignoring initial Search API failure.", runtimeException);
                }
            }
            finally
            {
                Utility.waitToLoopTime(1, "Wait For Indexing. Retry Attempt: " + (searchCount + 1));
            }
        }
        return false;
    }

    private boolean isContentFoundWithRequest(SearchRequest searchRequest, String contentName)
    {
        SearchResponse response = query(searchRequest);
        if (restClient.getStatusCode().matches(String.valueOf(HttpStatus.OK.value())))
        {
            return isContentInSearchResponse(response, contentName);
        }
        else
        {
            throw new RuntimeException("API returned status code: " + restClient.getStatusCode()
                    + " Expected: " + HttpStatus.OK.value());
        }
    }

    public boolean isContentInSearchResponse(SearchResponse response, String contentName)
    {
        return response.getEntries().stream()
                .map(entry -> entry.getModel().getName())
                .anyMatch(name -> name.equalsIgnoreCase(contentName) || contentName.isBlank());
    }

    /**
     * Performs a search as the given user with the specified language and paging.
     */
    protected SearchResponse performSearch(UserModel asUser, String query, SearchLanguage queryLanguage, Pagination paging)
    {
        RestRequestQueryModel queryModel = new RestRequestQueryModel();
        queryModel.setQuery(query);

        if (asUser == null)
        {
            asUser = testUser;
        }

        if (queryLanguage != null)
        {
            queryModel.setLanguage(queryLanguage.toString());
        }

        return queryAsUser(asUser, queryModel, paging);
    }

    /**
     * Sets pagination options for the API query.
     */
    protected Pagination setPaging(Integer skipCount, Integer maxItems)
    {
        Pagination paging = new Pagination();
        if (skipCount != null)
        {
            paging.setSkipCount(skipCount);
        }
        if (maxItems != null)
        {
            paging.setMaxItems(maxItems);
        }
        return paging;
    }

    /**
     * Supported search languages.
     */
    protected enum SearchLanguage
    {
        CMIS,
        AFTS
    }

    /**
     * Helper method to test if the search query works and count matches where provided.
     */
    protected SearchResponse testSearchQuery(String query, Integer expectedCount)
    {
        Pagination paging = new Pagination();
        paging.setSkipCount(0);
        paging.setMaxItems(100);

        SearchResponse response = performSearch(testUser, query, SearchLanguage.AFTS, paging);

        if (expectedCount != null)
        {
            restClient.onResponse().assertThat().body("list.pagination.count", Matchers.equalTo(expectedCount));
        }

        return response;
    }

    /**
     * Helper method to test if the search query returns the expected unordered set of results.
     */
    protected SearchResponse testSearchQueryUnordered(String query, Set<String> expectedNames)
    {
        Pagination paging = new Pagination();
        paging.setSkipCount(0);
        paging.setMaxItems(100);

        SearchResponse response = performSearch(testUser, query, SearchLanguage.AFTS, paging);

        Set<String> names = response.getEntries().stream()
                .map(s -> s.getModel().getName())
                .collect(Collectors.toSet());

        org.junit.Assert.assertEquals(
                "Unexpected results for query: " + query,
                expectedNames, names);

        return response;
    }
}