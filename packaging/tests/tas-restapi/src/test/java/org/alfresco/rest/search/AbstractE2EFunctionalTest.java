/*
 * #%L
 * Alfresco Search Services E2E Test
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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

import static java.util.Optional.ofNullable;

import static org.testng.Assert.assertEquals;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;

import org.alfresco.cmis.CmisWrapper;
import org.alfresco.dataprep.SiteService.Visibility;
import org.alfresco.rest.core.RestProperties;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.exception.EmptyJsonResponseException;
import org.alfresco.rest.model.RestRequestSpellcheckModel;
import org.alfresco.utility.LogFactory;
import org.alfresco.utility.TasProperties;
import org.alfresco.utility.Utility;
import org.alfresco.utility.data.DataContent;
import org.alfresco.utility.data.DataSite;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.CustomContentModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.network.ServerHealth;

/**
 * Base Spring-bootstrapped functional test class for Search API E2E tests. Owns: Spring context, autowired services, common user/site setup, generic search/query helpers, indexing-wait helpers, spellcheck helpers and custom-model lifecycle helpers.
 * <p>
 * Mirrors the structure of {@code AbstractE2EFunctionalTest} from the InsightEngine repo, which is the source of truth for these tests.
 */
@ContextConfiguration("classpath:alfresco-restapi-context.xml")
public abstract class AbstractE2EFunctionalTest extends AbstractTestNGSpringContextTests
{
    private static final Logger LOGGER = LogFactory.getLogger();

    /** Maximum number of times a wait-for-indexing query will be retried before giving up. */
    protected static final int SEARCH_MAX_ATTEMPTS = 120;

    /** CMIS query language identifier, used by {@link SearchLanguage#toString()}. */
    protected static final String SEARCH_LANGUAGE_CMIS = "cmis";

    @Autowired
    protected RestProperties restProperties;

    @Autowired
    protected TasProperties properties;

    @Autowired
    protected ServerHealth serverHealth;

    @Autowired
    protected DataSite dataSite;

    @Autowired
    protected DataContent dataContent;

    @Autowired
    protected RestWrapper restClient;

    @Autowired
    protected CmisWrapper cmisApi;

    @Autowired
    protected DataUser dataUser;

    protected UserModel testUser;
    protected UserModel adminUserModel;
    protected UserModel testUser2;

    protected SiteModel testSite;
    protected SiteModel testSite2;

    /** Random unique string derived from the test site name; used to build unique file titles. */
    protected static String unique_searchString;

    /** Search query language. {@link #toString()} returns the lower-case identifier expected by Search API. */
    protected enum SearchLanguage
    {
        CMIS, AFTS;

        @Override
        public String toString()
        {
            return name().toLowerCase();
        }
    }

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

    // -------------------------------------------------------------------------
    // Custom content model lifecycle
    // -------------------------------------------------------------------------

    /** Deploys a custom content model from the given classpath path. Path must end with {@code -model.xml}. */
    public boolean deployCustomModel(String path)
    {
        if (path == null || !path.endsWith("-model.xml"))
        {
            return false;
        }
        try
        {
            dataContent.usingAdmin().deployContentModel(path);
            return true;
        }
        catch (Exception e)
        {
            LOGGER.warn("Error loading custom model: " + path, e);
            return false;
        }
    }

    /** Deactivates a deployed custom content model via the private CMM REST API. */
    public boolean deactivateCustomModel(String modelFileName)
    {
        try
        {
            CustomContentModel model = new CustomContentModel();
            model.setName(fileNameToModelLocalName(modelFileName));
            restClient.authenticateUser(adminUserModel).withPrivateAPI().usingCustomModel(model).deactivateModel();
            return true;
        }
        catch (Exception e)
        {
            LOGGER.warn("Error deactivating custom model: " + modelFileName, e);
            return false;
        }
    }

    /** Deletes a deactivated custom content model via the private CMM REST API. */
    public boolean deleteCustomModel(String modelFileName)
    {
        try
        {
            CustomContentModel model = new CustomContentModel();
            model.setName(fileNameToModelLocalName(modelFileName));
            restClient.authenticateUser(adminUserModel).withPrivateAPI().usingCustomModel(model).deleteModel();
            return true;
        }
        catch (Exception e)
        {
            LOGGER.warn("Error deleting custom model: " + modelFileName, e);
            return false;
        }
    }

