/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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

import static org.alfresco.rest.api.tests.util.RestApiUtil.toJsonAsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.google.common.collect.Ordering;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.rendition2.RenditionService2Impl;
import org.alfresco.repo.rendition2.SynchronousTransformClient;
import org.alfresco.rest.api.model.Site;
import org.alfresco.rest.api.nodes.NodesEntityResource;
import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.alfresco.rest.api.tests.RepoService.TestPerson;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.ExpectedErrorResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.ExpectedPaging;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.data.ContentInfo;
import org.alfresco.rest.api.tests.client.data.Document;
import org.alfresco.rest.api.tests.client.data.Rendition;
import org.alfresco.rest.api.tests.client.data.Rendition.RenditionStatus;
import org.alfresco.rest.api.tests.util.MultiPartBuilder;
import org.alfresco.rest.api.tests.util.MultiPartBuilder.FileData;
import org.alfresco.rest.api.tests.util.MultiPartBuilder.MultiPartRequest;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.util.TempFileProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * V1 REST API tests for Renditions
 *
 * @author Jamal Kaabi-Mofrad
 */
public class RenditionsTest extends AbstractBaseApiTest
{
    /**
     * Test network one
     */
    TestNetwork networkN1;

    /**
     * User one from network one
     */
    private TestPerson userOneN1;

    /**
     * Private site of user one from network one
     */
    private Site userOneN1Site;
    
    private final static long DELAY_IN_MS = 500;

    protected static ContentService contentService;
    private static SynchronousTransformClient synchronousTransformClient;

    @Before
    public void setup() throws Exception
    {
        contentService = applicationContext.getBean("contentService", ContentService.class);
        synchronousTransformClient = applicationContext.getBean("synchronousTransformClient", SynchronousTransformClient.class);
        networkN1 = repoService.createNetworkWithAlias("ping", true);
        networkN1.create();
        userOneN1 = networkN1.createUser();

        setRequestContext(networkN1.getId(), userOneN1.getId(), null);

        String siteTitle = "RandomSite" + System.currentTimeMillis();
        userOneN1Site = createSite(siteTitle, SiteVisibility.PRIVATE);
    }

    @After
    public void tearDown() throws Exception
    {
        setRequestContext(networkN1.getId(), userOneN1.getId(), null);
        deleteSite(userOneN1Site.getId(), true, 204);
    }

