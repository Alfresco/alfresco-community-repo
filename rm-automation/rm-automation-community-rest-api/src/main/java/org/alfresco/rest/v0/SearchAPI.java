/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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

package org.alfresco.rest.v0;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.alfresco.dataprep.AlfrescoHttpClient;
import org.alfresco.dataprep.AlfrescoHttpClientFactory;
import org.alfresco.rest.core.v0.BaseAPI;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Helper methods for performing search using various Alfresco search APIs.
 *
 * @author Kristijan Conkas
 * @since 2.5
 */
@Component
public class SearchAPI extends BaseAPI
{

    /** http client factory */
    @Autowired
    private AlfrescoHttpClientFactory alfrescoHttpClientFactory;

    /** search API endpoint */
    private static final String SEARCH_ENDPOINT = "{0}alfresco/s/slingshot/search?{1}";

    /** faceted search API endpoint */
    private static final String FACETED_SEARCH_ENDPOINT = "{0}alfresco/s/slingshot/rmsearch/faceted/rmsearch?{1}";

    /** RM search URL template */
    private static final String RM_SEARCH_ENDPOINT = "{0}alfresco/s/slingshot/rmsearch/{1}?{2}";

    /** RM document search filters */
    private static final String RM_DEFAULT_RECORD_FILTERS =
        "records/true,undeclared/true,vital/false,folders/false,categories/false,frozen/false,cutoff/false";

    /**
     * Perform search request on search endpoint as a user.
     * <p>
     * This method is applicable only to endpoints that support HTTP GET requests and return JSON body as response.
     * @param searchEndpoint
     * @param searchUser
     * @param searchPassword
     * @return search results as a {@link JSONObject}, please refer to API documentation for details
     */
    private JSONObject doSearch(
        String searchEndpoint,
        String searchUser,
        String searchPassword)
    {
      return facetedRequest(searchUser, searchPassword, null, searchEndpoint);
    }

    /**
     * Generic rm search.
     * @param username
     * @param password
     * @param site
     * @param query
     * @param filters
     * @return search results (see API reference for more details), null for any errors
     */
    public JSONObject rmSearch(
        String username,
        String password,
        String site,
        String query,
        String filters)
    {
        List<BasicNameValuePair> searchParameters = new ArrayList<BasicNameValuePair>();
        searchParameters.add(new BasicNameValuePair("query", query));
        searchParameters.add(new BasicNameValuePair("filters", filters));

        String requestURL = MessageFormat.format(
            RM_SEARCH_ENDPOINT,
            alfrescoHttpClientFactory.getObject().getAlfrescoUrl(),
            (site != null) ? site : RM_SITE_ID,
            URLEncodedUtils.format(searchParameters, "UTF-8"));

        return doSearch(requestURL, username, password);
    }

    /**
     * Search as a user for records on site "rm" matching query, using SearchAPI.RM_DEFAULT_RECORD_FILTERS
     * <br>
     * If more fine-grained control of search parameters is required, use rmSearch() directly.
     * @param username
     * @param password
     * @param query
     * @return list of record names
     */
    public List<String> searchForRecordsAsUser(
        String username,
        String password,
        String query)
    {
        return getItemNames(rmSearch(username, password, "rm", query, RM_DEFAULT_RECORD_FILTERS));
    }

    /**
     * Generic faceted search.
     * @param username
     * @param password
     * @param parameters
     * @return search results (see API reference for more details), null for any errors
     */
    public JSONObject facetedSearch(String username, String password, List<NameValuePair> parameters)
    {
        return facetedRequest(username, password, parameters, FACETED_SEARCH_ENDPOINT);
    }

    /**
     * Execute faceted search for term.
     * @param searchUser
     * @param searchPassword
     * @param searchTerm
     * @return search results (see API reference for more details)
     */
    public JSONObject facetedSearchForTerm(String searchUser, String searchPassword, String searchTerm)
    {
        return facetedSearch(
            searchUser,
            searchPassword,
            Arrays.asList(new BasicNameValuePair("term", searchTerm)));
    }

    /**
     * Helper method to search for documents as a user using faceted search.
     * @param username to search as
     * @param password for username
     * @param term search term
     * @return list of document names found
     */
    public List<String> searchForDocumentsAsUser(String username, String password, String term)
    {
        return getItemNames(facetedSearchForTerm(username, password, term));
    }

    /**
     * Helper method to search for documents or folders as a user using search.
     *
     * @param username to search as
     * @param password for username
     * @param term search term
     * @return list of document names found
     * @throws IOException
     */
    public List<String> searchForContentAsUser(String username, String password, String... searchParameters)
                throws Exception
    {
        AlfrescoHttpClient client = alfrescoHttpClientFactory.getObject();
        String requestURL = MessageFormat.format(SEARCH_ENDPOINT, client.getAlfrescoUrl());

        List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();
        for (String searchParameter : searchParameters)
        {
            nameValuePairs.add(new BasicNameValuePair("term", searchParameter));
        }
        requestURL = MessageFormat.format(SEARCH_ENDPOINT, client.getAlfrescoUrl(),
                    URLEncodedUtils.format(nameValuePairs, Charset.forName("UTF-8")));
        Thread.sleep(15000);
        return getItemNames(doGetRequest(username, password, requestURL));
    }

    /**
     * Helper method to extract list of names from search result.
     * @param searchResult
     * @return list of document or record names in search result
     * @throws RuntimeException for malformed search response
     */
    private List<String> getItemNames(JSONObject searchResult)
    {
        return getPropertyValues(searchResult, "name");
    }
}
