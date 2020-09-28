/*
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
package org.alfresco.rest;

import static org.alfresco.rest.api.tests.util.RestApiUtil.toJsonAsStringNonNull;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.NodeTargetAssoc;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.alfresco.rest.api.tests.client.data.ContentInfo;
import org.alfresco.rest.api.tests.client.data.Document;
import org.alfresco.rest.api.tests.client.data.Folder;
import org.alfresco.rest.api.tests.client.data.Node;
import org.alfresco.rest.api.tests.client.data.PathInfo;
import org.alfresco.rest.api.tests.client.data.Rendition;
import org.alfresco.rest.api.tests.util.MultiPartBuilder;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.rest.api.trashcan.TrashcanEntityResource;
import org.junit.After;
import org.junit.Test;
import org.springframework.extensions.webscripts.Status;

import com.google.common.collect.Ordering;

/**
 * V1 REST API tests for managing the user's Trashcan (ie. "deleted nodes")
 * 
 * Tests Deleting nodes and recovering
 *
 * @author gethin
 */
public class DeletedNodesTest extends AbstractSingleNetworkSiteTest
{

    protected static final String URL_DELETED_NODES = "deleted-nodes";
    private static final String URL_RENDITIONS = "renditions";

    private final static long DELAY_IN_MS = 500;
    