    /**
     * Tests get node renditions.
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/renditions}
     */
    @Test
    public void testListNodeRenditions() throws Exception
    {
        setRequestContext(networkN1.getId(), userOneN1.getId(), null);
        
        // Create a folder within the site document's library
        String folderName = "folder" + System.currentTimeMillis();
        String folder_Id = addToDocumentLibrary(userOneN1Site, folderName, TYPE_CM_FOLDER, userOneN1.getId());

        // Create multipart request
        String fileName = "quick.pdf";
        File file = getResourceFile(fileName);
        MultiPartBuilder multiPartBuilder = MultiPartBuilder.create().setFileData(new FileData(fileName, file));
        MultiPartRequest reqBody = multiPartBuilder.build();

        // Upload quick.pdf file into 'folder'
        HttpResponse response = post(getNodeChildrenUrl(folder_Id), reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        String contentNodeId = document.getId();

        Paging paging = getPaging(0, 50);
        // List all available renditions (includes those that have been created and those that are yet to be created)
        response = getAll(getNodeRenditionsUrl(contentNodeId), paging, 200);
        List<Rendition> renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        assertTrue(renditions.size() >= 5);
        Rendition docLib = getRendition(renditions, "doclib");
        assertNotNull(docLib);
        assertEquals(RenditionStatus.NOT_CREATED, docLib.getStatus());
        ContentInfo contentInfo = docLib.getContent();
        assertNotNull(contentInfo);
        assertEquals(MimetypeMap.MIMETYPE_IMAGE_PNG, contentInfo.getMimeType());
        assertEquals("PNG Image", contentInfo.getMimeTypeName());
        assertNull(contentInfo.getEncoding());
        assertNull(contentInfo.getSizeInBytes());

        // Add a filter to select the renditions based on the given status
        Map<String, String> params = new HashMap<>(1);
        params.put("where", "(status='NOT_CREATED')");
        // List only the NOT_CREATED renditions
        response = getAll(getNodeRenditionsUrl(contentNodeId), paging, params, 200);
        renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        assertTrue(renditions.size() >= 5);

        params.put("where", "(status='CREATED')");
        // List only the CREATED renditions
        response = getAll(getNodeRenditionsUrl(contentNodeId), paging, params, 200);
        renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        assertEquals("There is no rendition created yet.", 0, renditions.size());

        // Test paging
        // SkipCount=0,MaxItems=2
        paging = getPaging(0, 2);
        // List all available renditions
        response = getAll(getNodeRenditionsUrl(contentNodeId), paging, 200);
        renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        assertEquals(2, renditions.size());
        ExpectedPaging expectedPaging = RestApiUtil.parsePaging(response.getJsonResponse());
        assertEquals(2, expectedPaging.getCount().intValue());
        assertEquals(0, expectedPaging.getSkipCount().intValue());
        assertEquals(2, expectedPaging.getMaxItems().intValue());
        assertTrue(expectedPaging.getTotalItems() >= 5);
        assertTrue(expectedPaging.getHasMoreItems());

        // SkipCount=2,MaxItems=3
        paging = getPaging(2, 3);
        // List all available renditions
        response = getAll(getNodeRenditionsUrl(contentNodeId), paging, 200);
        renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        assertEquals(3, renditions.size());
        expectedPaging = RestApiUtil.parsePaging(response.getJsonResponse());
        assertEquals(3, expectedPaging.getCount().intValue());
        assertEquals(2, expectedPaging.getSkipCount().intValue());
        assertEquals(3, expectedPaging.getMaxItems().intValue());
        assertTrue(expectedPaging.getTotalItems() >= 5);

        // Create 'doclib' rendition
        createAndGetRendition(contentNodeId, docLib.getId());

        // List all available renditions (includes those that have been created and those that are yet to be created)
        paging = getPaging(0, 50);
        response = getAll(getNodeRenditionsUrl(contentNodeId), paging, 200);
        renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        assertTrue(renditions.size() >= 5);
        docLib = getRendition(renditions, "doclib");
        assertNotNull(docLib);
        assertEquals(RenditionStatus.CREATED, docLib.getStatus());
        contentInfo = docLib.getContent();
        assertNotNull(contentInfo);
        assertEquals(MimetypeMap.MIMETYPE_IMAGE_PNG, contentInfo.getMimeType());
        assertEquals("PNG Image", contentInfo.getMimeTypeName());
        assertNotNull(contentInfo.getEncoding());
        assertTrue(contentInfo.getSizeInBytes() > 0);

        // List only the CREATED renditions
        response = getAll(getNodeRenditionsUrl(contentNodeId), paging, params, 200);
        renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        assertEquals("Should've only returned the 'doclib' rendition.", 1, renditions.size());

        params.put("where", "(status='NOT_CREATED')");
        // List only the NOT_CREATED renditions
        response = getAll(getNodeRenditionsUrl(contentNodeId), paging, params, 200);
        renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        assertTrue(renditions.size() > 0);
        docLib = getRendition(renditions, "doclib");
        assertNull("'doclib' rendition has already been created.", docLib);

        // Test returned renditions are ordered (natural sort order)
        // List all renditions
        response = getAll(getNodeRenditionsUrl(contentNodeId), paging, params, 200);
        renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        assertTrue(Ordering.natural().isOrdered(renditions));
        // Try again to make sure the ordering wasn't coincidental
        response = getAll(getNodeRenditionsUrl(contentNodeId), paging, params, 200);
        renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        assertTrue(Ordering.natural().isOrdered(renditions));

        // nodeId in the path parameter does not represent a file
        getAll(getNodeRenditionsUrl(folder_Id), paging, params, 400);

        // nodeId in the path parameter does not exist
        getAll(getNodeRenditionsUrl(UUID.randomUUID().toString()), paging, params, 404);

        // Create a node without any content
        String emptyContentNodeId = addToDocumentLibrary(userOneN1Site, "emptyDoc.txt", TYPE_CM_CONTENT, userOneN1.getId());
        getAll(getNodeRenditionsUrl(emptyContentNodeId), paging, params, 200);

        // Invalid status value
        params.put("where", "(status='WRONG')");
        getAll(getNodeRenditionsUrl(contentNodeId), paging, params, 400);

        // Invalid filter (only 'status' is supported)
        params.put("where", "(id='doclib')");
        getAll(getNodeRenditionsUrl(contentNodeId), paging, params, 400);
    }

    /**
     * Tests get node rendition.
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/renditions/<renditionId>}
     */
    @Test
    public void testGetNodeRendition() throws Exception
    {
        setRequestContext(networkN1.getId(), userOneN1.getId(), null);
        
        // Create a folder within the site document's library
        String folderName = "folder" + System.currentTimeMillis();
        String folder_Id = addToDocumentLibrary(userOneN1Site, folderName, TYPE_CM_FOLDER, userOneN1.getId());

        // Create multipart request
        String fileName = "quick.pdf";
        File file = getResourceFile(fileName);
        MultiPartBuilder multiPartBuilder = MultiPartBuilder.create().setFileData(new FileData(fileName, file));
        MultiPartRequest reqBody = multiPartBuilder.build();

        // Upload quick.pdf file into 'folder'
        HttpResponse response = post(getNodeChildrenUrl(folder_Id), reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        String contentNodeId = document.getId();

        // pause briefly
        Thread.sleep(DELAY_IN_MS);

        // Get rendition (not created yet) information for node
        response = getSingle(getNodeRenditionsUrl(contentNodeId), "doclib", 200);
        Rendition rendition = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Rendition.class);
        assertNotNull(rendition);
        assertEquals(RenditionStatus.NOT_CREATED, rendition.getStatus());
        ContentInfo contentInfo = rendition.getContent();
        assertNotNull(contentInfo);
        assertEquals(MimetypeMap.MIMETYPE_IMAGE_PNG, contentInfo.getMimeType());
        assertEquals("PNG Image", contentInfo.getMimeTypeName());
        assertNull("Shouldn't have returned the encoding, as the rendition hasn't been created yet.", contentInfo.getEncoding());
        assertNull("Shouldn't have returned the size, as the rendition hasn't been created yet.", contentInfo.getSizeInBytes());

        // Create and get 'doclib' rendition
        rendition = createAndGetRendition(contentNodeId, "doclib");
        assertNotNull(rendition);
        assertEquals(RenditionStatus.CREATED, rendition.getStatus());
        contentInfo = rendition.getContent();
        assertNotNull(contentInfo);
        assertEquals(MimetypeMap.MIMETYPE_IMAGE_PNG, contentInfo.getMimeType());
        assertEquals("PNG Image", contentInfo.getMimeTypeName());
        assertNotNull(contentInfo.getEncoding());
        assertTrue(contentInfo.getSizeInBytes() > 0);

        // nodeId in the path parameter does not represent a file
        getSingle(getNodeRenditionsUrl(folder_Id), "doclib", 400);

        // nodeId in the path parameter does not exist
        getSingle(getNodeRenditionsUrl(UUID.randomUUID().toString()), "doclib", 404);

        // renditionId in the path parameter is not registered/available
        getSingle(getNodeRenditionsUrl(contentNodeId), ("renditionId" + System.currentTimeMillis()), 404);

        if (areLocalTransformsAvailable())
        {
            String emptyContentNodeId = addToDocumentLibrary(userOneN1Site, "emptyDoc.txt", TYPE_CM_CONTENT, userOneN1.getId());
            getSingle(getNodeRenditionsUrl(emptyContentNodeId), "doclib", 200);
        }

        // Create multipart request
        String jpgFileName = "quick.jpg";
        File jpgFile = getResourceFile(fileName);
        reqBody = MultiPartBuilder.create()
                    .setFileData(new FileData(jpgFileName, jpgFile))
                    .build();

        // Upload quick.jpg file into 'folder'
        response = post(getNodeChildrenUrl(folder_Id), reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document jpgImage = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        String jpgImageNodeId = jpgImage.getId();

        // List all available renditions (includes those that have been created and those that are yet to be created)
        response = getAll(getNodeRenditionsUrl(jpgImageNodeId), getPaging(0, 50), 200);
        List<Rendition> renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        // Check there is no pdf rendition is available for the jpg file
        Rendition pdf = getRendition(renditions, "pdf");
        assertNull(pdf);

        // The renditionId (pdf) is registered but it is not applicable for the node's mimeType
        getSingle(getNodeRenditionsUrl(jpgImageNodeId), "pdf", 404);
    }

    /**
     * Tests create rendition.
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/renditions}
     */
    @Test
    public void testCreateRendition() throws Exception
    {
        setRequestContext(networkN1.getId(), userOneN1.getId(), null);
        
        // Create a folder within the site document's library
        String folderName = "folder" + System.currentTimeMillis();
        String folder_Id = addToDocumentLibrary(userOneN1Site, folderName, TYPE_CM_FOLDER, userOneN1.getId());

        // Create multipart request
        String fileName = "quick.pdf";
        File file = getResourceFile(fileName);
        MultiPartBuilder multiPartBuilder = MultiPartBuilder.create().setFileData(new FileData(fileName, file));
        MultiPartRequest reqBody = multiPartBuilder.build();

        // Upload quick.pdf file into 'folder'
        HttpResponse response = post(getNodeChildrenUrl(folder_Id), reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        String contentNodeId = document.getId();

        // pause briefly
        Thread.sleep(DELAY_IN_MS);

        // Get rendition (not created yet) information for node
        response = getSingle(getNodeRenditionsUrl(contentNodeId), "imgpreview", 200);
        Rendition rendition = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Rendition.class);
        assertNotNull(rendition);
        assertEquals(RenditionStatus.NOT_CREATED, rendition.getStatus());

        // Try to download non-existent rendition (and no placeholder)
        Map<String, String> params = new HashMap<>();
        params.put("placeholder", "false");
        getSingle(getNodeRenditionsUrl(contentNodeId), ("doclib/content"), params, 404);

        // Download placeholder instead
        params = new HashMap<>();
        params.put("placeholder", "true");
        response = getSingle(getNodeRenditionsUrl(contentNodeId), ("doclib/content"), params, 200);
        assertNotNull(response.getResponseAsBytes());

        // Create and get 'imgpreview' rendition
        rendition = createAndGetRendition(contentNodeId, "imgpreview");
        assertNotNull(rendition);
        assertEquals(RenditionStatus.CREATED, rendition.getStatus());
        ContentInfo contentInfo = rendition.getContent();
        assertNotNull(contentInfo);
        assertEquals(MimetypeMap.MIMETYPE_IMAGE_JPEG, contentInfo.getMimeType());
        assertEquals("JPEG Image", contentInfo.getMimeTypeName());
        assertNotNull(contentInfo.getEncoding());
        assertTrue(contentInfo.getSizeInBytes() > 0);

        // Create 'doclib' rendition request
        Rendition renditionRequest = new Rendition().setId("doclib");

        if (areLocalTransformsAvailable())
        {
        // Create a node without any content
        String emptyContentNodeId = addToDocumentLibrary(userOneN1Site, "emptyDoc.txt", TYPE_CM_CONTENT, userOneN1.getId());
        post(getNodeRenditionsUrl(emptyContentNodeId), toJsonAsString(renditionRequest), 202);

        // Rendition for binary content
        String content = "The quick brown fox jumps over the lazy dog.";
        file = TempFileProvider.createTempFile(new ByteArrayInputStream(content.getBytes()), getClass().getSimpleName(), ".bin");
        multiPartBuilder = MultiPartBuilder.create().setFileData(new FileData("binaryFileName", file));
        reqBody = multiPartBuilder.build();
        response = post(getNodeChildrenUrl(folder_Id), reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document binaryDocument = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        post(getNodeRenditionsUrl(binaryDocument.getId()), toJsonAsString(renditionRequest), 202);
        }

        //
        // -ve Tests
        //
        
        // The rendition requested already exists
        response = post(getNodeRenditionsUrl(contentNodeId), toJsonAsString(new Rendition().setId("imgpreview")), 409);
        ExpectedErrorResponse errorResponse = RestApiUtil.parseErrorResponse(response.getJsonResponse());
        assertNotNull(errorResponse);
        assertNotNull(errorResponse.getErrorKey());
        assertNotNull(errorResponse.getBriefSummary());
        assertNotNull(errorResponse.getStackTrace());
        assertNotNull(errorResponse.getDescriptionURL());
        assertEquals(409, errorResponse.getStatusCode());

        // nodeId in the path parameter does not represent a file
        post(getNodeRenditionsUrl(folder_Id), toJsonAsString(renditionRequest), 400);

        // nodeId in the path parameter does not exist
        response = post(getNodeRenditionsUrl(UUID.randomUUID().toString()), toJsonAsString(renditionRequest), 404);
        // EntityNotFoundException
        errorResponse = RestApiUtil.parseErrorResponse(response.getJsonResponse());
        assertNotNull(errorResponse);
        assertNotNull(errorResponse.getErrorKey());
        assertNotNull(errorResponse.getBriefSummary());
        assertNotNull(errorResponse.getStackTrace());
        assertNotNull(errorResponse.getDescriptionURL());
        assertEquals(404, errorResponse.getStatusCode());

        // renditionId is not registered
        final String randomRenditionId = "renditionId" + System.currentTimeMillis();
        post(getNodeRenditionsUrl(contentNodeId), toJsonAsString(new Rendition().setId(randomRenditionId)), 404);

        // renditionId is null
        post(getNodeRenditionsUrl(contentNodeId), toJsonAsString(new Rendition().setId(null)), 400);

        // renditionId is empty
        post(getNodeRenditionsUrl(contentNodeId), toJsonAsString(new Rendition().setId("")), 400);

        // Multiple rendition request (as of ACS 6.1)

        // Multiple rendition request, including an empty id
        List<Rendition> multipleRenditionRequest = new ArrayList<>(2);
        multipleRenditionRequest.add(new Rendition().setId(""));
        multipleRenditionRequest.add(new Rendition().setId("medium"));
        post(getNodeRenditionsUrl(contentNodeId), toJsonAsString(multipleRenditionRequest), 400);

        // Multiple rendition request. doclib has already been done
        multipleRenditionRequest = new ArrayList<>(3);
        multipleRenditionRequest.add(new Rendition().setId("doclib"));
        multipleRenditionRequest.add(new Rendition().setId("medium"));
        multipleRenditionRequest.add(new Rendition().setId("avatar,avatar32"));
        post(getNodeRenditionsUrl(contentNodeId), toJsonAsString(multipleRenditionRequest), 202);
        assertRenditionCreatedWithWait(contentNodeId, "doclib", "medium", "avatar", "avatar32");

        // Multiple rendition request. All have already been done.
        multipleRenditionRequest = new ArrayList<>(2);
        multipleRenditionRequest.add(new Rendition().setId("doclib"));
        multipleRenditionRequest.add(new Rendition().setId("medium"));
        multipleRenditionRequest.add(new Rendition().setId("avatar"));
        post(getNodeRenditionsUrl(contentNodeId), toJsonAsString(multipleRenditionRequest), 409);

        // Disable thumbnail generation
        RenditionService2Impl renditionService2 = applicationContext.getBean("renditionService2", RenditionService2Impl.class);
        renditionService2.setThumbnailsEnabled(false);
        try
        {
            // Create multipart request
            String txtFileName = "quick-1.txt";
            File txtFile = getResourceFile(fileName);
            reqBody = MultiPartBuilder.create().setFileData(new FileData(txtFileName, txtFile)).build();

            // Upload quick-1.txt file into 'folder'
            response = post(getNodeChildrenUrl(folder_Id), reqBody.getBody(), null, reqBody.getContentType(), 201);
            Document txtDocument = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
            // Thumbnail generation has been disabled
            response = post(getNodeRenditionsUrl(txtDocument.getId()), toJsonAsString(renditionRequest), 501);
            errorResponse = RestApiUtil.parseErrorResponse(response.getJsonResponse());
            assertNotNull(errorResponse);
            assertNotNull(errorResponse.getErrorKey());
            assertNotNull(errorResponse.getBriefSummary());
            assertNotNull(errorResponse.getStackTrace());
            assertNotNull(errorResponse.getDescriptionURL());
            assertEquals(501, errorResponse.getStatusCode());
        }
        finally
        {
            renditionService2.setThumbnailsEnabled(true);
        }
    }

    @Test
    public void testRequestRenditionContentDirectUrl() throws Exception
    {
        setRequestContext(user1);

        RepoService.TestNetwork networkN1;
        RepoService.TestPerson userOneN1;
        Site userOneN1Site;

        networkN1 = repoService.createNetworkWithAlias("ping", true);
        networkN1.create();
        userOneN1 = networkN1.createUser();

        setRequestContext(networkN1.getId(), userOneN1.getId(), null);

        String siteTitle = "RandomSite" + System.currentTimeMillis();
        userOneN1Site = createSite(siteTitle, SiteVisibility.PRIVATE);

        // Create a folder within the site document's library
        String folderName = "folder" + System.currentTimeMillis();
        String parentId = getSiteContainerNodeId(userOneN1Site.getId(), "documentLibrary");
        String folder_Id = createNode(parentId, folderName, TYPE_CM_FOLDER, null).getId();

        // Create multipart request - pdf file
        String renditionName = "doclib";
        String fileName = "quick.pdf";
        File file = getResourceFile(fileName);
        MultiPartRequest reqBody = MultiPartBuilder.create()
                .setFileData(new FileData(fileName, file))
                .setRenditions(Collections.singletonList(renditionName))
                .build();

        // Upload quick.pdf file into 'folder' - including request to create 'doclib' thumbnail
        HttpResponse response = post(getNodeChildrenUrl(folder_Id), reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        String contentNodeId = document.getId();

        // wait and check that rendition is created ...
        Rendition rendition = waitAndGetRendition(contentNodeId, null, renditionName);
        assertNotNull(rendition);
        assertEquals(Rendition.RenditionStatus.CREATED, rendition.getStatus());

        HttpResponse dauResponse = post(getRequestRenditionDirectAccessUrl(contentNodeId, renditionName), null, null, null, null, 501);
    }

    /**
     * Tests create rendition when on upload/create of a file
     *
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/children}
     */
    @Test
    public void testCreateRenditionOnUpload() throws Exception
    {
        String userId = userOneN1.getId();
        setRequestContext(networkN1.getId(), userOneN1.getId(), null);

        // Create a folder within the site document's library
        String folderName = "folder" + System.currentTimeMillis();
        String folder_Id = addToDocumentLibrary(userOneN1Site, folderName, TYPE_CM_FOLDER, userId);

        // Create multipart request - pdf file
        String renditionName = "doclib";
        String fileName = "quick.pdf";
        File file = getResourceFile(fileName);
        MultiPartRequest reqBody = MultiPartBuilder.create()
                .setFileData(new FileData(fileName, file))
                .setRenditions(Collections.singletonList(renditionName))
                .build();

        // Upload quick.pdf file into 'folder' - including request to create 'doclib' thumbnail
        HttpResponse response = post(getNodeChildrenUrl(folder_Id), reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        String contentNodeId = document.getId();

        // wait and check that rendition is created ...
        Rendition rendition = waitAndGetRendition(contentNodeId, null, renditionName);
        assertNotNull(rendition);
        assertEquals(RenditionStatus.CREATED, rendition.getStatus());

        Map<String, String> params = new HashMap<>();
        params.put("placeholder", "false");
        response = getSingle(getNodeRenditionsUrl(contentNodeId), ("doclib/content"), params, 200);
        assertNotNull(response.getResponseAsBytes());

        if (areLocalTransformsAvailable())
        {
            // Create multipart request - Word doc file
            renditionName = "doclib";
            fileName = "farmers_markets_list_2003.doc";
            file = getResourceFile(fileName);
            reqBody = MultiPartBuilder.create()
                    .setFileData(new FileData(fileName, file))
                    .setRenditions(Collections.singletonList(renditionName))
                    .build();

            // Upload file into 'folder' - including request to create 'doclib' thumbnail
            response = post(getNodeChildrenUrl(folder_Id), reqBody.getBody(), null, reqBody.getContentType(), 201);
            document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
            contentNodeId = document.getId();

            assertRenditionCreatedWithWait(contentNodeId, renditionName);
        }

        /* RA-834: commented-out since not currently applicable for empty file
        Document d1 = new Document();
        d1.setName("d1.txt");
        d1.setNodeType("cm:content");
        ContentInfo ci = new ContentInfo();
        ci.setMimeType(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        d1.setContent(ci);

        // create empty file including request to generate a thumbnail
        renditionName = "doclib";
        response = post(getNodeChildrenUrl(folder_Id), userId, toJsonAsStringNonNull(d1), "?renditions="+renditionName, 201);
        Document documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        String d1Id = documentResp.getId();

        // wait and check that rendition is created ...
        rendition = waitAndGetRendition(userId, d1Id, renditionName);
        assertNotNull(rendition);
        assertEquals(RenditionStatus.CREATED, rendition.getStatus());
        */

        // Multiple renditions requested
        reqBody = MultiPartBuilder.create()
                .setFileData(new FileData(fileName, file))
                .setAutoRename(true)
                .setRenditions(Arrays.asList(new String[]{"doclib,imgpreview"}))
                .build();
        post(getNodeChildrenUrl(folder_Id), reqBody.getBody(), null, reqBody.getContentType(), 201);

        // Unknown rendition
        reqBody = MultiPartBuilder.create()
                .setFileData(new FileData(fileName, file))
                .setAutoRename(true)
                .setRenditions(Arrays.asList(new String[]{"unknown"}))
                .build();
        post(getNodeChildrenUrl(folder_Id), reqBody.getBody(), null, reqBody.getContentType(), 404);

        // ThumbnailService is disabled
        ThumbnailService thumbnailService = applicationContext.getBean("thumbnailService", ThumbnailService.class);
        thumbnailService.setThumbnailsEnabled(false);
        try
        {
            // Create multipart request
            String txtFileName = "quick-1.txt";
            File txtFile = getResourceFile(fileName);
            reqBody = MultiPartBuilder.create()
                    .setFileData(new FileData(txtFileName, txtFile))
                    .setRenditions(Arrays.asList(new String[]{"doclib"}))
                    .build();

            post(getNodeChildrenUrl(folder_Id), reqBody.getBody(), null, reqBody.getContentType(), 201);
        }
        finally
        {
            thumbnailService.setThumbnailsEnabled(true);
        }
    }

    protected void assertRenditionCreatedWithWait(String contentNodeId, String... renditionNames) throws Exception
    {
        for (String renditionName : renditionNames)
        {
            Rendition rendition = waitAndGetRendition(contentNodeId, null, renditionName);
            assertNotNull(rendition);
            assertEquals(RenditionStatus.CREATED, rendition.getStatus());
        }
    }

    /**
     * Tests create rendition after uploading new version(s)
     *
     */
    @Test
    public void testCreateRenditionForNewVersion() throws Exception
    {
        String PROP_LTM = "cm:lastThumbnailModification";
        
        String RENDITION_NAME = "imgpreview";
        
        String userId = userOneN1.getId();
        setRequestContext(networkN1.getId(), userOneN1.getId(), null);

        // Create a folder within the site document's library
        String folderName = "folder" + System.currentTimeMillis();
        String folder_Id = addToDocumentLibrary(userOneN1Site, folderName, TYPE_CM_FOLDER, userId);

        // Create multipart request - pdf file
        String fileName = "quick.pdf";
        File file = getResourceFile(fileName);
        MultiPartRequest reqBody = MultiPartBuilder.create()
                .setFileData(new FileData(fileName, file))
                .build();
        Map<String, String> params = Collections.singletonMap("include", "properties");

        // Upload quick.pdf file into 'folder' - do not include request to create 'doclib' thumbnail
        HttpResponse response = post(getNodeChildrenUrl(folder_Id), reqBody.getBody(), params, null, "alfresco", reqBody.getContentType(), 201);
        Document document1 = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        String contentNodeId = document1.getId();
        assertNotNull(document1.getProperties());
        assertNull(document1.getProperties().get(PROP_LTM));
        
        // pause briefly
        Thread.sleep(DELAY_IN_MS);

        // Get rendition (not created yet) information for node
        response = getSingle(getNodeRenditionsUrl(contentNodeId), RENDITION_NAME, 200);
        Rendition rendition = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Rendition.class);
        assertNotNull(rendition);
        assertEquals(RenditionStatus.NOT_CREATED, rendition.getStatus());

        params = new HashMap<>();
        params.put("placeholder", "false");
        getSingle(getNodeRenditionsUrl(contentNodeId), (RENDITION_NAME+"/content"), params, 404);
        
        // TODO add test to request creation of rendition as another user (that has read-only access on the content, not write)

        // Create and get 'imgpreview' rendition
        rendition = createAndGetRendition(contentNodeId, RENDITION_NAME);
        assertNotNull(rendition);
        assertEquals(RenditionStatus.CREATED, rendition.getStatus());
        ContentInfo contentInfo = rendition.getContent();
        assertNotNull(contentInfo);
        assertEquals(MimetypeMap.MIMETYPE_IMAGE_JPEG, contentInfo.getMimeType());
        assertEquals("JPEG Image", contentInfo.getMimeTypeName());
        assertNotNull(contentInfo.getEncoding());
        assertTrue(contentInfo.getSizeInBytes() > 0);

        params = new HashMap<>();
        params.put("placeholder", "false");
        response = getSingle(getNodeRenditionsUrl(contentNodeId), (RENDITION_NAME+"/content"), params, 200);
        
        byte[] renditionBytes1 = response.getResponseAsBytes();
        assertNotNull(renditionBytes1);
        
        // check node details ...
        params = Collections.singletonMap("include", "properties");
        response = getSingle(NodesEntityResource.class, contentNodeId, params, 200);
        Document document1b = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        
//        assertEquals(document1b.getModifiedAt(), document1.getModifiedAt());
        assertEquals(document1b.getModifiedByUser().getId(), document1.getModifiedByUser().getId());
        assertEquals(document1b.getModifiedByUser().getDisplayName(), document1.getModifiedByUser().getDisplayName());
        
        assertNotEquals(document1b.getProperties().get(PROP_LTM), document1.getProperties().get(PROP_LTM));
        
        // upload another version of "quick.pdf" and check again
        
        fileName = "quick-2.pdf";
        file = getResourceFile(fileName);
        reqBody = MultiPartBuilder.create()
                .setFileData(new FileData("quick.pdf", file))
                .setOverwrite(true)
                .build();

        response = post(getNodeChildrenUrl(folder_Id), reqBody.getBody(), null, null, "alfresco", reqBody.getContentType(), 201);
        Document document2 = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertEquals(contentNodeId, document2.getId());

        // wait to allow new version of the rendition to be created ...
        Thread.sleep(DELAY_IN_MS * 4);

        params = new HashMap<>();
        params.put("placeholder", "false");
        response = getSingle(getNodeRenditionsUrl(contentNodeId), (RENDITION_NAME+"/content"), params, 200);
        assertNotNull(response.getResponseAsBytes());
        
        // check rendition binary has changed
        assertNotEquals(renditionBytes1, response.getResponseAsBytes());

        // check node details ...
        params = Collections.singletonMap("include", "properties");
        response = getSingle(NodesEntityResource.class, contentNodeId, params, 200);
        Document document2b = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

//        assertTrue(document2b.getModifiedAt().after(document1.getModifiedAt()));
        assertEquals(document2b.getModifiedByUser().getId(), document1.getModifiedByUser().getId());
        assertEquals(document2b.getModifiedByUser().getDisplayName(), document1.getModifiedByUser().getDisplayName());
        
        // check last thumbnail modification property has changed ! (REPO-1644)
        assertNotEquals(document2b.getProperties().get(PROP_LTM), document1b.getProperties().get(PROP_LTM));
    }

    /**
     * Tests download rendition.
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/renditions/<renditionId>/content}
     */
    @Test
    public void testDownloadRendition() throws Exception
    {
        setRequestContext(networkN1.getId(), userOneN1.getId(), null);
        
        // Create a folder within the site document's library
        String folderName = "folder" + System.currentTimeMillis();
        String folder_Id = addToDocumentLibrary(userOneN1Site, folderName, TYPE_CM_FOLDER, userOneN1.getId());

        // Create multipart request
        String fileName = "quick.pdf";
        File file = getResourceFile(fileName);
        MultiPartBuilder multiPartBuilder = MultiPartBuilder.create()
                    .setFileData(new FileData(fileName, file));
        MultiPartRequest reqBody = multiPartBuilder.build();

        // Upload quick.pdf file into 'folder'
        HttpResponse response = post(getNodeChildrenUrl(folder_Id), reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        String contentNodeId = document.getId();

        // pause briefly
        Thread.sleep(DELAY_IN_MS);

        // Get rendition (not created yet) information for node
        response = getSingle(getNodeRenditionsUrl(contentNodeId), "doclib", 200);
        Rendition rendition = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Rendition.class);
        assertNotNull(rendition);
        assertEquals(RenditionStatus.NOT_CREATED, rendition.getStatus());

        // Download placeholder - by default with Content-Disposition header
        Map<String, String> params = new HashMap<>();
        params.put("placeholder", "true");
        response = getSingle(getNodeRenditionsUrl(contentNodeId), ("doclib/content"), params, 200);
        assertNotNull(response.getResponseAsBytes());
        Map<String, String> responseHeaders = response.getHeaders();
        assertNotNull(responseHeaders);
        String contentDisposition = responseHeaders.get("Content-Disposition");
        assertNotNull(contentDisposition);
        assertTrue(contentDisposition.contains("filename=\"doclib\""));
        String contentType = responseHeaders.get("Content-Type");
        assertNotNull(contentType);
        assertTrue(contentType.startsWith(MimetypeMap.MIMETYPE_IMAGE_PNG));

        // Download placeholder - without Content-Disposition header (attachment=false)
        params.put("attachment", "false");
        response = getSingle(getNodeRenditionsUrl(contentNodeId), ("doclib/content"), params, 200);
        assertNotNull(response.getResponseAsBytes());
        responseHeaders = response.getHeaders();
        assertNotNull(responseHeaders);
        String cacheControl = responseHeaders.get("Cache-Control");
        assertNotNull(cacheControl);
        assertTrue(cacheControl.contains("must-revalidate"));
        assertNull(responseHeaders.get("Content-Disposition"));
        contentType = responseHeaders.get("Content-Type");
        assertNotNull(contentType);
        assertTrue(contentType.startsWith(MimetypeMap.MIMETYPE_IMAGE_PNG));

        // Test 304 response - placeholder=true&attachment=false
        String lastModifiedHeader = responseHeaders.get(LAST_MODIFIED_HEADER);
        assertNotNull(lastModifiedHeader);
        Map<String, String> headers = Collections.singletonMap(IF_MODIFIED_SINCE_HEADER, lastModifiedHeader);
        // Currently the placeholder file is not cached.
        // As the placeholder is not a NodeRef, so we can't get the ContentModel.PROP_MODIFIED date.
        getSingle(getNodeRenditionsUrl(contentNodeId), "doclib/content", params, headers, 200);

        // Create and get 'doclib' rendition
        rendition = createAndGetRendition(contentNodeId, "doclib");
        assertNotNull(rendition);
        assertEquals(RenditionStatus.CREATED, rendition.getStatus());

        // Download rendition - by default with Content-Disposition header
        response = getSingle(getNodeRenditionsUrl(contentNodeId), "doclib/content", 200);
        assertNotNull(response.getResponseAsBytes());
        responseHeaders = response.getHeaders();
        assertNotNull(responseHeaders);
        contentDisposition = responseHeaders.get("Content-Disposition");
        assertNotNull(contentDisposition);
        assertTrue(contentDisposition.contains("filename=\"doclib\""));
        contentType = responseHeaders.get("Content-Type");
        assertNotNull(contentType);
        assertTrue(contentType.startsWith(MimetypeMap.MIMETYPE_IMAGE_PNG));

        // Download rendition - without Content-Disposition header (attachment=false)
        params = Collections.singletonMap("attachment", "false");
        response = getSingle(getNodeRenditionsUrl(contentNodeId), "doclib/content", params, 200);
        assertNotNull(response.getResponseAsBytes());
        responseHeaders = response.getHeaders();
        assertNotNull(responseHeaders);
        assertNull(responseHeaders.get("Content-Disposition"));
        contentType = responseHeaders.get("Content-Type");
        assertNotNull(contentType);
        assertTrue(contentType.startsWith(MimetypeMap.MIMETYPE_IMAGE_PNG));

        // Download rendition - with Content-Disposition header (attachment=true) same as default
        params = Collections.singletonMap("attachment", "true");
        response = getSingle(getNodeRenditionsUrl(contentNodeId), "doclib/content", params, 200);
        assertNotNull(response.getResponseAsBytes());
        responseHeaders = response.getHeaders();
        assertNotNull(responseHeaders);
        // Check the cache settings which have been set in the RenditionsImpl#getContent()
        cacheControl = responseHeaders.get("Cache-Control");
        assertNotNull(cacheControl);
        assertFalse(cacheControl.contains("must-revalidate"));
        assertTrue(cacheControl.contains("max-age=31536000"));
        contentDisposition = responseHeaders.get("Content-Disposition");
        assertNotNull(contentDisposition);
        assertTrue(contentDisposition.contains("filename=\"doclib\""));
        contentType = responseHeaders.get("Content-Type");
        assertNotNull(contentType);
        assertTrue(contentType.startsWith(MimetypeMap.MIMETYPE_IMAGE_PNG));

        // Test 304 response - doclib rendition (attachment=true)
        lastModifiedHeader = responseHeaders.get(LAST_MODIFIED_HEADER);
        assertNotNull(lastModifiedHeader);
        headers = Collections.singletonMap(IF_MODIFIED_SINCE_HEADER, lastModifiedHeader);
        getSingle(getNodeRenditionsUrl(contentNodeId), "doclib/content", params, headers, 304);

        // Here we want to overwrite/update the existing content in order to force a new rendition creation,
        // so the ContentModel.PROP_MODIFIED date would be different. Hence, we use the multipart upload by providing
        // the old fileName and setting overwrite field to true
        file = getResourceFile("quick-2.pdf");
        multiPartBuilder = MultiPartBuilder.create()
                    .setFileData(new FileData(fileName, file))
                    .setOverwrite(true);
        reqBody = multiPartBuilder.build();

        // Update quick.pdf
        post(getNodeChildrenUrl(folder_Id), reqBody.getBody(), null, reqBody.getContentType(), 201);

        // The requested "If-Modified-Since" date is older than rendition modified date
        response = getSingleWithDelayRetry(getNodeRenditionsUrl(contentNodeId), "doclib/content", params, headers, MAX_RETRY,
                    PAUSE_TIME, 200);
        assertNotNull(response);
        responseHeaders = response.getHeaders();
        assertNotNull(responseHeaders);
        String newLastModifiedHeader = responseHeaders.get(LAST_MODIFIED_HEADER);
        assertNotNull(newLastModifiedHeader);
        assertNotEquals(lastModifiedHeader, newLastModifiedHeader);

        //-ve tests
        // nodeId in the path parameter does not represent a file
        getSingle(getNodeRenditionsUrl(folder_Id), "doclib/content", 400);

        // nodeId in the path parameter does not exist
        getSingle(getNodeRenditionsUrl(UUID.randomUUID().toString()), "doclib/content", 404);

        // renditionId in the path parameter is not registered/available
        getSingle(getNodeRenditionsUrl(contentNodeId), ("renditionId" + System.currentTimeMillis() + "/content"), 404);

        InputStream inputStream = new ByteArrayInputStream("The quick brown fox jumps over the lazy dog".getBytes());
        file = TempFileProvider.createTempFile(inputStream, "RenditionsTest-", ".abcdef");
        reqBody = MultiPartBuilder.create()
                    .setFileData(new FileData(file.getName(), file))
                    .build();
        // Upload temp file into 'folder'
        response = post(getNodeChildrenUrl(folder_Id), reqBody.getBody(), null, reqBody.getContentType(), 201);
        document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        contentNodeId = document.getId();

        // The content of the rendition does not exist and the placeholder parameter is not present
        getSingle(getNodeRenditionsUrl(contentNodeId), "doclib/content", 404);

        // The content of the rendition does not exist and the placeholder parameter has a value of "false"
        params = Collections.singletonMap("placeholder", "false");
        getSingle(getNodeRenditionsUrl(contentNodeId), "doclib/content", params, 404);

        // The rendition does not exist, a placeholder is not available and the placeholder parameter has a value of "true"
        params = Collections.singletonMap("placeholder", "true");
        getSingle(getNodeRenditionsUrl(contentNodeId), ("renditionId" + System.currentTimeMillis() + "/content"), params, 404);

        // Create a node without any content
        String emptyContentNodeId = addToDocumentLibrary(userOneN1Site, "emptyDoc.txt", TYPE_CM_CONTENT, userOneN1.getId());
        getSingle(getNodeRenditionsUrl(emptyContentNodeId), "doclib/content", params, 200);
    }

    private String addToDocumentLibrary(Site testSite, String name, String nodeType, String userId) throws Exception
    {
        String parentId = getSiteContainerNodeId(testSite.getId(), "documentLibrary");
        return createNode(parentId, name, nodeType, null).getId();
    }

    private Rendition getRendition(List<Rendition> renditions, String renditionName)
    {
        for (Rendition rn : renditions)
        {
            if (rn.getId().equals(renditionName))
            {
                return rn;
            }
        }
        return null;
    }

    @Override
    public String getScope()
    {
        return "public";
    }

    /**
     * Returns <code>true</code> if doc to pdf transformations available, indicating Local transforms ar available.
     */
    protected boolean areLocalTransformsAvailable()
    {
        return synchronousTransformClient.isSupported(MimetypeMap.MIMETYPE_WORD, -1, null,
                MimetypeMap.MIMETYPE_PDF, Collections.emptyMap(), null, null);
    }
}
