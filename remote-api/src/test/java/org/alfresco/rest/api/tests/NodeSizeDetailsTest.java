/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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
package org.alfresco.rest.api.tests;

import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.NodeSizeDetails;
import org.alfresco.rest.api.model.Site;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.alfresco.rest.api.tests.client.data.*;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * V1 REST API tests for calculating and retrieving Folder size.
 */
@FixMethodOrder (MethodSorters.NAME_ASCENDING)
@RunWith (JUnit4.class)
public class NodeSizeDetailsTest extends AbstractBaseApiTest
{
    private static final Logger LOG = LoggerFactory.getLogger(NodeSizeDetailsTest.class);

    private Site userOneN1Site;
    private String folderId;
    private PermissionService permissionService;
    private Nodes nodes;

    // Method to create content info
    private ContentInfo createContentInfo()
    {
        ContentInfo ciExpected = new ContentInfo();
        ciExpected.setMimeType("text/plain");
        ciExpected.setMimeTypeName("Plain Text");
        ciExpected.setSizeInBytes(44500L);
        ciExpected.setEncoding("ISO-8859-1");
        return ciExpected;
    }

    private String addToDocumentLibrary(Site testSite, String name, String nodeType)
    {
        String parentId;
        try
        {
            parentId = getSiteContainerNodeId(testSite.getId(), "documentLibrary");
            return createNode(parentId, name, nodeType, null).getId();
        }
        catch (Exception e)
        {
            LOG.error("Exception occurred in NodeSizeDetailsTest:addToDocumentLibrary {}", e.getMessage());
        }
        return null;
    }

    @Before
    public void setup() throws Exception
    {
        super.setup();

        setRequestContext(user1);

        String siteTitle = "RandomSite" + System.currentTimeMillis();
        this.userOneN1Site = createSite("RN"+RUNID, siteTitle, siteTitle, SiteVisibility.PRIVATE, 201);

        // Create a folder within the site document's library.
        String folderName = "folder" + System.currentTimeMillis();
        folderId = addToDocumentLibrary(userOneN1Site, folderName, TYPE_CM_FOLDER);
        permissionService = applicationContext.getBean("permissionService", PermissionService.class);
        nodes = applicationContext.getBean("Nodes", Nodes.class);
    }

    /**
     * Test case for POST/size-details, which request and calculates the size of a folder.
     * GET/size-details, which retrieve the SizeDetails of the folder Node.
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/size-details}
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/size-details/<jobId>}
     */
    @Test
    public void testPostAndGetFolderSizeDetails() throws Exception
    {
        // Perform POST request
        HttpResponse postResponse = post(generateNodeSizeDetailsUrl(folderId), null, 202);

        assertNotNull("After executing POST/size-details first time, it will provide jobId with 202 status code",postResponse.getJsonResponse());

        String jobId = NodeSizeDetails.parseJson((JSONObject)postResponse.getJsonResponse().get("entry"));

        assertNotNull("In response, JobId should be present", jobId);

        // Prepare parameters.
        Map<String, String> params = new HashMap<>();
        params.put("nodeId", folderId);
        params.put("jobId", jobId);

        HttpResponse getResponse = getSingle(getNodeSizeDetailsUrl(folderId, jobId), user1, params, 200);

        assertNotNull("After executing GET/size-details, it will provide NodeSizeDetails with 200 status code",getResponse.getJsonResponse());

        String getJsonResponse = String.valueOf(getResponse.getJsonResponse());

        assertTrue("We are not getting correct response "+getJsonResponse,getJsonResponse.contains("size") || getJsonResponse.contains("status"));
    }

    @Test
    public void testPerformanceTesting() throws Exception
    {
        setRequestContext(user1);
        UserInfo userInfo = new UserInfo(user1);

        String folder0Name = "f0-testParentFolder-"+RUNID;
        String parentFolder = createFolder(tDocLibNodeId, folder0Name,null).getId();

        for(int i=1;i<=500;i++)
        {
            String folderBName = "folder"+i+RUNID + "_B";
            String folderBId = createFolder(parentFolder, folderBName, null).getId();
            String fileName = "content"+i+ RUNID + ".txt";
            Document d1 = new Document();
            d1.setIsFolder(false);
            d1.setParentId(folderBId);
            d1.setName(fileName);
            d1.setNodeType(TYPE_CM_CONTENT);
            d1.setContent(createContentInfo());
            d1.setCreatedByUser(userInfo);
            d1.setModifiedByUser(userInfo);
        }

        PublicApiClient.Paging paging = getPaging(0, 1000);
        HttpResponse response = getAll(getNodeChildrenUrl(parentFolder), paging, 200);
        List<Node> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        assertEquals(500, nodes.size());

        //Start Time before triggering POST/size-details API
        LocalTime expectedTime = LocalTime.now().plusSeconds(5);

        // Perform POST request
        HttpResponse postResponse = post(generateNodeSizeDetailsUrl(parentFolder), null, 202);

        assertNotNull("After executing POST/size-details first time, it will provide jobId with 202 status code",postResponse.getJsonResponse());

        String jobId = NodeSizeDetails.parseJson((JSONObject)postResponse.getJsonResponse().get("entry"));

        assertNotNull("In response, JobId should be present", jobId);

        // Prepare parameters.
        Map<String, String> params = new HashMap<>();
        params.put("nodeId", folderId);
        params.put("jobId", jobId);

        HttpResponse getResponse = getSingle(getNodeSizeDetailsUrl(folderId, jobId), user1, params, 200);

        assertNotNull("After executing GET/size-details, it will provide NodeSizeDetails with 200 status code",getResponse.getJsonResponse());

        String getJsonResponse = String.valueOf(getResponse.getJsonResponse());

        assertTrue("We are not getting correct response "+getJsonResponse,getJsonResponse.contains("size") || getJsonResponse.contains("status"));

        //current Time after executing GET/size-details
        LocalTime actualTime = LocalTime.now();
        assertTrue("Calculating folder node is taking time greater than 5 seconds ",actualTime.isBefore(expectedTime));
    }

    /**
     * Test case for others HTTP status codes.
     */
    @Test
    public void testHTTPStatus() throws Exception
    {
        setRequestContext(null);
        delete(generateNodeSizeDetailsUrl(folderId), folderId, null, 401);

        setRequestContext(user1);
        String folderName = "folder0" + System.currentTimeMillis();

        HttpResponse responseForNotFound = post(generateNodeSizeDetailsUrl(folderName), null, 404);
        assertNotNull(responseForNotFound);

        folderName = "folder1" + System.currentTimeMillis();
        String nodeType = "cm:content";

        Node nodeResp = createNode(folderId, folderName, nodeType, null);
        String n1Id = nodeResp.getId();

        // Perform POST request
        HttpResponse responseForInvalidNode = post(generateNodeSizeDetailsUrl(n1Id), null, 422);
        assertNotNull(responseForInvalidNode);
    }

    @After
    public void tearDown() throws Exception
    {
        deleteSite(userOneN1Site.getId(), true, 204);
    }

    @Override
    public String getScope()
    {
        return "public";
    }
}