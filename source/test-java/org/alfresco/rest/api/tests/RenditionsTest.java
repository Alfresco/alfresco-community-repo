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
import static org.alfresco.rest.api.tests.util.RestApiUtil.toJsonAsStringNonNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.google.common.collect.Ordering;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.alfresco.rest.api.tests.RepoService.TestPerson;
import org.alfresco.rest.api.tests.RepoService.TestSite;
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
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.namespace.QName;
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
 * Renditions API tests.
 *
 * @author Jamal Kaabi-Mofrad
 */
public class RenditionsTest extends AbstractBaseApiTest
{
    /**
     * User one from network one
     */
    private TestPerson userOneN1;

    /**
     * Private site of user one from network one
     */
    private TestSite userOneN1Site;

    @Before
    public void setup() throws Exception
    {
        TestNetwork networkOne = repoService.createNetworkWithAlias("ping", true);
        networkOne.create();

        userOneN1 = networkOne.createUser();
        AuthenticationUtil.setFullyAuthenticatedUser(userOneN1.getId());
        userOneN1Site = createSite(networkOne, userOneN1, SiteVisibility.PRIVATE);
    }

    @After
    public void tearDown() throws Exception
    {
        AuthenticationUtil.clearCurrentSecurityContext();
    }

    /**
     * Tests get node renditions.
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/renditions}
     */
    @Test
    public void testListNodeRenditions() throws Exception
    {
        // Create a folder within the site document's library
        String folderName = "folder" + System.currentTimeMillis();
        String folder_Id = addToDocumentLibrary(userOneN1Site, folderName, ContentModel.TYPE_FOLDER, userOneN1.getId());

        // Create multipart request
        String fileName = "quick.pdf";
        File file = getResourceFile(fileName);
        MultiPartBuilder multiPartBuilder = MultiPartBuilder.create().setFileData(new FileData(fileName, file, MimetypeMap.MIMETYPE_PDF));
        MultiPartRequest reqBody = multiPartBuilder.build();

        // Upload quick.pdf file into 'folder'
        HttpResponse response = post(getNodeChildrenUrl(folder_Id), userOneN1.getId(), reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        String contentNodeId = document.getId();

        Paging paging = getPaging(0, 50);
        // List all available renditions (includes those that have been created and those that are yet to be created)
        response = getAll(getNodeRenditionsUrl(contentNodeId), userOneN1.getId(), paging, 200);
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
        response = getAll(getNodeRenditionsUrl(contentNodeId), userOneN1.getId(), paging, params, 200);
        renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        assertTrue(renditions.size() >= 5);

        params.put("where", "(status='CREATED')");
        // List only the CREATED renditions
        response = getAll(getNodeRenditionsUrl(contentNodeId), userOneN1.getId(), paging, params, 200);
        renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        assertEquals("There is no rendition created yet.", 0, renditions.size());

        // Test paging
        // SkipCount=0,MaxItems=2
        paging = getPaging(0, 2);
        // List all available renditions
        response = getAll(getNodeRenditionsUrl(contentNodeId), userOneN1.getId(), paging, 200);
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
        response = getAll(getNodeRenditionsUrl(contentNodeId), userOneN1.getId(), paging, 200);
        renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        assertEquals(3, renditions.size());
        expectedPaging = RestApiUtil.parsePaging(response.getJsonResponse());
        assertEquals(3, expectedPaging.getCount().intValue());
        assertEquals(2, expectedPaging.getSkipCount().intValue());
        assertEquals(3, expectedPaging.getMaxItems().intValue());
        assertTrue(expectedPaging.getTotalItems() >= 5);

        // Create 'doclib' rendition
        createAndGetRendition(userOneN1.getId(), contentNodeId, docLib.getId());

        // List all available renditions (includes those that have been created and those that are yet to be created)
        paging = getPaging(0, 50);
        response = getAll(getNodeRenditionsUrl(contentNodeId), userOneN1.getId(), paging, 200);
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
        response = getAll(getNodeRenditionsUrl(contentNodeId), userOneN1.getId(), paging, params, 200);
        renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        assertEquals("Should've only returned the 'doclib' rendition.", 1, renditions.size());

        params.put("where", "(status='NOT_CREATED')");
        // List only the NOT_CREATED renditions
        response = getAll(getNodeRenditionsUrl(contentNodeId), userOneN1.getId(), paging, params, 200);
        renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        assertTrue(renditions.size() > 0);
        docLib = getRendition(renditions, "doclib");
        assertNull("'doclib' rendition has already been created.", docLib);

        // Test returned renditions are ordered (natural sort order)
        // List all renditions
        response = getAll(getNodeRenditionsUrl(contentNodeId), userOneN1.getId(), paging, params, 200);
        renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        assertTrue(Ordering.natural().isOrdered(renditions));
        // Try again to make sure the ordering wasn't coincidental
        response = getAll(getNodeRenditionsUrl(contentNodeId), userOneN1.getId(), paging, params, 200);
        renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        assertTrue(Ordering.natural().isOrdered(renditions));

        // nodeId in the path parameter does not represent a file
        getAll(getNodeRenditionsUrl(folder_Id), userOneN1.getId(), paging, params, 400);

        // nodeId in the path parameter does not exist
        getAll(getNodeRenditionsUrl(UUID.randomUUID().toString()), userOneN1.getId(), paging, params, 404);

        // Create a node without any content
        String emptyContentNodeId = addToDocumentLibrary(userOneN1Site, "emptyDoc.txt", ContentModel.TYPE_CONTENT, userOneN1.getId());
        // The source node has no content
        getAll(getNodeRenditionsUrl(emptyContentNodeId), userOneN1.getId(), paging, params, 400);

        // Invalid status value
        params.put("where", "(status='WRONG')");
        getAll(getNodeRenditionsUrl(contentNodeId), userOneN1.getId(), paging, params, 400);

        // Invalid filter (only 'status' is supported)
        params.put("where", "(id='doclib')");
        getAll(getNodeRenditionsUrl(contentNodeId), userOneN1.getId(), paging, params, 400);
    }

    /**
     * Tests get node rendition.
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/renditions/<renditionId>}
     */
    @Test
    public void testGetNodeRendition() throws Exception
    {
        // Create a folder within the site document's library
        String folderName = "folder" + System.currentTimeMillis();
        String folder_Id = addToDocumentLibrary(userOneN1Site, folderName, ContentModel.TYPE_FOLDER, userOneN1.getId());

        // Create multipart request
        String fileName = "quick.pdf";
        File file = getResourceFile(fileName);
        MultiPartBuilder multiPartBuilder = MultiPartBuilder.create().setFileData(new FileData(fileName, file, MimetypeMap.MIMETYPE_PDF));
        MultiPartRequest reqBody = multiPartBuilder.build();

        // Upload quick.pdf file into 'folder'
        HttpResponse response = post(getNodeChildrenUrl(folder_Id), userOneN1.getId(), reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        String contentNodeId = document.getId();

        // Get rendition (not created yet) information for node
        response = getSingle(getNodeRenditionsUrl(contentNodeId), userOneN1.getId(), "doclib", 200);
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
        rendition = createAndGetRendition(userOneN1.getId(), contentNodeId, "doclib");
        assertNotNull(rendition);
        assertEquals(RenditionStatus.CREATED, rendition.getStatus());
        contentInfo = rendition.getContent();
        assertNotNull(contentInfo);
        assertEquals(MimetypeMap.MIMETYPE_IMAGE_PNG, contentInfo.getMimeType());
        assertEquals("PNG Image", contentInfo.getMimeTypeName());
        assertNotNull(contentInfo.getEncoding());
        assertTrue(contentInfo.getSizeInBytes() > 0);

        // nodeId in the path parameter does not represent a file
        getSingle(getNodeRenditionsUrl(folder_Id), userOneN1.getId(), "doclib", 400);

        // nodeId in the path parameter does not exist
        getSingle(getNodeRenditionsUrl(UUID.randomUUID().toString()), userOneN1.getId(), "doclib", 404);

        // renditionId in the path parameter is not registered/available
        getSingle(getNodeRenditionsUrl(contentNodeId), userOneN1.getId(), ("renditionId" + System.currentTimeMillis()), 404);

        // Create a node without any content
        String emptyContentNodeId = addToDocumentLibrary(userOneN1Site, "emptyDoc.txt", ContentModel.TYPE_CONTENT, userOneN1.getId());
        // The source node has no content
        getSingle(getNodeRenditionsUrl(emptyContentNodeId), userOneN1.getId(), "doclib", 400);

        // Create multipart request
        String jpgFileName = "quick.jpg";
        File jpgFile = getResourceFile(fileName);
        reqBody = MultiPartBuilder.create()
                    .setFileData(new FileData(jpgFileName, jpgFile, MimetypeMap.MIMETYPE_IMAGE_JPEG))
                    .build();

        // Upload quick.jpg file into 'folder'
        response = post(getNodeChildrenUrl(folder_Id), userOneN1.getId(), reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document jpgImage = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        String jpgImageNodeId = jpgImage.getId();

        // List all available renditions (includes those that have been created and those that are yet to be created)
        response = getAll(getNodeRenditionsUrl(jpgImageNodeId), userOneN1.getId(), getPaging(0, 50), 200);
        List<Rendition> renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        // Check there is no pdf rendition is available for the jpg file
        Rendition pdf = getRendition(renditions, "pdf");
        assertNull(pdf);

        // The renditionId (pdf) is registered but it is not applicable for the node's mimeType
        getSingle(getNodeRenditionsUrl(jpgImageNodeId), userOneN1.getId(), "pdf", 404);
    }

    /**
     * Tests create rendition.
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/renditions}
     */
    @Test
    public void testCreateRendition() throws Exception
    {
        // Create a folder within the site document's library
        String folderName = "folder" + System.currentTimeMillis();
        String folder_Id = addToDocumentLibrary(userOneN1Site, folderName, ContentModel.TYPE_FOLDER, userOneN1.getId());

        // Create multipart request
        String fileName = "quick.pdf";
        File file = getResourceFile(fileName);
        MultiPartBuilder multiPartBuilder = MultiPartBuilder.create().setFileData(new FileData(fileName, file, MimetypeMap.MIMETYPE_PDF));
        MultiPartRequest reqBody = multiPartBuilder.build();

        // Upload quick.pdf file into 'folder'
        HttpResponse response = post(getNodeChildrenUrl(folder_Id), userOneN1.getId(), reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        String contentNodeId = document.getId();

        // Get rendition (not created yet) information for node
        response = getSingle(getNodeRenditionsUrl(contentNodeId), userOneN1.getId(), "imgpreview", 200);
        Rendition rendition = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Rendition.class);
        assertNotNull(rendition);
        assertEquals(RenditionStatus.NOT_CREATED, rendition.getStatus());

        // Create and get 'imgpreview' rendition
        rendition = createAndGetRendition(userOneN1.getId(), contentNodeId, "imgpreview");
        assertNotNull(rendition);
        assertEquals(RenditionStatus.CREATED, rendition.getStatus());
        ContentInfo contentInfo = rendition.getContent();
        assertNotNull(contentInfo);
        assertEquals(MimetypeMap.MIMETYPE_IMAGE_JPEG, contentInfo.getMimeType());
        assertEquals("JPEG Image", contentInfo.getMimeTypeName());
        assertNotNull(contentInfo.getEncoding());
        assertTrue(contentInfo.getSizeInBytes() > 0);

        // -ve Tests
        // The rendition requested already exists
        response = post(getNodeRenditionsUrl(contentNodeId), userOneN1.getId(), toJsonAsString(new Rendition().setId("imgpreview")), 409);
        ExpectedErrorResponse errorResponse = RestApiUtil.parseErrorResponse(response.getJsonResponse());
        assertNotNull(errorResponse);
        assertNotNull(errorResponse.getErrorKey());
        assertNotNull(errorResponse.getBriefSummary());
        assertNotNull(errorResponse.getStackTrace());
        assertNotNull(errorResponse.getDescriptionURL());
        assertEquals(409, errorResponse.getStatusCode());

        // Create 'doclib' rendition request
        Rendition renditionRequest = new Rendition().setId("doclib");
        // nodeId in the path parameter does not represent a file
        post(getNodeRenditionsUrl(folder_Id), userOneN1.getId(), toJsonAsString(renditionRequest), 400);

        // nodeId in the path parameter does not exist
        response = post(getNodeRenditionsUrl(UUID.randomUUID().toString()), userOneN1.getId(), toJsonAsString(renditionRequest), 404);
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
        post(getNodeRenditionsUrl(contentNodeId), userOneN1.getId(), toJsonAsString(new Rendition().setId(randomRenditionId)), 404);

        // renditionId is null
        post(getNodeRenditionsUrl(contentNodeId), userOneN1.getId(), toJsonAsString(new Rendition().setId(null)), 400);

        // renditionId is empty
        post(getNodeRenditionsUrl(contentNodeId), userOneN1.getId(), toJsonAsString(new Rendition().setId("")), 400);

        // -ve test - we do not currently accept multiple create entities
        List<Rendition> request = new ArrayList<>(2);
        request.add(new Rendition().setId("doclib"));
        request.add(new Rendition().setId("imgpreview"));
        post(getNodeRenditionsUrl(contentNodeId), userOneN1.getId(), toJsonAsString(request), 400);

        // Create a node without any content
        String emptyContentNodeId = addToDocumentLibrary(userOneN1Site, "emptyDoc.txt", ContentModel.TYPE_CONTENT, userOneN1.getId());

        // The source node has no content
        request = new ArrayList<>(2);
        request.add(new Rendition().setId("doclib"));
        post(getNodeRenditionsUrl(emptyContentNodeId), userOneN1.getId(), toJsonAsString(renditionRequest), 400);

        String content = "The quick brown fox jumps over the lazy dog.";
        file = TempFileProvider.createTempFile(new ByteArrayInputStream(content.getBytes()), getClass().getSimpleName(), ".bin");
        multiPartBuilder = MultiPartBuilder.create().setFileData(new FileData("binaryFileName", file, MimetypeMap.MIMETYPE_BINARY));
        reqBody = multiPartBuilder.build();
        response = post(getNodeChildrenUrl(folder_Id), userOneN1.getId(), reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document binaryDocument = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        // No transformer is currently available for 'application/octet-stream'
        post(getNodeRenditionsUrl(binaryDocument.getId()), userOneN1.getId(), toJsonAsString(renditionRequest), 400);

        ThumbnailService thumbnailService = applicationContext.getBean("thumbnailService", ThumbnailService.class);
        // Disable thumbnail generation
        thumbnailService.setThumbnailsEnabled(false);
        try
        {
            // Create multipart request
            String txtFileName = "quick-1.txt";
            File txtFile = getResourceFile(fileName);
            reqBody = MultiPartBuilder.create().setFileData(new FileData(txtFileName, txtFile, MimetypeMap.MIMETYPE_TEXT_PLAIN)).build();

            // Upload quick-1.txt file into 'folder'
            response = post(getNodeChildrenUrl(folder_Id), userOneN1.getId(), reqBody.getBody(), null, reqBody.getContentType(), 201);
            Document txtDocument = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
            // Thumbnail generation has been disabled
            response = post(getNodeRenditionsUrl(txtDocument.getId()), userOneN1.getId(), toJsonAsString(renditionRequest), 501);
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
            thumbnailService.setThumbnailsEnabled(true);
        }
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

        // Create a folder within the site document's library
        String folderName = "folder" + System.currentTimeMillis();
        String folder_Id = addToDocumentLibrary(userOneN1Site, folderName, ContentModel.TYPE_FOLDER, userId);

        // Create multipart request - pdf file
        String renditionName = "doclib";
        String fileName = "quick.pdf";
        File file = getResourceFile(fileName);
        MultiPartRequest reqBody = MultiPartBuilder.create()
                .setFileData(new FileData(fileName, file, MimetypeMap.MIMETYPE_PDF))
                .setRenditions(Collections.singletonList(renditionName))
                .build();

        // Upload quick.pdf file into 'folder' - including request to create 'doclib' thumbnail
        HttpResponse response = post(getNodeChildrenUrl(folder_Id), userId, reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        String contentNodeId = document.getId();

        // wait and check that rendition is created ...
        Rendition rendition = waitAndGetRendition(userId, contentNodeId, renditionName);
        assertNotNull(rendition);
        assertEquals(RenditionStatus.CREATED, rendition.getStatus());

        // Create multipart request - Word doc file
        renditionName = "doclib";
        fileName = "quick.docx";
        file = getResourceFile(fileName);
        reqBody = MultiPartBuilder.create()
                .setFileData(new FileData(fileName, file, MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING))
                .setRenditions(Collections.singletonList(renditionName))
                .build();

        // Upload quick.txt file into 'folder' - including request to create 'doclib' thumbnail
        response = post(getNodeChildrenUrl(folder_Id), userId, reqBody.getBody(), null, reqBody.getContentType(), 201);
        document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        contentNodeId = document.getId();

        // wait and check that rendition is created ...
        rendition = waitAndGetRendition(userId, contentNodeId, renditionName);
        assertNotNull(rendition);
        assertEquals(RenditionStatus.CREATED, rendition.getStatus());

        /*
        // TODO open question (RA-834)
        // - should we accept for  JSON when creating empty file (ie. with zero-byte content)
        // - eg. might fail, eg. doclib for empty plain text / pdf ?
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

        // -ve - currently we do not support multiple rendition requests on create
        reqBody = MultiPartBuilder.create()
                .setFileData(new FileData(fileName, file, MimetypeMap.MIMETYPE_PDF))
                .setRenditions(Arrays.asList(new String[]{"doclib,imgpreview"}))
                .build();

        post(getNodeChildrenUrl(folder_Id), userId, reqBody.getBody(), null, reqBody.getContentType(), 400);

        // -ve
        reqBody = MultiPartBuilder.create()
                .setFileData(new FileData(fileName, file, MimetypeMap.MIMETYPE_PDF))
                .setRenditions(Arrays.asList(new String[]{"unknown"}))
                .build();

        post(getNodeChildrenUrl(folder_Id), userId, reqBody.getBody(), null, reqBody.getContentType(), 404);

        // -ve
        ThumbnailService thumbnailService = applicationContext.getBean("thumbnailService", ThumbnailService.class);
        thumbnailService.setThumbnailsEnabled(false);
        try
        {
            // Create multipart request
            String txtFileName = "quick-1.txt";
            File txtFile = getResourceFile(fileName);
            reqBody = MultiPartBuilder.create()
                    .setFileData(new FileData(txtFileName, txtFile, MimetypeMap.MIMETYPE_TEXT_PLAIN))
                    .setRenditions(Arrays.asList(new String[]{"doclib"}))
                    .build();

            post(getNodeChildrenUrl(folder_Id), userId, reqBody.getBody(), null, reqBody.getContentType(), 501);
        }
        finally
        {
            thumbnailService.setThumbnailsEnabled(true);
        }
    }

    /**
     * Tests download rendition.
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/renditions/<renditionId>/content}
     */
    @Test
    public void testDownloadRendition() throws Exception
    {
        // Create a folder within the site document's library
        String folderName = "folder" + System.currentTimeMillis();
        String folder_Id = addToDocumentLibrary(userOneN1Site, folderName, ContentModel.TYPE_FOLDER, userOneN1.getId());

        // Create multipart request
        String fileName = "quick.pdf";
        File file = getResourceFile(fileName);
        MultiPartBuilder multiPartBuilder = MultiPartBuilder.create()
                    .setFileData(new FileData(fileName, file, MimetypeMap.MIMETYPE_PDF));
        MultiPartRequest reqBody = multiPartBuilder.build();

        // Upload quick.pdf file into 'folder'
        HttpResponse response = post(getNodeChildrenUrl(folder_Id), userOneN1.getId(), reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        String contentNodeId = document.getId();

        // Get rendition (not created yet) information for node
        response = getSingle(getNodeRenditionsUrl(contentNodeId), userOneN1.getId(), "doclib", 200);
        Rendition rendition = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Rendition.class);
        assertNotNull(rendition);
        assertEquals(RenditionStatus.NOT_CREATED, rendition.getStatus());

        // Download placeholder - by default with Content-Disposition header
        Map<String, String> params = new HashMap<>();
        params.put("placeholder", "true");
        response = getSingle(getNodeRenditionsUrl(contentNodeId), userOneN1.getId(), ("doclib/content"), params, 200);
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
        response = getSingle(getNodeRenditionsUrl(contentNodeId), userOneN1.getId(), ("doclib/content"), params, 200);
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
        getSingle(getNodeRenditionsUrl(contentNodeId), userOneN1.getId(), "doclib/content", params, headers, 200);

        // Create and get 'doclib' rendition
        rendition = createAndGetRendition(userOneN1.getId(), contentNodeId, "doclib");
        assertNotNull(rendition);
        assertEquals(RenditionStatus.CREATED, rendition.getStatus());

        // Download rendition - by default with Content-Disposition header
        response = getSingle(getNodeRenditionsUrl(contentNodeId), userOneN1.getId(), "doclib/content", 200);
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
        response = getSingle(getNodeRenditionsUrl(contentNodeId), userOneN1.getId(), "doclib/content", params, 200);
        assertNotNull(response.getResponseAsBytes());
        responseHeaders = response.getHeaders();
        assertNotNull(responseHeaders);
        assertNull(responseHeaders.get("Content-Disposition"));
        contentType = responseHeaders.get("Content-Type");
        assertNotNull(contentType);
        assertTrue(contentType.startsWith(MimetypeMap.MIMETYPE_IMAGE_PNG));

        // Download rendition - with Content-Disposition header (attachment=true) same as default
        params = Collections.singletonMap("attachment", "true");
        response = getSingle(getNodeRenditionsUrl(contentNodeId), userOneN1.getId(), "doclib/content", params, 200);
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
        getSingle(getNodeRenditionsUrl(contentNodeId), userOneN1.getId(), "doclib/content", params, headers, 304);

        // Here we want to overwrite/update the existing content in order to force a new rendition creation,
        // so the ContentModel.PROP_MODIFIED date would be different. Hence, we use the multipart upload by providing
        // the old fileName and setting overwrite field to true
        file = getResourceFile("quick-2.pdf");
        multiPartBuilder = MultiPartBuilder.create()
                    .setFileData(new FileData(fileName, file, MimetypeMap.MIMETYPE_PDF))
                    .setOverwrite(true);
        reqBody = multiPartBuilder.build();

        // Update quick.pdf
        post(getNodeChildrenUrl(folder_Id), userOneN1.getId(), reqBody.getBody(), null, reqBody.getContentType(), 201);

        // The requested "If-Modified-Since" date is older than rendition modified date
        response = getSingleWithDelayRetry(getNodeRenditionsUrl(contentNodeId), userOneN1.getId(), "doclib/content", params, headers, MAX_RETRY,
                    PAUSE_TIME, 200);
        assertNotNull(response);
        responseHeaders = response.getHeaders();
        assertNotNull(responseHeaders);
        String newLastModifiedHeader = responseHeaders.get(LAST_MODIFIED_HEADER);
        assertNotNull(newLastModifiedHeader);
        assertNotEquals(lastModifiedHeader, newLastModifiedHeader);

        //-ve tests
        // nodeId in the path parameter does not represent a file
        getSingle(getNodeRenditionsUrl(folder_Id), userOneN1.getId(), "doclib/content", 400);

        // nodeId in the path parameter does not exist
        getSingle(getNodeRenditionsUrl(UUID.randomUUID().toString()), userOneN1.getId(), "doclib/content", 404);

        // renditionId in the path parameter is not registered/available
        getSingle(getNodeRenditionsUrl(contentNodeId), userOneN1.getId(), ("renditionId" + System.currentTimeMillis() + "/content"), 404);

        InputStream inputStream = new ByteArrayInputStream("The quick brown fox jumps over the lazy dog".getBytes());
        file = TempFileProvider.createTempFile(inputStream, "RenditionsTest-", ".abcdef");
        reqBody = MultiPartBuilder.create()
                    .setFileData(new FileData(file.getName(), file, MimetypeMap.MIMETYPE_TEXT_PLAIN))
                    .build();
        // Upload temp file into 'folder'
        response = post(getNodeChildrenUrl(folder_Id), userOneN1.getId(), reqBody.getBody(), null, reqBody.getContentType(), 201);
        document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        contentNodeId = document.getId();

        // The content of the rendition does not exist and the placeholder parameter is not present
        getSingle(getNodeRenditionsUrl(contentNodeId), userOneN1.getId(), "doclib/content", 404);

        // The content of the rendition does not exist and the placeholder parameter has a value of "false"
        params = Collections.singletonMap("placeholder", "false");
        getSingle(getNodeRenditionsUrl(contentNodeId), userOneN1.getId(), "doclib/content", params, 404);

        // The rendition does not exist, a placeholder is not available and the placeholder parameter has a value of "true"
        params = Collections.singletonMap("placeholder", "true");
        getSingle(getNodeRenditionsUrl(contentNodeId), userOneN1.getId(), ("renditionId" + System.currentTimeMillis() + "/content"), params, 404);

        // Create a node without any content
        String emptyContentNodeId = addToDocumentLibrary(userOneN1Site, "emptyDoc.txt", ContentModel.TYPE_CONTENT, userOneN1.getId());
        // The source node has no content
        getSingle(getNodeRenditionsUrl(emptyContentNodeId), userOneN1.getId(), "doclib/content", params, 400);

    }

    private String addToDocumentLibrary(final TestSite testSite, final String name, final QName type, String user)
    {
        return TenantUtil.runAsUserTenant(new TenantUtil.TenantRunAsWork<String>()
        {
            @Override
            public String doWork() throws Exception
            {
                return repoService.addToDocumentLibrary(testSite, name, type).getId();
            }
        }, user, testSite.getNetworkId());
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
}
