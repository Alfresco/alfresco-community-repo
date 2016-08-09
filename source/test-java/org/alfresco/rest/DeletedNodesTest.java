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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.alfresco.rest.api.tests.client.data.Document;
import org.alfresco.rest.api.tests.client.data.Folder;
import org.alfresco.rest.api.tests.client.data.Node;
import org.alfresco.rest.api.tests.client.data.PathInfo;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.junit.After;
import org.junit.Test;
import org.springframework.extensions.webscripts.Status;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

        //The list is ordered with the most recently deleted node first
        checkDeletedNodes(now, createdFolder, createdFolderNonSite, document, nodes);

        //User 2 can't get it but user 1 can.
        setRequestContext(user2);
        getSingle(URL_DELETED_NODES, createdFolderNonSite.getId(), Status.STATUS_FORBIDDEN);

        setRequestContext(user1);
        
        //Invalid node ref
        getSingle(URL_DELETED_NODES, "iddontexist", 404);

        //Now as admin
        setRequestContext(networkAdmin);
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

        //Create a folder outside a site
        Folder createdFolderNonSite = createFolder(Nodes.PATH_MY, folder1, null);
        assertNotNull(createdFolderNonSite);

        Document document = createEmptyTextFile(f1Id, "restoreme.txt");
        deleteNode(document.getId());
        
        //Create another document with the same name
        Document documentSameName = createEmptyTextFile(f1Id, "restoreme.txt");

        //Can't restore a node of the same name
        post(URL_DELETED_NODES+"/"+document.getId()+"/restore", null, null, Status.STATUS_CONFLICT);

        deleteNode(documentSameName.getId());

        //Now we can restore it.
        post(URL_DELETED_NODES+"/"+document.getId()+"/restore", null, null, 200);

        deleteNode(createdFolder.getId());

        //We deleted the parent folder so lets see if we can restore a child doc, hopefully not.
        post(URL_DELETED_NODES+"/"+documentSameName.getId()+"/restore", null, null, Status.STATUS_NOT_FOUND);

        //Can't delete "nonsense" noderef
        post("deleted-nodes/nonsense/restore", null, null, Status.STATUS_NOT_FOUND);

        //User 2 can't restore it but user 1 can.
        setRequestContext(user2);
        post(URL_DELETED_NODES+"/"+createdFolder.getId()+"/restore", null, null, Status.STATUS_FORBIDDEN);
        setRequestContext(user1);
        post(URL_DELETED_NODES+"/"+createdFolder.getId()+"/restore", null, null, 200);
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

}
