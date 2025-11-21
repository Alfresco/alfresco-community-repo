/*-
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import org.alfresco.repo.transaction.TransactionServiceImpl;
import org.alfresco.rest.AbstractSingleNetworkSiteTest;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.ExpectedPaging;
import org.alfresco.rest.api.tests.client.PublicApiHttpClient;
import org.alfresco.rest.api.tests.client.data.Node;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.util.testing.category.LuceneTests;
import org.alfresco.util.testing.category.RedundantTests;

/**
 * Basic test to exercise the Search API endpoint
 *
 * <p>
 * POST:
 * </p>
 * {@literal <host>:<port>/alfresco/api/<networkId>/public/search/versions/1/search}
 *
 * @author Gethin James
 */
@Category({LuceneTests.class, RedundantTests.class})
public class BasicSearchApiIntegrationTest extends AbstractSingleNetworkSiteTest
{
    private static final String JSON = "{ \"query\": {\"query\": \"cm:name:king\",\"userQuery\": \"great\",\"language\": \"afts\"}, \"fields\" : [\"id\",\"name\", \"search\"]}";
    private static final String BAD_JSON = "{ \"query\": {\"qu\": \"cm:some nonsense \",\"userQuery\": \"great\",\"language\": \"afts\"}}";

    @Before
    public void setup() throws Exception
    {
        super.setup();
        setRequestContext(user1);
    }

    /**
     * Tests basic api for search
     */
    @Test
    public void testQuery() throws Exception
    {
        String f1Id = null;
        try
        {
            // As user 1 ...
            // Try to get nodes with search term 'king*' - assume clean repo (ie. none to start with)
            HttpResponse response = post(JSON);
            assertThat(response.getStatusCode()).isEqualTo(200);

            List<Node> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertThat(nodes).isEmpty();

            String myFolderNodeId = getMyNodeId();
            f1Id = createFolder(myFolderNodeId, "king").getId();

            response = post(JSON);
            assertThat(response.getStatusCode()).isEqualTo(200);
            ExpectedPaging paging = RestApiUtil.parsePaging(response.getJsonResponse());
            assertThat(paging.getTotalItems().intValue()).isEqualTo(1);
            assertThat(paging.getHasMoreItems()).isFalse();
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertThat(nodes).hasSize(1);
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
    public void testBodySerializedCorrectly() throws IOException
    {
        HttpResponse response = post(SerializerTestHelper.JSON);
        assertThat(response.getStatusCode()).isEqualTo(200);
    }

    @Test
    public void testBadQuery() throws Exception
    {
        assertThat(post(BAD_JSON).getStatusCode()).isEqualTo(100);
    }

    @Test
    public void testReadOnlyServerCanPerformSearch() throws Exception
    {
        TransactionServiceImpl transactionService = (TransactionServiceImpl) applicationContext.getBean(ServiceRegistry.TRANSACTION_SERVICE.getLocalName());
        try
        {
            transactionService.setAllowWrite(false);

            HttpResponse response = post(JSON);

            assertThat(response.getStatusCode()).isEqualTo(200);
        }
        finally
        {
            transactionService.setAllowWrite(true);
        }
    }

    private HttpResponse post(String body) throws IOException
    {
        PublicApiHttpClient.RequestBuilder requestBuilder = httpClient.new PostRequestBuilder().setBodyAsString(body)
                .setRequestContext(publicApiClient.getRequestContext())
                .setScope(getScope())
                .setApiName("search")
                .setEntityCollectionName("search");
        return publicApiClient.execute(requestBuilder);
    }
}
