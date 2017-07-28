/*-
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.api.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.alfresco.rest.AbstractSingleNetworkSiteTest;
import org.alfresco.rest.api.Queries;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.ExpectedPaging;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.data.FolderNode;
import org.alfresco.rest.api.tests.client.data.Node;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic test to exercise the Search API endpoint
 *
 * <p>POST:</p>
 * {@literal <host>:<port>/alfresco/api/<networkId>/public/search/versions/1/search}
 *
 * @author Gethin James
 */
public class BasicSearchApiIntegrationTest extends AbstractSingleNetworkSiteTest
{
    private static final String URL_SEARCH = "search";
    private static final String SEARCH_API_NAME = "search";
    private static final String  json = "{ \"query\": {\"query\": \"cm:name:king\",\"userQuery\": \"great\",\"language\": \"afts\"}, \"fields\" : [\"id\",\"name\", \"search\"]}";
    private static final String  bad_json = "{ \"query\": {\"qu\": \"cm:some nonsense \",\"userQuery\": \"great\",\"language\": \"afts\"}}";

    /**
     * Tests basic api for search
     */
    @Test
    public void testQuery() throws Exception
    {
        setRequestContext(user1);
        String f1Id = null;
        try
        {
            // As user 1 ...
            // Try to get nodes with search term 'king*' - assume clean repo (ie. none to start with)
            HttpResponse response = post(URL_SEARCH, json, null, null, SEARCH_API_NAME, 200);
            List<Node> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(0, nodes.size());

            String myFolderNodeId = getMyNodeId();

            f1Id = createFolder(myFolderNodeId, "king").getId();

            response = post(URL_SEARCH, json, null, null, SEARCH_API_NAME, 200);
            ExpectedPaging paging = RestApiUtil.parsePaging(response.getJsonResponse());
            assertEquals(1, paging.getTotalItems().intValue());
            assertFalse(paging.getHasMoreItems());
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(1, nodes.size());

            //Just happy if it doesn't error
            response = post(URL_SEARCH, SerializerTestHelper.JSON, null, null, SEARCH_API_NAME, 200);

        }
        finally
        {
            // some cleanup
            if (f1Id != null)
            {
                deleteNode(f1Id, true, 204);
            }
        }
    }

    @Test
    public void testBadQuery() throws Exception
    {
        setRequestContext(user1);
        //Bad request
        HttpResponse response = post(URL_SEARCH, bad_json, null, null, SEARCH_API_NAME, 400);
    }
}