    /**
     * Converts a model file name like {@code "sharding-content-model.xml"} (or {@code "model/sharding-content-model.xml"}) into its CamelCase local name {@code "ShardingContentModel"} as expected by the private CMM REST API.
     */
    private String fileNameToModelLocalName(String modelFileName)
    {
        String base = modelFileName.contains("/")
                ? modelFileName.substring(modelFileName.lastIndexOf("/") + 1)
                : modelFileName;
        if (base.endsWith(".xml"))
        {
            base = base.substring(0, base.length() - 4);
        }
        StringBuilder sb = new StringBuilder();
        for (String part : base.split("-"))
        {
            if (!part.isEmpty())
            {
                sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }

    protected DataUser getDataUser()
    {
        return dataUser;
    }

    // -------------------------------------------------------------------------
    // Query helpers
    // -------------------------------------------------------------------------

    /** Executes a {@link SearchRequest} as {@link #testUser}. */
    protected SearchResponse query(SearchRequest query)
    {
        return restClient.authenticateUser(testUser).withSearchAPI().search(query);
    }

    /** Runs a simple AFTS query as admin and returns the response. */
    public SearchResponse query(String queryString)
    {
        return queryAsUser(dataUser.getAdminUser(), queryString);
    }

    /** Runs a search with the given query and highlight model as {@link #testUser}. */
    protected SearchResponse query(RestRequestQueryModel queryReq, RestRequestHighlightModel highlight)
    {
        SearchRequest searchReq = new SearchRequest(queryReq);
        searchReq.setHighlight(highlight);
        return restClient.authenticateUser(testUser).withSearchAPI().search(searchReq);
    }

    /** Runs an AFTS query as the given user. */
    protected SearchResponse queryAsUser(UserModel user, String queryString)
    {
        SearchRequest searchRequest = new SearchRequest();
        RestRequestQueryModel queryModel = new RestRequestQueryModel();
        queryModel.setQuery(queryString);
        searchRequest.setQuery(queryModel);
        return restClient.authenticateUser(user).withSearchAPI().search(searchRequest);
    }

    /** Runs the given query and spellcheck model as the given user. */
    protected SearchResponse queryAsUser(UserModel user, RestRequestQueryModel queryModel, RestRequestSpellcheckModel spellcheckQuery)
    {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setQuery(queryModel);
        searchRequest.setSpellcheck(spellcheckQuery);
        return restClient.authenticateUser(user).withSearchAPI().search(searchRequest);
    }

    /** Runs the given query as the given user, optionally with pagination. */
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

    /** Builds a {@link SearchRequest} from a single AFTS term. */
    protected SearchRequest createQuery(String term)
    {
        SearchRequest query = new SearchRequest();
        RestRequestQueryModel queryReq = new RestRequestQueryModel();
        queryReq.setQuery(term);
        query.setQuery(queryReq);
        return query;
    }

    // -------------------------------------------------------------------------
    // Wait-for-indexing helpers
    // -------------------------------------------------------------------------

    /** Waits using a free-form query (no field prefix injected). */
    public boolean waitForIndexing(String userQuery, boolean expectedInResults)
    {
        return waitForIndexing(null, userQuery, expectedInResults);
    }

    /** Waits for {@code name:'userQuery'} (metadata is indexed before content). */
    public boolean waitForMetadataIndexing(String userQuery, boolean expectedInResults)
    {
        return waitForIndexing("name", userQuery, expectedInResults);
    }

    /** Waits for {@code cm:content:'userQuery'} (content indexing can be slower than metadata). */
    public boolean waitForContentIndexing(String userQuery, boolean expectedInResults)
    {
        return waitForIndexing("cm:content", userQuery, expectedInResults);
    }

    private boolean waitForIndexing(String fieldName, String userQuery, boolean expectedInResults)
    {
        String query = (fieldName == null) ? userQuery : String.format("%s:'%s'", fieldName, userQuery);
        return isContentInSearchResults(query, null, expectedInResults);
    }

    /**
     * Polls the search API until the expected content appears or is absent from the results, or until {@link #SEARCH_MAX_ATTEMPTS} attempts have elapsed. Uses the configured Solr wait time per retry attempt.
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
                // Empty response — try again
            }
            catch (RuntimeException runtimeException)
            {
                if (searchCount > ignoreRuntimeExceptionThreshold)
                {
                    throw runtimeException;
                }
                LOGGER.warn("Ignoring initial Search API failure.", runtimeException);
            }
            finally
            {
                Utility.waitToLoopTime(properties.getSolrWaitTimeInSeconds(),
                        "Wait For Indexing. Retry Attempt: " + (searchCount + 1));
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
        String responseBody = restClient.onResponse().getResponse().body().prettyPrint();
        throw new RuntimeException("API returned status code: " + restClient.getStatusCode()
                + " Expected: " + HttpStatus.OK.value() + "; Response body: " + responseBody);
    }

    /** True if a result entry's name matches {@code contentName} (case-insensitive), or if {@code contentName} is blank. */
    public boolean isContentInSearchResponse(SearchResponse response, String contentName)
    {
        return response.getEntries().stream()
                .map(entry -> entry.getModel().getName())
                .anyMatch(name -> name.equalsIgnoreCase(contentName) || contentName.isBlank());
    }

    // -------------------------------------------------------------------------
    // testSearchQuery* helpers
    // -------------------------------------------------------------------------

    /** Runs a search and (optionally) asserts the {@code count} in the pagination response. Defaults to AFTS. */
    protected SearchResponse testSearchQuery(String query, Integer expectedCount)
    {
        return testSearchQuery(query, expectedCount, SearchLanguage.AFTS);
    }

    /** Runs a search using the given language and (optionally) asserts the {@code count} in the pagination response. */
    protected SearchResponse testSearchQuery(String query, Integer expectedCount, SearchLanguage queryLanguage)
    {
        SearchResponse response = performSearch(testUser, query, queryLanguage, getDefaultPagingOptions());
        if (expectedCount != null)
        {
            restClient.onResponse().assertThat().body("list.pagination.count", Matchers.equalTo(expectedCount));
        }
        return response;
    }

    /** Runs a search and asserts that the returned entry names exactly match {@code expectedNames} in the given order. */
    protected SearchResponse testSearchQueryOrdered(String query, List<String> expectedNames, SearchLanguage queryLanguage)
    {
        SearchResponse response = performSearch(testUser, query, queryLanguage, getDefaultPagingOptions());
        List<String> names = response.getEntries().stream()
                .map(s -> s.getModel().getName())
                .collect(Collectors.toList());
        assertEquals(names, expectedNames,
                "Unexpected results for query: " + query + " Expected: " + expectedNames + " but got " + names);
        return response;
    }

    /** Convenience overload using AFTS. */
    protected SearchResponse testSearchQueryUnordered(String query, Set<String> expectedNames)
    {
        return testSearchQueryUnordered(query, expectedNames, SearchLanguage.AFTS);
    }

    /** Runs a search and asserts that the returned entry names equal {@code expectedNames} as a set (order ignored). */
    protected SearchResponse testSearchQueryUnordered(String query, Set<String> expectedNames, SearchLanguage queryLanguage)
    {
        SearchResponse response = performSearch(testUser, query, queryLanguage, getDefaultPagingOptions());
        Set<String> names = response.getEntries().stream()
                .map(s -> s.getModel().getName())
                .collect(Collectors.toSet());
        assertEquals(names, expectedNames, "Unexpected results for query: " + query);
        return response;
    }

    protected SearchResponse performSearch(UserModel asUser, String query, SearchLanguage queryLanguage, Pagination paging)
    {
        RestRequestQueryModel queryModel = new RestRequestQueryModel();
        queryModel.setQuery(query);

        UserModel user = ofNullable(asUser).orElse(testUser);
        if (queryLanguage != null)
        {
            queryModel.setLanguage(queryLanguage.toString());
        }
        return queryAsUser(user, queryModel, paging);
    }

    private Pagination getDefaultPagingOptions()
    {
        Pagination paging = new Pagination();
        paging.setSkipCount(0);
        paging.setMaxItems(100);
        return paging;
    }

    /** Builds a pagination object from optional {@code skipCount} / {@code maxItems}. */
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

    // -------------------------------------------------------------------------
    // Spellcheck helpers
    // -------------------------------------------------------------------------

    /**
     * Builds and runs a spellcheck-enabled search request:
     * 
     * <pre>
     * {
     *   "query":      { "query": "&lt;query&gt;",     "userQuery": "&lt;userQuery&gt;" },
     *   "spellcheck": { "query": "&lt;userQuery&gt;" }
     * }
     * </pre>
     * 
     * If {@code user} is {@code null}, {@link #testUser} is used.
     */
    protected SearchResponse SearchSpellcheckQuery(UserModel user, String query, String userQuery)
    {
        RestRequestSpellcheckModel spellCheck = new RestRequestSpellcheckModel();
        spellCheck.setQuery(userQuery);

        UserModel searchUser = ofNullable(user).orElse(testUser);
        RestRequestQueryModel queryReq = new RestRequestQueryModel();
        queryReq.setQuery(query);
        queryReq.setUserQuery(userQuery);
        return queryAsUser(searchUser, queryReq, spellCheck);
    }

    /**
     * Asserts the {@code spellCheck} object on a search response context:
     * <ul>
     * <li>If {@code spellCheckType} is {@code null}, asserts the {@code spellCheck} field is null.</li>
     * <li>Otherwise asserts {@code type} equals {@code spellCheckType}.</li>
     * <li>If {@code spellCheckSuggestion} is non-null, asserts it appears in {@code suggestions}.</li>
     * </ul>
     */
    public void testSearchSpellcheckResponse(SearchResponse response, String spellCheckType, String spellCheckSuggestion)
    {
        if (spellCheckType != null)
        {
            response.getContext().assertThat().field("spellCheck").isNotEmpty();
            response.getContext().getSpellCheck().assertThat().field("type").is(spellCheckType);
        }
        else
        {
            response.getContext().assertThat().field("spellCheck").isNull();
        }

        if (spellCheckSuggestion != null)
        {
            response.getContext().assertThat().field("spellCheck").isNotEmpty();
            response.getContext().getSpellCheck().assertThat().field("suggestions").contains(spellCheckSuggestion);
        }
    }
}
