/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail. Otherwise, the software is
 * provided under the following open source license terms:
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.repo.web.scripts.solr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.alfresco.rest.AbstractSingleNetworkSiteTest;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.alfresco.rest.api.tests.client.data.Node;
import org.alfresco.rest.api.tests.util.MultiPartBuilder;
import org.alfresco.rest.api.tests.util.RestApiUtil;

//@RunWith(SpringRunner.class)
//@ContextConfiguration({"classpath:alfresco/application-context.xml"})
//@ContextCustomizerFactories(factories = {}, mergeMode = ContextCustomizerFactories.MergeMode.REPLACE_DEFAULTS)
public class NodeContentGetTest extends AbstractSingleNetworkSiteTest
{

    private final URL TEST_FILE_URL = NodeContentGetTest.class.getClassLoader().getResource("publicapi/upload/babekyrtso.pdf");
    private final String TEST_FILE_NAME = "testFile.pdf";
    private final String SEARCH_TERM = "babekyrtso";
    private RepoHttpClient repoHttpClient;

    @Before
    public void before() throws Exception
    {
        super.setup();
        repoHttpClient = new RepoHttpClient();

        // System.setProperty("localTransform.core-aio.url", "http://localhost:8090/");
        // System.setProperty("transform.service.enabled", "false");
        // System.setProperty("local.transform.service.enabled", "true");
        // System.setProperty("transformer.strict.mimetype.check", "true");
        // System.setProperty("content.transformer.retryOn.different.mimetype", "true");
    }

    @Test
    public void testNodeContentGet() throws Exception
    {

        // repoHttpClient.uploadFile(TEST_FILE_URL,TEST_FILE_NAME);
        setRequestContext(user1);
        String fileName = "babekyrtso.pdf";
        String term = "babekyrtso";
        File file = getResourceFile(fileName);

        MultiPartBuilder multiPartBuilder = MultiPartBuilder.create()
                .setFileData(new MultiPartBuilder.FileData(TEST_FILE_NAME, file));
        MultiPartBuilder.MultiPartRequest reqBody = multiPartBuilder.build();

        HttpResponse uploadResponse = post(getNodeChildrenUrl(Nodes.PATH_MY), reqBody.getBody(), null, reqBody.getContentType(), 201);

        String searchJson = "{\"query\":{\"language\":\"afts\",\"query\":\"" + term + "\"}}";
        HttpResponse response = post("search", searchJson, null, null, "search", 200);
        PublicApiClient.ExpectedPaging paging = RestApiUtil.parsePaging(response.getJsonResponse());
        assertEquals(1, paging.getTotalItems().intValue());
        assertFalse(paging.getHasMoreItems());
        List<Node> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        assertEquals(1, nodes.size());
    }
}
