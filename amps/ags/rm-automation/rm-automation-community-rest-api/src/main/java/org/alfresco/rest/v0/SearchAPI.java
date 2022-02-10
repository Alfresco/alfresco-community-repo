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
package org.alfresco.rest.v0;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.alfresco.dataprep.AlfrescoHttpClientFactory;
import org.alfresco.rest.core.v0.BaseAPI;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
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

    /** faceted search API endpoint */
    private static final String FACETED_SEARCH_ENDPOINT = "{0}alfresco/s/slingshot/rmsearch/faceted/rmsearch?{1}";

    /** share live search API endpoint */
    private static final String SHARE_LIVE_SEARCH_DOCS_ENDPOINT = "{0}alfresco/s/slingshot/live-search-docs?{1}";

    /** RM search URL template */
    private static final String RM_SEARCH_ENDPOINT = "{0}alfresco/s/slingshot/rmsearch/{1}?{2}";

    /** RM all nodes search filters */
    private static final String RM_DEFAULT_NODES_FILTERS =
                "records/true,undeclared/true,vital/false,folders/{0},categories/{1},frozen/false,cutoff/false";

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
     * @param sortby
     * @return search results (see API reference for more details), null for any errors
     */
    public JSONObject rmSearch(
        String username,
        String password,
        String site,
        String query,
        String filters,
        String sortby)
    {
        List<BasicNameValuePair> searchParameters = new ArrayList<>();
        searchParameters.add(new BasicNameValuePair("query", query));
        searchParameters.add(new BasicNameValuePair("filters", filters));
        if (sortby != null)
        {
            searchParameters.add(new BasicNameValuePair("sortby", sortby));
        }

        String requestURL = MessageFormat.format(
            RM_SEARCH_ENDPOINT,
            alfrescoHttpClientFactory.getObject().getAlfrescoUrl(),
            (site != null) ? site : RM_SITE_ID,
            URLEncodedUtils.format(searchParameters, "UTF-8"));

        return doSearch(requestURL, username, password);
    }

    /**
     * Search as a user for nodes on site "rm" matching query, using SearchAPI.RM_DEFAULT_RECORD_FILTERS and sorted
     * by sortby
     * <br>
     *
     * @param username
     * @param password
     * @param query
     * @param sortby
     * @return list of node names
     */

    public List<String> searchForNodeNamesAsUser(String username, String password, String query, String sortby,
                boolean includeCategories, boolean includeFolders)
    {
        String searchFilterParamaters = MessageFormat.format(RM_DEFAULT_NODES_FILTERS, Boolean.toString(includeFolders),
                    Boolean.toString(includeCategories));

        return getItemNames(rmSearch(username, password, "rm", query, searchFilterParamaters, sortby));
    }

    /**
     * Search as a user for nodes on site "rm" matching query, using SearchAPI.RM_DEFAULT_RECORD_FILTERS and sorted
     * by sortby and returns the property value for the given nodeRef and property name
     * 
     * @param username
     * @param password
     * @param query
     * @param sortby
     * @param includeCategories
     * @param includeFolders
     * @return list of node properties
     */
    public String searchForNodePropertyAsUser(String username, String password, String nodeRef, String propertyName, String query, String sortby,
                boolean includeCategories, boolean includeFolders)
    {
        String searchFilterParamaters = MessageFormat.format(RM_DEFAULT_NODES_FILTERS, Boolean.toString(includeFolders),
                    Boolean.toString(includeCategories));
        return getItemProperty(rmSearch(username, password, "rm", query, searchFilterParamaters, sortby), nodeRef, propertyName); 
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
     * Execute share live search for documents.
     *
     * @param searchUser
     * @param searchPassword
     * @param searchTerm
     * @return search results (see API reference for more details)
     */
    public JSONObject liveSearchForDocuments(String searchUser, String searchPassword, String searchTerm)
    {
        return facetedRequest(searchUser, searchPassword, Arrays.asList(new BasicNameValuePair("t", searchTerm)),
                    SHARE_LIVE_SEARCH_DOCS_ENDPOINT);
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
     * Helper method to search for documents as a user using share live search.
     * @param username to search as
     * @param password for username
     * @param term search term
     * @return list of document names found
     */
    public List<String> liveSearchForDocumentsAsUser(String username, String password, String term) throws JSONException
    {
        JSONObject searchResult = liveSearchForDocuments(username, password, term);
        LOGGER.info(searchResult.toString(3));
        return getItemNames(searchResult);
    }

    /**
     * Helper method to extract list of names from search result.
     * 
     * @param searchResult
     * @return list of document or record names in search result
     * @throws FileNotFoundException 
     * @throws JsonSyntaxException 
     * @throws JsonIOException 
     * @throws RuntimeException for malformed search response
     */
    /**
     * Helper method to extract list of names from search result.
     * 
     * @param searchResult
     * @return
     */
    private List<String> getItemNames(JSONObject searchResult)
    {
        return getPropertyValues(searchResult, "name");
    }
    
    /**
     * Helper method to extract list of property values from search result for the given nodeRef.
     * 
     * @param searchResult
     * @param nodeRef
     * @param propertyName
     * @return
     */
    private String getItemProperty(JSONObject searchResult, String nodeRef, String propertyName)
    {
        return getPropertyValue(searchResult, nodeRef, propertyName);
    }
}
