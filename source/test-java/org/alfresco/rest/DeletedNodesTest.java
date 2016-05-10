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
package org.alfresco.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.tests.RepoService;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.rest.api.tests.client.data.Document;
import org.alfresco.rest.api.tests.client.data.Folder;
import org.alfresco.rest.api.tests.client.data.Node;
import org.alfresco.rest.api.tests.client.data.PathInfo;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.junit.Test;
import org.springframework.extensions.webscripts.Status;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Tests Deleting nodes and recovering
 *
 * @author gethin
 */
public class DeletedNodesTest extends AbstractSingleNetworkSiteTest
{

    protected static final String URL_DELETED_NODES = "deleted-nodes";
    private RepoService.TestPerson u2;

    @Override
    public void setup() throws Exception
    {
        super.setup();
        u2 = networkOne.createUser();
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
        publicApiClient.setRequestContext(new RequestContext(u1.getId()));
        Date now = new Date();
        String folder1 = "folder" + now.getTime() + "_1";
        Folder createdFolder = createFolder(u1.getId(), docLibNodeRef.getId(), folder1, null);
        assertNotNull(createdFolder);

        //Create a folder outside a site
        Folder createdFolderNonSite = createFolder(u1.getId(),  Nodes.PATH_MY, folder1, null);
        assertNotNull(createdFolderNonSite);

        Document document = createDocument(createdFolder, "d1.txt");

        PublicApiClient.Paging paging = getPaging(0, 5);
        //First get any deleted nodes
        HttpResponse response = getAll(URL_DELETED_NODES, u1.getId(), paging, 200);
        List<Node> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        assertNotNull(nodes);
        int numOfNodes = nodes.size();

        delete(URL_NODES, u1.getId(), document.getId(), 204);
        delete(URL_NODES, u1.getId(), createdFolder.getId(), 204);
        delete(URL_NODES, u1.getId(), createdFolderNonSite.getId(), 204);

        response = getAll(URL_DELETED_NODES, u1.getId(), paging, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        assertNotNull(nodes);
        assertEquals(numOfNodes+3,nodes.size());

        Map<String, String> params = Collections.singletonMap("include", "path");
        response = getSingle(URL_DELETED_NODES, u1.getId(), document.getId(), params, 200);
        Document node = jacksonUtil.parseEntry(response.getJsonResponse(), Document.class);
        assertNotNull(node);
        assertEquals(u1.getId(), node.getArchivedByUser().getId());
        assertTrue(node.getArchivedAt().after(now));
        PathInfo path = node.getPath();
        assertNull("Path should be null because its parent has been deleted",path);
        assertNull("We don't show the parent id for a deleted node",node.getParentId());

        response = getSingle(URL_DELETED_NODES, u1.getId(), createdFolder.getId(), params, 200);
        Folder fNode = jacksonUtil.parseEntry(response.getJsonResponse(), Folder.class);
        assertNotNull(fNode);
        assertEquals(u1.getId(), fNode.getArchivedByUser().getId());
        assertTrue(fNode.getArchivedAt().after(now));
        path = fNode.getPath();
        assertNotNull(path);
        assertEquals("/Company Home/Sites/"+tSite.getSiteId()+"/documentLibrary", path.getName());
        assertTrue(path.getIsComplete());
        assertNull("We don't show the parent id for a deleted node",fNode.getParentId());

        response = getSingle(URL_DELETED_NODES, u1.getId(), createdFolderNonSite.getId(), params, 200);
        fNode = jacksonUtil.parseEntry(response.getJsonResponse(), Folder.class);
        assertNotNull(fNode);
        assertEquals(u1.getId(), fNode.getArchivedByUser().getId());
        assertTrue(fNode.getArchivedAt().after(now));
        path = fNode.getPath();
        assertNotNull(path);
        assertEquals("/Company Home/User Homes/"+u1.getId(), path.getName());
        assertTrue(path.getIsComplete());

        //The list is ordered with the most recently deleted node first
        checkDeletedNodes(now, createdFolder, createdFolderNonSite, document, nodes);

        //User 2 can't get it but user 1 can.
        getSingle(URL_DELETED_NODES, u2.getId(), createdFolderNonSite.getId(), Status.STATUS_FORBIDDEN);

        //Invalid node ref
        getSingle(URL_DELETED_NODES, u1.getId(), "iddontexist", 404);

        //Now as admin
        publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), "admin@"+networkOne.getId(), "admin"));
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
        publicApiClient.setRequestContext(new RequestContext(u1.getId()));
        Date now = new Date();
        String folder1 = "folder" + now.getTime() + "_1";
        Folder createdFolder = createFolder(u1.getId(), docLibNodeRef.getId(), folder1, null);
        assertNotNull(createdFolder);

        //Create a folder outside a site
        Folder createdFolderNonSite = createFolder(u1.getId(), Nodes.PATH_MY, folder1, null);
        assertNotNull(createdFolderNonSite);

        Document document = createDocument(createdFolder, "restoreme.txt");
        delete(URL_NODES, u1.getId(), document.getId(), 204);
        //Create another document with the same name
        Document documentSameName = createDocument(createdFolder, "restoreme.txt");

        //Can't restore a node of the same name
        post(URL_DELETED_NODES+"/"+document.getId()+"/restore", u1.getId(), null, null, Status.STATUS_CONFLICT);

        delete(URL_NODES, u1.getId(), documentSameName.getId(), 204);

        //Now we can restore it.
        post(URL_DELETED_NODES+"/"+document.getId()+"/restore", u1.getId(), null, null, 201);

        delete(URL_NODES, u1.getId(), createdFolder.getId(), 204);

        //We deleted the parent folder so lets see if we can restore a child doc, hopefully not.
        post(URL_DELETED_NODES+"/"+documentSameName.getId()+"/restore", u1.getId(), null, null, Status.STATUS_NOT_FOUND);

        //Can't delete "nonsense" noderef
        post("deleted-nodes/nonsense/restore", u1.getId(), null, null, Status.STATUS_NOT_FOUND);

        //User 2 can't restore it but user 1 can.
        post(URL_DELETED_NODES+"/"+createdFolder.getId()+"/restore", u2.getId(), null, null, Status.STATUS_FORBIDDEN);
        post(URL_DELETED_NODES+"/"+createdFolder.getId()+"/restore", u1.getId(), null, null, 201);
    }

    /**
     * Tests purging a deleted node
     * <p>delete:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/deleted-nodes/<nodeId>/}
     */
    @Test
    public void testCreateAndPurge() throws Exception
    {
        publicApiClient.setRequestContext(new RequestContext(u1.getId()));
        Date now = new Date();
        String folder1 = "folder" + now.getTime() + "_1";
        Folder createdFolder = createFolder(u1.getId(), docLibNodeRef.getId(), folder1, null);
        assertNotNull(createdFolder);

        delete(URL_NODES, u1.getId(), createdFolder.getId(), 204);

        HttpResponse response = getSingle(URL_DELETED_NODES, u1.getId(), createdFolder.getId(), 200);
        Folder fNode = jacksonUtil.parseEntry(response.getJsonResponse(), Folder.class);
        assertNotNull(fNode);

        //try purging "nonsense"
        delete(URL_DELETED_NODES, u1.getId(), "nonsense", 404);

        //User 2 can't do it
        delete(URL_DELETED_NODES, u2.getId(), createdFolder.getId(), Status.STATUS_FORBIDDEN);

        //Now purge the folder
        delete(URL_DELETED_NODES, u1.getId(), createdFolder.getId(), 204);

        //This time we can't find it.
        getSingle(URL_DELETED_NODES, u1.getId(), createdFolder.getId(), 404);
    }

    /**
     *  Checks the deleted nodes are in the correct order.
     */
    protected void checkDeletedNodes(Date now, Folder createdFolder, Folder createdFolderNonSite, Document document, List<Node> nodes)
    {
        Node aNode = (Node) nodes.get(0);
        assertNotNull(aNode);
        assertEquals("This folder was deleted most recently", createdFolderNonSite.getId(), aNode.getId());
        assertEquals(u1.getId(), aNode.getArchivedByUser().getId());
        assertTrue(aNode.getArchivedAt().after(now));
        assertNull("We don't show the parent id for a deleted node",aNode.getParentId());

        Node folderNode = (Node) nodes.get(1);
        assertNotNull(folderNode);
        assertEquals(createdFolder.getId(), folderNode.getId());
        assertEquals(u1.getId(), folderNode.getArchivedByUser().getId());
        assertTrue(folderNode.getArchivedAt().after(now));
        assertTrue("This folder was deleted before the non-site folder", folderNode.getArchivedAt().before(aNode.getArchivedAt()));
        assertNull("We don't show the parent id for a deleted node",folderNode.getParentId());

        aNode = (Node) nodes.get(2);
        assertNotNull(aNode);
        assertEquals(document.getId(), aNode.getId());
        assertEquals(u1.getId(), aNode.getArchivedByUser().getId());
        assertTrue(aNode.getArchivedAt().after(now));
        assertNull("We don't show the parent id for a deleted node",aNode.getParentId());
    }

}
