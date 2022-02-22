/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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

import static org.alfresco.rest.api.tests.util.RestApiUtil.parsePaging;
import static org.alfresco.rest.api.tests.util.RestApiUtil.toJsonAsStringNonNull;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentLimitProvider.SimpleFixedLimitProvider;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.rest.AbstractSingleNetworkSiteTest;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.LockInfo;
import org.alfresco.rest.api.model.ClassDefinition;
import org.alfresco.rest.api.model.ConstraintDefinition;
import org.alfresco.rest.api.model.NodePermissions;
import org.alfresco.rest.api.model.NodeTarget;
import org.alfresco.rest.api.model.PropertyDefinition;
import org.alfresco.rest.api.model.Site;
import org.alfresco.rest.api.nodes.NodesEntityResource;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.alfresco.rest.api.tests.client.PublicApiClient.ExpectedPaging;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.PublicApiHttpClient.BinaryPayload;
import org.alfresco.rest.api.tests.client.data.Association;
import org.alfresco.rest.api.tests.client.data.ContentInfo;
import org.alfresco.rest.api.tests.client.data.Document;
import org.alfresco.rest.api.tests.client.data.Folder;
import org.alfresco.rest.api.tests.client.data.Node;
import org.alfresco.rest.api.tests.client.data.PathInfo;
import org.alfresco.rest.api.tests.client.data.PathInfo.ElementInfo;
import org.alfresco.rest.api.tests.client.data.Rendition;
import org.alfresco.rest.api.tests.client.data.SiteRole;
import org.alfresco.rest.api.tests.client.data.UserInfo;
import org.alfresco.rest.api.tests.util.MultiPartBuilder;
import org.alfresco.rest.api.tests.util.MultiPartBuilder.FileData;
import org.alfresco.rest.api.tests.util.MultiPartBuilder.MultiPartRequest;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.GUID;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.collections.map.MultiValueMap;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * V1 REST API tests for Nodes (files, folders and custom node types)
 * 
 * <ul>
 * <li> {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>} </li>
 * <li> {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/children} </li>
 * </ul>
 *
 * TODO
 * - improve test 'fwk' to enable api tests to be run against remote repo (rather than embedded jetty)
 * - requires replacement of remaining non-remote calls with remote (preferably public) apis
 *   - eg. createUser (or any other usage of repoService), permissionService, AuthenticationUtil, ...
 *
 * @author Jamal Kaabi-Mofrad
 * @author janv
 */
public class NodeApiTest extends AbstractSingleNetworkSiteTest
{
    private static final String PROP_OWNER = "cm:owner";

    private static final String URL_DELETED_NODES = "deleted-nodes";
    private static final String EMPTY_BODY = "{}";

    protected PermissionService permissionService;
    protected AuthorityService authorityService;
    private NodeService nodeService;
    private NamespaceService namespaceService;


    private String rootGroupName = null;
    private String groupA = null;
    private String groupB = null;

