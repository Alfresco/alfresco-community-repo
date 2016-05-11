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
    private static final long PAUSE_TIME = 5000; //millisecond
    private static final int MAX_RETRY = 8;

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
        HttpResponse response = post("nodes/" + folder_Id + "/children", userOneN1.getId(), reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        String contentNodeId = document.getId();

        Paging paging = getPaging(0, 50);
        // List all available renditions (includes those that have been created and those that are yet to be created)
        response = getAll(getRenditionsUrl(contentNodeId), userOneN1.getId(), paging, 200);
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
        response = getAll(getRenditionsUrl(contentNodeId), userOneN1.getId(), paging, params, 200);
        renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        assertTrue(renditions.size() >= 5);

        params.put("where", "(status='CREATED')");
        // List only the CREATED renditions
        response = getAll(getRenditionsUrl(contentNodeId), userOneN1.getId(), paging, params, 200);
        renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        assertEquals("There is no rendition created yet.", 0, renditions.size());

        // Test paging
        // SkipCount=0,MaxItems=2
        paging = getPaging(0, 2);
        // List all available renditions
        response = getAll(getRenditionsUrl(contentNodeId), userOneN1.getId(), paging, 200);
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
        response = getAll(getRenditionsUrl(contentNodeId), userOneN1.getId(), paging, 200);
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
        response = getAll(getRenditionsUrl(contentNodeId), userOneN1.getId(), paging, 200);
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
        response = getAll(getRenditionsUrl(contentNodeId), userOneN1.getId(), paging, params, 200);
        renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        assertEquals("Should've only returned the 'doclib' rendition.", 1, renditions.size());

        params.put("where", "(status='NOT_CREATED')");
        // List only the NOT_CREATED renditions
        response = getAll(getRenditionsUrl(contentNodeId), userOneN1.getId(), paging, params, 200);
        renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        assertTrue(renditions.size() > 0);
        docLib = getRendition(renditions, "doclib");
        assertNull("'doclib' rendition has already been created.", docLib);

        // Test returned renditions are ordered (natural sort order)
        // List all renditions
        response = getAll(getRenditionsUrl(contentNodeId), userOneN1.getId(), paging, params, 200);
        renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        assertTrue(Ordering.natural().isOrdered(renditions));
        // Try again to make sure the ordering wasn't coincidental
        response = getAll(getRenditionsUrl(contentNodeId), userOneN1.getId(), paging, params, 200);
        renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        assertTrue(Ordering.natural().isOrdered(renditions));

        // nodeId in the path parameter does not represent a file
        getAll(getRenditionsUrl(folder_Id), userOneN1.getId(), paging, params, 400);

        // nodeId in the path parameter does not exist
        getAll(getRenditionsUrl(UUID.randomUUID().toString()), userOneN1.getId(), paging, params, 404);
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
        HttpResponse response = post("nodes/" + folder_Id + "/children", userOneN1.getId(), reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        String contentNodeId = document.getId();

        // Get rendition (not created yet) information for node
        response = getSingle(getRenditionsUrl(contentNodeId), userOneN1.getId(), "doclib", 200);
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
        getSingle(getRenditionsUrl(folder_Id), userOneN1.getId(), "doclib", 400);

        // nodeId in the path parameter does not exist
        getSingle(getRenditionsUrl(UUID.randomUUID().toString()), userOneN1.getId(), "doclib", 404);

        // renditionId in the path parameter is not registered/available
        getSingle(getRenditionsUrl(contentNodeId), userOneN1.getId(), ("renditionId" + System.currentTimeMillis()), 404);
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
        HttpResponse response = post("nodes/" + folder_Id + "/children", userOneN1.getId(), reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        String contentNodeId = document.getId();

        // Get rendition (not created yet) information for node
        response = getSingle(getRenditionsUrl(contentNodeId), userOneN1.getId(), "imgpreview", 200);
        Rendition rendition = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Rendition.class);
        assertNotNull(rendition);
        assertEquals(RenditionStatus.NOT_CREATED, rendition.getStatus());

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

        // -ve Tests
        // The rendition requested already exists
        post(getRenditionsUrl(folder_Id), userOneN1.getId(), toJsonAsString(new Rendition().setId("imgpreview")), 400);

        // Create 'doclib' rendition request
        Rendition renditionRequest = new Rendition().setId("doclib");
        // nodeId in the path parameter does not represent a file
        post(getRenditionsUrl(folder_Id), userOneN1.getId(), toJsonAsString(renditionRequest), 400);

        // nodeId in the path parameter does not exist
        post(getRenditionsUrl(UUID.randomUUID().toString()), userOneN1.getId(), toJsonAsString(renditionRequest), 404);

        // renditionId is not registered
        final String randomRenditionId = "renditionId" + System.currentTimeMillis();
        post(getRenditionsUrl(contentNodeId), userOneN1.getId(), toJsonAsString(new Rendition().setId(randomRenditionId)), 404);

        // Create a node without any content
        String emptyContentNodeId = addToDocumentLibrary(userOneN1Site, "emptyDoc.txt", ContentModel.TYPE_CONTENT, userOneN1.getId());
        // The source node has no content
        post(getRenditionsUrl(emptyContentNodeId), userOneN1.getId(), toJsonAsString(renditionRequest), 400);

        String content = "The quick brown fox jumps over the lazy dog.";
        file = TempFileProvider.createTempFile(new ByteArrayInputStream(content.getBytes()), getClass().getSimpleName(), ".bin");
        multiPartBuilder = MultiPartBuilder.create().setFileData(new FileData("binaryFileName", file, MimetypeMap.MIMETYPE_BINARY));
        reqBody = multiPartBuilder.build();
        response = post("nodes/" + folder_Id + "/children", userOneN1.getId(), reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document binaryDocument = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        // No transformer is currently available for 'application/octet-stream'
        post(getRenditionsUrl(binaryDocument.getId()), userOneN1.getId(), toJsonAsString(renditionRequest), 400);

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
            response = post("nodes/" + folder_Id + "/children", userOneN1.getId(), reqBody.getBody(), null, reqBody.getContentType(), 201);
            Document txtDocument = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
            // Thumbnail generation has been disabled
            post(getRenditionsUrl(txtDocument.getId()), userOneN1.getId(), toJsonAsString(renditionRequest), 501);
        }
        finally
        {
            thumbnailService.setThumbnailsEnabled(true);
        }
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

    private Rendition createAndGetRendition(String sourceNodeId, String renditionId) throws Exception
    {
        Rendition renditionRequest = new Rendition();
        renditionRequest.setId(renditionId);

        int retryCount = 0;
        while (retryCount < MAX_RETRY)
        {
            try
            {
                post(getRenditionsUrl(sourceNodeId), userOneN1.getId(), toJsonAsString(renditionRequest), 202);
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
                HttpResponse response = getSingle(getRenditionsUrl(sourceNodeId), userOneN1.getId(), renditionId, 200);
                Rendition rendition = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Rendition.class);
                assertNotNull(rendition);
                assertEquals(RenditionStatus.CREATED, rendition.getStatus());
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

    private String getRenditionsUrl(String nodeId)
    {
        return "nodes/" + nodeId + "/renditions";
    }

    @Override
    public String getScope()
    {
        return "public";
    }
}
