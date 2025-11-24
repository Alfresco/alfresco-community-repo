/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2025 - 2025 Alfresco Software Limited
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
import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;

import org.junit.Test;

import org.alfresco.repo.transaction.TransactionServiceImpl;
import org.alfresco.rest.AbstractSingleNetworkSiteTest;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiHttpClient;
import org.alfresco.rest.api.tests.client.data.Node;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.service.ServiceRegistry;

public class SearchInReadOnlyModeTest extends AbstractSingleNetworkSiteTest
{
    @Test
    public void testReadOnlyServerCanPerformSearch() throws IOException
    {
        TransactionServiceImpl transactionService = (TransactionServiceImpl) applicationContext.getBean(ServiceRegistry.TRANSACTION_SERVICE.getLocalName());
        try
        {
            transactionService.setAllowWrite(false);
            setRequestContext(user1);
            verifySystemIsInReadOnlyMode();

            HttpResponse response = postSearch();

            // there is no search engine running in the test framework, so we just verify we get a 200 response
            assertThat(response.getStatusCode()).isEqualTo(200);
        }
        finally
        {
            transactionService.setAllowWrite(true);
        }
    }

    private void verifySystemIsInReadOnlyMode() throws IOException
    {

        Node n = new Node();
        n.setName("test-folder-any-name-" + RUNID);
        n.setNodeType(TYPE_CM_FOLDER);

        HttpResponse response = publicApiClient.post(getScope(), getNodeChildrenUrl(getNodeId()), null, null, null, RestApiUtil.toJsonAsStringNonNull(n));
        assertThat(response.getStatusCode()).isEqualTo(403);
        assertThat(response.getResponse()).contains("The system is currently in read-only mode.");
    }

    private String getNodeId()
    {
        try
        {
            return getMyNodeId();
        }
        catch (Exception e)
        {
            fail("Test setup failure: unable to get My Node ID", e);
            return null;
        }
    }

    private HttpResponse postSearch() throws IOException
    {
        final String json = "{ \"query\": {\"query\": \"cm:name:king\",\"userQuery\": \"great\",\"language\": \"afts\"}, \"fields\" : [\"id\",\"name\", \"search\"]}";
        PublicApiHttpClient.RequestBuilder requestBuilder = httpClient.new PostRequestBuilder().setBodyAsString(json)
                .setRequestContext(publicApiClient.getRequestContext())
                .setScope(getScope())
                .setApiName("search")
                .setEntityCollectionName("search");
        return publicApiClient.execute(requestBuilder);
    }
}
