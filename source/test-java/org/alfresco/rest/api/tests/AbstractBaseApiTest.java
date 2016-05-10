/*
 * Copyright (C) 2005-2016 Alfresco Software Limited.
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
package org.alfresco.rest.api.tests;

import static org.alfresco.rest.api.tests.util.RestApiUtil.toJsonAsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.nodes.NodesEntityResource;
import org.alfresco.rest.api.tests.RepoService.SiteInformation;
import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.alfresco.rest.api.tests.RepoService.TestPerson;
import org.alfresco.rest.api.tests.RepoService.TestSite;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.alfresco.rest.api.tests.client.PublicApiHttpClient.BinaryPayload;
import org.alfresco.rest.api.tests.client.PublicApiHttpClient.RequestBuilder;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.rest.api.tests.client.data.Document;
import org.alfresco.rest.api.tests.client.data.Folder;
import org.alfresco.rest.api.tests.client.data.Node;
import org.alfresco.rest.api.tests.client.data.Rendition;
import org.alfresco.rest.api.tests.util.MultiPartBuilder;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.TempFileProvider;
import org.springframework.util.ResourceUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

/**
 * Generic methods for calling the Api, taken from BaseCustomModelApiTest
 */
public abstract class AbstractBaseApiTest extends EnterpriseTestApi
{
    public static final String LAST_MODIFIED_HEADER = "Last-Modified";
    public static final String IF_MODIFIED_SINCE_HEADER = "If-Modified-Since";

    private static final String RESOURCE_PREFIX = "publicapi/upload/";

    protected static final String URL_NODES = "nodes";

    private static final String URL_RENDITIONS = "renditions";
    private static final String URL_CHILDREN = "children";
    private static final String URL_CONTENT = "content";


    /**
     * The api scope. either public or private
     * @return public or private
     */
    public abstract String getScope();