    @Before
    public void setup() throws Exception
    {
        super.setup();

        permissionService = applicationContext.getBean("permissionService", PermissionService.class);
        authorityService = (AuthorityService) applicationContext.getBean("AuthorityService");
        nodeService = applicationContext.getBean("NodeService", NodeService.class);
        namespaceService= (NamespaceService) applicationContext.getBean("NamespaceService");
    }
    
    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
    }
    
    /**
     * Tests get document library children.
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/children}
     */
    @Test
    public void testListChildrenWithinSiteDocLib() throws Exception
    {
        setRequestContext(user1);

        // create folder f0
        String folder0Name = "f0-testListChildrenWithinSiteDocLib-"+RUNID;
        String f0Id = createFolder(tDocLibNodeId, folder0Name).getId();
        
        String folder1 = "folder" + RUNID + "_1";
        createFolder(f0Id, folder1, null).getId();

        String folder2 = "folder" + RUNID + "_2";
        createFolder(f0Id, folder2, null).getId();

        String content1 = "content" + RUNID + "_1";
        createTextFile(f0Id, content1, "The quick brown fox jumps over the lazy dog 1.").getId();

        String content2 = "content" + RUNID + "_2";
        createTextFile(f0Id, content2, "The quick brown fox jumps over the lazy dog 2.").getId();

        String forum1 = "forum" + RUNID + "_1";
        createNode(f0Id, forum1, "fm:topic", null);

        Paging paging = getPaging(0, 100);
        HttpResponse response = getAll(getNodeChildrenUrl(f0Id), paging, 200);
        List<Node> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        assertEquals(4, nodes.size()); // forum is part of the default ignored types
        // Paging
        ExpectedPaging expectedPaging = RestApiUtil.parsePaging(response.getJsonResponse());
        assertEquals(4, expectedPaging.getCount().intValue());
        assertEquals(0, expectedPaging.getSkipCount().intValue());
        assertEquals(100, expectedPaging.getMaxItems().intValue());
        assertFalse(expectedPaging.getHasMoreItems().booleanValue());

        // Order by folders and modified date first
        Map<String, String> orderBy = Collections.singletonMap("orderBy", "isFolder DESC,modifiedAt DESC");
        response = getAll(getNodeChildrenUrl(f0Id), paging, orderBy, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        assertEquals(4, nodes.size());
        assertEquals(folder2, nodes.get(0).getName());
        assertTrue(nodes.get(0).getIsFolder());
        assertFalse(nodes.get(0).getIsFile());
        assertEquals(folder1, nodes.get(1).getName());
        assertTrue(nodes.get(1).getIsFolder());
        assertFalse(nodes.get(1).getIsFile());
        assertEquals(content2, nodes.get(2).getName());
        assertFalse(nodes.get(2).getIsFolder());
        assertTrue(nodes.get(2).getIsFile());
        assertEquals(content1, nodes.get(3).getName());
        assertFalse(nodes.get(3).getIsFolder());
        assertTrue(nodes.get(3).getIsFile());

        // Order by folders last and modified date first
        orderBy = Collections.singletonMap("orderBy", "isFolder ASC,modifiedAt DESC");
        response = getAll(getNodeChildrenUrl(f0Id), paging, orderBy, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        assertEquals(4, nodes.size());
        assertEquals(content2, nodes.get(0).getName());
        assertEquals(content1, nodes.get(1).getName());
        assertEquals(folder2, nodes.get(2).getName());
        assertEquals(folder1, nodes.get(3).getName());

        // Order by folders and modified date last
        orderBy = Collections.singletonMap("orderBy", "isFolder,modifiedAt");
        response = getAll(getNodeChildrenUrl(f0Id), paging, orderBy, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        assertEquals(4, nodes.size());
        assertEquals(content1, nodes.get(0).getName());
        assertEquals(content2, nodes.get(1).getName());
        assertEquals(folder1, nodes.get(2).getName());
        assertEquals(folder2, nodes.get(3).getName());

        // Order by folders and modified date first
        orderBy = Collections.singletonMap("orderBy", "isFolder DESC,modifiedAt DESC");

        // SkipCount=0,MaxItems=2
        paging = getPaging(0, 2);
        response = getAll(getNodeChildrenUrl(f0Id), paging, orderBy, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        assertEquals(2, nodes.size());
        assertEquals(folder2, nodes.get(0).getName());
        assertEquals(folder1, nodes.get(1).getName());
        expectedPaging = RestApiUtil.parsePaging(response.getJsonResponse());
        assertEquals(2, expectedPaging.getCount().intValue());
        assertEquals(0, expectedPaging.getSkipCount().intValue());
        assertEquals(2, expectedPaging.getMaxItems().intValue());
        assertTrue(expectedPaging.getHasMoreItems().booleanValue());

        // SkipCount=null,MaxItems=2
        paging = getPaging(null, 2);
        response = getAll(getNodeChildrenUrl(f0Id), paging, orderBy, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        assertEquals(2, nodes.size());
        assertEquals(folder2, nodes.get(0).getName());
        assertEquals(folder1, nodes.get(1).getName());
        expectedPaging = RestApiUtil.parsePaging(response.getJsonResponse());
        assertEquals(2, expectedPaging.getCount().intValue());
        assertEquals(0, expectedPaging.getSkipCount().intValue());
        assertEquals(2, expectedPaging.getMaxItems().intValue());
        assertTrue(expectedPaging.getHasMoreItems().booleanValue());

        // SkipCount=2,MaxItems=4
        paging = getPaging(2, 4);
        response = getAll(getNodeChildrenUrl(f0Id), paging, orderBy, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        assertEquals(2, nodes.size());
        assertEquals(content2, nodes.get(0).getName());
        assertEquals(content1, nodes.get(1).getName());
        expectedPaging = RestApiUtil.parsePaging(response.getJsonResponse());
        assertEquals(2, expectedPaging.getCount().intValue());
        assertEquals(2, expectedPaging.getSkipCount().intValue());
        assertEquals(4, expectedPaging.getMaxItems().intValue());
        assertFalse(expectedPaging.getHasMoreItems().booleanValue());


        // SkipCount=2,MaxItems=null
        paging = getPaging(2, null);
        response = getAll(getNodeChildrenUrl(f0Id), paging, orderBy, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        assertEquals(2, nodes.size());
        assertEquals(content2, nodes.get(0).getName());
        assertEquals(content1, nodes.get(1).getName());
        expectedPaging = RestApiUtil.parsePaging(response.getJsonResponse());
        assertEquals(2, expectedPaging.getCount().intValue());
        assertEquals(2, expectedPaging.getSkipCount().intValue());
        assertEquals(100, expectedPaging.getMaxItems().intValue());
        assertFalse(expectedPaging.getHasMoreItems().booleanValue());

        setRequestContext(user2);

        // user2 tries to access user1's folder in a private docLib
        paging = getPaging(0, Integer.MAX_VALUE);
        getAll(getNodeChildrenUrl(f0Id), paging, 403);

        setRequestContext(user1);

        // -ve test - paging (via list children) cannot have skipCount < 0
        paging = getPaging(-1, 4);
        getAll(getNodeChildrenUrl(f0Id), paging, orderBy, 400);


        // -ve test - paging (via list children) cannot have maxItems < 1
        paging = getPaging(0, 0);
        getAll(getNodeChildrenUrl(f0Id), paging, orderBy, 400);
    }

    /**
     * Tests list children.
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/children}
     */
    @Test
    public void testListChildrenWithinMyFiles() throws Exception
    {
        setRequestContext(user1);
        
        String myNodeId = getMyNodeId();
        
        String rootChildrenUrl = getNodeChildrenUrl(Nodes.PATH_ROOT);

        String folder0Name = "folder " + RUNID + " 0";
        String folder0Id = createFolder(myNodeId, folder0Name, null).getId();

        String childrenUrl = getNodeChildrenUrl(folder0Id);

        Map<String, Object> props = new HashMap<>(1);
        props.put("cm:title", "This is folder 1");
        String folder1 = "folder " + RUNID + " 1";
        String folder1_Id = createFolder(folder0Id, folder1, props).getId();

        String contentF1 = "content" + RUNID + " in folder 1";
        String contentF1_Id = createTextFile(folder1_Id, contentF1, "The quick brown fox jumps over the lazy dog 1.").getId();

        props = new HashMap<>(1);
        props.put("cm:title", "This is folder 2");
        String folder2 = "folder " + RUNID + " 2";
        String folder2_Id = createFolder(folder0Id, folder2, props).getId();

        String contentF2 = "content" + RUNID + " in folder 2.txt";
        String contentF2_Id = createTextFile(folder2_Id, contentF2, "The quick brown fox jumps over the lazy dog 2.").getId();

        String content1 = "content" + RUNID + " 1.txt";
        String content1_Id = createTextFile(folder0Id, content1, "The quick brown fox jumps over the lazy dog.").getId();

        props = new HashMap<>();
        props.put(PROP_OWNER, user1);
        props.put("cm:lastThumbnailModification", Collections.singletonList("doclib:1444660852296"));

        Node nodeUpdate = new Node();
        nodeUpdate.setProperties(props);
        put(URL_NODES, content1_Id, toJsonAsStringNonNull(nodeUpdate), null, 200);

        List<String> folderIds = Arrays.asList(folder1_Id, folder2_Id);
        List<String> contentIds = Arrays.asList(content1_Id);
        
        Paging paging = getPaging(0, 100);
        HttpResponse response = getAll(childrenUrl, paging, 200);
        List<Document> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Document.class);
        assertEquals(3, nodes.size());

        // Order by folders and modified date first
        Map<String, String> orderBy = Collections.singletonMap("orderBy", "isFolder DESC,modifiedAt DESC");
        response = getAll(childrenUrl, paging, orderBy, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Document.class);
        assertEquals(3, nodes.size());
        assertEquals(folder2, nodes.get(0).getName());
        assertEquals(folder1, nodes.get(1).getName());
        Document node = nodes.get(2);
        assertEquals(content1, node.getName());
        assertEquals(TYPE_CM_CONTENT, node.getNodeType());
        assertEquals(content1_Id, node.getId());
        UserInfo createdByUser = node.getCreatedByUser();
        assertEquals(user1, createdByUser.getId());
        assertEquals(UserInfo.getTestDisplayName(user1), createdByUser.getDisplayName());
        UserInfo modifiedByUser = node.getModifiedByUser();
        assertEquals(user1, modifiedByUser.getId());
        assertEquals(UserInfo.getTestDisplayName(user1), modifiedByUser.getDisplayName());
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, node.getContent().getMimeType());
        assertNotNull(node.getContent().getMimeTypeName());
        assertNotNull(node.getContent().getEncoding());
        assertTrue(node.getContent().getSizeInBytes() > 0);

        // request without "include"
        Map<String, String> params = new HashMap<>();
        response = getAll(childrenUrl, paging, params, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Document.class);
        for (Node n : nodes)
        {
            assertNull("There shouldn't be a 'properties' object in the response.", n.getProperties());
            assertNull("There shouldn't be a 'isLink' object in the response.", n.getIsLink());
            assertNull("There shouldn't be a 'path' object in the response.", n.getPath());
            assertNull("There shouldn't be a 'aspectNames' object in the response.", n.getAspectNames());
        }

        // request with include - example 1
        params = new HashMap<>();
        params.put("include", "isLink");
        response = getAll(childrenUrl, paging, params, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Document.class);
        for (Node n : nodes)
        {
            assertNotNull("There should be a 'isLink' object in the response.", n.getIsLink());
        }

        // request with include - example 2
        params = new HashMap<>();
        params.put("include", "aspectNames,properties,path,isLink");
        response = getAll(childrenUrl, paging, params, 200);
        nodes = jacksonUtil.parseEntries(response.getJsonResponse(), Document.class);
        for (Node n : nodes)
        {
            assertNotNull("There should be a 'properties' object in the response.", n.getProperties()); // eg. cm:title, see above
            assertNotNull("There should be a 'isLink' object in the response.", n.getIsLink());
            assertNotNull("There should be a 'path' object in the response.", n.getPath());
            assertNotNull("There should be a 'aspectNames' object in the response.", n.getAspectNames());
        }

        // request specific property via include
        params = new HashMap<>();
        params.put("include", "cm:lastThumbnailModification");
        params.put("orderBy", "isFolder DESC,modifiedAt DESC");
        response = getAll(childrenUrl, paging, params, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Document.class);
        assertEquals(3, nodes.size());
        assertNull("There shouldn't be a 'properties' object in the response.", nodes.get(0).getProperties());
        assertNull("There shouldn't be a 'properties' object in the response.", nodes.get(1).getProperties());
        assertNotNull("There should be a 'properties' object in the response.", nodes.get(2).getProperties());
        Set<Entry<String, Object>> propsSet = nodes.get(2).getProperties().entrySet();
        assertEquals(1, propsSet.size());
        Entry<String, Object> entry = propsSet.iterator().next();
        assertEquals("cm:lastThumbnailModification", entry.getKey());
        assertEquals("doclib:1444660852296", ((List<?>) entry.getValue()).get(0));


        // filtering, via where clause - folders only
        params = new HashMap<>();
        params.put("where", "("+Nodes.PARAM_ISFOLDER+"=true)");
        response = getAll(childrenUrl, paging, params, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Document.class);
        assertEquals(2, nodes.size());

        assertTrue(nodes.get(0).getIsFolder());
        assertFalse(nodes.get(0).getIsFile());
        assertTrue(folderIds.contains(nodes.get(0).getId()));

        assertTrue(nodes.get(1).getIsFolder());
        assertFalse(nodes.get(1).getIsFile());
        assertTrue(folderIds.contains(nodes.get(1).getId()));

        // filtering, via where clause - content only
        params = new HashMap<>();
        params.put("where", "("+Nodes.PARAM_ISFILE+"=true)");
        response = getAll(childrenUrl, paging, params, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Document.class);
        assertEquals(1, nodes.size());
        assertFalse(nodes.get(0).getIsFolder());
        assertTrue(nodes.get(0).getIsFile());
        assertTrue(contentIds.contains(nodes.get(0).getId()));

        // list children via relativePath

        params = Collections.singletonMap(Nodes.PARAM_RELATIVE_PATH, folder1);
        response = getAll(childrenUrl, paging, params, 200);
        JSONObject jsonResponse = response.getJsonResponse();
        nodes = RestApiUtil.parseRestApiEntries(jsonResponse, Document.class);
        assertEquals(1, nodes.size());
        assertEquals(contentF1_Id, nodes.get(0).getId());

        JSONObject jsonList = (JSONObject)jsonResponse.get("list");
        assertNotNull(jsonList);
        JSONObject jsonSrcObj = (JSONObject)jsonResponse.get("source");
        assertNull(jsonSrcObj);

        params = Collections.singletonMap(Nodes.PARAM_RELATIVE_PATH, "User Homes/" + user1 + "/" + folder0Name + "/" + folder2);
        response = getAll(rootChildrenUrl, paging, params, 200);
        jsonResponse = response.getJsonResponse();
        nodes = RestApiUtil.parseRestApiEntries(jsonResponse, Document.class);
        assertEquals(1, nodes.size());
        assertEquals(contentF2_Id, nodes.get(0).getId());

        jsonList = (JSONObject)jsonResponse.get("list");
        assertNotNull(jsonList);
        jsonSrcObj = (JSONObject)jsonResponse.get("source");
        assertNull(jsonSrcObj);

        // list children via relativePath and also return the source entity

        params = new HashMap<>();
        params.put(Nodes.PARAM_RELATIVE_PATH, "User Homes/" + user1 + "/" + folder0Name + "/" + folder2);
        params.put("includeSource", "true");
        params.put("include", "path,isLink");
        params.put("fields", "id");
        response = getAll(rootChildrenUrl, paging, params, 200);
        jsonResponse = response.getJsonResponse();
        nodes = RestApiUtil.parseRestApiEntries(jsonResponse, Document.class);
        assertEquals(1, nodes.size());
        Document doc = nodes.get(0);
        assertEquals(contentF2_Id, doc.getId());
        assertNotNull(doc.getPath());
        assertEquals(Boolean.FALSE, doc.getIsLink());
        assertNull(doc.getName());

        jsonList = (JSONObject)jsonResponse.get("list");
        assertNotNull(jsonList);

        // source is not affected by include (or fields for that matter) - returns the default node response
        Folder src = RestApiUtil.parsePojo("source", jsonList, Folder.class);
        assertEquals(folder2_Id, src.getId());
        assertNull(src.getPath());
        assertNull(src.getIsLink());
        assertNotNull(src.getName());
        assertNotNull(src.getAspectNames());
        assertNotNull(src.getProperties());

        // -ve test - Invalid QName (Namespace prefix cm... is not mapped to a namespace URI) for the orderBy parameter.
        params = Collections.singletonMap("orderBy", Nodes.PARAM_ISFOLDER+" DESC,cm" + System.currentTimeMillis() + ":modified DESC");
        getAll(childrenUrl, paging, params, 400);

        paging = getPaging(0, 10);

        // -ve test - list folder children for unknown node should return 404
        getAll(getNodeChildrenUrl(UUID.randomUUID().toString()), paging, 404);

        // -ve test - user2 tries to access user1's home folder
        setRequestContext(user2);
        getAll(getNodeChildrenUrl(myNodeId), paging, 403);

        setRequestContext(user1);

        // -ve test - try to list children using relative path to unknown node
        params = Collections.singletonMap(Nodes.PARAM_RELATIVE_PATH, "User Homes/" + user1 + "/unknown");
        getAll(rootChildrenUrl, paging, params, 404);

        // -ve test - try to list children using relative path to node for which user does not have read permission (expect 404 instead of 403)
        params = Collections.singletonMap(Nodes.PARAM_RELATIVE_PATH, "User Homes/" + user2);
        getAll(rootChildrenUrl, paging, params, 404);

        // -ve test - list folder children with relative path to unknown node should return 400
        params = Collections.singletonMap(Nodes.PARAM_RELATIVE_PATH, "/unknown");
        getAll(getNodeChildrenUrl(content1_Id), paging, params, 400);
        
        // filtering, via where clause - negated comparison
        params = new HashMap<>();
        params.put("where", "(NOT "+Nodes.PARAM_ISFILE+"=true)");
        getAll(childrenUrl, paging, params, 400);
        
    }

    /**
     * Tests get node with path information.
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>?include=path}
     */
    @Test
    public void testGetPathElements_DocLib() throws Exception
    {
        setRequestContext(user1);

        // user1 creates a private site and adds user2 as a site consumer
        String site1Title = "site-testGetPathElements_DocLib-" + RUNID;
        String site1Id = createSite(site1Title, SiteVisibility.PRIVATE).getId();
        addSiteMember(site1Id, user2, SiteRole.SiteConsumer);
        
        String site1DocLibNodeId = getSiteContainerNodeId(site1Id, "documentLibrary");
        
        // /Company Home/Sites/RandomSite<timestamp>/documentLibrary/folder<timestamp>_A
        String folderA = "folder" + RUNID + "_A";
        String folderA_Id = createFolder(site1DocLibNodeId, folderA).getId();

        // /Company Home/Sites/RandomSite<timestamp>/documentLibrary/folder<timestamp>_A/folder<timestamp>_B
        String folderB = "folder" + RUNID + "_B";
        String folderB_Id = createFolder(folderA_Id, folderB).getId();
        NodeRef folderB_Ref = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, folderB_Id);

        // /Company Home/Sites/RandomSite<timestamp>/documentLibrary/folder<timestamp>_A/folder<timestamp>_B/folder<timestamp>_C
        String folderC = "folder" + RUNID + "_C";
        String folderC_Id = createFolder(folderB_Id, folderC).getId();
        NodeRef folderC_Ref = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, folderC_Id);

        // /Company Home/Sites/RandomSite<timestamp>/documentLibrary/folder<timestamp>_A/folder<timestamp>_B/folder<timestamp>_C/content<timestamp>
        String content = "content" + RUNID;
        String content1_Id = createTextFile(folderC_Id, content, "The quick brown fox jumps over the lazy dog.").getId();
        
        
        // TODO refactor with remote permission api calls (maybe use v0 until we have v1 ?)
        final String tenantDomain = (networkOne != null ? networkOne.getId() : TenantService.DEFAULT_DOMAIN);
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                return TenantUtil.runAsTenant(new TenantUtil.TenantRunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        // Revoke folderB inherited permissions
                        permissionService.setInheritParentPermissions(folderB_Ref, false);
                        // Grant user2 permission for folderC
                        permissionService.setPermission(folderC_Ref, user2, PermissionService.CONSUMER, true);
                        return null;
                    }
                }, tenantDomain);
            }
        }, user1);
        

        //...nodes/nodeId?include=path
        Map<String, String> params = Collections.singletonMap("include", "path");
        HttpResponse response = getSingle(NodesEntityResource.class, content1_Id, params, 200);
        Document node = jacksonUtil.parseEntry(response.getJsonResponse(), Document.class);
        PathInfo path = node.getPath();
        assertNotNull(path);
        assertTrue(path.getIsComplete());
        assertNotNull(path.getName());
        // the path should only include the parents (not the requested node)
        assertFalse(path.getName().endsWith(content));
        assertTrue(path.getName().startsWith("/Company Home"));
        List<ElementInfo> pathElements = path.getElements();
        assertEquals(7, pathElements.size());
        
        // Check path element names and types, and one or two random aspects.
        assertEquals("Company Home", pathElements.get(0).getName());
        assertEquals("cm:folder", pathElements.get(0).getNodeType());
        
        assertEquals("Sites", pathElements.get(1).getName());
        assertEquals("st:sites", pathElements.get(1).getNodeType());
        
        assertEquals(site1Id, pathElements.get(2).getName());
        assertEquals("st:site", pathElements.get(2).getNodeType());
        assertTrue(pathElements.get(2).getAspectNames().contains("cm:titled"));
        // Check that sys:* is filtered out - to be consistent with other aspect name lists
        // e.g. /nodes/{nodeId}/children?include=aspectNames
        assertFalse(pathElements.get(2).getAspectNames().contains("sys:undeletable"));
        assertFalse(pathElements.get(2).getAspectNames().contains("sys:unmovable"));

        assertEquals("documentLibrary", pathElements.get(3).getName());
        assertEquals("cm:folder", pathElements.get(3).getNodeType());
        assertTrue(pathElements.get(3).getAspectNames().contains("st:siteContainer"));
        
        assertEquals(folderA, pathElements.get(4).getName());
        assertEquals("cm:folder", pathElements.get(4).getNodeType());
        
        assertEquals(folderB, pathElements.get(5).getName());
        assertEquals("cm:folder", pathElements.get(5).getNodeType());

        assertEquals(folderC, pathElements.get(6).getName());
        assertEquals("cm:folder", pathElements.get(6).getNodeType());

        // Try the above tests with user2 (site consumer)
        setRequestContext(user2);
        
        response = getSingle(NodesEntityResource.class, content1_Id, params, 200);
        node = jacksonUtil.parseEntry(response.getJsonResponse(), Document.class);
        path = node.getPath();
        assertNotNull(path);
        assertFalse("The path is not complete as the user doesn't have permission to access the full path.", path.getIsComplete());
        assertNotNull(path.getName());
        // site consumer (user2) dose not have access to the folderB
        assertFalse("site consumer (user2) dose not have access to the folderB", path.getName().contains(folderB));
        assertFalse(path.getName().startsWith("/Company Home"));
        // Go up as far as they can, before getting access denied (i.e. "/folderC")
        assertTrue(path.getName().endsWith(folderC));
        pathElements = path.getElements();
        assertEquals(1, pathElements.size());
        assertEquals(folderC, pathElements.get(0).getName());

        // cleanup
        setRequestContext(user1);
        deleteSite(site1Id, true, 204);
    }

    /**
     * Tests get node information.
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>}
     */
    @Test
    public void testGetNodeInfo() throws Exception
    {
        setRequestContext(user1);

        HttpResponse response = getSingle(NodesEntityResource.class, Nodes.PATH_ROOT, null, 200);
        Node node = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        String rootNodeId = node.getId();

        response = getSingle(NodesEntityResource.class, Nodes.PATH_MY, null, 200);
        node = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        String myFilesNodeId = node.getId();
        assertNotNull(myFilesNodeId);
        assertEquals(user1, node.getName());
        assertTrue(node.getIsFolder());
        assertFalse(node.getIsFile());

        String userHomesId = node.getParentId();

        // /Company Home/User Homes/user<timestamp>/folder<timestamp>_A
        String folderA = "folder" + RUNID + "_A";
        String folderA_Id = createFolder(myFilesNodeId, folderA).getId();

        // /Company Home/User Homes/user<timestamp>/folder<timestamp>_A/folder<timestamp>_B
        String folderB = "folder" + RUNID + "_B";
        String folderB_Id = createFolder(folderA_Id, folderB).getId();

        // /Company Home/User Homes/user<timestamp>/folder<timestamp>_A/folder<timestamp>_B/content<timestamp>
        String title = "test title";
        Map<String,String> docProps = new HashMap<>();
        docProps.put("cm:title", title);
        docProps.put("cm:owner", user2);
        String contentName = "content " + RUNID + ".txt";
        String content1Id = createTextFile(folderB_Id, contentName, "The quick brown fox jumps over the lazy dog.", "UTF-8", docProps).getId();

        // TODO find a better solution to wait for the asynchronous metadata-extract/transform operation. E.g. awaitility
        Thread.sleep(3000);
        // get node info
        response = getSingle(NodesEntityResource.class, content1Id, null, 200);
        Document documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        String content_Id = documentResp.getId();

        // Expected result ...
        UserInfo expectedUser = new UserInfo(user1);

        Document d1 = new Document();
        d1.setId(content_Id);
        d1.setParentId(folderB_Id);
        d1.setName(contentName);
        d1.setNodeType(TYPE_CM_CONTENT);

        ContentInfo ciExpected = new ContentInfo();
        ciExpected.setMimeType("text/plain");
        ciExpected.setMimeTypeName("Plain Text");
        ciExpected.setSizeInBytes(44L);
        ciExpected.setEncoding("ISO-8859-1");

        d1.setContent(ciExpected);
        d1.setCreatedByUser(expectedUser);
        d1.setModifiedByUser(expectedUser);

        Map<String, Object> props = new HashMap<>();
        props.put("cm:title", title);
        props.put("cm:versionLabel", "1.0");
        props.put("cm:versionType", "MAJOR");
        props.put("cm:owner", new UserInfo(user2).toJSON());

        d1.setProperties(props);
        d1.setAspectNames(Arrays.asList("cm:auditable","cm:titled","cm:versionable","cm:author","cm:ownable"));

        // Note: Path is not part of the default info
        d1.expected(documentResp);

        // get node info + path
        //...nodes/nodeId?include=path
        Map<String, String> params = Collections.singletonMap("include", "path");
        response = getSingle(NodesEntityResource.class, content1Id, params, 200);
        documentResp = jacksonUtil.parseEntry(response.getJsonResponse(), Document.class);

        // Expected path ...
        // note: the pathInfo should only include the parents (not the requested node)
        List<ElementInfo> elements = new ArrayList<>(5);
        elements.add(new ElementInfo(rootNodeId, "Company Home"));
        elements.add(new ElementInfo(userHomesId, "User Homes"));
        elements.add(new ElementInfo(myFilesNodeId, user1));
        elements.add(new ElementInfo(folderA_Id, folderA));
        elements.add(new ElementInfo(folderB_Id, folderB));
        PathInfo expectedPath = new PathInfo("/Company Home/User Homes/"+user1+"/"+folderA+"/"+folderB, true, elements);
        d1.setPath(expectedPath);

        d1.expected(documentResp);

        // get node info via relativePath

        params = Collections.singletonMap(Nodes.PARAM_RELATIVE_PATH, "/"+folderA+"/"+folderB);
        response = getSingle(NodesEntityResource.class, Nodes.PATH_MY, params, 200);
        Folder folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertEquals(folderB_Id, folderResp.getId());

        params = Collections.singletonMap(Nodes.PARAM_RELATIVE_PATH, folderA+"/"+folderB+"/"+contentName);
        response = getSingle(NodesEntityResource.class, Nodes.PATH_MY, params, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertEquals(content_Id, documentResp.getId());

        // test path with utf-8 encoded param (eg. ¢ => )
        String folderC = "folder" + RUNID + " ¢";
        String folderC_Id = createFolder(folderB_Id, folderC).getId();

        params = Collections.singletonMap(Nodes.PARAM_RELATIVE_PATH, "/"+folderA+"/"+folderB+"/"+folderC);
        response = getSingle(NodesEntityResource.class, Nodes.PATH_MY, params, 200);
        folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertEquals(folderC_Id, folderResp.getId());

        // -ve test - get info for unknown node should return 404
        getSingle(NodesEntityResource.class, UUID.randomUUID().toString(), null, 404);

        // -ve test - user2 tries to get node info about user1's home folder
        setRequestContext(user2);
        getSingle(NodesEntityResource.class, myFilesNodeId, null, 403);

        setRequestContext(user1);

        // -ve test - try to get node info using relative path to unknown node
        params = Collections.singletonMap(Nodes.PARAM_RELATIVE_PATH, folderA+"/unknown");
        getSingle(NodesEntityResource.class, Nodes.PATH_MY, params, 404);

        // -ve test - try to get node info using relative path to node for which user does not have read permission (expect 404 instead of 403)
        params = Collections.singletonMap(Nodes.PARAM_RELATIVE_PATH, "User Homes/"+user2);
        getSingle(NodesEntityResource.class, Nodes.PATH_ROOT, params, 404);

        // -ve test - attempt to get node info for non-folder node with relative path should return 400
        params = Collections.singletonMap(Nodes.PARAM_RELATIVE_PATH, "/unknown");
        getSingle(NodesEntityResource.class, content_Id, params, 400);
    }

    /**
     * Tests well-known aliases.
     * <p>GET:</p>
     * <ul>
     * <li> {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/-root-}</li>
     * <li> {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/-my-} </li>
     * <li> {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/-shared-} </li>
     * </ul>
     */
    @Test
    public void testGetNodeWithKnownAlias() throws Exception
    {
        setRequestContext(user1);
        
        HttpResponse response = getSingle(NodesEntityResource.class, Nodes.PATH_ROOT, null, 200);
        Node node = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertEquals("Company Home", node.getName());
        assertNotNull(node.getId());
        assertNull(node.getPath());

        // unknown alias
        getSingle(NodesEntityResource.class, "testSomeUndefinedAlias", null, 404);

        response = getSingle(NodesEntityResource.class, Nodes.PATH_MY, null, 200);
        node = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        String myFilesNodeId = node.getId();
        assertNotNull(myFilesNodeId);
        assertEquals(user1, node.getName());
        assertTrue(node.getIsFolder());
        assertFalse(node.getIsFile());
        assertNull(node.getPath()); // note: path can be optionally "include"'ed - see separate test

        response = getSingle(NodesEntityResource.class, Nodes.PATH_SHARED, null, 200);
        node = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        String sharedFilesNodeId = node.getId();
        assertNotNull(sharedFilesNodeId);
        assertEquals("Shared", node.getName());
        assertTrue(node.getIsFolder());
        assertFalse(node.getIsFile());
        assertNull(node.getPath());

        //Delete user1's home
        setRequestContext(networkAdmin);
        deleteNode(myFilesNodeId);
        
        setRequestContext(user1);
        getSingle(NodesEntityResource.class, Nodes.PATH_MY, null, 404); // Not found
    }

    /**
     * Tests guess mimetype & guess encoding when uploading file/content (create or update) - also empty file/content
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/children}
     * <p>PUT:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/content}
     */
    @Test
    public void testGuessMimeTypeAndEncoding() throws Exception
    {
        setRequestContext(user1);
        
        String fId = createFolder(getMyNodeId(), "test-folder-guess-"+RUNID).getId();

        // create empty files

        Document d = new Document();
        d.setName("my doc");
        d.setNodeType(TYPE_CM_CONTENT);

        HttpResponse response = post(getNodeChildrenUrl(fId), toJsonAsStringNonNull(d), 201);
        Document documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertEquals(MimetypeMap.MIMETYPE_BINARY, documentResp.getContent().getMimeType());
        assertEquals("Binary File (Octet Stream)", documentResp.getContent().getMimeTypeName());
        assertEquals(0L, documentResp.getContent().getSizeInBytes().longValue());
        assertEquals("UTF-8",  documentResp.getContent().getEncoding());

        d = new Document();
        d.setName("my doc.txt");
        d.setNodeType(TYPE_CM_CONTENT);

        response = post(getNodeChildrenUrl(fId), toJsonAsStringNonNull(d), 201);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, documentResp.getContent().getMimeType());
        assertEquals("Plain Text", documentResp.getContent().getMimeTypeName());
        assertEquals(0L, documentResp.getContent().getSizeInBytes().longValue());
        assertEquals("UTF-8",  documentResp.getContent().getEncoding());

        d = new Document();
        d.setName("my doc.pdf");
        d.setNodeType(TYPE_CM_CONTENT);

        response = post(getNodeChildrenUrl(fId), toJsonAsStringNonNull(d), 201);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertEquals(MimetypeMap.MIMETYPE_PDF, documentResp.getContent().getMimeType());
        assertEquals("Adobe PDF Document", documentResp.getContent().getMimeTypeName());
        assertEquals(0L, documentResp.getContent().getSizeInBytes().longValue());
        assertEquals("UTF-8",  documentResp.getContent().getEncoding());

        // upload files

        String fileName = "quick-2.pdf";
        File file = getResourceFile(fileName);

        MultiPartBuilder multiPartBuilder = MultiPartBuilder.create()
                .setFileData(new FileData(fileName, file));
        MultiPartRequest reqBody = multiPartBuilder.build();

        response = post(getNodeChildrenUrl(fId), reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        ContentInfo contentInfo = document.getContent();
        assertEquals(MimetypeMap.MIMETYPE_PDF, contentInfo.getMimeType());
        assertEquals("UTF-8", contentInfo.getEncoding());

        fileName = "quick-2.pdf";
        file = getResourceFile(fileName);

        multiPartBuilder = MultiPartBuilder.create()
                .setFileData(new FileData("quick-2", file)); // note: we've deliberately dropped the file ext here
        reqBody = multiPartBuilder.build();

        response = post(getNodeChildrenUrl(fId), reqBody.getBody(), null, reqBody.getContentType(), 201);
        document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        contentInfo = document.getContent();
        assertEquals(MimetypeMap.MIMETYPE_PDF, contentInfo.getMimeType());
        assertEquals("UTF-8", contentInfo.getEncoding());

        fileName = "example-1.txt";
        file = getResourceFile(fileName);

        multiPartBuilder = MultiPartBuilder.create()
                .setFileData(new FileData(fileName, file));
        reqBody = multiPartBuilder.build();

        response = post(getNodeChildrenUrl(fId), reqBody.getBody(), null, reqBody.getContentType(), 201);
        document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        contentInfo = document.getContent();
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, contentInfo.getMimeType());
        assertEquals("ISO-8859-1", contentInfo.getEncoding());

        fileName = "example-2.txt";
        file = getResourceFile(fileName);

        multiPartBuilder = MultiPartBuilder.create()
                .setFileData(new FileData(fileName, file));
        reqBody = multiPartBuilder.build();

        response = post(getNodeChildrenUrl(fId), reqBody.getBody(), null, reqBody.getContentType(), 201);
        document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        contentInfo = document.getContent();
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, contentInfo.getMimeType());
        assertEquals("UTF-8", contentInfo.getEncoding());

        fileName = "shift-jis.txt";
        file = getResourceFile(fileName);

        multiPartBuilder = MultiPartBuilder.create()
                .setFileData(new FileData(fileName, file));
        reqBody = multiPartBuilder.build();

        response = post(getNodeChildrenUrl(fId), reqBody.getBody(), null, reqBody.getContentType(), 201);
        document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        contentInfo = document.getContent();
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, contentInfo.getMimeType());
        assertEquals("Shift_JIS", contentInfo.getEncoding());

        fileName = "example-1.xml";
        file = getResourceFile(fileName);

        multiPartBuilder = MultiPartBuilder.create()
                .setFileData(new FileData(fileName, file));
        reqBody = multiPartBuilder.build();

        response = post(getNodeChildrenUrl(fId), reqBody.getBody(), null, reqBody.getContentType(), 201);
        document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        contentInfo = document.getContent();
        assertEquals(MimetypeMap.MIMETYPE_XML, contentInfo.getMimeType());
        assertEquals("ISO-8859-1", contentInfo.getEncoding());

        fileName = "example-2.xml";
        file = getResourceFile(fileName);

        multiPartBuilder = MultiPartBuilder.create()
                .setFileData(new FileData(fileName, file));
        reqBody = multiPartBuilder.build();

        response = post(getNodeChildrenUrl(fId), reqBody.getBody(), null, reqBody.getContentType(), 201);
        document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        contentInfo = document.getContent();
        assertEquals(MimetypeMap.MIMETYPE_XML, contentInfo.getMimeType());
        assertEquals("UTF-8", contentInfo.getEncoding());

        // upload file, rename and then update file

        fileName = "quick.pdf";
        file = getResourceFile(fileName);

        multiPartBuilder = MultiPartBuilder.create()
                .setFileData(new FileData(fileName, file));
        reqBody = multiPartBuilder.build();

        response = post(getNodeChildrenUrl(fId), reqBody.getBody(), null, reqBody.getContentType(), 201);
        document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        String docId = document.getId();
        contentInfo = document.getContent();
        assertEquals(MimetypeMap.MIMETYPE_PDF, contentInfo.getMimeType());
        assertEquals("UTF-8", contentInfo.getEncoding());

        // rename (mimeType remains unchanged, binary has not changed)
        Document dUpdate = new Document();
        dUpdate.setName("quick.docx");

        response = put(URL_NODES, docId, toJsonAsStringNonNull(dUpdate), null, 200);
        document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        contentInfo = document.getContent();
        assertEquals(MimetypeMap.MIMETYPE_PDF, contentInfo.getMimeType());
        assertEquals("UTF-8", contentInfo.getEncoding());

        fileName = "quick.docx";
        file = getResourceFile(fileName);
        BinaryPayload payload = new BinaryPayload(file);

        response = putBinary(getNodeContentUrl(docId), payload, null, null, 200);
        document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        contentInfo = document.getContent();
        assertEquals(MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING, contentInfo.getMimeType());
        assertEquals("UTF-8", contentInfo.getEncoding());
        
        // additional test
        fileName = "CMIS-Delete.json";
        file = getResourceFile(fileName);

        multiPartBuilder = MultiPartBuilder.create()
                .setFileData(new FileData("special-"+GUID.generate(), file));
        reqBody = multiPartBuilder.build();

        response = post(getNodeChildrenUrl(fId), reqBody.getBody(), null, reqBody.getContentType(), 201);
        document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        contentInfo = document.getContent();
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, contentInfo.getMimeType());
        assertEquals("UTF-8", contentInfo.getEncoding());

        // cleanup
        deleteNode(fId);
    }

    /**
     * Tests Multipart upload to user's home (a.k.a My Files).
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/children}
     */
    @Test
    public void testUploadToMyFiles() throws Exception
    {
        setRequestContext(user1);

        // create folder f0
        String folder0Name = "f0-testUploadToMyFiles-"+RUNID;
        Folder folderResp = createFolder(Nodes.PATH_MY, folder0Name);
        String f0Id = folderResp.getId();
        
        final String fileName = "quick.pdf";
        final File file = getResourceFile(fileName);

        Paging paging = getPaging(0, Integer.MAX_VALUE);
        HttpResponse response = getAll(getNodeChildrenUrl(f0Id), paging, 200);
        PublicApiClient.ExpectedPaging pagingResult = parsePaging(response.getJsonResponse());
        assertNotNull(paging);
        final int numOfNodes = pagingResult.getCount();

        MultiPartBuilder multiPartBuilder = MultiPartBuilder.create()
                    .setFileData(new FileData(fileName, file));
        MultiPartRequest reqBody = multiPartBuilder.build();

        // Try to upload into a non-existent folder
        post(getNodeChildrenUrl(UUID.randomUUID().toString()), reqBody.getBody(), null, reqBody.getContentType(), 404);

        // Upload
        response = post(getNodeChildrenUrl(f0Id), reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        // Check the upload response
        assertEquals(fileName, document.getName());
        ContentInfo contentInfo = document.getContent();
        assertNotNull(contentInfo);
        assertEquals(MimetypeMap.MIMETYPE_PDF, contentInfo.getMimeType());
        // Default encoding
        assertEquals("UTF-8", contentInfo.getEncoding());
        // Check there is no path info returned.
        // The path info should only be returned when it is requested via a include statement.
        assertNull(document.getPath());

        // Retrieve the uploaded file
        response = getSingle(NodesEntityResource.class, document.getId(), null, 200);
        document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertEquals(fileName, document.getName());
        contentInfo = document.getContent();
        assertNotNull(contentInfo);
        assertEquals(MimetypeMap.MIMETYPE_PDF, contentInfo.getMimeType());

        // Check 'get children' is confirming the upload
        response = getAll(getNodeChildrenUrl(f0Id), paging, 200);
        pagingResult = parsePaging(response.getJsonResponse());
        assertNotNull(paging);
        assertEquals(numOfNodes + 1, pagingResult.getCount().intValue());

        // Upload the same file again to check the name conflicts handling
        post(getNodeChildrenUrl(f0Id), reqBody.getBody(), null, reqBody.getContentType(), 409);

        response = getAll(getNodeChildrenUrl(f0Id), paging, 200);
        pagingResult = parsePaging(response.getJsonResponse());
        assertNotNull(paging);
        assertEquals("Duplicate file name. The file shouldn't have been uploaded.", numOfNodes + 1, pagingResult.getCount().intValue());

        // Set autoRename=true and upload the same file again
        reqBody = MultiPartBuilder.copy(multiPartBuilder)
                          .setAutoRename(true)
                          .build();

        response = post(getNodeChildrenUrl(f0Id), reqBody.getBody(), null, reqBody.getContentType(), 201);
        document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        // Check the upload response
        assertEquals("quick-1.pdf", document.getName());

        // upload the same file again, and request the path info to be present in the response
        response = post(getNodeChildrenUrl(f0Id), reqBody.getBody(), "?include=path", reqBody.getContentType(), 201);
        document = jacksonUtil.parseEntry(response.getJsonResponse(), Document.class);
        // Check the upload response
        assertEquals("quick-2.pdf", document.getName());
        assertNotNull(document.getPath());

        response = getAll(getNodeChildrenUrl(f0Id), paging, 200);
        pagingResult = parsePaging(response.getJsonResponse());
        assertNotNull(paging);
        assertEquals(numOfNodes + 3, pagingResult.getCount().intValue());

        // upload without specifying content type or without overriding filename - hence guess mimetype and use file's name
        final String fileName1 = "quick-1.txt";
        final File file1 = getResourceFile(fileName1);
        reqBody = MultiPartBuilder.create()
                .setFileData(new FileData(null, file1))
                .build();
        response = post(getNodeChildrenUrl(f0Id), reqBody.getBody(), null, reqBody.getContentType(), 201);
        document = jacksonUtil.parseEntry(response.getJsonResponse(), Document.class);
        // Check the upload response
        assertEquals(fileName1, document.getName());
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, document.getContent().getMimeType());

        // upload with "default" binary content type and override filename - hence guess mimetype & use overridden name
        final String fileName2 = "quick-2.txt";
        final String fileName2b = "quick-2b.txt";
        final File file2 = getResourceFile(fileName2);
        reqBody = MultiPartBuilder.create()
                .setFileData(new FileData(fileName2b, file2))
                .build();
        response = post(getNodeChildrenUrl(f0Id), reqBody.getBody(), null, reqBody.getContentType(), 201);
        document = jacksonUtil.parseEntry(response.getJsonResponse(), Document.class);
        // Check the upload response
        assertEquals(fileName2b, document.getName());
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, document.getContent().getMimeType());

        
        response = getSingle(NodesEntityResource.class, Nodes.PATH_MY, null, 200);
        Folder user1Home = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);

        // User2 tries to upload a new file into the user1's home folder.
        
        setRequestContext(user2);
        
        final File file3 = getResourceFile(fileName2);
        reqBody = MultiPartBuilder.create()
                    .setFileData(new FileData(fileName2, file3))
                    .build();
        post(getNodeChildrenUrl(user1Home.getId()), reqBody.getBody(), null, reqBody.getContentType(), 403);
        
        post(getNodeChildrenUrl(f0Id), reqBody.getBody(), null, reqBody.getContentType(), 403);

        setRequestContext(user1);

        response = getAll(getNodeChildrenUrl(f0Id), paging, 200);
        pagingResult = parsePaging(response.getJsonResponse());
        assertNotNull(paging);
        assertEquals("Access Denied. The file shouldn't have been uploaded.", numOfNodes + 5, pagingResult.getCount().intValue());

        // User1 tries to upload a file into a document rather than a folder!
        post(getNodeChildrenUrl(document.getId()), reqBody.getBody(), null, reqBody.getContentType(), 400);

        // Try to upload a file without defining the required formData
        reqBody = MultiPartBuilder.create().setAutoRename(true).build();
        post(getNodeChildrenUrl(f0Id), reqBody.getBody(), null, reqBody.getContentType(), 400);

        // Test unsupported node type
        reqBody = MultiPartBuilder.create()
                    .setFileData(new FileData(fileName2, file2))
                    .setAutoRename(true)
                    .setNodeType("cm:link")
                    .build();
        post(getNodeChildrenUrl(f0Id), reqBody.getBody(), null, reqBody.getContentType(), 400);

        // User1 uploads a new file
        reqBody = MultiPartBuilder.create()
                    .setFileData(new FileData(fileName2, file2))
                    .build();
        response = post(getNodeChildrenUrl(f0Id), reqBody.getBody(), null, reqBody.getContentType(), 201);
        document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        // Check the upload response
        assertEquals(fileName2, document.getName());
        contentInfo = document.getContent();
        assertNotNull(contentInfo);
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, contentInfo.getMimeType());
        assertEquals("ISO-8859-1", contentInfo.getEncoding());

        // Test content size limit
        final SimpleFixedLimitProvider limitProvider = applicationContext.getBean("defaultContentLimitProvider", SimpleFixedLimitProvider.class);
        final long defaultSizeLimit = limitProvider.getSizeLimit();
        limitProvider.setSizeLimitString("20000"); //20 KB

        try
        {
            // quick.pdf size is about 23 KB
            reqBody = MultiPartBuilder.create()
                        .setFileData(new FileData(fileName, file))
                        .setAutoRename(true)
                        .build();

            // Try to upload a file larger than the configured size limit
            post(getNodeChildrenUrl(f0Id), reqBody.getBody(), null, reqBody.getContentType(), 413);
        }
        finally
        {
            limitProvider.setSizeLimitString(Long.toString(defaultSizeLimit));
        }
    }

    /**
     * Tests Multipart upload to a Site.
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/children}
     */
    @Test
    public void testUploadToSite() throws Exception
    {
        setRequestContext(user1);
        
        final String fileName = "quick-1.txt";
        final File file = getResourceFile(fileName);
        
        String folderA = "folder" + RUNID + "_A";
        String folderA_id = createFolder(tDocLibNodeId, folderA).getId();

        Paging paging = getPaging(0, Integer.MAX_VALUE);
        HttpResponse response = getAll(getNodeChildrenUrl(folderA_id), paging, 200);
        PublicApiClient.ExpectedPaging pagingResult = parsePaging(response.getJsonResponse());
        assertNotNull(paging);
        final int numOfNodes = pagingResult.getCount();

        MultiPartBuilder multiPartBuilder = MultiPartBuilder.create()
                    .setFileData(new FileData(fileName, file));
        MultiPartRequest reqBody = multiPartBuilder.build();
        // Try to upload
        response = post(getNodeChildrenUrl(folderA_id), reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        // Check the upload response
        assertEquals(fileName, document.getName());
        ContentInfo contentInfo = document.getContent();
        assertNotNull(contentInfo);
        // As the client didn't set the mimeType, the API must guess it.
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, contentInfo.getMimeType());

        // Retrieve the uploaded file
        response = getSingle(NodesEntityResource.class, document.getId(), null, 200);
        document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertEquals(fileName, document.getName());
        contentInfo = document.getContent();
        assertNotNull(contentInfo);
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, contentInfo.getMimeType());

        // Check 'get children' is confirming the upload
        response = getAll(getNodeChildrenUrl(folderA_id), paging, 200);
        pagingResult = parsePaging(response.getJsonResponse());
        assertNotNull(paging);
        assertEquals(numOfNodes + 1, pagingResult.getCount().intValue());

        // Upload the same file again to check the name conflicts handling
        post(getNodeChildrenUrl(folderA_id), reqBody.getBody(), null, reqBody.getContentType(), 409);

        response = getAll(getNodeChildrenUrl(folderA_id), paging, 200);
        pagingResult = parsePaging(response.getJsonResponse());
        assertNotNull(paging);
        assertEquals(numOfNodes + 1, pagingResult.getCount().intValue());

        setRequestContext(user2);
        
        final String fileName2 = "quick-2.txt";
        final File file2 = getResourceFile(fileName2);
        reqBody = MultiPartBuilder.create()
                    .setFileData(new FileData(fileName2, file2))
                    .build();
        // user2 tries to upload a new file into the folderA of user1
        post(getNodeChildrenUrl(folderA_id), reqBody.getBody(), null, reqBody.getContentType(), 403);

        setRequestContext(user1);
        
        // Test upload with properties
        response = post(getNodeChildrenUrl(folderA_id), reqBody.getBody(), null, reqBody.getContentType(), 201);
        document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        // Check the upload response
        assertEquals(fileName2, document.getName());
        contentInfo = document.getContent();
        assertNotNull(contentInfo);
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, contentInfo.getMimeType());
        assertNotNull(document.getProperties());
        assertNull(document.getProperties().get("cm:title"));
        assertNull(document.getProperties().get("cm:description"));

        // upload a file with properties. Also, set autoRename=true
        Map<String, String> props = new HashMap<>(2);
        props.put("cm:title", "test title");
        props.put("cm:description", "test description");
        reqBody = MultiPartBuilder.create()
                    .setFileData(new FileData(fileName2, file2))
                    .setAutoRename(true)
                    .setProperties(props)
                    .build();

        response = post(getNodeChildrenUrl(folderA_id), reqBody.getBody(), null, reqBody.getContentType(), 201);
        document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        // Check the upload response
        // "quick-2-1.txt" => fileName2 + autoRename
        assertEquals("quick-2-1.txt", document.getName());
        contentInfo = document.getContent();
        assertNotNull(contentInfo);
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, contentInfo.getMimeType());
        assertNotNull(document.getProperties());
        assertEquals("test title", document.getProperties().get("cm:title"));
        assertEquals("test description", document.getProperties().get("cm:description"));

        // Test unknown property name
        props = new HashMap<>(1);
        props.put("unknownPrefix" + System.currentTimeMillis() + ":description", "test description");
        reqBody = MultiPartBuilder.create()
                    .setFileData(new FileData(fileName2, file2))
                    .setAutoRename(true)
                    .setProperties(props)
                    .build();
        // Prop prefix is unknown
        post(getNodeChildrenUrl(folderA_id), reqBody.getBody(), null, reqBody.getContentType(), 400);

        // Test relativePath multi-part field.
        // Any folders in the relativePath that do not exist, are created before the content is created.
        multiPartBuilder = MultiPartBuilder.create()
                    .setFileData(new FileData(fileName, file))
                    .setRelativePath("X/Y/Z");
        reqBody = multiPartBuilder.build();

        response = post(getNodeChildrenUrl(folderA_id), reqBody.getBody(), "?include=path", reqBody.getContentType(), 201);
        document = jacksonUtil.parseEntry(response.getJsonResponse(), Document.class);
        // Check the upload response
        assertEquals(fileName, document.getName());
        contentInfo = document.getContent();
        assertNotNull(contentInfo);
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, contentInfo.getMimeType());
        // Check the uploaded file parent folders
        PathInfo pathInfo = document.getPath();
        assertNotNull(pathInfo);
        List<ElementInfo> elementInfos = pathInfo.getElements();
        assertNotNull(elementInfos);
        // /Company Home/Sites/RandomSite<timestamp>/documentLibrary/folder<timestamp>_A/X/Y/Z
        assertEquals(8, elementInfos.size());
        assertEquals(document.getParentId(), elementInfos.get(7).getId());
        assertEquals("Z", elementInfos.get(7).getName());
        assertEquals("Y", elementInfos.get(6).getName());
        assertEquals("X", elementInfos.get(5).getName());
        assertEquals(folderA, elementInfos.get(4).getName());

        // Try to create a folder with the same name as the document within the 'Z' folder.
        reqBody = MultiPartBuilder.copy(multiPartBuilder)
                    .setRelativePath("X/Y/Z/" + document.getName())
                    .build();
        post(getNodeChildrenUrl(folderA_id), reqBody.getBody(), null, reqBody.getContentType(), 409);

        // Test the same functionality as "mkdir -p x/y/z" which the folders should be created
        // as needed but no errors thrown if the path or any part of the path already exists.
        // NOTE: white spaces, leading and trailing "/" are ignored.
        reqBody = MultiPartBuilder.copy(multiPartBuilder)
                    .setRelativePath("/X/ Y/Z /CoolFolder/")
                    .build();
        response = post(getNodeChildrenUrl(folderA_id), reqBody.getBody(), null, reqBody.getContentType(), 201);
        document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        // Check the upload response
        assertEquals(fileName, document.getName());
        contentInfo = document.getContent();
        assertNotNull(contentInfo);
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, contentInfo.getMimeType());

        // Retrieve the uploaded file parent folder
        response = getSingle(NodesEntityResource.class, document.getParentId(), null, 200);
        Folder coolFolder = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertEquals(document.getParentId(), coolFolder.getId());
        assertEquals("CoolFolder", coolFolder.getName());

        // Try to upload quick-1.txt within coolFolder and set the relativePath to a blank string.
        reqBody = MultiPartBuilder.copy(multiPartBuilder)
                    .setRelativePath("  ")// blank
                    .build();
        // 409 -> as the blank string is ignored and quick-1.txt already exists in the coolFolder
        post(getNodeChildrenUrl(coolFolder.getId()), reqBody.getBody(), null, reqBody.getContentType(), 409);

        setRequestContext(user2);

        // user2 tries to upload the same file by creating sub-folders in the folderA of user1
        reqBody = MultiPartBuilder.copy(multiPartBuilder)
                    .setRelativePath("userTwoFolder1/userTwoFolder2")
                    .build();
        post(getNodeChildrenUrl(folderA_id), reqBody.getBody(), null, reqBody.getContentType(), 403);

        // -ve test: integrity error
        setRequestContext(user1);
        reqBody = MultiPartBuilder.create()
                .setFileData(new FileData("invalid:name", file))
                .build();
        // 422 -> invalid name (includes a ':' in this example)
        post(getNodeChildrenUrl(coolFolder.getId()), reqBody.getBody(), null, reqBody.getContentType(), 422);
    }

    /**
     * Tests delete (file or folder)
     * <p>DELETE:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>}
     */
    @Test
    public void testDelete() throws Exception
    {
        setRequestContext(user1);
        
        // create folder f0
        String folder0Name = "f0-testDelete-"+RUNID;
        String f0Id = createFolder(Nodes.PATH_MY, folder0Name).getId();

        String content1Id = createTextFile(f0Id, "content" + RUNID + "_1", "The quick brown fox jumps over the lazy dog.").getId();

        // delete file
        deleteNode(content1Id);

        assertTrue(existsArchiveNode(content1Id));

        // -ve test
        deleteNode(content1Id, 404);

        String folder1Id = createFolder(f0Id, "folder " + RUNID + "_1").getId();
        String folder2Id = createFolder(folder1Id, "folder " + RUNID + "_2").getId();

        String content2Id = createTextFile(folder2Id, "content" + RUNID + "_2", "The quick brown fox jumps over the lazy dog.").getId();

        // cascade delete folder
        deleteNode(folder1Id);

        assertTrue(existsArchiveNode(folder1Id));
        assertTrue(existsArchiveNode(folder2Id));
        assertTrue(existsArchiveNode(content2Id));

        // -ve test
        deleteNode(folder2Id, 404);
        deleteNode(content2Id, 404);

        // -ve test
        String rootNodeId = getRootNodeId();
        deleteNode(rootNodeId, 403);

        //
        // permanently delete - ie. bypass trashcan (archive store)
        //

        String folder3Id = createFolder(f0Id, "folder " + RUNID + "_3").getId();
        String folder4Id = createFolder(folder3Id, "folder " + RUNID + "_4").getId();
        
        deleteNode(folder3Id, true, 204);

        assertFalse(existsArchiveNode(folder3Id));
        assertFalse(existsArchiveNode(folder4Id));

        
        String sharedNodeId = getSharedNodeId();
        final String folder5Id = createFolder(sharedNodeId, "folder " + RUNID + "_5").getId();

        // -ve test - another user cannot delete
        setRequestContext(user2);
        deleteNode(folder5Id, 403);

        setRequestContext(user1);

        Map<String, Object> props = new HashMap<>();
        props.put(PROP_OWNER, user2);
        Node nUpdate = new Node();
        nUpdate.setProperties(props);

        HttpResponse response = put(URL_NODES, folder5Id, toJsonAsStringNonNull(nUpdate), null, 200);
        Node nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertEquals(user2, ((Map)nodeResp.getProperties().get(PROP_OWNER)).get("id"));

        // TODO see REPO-907. Apparently returns 204 here in tenant context ?? (eg. if useDefaultNetwork=false)
        if (useDefaultNetwork)
        {
            // -ve test - user1 can no longer delete
            deleteNode(folder5Id, 403); 
        }

        // TODO refactor with remote permission api calls (maybe use v0 until we have v1 ?)
        final String tenantDomain = (networkOne != null ? networkOne.getId() : TenantService.DEFAULT_DOMAIN);
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                return TenantUtil.runAsTenant(new TenantUtil.TenantRunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        permissionService.setPermission(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, folder5Id), user1, PermissionService.DELETE, true);
                        return null;
                    }
                }, tenantDomain);
            }
        }, user1);


        // -ve test - non-owner cannot bypass trashcan
        deleteNode(folder5Id, true, 403);

        // user1 has permission to delete (via trashcan)
        deleteNode(folder5Id);

        // admin can permanently delete
        String folder6Id = createFolder(sharedNodeId, "folder " + RUNID + "_6").getId();
        
        setRequestContext(networkAdmin);
        
        deleteNode(folder6Id, true, 204);

        // -ve - cannot delete Company Home root node
        deleteNode(rootNodeId, true, 403);

        Map<String, String> params = new HashMap<>();
        params.put(Nodes.PARAM_RELATIVE_PATH, "/Sites");
        response = getSingle(NodesEntityResource.class, rootNodeId, params, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        String sitesNodeId = nodeResp.getId();

        // -ve - cannot delete Sites node
        deleteNode(sitesNodeId, true, 403);

        params = new HashMap<>();
        params.put(Nodes.PARAM_RELATIVE_PATH, "/Data Dictionary");
        response = getSingle(NodesEntityResource.class, rootNodeId, params, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        String ddNodeId = nodeResp.getId();

        // -ve - cannot delete Data Dictionary node
        deleteNode(ddNodeId, true, 403);
    }

    private boolean existsArchiveNode(String nodeId) throws Exception
    {
        boolean result = false;
        
        HttpResponse response = publicApiClient.get(getScope(), URL_DELETED_NODES, nodeId, null, null, null);
        if ((response != null) && (response.getStatusCode() == 200))
        {
            Node node = jacksonUtil.parseEntry(response.getJsonResponse(), Node.class);
            result = ((node != null) && (node.getId() != null));
        }
        
        return result;
    }

    /**
     * Tests move (file or folder)
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/move}
     */
    @Test
    public void testMove() throws Exception
    {
        setRequestContext(user1);

        // create folder f0
        String folder0Name = "f0-testMove-"+RUNID;
        String f0Id = createFolder(Nodes.PATH_MY, folder0Name).getId();
        
        // create folder f1
        Folder folderResp = createFolder(f0Id, "f1");
        String f1Id = folderResp.getId();

        // create folder f2
        folderResp = createFolder(f0Id, "f2");
        String f2Id = folderResp.getId();

        // create doc d1
        String d1Name = "content" + RUNID + "_1";
        String d1Id = createTextFile(f1Id, d1Name, "The quick brown fox jumps over the lazy dog 1.").getId();

        // create doc d2
        String d2Name = "content" + RUNID + "_2";
        String d2Id = createTextFile(f2Id, d2Name, "The quick brown fox jumps over the lazy dog 2.").getId();

        // move file (without rename)

        NodeTarget tgt = new NodeTarget();
        tgt.setTargetParentId(f2Id);

        HttpResponse response = post("nodes/"+d1Id+"/move", toJsonAsStringNonNull(tgt), null, 200);
        Document documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        assertEquals(d1Name, documentResp.getName());
        assertEquals(f2Id, documentResp.getParentId());

        // move file (with rename)

        String d1NewName = d1Name+" updated !!";

        tgt = new NodeTarget();
        tgt.setName(d1NewName);
        tgt.setTargetParentId(f1Id);

        response = post("nodes/"+d1Id+"/move", toJsonAsStringNonNull(tgt), null, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        assertEquals(d1NewName, documentResp.getName());
        assertEquals(f1Id, documentResp.getParentId());

        // -ve tests

        // missing target
        tgt = new NodeTarget();
        tgt.setName("new name");
        post("nodes/"+d1Id+"/move", toJsonAsStringNonNull(tgt), null, 400);

        // name already exists
        tgt = new NodeTarget();
        tgt.setName(d2Name);
        tgt.setTargetParentId(f2Id);
        post("nodes/"+d1Id+"/move", toJsonAsStringNonNull(tgt), null, 409);

        // unknown source nodeId
        tgt = new NodeTarget();
        tgt.setTargetParentId(f2Id);
        post("nodes/"+UUID.randomUUID().toString()+"/move", toJsonAsStringNonNull(tgt), null, 404);

        // unknown target nodeId
        tgt = new NodeTarget();
        tgt.setTargetParentId(UUID.randomUUID().toString());
        post("nodes/"+d1Id+"/move", toJsonAsStringNonNull(tgt), null, 404);

        // target is not a folder
        tgt = new NodeTarget();
        tgt.setTargetParentId(d2Id);
        post("nodes/"+d1Id+"/move", toJsonAsStringNonNull(tgt), null, 400);

        String rootNodeId = getRootNodeId();

        // create folder f3 (sub-folder of f2)
        folderResp = createFolder(f2Id, "f3");
        String f3Id = folderResp.getId();

        // can't create cycle (move into own subtree)
        tgt = new NodeTarget();
        tgt.setTargetParentId(f3Id);
        post("nodes/"+f2Id+"/move", toJsonAsStringNonNull(tgt), null, 400);

        // no (write/create) permissions to move to target
        tgt = new NodeTarget();
        tgt.setTargetParentId(rootNodeId);
        post("nodes/"+d1Id+"/move", toJsonAsStringNonNull(tgt), null, 403);

        setRequestContext(user2);

        String my2NodeId = getMyNodeId();

        // no (write/delete) permissions to move source
        tgt = new NodeTarget();
        tgt.setTargetParentId(my2NodeId);
        post("nodes/"+f1Id+"/move", toJsonAsStringNonNull(tgt), null, 403);
        
        // -ve - cannot move (delete) Company Home root node
        setRequestContext(networkAdmin);
        post("nodes/"+rootNodeId+"/move", toJsonAsStringNonNull(tgt), null, 403);
        
        setRequestContext(user1);                                
        
        Map params = new HashMap<>();
        params.put(Nodes.PARAM_RELATIVE_PATH, "/Sites");
        response = getSingle(NodesEntityResource.class, rootNodeId, params, 200);
        Node nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        String sitesNodeId = nodeResp.getId();

        // -ve - cannot move (delete) Sites node
        setRequestContext(networkAdmin);
        post("nodes/"+sitesNodeId+"/move", toJsonAsStringNonNull(tgt), null, 403);

        setRequestContext(user1);
        
        params = new HashMap<>();
        params.put(Nodes.PARAM_RELATIVE_PATH, "/Data Dictionary");
        response = getSingle(NodesEntityResource.class, rootNodeId, params, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        String ddNodeId = nodeResp.getId();

        // -ve - cannot move (delete) Data Dictionary node
        setRequestContext(networkAdmin);
        post("nodes/"+ddNodeId+"/move", toJsonAsStringNonNull(tgt), null, 403);

        // -ve test - cannot move to multiple destinations in single POST call (unsupported)
        List<NodeTarget> nodeDestinations = new ArrayList<>(2);
        NodeTarget nodeTarget = new NodeTarget();
        nodeTarget.setTargetParentId(f1Id);
        nodeDestinations.add(nodeTarget);
        nodeTarget = new NodeTarget();
        nodeTarget.setTargetParentId(f2Id);
        nodeDestinations.add(nodeTarget);

        post("nodes/"+d1Id+"/move", toJsonAsStringNonNull(nodeDestinations), null, 405);
    }

    /**
     * Tests copy (file or folder)
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/copy}
     */
    @Test
    public void testCopy() throws Exception
    {
        setRequestContext(user1);
        
        // create folder
        Folder folderResp = createFolder(Nodes.PATH_MY, "fsource");
        String sourceId = folderResp.getId();

        // create folder
        folderResp = createFolder(Nodes.PATH_MY, "ftarget");
        String targetId = folderResp.getId();

        // create doc d1
        String d1Name = "content" + RUNID + "_1";
        String d1Id = createTextFile(sourceId, d1Name, "The quick brown fox jumps over the lazy dog 1.").getId();

        // create doc d2
        String d2Name = "content" + RUNID + "_2";
        String d2Id = createTextFile(sourceId, d2Name, "The quick brown fox jumps over the lazy dog 2.").getId();

        Map<String, String> body = new HashMap<>();
        body.put("targetParentId", targetId);

        HttpResponse response = post(URL_NODES, d1Id, "copy", toJsonAsStringNonNull(body).getBytes(), null, null, 201);
        Document documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        assertEquals(d1Name, documentResp.getName());
        assertEquals(targetId, documentResp.getParentId());

        // copy file (with rename)
        String newD2Name = d2Name + " updated !!";

        body = new HashMap<>();
        body.put("targetParentId", targetId);
        body.put("name", newD2Name);

        response = post(URL_NODES, d2Id, "copy", toJsonAsStringNonNull(body).getBytes(), null, null, 201);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        assertEquals(newD2Name, documentResp.getName());
        assertEquals(targetId, documentResp.getParentId());

        // -ve tests

        // missing target
        NodeTarget tgt = new NodeTarget();
        tgt.setName("new name");
        post("nodes/"+d1Id+"/copy", toJsonAsStringNonNull(tgt), null, 400);

        // name already exists - different parent
        tgt = new NodeTarget();
        tgt.setName(newD2Name);
        tgt.setTargetParentId(targetId);
        post("nodes/"+d1Id+"/copy", toJsonAsStringNonNull(tgt), null, 409);

        // name already exists - same parent
        tgt = new NodeTarget();
        tgt.setTargetParentId(sourceId);
        post("nodes/"+d1Id+"/copy", toJsonAsStringNonNull(tgt), null, 409);

        // unknown source nodeId
        tgt = new NodeTarget();
        tgt.setTargetParentId(targetId);
        post("nodes/"+UUID.randomUUID().toString()+"/copy", toJsonAsStringNonNull(tgt), null, 404);

        // unknown target nodeId
        tgt = new NodeTarget();
        tgt.setTargetParentId(UUID.randomUUID().toString());
        post("nodes/"+d1Id+"/copy", toJsonAsStringNonNull(tgt), null, 404);

        // target is not a folder
        tgt = new NodeTarget();
        tgt.setTargetParentId(d2Id);
        post("nodes/"+d1Id+"/copy", toJsonAsStringNonNull(tgt), null, 400);

        String rootNodeId = getRootNodeId();

        // no (write/create) permissions to copy to target
        tgt = new NodeTarget();
        tgt.setTargetParentId(rootNodeId);
        post("nodes/"+d1Id+"/copy", toJsonAsStringNonNull(tgt), null, 403);

        // -ve test - cannot copy to multiple destinations in single POST call (unsupported)
        List<NodeTarget> nodeDestinations = new ArrayList<>(2);
        NodeTarget nodeTarget = new NodeTarget();
        nodeTarget.setTargetParentId(sourceId);
        nodeDestinations.add(nodeTarget);
        nodeTarget = new NodeTarget();
        nodeTarget.setTargetParentId(targetId);
        nodeDestinations.add(nodeTarget);

        post("nodes/"+d1Id+"/copy", toJsonAsStringNonNull(nodeDestinations), null, 405);
    }

    @Test
    public void testCopySite() throws Exception
    {
        setRequestContext(user1);
        
        // create folder
        Folder folderResp = createFolder(Nodes.PATH_MY, "siteCopytarget");
        String targetId = folderResp.getId();

        Map<String, String> body = new HashMap<>();
        body.put("targetParentId", targetId);

        //test that you can't copy a site
        HttpResponse response = getSingle("sites", tSiteId, null, null, 200);
        Site siteResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Site.class);
        String siteNodeId = siteResp.getGuid();
        post("nodes/"+siteNodeId+"/copy", toJsonAsStringNonNull(body), null, 422);
        
        //test that you can't copy a site doclib
        post("nodes/"+tDocLibNodeId+"/copy", toJsonAsStringNonNull(body), null, 422);

    }

    /**
     * Tests move and copy folder between sites.
     *
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/move}
     *
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/copy}
     */
    @Test
    public void testMoveCopyBetweenSites() throws Exception
    {
        setRequestContext(user1);
        
        /*
         * Precondition - create two sites, invite users, create folders
         */
        
        // user1 creates a public site and adds user2 as a site collaborator
        String site1Title = "site-testMoveCopyBetweenSites1-" + RUNID;
        final String site1Id = createSite(site1Title, SiteVisibility.PUBLIC).getId();
        addSiteMember(site1Id, user2, SiteRole.SiteCollaborator);

        // Get user1Site's docLib node id
        final String user1SitetDocLibNodeId = getSiteContainerNodeId(site1Id, "documentLibrary");

        // user1 creates a folder in the docLib of his site (user1Site)
        String user1Folder = "folder" + RUNID + "_user1";
        String user1FolderNodeId = createFolder(user1SitetDocLibNodeId, user1Folder, null).getId();
        
        setRequestContext(user2);
        
        // user2 creates a public site and adds user1 as a site collaborator
        String site2Title = "site-testMoveCopyBetweenSites2--" + RUNID;
        final String site2Id = createSite(site2Title, SiteVisibility.PUBLIC).getId();
        addSiteMember(site2Id, user1, SiteRole.SiteCollaborator);
        
        // Get user2Site's docLib node id
        final String user2SitetDocLibNodeId = getSiteContainerNodeId(site2Id, "documentLibrary");

        // user2 creates 2 folders within the docLib of the user1Site
        String user2Folder1 = "folder1" + RUNID + "_user2";
        String user2FolderNodeId = createFolder(user1SitetDocLibNodeId, user2Folder1, null).getId();

        String user2Folder2 = "folder2" + RUNID + "_user2";
        String user2Folder2NodeId = createFolder(user1SitetDocLibNodeId, user2Folder2, null).getId();

        /*
         * Test move between sites
         */
        // user1 moves the folder created by user2 to the user2Site's docLib

        setRequestContext(user1);
        
        NodeTarget target = new NodeTarget();
        target.setTargetParentId(user2SitetDocLibNodeId);
        HttpResponse response = post("nodes/" + user2FolderNodeId + "/move", toJsonAsStringNonNull(target), null, 200);
        Folder moveFolderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertEquals(user2SitetDocLibNodeId, moveFolderResp.getParentId());

        // user1 tries to undo the move (moves back the folder to its original place)
        // as user1 is just a SiteCollaborator in the user2Site, he can't move the folder which he doesn't own - ACL access permission.
        target = new NodeTarget();
        target.setTargetParentId(user1SitetDocLibNodeId);
        post("nodes/" + user2FolderNodeId + "/move", toJsonAsStringNonNull(target), null, 403);

        // user1 moves the folder created by himself to the docLib of the user2Site
        target = new NodeTarget();
        target.setTargetParentId(user2SitetDocLibNodeId);
        response = post("nodes/" + user1FolderNodeId + "/move", toJsonAsStringNonNull(target), null, 200);
        moveFolderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertEquals(user2SitetDocLibNodeId, moveFolderResp.getParentId());

        // user1 tries to undo the move (moves back the folder to its original place)
        // The undo should be successful as user1 owns the folder
        target = new NodeTarget();
        target.setTargetParentId(user1SitetDocLibNodeId);
        response = post("nodes/" + user1FolderNodeId + "/move", toJsonAsStringNonNull(target), null, 200);
        moveFolderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertEquals(user1SitetDocLibNodeId, moveFolderResp.getParentId());


        /*
         * Test copy between sites
         */
        // user1 copies the folder created by user2 to the user2Site's docLib
        target = new NodeTarget();
        target.setTargetParentId(user2SitetDocLibNodeId);
        response = post("nodes/" + user2Folder2NodeId + "/copy", toJsonAsStringNonNull(target), null, 201);
        Folder copyFolderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertEquals(user2SitetDocLibNodeId, copyFolderResp.getParentId());

        // user1 tries to undo the copy (hard deletes the created copy)
        deleteNode( copyFolderResp.getId(), true, 204);
        
        // Check it's deleted
        getSingle("nodes", copyFolderResp.getId(), 404);
        
        
        // cleanup
        
        setRequestContext(user1);
        deleteSite(site1Id, true, 204);
        
        setRequestContext(user2);
        deleteSite(site2Id, true, 204);
    }

    /**
     * Tests create folder.
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/children}
     */
    @Test
    public void testCreateFolder() throws Exception
    {
        setRequestContext(user1);
        
        String myNodeId = getMyNodeId();

        UserInfo expectedUser = new UserInfo(user1);

        String postUrl = getNodeChildrenUrl(myNodeId);

        // create folder
        Folder folderResp = createFolder(myNodeId, "f1");
        String f1Id = folderResp.getId();

        Folder f1 = new Folder();
        f1.setName("f1");
        f1.setNodeType(TYPE_CM_FOLDER);

        f1.setIsFolder(true);
        f1.setParentId(myNodeId);
        f1.setAspectNames(Collections.singletonList("cm:auditable"));

        f1.setCreatedByUser(expectedUser);
        f1.setModifiedByUser(expectedUser);

        f1.expected(folderResp);

        // create sub-folder with properties
        Map<String,Object> props = new HashMap<>();
        props.put("cm:title","my folder title");
        props.put("cm:description","my folder description");

        folderResp = createFolder(f1Id, "f2", props);
        String f2Id = folderResp.getId();

        Folder f2 = new Folder();
        f2.setName("f2");
        f2.setNodeType(TYPE_CM_FOLDER);
        f2.setProperties(props);

        f2.setIsFolder(true);
        f2.setParentId(f1Id);
        f2.setAspectNames(Arrays.asList("cm:auditable","cm:titled"));

        f2.setCreatedByUser(expectedUser);
        f2.setModifiedByUser(expectedUser);

        f2.expected(folderResp);

        // create another folder in a (partially existing) folder path
        Node n = new Node();
        n.setName("fZ");
        n.setNodeType(TYPE_CM_FOLDER);
        n.setRelativePath("/f1/f2/f3/f4");

        // create node
        HttpResponse response = post(getNodeChildrenUrl(myNodeId), RestApiUtil.toJsonAsStringNonNull(n), 201);
        folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);

        // check parent hierarchy ...
        response = getSingle(NodesEntityResource.class, folderResp.getId(), null, 200);
        folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertEquals(folderResp.getName(),"fZ");

        response = getSingle(NodesEntityResource.class, folderResp.getParentId(), null, 200);
        folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertEquals(folderResp.getName(),"f4");

        response = getSingle(NodesEntityResource.class, folderResp.getParentId(), null, 200);
        folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertEquals(folderResp.getName(),"f3");

        response = getSingle(NodesEntityResource.class, folderResp.getParentId(), null, 200);
        folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertEquals(folderResp.getName(),"f2");
        assertEquals(folderResp.getId(), f2Id);

        // -ve test - name is mandatory
        Folder invalid = new Folder();
        invalid.setNodeType(TYPE_CM_FOLDER);
        post(postUrl, toJsonAsStringNonNull(invalid), 400);

        // -ve test - invalid name
        invalid = new Folder();
        invalid.setName("inv:alid");
        invalid.setNodeType(TYPE_CM_FOLDER);
        post(postUrl, toJsonAsStringNonNull(invalid), 422);

        // -ve test - node type is mandatory
        invalid = new Folder();
        invalid.setName("my folder");
        post(postUrl, toJsonAsStringNonNull(invalid), 400);

        // create empty file - used in -ve test below
        Document d1 = new Document();
        d1.setName("d1.txt");
        d1.setNodeType(TYPE_CM_CONTENT);

        response = post(postUrl, toJsonAsStringNonNull(d1), 201);
        Document documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        String d1Id = documentResp.getId();

        // -ve test - invalid (model integrity exception)
        Folder f3 = new Folder();
        f3.setName("f3");
        f3.setNodeType(TYPE_CM_FOLDER);
        post(getNodeChildrenUrl(d1Id), toJsonAsStringNonNull(f3), 422);

        // -ve test - it should not be possible to create a "system folder"
        invalid = new Folder();
        invalid.setName("my sys folder");
        invalid.setNodeType("cm:systemfolder");
        post(postUrl, toJsonAsStringNonNull(invalid), 400);

        // -ve test - unknown parent folder node id
        post(getNodeChildrenUrl(UUID.randomUUID().toString()), toJsonAsStringNonNull(f3), 404);

        // -ve test - duplicate name
        post(postUrl, toJsonAsStringNonNull(f1), 409);

        // Create a folder with a duplicate name (f1), but set the autoRename to true
        response = post(postUrl, toJsonAsStringNonNull(f1), "?"+Nodes.PARAM_AUTO_RENAME+"=true", 201);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertEquals("f1-1", documentResp.getName());

        // Create a folder with a duplicate name (f1) again, but set the autoRename to true
        response = post(postUrl, toJsonAsStringNonNull(f1), "?"+Nodes.PARAM_AUTO_RENAME+"=true", 201);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertEquals("f1-2", documentResp.getName());

        // -ve test - create a folder with a duplicate name (f1), but set the autoRename to false
        post(postUrl, toJsonAsStringNonNull(f1), "?"+Nodes.PARAM_AUTO_RENAME+"=false", 409);
        
        // Create folder using relative path
        n = new Node();
        n.setName("fX");
        n.setNodeType(TYPE_CM_FOLDER);
        n.setRelativePath("/f1/f2");
        
        response = post(postUrl, toJsonAsStringNonNull(n), "?"+Nodes.PARAM_AUTO_RENAME+"=true", 201);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertEquals("fX", documentResp.getName());

        // Create a folder using relative path, with a duplicate name (fX) but set the autoRename to true
        response = post(postUrl, toJsonAsStringNonNull(n), "?"+Nodes.PARAM_AUTO_RENAME+"=true", 201);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertEquals("fX-1", documentResp.getName());

        // -ve test - create a folder with a duplicate name (fX), but set the autoRename to false
        post(postUrl, toJsonAsStringNonNull(n), "?"+Nodes.PARAM_AUTO_RENAME+"=false", 409);

        // -ve test - invalid relative path
        n = new Node();
        n.setName("fX");
        n.setNodeType(TYPE_CM_FOLDER);
        n.setRelativePath("/f1/inv:alid");
        post(getNodeChildrenUrl(f2Id), RestApiUtil.toJsonAsStringNonNull(n), 422);

        // -ve test - invalid relative path - points to existing node that is not a folder
        n = new Node();
        n.setName("fY");
        n.setNodeType(TYPE_CM_FOLDER);
        n.setRelativePath("d1.txt");
        post(getNodeChildrenUrl(myNodeId), RestApiUtil.toJsonAsStringNonNull(n), 409);

        // -ve test - minor: error code if trying to create with property with invalid format (REPO-473)
        props = new HashMap<>();
        props.put("exif:pixelYDimension", "my unknown property");
        n = new Folder();
        n.setName("fZ");
        n.setNodeType(TYPE_CM_FOLDER);
        n.setProperties(props);
        post(getNodeChildrenUrl(myNodeId), RestApiUtil.toJsonAsStringNonNull(n), 400);
    }

    /**
     * Tests creation and listing of children using assoc type other than "cm:contains".
     *
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/children}
     *
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/children}
     */
    @Test
    public void testChildrenAssocType() throws Exception
    {
        setRequestContext(user1);
        
        String myNodeId = getMyNodeId();

        String fId = null;

        try
        {
            fId = createFolder(myNodeId, "testChildrenAssocType folder").getId();

            Node nodeUpdate = new Node();
            nodeUpdate.setAspectNames(Collections.singletonList(ASPECT_CM_PREFERENCES));

            put(URL_NODES, fId, toJsonAsStringNonNull(nodeUpdate), null, 200);

            HttpResponse response = getAll(getNodeChildrenUrl(fId), null, null, 200);
            List<Node> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(0, nodes.size());

            Node obj = new Node();
            obj.setName("c1");
            obj.setNodeType(TYPE_CM_CONTENT);

            // assoc type => cm:contains
            response = post(getNodeChildrenUrl(fId), toJsonAsStringNonNull(obj), 201);
            Node nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
            String c1Id = nodeResp.getId();
            assertEquals(fId, nodeResp.getParentId());

            response = getAll(getNodeChildrenUrl(fId), null, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(1, nodes.size());

            obj = new Node();
            obj.setName("c2");
            obj.setNodeType(TYPE_CM_CONTENT);
            Association assoc = new Association();
            assoc.setAssocType(ASSOC_TYPE_CM_PREFERENCE_IMAGE);
            obj.setAssociation(assoc);

            // assoc type => cm:preferenceImage
            response = post(getNodeChildrenUrl(fId), toJsonAsStringNonNull(obj), 201);
            nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
            String c2Id = nodeResp.getId();
            assertEquals(fId, nodeResp.getParentId());

            response = getAll(getNodeChildrenUrl(fId), null, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(2, nodes.size());

            Map<String, String> params = new HashMap<>();
            params.put("where", "(assocType='"+ASSOC_TYPE_CM_CONTAINS+"')");
            params.put("include", "association");
            response = getAll(getNodeChildrenUrl(fId), null, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(1, nodes.size());
            assertEquals(c1Id, nodes.get(0).getId());
            assertTrue(nodes.get(0).getAssociation().getIsPrimary());

            params = new HashMap<>();
            params.put("where", "(assocType='"+ASSOC_TYPE_CM_PREFERENCE_IMAGE+"')");
            params.put("include", "association");
            response = getAll(getNodeChildrenUrl(fId), null, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(1, nodes.size());
            assertEquals(c2Id, nodes.get(0).getId());
            assertTrue(nodes.get(0).getAssociation().getIsPrimary());

            //
            // test that we can also create children below content
            //

            obj = new Node();
            obj.setName("c3");
            obj.setNodeType(TYPE_CM_CONTENT);
            nodeUpdate.setAspectNames(Collections.singletonList(ASPECT_CM_PREFERENCES));

            // assoc type => cm:contains
            response = post(getNodeChildrenUrl(fId), toJsonAsStringNonNull(obj), 201);
            nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
            String c3Id = nodeResp.getId();

            obj = new Node();
            obj.setName("c4");
            obj.setNodeType(TYPE_CM_CONTENT);
            assoc = new Association();
            assoc.setAssocType(ASSOC_TYPE_CM_PREFERENCE_IMAGE);
            obj.setAssociation(assoc);

            // assoc type => cm:preferenceImage
            response = post(getNodeChildrenUrl(c3Id), toJsonAsStringNonNull(obj), 201);
            nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
            assertEquals(c3Id, nodeResp.getParentId());

            // -ve test
            obj = new Node();
            obj.setName("c5");
            obj.setNodeType(TYPE_CM_CONTENT);
            assoc = new Association();
            assoc.setAssocType(ASSOC_TYPE_CM_CONTAINS);
            obj.setAssociation(assoc);

            // assoc type => cm:contains (requires parent to be a folder !)
            post(getNodeChildrenUrl(c3Id), toJsonAsStringNonNull(obj), 422);
        }
        finally
        {
            // some cleanup
            if (fId != null)
            {
                deleteNode(fId);
            }
        }
    }

    // TODO test custom types with properties (sub-type of cm:cmobject)

    @Test
    public void testListChildrenIsFileIsFolderFilter() throws Exception
    {
        setRequestContext(user1);
        
        String myNodeId = getMyNodeId();
        
        String fId = createFolder(myNodeId, "f1-testUpdateOwner-"+RUNID).getId();
        
        String childrenUrl = getNodeChildrenUrl(fId);
        
        int folderCnt = 2;
        int fileCnt = 3;
        int objCnt = 4;

        // create some folders
        List<String> folderIds = new ArrayList<>(folderCnt);

        for (int i = 1; i <= folderCnt; i++)
        {
            folderIds.add(createFolder(fId, "folder "+i+" "+RUNID).getId());
        }

        // create some files
        List<String> fileIds = new ArrayList<>(fileCnt);
        for (int i = 1; i <= fileCnt; i++)
        {
            fileIds.add(createTextFile(fId, "file "+i+" "+RUNID, "The quick brown fox jumps over the lazy dog "+i).getId());
        }

        // create some nodes (cmobject)
        List<String> objIds = new ArrayList<>(objCnt);
        for (int i = 1; i <= objCnt; i++)
        {
            Node obj = new Node();
            obj.setName("obj "+i+" "+RUNID);
            obj.setNodeType(TYPE_CM_OBJECT);

            // create node/object
            HttpResponse response = post(childrenUrl, toJsonAsStringNonNull(obj), 201);
            Node nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
            objIds.add(nodeResp.getId());
        }

        List<String> allIds = new ArrayList<>(objCnt+folderCnt+fileCnt);
        allIds.addAll(folderIds);
        allIds.addAll(fileIds);
        allIds.addAll(objIds);

        List<String> folderAndFileIds = new ArrayList<>(folderCnt+fileCnt);
        folderAndFileIds.addAll(folderIds);
        folderAndFileIds.addAll(fileIds);

        List<String> notFileIds = new ArrayList<>(folderCnt+objCnt);
        notFileIds.addAll(folderIds);
        notFileIds.addAll(objIds);

        List<String> notFolderIds = new ArrayList<>(fileCnt+objCnt);
        notFolderIds.addAll(fileIds);
        notFolderIds.addAll(objIds);

        Paging paging = getPaging(0, Integer.MAX_VALUE);

        // no filtering

        HttpResponse response = getAll(childrenUrl, paging, null, 200);
        List<Node> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        checkNodeIds(nodes, allIds);

        // filtering, via where clause - folders

        Map<String, String> params = new HashMap<>();
        params.put("where", "(nodeType='"+TYPE_CM_FOLDER+"')");

        response = getAll(childrenUrl, paging, params, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        checkNodeIds(nodes, folderIds);

        params = new HashMap<>();
        params.put("where", "(isFolder=true)");

        response = getAll(childrenUrl, paging, params, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        checkNodeIds(nodes, folderIds);

        params = new HashMap<>();
        params.put("where", "(isFolder=true AND isFile=false)");

        response = getAll(childrenUrl, paging, params, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        checkNodeIds(nodes, folderIds);

        // filtering, via where clause - files

        params = new HashMap<>();
        params.put("where", "(nodeType='"+TYPE_CM_CONTENT+"')");

        response = getAll(childrenUrl, paging, params, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        checkNodeIds(nodes, fileIds);

        params = new HashMap<>();
        params.put("where", "(isFile=true)");

        response = getAll(childrenUrl, paging, params, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        checkNodeIds(nodes, fileIds);

        params = new HashMap<>();
        params.put("where", "(isFile=true AND isFolder=false)");

        response = getAll(childrenUrl, paging, params, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        checkNodeIds(nodes, fileIds);

        // filtering, via where clause - non-folders / non-files

        params = new HashMap<>();
        params.put("where", "(nodeType='"+TYPE_CM_OBJECT+"')");

        response = getAll(childrenUrl, paging, params, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        checkNodeIds(nodes, objIds);

        params = new HashMap<>();
        params.put("where", "(nodeType='cm:cmobject INCLUDESUBTYPES')");

        response = getAll(childrenUrl, paging, params, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        checkNodeIds(nodes, allIds);

        params = new HashMap<>();
        params.put("where", "(isFile=false AND isFolder=false)");

        response = getAll(childrenUrl, paging, params, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        checkNodeIds(nodes, objIds);

        // filtering, via where clause - not files
        params = new HashMap<>();
        params.put("where", "(isFile=false)");

        response = getAll(childrenUrl, paging, params, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        checkNodeIds(nodes, notFileIds);

        // filtering, via where clause - not folders
        params = new HashMap<>();
        params.put("where", "(isFolder=false)");

        response = getAll(childrenUrl, paging, params, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        checkNodeIds(nodes, notFolderIds);

        // -ve - node cannot be both a file and a folder
        params = new HashMap<>();
        params.put("where", "(isFile=true AND isFolder=true)");
        getAll(childrenUrl, paging, params, 400);

        // -ve - nodeType and isFile/isFolder are mutually exclusive
        params = new HashMap<>();
        params.put("where", "(nodeType='cm:object' AND isFolder=true)");
        getAll(childrenUrl, paging, params, 400);

        params = new HashMap<>();
        params.put("where", "(nodeType='cm:object' AND isFile=true)");
        getAll(childrenUrl, paging, params, 400);
    }

    private void checkNodeIds(List<Node> nodes, List<String> nodeIds)
    {
        assertEquals(nodeIds.size(), nodes.size());
        for (Node node : nodes)
        {
            assertTrue(nodeIds.contains(node.getId()));
        }
    }

    // note: app:folderlink & app:filelink both extend cm:link (which in turn extends cm:cmobject)
    //       (see applicationModel.xml / contentModel.xml)
    @Test
    public void testLinkCRUD() throws Exception
    {
        setRequestContext(user1);

        String myNodeId = getMyNodeId();
        UserInfo expectedUser = new UserInfo(user1);
        String myChildrenUrl = getNodeChildrenUrl(myNodeId);
        
        // create folder f1
        Folder folderResp = createFolder(myNodeId, "f1 "+RUNID);
        String f1Id = folderResp.getId();

        // create empty file d1 in f1
        Document d1 = new Document();
        d1.setName("d1.txt");
        d1.setNodeType(TYPE_CM_CONTENT);

        HttpResponse response = post(getNodeChildrenUrl(f1Id), toJsonAsStringNonNull(d1), 201);
        Document documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        String d1Id = documentResp.getId();

        // create folder f2
        folderResp = createFolder(myNodeId, "f2 "+RUNID);
        String f2Id = folderResp.getId();

        // create folder link node in f2 (pointing to f1)
        String nodeName = "f1 link";
        String nodeType = "app:folderlink";

        Map<String,Object> props = new HashMap<>();
        props.put("cm:destination", f1Id);

        Node nodeResp = createNode(f2Id, nodeName, nodeType, props);
        String n1Id = nodeResp.getId();

        Node n1 = new Node();
        n1.setName(nodeName);
        n1.setNodeType(nodeType);
        n1.setIsFolder(true);
        n1.setParentId(f2Id); // note: parent of the link (not where it is pointing)
        n1.setAspectNames(Collections.singletonList("cm:auditable"));
        n1.setProperties(props);
        n1.setCreatedByUser(expectedUser);
        n1.setModifiedByUser(expectedUser);

        n1.expected(nodeResp);

        // get node info
        response = getSingle(NodesEntityResource.class, n1Id, null, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);

        n1.expected(nodeResp);

        // create file link node in f2 pointing to d1
        nodeName = "d1 link";
        nodeType = "app:filelink";

        props = new HashMap<>();
        props.put("cm:destination", d1Id);

        nodeResp = createNode(f2Id, nodeName, nodeType, props);
        String n2Id = nodeResp.getId();

        Node n2 = new Node();
        n2.setName(nodeName);
        n2.setNodeType(nodeType);
        n2.setIsFolder(false);
        n2.setParentId(f2Id); // note: parent of the link (not where it is pointing)
        n2.setAspectNames(Collections.singletonList("cm:auditable"));
        n2.setProperties(props);
        n2.setCreatedByUser(expectedUser);
        n2.setModifiedByUser(expectedUser);

        n2.expected(nodeResp);

        // get node info
        response = getSingle(NodesEntityResource.class, n2Id, null, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);

        n2.expected(nodeResp);


        // update node - rename

        String updatedName = "f1 link renamed";

        Node nUpdate = new Node();
        nUpdate.setName(updatedName);

        response = put(URL_NODES, n1Id, toJsonAsStringNonNull(nUpdate), null, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        n1.setName(updatedName);
        n1.expected(nodeResp);


        // filtering, via where clause (nodeType + optionally including sub-types)

        List<String> linkIds = Arrays.asList(n1Id, n2Id);

        Map<String, String> params = new HashMap<>();
        params.put("where", "(nodeType='cm:link')");

        Paging paging = getPaging(0, Integer.MAX_VALUE);

        response = getAll(getNodeChildrenUrl(f2Id), paging, params, 200);
        List<Node> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        assertEquals(0, nodes.size());

        // filter by including sub-types - note: includesubtypes is case-insensitive

        params = new HashMap<>();
        params.put("where", "(nodeType='cm:link INCLUDESUBTYPES')");

        paging = getPaging(0, Integer.MAX_VALUE);

        response = getAll(getNodeChildrenUrl(f2Id), paging, params, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        assertEquals(linkIds.size(), nodes.size());
        assertTrue(linkIds.contains(nodes.get(0).getId()));
        assertTrue(linkIds.contains(nodes.get(1).getId()));

        params = new HashMap<>();
        params.put("where", "(nodeType='cm:link includeSubTypes')");

        paging = getPaging(0, Integer.MAX_VALUE);

        response = getAll(getNodeChildrenUrl(f2Id), paging, params, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        assertEquals(linkIds.size(), nodes.size());
        assertTrue(linkIds.contains(nodes.get(0).getId()));
        assertTrue(linkIds.contains(nodes.get(1).getId()));


        // delete link
        deleteNode(n1Id);

        // -ve test - delete - cannot delete nonexistent link
        deleteNode(n1Id, 404);

        // -ve test - create - name is mandatory
        Node invalid = new Node();
        invalid.setNodeType("cm:link");
        post(myChildrenUrl, toJsonAsStringNonNull(invalid), 400);

        // -ve test - create - node type is mandatory
        invalid = new Node();
        invalid.setName("my node");
        post(myChildrenUrl, toJsonAsStringNonNull(invalid), 400);

        // -ve test - create - unsupported node type
        invalid = new Node();
        invalid.setName("my node");
        invalid.setNodeType("sys:base");
        post(myChildrenUrl, toJsonAsStringNonNull(invalid), 400);

        // -ve test - create - duplicate name
        post(getNodeChildrenUrl(f2Id), toJsonAsStringNonNull(n2), 409);

        // -ve test - unknown nodeType when filtering
        params = new HashMap<>();
        params.put("where", "(nodeType='my:unknown'");
        getAll(getNodeChildrenUrl(f2Id), paging, params, 400);

        // -ver test - invalid node type localname format and suffix is not ' includesubtypes'
        params = new HashMap<>();
        params.put("where", "(nodeType='cm:link ')");
        getAll(getNodeChildrenUrl(f2Id), paging, params, 400);

        params = new HashMap<>();
        params.put("where", "(nodeType='cm:link blah')");
        getAll(getNodeChildrenUrl(f2Id), paging, params, 400);
    }

    /**
     * REPO-24
     * Tests upload fails with empty or invalid multipart/form-data.
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/children}
     */
    @Test
    public void testMultipartFormDataUpload() throws Exception
    {
        setRequestContext(user1);

        final String fileName = "quick-1.txt";
        final File file = getResourceFile(fileName);

        MultiPartBuilder multiPartBuilder = MultiPartBuilder.create()
                .setFileData(new FileData(fileName, file));
        MultiPartRequest reqBody = multiPartBuilder.build();

        // Upload with empty multipart
        post(getNodeChildrenUrl(Nodes.PATH_MY), "", null, reqBody.getContentType(), 400);

        // Upload with multipart with missing boundary e.g. multipart/form-data; boundary=7cgH0q1hSgrlmU7tUe8kGSWT4Un6aRH
        post(getNodeChildrenUrl(Nodes.PATH_MY), reqBody.getBody(), null, "multipart/form-data", 415);
    }


    /**
     * Tests create empty file.
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/children}
     */
    @Test
    public void testCreateEmptyFile() throws Exception
    {
        setRequestContext(user1);
        
        // create folder f0
        String folder0Name = "f0-testCreateEmptyFile-"+RUNID;
        String f0Id = createFolder(Nodes.PATH_MY, folder0Name).getId();

        UserInfo expectedUser = new UserInfo(user1);

        String postUrl = getNodeChildrenUrl(f0Id);

        Document d1 = new Document();
        d1.setName("d1.txt");
        d1.setNodeType(TYPE_CM_CONTENT);

        // create empty file
        HttpResponse response = post(postUrl, toJsonAsStringNonNull(d1), 201);
        Document documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        String d1Id = documentResp.getId();

        d1.setIsFolder(false);
        d1.setParentId(f0Id);
        d1.setAspectNames(Collections.singletonList("cm:auditable"));

        d1.setCreatedByUser(expectedUser);
        d1.setModifiedByUser(expectedUser);

        ContentInfo ciExpected = new ContentInfo();
        ciExpected.setMimeType("text/plain");
        ciExpected.setMimeTypeName("Plain Text");
        ciExpected.setSizeInBytes(0L);
        ciExpected.setEncoding("UTF-8");
        d1.setContent(ciExpected);

        d1.expected(documentResp);

        // create empty file with properties
        Map<String,Object> props = new HashMap<>();
        props.put("cm:title","my file title");
        props.put("cm:description","my file description");

        Document d2 = new Document();
        d2.setName("d2.txt");
        d2.setNodeType(TYPE_CM_CONTENT);
        d2.setProperties(props);

        response = post(postUrl, toJsonAsStringNonNull(d2), 201);

        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        d2.setIsFolder(false);
        d2.setParentId(f0Id);
        d2.setAspectNames(Arrays.asList("cm:auditable","cm:titled"));

        d2.setCreatedByUser(expectedUser);
        d2.setModifiedByUser(expectedUser);

        ciExpected = new ContentInfo();
        ciExpected.setMimeType("text/plain");
        ciExpected.setMimeTypeName("Plain Text");
        ciExpected.setSizeInBytes(0L);
        ciExpected.setEncoding("UTF-8");
        d2.setContent(ciExpected);

        d2.expected(documentResp);

        // create another empty file in a (partially existing) folder path
        Node n = new Node();
        n.setName("d3.txt");
        n.setNodeType(TYPE_CM_CONTENT);
        n.setRelativePath("/f1/f2");

        // create node
        response = post(getNodeChildrenUrl(f0Id), RestApiUtil.toJsonAsStringNonNull(n), 201);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        // check parent hierarchy ...
        response = getSingle(NodesEntityResource.class, documentResp.getId(), null, 200);
        Folder folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertEquals(folderResp.getName(),"d3.txt");

        response = getSingle(NodesEntityResource.class, folderResp.getParentId(), null, 200);
        folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertEquals(folderResp.getName(),"f2");

        response = getSingle(NodesEntityResource.class, folderResp.getParentId(), null, 200);
        folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertEquals(folderResp.getName(),"f1");

        response = getSingle(NodesEntityResource.class, folderResp.getParentId(), null, 200);
        folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertEquals(f0Id, folderResp.getId());

        // -ve test - name is mandatory
        Document invalid = new Document();
        invalid.setNodeType(TYPE_CM_CONTENT);
        post(postUrl, toJsonAsStringNonNull(invalid), 400);

        // -ve test - node type is mandatory
        invalid = new Document();
        invalid.setName("my file.txt");
        post(postUrl, toJsonAsStringNonNull(invalid), 400);

        // -ve test - invalid (model integrity exception)
        Document d3 = new Document();
        d3.setName("d3.txt");
        d3.setNodeType(TYPE_CM_CONTENT);
        post(getNodeChildrenUrl(d1Id), toJsonAsStringNonNull(d3), 422);

        // -ve test - unknown parent folder node id
        post(getNodeChildrenUrl(UUID.randomUUID().toString()), toJsonAsStringNonNull(d3), 404);

        // -ve test - duplicate name
        post(postUrl, toJsonAsStringNonNull(d1), 409);

        // Create a file with a duplicate name (d1.txt), but set the autoRename to true
        response = post(postUrl, toJsonAsStringNonNull(d1), "?"+Nodes.PARAM_AUTO_RENAME+"=true", 201);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertEquals("d1-1.txt", documentResp.getName());

        // Create a file with a duplicate name (d1.txt) again, but set the autoRename to true
        response = post(postUrl, toJsonAsStringNonNull(d1), "?"+Nodes.PARAM_AUTO_RENAME+"=true", 201);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertEquals("d1-2.txt", documentResp.getName());

        // Create a file with a duplicate name (d1-2.txt) again, but set the autoRename to true
        d1.setName("d1-2.txt");
        response = post(postUrl, toJsonAsStringNonNull(d1), "?"+Nodes.PARAM_AUTO_RENAME+"=true", 201);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertEquals("d1-2-1.txt", documentResp.getName());

        // -ve test - create a file with a duplicate name (d1-2.txt), but set the autoRename to false
        post(postUrl, toJsonAsStringNonNull(d1), "?"+Nodes.PARAM_AUTO_RENAME+"=false", 409);
    }

    @Test
    public void testUpdateNodeConcurrentlyUsingInMemoryBacked() throws Exception
    {
        // Less than its memory threshold ( 4 MB )
        updateNodeConcurrently(1024L);
    }

    @Test
    public void testUpdateNodeConcurrentlyUsingFileBacked() throws Exception
    {
        // Bigger than its memory threshold ( 5 > 4 MB )
        updateNodeConcurrently(5 * 1024 * 1024L);
    }

    private void updateNodeConcurrently(Long contentSize) throws Exception
    {
        setRequestContext(user1);

        // Create folder
        String folder0Name = "f0-testUpdateNodeConcurrently-" + RUNID;
        String f0Id = createFolder(Nodes.PATH_MY, folder0Name).getId();

        // Create empty file
        Document d1 = new Document();
        d1.setName("d1.txt");
        d1.setNodeType(TYPE_CM_CONTENT);

        Map params = new HashMap<>();
        params.put("majorVersion", "true");

        Document documentResp = createEmptyTextFile(f0Id, d1.getName(), params, 201);
        assertEquals("1.0", documentResp.getProperties().get("cm:versionLabel"));
        String docId = documentResp.getId();

        // Store the threads so that we can check if they are done
        List<Thread> threads = new ArrayList<Thread>();

        // Create threads
        for (int i = 0; i < 2; i++)
        {
            Runnable task = new UpdateNodeRunnable(docId, contentSize);

            Thread worker = new Thread(task);
            worker.setName(String.valueOf(i));
            worker.start();

            // Remember the thread for later usage
            threads.add(worker);
        }
        int running = 0;
        do
        {
            running = 0;
            for (Thread thread : threads)
            {
                if (thread.isAlive())
                {
                    running++;
                }
            }
        } while (running > 0);

        HttpResponse response = getSingle(URL_NODES, docId, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        assertTrue("File size is 0 bytes", documentResp.getContent().getSizeInBytes().intValue() > 0);

        // Download text content - by default with Content-Disposition header
        response = getSingle(NodesEntityResource.class, docId + "/content", null, 200);

        byte[] bytes = response.getResponseAsBytes();
        assertEquals(contentSize.intValue(), bytes.length);
    }

    private class UpdateNodeRunnable implements Runnable
    {
        private final String docId;
        private final Long contentSize;

        UpdateNodeRunnable(String docId, Long contentSize)
        {
            this.docId = docId;
            this.contentSize = contentSize;
        }

        @Override
        public void run()
        {
            setRequestContext(user1);

            Map<String, String> params = new HashMap<>();
            params.put("majorVersion", "true");

            Document documentResp = null;
            try
            {
                documentResp = updateTextFileWithRandomContent(docId, contentSize, params);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            assertTrue(documentResp.getAspectNames().contains("cm:versionable"));
            assertNotNull(documentResp.getProperties());

            assertEquals(contentSize, documentResp.getContent().getSizeInBytes());
        }
    }

    protected Document updateTextFileWithRandomContent(String contentId, Long contentSize, Map<String, String> params) throws Exception
    {
        return updateTextFileWithRandomContent(contentId, contentSize, params, 200);
    }

    protected Document updateTextFileWithRandomContent(String contentId, Long contentSize, Map<String, String> params, int expectedStatus) throws Exception
    {
        File txtFile = TempFileProvider.createTempFile(getClass().getSimpleName(), ".txt");
        RandomAccessFile file = new RandomAccessFile(txtFile.getPath(), "rw");
        file.setLength(contentSize);
        file.close();

        BinaryPayload payload = new BinaryPayload(txtFile);

        HttpResponse response = putBinary(getNodeContentUrl(contentId), payload, null, params, expectedStatus);
        if (expectedStatus != 200)
        {
            return null;
        }
        return RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
    }

    /**
     * Tests update node info (file or folder)
     * <p>PUT:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>}
     */
    @Test
    public void testUpdateNodeInfo() throws Exception
    {
        setRequestContext(user1);
        
        // create folder f0
        String folder0Name = "f0-testUpdateNodeInfo-"+RUNID;
        String f0Id = createFolder(Nodes.PATH_MY, folder0Name).getId();

        UserInfo expectedUser = new UserInfo(user1);

        String postUrl = getNodeChildrenUrl(f0Id);

        String folderName = "My Folder";

        // create folder
        Folder folderResp = createFolder(f0Id, folderName);

        String fId = folderResp.getId();

        Folder f1 = new Folder();
        f1.setName(folderName);
        f1.setNodeType(TYPE_CM_FOLDER);

        f1.setIsFolder(true);
        f1.setParentId(f0Id);
        f1.setAspectNames(Collections.singletonList("cm:auditable"));

        f1.setCreatedByUser(expectedUser);
        f1.setModifiedByUser(expectedUser);

        f1.expected(folderResp);

        // create empty file

        Document d1 = new Document();
        d1.setName("d1.txt");
        d1.setNodeType(TYPE_CM_CONTENT);

        HttpResponse response = post(postUrl, toJsonAsStringNonNull(d1), 201);
        Document documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        String dId = documentResp.getId();

        d1.setIsFolder(false);
        d1.setParentId(f0Id);
        d1.setAspectNames(Collections.singletonList("cm:auditable"));

        d1.setCreatedByUser(expectedUser);
        d1.setModifiedByUser(expectedUser);

        ContentInfo ciExpected = new ContentInfo();
        ciExpected.setMimeType("text/plain");
        ciExpected.setMimeTypeName("Plain Text");
        ciExpected.setSizeInBytes(0L);
        ciExpected.setEncoding("UTF-8");
        d1.setContent(ciExpected);

        d1.expected(documentResp);

        // update file - name (=> rename within current folder)

        Document dUpdate = new Document();
        dUpdate.setName("d1b.txt");

        response = put(URL_NODES, dId, toJsonAsStringNonNull(dUpdate), null, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        d1.setName("d1b.txt");
        d1.expected(documentResp);

        // update file - add some properties

        Map<String,Object> props = new HashMap<>();
        props.put("cm:title","my file title");
        props.put("cm:description","my file description");

        dUpdate = new Document();
        dUpdate.setProperties(props);

        response = put(URL_NODES, dId, toJsonAsStringNonNull(dUpdate), null, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        d1.setProperties(props);
        d1.setAspectNames(Arrays.asList("cm:auditable","cm:titled"));
        d1.expected(documentResp);

        // update file - add versionable aspect

        dUpdate = new Document();
        dUpdate.setAspectNames(Arrays.asList("cm:auditable","cm:titled","cm:versionable"));

        response = put(URL_NODES, dId, toJsonAsStringNonNull(dUpdate), null, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        d1.getProperties().put("cm:versionLabel","1.0");
        d1.getProperties().put("cm:versionType","MAJOR");
        d1.setAspectNames(Arrays.asList("cm:auditable","cm:titled","cm:versionable"));
        d1.expected(documentResp);

        response = getSingle(URL_NODES, dId, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        d1.getProperties().put("cm:versionLabel", "1.0");
        d1.getProperties().put("cm:versionType", "MAJOR");
        d1.expected(documentResp);

        // update file - remove titled aspect (and it's related aspect properties)

        dUpdate = new Document();
        dUpdate.setAspectNames(Arrays.asList("cm:auditable","cm:versionable"));

        response = put(URL_NODES, dId, toJsonAsStringNonNull(dUpdate), null, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        d1.getProperties().remove("cm:title");
        d1.getProperties().remove("cm:description");
        d1.setAspectNames(Arrays.asList("cm:auditable","cm:versionable"));
        d1.expected(documentResp);

        // update folder - rename and add some properties

        props = new HashMap<>();
        props.put("cm:title","my folder title");
        props.put("cm:description","my folder description");

        folderName = "My Updated Folder";
        Folder fUpdate = new Folder();
        fUpdate.setProperties(props);
        fUpdate.setName(folderName);

        response = put(URL_NODES, fId, toJsonAsStringNonNull(fUpdate), null, 200);
        folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);

        f1.setName(folderName);
        f1.setAspectNames(Arrays.asList("cm:auditable","cm:titled"));
        f1.setProperties(props);
        f1.expected(folderResp);

        // update folder - unset a property

        props = new HashMap<>();
        props.put("cm:title",null);

        fUpdate = new Folder();
        fUpdate.setProperties(props);

        response = put(URL_NODES, fId, toJsonAsStringNonNull(fUpdate), null, 200);
        folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);

        f1.getProperties().remove("cm:title");
        f1.expected(folderResp);

        // update folder - specialise node type

        fUpdate = new Folder();
        fUpdate.setNodeType("app:glossary");

        response = put(URL_NODES, fId, toJsonAsStringNonNull(fUpdate), null, 200);
        folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);

        f1.setNodeType("app:glossary");
        f1.expected(folderResp);
        
        {
            // test versioning for metadata-only updates
            
            Map params = new HashMap<>();
            params.put("majorVersion", "true");
            params.put("comment", "Initial empty file :-)");

            String fileName = "My File";
            Node nodeResp = createEmptyTextFile(f0Id, fileName, params, 201);
            assertEquals("1.0", nodeResp.getProperties().get("cm:versionLabel"));

            props = new HashMap<>();
            props.put("cm:title", "my file title");
            dUpdate = new Document();
            dUpdate.setProperties(props);

            response = put(URL_NODES, dId, toJsonAsStringNonNull(dUpdate), null, 200);
            nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
            assertEquals("1.0", nodeResp.getProperties().get("cm:versionLabel"));

            // turn-off auto-version on metadata-only updates (OOTB this is now false by default, as per MNT-12226)
            props = new HashMap<>();
            props.put("cm:autoVersionOnUpdateProps", true);
            dUpdate = new Document();
            dUpdate.setProperties(props);
            
            response = put(URL_NODES, dId, toJsonAsStringNonNull(dUpdate), null, 200);
            nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
            assertEquals("1.1", nodeResp.getProperties().get("cm:versionLabel"));
            
            props = new HashMap<>();
            props.put("cm:title","my file title 2");
            dUpdate = new Document();
            dUpdate.setProperties(props);
            
            response = put(URL_NODES, dId, toJsonAsStringNonNull(dUpdate), null, 200);
            nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
            assertEquals("1.2", nodeResp.getProperties().get("cm:versionLabel"));

            props = new HashMap<>();
            props.put("cm:title","my file title 3");
            dUpdate = new Document();
            dUpdate.setProperties(props);

            response = put(URL_NODES, dId, toJsonAsStringNonNull(dUpdate), null, 200);
            nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
            assertEquals("1.3", nodeResp.getProperties().get("cm:versionLabel"));

            // turn-off auto-version on metadata-only updates
            props = new HashMap<>();
            props.put("cm:autoVersionOnUpdateProps", false);
            dUpdate = new Document();
            dUpdate.setProperties(props);

            response = put(URL_NODES, dId, toJsonAsStringNonNull(dUpdate), null, 200);
            nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
            assertEquals("1.3", nodeResp.getProperties().get("cm:versionLabel"));

            props = new HashMap<>();
            props.put("cm:title","my file title 4");
            dUpdate = new Document();
            dUpdate.setProperties(props);

            response = put(URL_NODES, dId, toJsonAsStringNonNull(dUpdate), null, 200);
            nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
            assertEquals("1.3", nodeResp.getProperties().get("cm:versionLabel"));
        }

        // update folder(s) via well-known aliases rather than node id

        // note: as of now, the platform does allow a user to modify their home folder [this may change in the future, if so adjust the test accordingly]
        response = getSingle(URL_NODES, Nodes.PATH_MY, 200);
        Folder user1MyFolder = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        String user1MyFolderId = user1MyFolder.getId();

        String description = "my folder description "+RUNID;
        props = new HashMap<>();
        props.put("cm:description", description);

        fUpdate = new Folder();
        fUpdate.setProperties(props);

        response = put(URL_NODES, Nodes.PATH_MY, toJsonAsStringNonNull(fUpdate), null, 200);
        folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertEquals(description, folderResp.getProperties().get("cm:description"));

        setRequestContext(networkAdmin);

        props = new HashMap<>();
        props.put("cm:description", description);

        fUpdate = new Folder();
        fUpdate.setProperties(props);

        response = put(URL_NODES, Nodes.PATH_ROOT, toJsonAsStringNonNull(fUpdate), null, 200);
        folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertEquals(description, folderResp.getProperties().get("cm:description"));

        props = new HashMap<>();
        props.put("cm:description", description);

        fUpdate = new Folder();
        fUpdate.setProperties(props);

        response = put(URL_NODES, Nodes.PATH_SHARED, toJsonAsStringNonNull(fUpdate), null, 200);
        folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertEquals(description, folderResp.getProperties().get("cm:description"));


        setRequestContext(user1);

        // -ve test - fail on unknown property
        props = new HashMap<>();
        props.put("cm:xyz","my unknown property");
        dUpdate = new Document();
        dUpdate.setProperties(props);
        put(URL_NODES, dId, toJsonAsStringNonNull(dUpdate), null, 400);

        // -ve test - fail on unknown aspect
        List<String> aspects = new ArrayList<>(d1.getAspectNames());
        aspects.add("cm:unknownAspect");
        dUpdate = new Document();
        dUpdate.setAspectNames(aspects);
        put(URL_NODES, dId, toJsonAsStringNonNull(dUpdate), null, 400);

        // -ve test - duplicate name
        dUpdate = new Document();
        dUpdate.setName(folderName);
        put(URL_NODES, dId, toJsonAsStringNonNull(dUpdate), null, 409);

        // -ve test - unknown node id
        dUpdate = new Document();
        dUpdate.setName("some.txt");
        put(URL_NODES, UUID.randomUUID().toString(), toJsonAsStringNonNull(dUpdate), null, 404);

        // -ve test - generalise node type
        fUpdate = new Folder();
        fUpdate.setNodeType(TYPE_CM_FOLDER);
        put(URL_NODES, fId, toJsonAsStringNonNull(fUpdate), null, 400);

        // -ve test - try to move to a different parent using PUT (note: should use new POST /nodes/{nodeId}/move operation instead)

        folderResp = createFolder(f0Id, "folder 2");
        String f2Id = folderResp.getId();

        fUpdate = new Folder();
        fUpdate.setParentId(f2Id);
        put(URL_NODES, fId, toJsonAsStringNonNull(fUpdate), null, 400);

        // ok - if parent does not change
        fUpdate = new Folder();
        fUpdate.setParentId(f0Id);
        put(URL_NODES, fId, toJsonAsStringNonNull(fUpdate), null, 200);

        // -ve test - minor: error code if trying to update property with invalid format (REPO-473)
        props = new HashMap<>();
        props.put("exif:pixelYDimension", "my unknown property");
        fUpdate = new Folder();
        fUpdate.setProperties(props);
        put(URL_NODES, f2Id, toJsonAsStringNonNull(fUpdate), null, 400);

        // -ve test - minor: error code if trying to update property with invalid format (REPO-1635)
        props = new HashMap<>();
        props.put("exif:dateTimeOriginal", "25-11-2016");
        fUpdate = new Folder();
        fUpdate.setProperties(props);
        put(URL_NODES, f2Id, toJsonAsStringNonNull(fUpdate), null, 400);
        
        // +ve test - try again with valid formats (REPO-473, REPO-1635)
        props = new HashMap<>();
        props.put("exif:pixelYDimension", "123");
        props.put("exif:dateTimeOriginal", "2016-11-21T16:26:19.037+0000");
        fUpdate = new Folder();
        fUpdate.setProperties(props);
        put(URL_NODES, f2Id, toJsonAsStringNonNull(fUpdate), null, 200);
        
        
        // -ve test - non-admin cannot modify root (Company Home) folder
        props = new HashMap<>();
        props.put("cm:description", "my folder description");
        fUpdate = new Folder();
        fUpdate.setProperties(props);
        put(URL_NODES, Nodes.PATH_ROOT, toJsonAsStringNonNull(fUpdate), null, 403);

        // -ve test - non-admin cannot modify "Shared" folder
        props = new HashMap<>();
        props.put("cm:description", "my folder description");
        fUpdate = new Folder();
        fUpdate.setProperties(props);
        put(URL_NODES, Nodes.PATH_SHARED, toJsonAsStringNonNull(fUpdate), null, 403);

        setRequestContext(user2);

        // -ve test - user cannot modify another user's home folder
        props = new HashMap<>();
        props.put("cm:description", "my folder description");
        fUpdate = new Folder();
        fUpdate.setProperties(props);
        put(URL_NODES, user1MyFolderId, toJsonAsStringNonNull(fUpdate), null, 403);

    }

    /**
     * Tests update owner (file or folder)
     * <p>PUT:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>}
     */
    @Test
    public void testUpdateOwner() throws Exception
    {
        setRequestContext(user1);
        
        // create folder f0
        String folderName = "f0-testUpdateOwner-"+RUNID;
        Folder folderResp = createFolder(Nodes.PATH_SHARED, folderName);
        String f0Id = folderResp.getId();

        assertNull(user1, folderResp.getProperties()); // owner is implied

        // explicitly set owner to oneself
        Map<String, Object> props = new HashMap<>();
        props.put(PROP_OWNER, user1);
        Folder fUpdate = new Folder();
        fUpdate.setProperties(props);

        HttpResponse response = put(URL_NODES, f0Id, toJsonAsStringNonNull(fUpdate), null, 200);
        folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);

        assertEquals(user1, ((Map)folderResp.getProperties().get(PROP_OWNER)).get("id"));

        // create doc d1
        String d1Name = "content1 " + RUNID;
        String d1Id = createTextFile(f0Id, d1Name, "The quick brown fox jumps over the lazy dog.").getId();

        // get node info
        response = getSingle(NodesEntityResource.class, d1Id, null, 200);
        Document documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        // note: owner is implied
        assertEquals(2, documentResp.getProperties().size());
        assertEquals("1.0", documentResp.getProperties().get("cm:versionLabel"));
        assertEquals("MAJOR", documentResp.getProperties().get("cm:versionType"));

        props = new HashMap<>();
        props.put(PROP_OWNER, user1);
        Document dUpdate = new Document();
        dUpdate.setProperties(props);

        response = put(URL_NODES, d1Id, toJsonAsStringNonNull(dUpdate), null, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        assertEquals(user1, ((Map)documentResp.getProperties().get(PROP_OWNER)).get("id"));

        // -ve test - cannot set owner to a nonexistent user

        props = new HashMap<>();
        props.put(PROP_OWNER, "unknownusernamedoesnotexist");
        dUpdate = new Document();
        dUpdate.setProperties(props);

        put(URL_NODES, d1Id, toJsonAsStringNonNull(dUpdate), null, 400);

        setRequestContext(user2);

        response = getSingle(URL_NODES, d1Id, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        assertEquals(user1, ((Map)documentResp.getProperties().get(PROP_OWNER)).get("id"));

        // -ve test - cannot take/change ownership

        props = new HashMap<>();
        props.put(PROP_OWNER, user2);
        dUpdate = new Document();
        dUpdate.setProperties(props);

        put(URL_NODES, d1Id, toJsonAsStringNonNull(dUpdate), null, 403);

        props = new HashMap<>();
        props.put(PROP_OWNER, user1);
        dUpdate = new Document();
        dUpdate.setProperties(props);

        put(URL_NODES, d1Id, toJsonAsStringNonNull(dUpdate), null, 403);

        setRequestContext(user1);

        props = new HashMap<>();
        props.put(PROP_OWNER, user2);
        dUpdate = new Document();
        dUpdate.setProperties(props);

        response = put(URL_NODES, d1Id, toJsonAsStringNonNull(dUpdate), null, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        assertEquals(user2, ((Map)documentResp.getProperties().get(PROP_OWNER)).get("id"));

        setRequestContext(user2);

        response = getSingle(URL_NODES, d1Id, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        assertEquals(user2, ((Map)documentResp.getProperties().get(PROP_OWNER)).get("id"));

        // -ve test - user2 cannot delete the test folder/file - TODO is that expected ?
        setRequestContext(user2);
        deleteNode(f0Id, 403);
        
        setRequestContext(user1);
        deleteNode(f0Id);
    }

    /**
     * Tests update type
     * <p>PUT:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>}
     */
    @Test
    public void testUpdateType() throws Exception
    {
        setRequestContext(user1);

        // create folder f0
        String folderName = "f0-testUpdateOwner-"+RUNID;
        Folder folderResp = createFolder(Nodes.PATH_SHARED, folderName);
        String f0Id = folderResp.getId();

        assertNull(user1, folderResp.getProperties()); // owner is implied

        // non-update case
        Folder fUpdate = new Folder();
        fUpdate.setNodeType(folderResp.getNodeType());

        HttpResponse response = put(URL_NODES, f0Id, toJsonAsStringNonNull(fUpdate), null, 200);
        folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);

        assertEquals(TYPE_CM_FOLDER, folderResp.getNodeType());

        // set type to an incompatible type
        fUpdate.setNodeType(TYPE_CM_CONTENT);
        put(URL_NODES, f0Id, toJsonAsStringNonNull(fUpdate), null, 400);

        // set type to system folder (a special, unsupported case)
        fUpdate.setNodeType("cm:systemfolder");
        put(URL_NODES, f0Id, toJsonAsStringNonNull(fUpdate), null, 400);

        // set type to supported folder sub-type
        // (none exists in contentModel, so forumsModel it is)
        fUpdate.setNodeType("fm:forums");
        response = put(URL_NODES, f0Id, toJsonAsStringNonNull(fUpdate), null, 200);
        folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertEquals("fm:forums", folderResp.getNodeType());

        // set type to a generalised type (unsupported case)
        fUpdate.setNodeType(TYPE_CM_FOLDER);
        put(URL_NODES, f0Id, toJsonAsStringNonNull(fUpdate), null, 400);
    }

    /**
     * Tests update file content
     * <p>PUT:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/content}
     */
    @Test
    public void testUpdateFileWithBinaryUpload() throws Exception
    {
        setRequestContext(user1);
        
        String myNodeId = getMyNodeId();

        String folderName = "f1-testUpdateFileWithBinaryUpload-"+RUNID;
        Folder folderResp = createFolder(myNodeId, folderName);
        String f1_nodeId = folderResp.getId();
        
        String anoNodeName = "another";
        createFolder(f1_nodeId, anoNodeName);

        Document doc = new Document();
        final String docName = "testdoc.txt";
        doc.setName(docName);
        doc.setNodeType(TYPE_CM_CONTENT);
        doc.setProperties(Collections.singletonMap("cm:title", (Object)"test title"));
        ContentInfo contentInfo = new ContentInfo();
        doc.setContent(contentInfo);

        // create an empty file within F1 folder
        HttpResponse response = post(getNodeChildrenUrl(f1_nodeId), toJsonAsStringNonNull(doc), 201);
        Document docResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        assertEquals(docName, docResp.getName());
        assertNotNull(docResp.getContent());
        assertEquals(0, docResp.getContent().getSizeInBytes().intValue());
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, docResp.getContent().getMimeType());
        // Default encoding
        assertEquals("UTF-8", docResp.getContent().getEncoding());

        // Update the empty node's content
        String content = "The quick brown fox jumps over the lazy dog.";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
        File txtFile = TempFileProvider.createTempFile(inputStream, getClass().getSimpleName(), ".txt");
        BinaryPayload payload = new BinaryPayload(txtFile);

        // Try to update a folder!
        putBinary(getNodeContentUrl(f1_nodeId), payload, null, null, 400);

        // Try to update a non-existent file
        putBinary(getNodeContentUrl(UUID.randomUUID().toString()), payload, null, null, 404);

        final String url = getNodeContentUrl(docResp.getId());

        // Update the empty file
        response = putBinary(url, payload, null, null, 200);
        docResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertEquals(docName, docResp.getName());
        assertNotNull(docResp.getId());
        assertNotNull(docResp.getCreatedAt());
        assertNotNull(docResp.getCreatedByUser());
        assertNotNull(docResp.getModifiedAt());
        assertNotNull(docResp.getModifiedByUser());
        assertFalse(docResp.getIsFolder());
        assertTrue(docResp.getIsFile());
        assertNull(docResp.getIsLink());
        assertEquals(TYPE_CM_CONTENT, docResp.getNodeType());
        assertNotNull(docResp.getParentId());
        assertEquals(f1_nodeId, docResp.getParentId());
        assertNotNull(docResp.getProperties());
        assertNotNull(docResp.getAspectNames());
        contentInfo = docResp.getContent();
        assertNotNull(contentInfo);
        assertNotNull(contentInfo.getEncoding());
        assertTrue(contentInfo.getSizeInBytes() > 0);
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, contentInfo.getMimeType());
        assertNotNull(contentInfo.getMimeTypeName());
        assertEquals("ISO-8859-1", contentInfo.getEncoding());
        // path is not part of the default response
        assertNull(docResp.getPath());

        // Download the file
        response = getSingle(url, user1, null, 200);
        assertEquals(content, response.getResponse());

        // Update the node's content again. Also make the response return the path!
        content = "The quick brown fox jumps over the lazy dog updated !";
        inputStream = new ByteArrayInputStream(content.getBytes());
        txtFile = TempFileProvider.createTempFile(inputStream, getClass().getSimpleName(), ".txt");
        payload = new BinaryPayload(txtFile);

        response = putBinary(url + "?include=path", payload, null, null, 200);
        docResp = jacksonUtil.parseEntry(response.getJsonResponse(), Document.class);
        assertEquals(docName, docResp.getName());
        assertNotNull(docResp.getContent());
        assertTrue(docResp.getContent().getSizeInBytes().intValue() > 0);
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, docResp.getContent().getMimeType());
        assertEquals("ISO-8859-1", docResp.getContent().getEncoding());
        PathInfo pathInfo = docResp.getPath();
        assertNotNull(pathInfo);
        assertTrue(pathInfo.getIsComplete());
        List<ElementInfo> pathElements = pathInfo.getElements();
        assertNotNull(pathElements);
        assertTrue(pathElements.size() > 0);
        // check the last element is F1
        assertEquals(folderResp.getName(), pathElements.get(pathElements.size() - 1).getName());

        // Download the file
        response = getSingle(url, user1, null, 200);
        assertEquals(content, response.getResponse());
        
        // Update the node's content again. Also rename the file !
        content = "The quick brown fox jumps over the lazy dog updated again !";
        inputStream = new ByteArrayInputStream(content.getBytes());
        txtFile = TempFileProvider.createTempFile(inputStream, getClass().getSimpleName(), ".txt");
        payload = new BinaryPayload(txtFile);

        String docName2 = "hello-world.txt";
        Map params = new HashMap<>();
        params.put(Nodes.PARAM_NAME, docName2);
        response = putBinary(url, payload, null, params, 200);
        docResp = jacksonUtil.parseEntry(response.getJsonResponse(), Document.class);
        assertEquals(docName2, docResp.getName());
        
        // Download the file
        response = getSingle(url, user1, null, 200);
        assertEquals(content, response.getResponse());

        // -ve - optional "name" is invalid
        params = new HashMap<>();
        params.put(Nodes.PARAM_NAME, "hello/world.txt");
        payload = new BinaryPayload(txtFile);
        putBinary(url, payload, null, params, 422);
        
        // -ve - optional "name" already exists ...
        params = new HashMap<>();
        params.put(Nodes.PARAM_NAME, anoNodeName);
        payload = new BinaryPayload(txtFile);
        putBinary(url, payload, null, params, 409);

        // -ve - try to  update content using multi-part form data
        payload = new BinaryPayload(txtFile, "multipart/form-data", null);
        putBinary(url, payload, null, null, 415);

        // -ve - try to invalid media type argument (when parsing request)
        payload = new BinaryPayload(txtFile, "/jpeg", null);
        putBinary(url, payload, null, null, 415);
    }

    /**
     * Tests download of file/content.
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/content}
     */
    @Test
    public void testDownloadFileContent() throws Exception
    {
        setRequestContext(user1);

        //
        // Test plain text
        //

        String fileName = "quick-1.txt";
        File file = getResourceFile(fileName);

        MultiPartBuilder multiPartBuilder = MultiPartBuilder.create()
                .setFileData(new FileData(fileName, file));
        MultiPartRequest reqBody = multiPartBuilder.build();

        // Upload text content
        HttpResponse response = post(getNodeChildrenUrl(Nodes.PATH_MY), reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        String contentNodeId = document.getId();

        // Check the upload response
        assertEquals(fileName, document.getName());
        ContentInfo contentInfo = document.getContent();
        assertNotNull(contentInfo);
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, contentInfo.getMimeType());

        // Download text content - by default with Content-Disposition header
        response = getSingle(NodesEntityResource.class, contentNodeId + "/content", null, 200);

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
        getSingle(getNodeContentUrl(contentNodeId), null, null, headers, 304);

        // Update the content to change the node's modified date
        Document docUpdate = new Document();
        docUpdate.setProperties(Collections.singletonMap("cm:description", (Object) "desc updated!"));
        // Wait a second then update, as the dates will be rounded to
        // ignore millisecond when checking for If-Modified-Since
        Thread.sleep(1000L);
        response = put(URL_NODES, contentNodeId, toJsonAsStringNonNull(docUpdate), null, 200);
        Document updatedDocument = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertEquals(contentNodeId, updatedDocument.getId());

        // The requested "If-Modified-Since" date is older than node's modified date
        response = getSingle(getNodeContentUrl(contentNodeId), null, null, headers, 200);
        responseHeaders = response.getHeaders();
        assertNotNull(responseHeaders);
        assertNotNull(responseHeaders.get("Cache-Control"));
        assertNotNull(responseHeaders.get("Expires"));
        String newLastModifiedHeader = responseHeaders.get(LAST_MODIFIED_HEADER);
        assertNotNull(newLastModifiedHeader);
        assertNotEquals(lastModifiedHeader, newLastModifiedHeader);

        //
        // Test binary (eg. PDF)
        //

        fileName = "quick.pdf";
        file = getResourceFile(fileName);
        byte[] originalBytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));

        multiPartBuilder = MultiPartBuilder.create()
                .setFileData(new FileData(fileName, file));
        reqBody = multiPartBuilder.build();

        // Upload binary content
        response = post(getNodeChildrenUrl(Nodes.PATH_MY), reqBody.getBody(), null, reqBody.getContentType(), 201);
        document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        contentNodeId = document.getId();

        // Check the upload response
        assertEquals(fileName, document.getName());
        contentInfo = document.getContent();
        assertNotNull(contentInfo);
        assertEquals(MimetypeMap.MIMETYPE_PDF, contentInfo.getMimeType());

        // Download binary content (as bytes) - without Content-Disposition header (attachment=false)
        Map<String, String> params = new LinkedHashMap<>();
        params.put("attachment", "false");

        response = getSingle(NodesEntityResource.class, contentNodeId + "/content", params, 200);
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
        getSingle(getNodeContentUrl(contentNodeId), null, null, headers, 304);
    }

    /**
     * Tests download of file/content using backed temp file for streaming.
     * <p>
     * GET:
     * </p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/content}
     */
    @Test
    public void testDownloadFileContentUsingTempFile() throws Exception
    {
        setRequestContext(user1);

        // This should be grater then TempOutputStream.DEFAULT_MEMORY_THRESHOLD
        Long contentSize = 5 * 1024 * 1024L;
        String fileName = "tempFile.txt";

        File file = null;
        try
        {
            file = TempFileProvider.createTempFile(getClass().getSimpleName(), ".txt");
            RandomAccessFile rndFile = new RandomAccessFile(file.getPath(), "rw");
            rndFile.setLength(contentSize);
            rndFile.close();

            MultiPartBuilder multiPartBuilder = MultiPartBuilder.create().setFileData(new FileData(fileName, file));
            MultiPartRequest reqBody = multiPartBuilder.build();

            // Upload text content
            HttpResponse response = post(getNodeChildrenUrl(Nodes.PATH_MY), reqBody.getBody(), null, reqBody.getContentType(), 201);
            Document document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

            String contentNodeId = document.getId();

            // Check the upload response
            assertEquals(fileName, document.getName());
            ContentInfo contentInfo = document.getContent();
            assertNotNull(contentInfo);
            assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, contentInfo.getMimeType());

            // Download text content
            response = getSingle(NodesEntityResource.class, contentNodeId + "/content", null, 200);

            byte[] bytes = response.getResponseAsBytes();
            assertEquals(contentSize.intValue(), bytes.length);
        }
        finally
        {
            file.delete();
        }
    }

    /**
     * Tests download of file/content - basic read permission
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/content}
     */
    @Test
    public void testDownloadFileContentReadPermission() throws Exception
    {
        setRequestContext(user1);

        String fileName = "quick-1.txt";
        File file = getResourceFile(fileName);

        MultiPartBuilder multiPartBuilder = MultiPartBuilder.create()
                .setFileData(new FileData(fileName, file));
        MultiPartRequest reqBody = multiPartBuilder.build();

        // Upload text content
        HttpResponse response = post(getNodeChildrenUrl(Nodes.PATH_MY), reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        String contentNodeId = document.getId();

        // Download text content
        response = getSingle(NodesEntityResource.class, contentNodeId+"/content", null, 200);
        String textContent = response.getResponse();
        assertEquals("The quick brown fox jumps over the lazy dog", textContent);

        // Also test versions endpoint (1.0 in this case)
        response = getSingle(NodesEntityResource.class, contentNodeId+"/versions/1.0/content", null, 200);
        textContent = response.getResponse();
        assertEquals("The quick brown fox jumps over the lazy dog", textContent);

        // -ve test: user2 does not have read permission
        setRequestContext(user2);
        getSingle(NodesEntityResource.class, contentNodeId+"/content", null, 403);
        getSingle(NodesEntityResource.class, contentNodeId+"/versions/1.0/content", null, 403);

        // add Consumer (~ Read) permission
        setRequestContext(user1);

        Document dUpdate = new Document();
        NodePermissions nodePermissions = new NodePermissions();
        List<NodePermissions.NodePermission> locallySetPermissions = new ArrayList<>();
        locallySetPermissions.add(new NodePermissions.NodePermission(user2, PermissionService.CONSUMER, AccessStatus.ALLOWED.toString()));
        nodePermissions.setLocallySet(locallySetPermissions);
        dUpdate.setPermissions(nodePermissions);

        // update node
        response = put(URL_NODES, contentNodeId, toJsonAsStringNonNull(dUpdate), null, 200);

        setRequestContext(user2);

        // Download text content
        response = getSingle(NodesEntityResource.class, contentNodeId+"/content", null, 200);
        textContent = response.getResponse();
        assertEquals("The quick brown fox jumps over the lazy dog", textContent);

        // Also test versions endpoint (1.0 in this case)
        response = getSingle(NodesEntityResource.class, contentNodeId+"/versions/1.0/content", null, 200);
        textContent = response.getResponse();
        assertEquals("The quick brown fox jumps over the lazy dog", textContent);
    }

    @Test
    public void testGetNodeWithEmptyProperties() throws Exception
    {
        setRequestContext(user1);

        String myNodeId = getMyNodeId();

        // create folder f1
        Folder folderResp = createFolder(myNodeId, "fld1_" + RUNID);
        String f1Id = folderResp.getId();

        String nodeName = "f1 link";
        String nodeType = "app:folderlink";
        String propertyName = "cm:destination";

        Map<String, Object> props = new HashMap<>();
        props.put(propertyName, "");

        Node nodeResp = createNode(f1Id, nodeName, nodeType, props);
        String nodeId = nodeResp.getId();

        Node n1 = new Node();
        n1.setName(nodeName);
        n1.setNodeType(nodeType);
        n1.setIsFolder(true);
        // note: parent of the link (not where it is pointing)
        n1.setParentId(f1Id);
        n1.setAspectNames(Collections.singletonList("cm:auditable"));
        // Empty (zero length) string values are considered to be
        // null values, and will be represented the same as null
        // values (i.e. by non-existence of the property).
        n1.setProperties(null);

        // Check create response.
        n1.expected(nodeResp);

        HttpResponse httpResponse = getSingle(NodesEntityResource.class, nodeId, null, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(httpResponse.getJsonResponse(), Node.class);

        // Check get response.
        n1.expected(nodeResp);
    }

    /**
     * Tests optional lookup of Allowable Operations (eg. when getting node info, listing node children, ...)
     *
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/children?include=allowableOperations}
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>?include=allowableOperations}
     */
    @Test
    public void testAllowableOps() throws Exception
    {
        // as user1 ...
        setRequestContext(user1);

        String rootNodeId = getRootNodeId();
        String sharedNodeId = getSharedNodeId();

        Map params = new HashMap<>();
        params.put(Nodes.PARAM_RELATIVE_PATH, "/Sites");
        HttpResponse response = getSingle(NodesEntityResource.class, rootNodeId, params, 200);
        Node nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        String sitesNodeId = nodeResp.getId();

        params = new HashMap<>();
        params.put(Nodes.PARAM_RELATIVE_PATH, "/Data Dictionary");
        response = getSingle(NodesEntityResource.class, rootNodeId, params, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        String ddNodeId = nodeResp.getId();


        params = new HashMap<>();
        params.put("include", "allowableOperations");

        response = getSingle(NodesEntityResource.class, rootNodeId, params, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNull(nodeResp.getAllowableOperations());

        response = getSingle(NodesEntityResource.class, sharedNodeId, params, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNotNull(nodeResp.getAllowableOperations());
        assertEquals(1, nodeResp.getAllowableOperations().size());
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_CREATE));

        // -ve
        deleteNode(sharedNodeId, 403);

        response = getSingle(NodesEntityResource.class, getMyNodeId(), params, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNotNull(nodeResp.getAllowableOperations());
        assertEquals(4, nodeResp.getAllowableOperations().size());
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_DELETE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_CREATE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_UPDATE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_UPDATE_PERMISSIONS));

        // create folder
        nodeResp = createFolder(sharedNodeId, "folder 1 - "+RUNID);
        String folderId = nodeResp.getId();
        assertNull(nodeResp.getAllowableOperations());

        response = getSingle(NodesEntityResource.class, folderId, null, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNull(nodeResp.getAllowableOperations());

        response = getSingle(NodesEntityResource.class, folderId, params, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNotNull(nodeResp.getAllowableOperations());
        assertEquals(4, nodeResp.getAllowableOperations().size());
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_DELETE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_CREATE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_UPDATE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_UPDATE_PERMISSIONS));

        // create file
        nodeResp = createTextFile(folderId, "my file - "+RUNID+".txt", "The quick brown fox jumps over the lazy dog");
        String fileId = nodeResp.getId();
        assertNull(nodeResp.getAllowableOperations());

        response = getSingle(NodesEntityResource.class, fileId, null, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNull(nodeResp.getAllowableOperations());

        // a file - no create
        response = getSingle(NodesEntityResource.class, fileId, params, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNotNull(nodeResp.getAllowableOperations());
        assertEquals(3, nodeResp.getAllowableOperations().size());
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_DELETE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_UPDATE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_UPDATE_PERMISSIONS));


        // as user2 ...
        setRequestContext(user2);

        response = getSingle(NodesEntityResource.class, folderId, params, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNotNull(nodeResp.getAllowableOperations());
        assertEquals(1, nodeResp.getAllowableOperations().size());
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_CREATE));

        // -ve
        deleteNode(folderId, 403);

        response = getSingle(NodesEntityResource.class, fileId, params, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNull(nodeResp.getAllowableOperations());

        // -ve
        deleteNode(fileId, 403);

        // as admin ...
        setRequestContext(networkAdmin);

        response = publicApiClient.get(NodesEntityResource.class, folderId, null, params);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNotNull(nodeResp.getAllowableOperations());
        assertEquals(4, nodeResp.getAllowableOperations().size());
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_DELETE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_CREATE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_UPDATE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_UPDATE_PERMISSIONS));

        // a file - no create
        response = publicApiClient.get(NodesEntityResource.class, fileId, null, params);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNotNull(nodeResp.getAllowableOperations());
        assertEquals(3, nodeResp.getAllowableOperations().size());
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_DELETE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_UPDATE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_UPDATE_PERMISSIONS));

        response = publicApiClient.get(NodesEntityResource.class, sharedNodeId, null, params);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNotNull(nodeResp.getAllowableOperations());
        assertEquals(4, nodeResp.getAllowableOperations().size());
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_CREATE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_UPDATE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_DELETE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_UPDATE_PERMISSIONS));

        // Company Home - no delete
        response = publicApiClient.get(NodesEntityResource.class, rootNodeId, null, params);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNotNull(nodeResp.getAllowableOperations());
        assertEquals(3, nodeResp.getAllowableOperations().size());
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_CREATE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_UPDATE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_UPDATE_PERMISSIONS));

        // -ve
        deleteNode(rootNodeId, 403);

        // Sites - no delete
        response = publicApiClient.get(NodesEntityResource.class, sitesNodeId, null, params);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNotNull(nodeResp.getAllowableOperations());
        assertEquals(3, nodeResp.getAllowableOperations().size());
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_CREATE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_UPDATE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_UPDATE_PERMISSIONS));

        // -ve
        deleteNode(sitesNodeId, 403);

        // Data Dictionary - no delete
        response = publicApiClient.get(NodesEntityResource.class, ddNodeId, null, params);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNotNull(nodeResp.getAllowableOperations());
        assertEquals(3, nodeResp.getAllowableOperations().size());
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_CREATE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_UPDATE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_UPDATE_PERMISSIONS));

        // -ve
        deleteNode(ddNodeId, 403);

        publicApiClient.setRequestContext(null);

        // as user1 ...
        String userId = user1;
        setRequestContext(userId);

        response = getSingle("sites", tSiteId, null, null, 200);
        Site siteResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Site.class);
        String siteNodeId = siteResp.getGuid();

        response = getSingle(NodesEntityResource.class, siteNodeId, params, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertEquals(userId, nodeResp.getCreatedByUser().getId());
        assertNotNull(nodeResp.getAllowableOperations());
        assertEquals(3, nodeResp.getAllowableOperations().size());
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_CREATE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_UPDATE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_UPDATE_PERMISSIONS));

        // -ve
        deleteNode(siteNodeId, 403);

        setRequestContext(user1);

        // fix for REPO-514 (NPE for a node that was neither a file/document nor a folder)
        Node n = new Node();
        n.setName("o1");
        n.setNodeType(TYPE_CM_OBJECT);
        response = post(getNodeChildrenUrl(folderId), toJsonAsStringNonNull(n), 201);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        String o1Id = nodeResp.getId();

        params = new HashMap<>();
        params.put("include", "allowableOperations");
        response = getSingle(NodesEntityResource.class, o1Id, params, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNotNull(nodeResp.getAllowableOperations());

        // some cleanup
        deleteNode(folderId, true, 204);
    }

    /**
     * Tests lock of a node
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/lock}
     */
   @Test
    public void testLock() throws Exception
    {
        setRequestContext(user1);

        // create folder
        Folder folderResp = createFolder(Nodes.PATH_MY, "folder" + RUNID);
        String folderId = folderResp.getId();
        
        // try to lock the folder and check that is not allowed
        LockInfo lockInfo = new LockInfo();
        lockInfo.setTimeToExpire(60);
        lockInfo.setType("FULL");
        lockInfo.setLifetime("PERSISTENT");
        HttpResponse response = post(getNodeOperationUrl(folderId, "lock"), toJsonAsStringNonNull(lockInfo), null, 400);
        
        // create document d1
        String d1Name = "content" + RUNID + "_1l";
        Document d1 = createTextFile(folderId, d1Name, "The quick brown fox jumps over the lazy dog 1.");
        String d1Id = d1.getId();
        
        // lock d1 document
        lockInfo = new LockInfo();
        lockInfo.setTimeToExpire(30);
        lockInfo.setType("FULL");
        lockInfo.setLifetime("PERSISTENT");
        response = post(getNodeOperationUrl(d1Id, "lock"), toJsonAsStringNonNull(lockInfo), null, 200);
        Document documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        assertEquals(d1Name, documentResp.getName());
        assertEquals(d1Id, documentResp.getId());
        assertEquals(LockType.READ_ONLY_LOCK.toString(), documentResp.getProperties().get("cm:lockType"));
        assertNotNull(documentResp.getProperties().get("cm:lockOwner"));
        assertNull(documentResp.getIsLocked());
        
        // invalid test - delete a locked node
        deleteNode(d1Id, true, 409);
        
        // wait for expiration time set to pass and delete node
		TimeUnit.SECONDS.sleep(30);
		deleteNode(d1Id, true, 204);
       
        // create doc d2
        String d2Name = "content" + RUNID + "_2l";
        Document d2 = createTextFile(folderId, d2Name, "The quick brown fox jumps over the lazy dog 2.");
        String d2Id = d2.getId();

        response = getSingle(URL_NODES, d2Id, null, null, 200);
        Node node = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNull(node.getProperties().get("cm:lockType"));
        assertNull(node.getProperties().get("cm:lockOwner"));
        assertNull(node.getIsLocked());

        Map<String, String> params = Collections.singletonMap("include", "isLocked");
        response = getSingle(URL_NODES, d2Id, params, null, 200);
        node = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNull(node.getProperties().get("cm:lockType"));
        assertNull(node.getProperties().get("cm:lockOwner"));
        assertFalse(node.getIsLocked());

        lockInfo = new LockInfo();
        lockInfo.setTimeToExpire(60);
        lockInfo.setType("FULL");
        lockInfo.setLifetime("PERSISTENT");

        response = post(getNodeOperationUrl(d2Id, "lock"), toJsonAsStringNonNull(lockInfo), null, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        assertEquals(d2Name, documentResp.getName());
        assertEquals(d2Id, documentResp.getId());
        assertEquals(LockType.READ_ONLY_LOCK.toString(), documentResp.getProperties().get("cm:lockType"));
        assertNotNull(documentResp.getProperties().get("cm:lockOwner"));
        assertNull(documentResp.getIsLocked());

        unlock(d2Id);
        // Empty lock body, the default values are used
        post(getNodeOperationUrl(d2Id, "lock"), EMPTY_BODY, null, 200);

        // Lock on already locked node
        post(getNodeOperationUrl(d2Id, "lock"), toJsonAsStringNonNull(lockInfo), null, 200);

        // Test delete on a folder which contains a locked node - NodeLockedException
        deleteNode(folderId, true, 409);

        // Content update on a locked node
        updateTextFile(d2Id, "Updated text", null, 409);
        unlock(d2Id);

        // Test lock file
        // create folder
        String folderAName = "folder" + RUNID + "_A";
        Folder folderA = createFolder(Nodes.PATH_MY, folderAName);
        String folderAId = folderA.getId();

        // create a file in the folderA
        Document dA1 = createTextFile(folderAId, "content" + RUNID + "_A1", "A1 content");
        String dA1Id = dA1.getId();

        lockInfo = new LockInfo();
        lockInfo.setTimeToExpire(60);
        lockInfo.setType("ALLOW_OWNER_CHANGES");
        lockInfo.setLifetime("EPHEMERAL");

        // lock the file
        post(getNodeOperationUrl(dA1Id, "lock"), toJsonAsStringNonNull(lockInfo), null, 200);

        params = Collections.singletonMap("include", "aspectNames,properties,isLocked");
        response = getSingle(URL_NODES, dA1Id, params, null, 200);
        node = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertTrue(node.getIsLocked());
        // note: this can be updated by the owner since the lock type is "ALLOW_OWNER_CHANGES"
        updateTextFile(node.getId(), "Updated text", null, 200);

        // Lock body properties - boundary values
        Document dB1 = createTextFile(folderAId, "content" + RUNID + "_dB1", "dB1 content");
        String dB1Id = dB1.getId();

        lockInfo = new LockInfo();
        lockInfo.setTimeToExpire(-100); // values lower than 0 are considered as no expiry time
        post(getNodeOperationUrl(dB1Id, "lock"), toJsonAsStringNonNull(lockInfo), null, 200);

        // Lock node by a different user than the owner
        setRequestContext(user1);
        Folder folder1Resp = createFolder(Nodes.PATH_SHARED, "folder1" + RUNID);
        String folder1Id = folder1Resp.getId();

        String f1d1Name = "content f1" + RUNID + "_1l";
        Document f1d1 = createTextFile(folder1Id, f1d1Name, "The quick brown fox jumps over the lazy dog 1.");
        String f1d1Id = f1d1.getId();
        // use admin for now (might be better to use a user with given WRITE permission)
        setRequestContext(networkAdmin);
        post(getNodeOperationUrl(f1d1Id, "lock"), EMPTY_BODY, null, 200);
        unlock(f1d1Id);
        // -ve tests

        // Missing target node
        lockInfo = new LockInfo();
        post(getNodeOperationUrl("fakeId", "lock"), toJsonAsStringNonNull(lockInfo), null, 404);

        // Cannot lock Data Dictionary node
        params = new HashMap<>();
        params.put(Nodes.PARAM_RELATIVE_PATH, "/Data Dictionary");
        response = getSingle(NodesEntityResource.class, getRootNodeId(), params, 200);
        Node nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        String ddNodeId = nodeResp.getId();

        setRequestContext(networkAdmin);
        post(getNodeOperationUrl(ddNodeId, "lock"), toJsonAsStringNonNull(lockInfo), null, 403);

        // Lock node already locked by another user - UnableToAquireLockException
        post(getNodeOperationUrl(dB1Id, "lock"), EMPTY_BODY, null, 422);

        // Lock node without permission (node created by user 1 in the Home folder)
        setRequestContext(user1);
        Folder folder2Resp = createFolder(Nodes.PATH_MY, "folder2" + RUNID);
        String folder2Id = folder2Resp.getId();

        String f2d1Name = "content f2" + RUNID + "_1l";
        Document f2d1 = createTextFile(folder2Id, f2d1Name, "The quick brown fox jumps over the lazy dog 1.");
        String f2d1Id = f2d1.getId();

        setRequestContext(user2);
        post(getNodeOperationUrl(f2d1Id, "lock"), EMPTY_BODY, null, 403);

        // Invalid lock body values
        setRequestContext(user1);

        Folder folderC = createFolder(Nodes.PATH_MY, "folder" + RUNID + "_C");
        String folderCId = folderC.getId();

        Document dC1 = createTextFile(folderCId, "content" + RUNID + "_dC1", "dC1 content");
        String dC1Id = dC1.getId();

        Map<String, String> body = new HashMap<>();
        body.put("type", "FULL123");
        post(getNodeOperationUrl(dC1Id, "lock"), toJsonAsStringNonNull(body), null, 400);

        body = new HashMap<>();
        body.put("type", "ALLOW_ADD_CHILDREN");
        post(getNodeOperationUrl(dC1Id, "lock"), toJsonAsStringNonNull(body), null, 400);

        body = new HashMap<>();
        body.put("lifetime", "PERSISTENT123");
        post(getNodeOperationUrl(dC1Id, "lock"), toJsonAsStringNonNull(body), null, 400);

        body = new HashMap<>();
        body.put("includeChildren", "true");
        post(getNodeOperationUrl(dC1Id, "lock"), toJsonAsStringNonNull(body), null, 400);
        
        body = new HashMap<>();
        body.put("timeToExpire", "NaN");
        post(getNodeOperationUrl(dC1Id, "lock"), toJsonAsStringNonNull(body), null, 400);

        body = new HashMap<>();
        body.put("invalid_property", "true");
        post(getNodeOperationUrl(dC1Id, "lock"), toJsonAsStringNonNull(body), null, 400);

        // Invalid lock of a folder
        post(getNodeOperationUrl(folderId, "lock"), toJsonAsStringNonNull(lockInfo), null, 400);

        //cleanup
        setRequestContext(user1); // all locks were made by user1
        unlock(dB1Id);
        deleteNode(dB1Id);
        deleteNode(folderId);
        deleteNode(folderAId);
        deleteNode(folderCId);
        deleteNode(folder1Id);
        deleteNode(folder2Id);
    }


    /**
     * Tests unlock of a node
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/unlock}
     */
    @Test
    public void testUnlock() throws Exception
    {
        setRequestContext(user1);

        // create folder
        Folder folderResp = createFolder(Nodes.PATH_MY, "folder" + RUNID);
        String folderId = folderResp.getId();

        // create doc d1
        String d1Name = "content" + RUNID + "_1l";
        Document d1 = createTextFile(folderId, d1Name, "The quick brown fox jumps over the lazy dog 1.");
        String d1Id = d1.getId();

        lock(d1Id, EMPTY_BODY);

        HttpResponse response = post(getNodeOperationUrl(d1Id, "unlock"), null, null, 200);
        Document documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        assertEquals(d1Name, documentResp.getName());
        assertEquals(d1Id, documentResp.getId());
        assertNull(documentResp.getProperties().get("cm:lockType"));
        assertNull(documentResp.getProperties().get("cm:lockOwner"));

        lock(d1Id, EMPTY_BODY);
        // Users with admin rights can unlock nodes locked by other users.
        setRequestContext(networkAdmin);
        post(getNodeOperationUrl(d1Id, "unlock"), null, null, 200);

        // -ve
        // Missing target node
        post(getNodeOperationUrl("fakeId", "unlock"), null, null, 404);

        // Unlock by a user without permission
        lock(d1Id, EMPTY_BODY);
        setRequestContext(user2);
        post(getNodeOperationUrl(d1Id, "unlock"), null, null, 403);

        setRequestContext(user1);

        //Unlock on a not locked node
        post(getNodeOperationUrl(folderId, "unlock"), null, null, 422);

        // clean up
        setRequestContext(user1); // all locks were made by user1

        unlock(d1Id);
        deleteNode(folderId);
    }

    @Test
    public void testLockFileCreatedByDeletedUser() throws Exception
    {
        // Create temp user
        String user = createUser("userRND-" + RUNID, "userRNDPassword", networkOne);

        setRequestContext(user);

        // Create folder
        Folder folderResp = createFolder(Nodes.PATH_MY, "folderRND" + RUNID);
        String folderId = folderResp.getId();

        // Create doc d1
        String d1Name = "content" + RUNID + "_1l";
        Document d1 = createTextFile(folderId, d1Name, "The quick brown fox jumps over the lazy dog 1.");
        String d1Id = d1.getId();

        setRequestContext(networkAdmin);

        transactionHelper.doInTransaction(() -> {
            deleteUser(user, networkOne);
            return null;
        });

        // Lock d1 document
        LockInfo lockInfo = new LockInfo();
        lockInfo.setTimeToExpire(30);
        lockInfo.setType("FULL");
        lockInfo.setLifetime("PERSISTENT");
        HttpResponse response = post(getNodeOperationUrl(d1Id, "lock"), toJsonAsStringNonNull(lockInfo), null, 200);
        Document documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        // Check that the lock was successful - from default response
        assertEquals(d1Name, documentResp.getName());
        assertEquals(d1Id, documentResp.getId());
        assertEquals(LockType.READ_ONLY_LOCK.toString(), documentResp.getProperties().get("cm:lockType"));
        assertNotNull(documentResp.getProperties().get("cm:lockOwner"));
        assertNull(documentResp.getIsLocked());

        // Get node info (ensure rollback didn't happen)
        Map<String, String> params = Collections.singletonMap("include", "isLocked");
        response = getSingle(NodesEntityResource.class, d1Id, params, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        // Check that the lock was successful - from get info call response
        assertEquals(d1Name, documentResp.getName());
        assertEquals(d1Id, documentResp.getId());
        assertEquals(LockType.READ_ONLY_LOCK.toString(), documentResp.getProperties().get("cm:lockType"));
        assertNotNull(documentResp.getProperties().get("cm:lockOwner"));
        assertTrue(documentResp.getIsLocked());

        // Clean up
        unlock(d1Id);

        // Get node info (ensure rollback didn't happen)
        response = getSingle(NodesEntityResource.class, d1Id, params, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        // Check that the unlock was successful
        assertEquals(d1Name, documentResp.getName());
        assertEquals(d1Id, documentResp.getId());
        assertEquals(null, documentResp.getProperties().get("cm:lockType"));
        assertNull(documentResp.getProperties().get("cm:lockOwner"));
        assertFalse(documentResp.getIsLocked());

        deleteNode(folderId);
    }

    @Test
    public void testMoveFileCreatedByDeletedUser() throws Exception
    {
        // Create temp user
        String user = createUser("userTstMove-" + RUNID, "userRNDPassword", networkOne);
        
        setRequestContext(user);

        // create folder f0
        String folder0Name = "f0-testMove-"+RUNID;
        String f0Id = createFolder(Nodes.PATH_MY, folder0Name).getId();

        // create folder f1
        Folder folderResp = createFolder(f0Id, "f1");
        String f1Id = folderResp.getId();

        // create folder f2
        folderResp = createFolder(f0Id, "f2");
        String f2Id = folderResp.getId();

        // create doc d1
        String d1Name = "content" + RUNID + "_1";
        String d1Id = createTextFile(f1Id, d1Name, "The quick brown fox jumps over the lazy dog 1.").getId();

        setRequestContext(networkAdmin);

        transactionHelper.doInTransaction(() -> {
            deleteUser(user, networkOne);
            return null;
        });
        
        // move file (without rename)

        NodeTarget tgt = new NodeTarget();
        tgt.setTargetParentId(f2Id);

        HttpResponse response = post("nodes/"+d1Id+"/move", toJsonAsStringNonNull(tgt), null, 200);
        Document documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        assertEquals(d1Name, documentResp.getName());
        assertEquals(f2Id, documentResp.getParentId());

        // Get node info (ensure rollback didn't happen)
        response = getSingle(NodesEntityResource.class, d1Id, null, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertEquals(f2Id, documentResp.getParentId());

        // move file (with rename)

        String d1NewName = d1Name+" updated !!";

        tgt = new NodeTarget();
        tgt.setName(d1NewName);
        tgt.setTargetParentId(f1Id);

        response = post("nodes/"+d1Id+"/move", toJsonAsStringNonNull(tgt), null, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        assertEquals(d1NewName, documentResp.getName());
        assertEquals(f1Id, documentResp.getParentId());

        // Get node info (ensure rollback didn't happen)
        response = getSingle(NodesEntityResource.class, d1Id, null, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertEquals(f1Id, documentResp.getParentId());
    }

    /**
     * Tests update of content after owner of the document is deleted
     * <p>PUT:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/content}
     */
    @Test
    public void testUploadContentDeletedOwner() throws Exception
    {
        // Create person2delete
        String personToDelete = createUser("usertodelete-" + RUNID, "userdelPassword", networkOne);

        // PersonToDelete creates a site and adds user1 as a site collab
        setRequestContext(personToDelete);
        String site1Title = "site-testUploadContentDeadUser_DocLib-" + RUNID;
        String site1Id = createSite(site1Title, SiteVisibility.PUBLIC).getId();
        String site1DocLibNodeId = getSiteContainerNodeId(site1Id, "documentLibrary");

        addSiteMember(site1Id, user1, SiteRole.SiteCollaborator);

        // PersonToDelete creates a file within DL
        Document deadDoc = createTextFile(site1DocLibNodeId, "testdeaddoc.txt", "The quick brown fox jumps over the lazy dog 1.");
        final String deadDocUrl = getNodeContentUrl(deadDoc.getId());

        // PersonToDelete updates the file
        String content = "Soft you a word or two before you go... I took by the throat the circumcised dog, And smote him, thus.";
        String docName = "goodbye-world.txt";
        Map params_doc = new HashMap<>();
        params_doc.put(Nodes.PARAM_NAME, docName);
        deadDoc = updateFileWithContent(deadDoc.getId(), content, params_doc, 200);
        assertEquals("person2delete cannot update document", docName, deadDoc.getName());

        // Download the file and confirm its contents on person2delete
        HttpResponse response = getSingle(deadDocUrl, personToDelete, null, 200);
        assertEquals("person2delete cannot view document", content, response.getResponse());

        // Download the file and confirm its contents on user1
        response = getSingle(deadDocUrl, user1, null, 200);
        assertEquals("user1 cannot view document", content, response.getResponse());

        // PersonToDelete is deleted
        transactionHelper.doInTransaction(() -> {
            deleteUser(personToDelete, networkOne);
            return null;
        });

        // User1 updates the file
        setRequestContext(user1);
        content = "This did I fear, but thought he had no weapon; For he was great of heart.";
        updateFileWithContent(deadDoc.getId(), content, null, 200);

        // Download the file and confirm its contents (ensure rollback didn't happen)
        response = getSingle(deadDocUrl, user1, null, 200);
        assertEquals("user1 cannot update after owner is deleted", content, response.getResponse());
    }

    private Document updateFileWithContent(String docId, String content, Map<String, String> params, int expectedStatus) throws Exception
    {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
        File txtFile = TempFileProvider.createTempFile(inputStream, getClass().getSimpleName(), ".txt");

        BinaryPayload payload = new BinaryPayload(txtFile);

        HttpResponse response = putBinary(getNodeContentUrl(docId), payload, null, params, expectedStatus);
        if (expectedStatus != 200)
        {
            return null;
        }
        return RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
    }

    /**
     * Creates authority context
     *
     * @param user
     * @return
     */
    private void createAuthorityContext(String user)
    {
        String groupName = "Group_ROOT" + GUID.generate();

        AuthenticationUtil.setRunAsUser(user);
        if (rootGroupName == null)
        {
            rootGroupName = authorityService.getName(AuthorityType.GROUP, groupName);
        }

        if (!authorityService.authorityExists(rootGroupName))
        {
            AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
            rootGroupName = authorityService.createAuthority(AuthorityType.GROUP, groupName);
            groupA = authorityService.createAuthority(AuthorityType.GROUP, "Test_GroupA");
            authorityService.addAuthority(rootGroupName, groupA);
            groupB = authorityService.createAuthority(AuthorityType.GROUP, "Test_GroupB");
            authorityService.addAuthority(rootGroupName, groupB);
            authorityService.addAuthority(groupA, user1);
            authorityService.addAuthority(groupB, user2);
        }
    }

    /**
     * Clears authority context: removes root group and all child groups
     */
    private void clearAuthorityContext()
    {
        if (authorityService.authorityExists(rootGroupName))
        {
            AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
            authorityService.deleteAuthority(rootGroupName, true);
        }
    }

    @Test
    public void testRetrievePermissions() throws Exception
    {
        try
        {
            createAuthorityContext(user1);
            testRetrieveNodePermissionsSpecialNodes();
            testRetrieveNodePermissions();
        }
        finally
        {
            clearAuthorityContext();
        }
    }

    /**
     * Test retrieve node permissions for special nodes like:
     * 'Company Home', 'Data Dictionary', 'Shared', 'Sites', 'User Home'
     *
     * @throws Exception
     */
    private void testRetrieveNodePermissionsSpecialNodes() throws Exception
    {
        setRequestContext(user1);
        String rootNodeId = getRootNodeId();
        String userHome = getMyNodeId();
        String sharedFolder = getSharedNodeId();
        String sitesNodeId = getSitesNodeId();
        String ddNodeId = getDataDictionaryNodeId();

        Map params = new HashMap<>();
        params.put("include", "permissions");

        // Test permissions for node 'Company Home'
        HttpResponse response = getSingle(NodesEntityResource.class, rootNodeId, params, 200);
        Node nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNull(nodeResp.getPermissions());

        // Test permissions for node 'Sites'
        response = getSingle(NodesEntityResource.class, sitesNodeId, params, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNull(nodeResp.getPermissions());

        // Test permissions for node 'Data Dictionary'
        response = getSingle(NodesEntityResource.class, ddNodeId, params, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNull(nodeResp.getPermissions());

        // Test permissions for node 'Shared Folder'
        response = getSingle(NodesEntityResource.class, sharedFolder, params, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNotNull(nodeResp.getPermissions());

        Set<String> expectedSettable = new HashSet<>(Arrays.asList("Coordinator", "Collaborator", "Contributor", "Consumer", "Editor"));

        // Test permissions for node 'User Home'
        response = getSingle(NodesEntityResource.class, userHome, params, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNotNull(nodeResp.getPermissions());
        assertNotNull(nodeResp.getPermissions().getSettable());
        assertTrue("Incorrect list of settable permissions returned!", nodeResp.getPermissions().getSettable().containsAll(expectedSettable));
        assertFalse(nodeResp.getPermissions().getIsInheritanceEnabled());

        // Try as admin
        setRequestContext(networkAdmin);

        // Test permissions for node 'Company Home'
        response = getSingle(NodesEntityResource.class, rootNodeId, params, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertFalse(nodeResp.getPermissions().getIsInheritanceEnabled());
        assertNull(nodeResp.getPermissions().getInherited());
        assertNotNull(nodeResp.getPermissions().getLocallySet());
        assertTrue(nodeResp.getPermissions().getLocallySet().contains(new NodePermissions.NodePermission("GROUP_EVERYONE", PermissionService.CONSUMER, AccessStatus.ALLOWED.toString())));
        assertNotNull(nodeResp.getPermissions().getSettable());
        assertTrue("Incorrect list of settable permissions returned!", nodeResp.getPermissions().getSettable().containsAll(expectedSettable));

        // Test permissions for node 'Sites'
        response = getSingle(NodesEntityResource.class, sitesNodeId, params, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertTrue(nodeResp.getPermissions().getIsInheritanceEnabled());
        assertNotNull(nodeResp.getPermissions().getInherited());
        assertTrue(nodeResp.getPermissions().getInherited().contains(new NodePermissions.NodePermission("GROUP_EVERYONE", PermissionService.CONSUMER, AccessStatus.ALLOWED.toString())));
        assertNull(nodeResp.getPermissions().getLocallySet());
        assertNotNull(nodeResp.getPermissions().getSettable());
        assertTrue("Incorrect list of settable permissions returned!", nodeResp.getPermissions().getSettable().containsAll(expectedSettable));

        // Test permissions for node 'Data Dictionary'
        response = getSingle(NodesEntityResource.class, ddNodeId, params, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertFalse(nodeResp.getPermissions().getIsInheritanceEnabled());
        assertNull(nodeResp.getPermissions().getInherited());
        assertNotNull(nodeResp.getPermissions().getLocallySet());
        assertTrue(nodeResp.getPermissions().getLocallySet().contains(new NodePermissions.NodePermission("GROUP_EVERYONE", PermissionService.CONSUMER, AccessStatus.ALLOWED.toString())));
        assertNotNull(nodeResp.getPermissions().getSettable());
        assertTrue("Incorrect list of settable permissions returned!", nodeResp.getPermissions().getSettable().containsAll(expectedSettable));

        // Test permissions for node 'Shared Folder'
        response = getSingle(NodesEntityResource.class, sharedFolder, params, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertFalse(nodeResp.getPermissions().getIsInheritanceEnabled());
        assertNull(nodeResp.getPermissions().getInherited());
        assertNotNull(nodeResp.getPermissions().getLocallySet());
        assertTrue(nodeResp.getPermissions().getLocallySet().contains(new NodePermissions.NodePermission("GROUP_EVERYONE", PermissionService.CONTRIBUTOR, AccessStatus.ALLOWED.toString())));
        assertNotNull(nodeResp.getPermissions().getSettable());
        assertTrue("Incorrect list of settable permissions returned!", nodeResp.getPermissions().getSettable().containsAll(expectedSettable));

        // Test permissions for node 'User Home'
        response = getSingle(NodesEntityResource.class, userHome, params, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertFalse(nodeResp.getPermissions().getIsInheritanceEnabled());
        assertNull(nodeResp.getPermissions().getInherited());
        assertNotNull(nodeResp.getPermissions().getLocallySet());
        assertTrue(nodeResp.getPermissions().getLocallySet().contains(new NodePermissions.NodePermission("ROLE_OWNER", "All", AccessStatus.ALLOWED.toString())));
        assertTrue(nodeResp.getPermissions().getLocallySet().contains(new NodePermissions.NodePermission(user1, "All", AccessStatus.ALLOWED.toString())));
        assertNotNull(nodeResp.getPermissions().getSettable());
        assertTrue("Incorrect list of settable permissions returned!", nodeResp.getPermissions().getSettable().containsAll(expectedSettable));

    }

    private void testRetrieveNodePermissions() throws Exception
    {
        setRequestContext(user1);
        // create folder with an empty document
        String postUrl = createFolder();
        String docId = createDocument(postUrl);

        Map params = new HashMap<>();
        params.put("include", "permissions");

        // update permissions
        Document dUpdate = new Document();
        NodePermissions nodePermissions = new NodePermissions();
        List<NodePermissions.NodePermission> locallySetPermissions = new ArrayList<>();
        locallySetPermissions.add(new NodePermissions.NodePermission(groupA, PermissionService.CONSUMER, AccessStatus.ALLOWED.toString()));
        locallySetPermissions.add(new NodePermissions.NodePermission(groupB, PermissionService.CONSUMER, AccessStatus.DENIED.toString()));
        nodePermissions.setLocallySet(locallySetPermissions);
        dUpdate.setPermissions(nodePermissions);

        // update node
        HttpResponse response = put(URL_NODES, docId, toJsonAsStringNonNull(dUpdate), null, 200);
        Document documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        // Check if permission are retrieved if 'include=permissions' is not
        // sent in the request
        response = getSingle(NodesEntityResource.class, documentResp.getId(), null, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertNull("Permissions should not be retrieved unless included!", documentResp.getPermissions());

        // Call again with 'include=permissions'
        response = getSingle(NodesEntityResource.class, documentResp.getId(), params, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        // Check that all permissions are retrieved
        assertNotNull(documentResp.getPermissions());
        assertTrue("Locally set permissions were not set properly!", documentResp.getPermissions().getLocallySet().size() == 2);
        // Check inherit default true
        assertTrue("Inheritance flag was not retrieved!", documentResp.getPermissions().getIsInheritanceEnabled());
        // Check inherited permissions (for ROLE_OWNER and user1)
        assertNotNull(documentResp.getPermissions().getInherited());
        assertTrue(documentResp.getPermissions().getInherited().size() == 2);
        assertNotNull(documentResp.getPermissions().getSettable());
        assertTrue(documentResp.getPermissions().getSettable().size() == 5);
        Set<String> expectedSettable = new HashSet<>(Arrays.asList("Coordinator", "Collaborator", "Contributor", "Consumer", "Editor"));
        assertTrue("Incorrect list of settable permissions returned!", documentResp.getPermissions().getSettable().containsAll(expectedSettable));
    }

    /**
     * Tests set permissions on a new node
     *
     * @throws Exception
     */
    @Test
    public void testCreateNodePermissions() throws Exception
    {
        try
        {
            createAuthorityContext(networkAdmin);

            setRequestContext(user1);
            // +ve tests
            testCreatePermissionsOnNode();

            // -ve tests
            // invalid permission tests (authority, name or access level)
            testCreatePermissionInvalidAuthority();
            testCreatePermissionInvalidName();
            testCreatePermissionInvalidAccessStatus();
            testCreatePermissionAddDuplicate();

            // required permission properties missing
            testCreatePermissionMissingFields();
            
            // 'Permission Denied' tests
            testCreatePermissionsPermissionDeniedUser();

            // Inherit from parent tests
            testCreatePermissionsSetFalseInheritFromParent();
        }
        finally
        {
            clearAuthorityContext();
        }
    }

    /**
     * Tests set permissions on an existing node
     *
     * @throws Exception
     */
    @Test
    public void testUpdateNodePermissions() throws Exception
    {
        try
        {
            createAuthorityContext(networkAdmin);

            setRequestContext(user1);
            // +ve tests
            testUpdatePermissionsOnNode();

            // -ve tests
            // invalid permission tests (authority, name or access level)
            testUpdatePermissionInvalidAuthority();
            testUpdatePermissionInvalidName();
            testUpdatePermissionInvalidAccessStatus();
            testUpdatePermissionAddDuplicate();

            // required permission properties missing
            testUpdatePermissionMissingFields();
            
            // 'Permission Denied' tests
            testUpdatePermissionsPermissionDeniedUser();
            testUpdatePermissionsOnSpecialNodes();

            // Inherit from parent tests
            testUpdatePermissionsDefaultInheritFromParent();
            testUpdatePermissionsSetFalseInheritFromParent();
        }
        finally
        {
            clearAuthorityContext();
        }
    }
    
    /**
     * Test upload using relativePath
     */
    @Test
    public void testUploadUsingRelativePath() throws Exception
    {
        setRequestContext(user1);

        // user1 creates a private site and adds user2 as a site consumer
        String site1Title = "site-testGetPathElements_DocLib-" + RUNID;
        String site1Id = createSite(site1Title, SiteVisibility.PRIVATE).getId();
        addSiteMember(site1Id, user2, SiteRole.SiteConsumer);

        String site1DocLibNodeId = getSiteContainerNodeId(site1Id, "documentLibrary");

        // /Company
        // Home/Sites/RandomSite<timestamp>/documentLibrary/folder<timestamp>_A
        String folderA = "folder" + RUNID + "_A";
        String folderA_Id = createFolder(site1DocLibNodeId, folderA).getId();

        // /Company
        // Home/Sites/RandomSite<timestamp>/documentLibrary/folder<timestamp>_A/folder<timestamp>_B
        String folderB = "folder" + RUNID + "_B";
        String folderB_Id = createFolder(folderA_Id, folderB).getId();
        NodeRef folderB_Ref = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, folderB_Id);

        // /Company
        // Home/Sites/RandomSite<timestamp>/documentLibrary/folder<timestamp>_A/folder<timestamp>_B/folder<timestamp>_C
        String folderC = "folder" + RUNID + "_C";
        String folderC_Id = createFolder(folderB_Id, folderC).getId();
        NodeRef folderC_Ref = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, folderC_Id);

        // /Company
        // Home/Sites/RandomSite<timestamp>/documentLibrary/folder<timestamp>_A/folder<timestamp>_B/folder<timestamp>_C/content<timestamp>
        String content = "content" + RUNID;
        String content_Id = createTextFile(folderC_Id, content, "The quick brown fox jumps over the lazy dog.").getId();

        Map<String, String> params = new HashMap<>();
        params.put("include", "path");
        params.put("relativePath", folderA + "/" + folderB + "/" + folderC);

        // call get with relativePathParam
        HttpResponse response = getAll(getNodeChildrenUrl(site1DocLibNodeId), null, params, 200);
        List<Node> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);

        assertEquals(1, nodes.size());
        assertEquals("/" + folderA + "/" + folderB + "/" + folderC, ((Node) (nodes.get(0))).getPath().getRelativePath());
    }

    /**
     * Test create permission on a node
     *
     * @throws Exception
     */
    private void testCreatePermissionsOnNode() throws Exception
    {
        String postUrl = createFolder();

        NodePermissions nodePermissions = new NodePermissions();
        List<NodePermissions.NodePermission> locallySetPermissions = new ArrayList<>();
        locallySetPermissions.add(new NodePermissions.NodePermission(groupA, PermissionService.CONSUMER, AccessStatus.ALLOWED.toString()));
        nodePermissions.setLocallySet(locallySetPermissions);

        String dId = createDocument(postUrl, nodePermissions);

        validatePermissions(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, dId), locallySetPermissions);

        // Check permissions on node for user2 (part of groupB)
        AuthenticationUtil.setRunAsUser(user2);
        assertTrue(permissionService.hasPermission(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, dId), PermissionService.CONSUMER) == AccessStatus.DENIED);

        // Check permissions on node for user1 (part of groupA)
        AuthenticationUtil.setRunAsUser(user1);
        assertTrue(permissionService.hasPermission(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, dId), PermissionService.CONSUMER) == AccessStatus.ALLOWED);
    }

    /**
     * Test attempt to set permission with an invalid authority
     *
     * @throws Exception
     */
    private void testCreatePermissionInvalidAuthority() throws Exception
    {
        String postUrl = createFolder();

        NodePermissions nodePermissions = new NodePermissions();
        List<NodePermissions.NodePermission> locallySetPermissions = new ArrayList<>();
        locallySetPermissions.add(new NodePermissions.NodePermission("NonExistingAuthority", PermissionService.CONSUMER, AccessStatus.DENIED.toString()));
        nodePermissions.setLocallySet(locallySetPermissions);

        Document d1 = new Document();
        d1.setName("testDoc" + GUID.generate());
        d1.setNodeType(TYPE_CM_CONTENT);
        d1.setPermissions(nodePermissions);
        
        // "Cannot set permissions on this node - unknown authority:
        // NonExistingAuthority"
        post(postUrl, toJsonAsStringNonNull(d1), null, 400);
    }

    /**
     * Test attempt to set permission with an invalid name
     *
     * @throws Exception
     */
    private void testCreatePermissionInvalidName() throws Exception
    {
        String postUrl = createFolder();

        NodePermissions nodePermissions = new NodePermissions();
        List<NodePermissions.NodePermission> locallySetPermissions = new ArrayList<>();
        locallySetPermissions.add(new NodePermissions.NodePermission(groupA, "InvalidName", AccessStatus.DENIED.toString()));
        nodePermissions.setLocallySet(locallySetPermissions);

        Document d1 = new Document();
        d1.setName("testDoc" + GUID.generate());
        d1.setNodeType(TYPE_CM_CONTENT);
        d1.setPermissions(nodePermissions);

        // "Cannot set permissions on this node - unknown permission name:
        // InvalidName"
        post(postUrl, toJsonAsStringNonNull(d1), null, 400);
    }

    /**
     * Test attempt to set permission with an invalid access status
     *
     * @throws Exception
     */
    private void testCreatePermissionInvalidAccessStatus() throws Exception
    {
        String postUrl = createFolder();

        NodePermissions nodePermissions = new NodePermissions();
        List<NodePermissions.NodePermission> locallySetPermissions = new ArrayList<>();
        locallySetPermissions.add(new NodePermissions.NodePermission(groupA, PermissionService.CONSUMER, "InvalidAccessLevel"));
        nodePermissions.setLocallySet(locallySetPermissions);

        Document d1 = new Document();
        d1.setName("testDoc" + GUID.generate());
        d1.setNodeType(TYPE_CM_CONTENT);
        d1.setPermissions(nodePermissions);

        // "Cannot set permissions on this node - unknown access status:
        // InvalidName"
        post(postUrl, toJsonAsStringNonNull(d1), null, 400);
    }

    /**
     * Test add duplicate permissions
     *
     * @throws Exception
     */
    private void testCreatePermissionAddDuplicate() throws Exception
    {
        String postUrl = createFolder();

        // Add same permission with different access status
        NodePermissions nodePermissions = new NodePermissions();
        List<NodePermissions.NodePermission> locallySetPermissions = new ArrayList<>();
        locallySetPermissions.add(new NodePermissions.NodePermission(groupA, PermissionService.CONSUMER, AccessStatus.ALLOWED.toString()));
        locallySetPermissions.add(new NodePermissions.NodePermission(groupA, PermissionService.CONSUMER, AccessStatus.DENIED.toString()));
        nodePermissions.setLocallySet(locallySetPermissions);

        Document d1 = new Document();
        d1.setName("testDoc" + GUID.generate());
        d1.setNodeType(TYPE_CM_CONTENT);
        d1.setPermissions(nodePermissions);

        // "Duplicate node permissions, there is more than one permission with
        // the same authority and name!"
        post(postUrl, toJsonAsStringNonNull(d1), null, 400);

        // Add the same permission with same access status
        locallySetPermissions.clear();
        locallySetPermissions.add(new NodePermissions.NodePermission(groupA, PermissionService.CONSUMER, AccessStatus.ALLOWED.toString()));
        locallySetPermissions.add(new NodePermissions.NodePermission(groupA, PermissionService.CONSUMER, AccessStatus.ALLOWED.toString()));

        // "Duplicate node permissions, there is more than one permission with
        // the same authority and name!"
        post(postUrl, toJsonAsStringNonNull(d1), null, 400);
    }

    /**
     * Tests creating permissions on a new node without providing mandatory
     * properties
     * 
     * @throws Exception
     */
    private void testCreatePermissionMissingFields() throws Exception
    {
        String postUrl = createFolder();

        // Add same permission with different access status
        NodePermissions nodePermissions = new NodePermissions();
        List<NodePermissions.NodePermission> locallySetPermissions = new ArrayList<>();
        locallySetPermissions.add(new NodePermissions.NodePermission(null, PermissionService.CONSUMER, AccessStatus.ALLOWED.toString()));
        nodePermissions.setLocallySet(locallySetPermissions);

        Document d1 = new Document();
        d1.setName("testDoc" + GUID.generate());
        d1.setNodeType(TYPE_CM_CONTENT);
        d1.setPermissions(nodePermissions);

        // "Authority Id is expected."
        post(postUrl, toJsonAsStringNonNull(d1), null, 400);
        locallySetPermissions.clear();
        locallySetPermissions.add(new NodePermissions.NodePermission("", PermissionService.CONSUMER, AccessStatus.ALLOWED.toString()));
        post(postUrl, toJsonAsStringNonNull(d1), null, 400);

        locallySetPermissions.clear();
        locallySetPermissions.add(new NodePermissions.NodePermission(groupA, null, AccessStatus.ALLOWED.toString()));
        // "Permission name is expected."
        post(postUrl, toJsonAsStringNonNull(d1), null, 400);
        locallySetPermissions.clear();
        locallySetPermissions.add(new NodePermissions.NodePermission(groupA, "", AccessStatus.ALLOWED.toString()));
        post(postUrl, toJsonAsStringNonNull(d1), null, 400);
    }
    
    /**
     * Tests creating permissions on a new node that user doesn't have permission for
     *
     * @throws Exception
     */
    private void testCreatePermissionsPermissionDeniedUser() throws Exception
    {
        String postUrl = createFolder();

        NodePermissions nodePermissions = new NodePermissions();
        List<NodePermissions.NodePermission> locallySetPermissions = new ArrayList<>();
        locallySetPermissions.add(new NodePermissions.NodePermission(groupA, PermissionService.CONSUMER, AccessStatus.DENIED.toString()));
        nodePermissions.setLocallySet(locallySetPermissions);

        Document d1 = new Document();
        d1.setName("testDoc" + GUID.generate());
        d1.setNodeType(TYPE_CM_CONTENT);
        d1.setPermissions(nodePermissions);

        setRequestContext(user2);
        // "Permission Denied" expected
        post(postUrl, toJsonAsStringNonNull(d1), null, 403);
    }

    /**
     * Test set inherit from parent to false
     *
     * @throws Exception
     */
    private void testCreatePermissionsSetFalseInheritFromParent() throws Exception
    {
        String testFolderUrl = createFolder();

        NodePermissions nodePermissions = new NodePermissions();
        nodePermissions.setIsInheritanceEnabled(false);

        Document d1 = new Document();
        d1.setName("testDoc" + GUID.generate());
        d1.setNodeType(TYPE_CM_CONTENT);
        d1.setPermissions(nodePermissions);

        String dId = createDocument(testFolderUrl, nodePermissions);

        Map params = new HashMap<>();
        params.put("include", "permissions");

        HttpResponse response = getSingle(NodesEntityResource.class, dId, params, 200);
        Node nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);

        assertFalse("Inheritance hasn't been disabled!", nodeResp.getPermissions().getIsInheritanceEnabled());
        assertNull("Permissions were inherited from parent!", nodeResp.getPermissions().getInherited());

    }

    /**
     * Test update permission on a node
     *
     * @throws Exception
     */
    private void testUpdatePermissionsOnNode() throws Exception
    {
        // create folder with an empty document
        String postUrl = createFolder();
        String dId = createDocument(postUrl);

        // update permissions
        Document dUpdate = new Document();
        NodePermissions nodePermissions = new NodePermissions();
        List<NodePermissions.NodePermission> locallySetPermissions = new ArrayList<>();
        locallySetPermissions.add(new NodePermissions.NodePermission(groupA, PermissionService.CONSUMER, AccessStatus.ALLOWED.toString()));
        nodePermissions.setLocallySet(locallySetPermissions);
        dUpdate.setPermissions(nodePermissions);

        // update node
        HttpResponse response = put(URL_NODES, dId, toJsonAsStringNonNull(dUpdate), null, 200);
        Document documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        validatePermissions(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, documentResp.getId()), locallySetPermissions);

        // Check permissions on node for user2 (part of groupB)
        AuthenticationUtil.setRunAsUser(user2);
        assertTrue(permissionService.hasPermission(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, documentResp.getId()), PermissionService.CONSUMER) == AccessStatus.DENIED);

        // Check permissions on node for user1 (part of groupA)
        AuthenticationUtil.setRunAsUser(user1);
        assertTrue(permissionService.hasPermission(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, documentResp.getId()), PermissionService.CONSUMER) == AccessStatus.ALLOWED);

        // add two groups with different permissions for each
        locallySetPermissions.clear();
        locallySetPermissions.add(new NodePermissions.NodePermission(groupA, PermissionService.EDITOR, AccessStatus.ALLOWED.toString()));
        locallySetPermissions.add(new NodePermissions.NodePermission(groupB, PermissionService.CONSUMER, AccessStatus.ALLOWED.toString()));
        nodePermissions.setLocallySet(locallySetPermissions);
        dUpdate.setPermissions(nodePermissions);

        // update node
        response = put(URL_NODES, dId, toJsonAsStringNonNull(dUpdate), null, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        validatePermissions(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, documentResp.getId()), locallySetPermissions);

        // Check permissions on node for user2 (part of groupB)
        AuthenticationUtil.setRunAsUser(user2);
        assertTrue(permissionService.hasPermission(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, documentResp.getId()), PermissionService.CONSUMER) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, documentResp.getId()), PermissionService.EDITOR) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, documentResp.getId()), PermissionService.WRITE) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, documentResp.getId()), PermissionService.READ) == AccessStatus.ALLOWED);

        // Check permissions on node for user1 (part of groupA)
        AuthenticationUtil.setRunAsUser(user1);
        assertTrue(permissionService.hasPermission(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, documentResp.getId()), PermissionService.EDITOR) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, documentResp.getId()), PermissionService.WRITE) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, documentResp.getId()), PermissionService.READ) == AccessStatus.ALLOWED);
    }

    /**
     * Test attempt to set permission with an invalid authority
     *
     * @throws Exception
     */
    private void testUpdatePermissionInvalidAuthority() throws Exception
    {
        // create folder containing an empty document
        String postUrl = createFolder();
        String dId = createDocument(postUrl);

        // update permissions
        Document dUpdate = new Document();
        NodePermissions nodePermissions = new NodePermissions();
        List<NodePermissions.NodePermission> locallySetPermissions = new ArrayList<>();
        locallySetPermissions.add(new NodePermissions.NodePermission("NonExistingAuthority", PermissionService.CONSUMER, AccessStatus.DENIED.toString()));
        nodePermissions.setLocallySet(locallySetPermissions);
        dUpdate.setPermissions(nodePermissions);

        // "Cannot set permissions on this node - unknown authority:
        // NonExistingAuthority"
        put(URL_NODES, dId, toJsonAsStringNonNull(dUpdate), null, 400);
    }

    /**
     * Test attempt to set permission with an invalid name
     *
     * @throws Exception
     */
    private void testUpdatePermissionInvalidName() throws Exception
    {
        // create folder with an empty document
        String postUrl = createFolder();
        String dId = createDocument(postUrl);

        // update permissions
        Document dUpdate = new Document();
        NodePermissions nodePermissions = new NodePermissions();
        List<NodePermissions.NodePermission> locallySetPermissions = new ArrayList<>();
        locallySetPermissions.add(new NodePermissions.NodePermission(groupA, "InvalidName", AccessStatus.DENIED.toString()));
        nodePermissions.setLocallySet(locallySetPermissions);
        dUpdate.setPermissions(nodePermissions);

        // "Cannot set permissions on this node - unknown permission name:
        // InvalidName"
        put(URL_NODES, dId, toJsonAsStringNonNull(dUpdate), null, 400);
    }

    /**
     * Test attempt to set permission with an invalid access status
     *
     * @throws Exception
     */
    private void testUpdatePermissionInvalidAccessStatus() throws Exception
    {
        // create folder with an empty document
        String postUrl = createFolder();
        String dId = createDocument(postUrl);

        // update permissions
        Document dUpdate = new Document();
        NodePermissions nodePermissions = new NodePermissions();
        List<NodePermissions.NodePermission> locallySetPermissions = new ArrayList<>();
        locallySetPermissions.add(new NodePermissions.NodePermission(groupA, PermissionService.CONSUMER, "InvalidAccessLevel"));
        nodePermissions.setLocallySet(locallySetPermissions);
        dUpdate.setPermissions(nodePermissions);

        // "Cannot set permissions on this node - unknown access status:
        // InvalidName"
        put(URL_NODES, dId, toJsonAsStringNonNull(dUpdate), null, 400);
    }

    /**
     * Test add duplicate permissions
     *
     * @throws Exception
     */
    private void testUpdatePermissionAddDuplicate() throws Exception
    {
        // create folder with an empty document
        String postUrl = createFolder();
        String dId = createDocument(postUrl);

        // update permissions
        Document dUpdate = new Document();
        // Add same permission with different access status
        NodePermissions nodePermissions = new NodePermissions();
        List<NodePermissions.NodePermission> locallySetPermissions = new ArrayList<>();
        locallySetPermissions.add(new NodePermissions.NodePermission(groupA, PermissionService.CONSUMER, AccessStatus.ALLOWED.toString()));
        locallySetPermissions.add(new NodePermissions.NodePermission(groupA, PermissionService.CONSUMER, AccessStatus.DENIED.toString()));
        nodePermissions.setLocallySet(locallySetPermissions);
        dUpdate.setPermissions(nodePermissions);

        // "Duplicate node permissions, there is more than one permission with
        // the same authority and name!"
        put(URL_NODES, dId, toJsonAsStringNonNull(dUpdate), null, 400);

        // Add the same permission with same access status
        locallySetPermissions.clear();
        locallySetPermissions.add(new NodePermissions.NodePermission(groupA, PermissionService.CONSUMER, AccessStatus.ALLOWED.toString()));
        locallySetPermissions.add(new NodePermissions.NodePermission(groupA, PermissionService.CONSUMER, AccessStatus.ALLOWED.toString()));
        nodePermissions.setLocallySet(locallySetPermissions);
        dUpdate.setPermissions(nodePermissions);

        // "Duplicate node permissions, there is more than one permission with
        // the same authority and name!"
        put(URL_NODES, dId, toJsonAsStringNonNull(dUpdate), null, 400);
    }

    /**
     * Tests updating permissions on a node without providing mandatory
     * properties
     * 
     * @throws Exception
     */
    private void testUpdatePermissionMissingFields() throws Exception
    {
        // create folder with an empty document
        String postUrl = createFolder();
        String dId = createDocument(postUrl);

        // update permissions
        Document dUpdate = new Document();
        // Add same permission with different access status
        NodePermissions nodePermissions = new NodePermissions();
        List<NodePermissions.NodePermission> locallySetPermissions = new ArrayList<>();
        locallySetPermissions.add(new NodePermissions.NodePermission(null, PermissionService.CONSUMER, AccessStatus.ALLOWED.toString()));
        nodePermissions.setLocallySet(locallySetPermissions);
        dUpdate.setPermissions(nodePermissions);

        // "Authority Id is expected."
        put(URL_NODES, dId, toJsonAsStringNonNull(dUpdate), null, 400);
        locallySetPermissions.clear();
        locallySetPermissions.add(new NodePermissions.NodePermission("", PermissionService.CONSUMER, AccessStatus.ALLOWED.toString()));
        put(URL_NODES, dId, toJsonAsStringNonNull(dUpdate), null, 400);

        locallySetPermissions.clear();
        locallySetPermissions.add(new NodePermissions.NodePermission(groupA, null, AccessStatus.ALLOWED.toString()));
        // "Permission name is expected."
        put(URL_NODES, dId, toJsonAsStringNonNull(dUpdate), null, 400);
        locallySetPermissions.clear();
        locallySetPermissions.add(new NodePermissions.NodePermission(groupA, "", AccessStatus.ALLOWED.toString()));
        put(URL_NODES, dId, toJsonAsStringNonNull(dUpdate), null, 400);
    }
    
    /**
     * Tests updating permissions on a node that user doesn't have permission for
     *
     * @throws Exception
     */
    private void testUpdatePermissionsPermissionDeniedUser() throws Exception
    {
        // create folder with an empty document
        String postUrl = createFolder();
        String dId = createDocument(postUrl);

        // update permissions
        Document dUpdate = new Document();
        NodePermissions nodePermissions = new NodePermissions();
        List<NodePermissions.NodePermission> locallySetPermissions = new ArrayList<>();
        locallySetPermissions.add(new NodePermissions.NodePermission(groupA, PermissionService.CONSUMER, AccessStatus.DENIED.toString()));
        nodePermissions.setLocallySet(locallySetPermissions);
        dUpdate.setPermissions(nodePermissions);

        setRequestContext(user2);
        // "Permission Denied" expected
        put(URL_NODES, dId, toJsonAsStringNonNull(dUpdate), null, 403);
    }

    /**
     * Test update permissions on special nodes like
     * 'Company Home', 'Sites', 'Shared', 'User Home', 'Data Dictionary'
     *
     * @throws Exception
     */
    private void testUpdatePermissionsOnSpecialNodes() throws Exception
    {
        NodePermissions nodePermissions = new NodePermissions();
        List<NodePermissions.NodePermission> locallySetPermissions = new ArrayList<>();
        locallySetPermissions.add(new NodePermissions.NodePermission(groupA, PermissionService.EDITOR, AccessStatus.ALLOWED.toString()));
        nodePermissions.setLocallySet(locallySetPermissions);

        // 'Company Home'
        HttpResponse response = getSingle(NodesEntityResource.class, getRootNodeId(), null, 200);
        Node node = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        node.setPermissions(nodePermissions);
        put(URL_NODES, node.getId(), toJsonAsStringNonNull(node), null, 403);

        // 'Sites' folder
        response = getSingle(NodesEntityResource.class, getSharedNodeId(), null, 200);
        node = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        node.setPermissions(nodePermissions);
        put(URL_NODES, node.getId(), toJsonAsStringNonNull(node), null, 403);

        // 'Data Dictionary' folder
        response = getSingle(NodesEntityResource.class, getDataDictionaryNodeId(), null, 200);
        node = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        node.setPermissions(nodePermissions);
        put(URL_NODES, node.getId(), toJsonAsStringNonNull(node), null, 403);

        // 'Shared' folder
        response = getSingle(NodesEntityResource.class, getSharedNodeId(), null, 200);
        node = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        node.setPermissions(nodePermissions);
        put(URL_NODES, node.getId(), toJsonAsStringNonNull(node), null, 403);

        // 'User Home' folder
        HttpResponse responseUserHome = getSingle(NodesEntityResource.class, getMyNodeId(), null, 200);
        Node nodeUserHome = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        node.setPermissions(nodePermissions);
        put(URL_NODES, node.getId(), toJsonAsStringNonNull(node), null, 403);
    }

    /**
     * Test default inherit from parent
     *
     * @throws Exception
     */
    private void testUpdatePermissionsDefaultInheritFromParent() throws Exception
    {
        // create folder
        Folder folder = new Folder();
        folder.setName("testFolder" + GUID.generate());
        folder.setNodeType(TYPE_CM_FOLDER);

        // set permissions on previously created folder
        NodePermissions nodePermissions = new NodePermissions();
        List<NodePermissions.NodePermission> locallySetPermissions = new ArrayList<>();
        locallySetPermissions.add(new NodePermissions.NodePermission(groupA, PermissionService.EDITOR, AccessStatus.DENIED.toString()));
        nodePermissions.setLocallySet(locallySetPermissions);
        folder.setPermissions(nodePermissions);

        HttpResponse response = post(getNodeChildrenUrl(Nodes.PATH_MY), RestApiUtil.toJsonAsStringNonNull(folder), 201);
        Folder f = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);

        // create a new document in testFolder
        String docId = createDocument(getNodeChildrenUrl(f.getId()));

        Map params = new HashMap<>();
        params.put("include", "permissions");

        response = getSingle(NodesEntityResource.class, docId, params, 200);
        Document docResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        assertTrue("Inheritance hasn't been enabled!", docResp.getPermissions().getIsInheritanceEnabled());
        assertTrue("Permissions were not inherited from parent!", docResp.getPermissions().getInherited().size() > 0);
    }

    /**
     * Test set inherit from parent to false
     *
     * @throws Exception
     */
    private void testUpdatePermissionsSetFalseInheritFromParent() throws Exception
    {
        // create folder
        String testFolderUrl = createFolder();
        String dId = createDocument(testFolderUrl);

        // create a new document in testFolder and set inherit to false
        Document dUpdate = new Document();
        NodePermissions nodePermissionsUpdate = new NodePermissions();
        nodePermissionsUpdate.setIsInheritanceEnabled(false);
        dUpdate.setPermissions(nodePermissionsUpdate);

        HttpResponse response = put(URL_NODES, dId, toJsonAsStringNonNull(dUpdate), null, 200);
        Document documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        Map params = new HashMap<>();
        params.put("include", "permissions");

        response = getSingle(NodesEntityResource.class, documentResp.getId(), params, 200);
        Node nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);

        assertFalse("Inheritance hasn't been disabled!", nodeResp.getPermissions().getIsInheritanceEnabled());
        assertNull("Permissions were inherited from parent!", nodeResp.getPermissions().getInherited());

    }
    
    @Test
    public void createContentWithAllParametersAccepted() throws Exception
    {
        setRequestContext(user1);

        // Let's start with creating a folder in our home folder.
        String folderName = "My Folder" + GUID.generate();
        // -my- (eg. User's Home for on-prem)
        String myNodeId = getMyNodeId();
        Folder folder = new Folder();
        folder.setName(folderName);
        folder.setNodeType(TYPE_CM_FOLDER);
        HttpResponse response = post(getNodeChildrenUrl(myNodeId), toJsonAsStringNonNull(folder), 201);
        Folder folderResponse = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        // Check the upload response
        assertEquals(folderName, folderResponse.getName());
        assertEquals(TYPE_CM_FOLDER, folderResponse.getNodeType());

        // Let's now create an empty file within our home folder.
        String fileName = "myfile" + GUID.generate() + ".txt";
        Document document = new Document();
        document.setName(fileName);
        document.setNodeType(TYPE_CM_CONTENT);
        response = post(getNodeChildrenUrl(myNodeId), toJsonAsStringNonNull(document), 201);
        Document documentResponse = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        // Check the upload response
        assertEquals(fileName, documentResponse.getName());
        assertEquals(TYPE_CM_CONTENT, documentResponse.getNodeType());

        // multipart/form-data upload with known parameters
        /*
         * case "name": case "filedata": case "autorename": case "nodetype":
         * case "overwrite": case "majorversion": case "comment": case
         * "relativepath": case "renditions":
         */
        fileName = "quick-2.pdf";
        File file = getResourceFile(fileName);

        MultiPartBuilder multiPartBuilder = MultiPartBuilder.create().setFileData(new FileData(fileName, file));
        multiPartBuilder.setAutoRename(true);
        multiPartBuilder.setNodeType(TYPE_CM_CONTENT);
        multiPartBuilder.setOverwrite(false);
        multiPartBuilder.setMajorVersion(true);
        multiPartBuilder.setDescription("test");
        // multiPartBuilder.setRelativePath("/");
        multiPartBuilder.setRenditions(Collections.singletonList("doclib"));
        MultiPartRequest reqBody = multiPartBuilder.build();
        response = post(getNodeChildrenUrl(myNodeId), reqBody.getBody(), null, reqBody.getContentType(), 201);
        document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        ContentInfo contentInfo = document.getContent();
        assertEquals(MimetypeMap.MIMETYPE_PDF, contentInfo.getMimeType());
        assertEquals("UTF-8", contentInfo.getEncoding());

        // multipart/form-data upload with unknown parameters
        /*
         * 
         * setPropeties custom
         */

        document.setNodeType("custom:destination");
        multiPartBuilder = MultiPartBuilder.create().setFileData(new FileData(fileName, file));
        multiPartBuilder.setAutoRename(true);
        multiPartBuilder.setNodeType("custom:destination");
        Map<String, String> props = new MultiValueMap();
        props.put("cm:title", "test title");
        props.put("custom:locations", "loc1");
        props.put("custom:locations", "loc2");
        props.put("custom:locations", "loc3");
        multiPartBuilder.setProperties(props);

        reqBody = multiPartBuilder.build();
        response = post(getNodeChildrenUrl(myNodeId), reqBody.getBody(), null, reqBody.getContentType(), 201);
        document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        contentInfo = document.getContent();
        assertEquals(MimetypeMap.MIMETYPE_PDF, contentInfo.getMimeType());
        assertEquals("UTF-8", contentInfo.getEncoding());

    }
    
    @Test
    public void updatePropertiesMultivalueTest() throws Exception
    {
        setRequestContext(user1);
        
        String myNodeId = getMyNodeId();
        // create a multiple-value field from multipart\formdata
        String fileName = "myfile" + GUID.generate() + ".txt";
        File file = getResourceFile("quick-2.pdf");
        MultiPartBuilder multiPartBuilder = MultiPartBuilder.create().setFileData(new FileData(fileName, file));
        multiPartBuilder.setAutoRename(true);
        multiPartBuilder.setNodeType("custom:destination");
        multiPartBuilder.setOverwrite(false);
        multiPartBuilder.setMajorVersion(true);
        multiPartBuilder.setDescription("test");
        multiPartBuilder.setRenditions(Collections.singletonList("doclib"));
        Map<String, String> props = new MultiValueMap();
        props.put("cm:title", "test title");
        props.put("custom:locations", "loc1");
        props.put("custom:locations", "loc2");
        props.put("custom:locations", "loc3");
        multiPartBuilder.setProperties(props);

        MultiPartRequest reqBody = multiPartBuilder.build();
        HttpResponse response = post(getNodeChildrenUrl(myNodeId), reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document documentResponse = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        //build the multiple-value as json array       
        String jsonUpdate = "{" +
         	         "\"name\":\"My Other Folder\","+
         	         "\"nodeType\":\"custom:destination\"," +
           	          "\"properties\":"+
           	          "{" +
           	          		"\"cm:title\":\"Folder title\","+
           	          		"\"cm:description\":\"This is an important folder\"," +
           	          		"\"custom:locations\":["+
       	                                 "\"location X\","+
       	                                 "\"location Y\""+
       	                                 "]" +
           	          "}" +
         	      "}";
         
        response = put(URL_NODES, documentResponse.getId(), jsonUpdate, null, 200);

        Node nodeUpdateResponse = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        Map<String, Object> propUpdateResponse = nodeUpdateResponse.getProperties();
        assertNotNull(propUpdateResponse.get("custom:locations"));
        assertTrue(((ArrayList) (propUpdateResponse.get("custom:locations"))).size() == 2);

        // build the multiple-value as array
        Map<String, Object> properties = new HashMap();
        List locations = new ArrayList<String>();
        locations.add("location X1");
        properties.put("custom:locations", locations);
        Node nodeUpdate = new Node();
        nodeUpdate.setProperties(properties);

        response = put(URL_NODES, documentResponse.getId(), toJsonAsStringNonNull(nodeUpdate), null, 200);

        nodeUpdateResponse = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        propUpdateResponse = nodeUpdateResponse.getProperties();
        assertNotNull(propUpdateResponse.get("custom:locations"));
        assertTrue(((ArrayList) (propUpdateResponse.get("custom:locations"))).size() == 1);
    }

    @Test
    public void versioningEnabledMultipartNodeCreationTest() throws Exception
    {
        setRequestContext(user1);
        String myNodeId = getMyNodeId();
        // Test Scenarios:
        // 1:  majorVersion not set -  versioningEnabled not set  Expect: MAJOR version
        // 2:  majorVersion not set -  versioningEnabled false    Expect: versioning disabled
        // 3:  majorVersion true    -  versioningEnabled false    Expect: versioning disabled
        // 4:  majorVersion false   -  versioningEnabled false    Expect: versioning disabled
        // 5:  majorVersion not set -  versioningEnabled true     Expect: MAJOR version
        // 6:  majorVersion true    -  versioningEnabled true     Expect: MAJOR version
        // 7:  majorVersion false   -  versioningEnabled true     Expect: Minor version
        // 8:  majorVersion not set -  versioningEnabled False    Expect: versioning disabled
        // 9:  majorVersion not set -  versioningEnabled invalid   Expect: MAJOR version

        // Scenario 1:
        String fileName = "myfile" + UUID.randomUUID() + ".txt";
        File file = getResourceFile("quick-2.pdf");
        MultiPartBuilder multiPartBuilder = MultiPartBuilder.create().setFileData(new FileData(fileName, file));

        MultiPartRequest reqBody = multiPartBuilder.build();
        HttpResponse response = post(getNodeChildrenUrl(myNodeId), reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document documentResponse = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        // Default behaviour, expect to be MAJOR Version 1.0
        Map<String, Object> documentProperties = documentResponse.getProperties();
        assertEquals(2, documentProperties.size());
        assertEquals("MAJOR", documentProperties.get("cm:versionType"));
        assertEquals("1.0", documentProperties.get("cm:versionLabel"));

        // Scenario 2:
        fileName = "myfile" + UUID.randomUUID() + ".txt";
        multiPartBuilder = MultiPartBuilder.create().setFileData(new FileData(fileName, file));
        multiPartBuilder.setVersioningEnabled("false");

        reqBody = multiPartBuilder.build();
        response = post(getNodeChildrenUrl(myNodeId), reqBody.getBody(), null, reqBody.getContentType(), 201);
        documentResponse = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        documentProperties = documentResponse.getProperties();
        assertNull(documentProperties);

        // Scenario 3:
        fileName = "myfile" + UUID.randomUUID() + ".txt";
        multiPartBuilder = MultiPartBuilder.create().setFileData(new FileData(fileName, file));
        multiPartBuilder.setMajorVersion(true);
        multiPartBuilder.setVersioningEnabled("false");

        reqBody = multiPartBuilder.build();
        response = post(getNodeChildrenUrl(myNodeId), reqBody.getBody(), null, reqBody.getContentType(), 201);
        documentResponse = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        documentProperties = documentResponse.getProperties();
        assertNull(documentProperties);

        // Scenario 4:
        fileName = "myfile" + UUID.randomUUID() + ".txt";
        multiPartBuilder = MultiPartBuilder.create().setFileData(new FileData(fileName, file));
        multiPartBuilder.setMajorVersion(false);
        multiPartBuilder.setVersioningEnabled("false");

        reqBody = multiPartBuilder.build();
        response = post(getNodeChildrenUrl(myNodeId), reqBody.getBody(), null, reqBody.getContentType(), 201);
        documentResponse = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        documentProperties = documentResponse.getProperties();
        assertNull(documentProperties);

        // Scenario 5:
        fileName = "myfile" + UUID.randomUUID() + ".txt";
        multiPartBuilder = MultiPartBuilder.create().setFileData(new FileData(fileName, file));
        multiPartBuilder.setVersioningEnabled("true");

        reqBody = multiPartBuilder.build();
        response = post(getNodeChildrenUrl(myNodeId), reqBody.getBody(), null, reqBody.getContentType(), 201);
        documentResponse = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        documentProperties = documentResponse.getProperties();
        assertEquals(2, documentProperties.size());
        assertEquals("MAJOR", documentProperties.get("cm:versionType"));
        assertEquals("1.0", documentProperties.get("cm:versionLabel"));

        // Scenario 6:
        fileName = "myfile" + UUID.randomUUID() + ".txt";
        multiPartBuilder = MultiPartBuilder.create().setFileData(new FileData(fileName, file));
        multiPartBuilder.setMajorVersion(true);
        multiPartBuilder.setVersioningEnabled("true");

        reqBody = multiPartBuilder.build();
        response = post(getNodeChildrenUrl(myNodeId), reqBody.getBody(), null, reqBody.getContentType(), 201);
        documentResponse = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        documentProperties = documentResponse.getProperties();
        assertEquals(2, documentProperties.size());
        assertEquals("MAJOR", documentProperties.get("cm:versionType"));
        assertEquals("1.0", documentProperties.get("cm:versionLabel"));

        // Scenario 7:
        fileName = "myfile" + UUID.randomUUID() + ".txt";
        multiPartBuilder = MultiPartBuilder.create().setFileData(new FileData(fileName, file));
        multiPartBuilder.setMajorVersion(false);
        multiPartBuilder.setVersioningEnabled("true");

        reqBody = multiPartBuilder.build();
        response = post(getNodeChildrenUrl(myNodeId), reqBody.getBody(), null, reqBody.getContentType(), 201);
        documentResponse = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        documentProperties = documentResponse.getProperties();
        assertEquals(2, documentProperties.size());
        assertEquals("MINOR", documentProperties.get("cm:versionType"));
        assertEquals("0.1", documentProperties.get("cm:versionLabel"));

        // Scenario 8:
        fileName = "myfile" + UUID.randomUUID() + ".txt";
        multiPartBuilder = MultiPartBuilder.create().setFileData(new FileData(fileName, file));
        multiPartBuilder.setVersioningEnabled("False");

        reqBody = multiPartBuilder.build();
        response = post(getNodeChildrenUrl(myNodeId), reqBody.getBody(), null, reqBody.getContentType(), 201);
        documentResponse = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        documentProperties = documentResponse.getProperties();
        assertNull(documentProperties);

        // Scenario 9:
        fileName = "myfile" + UUID.randomUUID() + ".txt";
        multiPartBuilder = MultiPartBuilder.create().setFileData(new FileData(fileName, file));
        multiPartBuilder.setVersioningEnabled("invalid");

        reqBody = multiPartBuilder.build();
        response = post(getNodeChildrenUrl(myNodeId), reqBody.getBody(), null, reqBody.getContentType(), 201);
        documentResponse = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        documentProperties = documentResponse.getProperties();
        assertEquals("MAJOR", documentProperties.get("cm:versionType"));
        assertEquals("1.0", documentProperties.get("cm:versionLabel"));
    }

    @Test
    public void versioningEnabledJSONNodeCreationTest() throws Exception
    {
        setRequestContext(user1);
        String myNodeId = getMyNodeId();

        // Test Scenarios:
        // 1: majorVersion not set -  versioningEnabled not set  Expect: versioning disabled
        // 2: majorVersion not set -  versioningEnabled false    Expect: versioning disabled
        // 3: majorVersion true    -  versioningEnabled false    Expect: versioning disabled
        // 4: majorVersion false   -  versioningEnabled false    Expect: versioning disabled
        // 5: majorVersion not set -  versioningEnabled true     Expect: MAJOR version
        // 6: majorVersion true    -  versioningEnabled true     Expect: MAJOR version
        // 7: majorVersion false   -  versioningEnabled true     Expect: Minor version
        // 8: majorVersion not set -  versioningEnabled False    Expect: versioning disabled
        // 9: majorVersion not set -  versioningEnabled invalid   Expect: versioning disabled
        // 10 majorVersion not set -  versioningenabled true     Expect: versioning disabled

        Document d1 = new Document();
        Map<String, String> requestHeaders = new HashMap<>();

        //Scenario 1:
        d1.setName("testDoc" + UUID.randomUUID());
        d1.setNodeType(TYPE_CM_CONTENT);

        HttpResponse response = post(getNodeChildrenUrl(myNodeId), toJsonAsStringNonNull(d1),requestHeaders, null, null, 201);
        Document documentResponse = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        Map<String, Object> documentProperties = documentResponse.getProperties();
        assertNull(documentProperties);

        //Scenario 2:
        d1.setName("testDoc" + UUID.randomUUID());
        requestHeaders = new HashMap<>();
        requestHeaders.put("versioningEnabled","false");

        response = post(getNodeChildrenUrl(myNodeId), toJsonAsStringNonNull(d1),requestHeaders, null, null, 201);
        documentResponse = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        documentProperties = documentResponse.getProperties();
        assertNull(documentProperties);

        //Scenario 3:
        d1.setName("testDoc" + UUID.randomUUID());
        requestHeaders = new HashMap<>();
        requestHeaders.put("versioningEnabled","false");
        requestHeaders.put("majorVersion","true");

        response = post(getNodeChildrenUrl(myNodeId), toJsonAsStringNonNull(d1),requestHeaders, null, null, 201);
        documentResponse = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        documentProperties = documentResponse.getProperties();
        assertNull(documentProperties);

        //Scenario 4:
        d1.setName("testDoc" + UUID.randomUUID());
        requestHeaders = new HashMap<>();
        requestHeaders.put("versioningEnabled","false");
        requestHeaders.put("majorVersion","false");

        response = post(getNodeChildrenUrl(myNodeId), toJsonAsStringNonNull(d1),requestHeaders, null, null, 201);
        documentResponse = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        documentProperties = documentResponse.getProperties();
        assertNull(documentProperties);

        //Scenario 5:
        d1.setName("testDoc" + UUID.randomUUID());
        requestHeaders = new HashMap<>();
        requestHeaders.put("versioningEnabled","true");

        response = post(getNodeChildrenUrl(myNodeId), toJsonAsStringNonNull(d1),requestHeaders, null, null, 201);
        documentResponse = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        documentProperties = documentResponse.getProperties();
        assertEquals("MAJOR", documentProperties.get("cm:versionType"));
        assertEquals("1.0", documentProperties.get("cm:versionLabel"));

        //Scenario 6:
        d1.setName("testDoc" + UUID.randomUUID());
        requestHeaders = new HashMap<>();
        requestHeaders.put("versioningEnabled","true");
        requestHeaders.put("majorVersion","true");

        response = post(getNodeChildrenUrl(myNodeId), toJsonAsStringNonNull(d1),requestHeaders, null, null, 201);
        documentResponse = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        documentProperties = documentResponse.getProperties();
        assertEquals("MAJOR", documentProperties.get("cm:versionType"));
        assertEquals("1.0", documentProperties.get("cm:versionLabel"));

        //Scenario 7:
        d1.setName("testDoc" + UUID.randomUUID());
        requestHeaders = new HashMap<>();
        requestHeaders.put("versioningEnabled","true");
        requestHeaders.put("majorVersion","false");

        response = post(getNodeChildrenUrl(myNodeId), toJsonAsStringNonNull(d1),requestHeaders, null, null, 201);
        documentResponse = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        documentProperties = documentResponse.getProperties();
        assertEquals("MINOR", documentProperties.get("cm:versionType"));
        assertEquals("0.1", documentProperties.get("cm:versionLabel"));

        //Scenario 8:
        d1.setName("testDoc" + UUID.randomUUID());
        requestHeaders = new HashMap<>();
        requestHeaders.put("versioningEnabled","False");

        response = post(getNodeChildrenUrl(myNodeId), toJsonAsStringNonNull(d1),requestHeaders, null, null, 201);
        documentResponse = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        documentProperties = documentResponse.getProperties();
        assertNull(documentProperties);

        //Scenario 9:
        d1.setName("testDoc" + UUID.randomUUID());
        requestHeaders = new HashMap<>();
        requestHeaders.put("versioningEnabled","invalid");

        response = post(getNodeChildrenUrl(myNodeId), toJsonAsStringNonNull(d1),requestHeaders, null, null, 201);
        documentResponse = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        documentProperties = documentResponse.getProperties();
        assertNull(documentProperties);

        //Scenario 10:
        d1.setName("testDoc" + UUID.randomUUID());
        requestHeaders = new HashMap<>();
        requestHeaders.put("versioningenabled","true");

        response = post(getNodeChildrenUrl(myNodeId), toJsonAsStringNonNull(d1),requestHeaders, null, null, 201);
        documentResponse = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        documentProperties = documentResponse.getProperties();
        assertNull(documentProperties);
    }

    @Test public void testAuditableProperties() throws Exception
    {
        setRequestContext(user1);
        String myNodeId = getMyNodeId();

        UserInfo expectedUser = new UserInfo(user1);

        String auditCreator = "unacceptable creator";
        String auditCreated = "unacceptable created";
        String auditModifier = "unacceptable modifier";
        String auditModified = "unacceptable modified";
        String auditAccessed = "unacceptable accessed";

        Map<String, Object> auditableProperties = new HashMap<>();
        auditableProperties.put("cm:creator", auditCreator);
        auditableProperties.put("cm:created", auditCreated);
        auditableProperties.put("cm:modifier", auditModifier);
        auditableProperties.put("cm:modified", auditModified);
        auditableProperties.put("cm:accessed", auditAccessed);

        Map<String, Object> systemProperties = new HashMap<>();
        systemProperties.put("sys:node:uuid", "someRandomID");

        //create folder node
        Node node = new Node();
        node.setName("folderName");
        node.setNodeType(TYPE_CM_FOLDER);
        node.setProperties(auditableProperties);
        HttpResponse response = post(getNodeChildrenUrl(myNodeId), RestApiUtil.toJsonAsStringNonNull(node), 400);

        node.setProperties(systemProperties);
        post(getNodeChildrenUrl(myNodeId), RestApiUtil.toJsonAsStringNonNull(node), 400);

        node.setProperties(new HashMap<>());
        response = post(getNodeChildrenUrl(myNodeId), RestApiUtil.toJsonAsStringNonNull(node), 201);
        Node createdFolder =  RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertEquals(createdFolder.getCreatedByUser().getId(), expectedUser.getId());
        validateAuditableProperties(auditableProperties, createdFolder);

        //update folder node
        createdFolder.setProperties(auditableProperties);
        put(URL_NODES, createdFolder.getId(), toJsonAsStringNonNull(createdFolder), null, 400);

        Map<String, Object> otherProperties = new HashMap<>();
        otherProperties.put("cm:title", "newTitle");
        createdFolder.setProperties(otherProperties);
        response = put(URL_NODES, createdFolder.getId(), toJsonAsStringNonNull(createdFolder), null, 200);
        Node updateFolderResponse = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        validateAuditableProperties(auditableProperties, updateFolderResponse);
    }

    private void validateAuditableProperties(Map<String, Object> givenProperties, Node node)
    {
        assertFalse(givenProperties.get("cm:creator").equals(node.getCreatedByUser().getDisplayName()));
        assertFalse(givenProperties.get("cm:created").equals(node.getCreatedAt().getTime()));
        assertFalse(givenProperties.get("cm:modifier").equals(node.getModifiedAt().getTime()));
        assertFalse(givenProperties.get("cm:modified").equals(node.getModifiedByUser().getDisplayName()));
    }

    @Test public void testPrimaryPath() throws Exception
    {
        setRequestContext(user1);
        AuthenticationUtil.setFullyAuthenticatedUser(user1);
        String myNodeId = getMyNodeId();

        String nameA = "folder_A";
        String nameA01 = "folder_A01";
        String nameA02 = "folder_A02";
        String nameA03 = "folder_A03";
        String nameB = "folder_B";

        // /Company Home/User Homes/user<timestamp>/folder_A/folder_B
        Folder folderA = createFolder(myNodeId, nameA);
        Folder folderB = createFolder(folderA.getId(), nameB);
        NodeRef folderANodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, folderA.getId());
        NodeRef folderBNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, folderB.getId());

        folderA.setName(nameA01);
        put(URL_NODES, folderA.getId(), toJsonAsStringNonNull(folderA), null, 200);
        Path folderAPath = nodeService.getPath(folderANodeRef);
        Path.ChildAssocElement pathElement = (Path.ChildAssocElement) folderAPath.last();
        String localNameForFolderA = pathElement.getRef().getQName().getLocalName();
        assertFalse(nameA.equals(localNameForFolderA));

        folderA.setName(nameA02);
        Map<String, Object> properties = new HashMap<>();
        properties.put("cm:name", nameA03);
        folderA.setProperties(properties);
        put(URL_NODES, folderA.getId(), toJsonAsStringNonNull(folderA), null, 200);
        folderAPath = nodeService.getPath(folderANodeRef);
        pathElement = (Path.ChildAssocElement) folderAPath.last();
        localNameForFolderA = pathElement.getRef().getQName().getLocalName();
        assertFalse(nameA.equals(localNameForFolderA));
        assertFalse(nameA03.equals(localNameForFolderA));
        assertTrue(nameA02.equals(localNameForFolderA));

        Path folderBPath = nodeService.getPath(folderBNodeRef);
        Path.ChildAssocElement pathBLastElement = (Path.ChildAssocElement) folderBPath.last();
        String currentPath = folderBPath.toDisplayPath(nodeService, permissionService) + "/" + pathBLastElement.getRef().getQName().getLocalName();
        String expectedPath = "/Company Home/User Homes/" + user1 + "/" + nameA02 + "/" + nameB;
        assertTrue(currentPath.equals(expectedPath));
    }

    @Test
    public void testPrimaryPathVersion() throws Exception
    {
        setRequestContext(user1);
        AuthenticationUtil.setFullyAuthenticatedUser(user1);
        String myNodeId = getMyNodeId();

        // /Company Home/User Homes/user<timestamp>/folder_A
        String folderName = "folder_A";
        Folder folder = createFolder(myNodeId, folderName);
        NodeRef folderNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, folder.getId());

        // /Company Home/User Homes/user<timestamp>/folder_A/testDoc<GUID>
        String docName = "testDoc" + GUID.generate();
        Document doc = new Document();
        doc.setName(docName);
        doc.setNodeType(TYPE_CM_CONTENT);
        HttpResponse response = post(getNodeChildrenUrl(folderNodeRef.getId()), toJsonAsStringNonNull(doc), 201);
        Document docResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        NodeRef docNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, docResp.getId());

        // Checks that current path and name match
        String expectedPath1 = "/Company Home/User Homes/" + user1 + "/" + folderName + "/" + docName;
        Path docPath1 = nodeService.getPath(docNodeRef);
        Path.ChildAssocElement docPathLast1 = (Path.ChildAssocElement) docPath1.last();
        String docLocalName1 = docPathLast1.getRef().getQName().getLocalName();
        String currentPath1 = docPath1.toDisplayPath(nodeService, permissionService) + "/" + docLocalName1;
        assertTrue(docName.equals(docLocalName1));
        assertTrue(expectedPath1.equals(currentPath1));

        // Upload document new content supplying a different name
        String docName2 = "testDoc2" + GUID.generate();
        Map<String, String> params = new HashMap<>();
        params.put("name", docName2);
        Document docResp2 = updateTextFileWithRandomContent(docNodeRef.getId(), 1024L, params);
        NodeRef docNodeRef2 = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, docResp2.getId());

        // Checks new path and name after new version upload
        String expectedPath2 = "/Company Home/User Homes/" + user1 + "/" + folderName + "/" + docName2;
        Path docPath2 = nodeService.getPath(docNodeRef2);
        Path.ChildAssocElement docPathLast2 = (Path.ChildAssocElement) docPath2.last();
        String docLocalName2 = docPathLast2.getRef().getQName().getLocalName();
        String currentPath2 = docPath2.toDisplayPath(nodeService, permissionService) + "/" + docLocalName2;
        assertFalse(docLocalName1.equals(docLocalName2));
        assertTrue(docName2.equals(docLocalName2));
        assertFalse(expectedPath1.equals(currentPath2));
        assertTrue(expectedPath2.equals(currentPath2));
    }

    private String getDataDictionaryNodeId() throws Exception
    {
        Map params = new HashMap<>();
        params.put(Nodes.PARAM_RELATIVE_PATH, "/Data Dictionary");
        HttpResponse response = getSingle(NodesEntityResource.class, getRootNodeId(), params, 200);
        Node nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        return nodeResp.getId();
    }

    private String getSitesNodeId() throws Exception
    {
        Map params = new HashMap<>();
        params.put(Nodes.PARAM_RELATIVE_PATH, "/Sites");
        HttpResponse response = getSingle(NodesEntityResource.class, getRootNodeId(), params, 200);
        Node nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        return nodeResp.getId();
    }

    private String createFolder() throws Exception
    {
        String folderName = "testPermissionsFolder-" + GUID.generate();
        String folderId = createFolder(Nodes.PATH_MY, folderName).getId();
        return getNodeChildrenUrl(folderId);
    }

    /**
     * Created an empty document in the given url path
     * 
     * @param url
     * @return
     * @throws Exception
     */
    private String createDocument(String url) throws Exception
    {
        Document d1 = new Document();
        d1.setName("testDoc" + GUID.generate());
        d1.setNodeType(TYPE_CM_CONTENT);

        HttpResponse response = post(url, toJsonAsStringNonNull(d1), 201);
        Document documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        return documentResp.getId();
    }

    /**
     * Created an empty document in the given url path
     * 
     * @param url
     * @return
     * @throws Exception
     */
    private String createDocument(String url, NodePermissions perms) throws Exception
    {
        Document d1 = new Document();
        d1.setName("testDoc" + GUID.generate());
        d1.setNodeType(TYPE_CM_CONTENT);
        d1.setPermissions(perms);
        
        HttpResponse response = post(url, toJsonAsStringNonNull(d1), 201);
        Document documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        return documentResp.getId();
    }

    private void validatePermissions(NodeRef nodeRef, List<NodePermissions.NodePermission> expectedPermissions)
    {
        Set<AccessPermission> permissions = permissionService.getAllSetPermissions(nodeRef);

        for (NodePermissions.NodePermission permission : expectedPermissions)
        {
            String authority = permission.getAuthorityId();
            AccessPermission ap = getPermission(permissions, authority);
            assertNotNull("Permission " + authority + " missing", ap);

            assertEquals(authority, ap.getAuthority());
            comparePermissions(authority, permission, ap);
        }
    }

    private void comparePermissions(String authority, NodePermissions.NodePermission permission, AccessPermission ap)
    {
        assertEquals("Wrong permission for " + authority, permission.getAuthorityId(), ap.getAuthority());
        assertEquals("Wrong permission for " + authority, permission.getName(), ap.getPermission());
        assertEquals("Wrong access status for " + authority, permission.getAccessStatus(), ap.getAccessStatus().toString());
    }

    /**
     * Searches through actual set of permissions
     *
     * @param permissions
     * @param expectedAuthority
     * @return
     */
    private AccessPermission getPermission(Set<AccessPermission> permissions, String expectedAuthority)
    {
        AccessPermission result = null;
        for (AccessPermission ap : permissions)
        {
            if (expectedAuthority.equals(ap.getAuthority()))
            {
                result = ap;
            }
        }
        return result;
    }

    @Override
    public String getScope()
    {
        return "public";
    }

    @Test 
    public void testRetrieveNodeDefinition() throws Exception
    {
        setRequestContext(networkOne.getId(), user1, null);
        
        String node1 = "nodeSample" + RUNID + "_1";
        String node1Type = TYPE_CM_CONTENT;
        Map<String,Object> props = new HashMap<>();
        props.put("cm:title", "add aspect property");
        Node node = createNode(Nodes.PATH_MY, node1, node1Type, props);
        String nodeId = node.getId();
        
        HttpResponse  response = getSingle(NodesEntityResource.class, nodeId, null, 200);
        Node nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNull("Definition should not be retrieved unless included!", nodeResp.getDefinition());

        Map params = new HashMap<>();
        params.put("include", "definition");
        response = getSingle(NodesEntityResource.class, nodeId, params, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        ClassDefinition classDefinition = nodeResp.getDefinition();
        assertNotNull(classDefinition);
        checkDefinitionProperties(classDefinition.getProperties());
    }
    
    private void checkDefinitionProperties(List<PropertyDefinition> properties)
    {
        assertNotNull(properties);
        shouldNotContainSystemProperties(properties);
        shouldContainParentProperties(properties);
        shouldContainAspectProperties(properties);

        PropertyDefinition testProperty = properties.stream().
                filter(property ->
                        property.getId().equals("cm:name"))
                .findFirst()
                .get();
        assertNotNull(testProperty);
        assertEquals("Name", testProperty.getTitle());
        assertEquals("d:text", testProperty.getDataType());
        assertEquals("Name", testProperty.getDescription());
        assertTrue(testProperty.getIsMandatory());
        assertTrue(testProperty.getIsMandatoryEnforced());
        assertFalse(testProperty.getIsMultiValued());
        assertNull(testProperty.getDefaultValue());
        checkPropertyConstraints(testProperty.getConstraints());

    }
    
    private void shouldNotContainSystemProperties(List<PropertyDefinition> properties)
    {
        assertTrue(properties.stream()
                .noneMatch(property -> 
                        property.getId().startsWith(NamespaceService.SYSTEM_MODEL_PREFIX) ||
                                property.getId().equals(ContentModel.PROP_CONTENT.toPrefixString(namespaceService))));
    }
    
    private void shouldContainParentProperties(List<PropertyDefinition> properties)
    {
        assertTrue(properties.stream()
                .anyMatch(property -> 
                        property.getId().equals("cm:name")));
    }

    private void shouldContainAspectProperties(List<PropertyDefinition> properties)
    {
        PropertyDefinition mandatoryAspectProperty = properties.stream()
                .filter(property -> property.getId().equals("cm:created"))
                .findFirst()
                .get();
        assertNotNull(mandatoryAspectProperty);

        PropertyDefinition nodeAspectProperty = properties.stream()
                .filter(property -> property.getId().equals("cm:title"))
                .findFirst()
                .get();
        assertNotNull(nodeAspectProperty);
    }

    private void checkPropertyConstraints(List<ConstraintDefinition> constraints)
    {
        assertNotNull(constraints);
        ConstraintDefinition constraintDefinition = constraints.stream()
                .filter(constraint -> constraint.getId().equals("cm:filename"))
                .findFirst()
                .get();
        assertNotNull(constraintDefinition);
        assertEquals("REGEX", constraintDefinition.getType());
        Map<String, Object> constraintParameters = constraintDefinition.getParameters();
        assertNotNull(constraintParameters);
        assertNull(constraintDefinition.getDescription());
        assertNull(constraintDefinition.getTitle());
        assertEquals(2, constraintParameters.size());
        assertEquals("(.*[\\\"\\*\\\\\\>\\<\\?\\/\\:\\|]+.*)|(.*[\\.]?.*[\\.]+$)|(.*[ ]+$)", constraintParameters.get("expression"));
        assertFalse((Boolean) constraintParameters.get("requiresMatch"));
    }

    @Test
    public void testRequestContentDirectUrl() throws Exception
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

        final String contentNodeId = document.getId();

        // Check the upload response
        assertEquals(fileName, document.getName());
        ContentInfo contentInfo = document.getContent();
        assertNotNull(contentInfo);
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, contentInfo.getMimeType());

        HttpResponse dauResponse = post(getRequestContentDirectUrl(contentNodeId), null, null, null, null, 501);
    }

    @Test
    public void testRequestContentDirectUrlClientErrorResponseForNodes() throws Exception
    {
        enableRestDirectAccessUrls();

        //Node does not exist
        setRequestContext(user1);

        HttpResponse nodeDoesNotExistResponse = post(getRequestContentDirectUrl("non-existing-node-id"), null, 404);

        //Node is not a file
        String folderId = createFolder(tDocLibNodeId, "some-folder-name").getId();
        HttpResponse nodeIsNotAFileReponse = post(getRequestContentDirectUrl(folderId), null, 400);

        disableRestDirectAccessUrls();
    }

    @Test
    public void testRequestContentDirectUrlClientErrorResponseForVersions() throws Exception
    {
        enableRestDirectAccessUrls();
        // Create a document
        setRequestContext(user1);

        String folderNodeId = createUniqueFolder(getMyNodeId());
        String contentNodeId = createUniqueContent(folderNodeId);

        // Verify versions
        HttpResponse versionIdDoesNotExistReponse = post(getRequestVersionDirectAccessUrl(contentNodeId, "1.2"), null, null, null, null, 404);
        HttpResponse versionIdInvalidReponse = post(getRequestVersionDirectAccessUrl(contentNodeId, "invalid-version"), null, null, null, null, 404);

        disableRestDirectAccessUrls();
    }

    @Test
    public void testRequestContentDirectUrlClientErrorResponseForRenditions() throws Exception
    {
        enableRestDirectAccessUrls();
        // Create a document
        setRequestContext(user1);

        String folderNodeId = createUniqueFolder(getMyNodeId());
        String contentNodeId = createUniqueContent(folderNodeId);

        // Verify renditions
        HttpResponse renditionIdDoesNotExistReponse = post(getRequestRenditionDirectAccessUrl(contentNodeId, "pdf"), null, null, null, null, 404);
        HttpResponse renditionIdInvalidReponse = post(getRequestRenditionDirectAccessUrl(contentNodeId, "invalid-rendition"), null, null, null, null, 404);

        disableRestDirectAccessUrls();
    }

    @Test
    public void testRequestContentDirectUrlClientErrorResponseForDeletion() throws Exception
    {
        enableRestDirectAccessUrls();
        // Create a document
        setRequestContext(user1);

        String folderNodeId = createUniqueFolder(getMyNodeId());
        String contentNodeId = createUniqueContent(folderNodeId);

        // Verify deletion
        HttpResponse nodeNotDeletedReponse = post(getRequestArchivedContentDirectUrl(contentNodeId), null, null, null, null, 404);

        disableRestDirectAccessUrls();
    }

    @Test
    public void testRequestDeleteRendition() throws Exception
    {
        setRequestContext(networkOne.getId(), user1, null);

        String myNodeId = getMyNodeId();

        // Create multipart request - txt file
        String renditionName = "pdf";
        String fileName = "quick-1.txt";
        File file = getResourceFile(fileName);
        MultiPartRequest reqBody = MultiPartBuilder.create()
                                                   .setFileData(new FileData(fileName, file))
                                                   .setRenditions(Collections.singletonList(renditionName))
                                                   .build();

        //Upload file to user home node
        HttpResponse response = post(getNodeChildrenUrl(myNodeId), reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        String contentNodeId = document.getId();

        // wait and check that rendition is created ...
        Rendition rendition = waitAndGetRendition(contentNodeId, null, renditionName);
        assertNotNull(rendition);
        assertEquals(Rendition.RenditionStatus.CREATED, rendition.getStatus());

        //clean rendition
        delete(getNodeRenditionIdUrl(contentNodeId, renditionName), null, null, null, null, 204);
        //retry to double-check deletion
        delete(getNodeRenditionIdUrl(contentNodeId, renditionName), null, null, null, null, 404);

        //check if rendition was cleaned
        HttpResponse getResponse = getSingle(getNodeRenditionIdUrl(contentNodeId, renditionName), null,  200);
        Rendition renditionDeleted = RestApiUtil.parseRestApiEntry(getResponse.getJsonResponse(), Rendition.class);
        assertNotNull(renditionDeleted);
        assertEquals(Rendition.RenditionStatus.NOT_CREATED, renditionDeleted.getStatus());
    }

    @Test
    public void testRequestVersionDeleteRendition() throws Exception
    {
        setRequestContext(networkOne.getId(), user1, null);

        String myNodeId = getMyNodeId();

        // Create multipart request - txt file
        String renditionName = "pdf";
        String fileName = "quick-1.txt";
        File file = getResourceFile(fileName);
        MultiPartRequest reqBody = MultiPartBuilder.create()
                                                   .setFileData(new FileData(fileName, file))
                                                   .setRenditions(Collections.singletonList(renditionName))
                                                   .build();

        //Upload file to user home node
        HttpResponse response = post(getNodeChildrenUrl(myNodeId), reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        String contentNodeId = document.getId();

        //Update file to newer version
        String content = "The quick brown fox jumps over the lazy dog\n the lazy dog jumps over the quick brown fox";
        Map<String, String> params = new HashMap<>();
        params.put("comment", "my version ");

        document = updateTextFile(contentNodeId, content, params);
        assertTrue(document.getAspectNames().contains("cm:versionable"));
        assertNotNull(document.getProperties());
        assertEquals("1.1", document.getProperties().get("cm:versionLabel"));

        // create rendition for old version and check that rendition is created ...
        Rendition renditionUpdated = createAndGetRendition(contentNodeId, "1.0", renditionName);
        assertNotNull(renditionUpdated);
        assertEquals(Rendition.RenditionStatus.CREATED, renditionUpdated.getStatus());

        //clean rendition
        delete(getNodeVersionRenditionIdUrl(contentNodeId, "1.0", renditionName), null, null, null, null, 204);
        //retry to double-check deletion
        delete(getNodeVersionRenditionIdUrl(contentNodeId, "1.0", renditionName), null, null, null, null, 404);

        //check if rendition was cleaned
        HttpResponse getResponse = getSingle(getNodeVersionRenditionIdUrl(contentNodeId, "1.0", renditionName), null,  200);
        Rendition renditionDeleted = RestApiUtil.parseRestApiEntry(getResponse.getJsonResponse(), Rendition.class);
        assertNotNull(renditionDeleted);
        assertEquals(Rendition.RenditionStatus.NOT_CREATED, renditionDeleted.getStatus());
    }

}

