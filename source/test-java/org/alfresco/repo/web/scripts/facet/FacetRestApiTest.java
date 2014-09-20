/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.facet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.impl.solr.facet.SolrFacetService;
import org.alfresco.repo.search.impl.solr.facet.SolrFacetServiceImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.collections.CollectionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;
import org.springframework.extensions.webscripts.TestWebScriptServer.*;

/**
 * This class tests the ReST API of the {@link SolrFacetService}.
 * 
 * @author Neil Mc Erlean
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public class FacetRestApiTest extends BaseWebScriptTest
{
    private static final String SEARCH_ADMIN_USER     = "searchAdmin";
    private static final String NON_SEARCH_ADMIN_USER = "nonSearchAdmin";

    private static final String FACETS = "facets";
    
    private final static String GET_ALL_FACETABLE_PROPERTIES_URL      = "/api/facet/facetable-properties";
    private final static String GET_SPECIFIC_FACETABLE_PROPERTIES_URL = "/api/facet/classes/{classname}/facetable-properties";
    private final static String GET_FACETS_URL       = "/api/facet/facet-config";
    private final static String PUT_FACET_URL_FORMAT = "/api/facet/facet-config/{0}?relativePos={1}";
    private final static String POST_FACETS_URL      = GET_FACETS_URL;
    private final static String PUT_FACETS_URL       = GET_FACETS_URL;

    private MutableAuthenticationService authenticationService;
    private AuthorityService             authorityService;
    private PersonService                personService;
    private RetryingTransactionHelper    transactionHelper;
    private List<String> filters = new ArrayList<String>();

    @Override protected void setUp() throws Exception
    {
        super.setUp();
        authenticationService = getServer().getApplicationContext().getBean("AuthenticationService", MutableAuthenticationService.class);
        authorityService      = getServer().getApplicationContext().getBean("AuthorityService", AuthorityService.class);
        personService         = getServer().getApplicationContext().getBean("PersonService", PersonService.class);
        transactionHelper     = getServer().getApplicationContext().getBean("retryingTransactionHelper", RetryingTransactionHelper.class);

        AuthenticationUtil.clearCurrentSecurityContext();
        // Create test users. TODO Create these users @BeforeClass or at a testsuite scope.
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override public Void doWork() throws Exception
            {
                createUser(SEARCH_ADMIN_USER);
                createUser(NON_SEARCH_ADMIN_USER);

                if ( !authorityService.getContainingAuthorities(AuthorityType.GROUP,
                                                                SEARCH_ADMIN_USER,
                                                                true)
                                    .contains(SolrFacetServiceImpl.GROUP_ALFRESCO_SEARCH_ADMINISTRATORS_AUTHORITY))
                {
                    authorityService.addAuthority(SolrFacetServiceImpl.GROUP_ALFRESCO_SEARCH_ADMINISTRATORS_AUTHORITY,
                                                  SEARCH_ADMIN_USER);
                }
                return null;
            }
        });
    }

    @Override public void tearDown() throws Exception
    {
        super.tearDown();

        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                deleteFilters();
                return null;
            }
        }, SEARCH_ADMIN_USER);

        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override public Void doWork() throws Exception
            {
                transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        deleteUser(SEARCH_ADMIN_USER);
                        deleteUser(NON_SEARCH_ADMIN_USER);
                        return null;
                    }
                });
                return null;
            }
        });
        AuthenticationUtil.clearCurrentSecurityContext();
    }

    public void testNonSearchAdminUserCannotCreateUpdateSolrFacets() throws Exception
    {
        // Create a filter
        final JSONObject filter = new JSONObject();
        final String filterName = "filter" + System.currentTimeMillis();
        filters.add(filterName);
        filter.put("filterID", filterName);
        filter.put("facetQName", "{http://www.alfresco.org/model/content/1.0}test1");
        filter.put("displayName", "facet-menu.facet.test1");
        filter.put("displayControl", "alfresco/search/FacetFilters/test1");
        filter.put("maxFilters", 5);
        filter.put("hitThreshold", 1);
        filter.put("minFilterValueLength", 4);
        filter.put("sortBy", "ALPHABETICALLY");

        // Non-Search-Admin tries to create a filter
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                // Post the filter
                sendRequest(new PostRequest(POST_FACETS_URL, filter.toString(), "application/json"), 403);
                return null;
            }
        }, NON_SEARCH_ADMIN_USER);

        // Search-Admin creates a filter
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                // Post the filter
                sendRequest(new PostRequest(POST_FACETS_URL, filter.toString(), "application/json"), 200);
                return null;
            }
        }, SEARCH_ADMIN_USER);

        // Non-Search-Admin tries to modify the filter
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                Response response = sendRequest(new GetRequest(GET_FACETS_URL + "/" + filterName), 200);
                JSONObject jsonRsp = new JSONObject(new JSONTokener(response.getContentAsString()));
                assertEquals(filterName, jsonRsp.getString("filterID"));
                assertEquals(5, jsonRsp.getInt("maxFilters"));
                // Now change the maxFilters value and try to update
                jsonRsp.put("maxFilters", 10);
                sendRequest(new PutRequest(PUT_FACETS_URL, jsonRsp.toString(), "application/json"), 403);

                return null;
            }
        }, NON_SEARCH_ADMIN_USER);
    }

    public void testNonSearchAdminUserCanGetFacets() throws Exception
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override public Void doWork() throws Exception
            {
                Response response = sendRequest(new GetRequest(GET_FACETS_URL), 200);
                JSONObject jsonRsp = new JSONObject(new JSONTokener(response.getContentAsString()));
                List<String> filters = getListFromJsonArray(jsonRsp.getJSONArray(FACETS));
                assertTrue(filters.size() > 0);
                return null;
            }
        }, NON_SEARCH_ADMIN_USER);
    }

    public void testSearchAdminCanGetFacets() throws Exception
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override public Void doWork() throws Exception
            {
                Response rsp = sendRequest(new GetRequest(GET_FACETS_URL), 200);

                String contentAsString = rsp.getContentAsString();
                JSONObject jsonRsp = new JSONObject(new JSONTokener(contentAsString));

                // FIXME The JSON payload should be contained within a 'data' object.
                JSONArray facetsArray = (JSONArray)jsonRsp.get(FACETS);
                assertNotNull("JSON 'facets' array was null", facetsArray);

                // We'll not add any further assertions on the JSON content. If we've
                // got valid JSON at this point, then that's good enough.
                return null;
            }
        }, SEARCH_ADMIN_USER);
    }

    public void testSearchAdminReordersFacets() throws Exception
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override public Void doWork() throws Exception
            {
                // get the existing facets.
                Response rsp = sendRequest(new GetRequest(GET_FACETS_URL), 200);

                JSONObject jsonRsp = new JSONObject(new JSONTokener(rsp.getContentAsString()));

                final JSONArray facetsArray = (JSONArray)jsonRsp.get(FACETS);
                assertNotNull("JSON 'facets' array was null", facetsArray);

                System.out.println("Received " + facetsArray.length() + " facets");

                final List<String> idsIndexes = getListFromJsonArray(facetsArray);

                System.out.println(" IDs, indexes = " + idsIndexes);

                // Reorder them such that the last facet is moved left one place.
                assertTrue("There should be more than 1 built-in facet", facetsArray.length() > 1);

                final String lastIndexId = idsIndexes.get(idsIndexes.size() - 1);
                final String url = PUT_FACET_URL_FORMAT.replace("{0}", lastIndexId)
                                                       .replace("{1}", "-1");
                rsp = sendRequest(new PutRequest(url, "", "application/json"), 200);


                // Now get the facets back and we should see that one has moved.
                rsp = sendRequest(new GetRequest(GET_FACETS_URL), 200);

                jsonRsp = new JSONObject(new JSONTokener(rsp.getContentAsString()));

                JSONArray newfacetsArray = (JSONArray)jsonRsp.get(FACETS);
                assertNotNull("JSON 'facets' array was null", newfacetsArray);

                System.out.println("Received " + newfacetsArray.length() + " facets");

                final List<String> newIdsIndexes = getListFromJsonArray(newfacetsArray);

                System.out.println(" IDs, indexes = " + newIdsIndexes);

                // Note here that the last Facet JSON object *is* moved one place up the list.
                assertEquals(CollectionUtils.moveLeft(1, lastIndexId, idsIndexes), newIdsIndexes);
                return null;
            }
        }, SEARCH_ADMIN_USER);
    }

    public void testDefaultValues() throws Exception
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                // Build the Filter object - ignore the optional values
                JSONObject filter_one = new JSONObject();
                String filterNameOne = "filterOne" + System.currentTimeMillis();
                filters.add(filterNameOne);
                filter_one.put("filterID", filterNameOne);
                filter_one.put("facetQName", "{http://www.alfresco.org/model/content/1.0}test1");
                filter_one.put("displayName", "facet-menu.facet.test1");
                filter_one.put("displayControl", "alfresco/search/FacetFilters/test1");
                filter_one.put("maxFilters", 5);
                filter_one.put("hitThreshold", 1);
                filter_one.put("minFilterValueLength", 4);
                filter_one.put("sortBy", "ALPHABETICALLY");

                // Post the filter
                Response response = sendRequest(new PostRequest(POST_FACETS_URL, filter_one.toString(),"application/json"), 200);

                // Retrieve the created filter
                response = sendRequest(new GetRequest(GET_FACETS_URL + "/" + filterNameOne), 200);
                JSONObject jsonRsp = new JSONObject(new JSONTokener(response.getContentAsString()));
                assertEquals(filterNameOne, jsonRsp.getString("filterID"));
                assertEquals("{http://www.alfresco.org/model/content/1.0}test1", jsonRsp.getString("facetQName"));
                assertEquals("facet-menu.facet.test1", jsonRsp.getString("displayName"));
                assertEquals("alfresco/search/FacetFilters/test1", jsonRsp.getString("displayControl"));
                assertEquals(5, jsonRsp.getInt("maxFilters"));
                assertEquals(1, jsonRsp.getInt("hitThreshold"));
                assertEquals(4, jsonRsp.getInt("minFilterValueLength"));
                assertEquals("ALPHABETICALLY", jsonRsp.getString("sortBy"));
                // Check the Default values
                assertEquals("ALL", jsonRsp.getString("scope"));
                assertFalse(jsonRsp.getBoolean("isEnabled"));
                assertFalse(jsonRsp.getBoolean("isDefault"));

                // Build the Filter object with all the values
                JSONObject filter_two = new JSONObject();
                String filterNameTwo = "filterTwo" + System.currentTimeMillis();
                filters.add(filterNameTwo);
                filter_two.put("filterID", filterNameTwo);
                filter_two.put("facetQName", "{http://www.alfresco.org/model/content/1.0}test2");
                filter_two.put("displayName", "facet-menu.facet.test2");
                filter_two.put("displayControl", "alfresco/search/FacetFilters/test2");
                filter_two.put("maxFilters", 5);
                filter_two.put("hitThreshold", 1);
                filter_two.put("minFilterValueLength", 4);
                filter_two.put("sortBy", "ALPHABETICALLY");
                filter_two.put("scope", "SCOPED_SITES");
                List<String> expectedValues = Arrays.asList(new String[] { "sit1", "site2", "site3" });
                filter_two.put("scopedSites", expectedValues);
                filter_two.put("isEnabled", true);

                // Post the filter
                response = sendRequest(new PostRequest(POST_FACETS_URL, filter_two.toString(), "application/json"), 200);

                // Retrieve the created filter
                response = sendRequest(new GetRequest(GET_FACETS_URL + "/" + filterNameTwo), 200);
                jsonRsp = new JSONObject(new JSONTokener(response.getContentAsString()));

                assertEquals(filterNameTwo, jsonRsp.getString("filterID"));
                assertEquals("SCOPED_SITES", jsonRsp.getString("scope"));
                assertTrue(jsonRsp.getBoolean("isEnabled"));
                JSONArray jsonArray = jsonRsp.getJSONArray("scopedSites");
                List<String> retrievedValues = getListFromJsonArray(jsonArray);
                // Sort the list
                Collections.sort(retrievedValues);
                assertEquals(expectedValues, retrievedValues);

                return null;
            }
        }, SEARCH_ADMIN_USER);
    }

    public void testFacetCustomProperties() throws Exception
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                // Build the Filter object
                JSONObject filter = new JSONObject();
                String filterName = "filter" + System.currentTimeMillis();
                filters.add(filterName);
                filter.put("filterID", filterName);
                filter.put("facetQName", "{http://www.alfresco.org/model/content/1.0}content.size.test");
                filter.put("displayName", "facet-menu.facet.size.test");
                filter.put("displayControl", "alfresco/search/FacetFilters/test");
                filter.put("maxFilters", 5);
                filter.put("hitThreshold", 1);
                filter.put("minFilterValueLength", 4);
                filter.put("sortBy", "ALPHABETICALLY");

                JSONObject customProp = new JSONObject();
                // 1st custom prop
                JSONObject blockIncludeRequest = new JSONObject();
                blockIncludeRequest.put("name", "blockIncludeFacetRequest");
                blockIncludeRequest.put("value", "true");
                customProp.put("blockIncludeFacetRequest", blockIncludeRequest);

                // 2nd custom prop
                JSONObject multipleValue = new JSONObject();
                multipleValue.put("name", "multipleValueTest");
                List<String> expectedValues = Arrays.asList(new String[] { "sit1", "site2", "site3" });
                multipleValue.put("value", expectedValues);
                customProp.put("multipleValueTest", multipleValue);

                filter.put("customProperties", customProp);

                // Post the filter
                Response response = sendRequest(new PostRequest(POST_FACETS_URL, filter.toString(),"application/json"), 200);
                // Retrieve the created filter
                response = sendRequest(new GetRequest(GET_FACETS_URL + "/" + filterName), 200);
                JSONObject jsonRsp = new JSONObject(new JSONTokener(response.getContentAsString()));
                customProp = jsonRsp.getJSONObject("customProperties");

                blockIncludeRequest = customProp.getJSONObject("blockIncludeFacetRequest");
                assertEquals("{http://www.alfresco.org/model/solrfacetcustomproperty/1.0}blockIncludeFacetRequest", blockIncludeRequest.get("name"));
                assertEquals("true", blockIncludeRequest.get("value"));

                multipleValue = customProp.getJSONObject("multipleValueTest");
                assertEquals("{http://www.alfresco.org/model/solrfacetcustomproperty/1.0}multipleValueTest", multipleValue.get("name"));

                JSONArray jsonArray = (JSONArray) multipleValue.get("value");
                List<String> retrievedValues = getListFromJsonArray(jsonArray);
                // Sort the list
                Collections.sort(retrievedValues);
                assertEquals(expectedValues, retrievedValues);

                return null;
            }
        }, SEARCH_ADMIN_USER);
    }

    public void testCreateUpdateFacetWithInvalidFilterId() throws Exception
    {
        // Build the Filter object
        final JSONObject filter = new JSONObject();
        final String filterName = "filter" + System.currentTimeMillis();
        filters.add(filterName);
        filter.put("filterID", filterName);
        filter.put("facetQName", "{http://www.alfresco.org/model/content/1.0}test1");
        filter.put("displayName", "facet-menu.facet.test1");
        filter.put("displayControl", "alfresco/search/FacetFilters/test1");
        filter.put("maxFilters", 5);
        filter.put("hitThreshold", 1);
        filter.put("minFilterValueLength", 4);
        filter.put("sortBy", "ALPHABETICALLY");

        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                // Post the filter
                sendRequest(new PostRequest(POST_FACETS_URL, filter.toString(), "application/json"), 200);
                return null;
            }
        }, SEARCH_ADMIN_USER);

        // Admin tries to change the FilterID value
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                // Retrieve the created filter
                Response response = sendRequest(new GetRequest(GET_FACETS_URL + "/" + filterName), 200);
                JSONObject jsonRsp = new JSONObject(new JSONTokener(response.getContentAsString()));
                assertEquals(filterName, jsonRsp.getString("filterID"));
                // Now change the filterID value and try to update
                jsonRsp.put("filterID", filterName + "Modified");
                sendRequest(new PutRequest(PUT_FACETS_URL, jsonRsp.toString(), "application/json"), 400);

                return null;
            }
        }, SEARCH_ADMIN_USER);

        // Admin tries to create a filter with a duplicate FilterID
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                // Post the filter
                sendRequest(new PostRequest(POST_FACETS_URL, filter.toString(), "application/json"), 400);

                return null;
            }
        }, SEARCH_ADMIN_USER);

        // Admin tries to create a filter with a malicious FilterID
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                Response response = sendRequest(new GetRequest(GET_FACETS_URL), 200);
                JSONObject jsonRsp = new JSONObject(new JSONTokener(response.getContentAsString()));
                JSONArray facetsArray = (JSONArray) jsonRsp.get(FACETS);
                assertNotNull("JSON 'facets' array was null", facetsArray);
                final List<String> facets = getListFromJsonArray(facetsArray);

                filter.put("filterID", "<script>alert('Maliciouse-FilterID')</script>");
                // Post the filter
                sendRequest(new PostRequest(POST_FACETS_URL, filter.toString(), "application/json"), 500);

                // Retrieve all filters
                response = sendRequest(new GetRequest(GET_FACETS_URL), 200);
                jsonRsp = new JSONObject(new JSONTokener(response.getContentAsString()));
                facetsArray = (JSONArray) jsonRsp.get(FACETS);

                assertNotNull("JSON 'facets' array was null", facetsArray);
                final List<String> newFacets = getListFromJsonArray(facetsArray);
                assertEquals(facets, newFacets);

                return null;
            }
        }, SEARCH_ADMIN_USER);

    }

    public void testUpdateSingleValue() throws Exception
    {
        // Build the Filter object
        final JSONObject filter = new JSONObject();
        final String filterName = "filter" + System.currentTimeMillis();
        filters.add(filterName);
        filter.put("filterID", filterName);
        filter.put("facetQName", "{http://www.alfresco.org/model/content/1.0}test");
        filter.put("displayName", "facet-menu.facet.test1");
        filter.put("displayControl", "alfresco/search/FacetFilters/test");
        filter.put("maxFilters", 5);
        filter.put("hitThreshold", 1);
        filter.put("minFilterValueLength", 4);
        filter.put("sortBy", "ALPHABETICALLY");
        filter.put("isEnabled", true);

        JSONObject customProp = new JSONObject();
        // 1st custom prop
        JSONObject blockIncludeRequest = new JSONObject();
        blockIncludeRequest.put("name", "blockIncludeFacetRequest");
        blockIncludeRequest.put("value", "true");
        customProp.put("blockIncludeFacetRequest", blockIncludeRequest);
        filter.put("customProperties", customProp);

        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                // Post the filter
                sendRequest(new PostRequest(POST_FACETS_URL, filter.toString(), "application/json"), 200);
                return null;
            }
        }, SEARCH_ADMIN_USER);

        // Admin updates displayName and facetQName in 2 put requests
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                // Retrieve the created filter
                Response response = sendRequest(new GetRequest(GET_FACETS_URL + "/" + filterName), 200);
                JSONObject jsonRsp = new JSONObject(new JSONTokener(response.getContentAsString()));

                assertEquals(filterName, jsonRsp.getString("filterID"));
                assertEquals("facet-menu.facet.test1", jsonRsp.getString("displayName"));
                assertEquals("{http://www.alfresco.org/model/content/1.0}test", jsonRsp.getString("facetQName"));
                assertTrue(jsonRsp.getBoolean("isEnabled"));

                // Just supply the filterID and the required value
                JSONObject singleValueJson = new JSONObject();
                singleValueJson.put("filterID", filterName);
                // Change the displayName value and update
                singleValueJson.put("displayName", "facet-menu.facet.modifiedValue");
                sendRequest(new PutRequest(PUT_FACETS_URL, singleValueJson.toString(), "application/json"), 200);

                // Change the isEnabled value and update
                // We simulate two PUT requests without refreshing the page in
                // between updates
                singleValueJson = new JSONObject();
                singleValueJson.put("filterID", filterName);
                singleValueJson.put("isEnabled", false);
                sendRequest(new PutRequest(PUT_FACETS_URL, singleValueJson.toString(), "application/json"), 200);

                response = sendRequest(new GetRequest(GET_FACETS_URL + "/" + filterName), 200);
                jsonRsp = new JSONObject(new JSONTokener(response.getContentAsString()));

                // Now see if the two changes have been persisted
                assertEquals("facet-menu.facet.modifiedValue", jsonRsp.getString("displayName"));
                assertFalse(jsonRsp.getBoolean("isEnabled"));
                // Make sure the rest of values haven't been changed
                assertEquals(filterName, jsonRsp.getString("filterID"));
                assertEquals("{http://www.alfresco.org/model/content/1.0}test", jsonRsp.getString("facetQName"));
                assertEquals("alfresco/search/FacetFilters/test", jsonRsp.getString("displayControl"));
                assertEquals(5, jsonRsp.getInt("maxFilters"));
                assertEquals(1, jsonRsp.getInt("hitThreshold"));
                assertEquals(4, jsonRsp.getInt("minFilterValueLength"));
                assertEquals("ALPHABETICALLY", jsonRsp.getString("sortBy"));
                assertEquals("ALL", jsonRsp.getString("scope"));
                assertFalse(jsonRsp.getBoolean("isDefault"));
                // Make sure custom properties haven't been deleted
                JSONObject retrievedCustomProp = jsonRsp.getJSONObject("customProperties");
                JSONObject retrievedBlockIncludeRequest = retrievedCustomProp.getJSONObject("blockIncludeFacetRequest");
                assertEquals("{http://www.alfresco.org/model/solrfacetcustomproperty/1.0}blockIncludeFacetRequest", retrievedBlockIncludeRequest.get("name"));
                assertEquals("true", retrievedBlockIncludeRequest.get("value"));

                // Change the facetQName value and update
                singleValueJson = new JSONObject();
                singleValueJson.put("filterID", filterName);
                singleValueJson.put("facetQName", "{http://www.alfresco.org/model/content/1.0}testModifiedValue");
                // We simulate that 'testModifiedValue' QName doesn't have custom properties
                singleValueJson.put("customProperties", new JSONObject());
                sendRequest(new PutRequest(PUT_FACETS_URL, singleValueJson.toString(), "application/json"), 200);

                response = sendRequest(new GetRequest(GET_FACETS_URL + "/" + filterName), 200);
                jsonRsp = new JSONObject(new JSONTokener(response.getContentAsString()));

                // Now see if the facetQName and its side-effect have been persisted
                assertEquals("{http://www.alfresco.org/model/content/1.0}testModifiedValue",jsonRsp.getString("facetQName"));
                assertNull("Custom properties should have been deleted.", jsonRsp.opt("customProperties"));
                // Make sure the rest of values haven't been changed
                assertEquals(filterName, jsonRsp.getString("filterID"));
                assertEquals("facet-menu.facet.modifiedValue", jsonRsp.getString("displayName"));
                assertEquals("alfresco/search/FacetFilters/test", jsonRsp.getString("displayControl"));
                assertEquals(5, jsonRsp.getInt("maxFilters"));
                assertEquals(1, jsonRsp.getInt("hitThreshold"));
                assertEquals(4, jsonRsp.getInt("minFilterValueLength"));
                assertEquals("ALPHABETICALLY", jsonRsp.getString("sortBy"));
                assertFalse(jsonRsp.getBoolean("isDefault"));
                assertEquals("ALL", jsonRsp.getString("scope"));
                assertFalse(jsonRsp.getBoolean("isEnabled"));

                return null;
            }
        }, SEARCH_ADMIN_USER);
    }
    
    public void testGetAllFacetableProperties() throws Exception
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override public Void doWork() throws Exception
            {
                final Response rsp = sendRequest(new GetRequest(GET_ALL_FACETABLE_PROPERTIES_URL), 200);
                
                // For now, we'll only perform limited testing of the response as we primarily
                // want to know that the GET call succeeded and that it correctly identified
                // *some* facetable properties.
                JSONObject jsonRsp = new JSONObject(new JSONTokener(rsp.getContentAsString()));
                
                JSONObject data = jsonRsp.getJSONObject("data");
                JSONArray properties = data.getJSONArray(FacetablePropertiesGet.PROPERTIES_KEY);
                
                final int arbitraryLimit = 25;
                assertTrue("Expected 'many' properties, but found 'not very many'", properties.length() > arbitraryLimit);
                
                return null;
            }
        }, SEARCH_ADMIN_USER);
    }
    
    public void testGetFacetablePropertiesForSpecificContentClasses() throws Exception
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override public Void doWork() throws Exception
            {
                final Response rsp = sendRequest(new GetRequest(GET_SPECIFIC_FACETABLE_PROPERTIES_URL.replace("{classname}", "cm:content")), 200);
                
                // For now, we'll only perform limited testing of the response as we primarily
                // want to know that the GET call succeeded and that it correctly identified
                // *some* facetable properties.
                JSONObject jsonRsp = new JSONObject(new JSONTokener(rsp.getContentAsString()));
                
                JSONObject data = jsonRsp.getJSONObject("data");
                JSONArray properties = data.getJSONArray(FacetablePropertiesGet.PROPERTIES_KEY);
                
                final int arbitraryLimit = 100;
                assertTrue("Expected 'not very many' properties, but found 'many'", properties.length() < arbitraryLimit);
                
                return null;
            }
        }, SEARCH_ADMIN_USER);
    }
    
    private List<String> getListFromJsonArray(JSONArray facetsArray) throws JSONException
    {
        List<String> result = new ArrayList<>();

        for (int i = 0; i < facetsArray.length(); i++)
        {
            Object object = facetsArray.get(i);
            if (object instanceof JSONObject)
            {
                final JSONObject nextFacet = (JSONObject) object;
                final String nextId = nextFacet.getString("filterID");
                result.add(nextId);
            }
            else
            {
                result.add((String) object);
            }
        }
        return result;
    }

    private void createUser(String userName)
    {
        if (! authenticationService.authenticationExists(userName))
        {
            authenticationService.createAuthentication(userName, "PWD".toCharArray());
        }

        if (! personService.personExists(userName))
        {
            PropertyMap ppOne = new PropertyMap(4);
            ppOne.put(ContentModel.PROP_USERNAME, userName);
            ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
            ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
            ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
            ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");

            personService.createPerson(ppOne);
        }
    }

    private void deleteUser(String userName)
    {
        if (personService.personExists(userName))
        {
            personService.deletePerson(userName);
        }
    }

    private void deleteFilters() throws IOException
    {
        for (String filter : filters)
        {
            sendRequest(new DeleteRequest(GET_FACETS_URL + "/" + filter), 200);
        }
    }
}