    protected HttpResponse post(String url, String runAsUser, String body, int expectedStatus) throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(runAsUser));

        HttpResponse response = publicApiClient.post(getScope(), url, null, null, null, body);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse post(String url, String runAsUser, String body,  String queryString, int expectedStatus) throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(runAsUser));
        if (queryString != null)
        {
            url += queryString;
        }
        HttpResponse response = publicApiClient.post(getScope(), url, null, null, null, body);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse post(String url, String runAsUser, String body, String queryString, String contentType, int expectedStatus) throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(runAsUser));
        if (queryString != null)
        {
            url += queryString;
        }
        HttpResponse response = publicApiClient.post(getScope(), url, null, null, null, body, contentType);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse post(String url, String runAsUser, byte[] body, String queryString, String contentType, int expectedStatus) throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(runAsUser));
        if (queryString != null)
        {
            url += queryString;
        }
        HttpResponse response = publicApiClient.post(getScope(), url, null, null, null, body, contentType);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse post(String runAsUser, String entityCollectionName, String entityId, String relationCollectionName, byte[] body, String queryString, String contentType, int expectedStatus) throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(runAsUser));
        HttpResponse response = publicApiClient.post(getScope(), entityCollectionName, entityId, relationCollectionName, null, body, contentType);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse getAll(String url, String runAsUser, PublicApiClient.Paging paging, int expectedStatus) throws Exception
    {
        return getAll(url, runAsUser, paging, null, expectedStatus);
    }

    protected HttpResponse getAll(String url, String runAsUser, PublicApiClient.Paging paging, Map<String, String> otherParams, int expectedStatus) throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(runAsUser));
        Map<String, String> params = (paging == null) ? null : createParams(paging, otherParams);

        HttpResponse response = publicApiClient.get(getScope(), url, null, null, null, params);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse getAll(Class<?> entityResource, String runAsUser, PublicApiClient.Paging paging, Map<String, String> otherParams, int expectedStatus) throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(runAsUser));

        HttpResponse response = publicApiClient.get(entityResource, null, null, otherParams);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse getSingle(String url, String runAsUser, String entityId, int expectedStatus) throws Exception
    {
        return getSingle(url, runAsUser, entityId, null, expectedStatus);
    }

    protected HttpResponse getSingle(String url, String runAsUser, String entityId, Map<String, String> params, int expectedStatus) throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(runAsUser));

        HttpResponse response = publicApiClient.get(getScope(), url, entityId, null, null, params);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse getSingle(Class<?> entityResource, String runAsUser, String entityId, Map<String, String> params, int expectedStatus) throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(runAsUser));

        HttpResponse response = publicApiClient.get(entityResource, entityId, null, params);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse getSingle(String url, String runAsUser, String entityId, Map<String, String> params, Map<String, String> headers, int expectedStatus) throws Exception
    {
        RequestBuilder requestBuilder = httpClient.new GetRequestBuilder()
                    .setRequestContext(new RequestContext(runAsUser))
                    .setScope(getScope())
                    .setEntityCollectionName(url)
                    .setEntityId(entityId)
                    .setParams(params)
                    .setHeaders(headers);

        HttpResponse response = publicApiClient.execute(requestBuilder);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse put(String url, String runAsUser, String entityId, String body, String queryString, int expectedStatus) throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(runAsUser));
        if (queryString != null)
        {
            entityId += queryString;
        }
        HttpResponse response = publicApiClient.put(getScope(), url, entityId, null, null, body, null);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse putBinary(String url, int version, String runAsUser, BinaryPayload payload, String queryString, Map<String, String> params,
                int expectedStatus) throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(runAsUser));
        if (queryString != null)
        {
            url += queryString;
        }

        HttpResponse response = publicApiClient.putBinary(getScope(), version, url, null, null, null, payload, params);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected HttpResponse putBinary(String url, String runAsUser, BinaryPayload payload, String queryString, Map<String, String> params,
                int expectedStatus) throws Exception
    {
        return putBinary(url, 1, runAsUser, payload, queryString, params, expectedStatus);
    }

    protected HttpResponse delete(String url, String runAsUser, String entityId, int expectedStatus) throws Exception
    {
        return delete(url, runAsUser, entityId,  null, expectedStatus);
    }

    protected HttpResponse delete(String url, String runAsUser, String entityId,  Map<String, String> params, int expectedStatus) throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(runAsUser));

        HttpResponse response = publicApiClient.delete(getScope(), 1, url, entityId, null, null, params);
        checkStatus(expectedStatus, response.getStatusCode());

        return response;
    }

    protected String createUser(String username)
    {
        PersonInfo personInfo = new PersonInfo(username, username, username, "password", null, null, null, null, null, null, null);
        RepoService.TestPerson person = repoService.createUser(personInfo, username, null);
        return person.getId();
    }

    protected String getOrCreateUser(String username, String password)
    {
        PersonInfo personInfo = new PersonInfo(username, username, username, password, null, null, null, null, null, null, null);
        RepoService.TestPerson person = repoService.getOrCreateUser(personInfo, username, null);
        return person.getId();
    }

    protected TestSite createSite(final TestNetwork testNetwork, TestPerson user, final SiteVisibility siteVisibility)
    {
        final String siteName = "RandomSite" + System.currentTimeMillis();
        final TestSite site = TenantUtil.runAsUserTenant(new TenantUtil.TenantRunAsWork<TestSite>()
        {
            @Override
            public TestSite doWork() throws Exception
            {
                SiteInformation siteInfo = new SiteInformation(siteName, siteName, siteName, siteVisibility);
                return repoService.createSite(testNetwork, siteInfo);
            }
        }, user.getId(), testNetwork.getId());
        assertNotNull(site);

        return site;
    }

    protected void checkStatus(int expectedStatus, int actualStatus)
    {
        if (expectedStatus > 0 && expectedStatus != actualStatus)
        {
            fail("Status code " + actualStatus + " returned, but expected " + expectedStatus);
        }
    }

    // -root- (eg. Company Home for on-prem)
    protected String getRootNodeId(String runAsUserId) throws Exception
    {
        HttpResponse response = getSingle(NodesEntityResource.class, runAsUserId, Nodes.PATH_ROOT, null, 200);
        Node node = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        return node.getId();
    }

    // -my- (eg. User's Home for on-prem)
    protected String getMyNodeId(String runAsUserId) throws Exception
    {
        HttpResponse response = getSingle(NodesEntityResource.class, runAsUserId, Nodes.PATH_MY, null, 200);
        Node node = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        return node.getId();
    }

    // -shared- (eg. "Shared" folder for on-prem)
    protected String getSharedNodeId(String runAsUserId) throws Exception
    {
        HttpResponse response = getSingle(NodesEntityResource.class, runAsUserId, Nodes.PATH_SHARED, null, 200);
        Node node = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        return node.getId();
    }

    protected Folder createFolder(String runAsUserId, String parentId, String folderName) throws Exception
    {
        return createFolder(runAsUserId, parentId, folderName, null);
    }

    protected Folder createFolder(String runAsUserId, String parentId, String folderName, Map<String, Object> props) throws Exception
    {
        return createNode(runAsUserId, parentId, folderName, "cm:folder", props, Folder.class);
    }

    protected Node createNode(String runAsUserId, String parentId, String nodeName, String nodeType, Map<String, Object> props) throws Exception
    {
        return createNode(runAsUserId, parentId, nodeName, nodeType, props, Node.class);
    }

    protected <T> T createNode(String runAsUserId, String parentId, String nodeName, String nodeType, Map<String, Object> props, Class<T> returnType)
                throws Exception
    {
        Node n = new Node();
        n.setName(nodeName);
        n.setNodeType(nodeType);
        n.setProperties(props);

        // create node
        HttpResponse response = post(getNodeChildrenUrl(parentId), runAsUserId, RestApiUtil.toJsonAsStringNonNull(n), 201);

        return RestApiUtil.parseRestApiEntry(response.getJsonResponse(), returnType);
    }

    protected Document createTextFile(String userId, String parentId, String fileName, String textContent) throws IOException, Exception
    {
        return createTextFile(userId, parentId, fileName, textContent, "UTF-8", null);
    }

    protected Document createTextFile(String userId, String parentId, String fileName, String textContent, String encoding, Map<String,String> props) throws IOException, Exception
    {
        return createTextFile(userId, parentId, fileName, textContent, encoding, props, 201);
    }

    protected Document createTextFile(String userId, String parentId, String fileName, String textContent, String encoding, Map<String,String> props, int expectedStatus) throws IOException, Exception
    {
        if (props == null)
        {
            props = Collections.EMPTY_MAP;

        }

        ByteArrayInputStream inputStream = new ByteArrayInputStream(textContent.getBytes());
        File txtFile = TempFileProvider.createTempFile(inputStream, getClass().getSimpleName(), ".txt");

        MultiPartBuilder.MultiPartRequest reqBody = MultiPartBuilder.create()
                .setFileData(new MultiPartBuilder.FileData(fileName, txtFile, MimetypeMap.MIMETYPE_TEXT_PLAIN, encoding))
                .setProperties(props)
                .build();

        HttpResponse response = post(getNodeChildrenUrl(parentId), userId, reqBody.getBody(), null, reqBody.getContentType(), expectedStatus);

        if (response.getJsonResponse().get("error") != null)
        {
            return null;
        }

        return RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
    }

    protected Document updateTextFile(String userId, String contentId, String textContent, Map<String,String> parameters) throws IOException, Exception
    {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(textContent.getBytes());
        File txtFile = TempFileProvider.createTempFile(inputStream, getClass().getSimpleName(), ".txt");
        BinaryPayload payload = new BinaryPayload(txtFile, MimetypeMap.MIMETYPE_TEXT_PLAIN);

        HttpResponse response = putBinary(getNodeContentUrl(contentId), userId, payload, null, parameters, 200);
        return RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
    }

    protected File getResourceFile(String fileName) throws FileNotFoundException
    {
        URL url = NodeApiTest.class.getClassLoader().getResource(RESOURCE_PREFIX + fileName);
        if (url == null)
        {
            fail("Cannot get the resource: " + fileName);
        }
        return ResourceUtils.getFile(url);
    }

    protected static final long PAUSE_TIME = 5000; //millisecond
    protected static final int MAX_RETRY = 10;

    protected Rendition createAndGetRendition(String userId, String sourceNodeId, String renditionId) throws Exception
    {
        Rendition renditionRequest = new Rendition();
        renditionRequest.setId(renditionId);

        int retryCount = 0;
        while (retryCount < MAX_RETRY)
        {
            try
            {
                HttpResponse res = post(getNodeRenditionsUrl(sourceNodeId), userId, toJsonAsString(renditionRequest), 202);
                assertNull(res.getJsonResponse());
                break;
            }
            catch (AssertionError ex)
            {
                // If no transformer is currently available,
                // wait for 'PAUSE_TIME' and try again.
                retryCount++;
                Thread.sleep(PAUSE_TIME);
            }
        }

        retryCount = 0;
        while (retryCount < MAX_RETRY)
        {
            try
            {
                HttpResponse response = getSingle(getNodeRenditionsUrl(sourceNodeId), userId, renditionId, 200);
                Rendition rendition = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Rendition.class);
                assertNotNull(rendition);
                assertEquals(Rendition.RenditionStatus.CREATED, rendition.getStatus());
                return rendition;
            }
            catch (AssertionError ex)
            {
                // If the asynchronous create rendition action is not finished yet,
                // wait for 'PAUSE_TIME' and try again.
                retryCount++;
                Thread.sleep(PAUSE_TIME);
            }
        }

        return null;
    }

    protected String getNodeRenditionsUrl(String nodeId)
    {
        return URL_NODES + "/" + nodeId + "/" + URL_RENDITIONS;
    }

    protected String getNodeChildrenUrl(String nodeId)
    {
        return URL_NODES + "/" + nodeId + "/" + URL_CHILDREN;
    }

    protected String getNodeContentUrl(String nodeId)
    {
        return URL_NODES + "/" + nodeId + "/" + URL_CONTENT;
    }
}