    @Override
    public void setup() throws Exception
    {
        super.setup();
    }

    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
    }
    
    /**
     * Tests getting deleted nodes
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/deleted-nodes/}
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/deleted-nodes/<nodeId>/}
     */
    @Test
    public void testCreateAndDelete() throws Exception
    {
        setRequestContext(user1);
        
        Date now = new Date();
        String folder1 = "folder-testCreateAndDelete-" + now.getTime() + "_1";
        Folder createdFolder = createFolder(tDocLibNodeId, folder1, null);
        assertNotNull(createdFolder);
        String f1Id = createdFolder.getId();

        //Create a folder outside a site
        Folder createdFolderNonSite = createFolder(Nodes.PATH_MY, folder1, null);
        assertNotNull(createdFolderNonSite);

        Document document = createEmptyTextFile(f1Id, "d1.txt");

        PublicApiClient.Paging paging = getPaging(0, 100);
        
        //First get any deleted nodes
        HttpResponse response = getAll(URL_DELETED_NODES, paging, 200);
        List<Node> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        assertNotNull(nodes);
        int numOfNodes = nodes.size();

        deleteNode(document.getId());
        deleteNode(createdFolder.getId());
        deleteNode(createdFolderNonSite.getId());

        response = getAll(URL_DELETED_NODES, paging, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        assertNotNull(nodes);
        assertEquals(numOfNodes+3,nodes.size());

        //The list is ordered with the most recently deleted node first
        checkDeletedNodes(now, createdFolder, createdFolderNonSite, document, nodes);

        // sanity check paging
        paging = getPaging(1, 1);
        response = getAll(URL_DELETED_NODES, paging, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        assertNotNull(nodes);
        assertEquals(1, nodes.size());
        PublicApiClient.ExpectedPaging expectedPaging = RestApiUtil.parsePaging(response.getJsonResponse());
        assertEquals(numOfNodes+3, expectedPaging.getTotalItems().intValue());
        assertEquals(1, expectedPaging.getCount().intValue());
        assertEquals(1, expectedPaging.getSkipCount().intValue());
        assertEquals(1, expectedPaging.getMaxItems().intValue());
        assertTrue(expectedPaging.getHasMoreItems().booleanValue());

        Map<String, String> params = Collections.singletonMap("include", "path");
        response = getSingle(URL_DELETED_NODES, document.getId(), params, 200);
        Document node = jacksonUtil.parseEntry(response.getJsonResponse(), Document.class);
        assertNotNull(node);
        assertEquals(user1, node.getArchivedByUser().getId());
        assertTrue(node.getArchivedAt().after(now));
        PathInfo path = node.getPath();
        assertNull("Path should be null because its parent has been deleted",path);
        assertNull("We don't show the parent id for a deleted node",node.getParentId());

        response = getSingle(URL_DELETED_NODES, createdFolder.getId(), params, 200);
        Folder fNode = jacksonUtil.parseEntry(response.getJsonResponse(), Folder.class);
        assertNotNull(fNode);
        assertEquals(user1, fNode.getArchivedByUser().getId());
        assertTrue(fNode.getArchivedAt().after(now));
        path = fNode.getPath();
        assertNotNull(path);
        assertEquals("/Company Home/Sites/"+tSiteId+"/documentLibrary", path.getName());
        assertTrue(path.getIsComplete());
        assertNull("We don't show the parent id for a deleted node",fNode.getParentId());

        response = getSingle(URL_DELETED_NODES, createdFolderNonSite.getId(), params, 200);
        fNode = jacksonUtil.parseEntry(response.getJsonResponse(), Folder.class);
        assertNotNull(fNode);
        assertEquals(user1, fNode.getArchivedByUser().getId());
        assertTrue(fNode.getArchivedAt().after(now));
        path = fNode.getPath();
        assertNotNull(path);
        assertEquals("/Company Home/User Homes/"+user1, path.getName());
        assertTrue(path.getIsComplete());

        //User 2 can't get it but user 1 can.
        setRequestContext(user2);
        getSingle(URL_DELETED_NODES, createdFolderNonSite.getId(), Status.STATUS_FORBIDDEN);

        setRequestContext(user1);
        
        //Invalid node ref
        getSingle(URL_DELETED_NODES, "iddontexist", 404);

        //Now as admin
        setRequestContext(networkAdmin);
        paging = getPaging(0, 100);
        response = publicApiClient.get(getScope(), URL_DELETED_NODES, null, null, null, createParams(paging, null));
        checkStatus(200, response.getStatusCode());
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        assertNotNull(nodes);
        checkDeletedNodes(now, createdFolder, createdFolderNonSite, document, nodes);
    }

    /**
     * Tests restoring deleted nodes
     * <p>post:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/deleted-nodes/<nodeId>/restore}
     */
    @Test
    public void testCreateAndRestore() throws Exception
    {
        setRequestContext(user1);

        Date now = new Date();
        String folder1 = "folder" + now.getTime() + "_1";
        Folder createdFolder = createFolder(tDocLibNodeId, folder1, null);
        assertNotNull(createdFolder);
        String f1Id = createdFolder.getId();

        // Create a folder outside a site
        Folder createdFolderNonSite = createFolder(Nodes.PATH_MY, folder1, null);
        assertNotNull(createdFolderNonSite);

        Document document = createEmptyTextFile(f1Id, "restoreme.txt");
        deleteNode(document.getId());

        // Create another document with the same name
        Document documentSameName = createEmptyTextFile(f1Id, "restoreme.txt");

        // Can't restore a node of the same name
        post(URL_DELETED_NODES + "/" + document.getId() + "/restore", null, null, Status.STATUS_CONFLICT);

        deleteNode(documentSameName.getId());

        // Now we can restore it.
        post(URL_DELETED_NODES + "/" + document.getId() + "/restore", null, null, 200);

        deleteNode(document.getId());

        // Create a new nodeTargetAssoc containing target id and association type
        NodeTargetAssoc nodeTargetAssoc = new NodeTargetAssoc();
        nodeTargetAssoc.setTargetParentId(f1Id);
        nodeTargetAssoc.setAssocType(ASSOC_TYPE_CM_CONTAINS);

        // restore to new location
        post(URL_DELETED_NODES + "/" + document.getId() + "/restore", toJsonAsStringNonNull(nodeTargetAssoc), null, 200);

        deleteNode(document.getId());
        // restore to nonexistent nodeId as the new location
        nodeTargetAssoc.setTargetParentId("nonexistentTargetNode");
        post(URL_DELETED_NODES + "/" + document.getId() + "/restore", toJsonAsStringNonNull(nodeTargetAssoc), null, 404);

        // restore to new location and using an invalid assocType
        nodeTargetAssoc.setTargetParentId(f1Id);
        nodeTargetAssoc.setAssocType("invalidAssociationType");
        post(URL_DELETED_NODES + "/" + document.getId() + "/restore", toJsonAsStringNonNull(nodeTargetAssoc), null, 400);

        // restore to new location without adding an association type
        nodeTargetAssoc.setTargetParentId(f1Id);
        nodeTargetAssoc.setAssocType(null);
        post(URL_DELETED_NODES + "/" + document.getId() + "/restore", toJsonAsStringNonNull(nodeTargetAssoc), null, 400);

        // create an folder as an admin
        setRequestContext(networkAdmin);

        String folderAdmin = "adminsFolder" + now.getTime() + "_1";
        Folder adminCreatedFolder = createFolder(Nodes.PATH_MY, folderAdmin, null);
        assertNotNull(adminCreatedFolder);
        String adminf1Id = adminCreatedFolder.getId();

        // switch context, re-delete the document and try to restore it to a
        // folder user1 does not have permissions to
        setRequestContext(user1);
        nodeTargetAssoc.setTargetParentId(adminf1Id);
        nodeTargetAssoc.setAssocType(ASSOC_TYPE_CM_CONTAINS);
        post(URL_DELETED_NODES + "/" + document.getId() + "/restore", toJsonAsStringNonNull(nodeTargetAssoc), null, 403);
        
        deleteNode(createdFolder.getId());

        // We deleted the parent folder so lets see if we can restore a child
        // doc, hopefully not.
        post(URL_DELETED_NODES + "/" + documentSameName.getId() + "/restore", null, null, Status.STATUS_NOT_FOUND);

        // Can't delete "nonsense" noderef
        post("deleted-nodes/nonsense/restore", null, null, Status.STATUS_NOT_FOUND);

        // User 2 can't restore it but user 1 can.
        setRequestContext(user2);
        post(URL_DELETED_NODES + "/" + createdFolder.getId() + "/restore", null, null, Status.STATUS_FORBIDDEN);
        setRequestContext(user1);
        post(URL_DELETED_NODES + "/" + createdFolder.getId() + "/restore", null, null, 200);
    }

    /**
     * Tests purging a deleted node
     * <p>delete:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/deleted-nodes/<nodeId>/}
     */
    @Test
    public void testCreateAndPurge() throws Exception
    {
        setRequestContext(user1);

        Date now = new Date();
        String folder1 = "folder" + now.getTime() + "_1";
        Folder createdFolder = createFolder(tDocLibNodeId, folder1, null);
        assertNotNull(createdFolder);

        deleteNode(createdFolder.getId());

        HttpResponse response = getSingle(URL_DELETED_NODES, createdFolder.getId(), 200);
        Folder fNode = jacksonUtil.parseEntry(response.getJsonResponse(), Folder.class);
        assertNotNull(fNode);

        //try purging "nonsense"
        delete(URL_DELETED_NODES, "nonsense", 404);

        //User 2 can't do it
        setRequestContext(user2);
        delete(URL_DELETED_NODES, createdFolder.getId(), Status.STATUS_FORBIDDEN);

        setRequestContext(user1);

        //Now purge the folder
        delete(URL_DELETED_NODES, createdFolder.getId(), 204);

        //This time we can't find it.
        getSingle(URL_DELETED_NODES, createdFolder.getId(), 404);
    }

    /**
     * Tests download of file/content.
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/deleted-nodes/<nodeId>/content}
     */
    @Test
    public void testDownloadFileContent() throws Exception
    {
        setRequestContext(user1);

        // Use existing test file
        String fileName = "quick-1.txt";
        File file = getResourceFile(fileName);

        MultiPartBuilder multiPartBuilder = MultiPartBuilder.create().setFileData(new MultiPartBuilder.FileData(fileName, file));
        MultiPartBuilder.MultiPartRequest reqBody = multiPartBuilder.build();

        // Upload text content
        HttpResponse response = post(getNodeChildrenUrl(Nodes.PATH_MY), reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        String contentNodeId = document.getId();

        // Check the upload response
        assertEquals(fileName, document.getName());
        ContentInfo contentInfo = document.getContent();
        assertNotNull(contentInfo);
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, contentInfo.getMimeType());

        // move the node to Trashcan
        deleteNode(document.getId());

        // Download text content - by default with Content-Disposition header
        response = getSingle(TrashcanEntityResource.class, contentNodeId + "/content", null, 200);

        String textContent = response.getResponse();
        assertEquals("The quick brown fox jumps over the lazy dog", textContent);
        Map<String, String> responseHeaders = response.getHeaders();
        assertNotNull(responseHeaders);
        assertEquals("attachment; filename=\"quick-1.txt\"; filename*=UTF-8''quick-1.txt", responseHeaders.get("Content-Disposition"));
        String cacheControl = responseHeaders.get("Cache-Control");
        assertNotNull(cacheControl);
        assertTrue(cacheControl.contains("must-revalidate"));
        assertTrue(cacheControl.contains("max-age=0"));
        assertNotNull(responseHeaders.get("Expires"));
        String lastModifiedHeader = responseHeaders.get(LAST_MODIFIED_HEADER);
        assertNotNull(lastModifiedHeader);
        Map<String, String> headers = Collections.singletonMap(IF_MODIFIED_SINCE_HEADER, lastModifiedHeader);
        // Test 304 response
        getSingle(URL_DELETED_NODES + "/" + contentNodeId + "/content", null, null, headers, 304);

        // Use existing pdf test file
        fileName = "quick.pdf";
        file = getResourceFile(fileName);
        byte[] originalBytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));

        multiPartBuilder = MultiPartBuilder.create().setFileData(new MultiPartBuilder.FileData(fileName, file));
        reqBody = multiPartBuilder.build();

        // Upload binary content
        response = post(getNodeChildrenUrl(Nodes.PATH_MY), reqBody.getBody(), null, reqBody.getContentType(), 201);
        document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        // move the node to Trashcan
        deleteNode(document.getId());
        contentNodeId = document.getId();

        // Check the upload response
        assertEquals(fileName, document.getName());
        contentInfo = document.getContent();
        assertNotNull(contentInfo);
        assertEquals(MimetypeMap.MIMETYPE_PDF, contentInfo.getMimeType());

        // Download binary content (as bytes) - without Content-Disposition
        // header (attachment=false)
        Map<String, String> params = new LinkedHashMap<>();
        params.put("attachment", "false");

        response = getSingle(TrashcanEntityResource.class, contentNodeId + "/content", params, 200);
        byte[] bytes = response.getResponseAsBytes();
        assertArrayEquals(originalBytes, bytes);

        responseHeaders = response.getHeaders();
        assertNotNull(responseHeaders);
        assertNull(responseHeaders.get("Content-Disposition"));
        assertNotNull(responseHeaders.get("Cache-Control"));
        assertNotNull(responseHeaders.get("Expires"));
        lastModifiedHeader = responseHeaders.get(LAST_MODIFIED_HEADER);
        assertNotNull(lastModifiedHeader);
        headers = Collections.singletonMap(IF_MODIFIED_SINCE_HEADER, lastModifiedHeader);
        // Test 304 response
        getSingle(URL_DELETED_NODES + "/" + contentNodeId + "/content", null, null, headers, 304);

        // -ve - nodeId in the path parameter does not exist
        getSingle(TrashcanEntityResource.class, UUID.randomUUID().toString() + "/content", params, 404);

        // -ve test - Authentication failed
        setRequestContext(null);
        getSingle(TrashcanEntityResource.class, contentNodeId + "/content", params, 401);

        // -ve - Current user does not have permission for nodeId
        setRequestContext(user2);
        getSingle(TrashcanEntityResource.class, contentNodeId + "/content", params, 403);
    }

    /**
     * Test retrieve renditions for deleted nodes
     * <p>post:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/deleted-nodes/<nodeId>/renditions}
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/deleted-nodes/<nodeId>/rendition/<renditionId>}
     */
    @Test
    public void testListRenditions() throws Exception
    {
        setRequestContext(user1);

        Date now = new Date();
        String folder1 = "folder" + now.getTime() + "_1";
        Folder createdFolder = createFolder(tDocLibNodeId, folder1, null);
        assertNotNull(createdFolder);
        String f1Id = createdFolder.getId();

        // Create multipart request
        String fileName = "quick.pdf";
        File file = getResourceFile(fileName);
        MultiPartBuilder multiPartBuilder = MultiPartBuilder.create().setFileData(new MultiPartBuilder.FileData(fileName, file));
        MultiPartBuilder.MultiPartRequest reqBody = multiPartBuilder.build();

        // Upload quick.pdf file into the folder previously created
        HttpResponse response = post(getNodeChildrenUrl(f1Id), reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        String contentNodeId = document.getId();

        // create doclib rendition and move node to trashcan
        createAndGetRendition(contentNodeId, "doclib");
        deleteNode(contentNodeId);

        // List all renditions and check for results
        PublicApiClient.Paging paging = getPaging(0, 50);
        response = getAll(getDeletedNodeRenditionsUrl(contentNodeId), paging, 200);
        List<Rendition> renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        assertTrue(renditions.size() >= 3);

        // +ve test - get previously created 'doclib' rendition
        response = getSingle(getDeletedNodeRenditionsUrl(contentNodeId), "doclib", 200);
        Rendition doclibRendition = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Rendition.class);

        assertNotNull(doclibRendition);
        assertEquals(Rendition.RenditionStatus.CREATED, doclibRendition.getStatus());
        ContentInfo contentInfo = doclibRendition.getContent();
        assertNotNull(contentInfo);
        assertEquals(MimetypeMap.MIMETYPE_IMAGE_PNG, contentInfo.getMimeType());
        assertEquals("PNG Image", contentInfo.getMimeTypeName());
        assertNotNull(contentInfo.getEncoding());
        assertTrue(contentInfo.getSizeInBytes() > 0);

        //  +ve test - Add a filter on rendition 'status' and list only 'NOT_CREATED' renditions
        Map<String, String> params = new HashMap<>(1);
        params.put("where", "(status='NOT_CREATED')");
        response = getAll(getDeletedNodeRenditionsUrl(contentNodeId), paging, params, 200);
        renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        assertTrue(renditions.size() >= 2);

        //  +ve test - Add a filter on rendition 'status' and list only the CREATED renditions
        params.put("where", "(status='CREATED')");
        response = getAll(getDeletedNodeRenditionsUrl(contentNodeId), paging, params, 200);
        renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        assertEquals("Only 'doclib' rendition should be returned.", 1, renditions.size());

        // SkipCount=0,MaxItems=2
        paging = getPaging(0, 2);
        // List all available renditions
        response = getAll(getDeletedNodeRenditionsUrl(contentNodeId), paging, 200);
        renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        assertEquals(2, renditions.size());
        PublicApiClient.ExpectedPaging expectedPaging = RestApiUtil.parsePaging(response.getJsonResponse());
        assertEquals(2, expectedPaging.getCount().intValue());
        assertEquals(0, expectedPaging.getSkipCount().intValue());
        assertEquals(2, expectedPaging.getMaxItems().intValue());
        assertTrue(expectedPaging.getTotalItems() >= 3);
        assertTrue(expectedPaging.getHasMoreItems());

        // SkipCount=1,MaxItems=3
        paging = getPaging(1, 3);
        // List all available renditions
        response = getAll(getDeletedNodeRenditionsUrl(contentNodeId), paging, 200);
        renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        assertEquals(3, renditions.size());
        expectedPaging = RestApiUtil.parsePaging(response.getJsonResponse());
        assertEquals(3, expectedPaging.getCount().intValue());
        assertEquals(1, expectedPaging.getSkipCount().intValue());
        assertEquals(3, expectedPaging.getMaxItems().intValue());
        assertTrue(expectedPaging.getTotalItems() >= 3);

        // +ve test - Test returned renditions are ordered (natural sort order)
        response = getAll(getDeletedNodeRenditionsUrl(contentNodeId), paging, params, 200);
        renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        assertTrue(Ordering.natural().isOrdered(renditions));
        // Check again to make sure the ordering wasn't coincidental
        response = getAll(getDeletedNodeRenditionsUrl(contentNodeId), paging, params, 200);
        renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        assertTrue(Ordering.natural().isOrdered(renditions));
        
        // -ve - nodeId in the path parameter does not exist
        getAll(getDeletedNodeRenditionsUrl(UUID.randomUUID().toString()), paging, params, 404);

        // -ve test - Create an empty text file
        Document emptyDoc = createEmptyTextFile(f1Id, "d1.txt");
        getAll(getDeletedNodeRenditionsUrl(emptyDoc.getId()), paging, params, 404);

        // -ve - nodeId in the path parameter does not represent a file
        deleteNode(f1Id);
        getAll(getDeletedNodeRenditionsUrl(f1Id), paging, params, 400);
        
        // -ve - Invalid status value
        params.put("where", "(status='WRONG')");
        getAll(getDeletedNodeRenditionsUrl(contentNodeId), paging, params, 400);

        // -ve - Invalid filter (only 'status' is supported)
        params.put("where", "(id='doclib')");
        getAll(getDeletedNodeRenditionsUrl(contentNodeId), paging, params, 400);

        // -ve test - Authentication failed
        setRequestContext(null);
        getAll(getDeletedNodeRenditionsUrl(contentNodeId), paging, params, 401);

        // -ve - Current user does not have permission for nodeId
        setRequestContext(user2);
        getAll(getDeletedNodeRenditionsUrl(contentNodeId), paging, params, 403);
        
        // Test get single node rendition
        setRequestContext(user1);
        // -ve - nodeId in the path parameter does not exist
        getSingle(getDeletedNodeRenditionsUrl(UUID.randomUUID().toString()), "doclib", 404);

        // -ve - renditionId in the path parameter is not registered/available
        getSingle(getNodeRenditionsUrl(contentNodeId), ("renditionId" + System.currentTimeMillis()), 404);

        // -ve - nodeId in the path parameter does not represent a file
        getSingle(getDeletedNodeRenditionsUrl(f1Id), "doclib", 400);

        // -ve test - Authentication failed
        setRequestContext(null);
        getSingle(getDeletedNodeRenditionsUrl(contentNodeId), "doclib", 401);

        // -ve - Current user does not have permission for nodeId
        setRequestContext(user2);
        getSingle(getDeletedNodeRenditionsUrl(contentNodeId), "doclib", 403);
    }

    /**
     * Tests download rendition.
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/deleted-nodes/<nodeId>/renditions/<renditionId>/content}
     */
    @Test
    public void testDownloadRendition() throws Exception
    {
        setRequestContext(user1);

        // Create a folder within the site document's library
        Date now = new Date();
        String folder1 = "folder" + now.getTime() + "_1";
        Folder createdFolder = createFolder(tDocLibNodeId, folder1, null);
        assertNotNull(createdFolder);
        String f1Id = createdFolder.getId();

        // Create multipart request using an existing file
        String fileName = "quick.pdf";
        File file = getResourceFile(fileName);
        MultiPartBuilder multiPartBuilder = MultiPartBuilder.create().setFileData(new MultiPartBuilder.FileData(fileName, file));
        MultiPartBuilder.MultiPartRequest reqBody = multiPartBuilder.build();

        // Upload quick.pdf file into 'folder'
        HttpResponse response = post(getNodeChildrenUrl(f1Id), reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        String contentNodeId = document.getId();
        
        Rendition rendition = createAndGetRendition(contentNodeId, "doclib");
        assertNotNull(rendition);
        assertEquals(Rendition.RenditionStatus.CREATED, rendition.getStatus());
        deleteNode(contentNodeId);

        // Download rendition - by default with Content-Disposition header
        response = getSingle(getDeletedNodeRenditionsUrl(contentNodeId), "doclib/content", 200);
        assertNotNull(response.getResponseAsBytes());
        Map<String, String> responseHeaders = response.getHeaders();
        assertNotNull(responseHeaders);
        String contentDisposition = responseHeaders.get("Content-Disposition");
        assertNotNull(contentDisposition);
        assertTrue(contentDisposition.contains("filename=\"doclib\""));
        String contentType = responseHeaders.get("Content-Type");
        assertNotNull(contentType);
        assertTrue(contentType.startsWith(MimetypeMap.MIMETYPE_IMAGE_PNG));

        // Download rendition - without Content-Disposition header
        // (attachment=false)
        Map<String, String> params = new HashMap<>();
        params = Collections.singletonMap("attachment", "false");
        response = getSingle(getDeletedNodeRenditionsUrl(contentNodeId), "doclib/content", params, 200);
        assertNotNull(response.getResponseAsBytes());
        responseHeaders = response.getHeaders();
        assertNotNull(responseHeaders);
        assertNull(responseHeaders.get("Content-Disposition"));
        contentType = responseHeaders.get("Content-Type");
        assertNotNull(contentType);
        assertTrue(contentType.startsWith(MimetypeMap.MIMETYPE_IMAGE_PNG));

        // Download rendition - with Content-Disposition header
        // (attachment=true) same as default
        params = Collections.singletonMap("attachment", "true");
        response = getSingle(getDeletedNodeRenditionsUrl(contentNodeId), "doclib/content", params, 200);
        assertNotNull(response.getResponseAsBytes());
        responseHeaders = response.getHeaders();
        assertNotNull(responseHeaders);
        String cacheControl = responseHeaders.get("Cache-Control");
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
        String lastModifiedHeader = responseHeaders.get(LAST_MODIFIED_HEADER);
        assertNotNull(lastModifiedHeader);
        Map<String, String> headers = Collections.singletonMap(IF_MODIFIED_SINCE_HEADER, lastModifiedHeader);
        getSingle(getDeletedNodeRenditionsUrl(contentNodeId), "doclib/content", params, headers, 304);

        // -ve tests
        // nodeId in the path parameter does not represent a file
        deleteNode(f1Id);
        getSingle(getDeletedNodeRenditionsUrl(f1Id), "doclib/content", 400);

        // nodeId in the path parameter does not exist
        getSingle(getDeletedNodeRenditionsUrl(UUID.randomUUID().toString()), "doclib/content", 404);

        // renditionId in the path parameter is not registered/available
        getSingle(getDeletedNodeRenditionsUrl(contentNodeId), ("renditionId" + System.currentTimeMillis() + "/content"), 404);

        // The rendition does not exist, a placeholder is not available and the
        // placeholder parameter has a value of "true"
        params = Collections.singletonMap("placeholder", "true");
        getSingle(getDeletedNodeRenditionsUrl(contentNodeId), ("renditionId" + System.currentTimeMillis() + "/content"), params, 404);
    }

    /**
     *  Checks the deleted nodes are in the correct order.
     */
    protected void checkDeletedNodes(Date now, Folder createdFolder, Folder createdFolderNonSite, Document document, List<Node> nodes)
    {
        Node aNode = (Node) nodes.get(0);
        assertNotNull(aNode);
        assertEquals("This folder was deleted most recently", createdFolderNonSite.getId(), aNode.getId());
        assertEquals(user1, aNode.getArchivedByUser().getId());
        assertTrue(aNode.getArchivedAt().after(now));
        assertNull("We don't show the parent id for a deleted node",aNode.getParentId());

        Node folderNode = (Node) nodes.get(1);
        assertNotNull(folderNode);
        assertEquals(createdFolder.getId(), folderNode.getId());
        assertEquals(user1, folderNode.getArchivedByUser().getId());
        assertTrue(folderNode.getArchivedAt().after(now));
        assertTrue("This folder was deleted before the non-site folder", folderNode.getArchivedAt().before(aNode.getArchivedAt()));
        assertNull("We don't show the parent id for a deleted node",folderNode.getParentId());

        aNode = (Node) nodes.get(2);
        assertNotNull(aNode);
        assertEquals(document.getId(), aNode.getId());
        assertEquals(user1, aNode.getArchivedByUser().getId());
        assertTrue(aNode.getArchivedAt().after(now));
        assertNull("We don't show the parent id for a deleted node",aNode.getParentId());
    }

    private String getDeletedNodeRenditionsUrl(String nodeId)
    {
        return URL_DELETED_NODES + "/" + nodeId + "/" + URL_RENDITIONS;
    }

}
