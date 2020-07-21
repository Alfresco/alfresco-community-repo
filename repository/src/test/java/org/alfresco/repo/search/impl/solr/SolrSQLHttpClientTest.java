/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.solr;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;

import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.admin.RepositoryState;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParserException;
import org.alfresco.repo.search.impl.lucene.SolrJsonProcessor;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.Pair;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

/** Tests for the {@link org.alfresco.repo.search.impl.solr.SolrSQLHttpClient}. */
public class SolrSQLHttpClientTest
{
    /** A language to use in the tests. */
    private static final String LANGUAGE = "LANGUAGE";
    /** A store to use for the tests. */
    private static final StoreRef STORE_REF = new StoreRef("STORE://REF/");

    /**
     * The class under test.
     *
     * We use a spy to allow stubbing {@link SolrSQLHttpClient#postQuery}, which
     * relies on manipulation of a {@link PostMethod} object.
     */
    @Spy
    @InjectMocks
    private SolrSQLHttpClient solrSQLHttpClient;
    /** An object returned by calls to {@code postSolrQuery}. */
    @Mock
    private ResultSet mockResultSet;
    @Mock
    private RepositoryState mockRepositoryState;
    @Mock
    private SearchParameters mockSearchParameters;
    @Mock
    private SolrStoreMappingWrapper mockSolrStoreMappingWrapper;
    @Mock
    private HttpClient mockHttpClient;
    @Mock
    private HostConfiguration mockHostConfiguration;
    @Mock
    private PermissionService mockPermissionService;
    @Mock
    private TenantService mockTenantService;
    /** A captor for the HTTP body sent to Solr. */
    @Captor
    private ArgumentCaptor<JSONObject> bodyCaptor;

    @Before
    public void setUp() throws Exception
    {
        initMocks(this);

        // Set up the mock HTTP call method on the class under test.
        doReturn(mockResultSet).when(solrSQLHttpClient).postSolrQuery(any(HttpClient.class), anyString(),
                    bodyCaptor.capture(), // Capture the supplied HTTP request body.
                    any(SolrJsonProcessor.class));
        // Set up the store configuration.
        when(mockSearchParameters.getStores()).thenReturn(new ArrayList(asList(STORE_REF)));
        HashMap<StoreRef, SolrStoreMappingWrapper> mappingLookup = new HashMap<>();
        mappingLookup.put(STORE_REF, mockSolrStoreMappingWrapper);
        // Set up the HTTP configuration.
        solrSQLHttpClient.setMappingLookup(mappingLookup);
        when(mockHttpClient.getHostConfiguration()).thenReturn(mockHostConfiguration);
        when(mockHostConfiguration.getHostURL()).thenReturn("hostURL");
        Pair<HttpClient, String> httpClientAndBaseUrl = new Pair<>(mockHttpClient, "baseURL");
        when(mockSolrStoreMappingWrapper.getHttpClientAndBaseUrl()).thenReturn(httpClientAndBaseUrl);
        // Set up the other services.
        when(mockPermissionService.getAuthorisations()).thenReturn(emptySet());
        when(mockTenantService.getCurrentUserDomain()).thenReturn("currentUserDomain");
        when(mockSearchParameters.getQuery()).thenReturn("statement");
    }

    /** Check that an exception is thrown if a query is executed while bootstrapping. */
    @Test(expected = AlfrescoRuntimeException.class)
    public void testExecuteQuery_bootstrapping()
    {
        when(mockRepositoryState.isBootstrapping()).thenReturn(true);

        // Call the method under test.
        solrSQLHttpClient.executeQuery(mockSearchParameters, LANGUAGE);
    }

    /** Check that an exception is thrown if an empty query is executed. */
    @Test(expected = AlfrescoRuntimeException.class)
    public void testExecuteQuery_queryMissing()
    {
        // Override the behaviour in the setUp method.
        when(mockSearchParameters.getQuery()).thenReturn(null);

        // Call the method under test.
        solrSQLHttpClient.executeQuery(mockSearchParameters, LANGUAGE);
    }

    /** Check executing a minimal query makes a HTTP call and returns the result. */
    @Test
    public void testExecuteQuery_minimalQuery()
    {
        // Call the method under test.
        ResultSet resultSet = solrSQLHttpClient.executeQuery(mockSearchParameters, LANGUAGE);

        assertEquals("Expected result to come back from HTTP call.", mockResultSet, resultSet);
    }

    /** Check that an exception is thrown if the Insight Engine can't be reached. */
    @Test
    public void testExecuteQuery_connectException() throws Exception
    {
        // Replace the mock HTTP call method so it throws a ConnectException.
        doThrow(new ConnectException()).when(solrSQLHttpClient).postSolrQuery(any(HttpClient.class), anyString(),
                    any(JSONObject.class),
                    any(SolrJsonProcessor.class));

        // Call the method under test.
        try
        {
            solrSQLHttpClient.executeQuery(mockSearchParameters, LANGUAGE);
            fail("Expected exception to be thrown due to failed connection.");
        }
        catch (LuceneQueryParserException e)
        {
            assertTrue("Expected message to mention InsightEngine.", e.getMessage().contains("InsightEngine"));
        }
    }

    /** Check that a query can be combined with filter queries. */
    @Test
    public void testExecuteQuery_filterQueries() throws JSONException
    {
        when(mockSearchParameters.getFilterQueries()).thenReturn(asList("FQ1", "FQ2"));

        // Call the method under test.
        ResultSet resultSet = solrSQLHttpClient.executeQuery(mockSearchParameters, LANGUAGE);

        assertEquals("Expected result to come back from HTTP call.", mockResultSet, resultSet);
        List<String> actual = stringJsonArrayToList(bodyCaptor.getValue().getJSONArray("filterQueries"));
        assertEquals("Unexpected filter queries in HTTP request.",
                    actual, asList("FQ1", "FQ2"));
    }

    /**
     * Convert a JSONArray of strings to a list of strings.
     *
     * @param jsonArray The JSON array.
     * @return A list of strings.
     * @throws JSONException Unexpected.
     */
    private List<String> stringJsonArrayToList(JSONArray jsonArray) throws JSONException
    {
        List<String> stringList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++)
        {
            stringList.add(jsonArray.getString(i));
        }
        return stringList;
    }
}
