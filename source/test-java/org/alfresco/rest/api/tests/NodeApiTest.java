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

import static org.alfresco.rest.api.tests.util.RestApiUtil.parsePaging;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.NodeTarget;
import org.alfresco.rest.api.nodes.NodesEntityResource;
import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.alfresco.rest.api.tests.RepoService.TestPerson;
import org.alfresco.rest.api.tests.RepoService.TestSite;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.alfresco.rest.api.tests.client.PublicApiClient.ExpectedPaging;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.PublicApiHttpClient.BinaryPayload;
import org.alfresco.rest.api.tests.client.data.ContentInfo;
import org.alfresco.rest.api.tests.client.data.Document;
import org.alfresco.rest.api.tests.client.data.Folder;
import org.alfresco.rest.api.tests.client.data.Node;
import org.alfresco.rest.api.tests.client.data.PathInfo;
import org.alfresco.rest.api.tests.client.data.PathInfo.ElementInfo;
import org.alfresco.rest.api.tests.client.data.SiteRole;
import org.alfresco.rest.api.tests.client.data.UserInfo;
import org.alfresco.rest.api.tests.util.JacksonUtil;
import org.alfresco.rest.api.tests.util.MultiPartBuilder;
import org.alfresco.rest.api.tests.util.MultiPartBuilder.FileData;
import org.alfresco.rest.api.tests.util.MultiPartBuilder.MultiPartRequest;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.rest.framework.jacksonextensions.JacksonHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.TempFileProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

/**
 * API tests for:
 * <ul>
 * <li> {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>} </li>
 * <li> {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/children} </li>
 * </ul>
 *
 * TODO
 * - improve test 'fwk' to enable api tests to be run against remote repo (rather than embedded jetty)
 * - requires replacement of non-remote calls (eg. repoService, siteService, permissionService) with calls to remote (preferably public) apis
 *
 * @author Jamal Kaabi-Mofrad
 * @author janv
 */
public class NodeApiTest extends AbstractBaseApiTest
{
    private static final String RESOURCE_PREFIX = "publicapi/upload/";

    private static final String URL_NODES = "nodes/";

    /**
     * User one from network one
     */
    private TestPerson userOneN1;
    /**
     * User two from network one
     */
    private TestPerson userTwoN1;
    /**
     * Private site of user one from network one
     */
    private TestSite userOneN1Site;
    private String user1;
    private String user2;
    private List<String> users = new ArrayList<>();

    protected MutableAuthenticationService authenticationService;
    protected PersonService personService;
    protected JacksonUtil jacksonUtil;
    protected PermissionService permissionService;


    @Before
    public void setup() throws Exception
    {
        authenticationService = applicationContext.getBean("authenticationService", MutableAuthenticationService.class);
        personService = applicationContext.getBean("personService", PersonService.class);
        jacksonUtil = new JacksonUtil(applicationContext.getBean("jsonHelper", JacksonHelper.class));
        permissionService = applicationContext.getBean("permissionService", PermissionService.class);

        user1 = createUser("user1" + System.currentTimeMillis());
        user2 = createUser("user2" + System.currentTimeMillis());
        // We just need to clean the on-premise-users,
        // so the tests for the specific network would work.
        users.add(user1);
        users.add(user2);

        TestNetwork networkOne = getTestFixture().getRandomNetwork();
        userOneN1 = networkOne.createUser();
        userTwoN1 = networkOne.createUser();

        userOneN1Site = createSite(networkOne, userOneN1, SiteVisibility.PRIVATE);
    }

    @After
    public void tearDown() throws Exception
    {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        for (final String user : users)
        {
            transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    if (personService.personExists(user))
                    {
                        authenticationService.deleteAuthentication(user);
                        personService.deletePerson(user);
                    }
                    return null;
                }
            });
        }
        users.clear();
        AuthenticationUtil.clearCurrentSecurityContext();
    }

    // root (eg. Company Home for on-prem)
    private String getRootNodeId(String runAsUserId) throws Exception
    {
        HttpResponse response = getSingle(NodesEntityResource.class, runAsUserId, Nodes.PATH_ROOT, null, 200);
        Node node = jacksonUtil.parseEntry(response.getJsonResponse(), Node.class);
        return node.getId();
    }

    // my (eg. User's Home for on-prem)
    private String getMyNodeId(String runAsUserId) throws Exception
    {
        HttpResponse response = getSingle(NodesEntityResource.class, runAsUserId, Nodes.PATH_MY, null, 200);
        Node node = jacksonUtil.parseEntry(response.getJsonResponse(), Node.class);
        return node.getId();
    }

    private Folder createFolder(String runAsUserId, String parentId, String folderName) throws Exception
    {
        return createFolder(runAsUserId, parentId, folderName, null);
    }

    private Folder createFolder(String runAsUserId, String parentId, String folderName, Map<String, Object> props) throws Exception
    {
        return createNode( runAsUserId, parentId, folderName, "cm:folder", props, Folder.class);
    }

    private Node createNode(String runAsUserId, String parentId, String nodeName, String nodeType, Map<String, Object> props) throws Exception
    {
        return createNode( runAsUserId, parentId, nodeName, nodeType, props, Node.class);
    }

    private <T> T createNode(String runAsUserId, String parentId, String nodeName, String nodeType, Map<String, Object> props, Class<T> returnType) throws Exception
    {
        Node n = new Node();
        n.setName(nodeName);
        n.setNodeType(nodeType);
        n.setProperties(props);

        // create node
        HttpResponse response = post(getChildrenUrl(parentId), runAsUserId, toJsonAsStringNonNull(n), 201);

        return RestApiUtil.parseRestApiEntry(response.getJsonResponse(), returnType);
    }

    /**
     * Tests get document library children.
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/children}
     */
    @Test
    public void testListDocLibChildren() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(userOneN1.getId());

        NodeRef docLibNodeRef = userOneN1Site.getContainerNodeRef(("documentLibrary"));

        String folder1 = "folder" + System.currentTimeMillis() + "_1";
        repoService.addToDocumentLibrary(userOneN1Site, folder1, ContentModel.TYPE_FOLDER);

        String folder2 = "folder" + System.currentTimeMillis() + "_2";
        repoService.addToDocumentLibrary(userOneN1Site, folder2, ContentModel.TYPE_FOLDER);

        String content1 = "content" + System.currentTimeMillis() + "_1";
        repoService.addToDocumentLibrary(userOneN1Site, content1, ContentModel.TYPE_CONTENT);

        String content2 = "content" + System.currentTimeMillis() + "_2";
        repoService.addToDocumentLibrary(userOneN1Site, content2, ContentModel.TYPE_CONTENT);

        String forum1 = "forum" + System.currentTimeMillis() + "_1";
        repoService.createObjectOfCustomType(docLibNodeRef, forum1, ForumModel.TYPE_TOPIC.toString());

        Paging paging = getPaging(0, 100);
        HttpResponse response = getAll(getChildrenUrl(docLibNodeRef), userOneN1.getId(), paging, 200);
        List<Node> nodes = jacksonUtil.parseEntries(response.getJsonResponse(), Node.class);
        assertEquals(4, nodes.size()); // forum is part of the default ignored types
        // Paging
        ExpectedPaging expectedPaging = RestApiUtil.parsePaging(response.getJsonResponse());
        assertEquals(4, expectedPaging.getCount().intValue());
        assertEquals(0, expectedPaging.getSkipCount().intValue());
        assertEquals(100, expectedPaging.getMaxItems().intValue());
        assertFalse(expectedPaging.getHasMoreItems().booleanValue());

        // Order by folders and modified date first
        Map<String, String> orderBy = Collections.singletonMap("orderBy", "isFolder DESC,modifiedAt DESC");
        response = getAll(getChildrenUrl(docLibNodeRef), userOneN1.getId(), paging, orderBy, 200);
        nodes = jacksonUtil.parseEntries(response.getJsonResponse(), Node.class);
        assertEquals(4, nodes.size());
        assertEquals(folder2, nodes.get(0).getName());
        assertTrue(nodes.get(0).getIsFolder());
        assertEquals(folder1, nodes.get(1).getName());
        assertTrue(nodes.get(1).getIsFolder());
        assertEquals(content2, nodes.get(2).getName());
        assertFalse(nodes.get(2).getIsFolder());
        assertEquals(content1, nodes.get(3).getName());
        assertFalse(nodes.get(3).getIsFolder());

        // Order by folders last and modified date first
        orderBy = Collections.singletonMap("orderBy", "isFolder ASC,modifiedAt DESC");
        response = getAll(getChildrenUrl(docLibNodeRef), userOneN1.getId(), paging, orderBy, 200);
        nodes = jacksonUtil.parseEntries(response.getJsonResponse(), Node.class);
        assertEquals(4, nodes.size());
        assertEquals(content2, nodes.get(0).getName());
        assertEquals(content1, nodes.get(1).getName());
        assertEquals(folder2, nodes.get(2).getName());
        assertEquals(folder1, nodes.get(3).getName());

        // Order by folders and modified date last
        orderBy = Collections.singletonMap("orderBy", "isFolder,modifiedAt");
        response = getAll(getChildrenUrl(docLibNodeRef), userOneN1.getId(), paging, orderBy, 200);
        nodes = jacksonUtil.parseEntries(response.getJsonResponse(), Node.class);
        assertEquals(4, nodes.size());
        assertEquals(content1, nodes.get(0).getName());
        assertEquals(content2, nodes.get(1).getName());
        assertEquals(folder1, nodes.get(2).getName());
        assertEquals(folder2, nodes.get(3).getName());

        // Order by folders and modified date first
        orderBy = Collections.singletonMap("orderBy", "isFolder DESC,modifiedAt DESC");
        // SkipCount=0,MaxItems=2
        paging = getPaging(0, 2);
        response = getAll(getChildrenUrl(docLibNodeRef), userOneN1.getId(), paging, orderBy, 200);
        nodes = jacksonUtil.parseEntries(response.getJsonResponse(), Node.class);
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
        response = getAll(getChildrenUrl(docLibNodeRef), userOneN1.getId(), paging, orderBy, 200);
        nodes = jacksonUtil.parseEntries(response.getJsonResponse(), Node.class);
        assertEquals(2, nodes.size());
        assertEquals(content2, nodes.get(0).getName());
        assertEquals(content1, nodes.get(1).getName());
        expectedPaging = RestApiUtil.parsePaging(response.getJsonResponse());
        assertEquals(2, expectedPaging.getCount().intValue());
        assertEquals(2, expectedPaging.getSkipCount().intValue());
        assertEquals(4, expectedPaging.getMaxItems().intValue());
        assertFalse(expectedPaging.getHasMoreItems().booleanValue());

        // userTwoN1 tries to access userOneN1's docLib
        AuthenticationUtil.setFullyAuthenticatedUser(userTwoN1.getId());
        paging = getPaging(0, Integer.MAX_VALUE);
        getAll(getChildrenUrl(docLibNodeRef), userTwoN1.getId(), paging, 403);
    }

    /**
     * Tests get user's home children.
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/children}
     */
    @Test
    public void testListMyFilesChildren() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(user1);

        String myNodeId = getMyNodeId(user1);
        NodeRef myFilesNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, myNodeId);

        String myChildrenUrl = getChildrenUrl(Nodes.PATH_MY);
        String rootChildrenUrl = getChildrenUrl(Nodes.PATH_ROOT);

        Map<String, Object> props = new HashMap<>(1);
        props.put("cm:title", "This is folder 1");
        String folder1 = "folder " + System.currentTimeMillis() + " 1";
        String folder1_Id = createFolder(user1, myNodeId, folder1, props).getId();

        String contentF1 = "content" + System.currentTimeMillis() + " in folder 1";
        String contentF1_Id = repoService.createDocument(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, folder1_Id), contentF1, "The quick brown fox jumps over the lazy dog 1.").getId();

        props = new HashMap<>(1);
        props.put("cm:title", "This is folder 2");
        String folder2 = "folder " + System.currentTimeMillis() + " 2";
        String folder2_Id = createFolder(user1, myNodeId, folder2, props).getId();

        String contentF2 = "content" + System.currentTimeMillis() + " in folder 2";
        String contentF2_Id = repoService.createDocument(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, folder2_Id), contentF2, "The quick brown fox jumps over the lazy dog 2.").getId();

        String content1 = "content" + System.currentTimeMillis() + " 1";
        NodeRef contentNodeRef = repoService.createDocument(myFilesNodeRef, content1, "The quick brown fox jumps over the lazy dog.");
        repoService.getNodeService().setProperty(contentNodeRef, ContentModel.PROP_OWNER, user1);
        repoService.getNodeService().setProperty(contentNodeRef, ContentModel.PROP_LAST_THUMBNAIL_MODIFICATION_DATA,
                (Serializable) Collections.singletonList("doclib:1444660852296"));

        List<String> folderIds = Arrays.asList(folder1_Id, folder2_Id);
        List<String> contentIds = Arrays.asList(contentNodeRef.getId());


        Paging paging = getPaging(0, Integer.MAX_VALUE);
        HttpResponse response = getAll(myChildrenUrl, user1, paging, 200);
        List<Document> nodes = jacksonUtil.parseEntries(response.getJsonResponse(), Document.class);
        assertEquals(3, nodes.size());

        // Order by folders and modified date first
        Map<String, String> orderBy = Collections.singletonMap("orderBy", "isFolder DESC,modifiedAt DESC");
        response = getAll(myChildrenUrl, user1, paging, orderBy, 200);
        nodes = jacksonUtil.parseEntries(response.getJsonResponse(), Document.class);
        assertEquals(3, nodes.size());
        assertEquals(folder2, nodes.get(0).getName());
        assertEquals(folder1, nodes.get(1).getName());
        Document node = nodes.get(2);
        assertEquals(content1, node.getName());
        assertEquals("cm:content", node.getNodeType());
        assertEquals(contentNodeRef.getId(), node.getId());
        UserInfo createdByUser = node.getCreatedByUser();
        assertEquals(user1, createdByUser.getId());
        assertEquals(user1 + " " + user1, createdByUser.getDisplayName());
        UserInfo modifiedByUser = node.getModifiedByUser();
        assertEquals(user1, modifiedByUser.getId());
        assertEquals(user1 + " " + user1, modifiedByUser.getDisplayName());
        assertEquals(MimetypeMap.MIMETYPE_BINARY, node.getContent().getMimeType());
        assertNotNull(node.getContent().getMimeTypeName());
        assertNotNull(node.getContent().getEncoding());
        assertTrue(node.getContent().getSizeInBytes() > 0);

        // request without select
        Map<String, String> params = new LinkedHashMap<>();
        response = getAll(myChildrenUrl, user1, paging, params, 200);
        nodes = jacksonUtil.parseEntries(response.getJsonResponse(), Document.class);
        for (Node n : nodes)
        {
            assertNull("There shouldn't be a 'properties' object in the response.", n.getProperties());
            assertNull("There shouldn't be a 'isLink' object in the response.", n.getIsLink());
            assertNull("There shouldn't be a 'path' object in the response.", n.getPath());
            assertNull("There shouldn't be a 'aspectNames' object in the response.", n.getAspectNames());
        }

        // request with select - example 1
        params = new LinkedHashMap<>();
        params.put("select", "isLink");
        response = getAll(myChildrenUrl, user1, paging, params, 200);
        nodes = jacksonUtil.parseEntries(response.getJsonResponse(), Document.class);
        for (Node n : nodes)
        {
            assertNotNull("There should be a 'isLink' object in the response.", n.getIsLink());
        }

        // request with select - example 2
        params = new LinkedHashMap<>();
        params.put("select", "aspectNames,properties, path,isLink");
        response = getAll(myChildrenUrl, user1, paging, params, 200);
        nodes = jacksonUtil.parseEntries(response.getJsonResponse(), Document.class);
        for (Node n : nodes)
        {
            assertNotNull("There should be a 'properties' object in the response.", n.getProperties()); // eg. cm:title, see above
            assertNotNull("There should be a 'isLink' object in the response.", n.getIsLink());
            assertNotNull("There should be a 'path' object in the response.", n.getPath());
            assertNotNull("There should be a 'aspectNames' object in the response.", n.getAspectNames());
        }

        // request specific property via select
        params = new LinkedHashMap<>();
        params.put("select", "cm:lastThumbnailModification");
        params.put("orderBy", "isFolder DESC,modifiedAt DESC");
        response = getAll(myChildrenUrl, user1, paging, params, 200);
        nodes = jacksonUtil.parseEntries(response.getJsonResponse(), Document.class);
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
        params = new LinkedHashMap<>();
        params.put("where", "(isFolder=true)");
        response = getAll(myChildrenUrl, user1, paging, params, 200);
        nodes = jacksonUtil.parseEntries(response.getJsonResponse(), Document.class);
        assertEquals(2, nodes.size());

        assertTrue(nodes.get(0).getIsFolder());
        assertTrue(nodes.get(1).getIsFolder());
        assertTrue(folderIds.contains(nodes.get(0).getId()));
        assertTrue(folderIds.contains(nodes.get(1).getId()));

        // filtering, via where clause - content only
        params = new LinkedHashMap<>();
        params.put("where", "(isFolder=false)");
        response = getAll(myChildrenUrl, user1, paging, params, 200);
        nodes = jacksonUtil.parseEntries(response.getJsonResponse(), Document.class);
        assertEquals(1, nodes.size());
        assertFalse(nodes.get(0).getIsFolder());
        assertTrue(contentIds.contains(nodes.get(0).getId()));

        // list children via relativePath

        params = Collections.singletonMap("relativePath", folder1);
        response = getAll(myChildrenUrl, user1, paging, params, 200);
        nodes = jacksonUtil.parseEntries(response.getJsonResponse(), Document.class);
        assertEquals(1, nodes.size());
        assertEquals(contentF1_Id, nodes.get(0).getId());

        params = Collections.singletonMap("relativePath", "User Homes/" + user1 + "/" + folder2);
        response = getAll(rootChildrenUrl, user1, paging, params, 200);
        nodes = jacksonUtil.parseEntries(response.getJsonResponse(), Document.class);
        assertEquals(1, nodes.size());
        assertEquals(contentF2_Id, nodes.get(0).getId());

        // -ve test - Invalid QName (Namespace prefix cm... is not mapped to a namespace URI) for the orderBy parameter.
        params = Collections.singletonMap("orderBy", "isFolder DESC,cm" + System.currentTimeMillis() + ":modified DESC");
        getAll(myChildrenUrl, user1, paging, params, 400);

        paging = getPaging(0, 10);

        // -ve test - list folder children for non-folder node should return 400
        getAll(getChildrenUrl(contentNodeRef), user1, paging, 400);

        // -ve test - list folder children for unknown node should return 404
        getAll(getChildrenUrl(UUID.randomUUID().toString()), user1, paging, 404);

        // -ve test - user2 tries to access user1's home folder
        AuthenticationUtil.setFullyAuthenticatedUser(user2);
        getAll(getChildrenUrl(myFilesNodeRef), user2, paging, 403);

        // -ve test - try to list children using relative path to unknown node
        params = Collections.singletonMap("relativePath", "User Homes/" + user1 + "/unknown");
        getAll(rootChildrenUrl, user1, paging, params, 404);

        // -ve test - try to list children using relative path to node for which user does not have read permission
        params = Collections.singletonMap("relativePath", "User Homes/" + user2);
        getAll(rootChildrenUrl, user1, paging, params, 403);

        // -ve test - try to list children using relative path to node that is of wrong type (ie. not a folder/container)
        params = Collections.singletonMap("relativePath", folder1 + "/" + contentF1);
        getAll(myChildrenUrl, user1, paging, params, 400);

        // -ve test - list folder children for non-folder node with relative path should return 400
        params = Collections.singletonMap("relativePath", "/unknown");
        getAll(getChildrenUrl(contentNodeRef), user1, paging, params, 400);
    }

    /**
     * Tests get node with path information.
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>?select=path}
     */
    @Test
    public void testGetPathElements_DocLib() throws Exception
    {
        String userId = userOneN1.getId();

        AuthenticationUtil.setFullyAuthenticatedUser(userId);
        userOneN1Site.inviteToSite(userTwoN1.getEmail(), SiteRole.SiteConsumer);

        NodeRef docLibNodeRef = userOneN1Site.getContainerNodeRef(("documentLibrary"));

        // /Company Home/Sites/RandomSite<timestamp>/documentLibrary/folder<timestamp>_A
        String folderA = "folder" + System.currentTimeMillis() + "_A";
        String folderA_Id = createFolder(userId, docLibNodeRef.getId(), folderA).getId();

        // /Company Home/Sites/RandomSite<timestamp>/documentLibrary/folder<timestamp>_A/folder<timestamp>_B
        String folderB = "folder" + System.currentTimeMillis() + "_B";
        String folderB_Id = createFolder(userId, folderA_Id, folderB).getId();
        NodeRef folderB_Ref = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, folderB_Id);

        // /Company Home/Sites/RandomSite<timestamp>/documentLibrary/folder<timestamp>_A/folder<timestamp>_B/folder<timestamp>_C
        String folderC = "folder" + System.currentTimeMillis() + "_C";
        String folderC_Id = createFolder(userId, folderB_Id, folderC).getId();
        NodeRef folderC_Ref = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, folderC_Id);

        // /Company Home/Sites/RandomSite<timestamp>/documentLibrary/folder<timestamp>_A/folder<timestamp>_B/folder<timestamp>_C/content<timestamp>
        String content = "content" + System.currentTimeMillis();
        NodeRef contentNodeRef = repoService.createDocument(folderC_Ref, content, "The quick brown fox jumps over the lazy dog.");

        // Revoke folderB inherited permissions
        permissionService.setInheritParentPermissions(folderB_Ref, false);
        // Grant userTwoN1 permission for folderC
        permissionService.setPermission(folderC_Ref, userTwoN1.getId(), PermissionService.CONSUMER, true);

        //...nodes/nodeId?select=path
        Map<String, String> params = Collections.singletonMap("select", "path");
        HttpResponse response = getSingle(NodesEntityResource.class, userOneN1.getId(), contentNodeRef.getId(), params, 200);
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
        assertEquals("Company Home", pathElements.get(0).getName());
        assertEquals("Sites", pathElements.get(1).getName());
        assertEquals(userOneN1Site.getSiteId(), pathElements.get(2).getName());
        assertEquals("documentLibrary", pathElements.get(3).getName());
        assertEquals(folderA, pathElements.get(4).getName());
        assertEquals(folderB, pathElements.get(5).getName());
        assertEquals(folderC, pathElements.get(6).getName());

        // Try the above tests with userTwoN1 (site consumer)
        AuthenticationUtil.setFullyAuthenticatedUser(userTwoN1.getId());
        response = getSingle(NodesEntityResource.class, userTwoN1.getId(), contentNodeRef.getId(), params, 200);
        node = jacksonUtil.parseEntry(response.getJsonResponse(), Document.class);
        path = node.getPath();
        assertNotNull(path);
        assertFalse("The path is not complete as the user doesn't have permission to access the full path.", path.getIsComplete());
        assertNotNull(path.getName());
        // site consumer (userTwoN1) dose not have access to the folderB
        assertFalse("site consumer (userTwoN1) dose not have access to the folderB", path.getName().contains(folderB));
        assertFalse(path.getName().startsWith("/Company Home"));
        // Go up as far as they can, before getting access denied (i.e. "/folderC")
        assertTrue(path.getName().endsWith(folderC));
        pathElements = path.getElements();
        assertEquals(1, pathElements.size());
        assertEquals(folderC, pathElements.get(0).getName());
    }

    /**
     * Tests get node information.
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>}
     */
    @Test
    public void testGetNodeInfo() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(user1);

        HttpResponse response = getSingle(NodesEntityResource.class, user1, Nodes.PATH_ROOT, null, 200);
        Node node = jacksonUtil.parseEntry(response.getJsonResponse(), Node.class);
        NodeRef companyHomeNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, node.getId());

        response = getSingle(NodesEntityResource.class, user1, Nodes.PATH_MY, null, 200);
        node = jacksonUtil.parseEntry(response.getJsonResponse(), Node.class);
        String myFilesNodeId = node.getId();
        assertNotNull(myFilesNodeId);
        assertEquals(user1, node.getName());
        assertTrue(node.getIsFolder());

        NodeRef myHomeNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, myFilesNodeId);
        NodeRef userHomesNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, node.getParentId());

        // /Company Home/User Homes/user<timestamp>/folder<timestamp>_A
        String folderA = "folder" + System.currentTimeMillis() + "_A";
        String folderA_Id = createFolder(user1, myFilesNodeId, folderA).getId();
        NodeRef folderA_Ref = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, folderA_Id);

        // /Company Home/User Homes/user<timestamp>/folder<timestamp>_A/folder<timestamp>_B
        String folderB = "folder" + System.currentTimeMillis() + "_B";
        String folderB_Id = createFolder(user1, folderA_Id, folderB).getId();
        NodeRef folderB_Ref = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, folderB_Id);

        // /Company Home/User Homes/user<timestamp>/folder<timestamp>_A/folder<timestamp>_B/content<timestamp>
        String contentName = "content " + System.currentTimeMillis();
        NodeRef contentNodeRef = repoService.createDocument(folderB_Ref, contentName, "The quick brown fox jumps over the lazy dog.");

        // Add property
        String title = "test title";
        repoService.getNodeService().setProperty(contentNodeRef, ContentModel.PROP_TITLE, title);

        // get node info
        response = getSingle(NodesEntityResource.class, user1, contentNodeRef.getId(), null, 200);
        Document documentResp = jacksonUtil.parseEntry(response.getJsonResponse(), Document.class);
        String content_Id = documentResp.getId();

        // Expected result ...
        UserInfo expectedUser = new UserInfo(user1, user1+" "+user1);

        Document d1 = new Document();
        d1.setId(content_Id);
        d1.setParentId(folderB_Id);
        d1.setName(contentName);
        d1.setNodeType("cm:content");
        ContentInfo ci = new ContentInfo();

        // TODO fix me !! (is this an issue with repoService.createDocument ?)
        //ci.setMimeType("text/plain");
        //ci.setMimeTypeName("Plain Text");
        ci.setMimeType("application/octet-stream");
        ci.setMimeTypeName("Binary File (Octet Stream)");

        ci.setSizeInBytes(44L);
        ci.setEncoding("UTF-8");
        d1.setContent(ci);
        d1.setCreatedByUser(expectedUser);
        d1.setModifiedByUser(expectedUser);

        Map<String,Object> props = new HashMap<>();
        props.put("cm:title",title);

        d1.setProperties(props);
        d1.setAspectNames(Arrays.asList("cm:auditable","cm:titled"));

        // Note: Path is not part of the default info
        d1.expected(documentResp);

        // get node info + path
        //...nodes/nodeId?select=path
        Map<String, String> params = Collections.singletonMap("select", "path");
        response = getSingle(NodesEntityResource.class, user1, contentNodeRef.getId(), params, 200);
        documentResp = jacksonUtil.parseEntry(response.getJsonResponse(), Document.class);

        // Expected path ...
        // note: the pathInfo should only include the parents (not the requested node)
        List<ElementInfo> elements = new ArrayList<>(5);
        elements.add(new ElementInfo(companyHomeNodeRef, "Company Home"));
        elements.add(new ElementInfo(userHomesNodeRef, "User Homes"));
        elements.add(new ElementInfo(myHomeNodeRef, user1));
        elements.add(new ElementInfo(folderA_Ref, folderA));
        elements.add(new ElementInfo(folderB_Ref, folderB));
        PathInfo expectedPath = new PathInfo("/Company Home/User Homes/"+user1+"/"+folderA+"/"+folderB, true, elements);
        d1.setPath(expectedPath);

        d1.expected(documentResp);

        // get node info via relativePath

        params = Collections.singletonMap("relativePath", "/"+folderA+"/"+folderB);
        response = getSingle(NodesEntityResource.class, user1, Nodes.PATH_MY, params, 200);
        Folder folderResp = jacksonUtil.parseEntry(response.getJsonResponse(), Folder.class);
        assertEquals(folderB_Id, folderResp.getId());

        params = Collections.singletonMap("relativePath", folderA+"/"+folderB+"/"+contentName);
        response = getSingle(NodesEntityResource.class, user1, Nodes.PATH_MY, params, 200);
        documentResp = jacksonUtil.parseEntry(response.getJsonResponse(), Document.class);
        assertEquals(content_Id, documentResp.getId());

        // test path with utf-8 encoded param (eg. ¢ => )
        String folderC = "folder" + System.currentTimeMillis() + " ¢";
        String folderC_Id = createFolder(user1, folderB_Id, folderC).getId();

        params = Collections.singletonMap("relativePath", "/"+folderA+"/"+folderB+"/"+folderC);
        response = getSingle(NodesEntityResource.class, user1, Nodes.PATH_MY, params, 200);
        folderResp = jacksonUtil.parseEntry(response.getJsonResponse(), Folder.class);
        assertEquals(folderC_Id, folderResp.getId());

        // -ve test - get info for unknown node should return 404
        getSingle(NodesEntityResource.class, user1, UUID.randomUUID().toString(), null, 404);

        // -ve test - user2 tries to get node info about user1's home folder
        AuthenticationUtil.setFullyAuthenticatedUser(user2);
        getSingle(NodesEntityResource.class, user2, myFilesNodeId, null, 403);

        // -ve test - try to get node info using relative path to unknown node
        params = Collections.singletonMap("relativePath", folderA+"/unknown");
        getSingle(NodesEntityResource.class, user1, Nodes.PATH_MY, params, 404);

        // -ve test - try to get node info using relative path to node for which user does not have read permission
        params = Collections.singletonMap("relativePath", "User Homes/"+user2);
        getSingle(NodesEntityResource.class, user1, Nodes.PATH_ROOT, params, 403);

        // -ve test - attempt to get node info for non-folder node with relative path should return 400
        params = Collections.singletonMap("relativePath", "/unknown");
        getSingle(NodesEntityResource.class, user1, content_Id, params, 400);
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
        HttpResponse response = getSingle(NodesEntityResource.class, user1, Nodes.PATH_ROOT, null, 200);
        Node node = jacksonUtil.parseEntry(response.getJsonResponse(), Node.class);
        assertEquals("Company Home", node.getName());
        assertNotNull(node.getId());
        assertNull(node.getPath());

        // unknown alias
        getSingle(NodesEntityResource.class, user1, "testSomeUndefinedAlias", null, 404);

        response = getSingle(NodesEntityResource.class, user1, Nodes.PATH_MY, null, 200);
        node = jacksonUtil.parseEntry(response.getJsonResponse(), Node.class);
        String myFilesNodeId = node.getId();
        assertNotNull(myFilesNodeId);
        assertEquals(user1, node.getName());
        assertTrue(node.getIsFolder());
        assertNull(node.getPath()); // note: path can be optionally "select"'ed - see separate test

        response = getSingle(NodesEntityResource.class, user1, Nodes.PATH_SHARED, null, 200);
        node = jacksonUtil.parseEntry(response.getJsonResponse(), Node.class);
        String sharedFilesNodeId = node.getId();
        assertNotNull(sharedFilesNodeId);
        assertEquals("Shared", node.getName());
        assertTrue(node.getIsFolder());
        assertNull(node.getPath());

        //Delete user1's home
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        repoService.getNodeService().deleteNode(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, myFilesNodeId));

        AuthenticationUtil.setFullyAuthenticatedUser(user1);
        getSingle(NodesEntityResource.class, user1, Nodes.PATH_MY, null, 404); // Not found
    }

    /**
     * Tests Multipart upload to user's home (a.k.a My Files).
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/children}
     */
    @Test
    public void testUploadToMyFiles() throws Exception
    {
        final String fileName = "quick.pdf";
        final File file = getResourceFile(fileName);

        Paging paging = getPaging(0, Integer.MAX_VALUE);
        HttpResponse response = getAll(getChildrenUrl(Nodes.PATH_MY), user1, paging, 200);
        PublicApiClient.ExpectedPaging pagingResult = parsePaging(response.getJsonResponse());
        assertNotNull(paging);
        final int numOfNodes = pagingResult.getCount();

        MultiPartBuilder multiPartBuilder = MultiPartBuilder.create()
                    .setFileData(new FileData(fileName, file, MimetypeMap.MIMETYPE_PDF));
        MultiPartRequest reqBody = multiPartBuilder.build();

        // Try to upload into a non-existent folder
        post(getChildrenUrl(UUID.randomUUID().toString()), user1, reqBody.getBody(), null, reqBody.getContentType(), 404);

        // Upload
        response = post(getChildrenUrl(Nodes.PATH_MY), user1, reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document document = jacksonUtil.parseEntry(response.getJsonResponse(), Document.class);
        // Check the upload response
        assertEquals(fileName, document.getName());
        ContentInfo contentInfo = document.getContent();
        assertNotNull(contentInfo);
        assertEquals(MimetypeMap.MIMETYPE_PDF, contentInfo.getMimeType());
        // Default encoding
        assertEquals("UTF-8", contentInfo.getEncoding());
        // Check there is no path info returned.
        // The path info should only be returned when it is requested via a select statement.
        assertNull(document.getPath());

        // Retrieve the uploaded file
        response = getSingle(NodesEntityResource.class, user1, document.getId(), null, 200);
        document = jacksonUtil.parseEntry(response.getJsonResponse(), Document.class);
        assertEquals(fileName, document.getName());
        contentInfo = document.getContent();
        assertNotNull(contentInfo);
        assertEquals(MimetypeMap.MIMETYPE_PDF, contentInfo.getMimeType());

        // Check 'get children' is confirming the upload
        response = getAll(getChildrenUrl(Nodes.PATH_MY), user1, paging, 200);
        pagingResult = parsePaging(response.getJsonResponse());
        assertNotNull(paging);
        assertEquals(numOfNodes + 1, pagingResult.getCount().intValue());

        // Upload the same file again to check the name conflicts handling
        post(getChildrenUrl(Nodes.PATH_MY), user1, reqBody.getBody(), null, reqBody.getContentType(), 409);

        response = getAll(getChildrenUrl(Nodes.PATH_MY), user1, paging, 200);
        pagingResult = parsePaging(response.getJsonResponse());
        assertNotNull(paging);
        assertEquals("Duplicate file name. The file shouldn't have been uploaded.", numOfNodes + 1, pagingResult.getCount().intValue());

        // Set autoRename=true and upload the same file again
        reqBody = MultiPartBuilder.copy(multiPartBuilder)
                          .setAutoRename(true)
                          .build();

        response = post(getChildrenUrl(Nodes.PATH_MY), user1, reqBody.getBody(), null, reqBody.getContentType(), 201);
        document = jacksonUtil.parseEntry(response.getJsonResponse(), Document.class);
        // Check the upload response
        assertEquals("quick-1.pdf", document.getName());

        // upload the same file again, and request the path info to be present in the response
        response = post(getChildrenUrl(Nodes.PATH_MY), user1, reqBody.getBody(), "?select=path", reqBody.getContentType(), 201);
        document = jacksonUtil.parseEntry(response.getJsonResponse(), Document.class);
        // Check the upload response
        assertEquals("quick-2.pdf", document.getName());
        assertNotNull(document.getPath());

        response = getAll(getChildrenUrl(Nodes.PATH_MY), user1, paging, 200);
        pagingResult = parsePaging(response.getJsonResponse());
        assertNotNull(paging);
        assertEquals(numOfNodes + 3, pagingResult.getCount().intValue());

        // User2 tries to upload a new file into the user1's home folder.
        response = getSingle(NodesEntityResource.class, user1, Nodes.PATH_MY, null, 200);
        Folder user1Home = jacksonUtil.parseEntry(response.getJsonResponse(), Folder.class);
        final String fileName2 = "quick-2.txt";
        final File file2 = getResourceFile(fileName2);
        reqBody = MultiPartBuilder.create()
                    .setFileData(new FileData(fileName2, file2, MimetypeMap.MIMETYPE_TEXT_PLAIN))
                    .build();
        post(getChildrenUrl(user1Home.getId()), user2, reqBody.getBody(), null, reqBody.getContentType(), 403);

        response = getAll(getChildrenUrl(Nodes.PATH_MY), user1, paging, 200);
        pagingResult = parsePaging(response.getJsonResponse());
        assertNotNull(paging);
        assertEquals("Access Denied. The file shouldn't have been uploaded.", numOfNodes + 3, pagingResult.getCount().intValue());

        // User1 tries to upload a file into a document rather than a folder!
        post(getChildrenUrl(document.getId()), user1, reqBody.getBody(), null, reqBody.getContentType(), 400);

        // Try to upload a file without defining the required formData
        reqBody = MultiPartBuilder.create().setAutoRename(true).build();
        post(getChildrenUrl(Nodes.PATH_MY), user1, reqBody.getBody(), null, reqBody.getContentType(), 400);

        // Test unsupported node type
        reqBody = MultiPartBuilder.create()
                    .setFileData(new FileData(fileName2, file2, null))
                    .setAutoRename(true)
                    .setNodeType("cm:link")
                    .build();
        post(getChildrenUrl(user1Home.getId()), user1, reqBody.getBody(), null, reqBody.getContentType(), 400);

        // User1 uploads a new file
        reqBody = MultiPartBuilder.create()
                    .setFileData(new FileData(fileName2, file2, MimetypeMap.MIMETYPE_TEXT_PLAIN, "windows-1252"))
                    .build();
        response = post(getChildrenUrl(user1Home.getId()), user1, reqBody.getBody(), null, reqBody.getContentType(), 201);
        document = jacksonUtil.parseEntry(response.getJsonResponse(), Document.class);
        // Check the upload response
        assertEquals(fileName2, document.getName());
        contentInfo = document.getContent();
        assertNotNull(contentInfo);
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, contentInfo.getMimeType());
        assertEquals("windows-1252", contentInfo.getEncoding());

        // Test invalid mimeType
        reqBody = MultiPartBuilder.create()
                    .setFileData(new FileData(fileName2, file2, "*/invalidSubType", "ISO-8859-1"))
                    .setAutoRename(true)
                    .build();
        post(getChildrenUrl(user1Home.getId()), user1, reqBody.getBody(), null, reqBody.getContentType(), 400);

    }

    /**
     * Tests Multipart upload to a Site.
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/children}
     */
    @Test
    public void testUploadToSite() throws Exception
    {
        final String fileName = "quick-1.txt";
        final File file = getResourceFile(fileName);

        AuthenticationUtil.setFullyAuthenticatedUser(userOneN1.getId());
        String folderA = "folder" + System.currentTimeMillis() + "_A";
        NodeRef folderA_Ref = repoService.addToDocumentLibrary(userOneN1Site, folderA, ContentModel.TYPE_FOLDER);

        Paging paging = getPaging(0, Integer.MAX_VALUE);
        HttpResponse response = getAll(getChildrenUrl(folderA_Ref), userOneN1.getId(), paging, 200);
        PublicApiClient.ExpectedPaging pagingResult = parsePaging(response.getJsonResponse());
        assertNotNull(paging);
        final int numOfNodes = pagingResult.getCount();

        MultiPartBuilder multiPartBuilder = MultiPartBuilder.create()
                    .setFileData(new FileData(fileName, file, null));
        MultiPartRequest reqBody = multiPartBuilder.build();
        // Try to upload
        response = post(getChildrenUrl(folderA_Ref), userOneN1.getId(), reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document document = jacksonUtil.parseEntry(response.getJsonResponse(), Document.class);
        // Check the upload response
        assertEquals(fileName, document.getName());
        ContentInfo contentInfo = document.getContent();
        assertNotNull(contentInfo);
        // As the client didn't set the mimeType, the API must guess it.
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, contentInfo.getMimeType());

        // Retrieve the uploaded file
        response = getSingle(NodesEntityResource.class, userOneN1.getId(), document.getId(), null, 200);
        document = jacksonUtil.parseEntry(response.getJsonResponse(), Document.class);
        assertEquals(fileName, document.getName());
        contentInfo = document.getContent();
        assertNotNull(contentInfo);
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, contentInfo.getMimeType());

        // Check 'get children' is confirming the upload
        response = getAll(getChildrenUrl(folderA_Ref), userOneN1.getId(), paging, 200);
        pagingResult = parsePaging(response.getJsonResponse());
        assertNotNull(paging);
        assertEquals(numOfNodes + 1, pagingResult.getCount().intValue());

        // Upload the same file again to check the name conflicts handling
        post(getChildrenUrl(folderA_Ref), userOneN1.getId(), reqBody.getBody(), null, reqBody.getContentType(), 409);

        response = getAll(getChildrenUrl(folderA_Ref), userOneN1.getId(), paging, 200);
        pagingResult = parsePaging(response.getJsonResponse());
        assertNotNull(paging);
        assertEquals(numOfNodes + 1, pagingResult.getCount().intValue());

        final String fileName2 = "quick-2.txt";
        final File file2 = getResourceFile(fileName2);
        reqBody = MultiPartBuilder.create()
                    .setFileData(new FileData(fileName2, file2, MimetypeMap.MIMETYPE_TEXT_PLAIN))
                    .build();
        // userTwoN1 tries to upload a new file into the folderA of userOneN1
        post(getChildrenUrl(folderA_Ref), userTwoN1.getId(), reqBody.getBody(), null, reqBody.getContentType(), 403);

        // Test upload with properties
        response = post(getChildrenUrl(folderA_Ref), userOneN1.getId(), reqBody.getBody(), null, reqBody.getContentType(), 201);
        document = jacksonUtil.parseEntry(response.getJsonResponse(), Document.class);
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
                    .setFileData(new FileData(fileName2, file2, MimetypeMap.MIMETYPE_TEXT_PLAIN))
                    .setAutoRename(true)
                    .setProperties(props)
                    .build();

        response = post(getChildrenUrl(folderA_Ref), userOneN1.getId(), reqBody.getBody(), null, reqBody.getContentType(), 201);
        document = jacksonUtil.parseEntry(response.getJsonResponse(), Document.class);
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
                    .setFileData(new FileData(fileName2, file2, MimetypeMap.MIMETYPE_TEXT_PLAIN))
                    .setAutoRename(true)
                    .setProperties(props)
                    .build();
        // Prop prefix is unknown
        post(getChildrenUrl(folderA_Ref), userOneN1.getId(), reqBody.getBody(), null, reqBody.getContentType(), 400);
    }

    /**
     * Tests delete (file or folder)
     * <p>DELETE:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>}
     */
    @Test
    public void testDelete() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(user1);

        String myNodeId = getMyNodeId(user1);
        NodeRef myFilesNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, myNodeId);

        String content1 = "content" + System.currentTimeMillis() + "_1";
        NodeRef content1Ref = repoService.createDocument(myFilesNodeRef, content1, "The quick brown fox jumps over the lazy dog.");

        // delete file
        delete("nodes", user1, content1Ref.getId(), 204);

        // -ve test
        delete("nodes", user1, content1Ref.getId(), 404);

        String folder1 = "folder" + System.currentTimeMillis() + "_1";
        String folder1Ref = createFolder(user1, myNodeId, folder1).getId();

        String folder2 = "folder" + System.currentTimeMillis() + "_2";
        String folder2Ref = createFolder(user1, folder1Ref, folder2).getId();

        String content2 = "content" + System.currentTimeMillis() + "_2";
        NodeRef content2Ref = repoService.createDocument(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, folder2Ref), content2, "The quick brown fox jumps over the lazy dog.");

        // cascade delete folder
        delete("nodes", user1, folder1Ref, 204);

        // -ve test
        delete("nodes", user1, folder2Ref, 404);
        delete("nodes", user1, content2Ref.getId(), 404);

        // -ve test
        String rootNodeId = getRootNodeId(user1);
        delete("nodes", user1, rootNodeId, 403);
    }

    /**
     * Tests move (file or folder)
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/move}
     */
    @Test
    public void testMove() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(user1);

        // create folder f1
        Folder folderResp = createFolder(user1, Nodes.PATH_MY, "f1");
        String f1Id = folderResp.getId();

        // create folder f2
        folderResp = createFolder(user1, Nodes.PATH_MY, "f2");
        String f2Id = folderResp.getId();

        // create doc d1
        NodeRef f1Ref = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, f1Id);
        String d1Name = "content" + System.currentTimeMillis() + "_1";
        NodeRef d1Ref = repoService.createDocument(f1Ref, d1Name, "The quick brown fox jumps over the lazy dog.");
        String d1Id = d1Ref.getId();

        // create doc d2
        NodeRef f2Ref = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, f2Id);
        String d2Name = "content" + System.currentTimeMillis() + "_2";
        NodeRef d2Ref = repoService.createDocument(f2Ref, d2Name, "The quick brown fox jumps over the lazy dog 2.");
        String d2Id = d2Ref.getId();

        // move file (without rename)

        NodeTarget moveTgt = new NodeTarget();
        moveTgt.setTargetParentId(f2Id);

        HttpResponse response = post("nodes/"+d1Id+"/move", user1, toJsonAsStringNonNull(moveTgt), null, 201);
        Document documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        assertEquals(d1Name, documentResp.getName());
        assertEquals(f2Id, documentResp.getParentId());

        // move file (with rename)

        String d1NewName = d1Name+" updated !!";

        moveTgt = new NodeTarget();
        moveTgt.setName(d1NewName);
        moveTgt.setTargetParentId(f1Id);

        response = post("nodes/"+d1Id+"/move", user1, toJsonAsStringNonNull(moveTgt), null, 201);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        assertEquals(d1NewName, documentResp.getName());
        assertEquals(f1Id, documentResp.getParentId());

        // -ve tests

        // name already exists
        moveTgt = new NodeTarget();
        moveTgt.setName(d2Name);
        moveTgt.setTargetParentId(f2Id);
        post("nodes/"+d1Id+"/move", user1, toJsonAsStringNonNull(moveTgt), null, 409);

        // unknown source nodeId
        moveTgt = new NodeTarget();
        moveTgt.setTargetParentId(f2Id);
        post("nodes/"+UUID.randomUUID().toString()+"/move", user1, toJsonAsStringNonNull(moveTgt), null, 404);

        // unknown target nodeId
        moveTgt = new NodeTarget();
        moveTgt.setTargetParentId(UUID.randomUUID().toString());
        post("nodes/"+d1Id+"/move", user1, toJsonAsStringNonNull(moveTgt), null, 404);

        // target is not a folder
        moveTgt = new NodeTarget();
        moveTgt.setTargetParentId(d2Id);
        post("nodes/"+d1Id+"/move", user1, toJsonAsStringNonNull(moveTgt), null, 400);

        String rootNodeId = getRootNodeId(user1);

        // create folder f3 (sub-folder of f2)
        folderResp = createFolder(user1, f2Id, "f3");
        String f3Id = folderResp.getId();

        // can't create cycle (move into own subtree)
        moveTgt = new NodeTarget();
        moveTgt.setTargetParentId(f3Id);
        post("nodes/"+f2Id+"/move", user1, toJsonAsStringNonNull(moveTgt), null, 400);

        // no (write/create) permissions to move to target
        moveTgt = new NodeTarget();
        moveTgt.setTargetParentId(rootNodeId);
        post("nodes/"+d1Id+"/move", user1, toJsonAsStringNonNull(moveTgt), null, 403);

        AuthenticationUtil.setFullyAuthenticatedUser(user2);

        String my2NodeId = getMyNodeId(user2);

        // no (write/delete) permissions to move source
        moveTgt = new NodeTarget();
        moveTgt.setTargetParentId(my2NodeId);
        post("nodes/"+f1Id+"/move", user2, toJsonAsStringNonNull(moveTgt), null, 403);
    }


    /**
     * Tests copy (file or folder)
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/copy}
     */
    @Test
    public void testCopy() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(user1);

        // create folder
        Folder folderResp = createFolder(user1, Nodes.PATH_MY, "fsource");
        String source = folderResp.getId();
        NodeRef sourceRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, source);

        // create folder
        folderResp = createFolder(user1, Nodes.PATH_MY, "ftarget");
        String target = folderResp.getId();
        NodeRef targetRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, target);

        // create doc d1
        String d1Name = "content" + System.currentTimeMillis() + "_1";
        NodeRef d1Ref = repoService.createDocument(sourceRef, d1Name, "The quick brown fox jumps over the lazy dog.");
        String d1Id = d1Ref.getId();

        // create doc d2
        String d2Name = "content" + System.currentTimeMillis() + "_2";
        NodeRef d2Ref = repoService.createDocument(sourceRef, d2Name, "The quick brown fox jumps over the lazy dog 2.");
        String d2Id = d2Ref.getId();

        Map<String, String> body = new HashMap<>();
        body.put("targetParentId", target);

        HttpResponse response = post(user1, "nodes", d1Id, "copy", toJsonAsStringNonNull(body).getBytes(), null, null, 201);
        Document documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        assertEquals(d1Name, documentResp.getName());
        assertEquals(target, documentResp.getParentId());

        // copy file (with rename)
        String newD2Name = d2Name + " updated !!";

        body = new HashMap<>();
        body.put("targetParentId", target);
        body.put("name", newD2Name);

        response = post(user1, "nodes", d2Id, "copy", toJsonAsStringNonNull(body).getBytes(), null, null, 201);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        assertEquals(newD2Name, documentResp.getName());
        assertEquals(target, documentResp.getParentId());
    }
    /**
     * Tests create folder.
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/children}
     */
    @Test
    public void testCreateFolder() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(user1);

        String myNodeId = getMyNodeId(user1);

        UserInfo expectedUser = new UserInfo(user1, user1+" "+user1);

        String postUrl = getChildrenUrl(myNodeId);

        // create folder
        Folder folderResp = createFolder(user1, myNodeId, "f1");

        Folder f1 = new Folder();
        f1.setName("f1");
        f1.setNodeType("cm:folder");

        f1.setIsFolder(true);
        f1.setParentId(myNodeId);
        f1.setAspectNames(Collections.singletonList("cm:auditable"));

        f1.setCreatedByUser(expectedUser);
        f1.setModifiedByUser(expectedUser);

        f1.expected(folderResp);

        // create folder with properties
        Map<String,Object> props = new HashMap<>();
        props.put("cm:title","my folder title");
        props.put("cm:description","my folder description");

        folderResp = createFolder(user1, myNodeId, "f2", props);

        Folder f2 = new Folder();
        f2.setName("f2");
        f2.setNodeType("cm:folder");
        f2.setProperties(props);

        f2.setIsFolder(true);
        f2.setParentId(myNodeId);
        f2.setAspectNames(Arrays.asList("cm:auditable","cm:titled"));

        f2.setCreatedByUser(expectedUser);
        f2.setModifiedByUser(expectedUser);

        f2.expected(folderResp);

        // -ve test - name is mandatory
        Folder invalid = new Folder();
        invalid.setNodeType("cm:folder");
        post(postUrl, user1, toJsonAsStringNonNull(invalid), 400);

        // -ve test - node type is mandatory
        invalid = new Folder();
        invalid.setName("my folder");
        post(postUrl, user1, toJsonAsStringNonNull(invalid), 400);

        // create empty file
        Document d1 = new Document();
        d1.setName("d1.txt");
        d1.setNodeType("cm:content");
        ContentInfo ci = new ContentInfo();
        ci.setMimeType("text/plain");
        d1.setContent(ci);

        HttpResponse response = post(postUrl, user1, toJsonAsStringNonNull(d1), 201);
        Document documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        String d1Id = documentResp.getId();

        // -ve test - invalid (eg. not a folder) parent id
        Folder f3 = new Folder();
        f3.setName("f3");
        f3.setNodeType("cm:folder");
        post(getChildrenUrl(d1Id), user1, toJsonAsStringNonNull(f3), 400);

        // -ve test - it should not be possible to create a "system folder"
        invalid = new Folder();
        invalid.setName("my sys folder");
        invalid.setNodeType("cm:systemfolder");
        post(postUrl, user1, toJsonAsStringNonNull(invalid), 400);

        // -ve test - unknown parent folder node id
        post(getChildrenUrl(UUID.randomUUID().toString()), user1, toJsonAsStringNonNull(f3), 404);

        // -ve test - duplicate name
        post(postUrl, user1, toJsonAsStringNonNull(f1), 409);

        // Create a folder with a duplicate name (f1), but set the autoRename to true
        response = post(postUrl, user1, toJsonAsStringNonNull(f1), "?autoRename=true", 201);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertEquals("f1-1", documentResp.getName());

        // Create a folder with a duplicate name (f1) again, but set the autoRename to true
        response = post(postUrl, user1, toJsonAsStringNonNull(f1), "?autoRename=true", 201);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertEquals("f1-2", documentResp.getName());

        // -ve test - create a folder with a duplicate name (f1), but set the autoRename to false
        post(postUrl, user1, toJsonAsStringNonNull(f1), "?autoRename=false", 409);
    }

    // TODO test custom type with properties (sub-type of cm:cmobject)

    // note: app:folderlink & app:filelink both extend cm:link (which in turn extends cm:cmobject)
    //       (see applicationModel.xml / contentModel.xml)
    @Test
    public void testLinkCRUD() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(user1);

        String myNodeId = getMyNodeId(user1);
        UserInfo expectedUser = new UserInfo(user1, user1+" "+user1);
        String myChildrenUrl = getChildrenUrl(myNodeId);

        long timeNow = System.currentTimeMillis();

        // create folder f1
        Folder folderResp = createFolder(user1, myNodeId, "f1 "+timeNow);
        String f1Id = folderResp.getId();

        // create empty file d1 in f1
        Document d1 = new Document();
        d1.setName("d1.txt");
        d1.setNodeType("cm:content");
        ContentInfo ci = new ContentInfo();
        ci.setMimeType("text/plain");
        d1.setContent(ci);

        HttpResponse response = post(getChildrenUrl(f1Id), user1, toJsonAsStringNonNull(d1), 201);
        Document documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        String d1Id = documentResp.getId();

        // create folder f2
        folderResp = createFolder(user1, myNodeId, "f2 "+timeNow);
        String f2Id = folderResp.getId();

        // create folder link node in f2 (pointing to f1)
        String nodeName = "f1 link";
        String nodeType = "app:folderlink";

        Map<String,Object> props = new HashMap<>();
        props.put("cm:destination", f1Id);

        Node nodeResp = createNode(user1, f2Id, nodeName, nodeType, props);
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
        response = getSingle(NodesEntityResource.class, user1, n1Id, null, 200);
        nodeResp = jacksonUtil.parseEntry(response.getJsonResponse(), Node.class);

        n1.expected(nodeResp);

        // create file link node in f2 pointing to d1
        nodeName = "d1 link";
        nodeType = "app:filelink";

        props = new HashMap<>();
        props.put("cm:destination", d1Id);

        nodeResp = createNode(user1, f2Id, nodeName, nodeType, props);
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
        response = getSingle(NodesEntityResource.class, user1, n2Id, null, 200);
        nodeResp = jacksonUtil.parseEntry(response.getJsonResponse(), Node.class);

        n2.expected(nodeResp);


        // update node - rename

        String updatedName = "f1 link renamed";

        Node nUpdate = new Node();
        nUpdate.setName(updatedName);

        response = put("nodes", user1, n1Id, toJsonAsStringNonNull(nUpdate), null, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        n1.setName(updatedName);
        n1.expected(nodeResp);


        // filtering, via where clause (nodeType + subTypes)

        List<String> linkIds = Arrays.asList(n1Id, n2Id);

        Map<String, String> params = new HashMap<>();
        params.put("where", "(nodeType='cm:link')");

        Paging paging = getPaging(0, Integer.MAX_VALUE);

        response = getAll(getChildrenUrl(f2Id), user1, paging, params, 200);
        List<Node> nodes = jacksonUtil.parseEntries(response.getJsonResponse(), Node.class);

        // TODO review
        //assertEquals(0, nodes.size());
        assertEquals(2, nodes.size());

        params = new HashMap<>();
        params.put("where", "(nodeType='cm:link' and subTypes=true)");

        paging = getPaging(0, Integer.MAX_VALUE);

        response = getAll(getChildrenUrl(f2Id), user1, paging, params, 200);
        nodes = jacksonUtil.parseEntries(response.getJsonResponse(), Node.class);
        assertEquals(2, nodes.size());
        assertTrue(linkIds.contains(nodes.get(0).getId()));
        assertTrue(linkIds.contains(nodes.get(1).getId()));


        // delete link
        delete("nodes", user1, n1Id, 204);

        // -ve test - delete - cannot delete nonexistent link
        delete("nodes", user1, n1Id, 404);

        // -ve test - create - name is mandatory
        Node invalid = new Node();
        invalid.setNodeType("cm:link");
        post(myChildrenUrl, user1, toJsonAsStringNonNull(invalid), 400);

        // -ve test - create - node type is mandatory
        invalid = new Node();
        invalid.setName("my node");
        post(myChildrenUrl, user1, toJsonAsStringNonNull(invalid), 400);

        // -ve test - create - unsupported node type
        invalid = new Node();
        invalid.setName("my node");
        invalid.setNodeType("sys:base");
        post(myChildrenUrl, user1, toJsonAsStringNonNull(invalid), 400);

        // -ve test - create - duplicate name
        post(getChildrenUrl(f2Id), user1, toJsonAsStringNonNull(n2), 409);

        // -ve test - unknown nodeType when filtering
        params = new HashMap<>();
        params.put("where", "(nodeType='my:unknown'");
        getAll(getChildrenUrl(f2Id), user1, paging, params, 400);
    }


    /**
     * Tests create empty file.
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/children}
     */
    @Test
    public void testCreateEmptyFile() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(user1);

        String myNodeId = getMyNodeId(user1);

        UserInfo expectedUser = new UserInfo(user1, user1+" "+user1);

        String postUrl = getChildrenUrl(myNodeId);

        Document d1 = new Document();
        d1.setName("d1.txt");
        d1.setNodeType("cm:content");
        ContentInfo ci = new ContentInfo();
        ci.setMimeType("text/plain");
        d1.setContent(ci);

        // create empty file
        HttpResponse response = post(postUrl, user1, toJsonAsStringNonNull(d1), 201);
        Document documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        String d1Id = documentResp.getId();

        d1.setIsFolder(false);
        d1.setParentId(myNodeId);
        d1.setAspectNames(Collections.singletonList("cm:auditable"));

        d1.setCreatedByUser(expectedUser);
        d1.setModifiedByUser(expectedUser);

        d1.getContent().setMimeTypeName("Plain Text");
        d1.getContent().setSizeInBytes(0L);
        d1.getContent().setEncoding("UTF-8");

        d1.expected(documentResp);

        // create empty file with properties
        Map<String,Object> props = new HashMap<>();
        props.put("cm:title","my file title");
        props.put("cm:description","my file description");

        Document d2 = new Document();
        d2.setName("d2.txt");
        d2.setNodeType("cm:content");
        d2.setProperties(props);

        response = post(postUrl, user1, toJsonAsStringNonNull(d2), 201);

        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        d2.setIsFolder(false);
        d2.setParentId(myNodeId);
        d2.setAspectNames(Arrays.asList("cm:auditable","cm:titled"));

        d2.setCreatedByUser(expectedUser);
        d2.setModifiedByUser(expectedUser);

        ContentInfo ciExpected = new ContentInfo();
        ciExpected.setMimeType("text/plain");
        ciExpected.setMimeTypeName("Plain Text");
        ciExpected.setSizeInBytes(0L);
        ciExpected.setEncoding("UTF-8");
        d2.setContent(ciExpected);

        d2.expected(documentResp);

        // -ve test - name is mandatory
        Document invalid = new Document();
        invalid.setNodeType("cm:content");
        post(postUrl, user1, toJsonAsStringNonNull(invalid), 400);

        // -ve test - node type is mandatory
        invalid = new Document();
        invalid.setName("my file.txt");
        post(postUrl, user1, toJsonAsStringNonNull(invalid), 400);

        // -ve test - invalid (eg. not a folder) parent id
        Document d3 = new Document();
        d3.setName("d3.txt");
        d3.setNodeType("cm:content");
        post(getChildrenUrl(d1Id), user1, toJsonAsStringNonNull(d3), 400);

        // -ve test - unknown parent folder node id
        post(getChildrenUrl(UUID.randomUUID().toString()), user1, toJsonAsStringNonNull(d3), 404);

        // -ve test - duplicate name
        post(postUrl, user1, toJsonAsStringNonNull(d1), 409);

        // Create a file with a duplicate name (d1.txt), but set the autoRename to true
        response = post(postUrl, user1, toJsonAsStringNonNull(d1), "?autoRename=true", 201);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertEquals("d1-1.txt", documentResp.getName());

        // Create a file with a duplicate name (d1.txt) again, but set the autoRename to true
        response = post(postUrl, user1, toJsonAsStringNonNull(d1), "?autoRename=true", 201);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertEquals("d1-2.txt", documentResp.getName());

        // Create a file with a duplicate name (d1-2.txt) again, but set the autoRename to true
        d1.setName("d1-2.txt");
        response = post(postUrl, user1, toJsonAsStringNonNull(d1), "?autoRename=true", 201);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertEquals("d1-2-1.txt", documentResp.getName());

        // -ve test - create a file with a duplicate name (d1-2.txt), but set the autoRename to false
        post(postUrl, user1, toJsonAsStringNonNull(d1), "?autoRename=false", 409);
    }

    /**
     * Tests update node info (file or folder)
     * <p>PUT:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>}
     */
    @Test
    public void testUpdateNodeInfo() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(user1);

        String myNodeId = getMyNodeId(user1);

        UserInfo expectedUser = new UserInfo(user1, user1+" "+user1);

        String postUrl = getChildrenUrl(myNodeId);

        String folderName = "My Folder";

        // create folder
        Folder folderResp = createFolder(user1, myNodeId, folderName);

        String fId = folderResp.getId();

        Folder f1 = new Folder();
        f1.setName(folderName);
        f1.setNodeType("cm:folder");

        f1.setIsFolder(true);
        f1.setParentId(myNodeId);
        f1.setAspectNames(Collections.singletonList("cm:auditable"));

        f1.setCreatedByUser(expectedUser);
        f1.setModifiedByUser(expectedUser);

        f1.expected(folderResp);

        // create empty file

        Document d1 = new Document();
        d1.setName("d1.txt");
        d1.setNodeType("cm:content");
        ContentInfo ci = new ContentInfo();
        ci.setMimeType("text/plain");
        d1.setContent(ci);

        HttpResponse response = post(postUrl, user1, toJsonAsStringNonNull(d1), 201);
        Document documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        String dId = documentResp.getId();

        d1.setIsFolder(false);
        d1.setParentId(myNodeId);
        d1.setAspectNames(Collections.singletonList("cm:auditable"));

        d1.setCreatedByUser(expectedUser);
        d1.setModifiedByUser(expectedUser);

        d1.getContent().setMimeTypeName("Plain Text");
        d1.getContent().setSizeInBytes(0L);
        d1.getContent().setEncoding("UTF-8");

        d1.expected(documentResp);

        // update file - name (=> rename within current folder)

        Document dUpdate = new Document();
        dUpdate.setName("d1b.txt");

        response = put("nodes", user1, dId, toJsonAsStringNonNull(dUpdate), null, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        d1.setName("d1b.txt");
        d1.expected(documentResp);

        // update file - add some properties

        Map<String,Object> props = new HashMap<>();
        props.put("cm:title","my file title");
        props.put("cm:description","my file description");

        dUpdate = new Document();
        dUpdate.setProperties(props);

        response = put("nodes", user1, dId, toJsonAsStringNonNull(dUpdate), null, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        d1.setProperties(props);
        d1.setAspectNames(Arrays.asList("cm:auditable","cm:titled"));
        d1.expected(documentResp);

        // update file - add versionable aspect

        dUpdate = new Document();
        dUpdate.setAspectNames(Arrays.asList("cm:auditable","cm:titled","cm:versionable"));

        response = put("nodes", user1, dId, toJsonAsStringNonNull(dUpdate), null, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        //d1.getProperties().put("cm:versionLabel","1.0"); // TODO ... fix api ?!
        d1.setAspectNames(Arrays.asList("cm:auditable","cm:titled","cm:versionable"));
        d1.expected(documentResp);

        response = getSingle("nodes", user1, dId, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        d1.getProperties().put("cm:versionLabel","1.0");
        d1.expected(documentResp);

        // update file - remove titled aspect (and it's related aspect properties)

        dUpdate = new Document();
        dUpdate.setAspectNames(Arrays.asList("cm:auditable","cm:versionable"));

        response = put("nodes", user1, dId, toJsonAsStringNonNull(dUpdate), null, 200);
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

        response = put("nodes", user1, fId, toJsonAsStringNonNull(fUpdate), null, 200);
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

        response = put("nodes", user1, fId, toJsonAsStringNonNull(fUpdate), null, 200);
        folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);

        f1.getProperties().remove("cm:title");
        f1.expected(folderResp);

        // update folder - specialise node type

        fUpdate = new Folder();
        fUpdate.setNodeType("app:glossary");

        response = put("nodes", user1, fId, toJsonAsStringNonNull(fUpdate), null, 200);
        folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);

        f1.setNodeType("app:glossary");
        f1.expected(folderResp);

        // -ve test - fail on unknown property
        props = new HashMap<>();
        props.put("cm:xyz","my unknown property");
        dUpdate = new Document();
        dUpdate.setProperties(props);
        put("nodes", user1, dId, toJsonAsStringNonNull(dUpdate), null, 400);
        
        // -ve test - fail on unknown aspect
        List<String> aspects = new ArrayList<>(d1.getAspectNames());
        aspects.add("cm:unknownAspect");
        dUpdate = new Document();
        dUpdate.setAspectNames(aspects);
        put("nodes", user1, dId, toJsonAsStringNonNull(dUpdate), null, 400);

        // -ve test - duplicate name
        dUpdate = new Document();
        dUpdate.setName(folderName);
        put("nodes", user1, dId, toJsonAsStringNonNull(dUpdate), null, 409);

        // -ve test - unknown node id
        dUpdate = new Document();
        dUpdate.setName("some.txt");
        put("nodes", user1, UUID.randomUUID().toString(), toJsonAsStringNonNull(dUpdate), null, 404);

        // -ve test - generalise node type
        fUpdate = new Folder();
        fUpdate.setNodeType("cm:folder");
        put("nodes", user1, fId, toJsonAsStringNonNull(fUpdate), null, 400);

        // -ve test - try to move to a different parent using PUT (note: should use new POST /nodes/{nodeId}/move operation instead)

        folderResp = createFolder(user1, myNodeId, "folder 2");
        String f2Id = folderResp.getId();

        fUpdate = new Folder();
        fUpdate.setParentId(f2Id);
        put("nodes", user1, fId, toJsonAsStringNonNull(fUpdate), null, 400);

        // ok - if parent does not change
        fUpdate = new Folder();
        fUpdate.setParentId(myNodeId);
        put("nodes", user1, fId, toJsonAsStringNonNull(fUpdate), null, 200);
    }

    /**
     * Tests update owner (file or folder)
     * <p>PUT:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>}
     */
    @Test
    public void testUpdateOwner() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(user1);

        String ownerProp = "cm:owner";

        // create folder f1
        String folderName = "f1 "+System.currentTimeMillis();
        Folder folderResp = createFolder(user1, Nodes.PATH_SHARED, folderName);
        String f1Id = folderResp.getId();

        assertNull(user1, folderResp.getProperties()); // owner is implied

        // explicitly set owner to oneself
        Map<String, Object> props = new HashMap<>();
        props.put(ownerProp, user1);
        Folder fUpdate = new Folder();
        fUpdate.setProperties(props);

        HttpResponse response = put("nodes", user1, f1Id, toJsonAsStringNonNull(fUpdate), null, 200);
        folderResp = jacksonUtil.parseEntry(response.getJsonResponse(), Folder.class);

        assertEquals(user1, ((Map)folderResp.getProperties().get(ownerProp)).get("id"));

        // create doc d1
        NodeRef f1Ref = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, f1Id);
        String d1Name = "content1 " + System.currentTimeMillis();
        NodeRef d1Ref = repoService.createDocument(f1Ref, d1Name, "The quick brown fox jumps over the lazy dog.");
        String d1Id = d1Ref.getId();

        // get node info
        response = getSingle(NodesEntityResource.class, user1, d1Id, null, 200);
        Document documentResp = jacksonUtil.parseEntry(response.getJsonResponse(), Document.class);

        assertNull(user1, documentResp.getProperties()); // owner is implied

        props = new HashMap<>();
        props.put(ownerProp, user1);
        Document dUpdate = new Document();
        dUpdate.setProperties(props);

        response = put("nodes", user1, d1Id, toJsonAsStringNonNull(dUpdate), null, 200);
        documentResp = jacksonUtil.parseEntry(response.getJsonResponse(), Document.class);

        assertEquals(user1, ((Map)documentResp.getProperties().get(ownerProp)).get("id"));

        // -ve test - cannot set owner to a nonexistent user

        props = new HashMap<>();
        props.put(ownerProp, "unknownusernamedoesnotexist");
        dUpdate = new Document();
        dUpdate.setProperties(props);

        put("nodes", user1, d1Id, toJsonAsStringNonNull(dUpdate), null, 400);

        AuthenticationUtil.setFullyAuthenticatedUser(user2);

        response = getSingle("nodes", user1, d1Id, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        assertEquals(user1, ((Map)documentResp.getProperties().get(ownerProp)).get("id"));

        // -ve test - cannot take/change ownership

        props = new HashMap<>();
        props.put(ownerProp, user2);
        dUpdate = new Document();
        dUpdate.setProperties(props);

        put("nodes", user2, d1Id, toJsonAsStringNonNull(dUpdate), null, 403);

        props = new HashMap<>();
        props.put(ownerProp, user1);
        dUpdate = new Document();
        dUpdate.setProperties(props);

        put("nodes", user2, d1Id, toJsonAsStringNonNull(dUpdate), null, 403);

        AuthenticationUtil.setFullyAuthenticatedUser(user1);

        props = new HashMap<>();
        props.put(ownerProp, user2);
        dUpdate = new Document();
        dUpdate.setProperties(props);

        response = put("nodes", user1, d1Id, toJsonAsStringNonNull(dUpdate), null, 200);
        documentResp = jacksonUtil.parseEntry(response.getJsonResponse(), Document.class);

        assertEquals(user2, ((Map)documentResp.getProperties().get(ownerProp)).get("id"));

        AuthenticationUtil.setFullyAuthenticatedUser(user2);

        response = getSingle("nodes", user2, d1Id, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        assertEquals(user2, ((Map)documentResp.getProperties().get(ownerProp)).get("id"));

        // -ve test - user2 cannot delete the test folder/file - TODO is that expected ?
        delete("nodes", user2, f1Id, 403);

        AuthenticationUtil.setFullyAuthenticatedUser(user1);

        delete("nodes", user1, f1Id, 204);
    }


    /**
     * Tests update file content
     * <p>PUT:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/content}
     */
    @Test
    public void testUpdateFileWithBinaryUpload() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(user1);

        String myNodeId = getMyNodeId(user1);
        NodeRef myFilesNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, myNodeId);

        Folder f1 = new Folder();
        f1.setName("F1");
        f1.setNodeType("cm:folder");

        HttpResponse response = post(getChildrenUrl(myFilesNodeRef), user1, toJsonAsStringNonNull(f1), 201);
        Folder folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertEquals(f1.getName(), folderResp.getName());
        final String f1_nodeId = folderResp.getId();
        assertNotNull(f1_nodeId);

        Document doc = new Document();
        final String docName = "testdoc";
        doc.setName(docName);
        doc.setNodeType("cm:content");
        doc.setProperties(Collections.singletonMap("cm:title", (Object)"test title"));
        ContentInfo contentInfo = new ContentInfo();
        contentInfo.setMimeType(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        doc.setContent(contentInfo);

        // create an empty file within F1 folder
        response = post(getChildrenUrl(f1_nodeId), user1, toJsonAsStringNonNull(doc), 201);
        Document docResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertEquals(docName, docResp.getName());
        assertNotNull(docResp.getContent());
        assertEquals(0, docResp.getContent().getSizeInBytes().intValue());
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, docResp.getContent().getMimeType());

        // Update content & Download URL
        final String url = URL_NODES + docResp.getId() + "/content";

        // Update the empty node's content
        String content = "The quick brown fox jumps over the lazy dog.";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
        File txtFile = TempFileProvider.createTempFile(inputStream, getClass().getSimpleName(), ".txt");
        BinaryPayload payload = new BinaryPayload(txtFile, MimetypeMap.MIMETYPE_TEXT_PLAIN);

        // Try to update a folder!
        putBinary(URL_NODES + f1_nodeId + "/content", user1, payload, null, null, 400);

        // Try to update a non-existent file
        putBinary(URL_NODES + UUID.randomUUID().toString() + "/content", user1, payload, null, null, 404);

        // Update the empty file
        response = putBinary(url, user1, payload, null, null, 200);
        docResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertEquals(docName, docResp.getName());
        assertNotNull(docResp.getId());
        assertNotNull(docResp.getCreatedAt());
        assertNotNull(docResp.getCreatedByUser());
        assertNotNull(docResp.getModifiedAt());
        assertNotNull(docResp.getModifiedByUser());
        assertFalse(docResp.getIsFolder());
        assertNull(docResp.getIsLink());
        assertEquals("cm:content", docResp.getNodeType());
        assertNotNull(docResp.getParentId());
        assertEquals(f1_nodeId, docResp.getParentId());
        assertNotNull(docResp.getProperties());
        assertNotNull(docResp.getAspectNames());
        contentInfo = docResp.getContent();
        assertNotNull(contentInfo);
        assertNotNull(contentInfo.getEncoding());
        // Default encoding
        assertEquals("UTF-8", contentInfo.getEncoding());
        assertTrue(contentInfo.getSizeInBytes() > 0);
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, contentInfo.getMimeType());
        assertNotNull(contentInfo.getMimeTypeName());
        // path is not part of the default response
        assertNull(docResp.getPath());

        // Download the file
        response = getSingle(url, user1, null, 200);
        assertEquals(content, response.getResponse());

        // Update the node's content again. Also, change the mimeType and make the response to return path!
        File pdfFile = getResourceFile("quick.pdf");
        payload = new BinaryPayload(pdfFile, MimetypeMap.MIMETYPE_PDF, "ISO-8859-1");

        response = putBinary(url + "?select=path", user1, payload, null, null, 200);
        docResp = jacksonUtil.parseEntry(response.getJsonResponse(), Document.class);
        assertEquals(docName, docResp.getName());
        assertNotNull(docResp.getContent());
        assertEquals("ISO-8859-1", docResp.getContent().getEncoding());
        assertTrue(docResp.getContent().getSizeInBytes().intValue() > 0);
        assertEquals(MimetypeMap.MIMETYPE_PDF, docResp.getContent().getMimeType());
        PathInfo pathInfo = docResp.getPath();
        assertNotNull(pathInfo);
        assertTrue(pathInfo.getIsComplete());
        List<ElementInfo> pathElements = pathInfo.getElements();
        assertNotNull(pathElements);
        assertTrue(pathElements.size() > 0);
        // check the last element is F1
        assertEquals(f1.getName(), pathElements.get(pathElements.size() - 1).getName());

        // update the original content with different encoding
        payload = new BinaryPayload(txtFile, MimetypeMap.MIMETYPE_TEXT_PLAIN, "ISO-8859-15");
        response = putBinary(url, user1, payload, null, null, 200);
        docResp = jacksonUtil.parseEntry(response.getJsonResponse(), Document.class);
        assertEquals(docName, docResp.getName());
        assertNotNull(docResp.getContent());
        assertEquals("ISO-8859-15", docResp.getContent().getEncoding());
        assertTrue(docResp.getContent().getSizeInBytes().intValue() > 0);
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, docResp.getContent().getMimeType());

        // Download the file
        response = getSingle(url, user1, null, 200);
        assertNotNull(content, response.getResponse());
    }

    /**
     * Tests download of file/content.
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/content}
     */
    @Test
    public void testDownloadFileContent() throws Exception
    {
        //
        // Test plain text
        //

        String fileName = "quick-1.txt";
        File file = getResourceFile(fileName);
        
        MultiPartBuilder multiPartBuilder = MultiPartBuilder.create()
                .setFileData(new FileData(fileName, file, MimetypeMap.MIMETYPE_TEXT_PLAIN));
        MultiPartRequest reqBody = multiPartBuilder.build();
        
        // Upload text content
        HttpResponse response = post(getChildrenUrl(Nodes.PATH_MY), user1, reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document document = jacksonUtil.parseEntry(response.getJsonResponse(), Document.class);
        
        String contentNodeId = document.getId();
        
        // Check the upload response
        assertEquals(fileName, document.getName());
        ContentInfo contentInfo = document.getContent();
        assertNotNull(contentInfo);
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, contentInfo.getMimeType());

        // Download text content - by default with Content-Disposition header
        response = getSingle(NodesEntityResource.class, user1, contentNodeId+"/content", null, 200);

        String textContent = response.getResponse();
        assertEquals("The quick brown fox jumps over the lazy dog", textContent);
        assertEquals("attachment; filename=\"quick-1.txt\"; filename*=UTF-8''quick-1.txt", response.getHeaders().get("Content-Disposition"));


        //
        // Test binary (eg. PDF)
        //

        fileName = "quick.pdf";
        file = getResourceFile(fileName);
        byte[] originalBytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));

        multiPartBuilder = MultiPartBuilder.create()
                .setFileData(new FileData(fileName, file, MimetypeMap.MIMETYPE_PDF));
        reqBody = multiPartBuilder.build();

        // Upload binary content
        response = post(getChildrenUrl(Nodes.PATH_MY), user1, reqBody.getBody(), null, reqBody.getContentType(), 201);
        document = jacksonUtil.parseEntry(response.getJsonResponse(), Document.class);

        contentNodeId = document.getId();

        // Check the upload response
        assertEquals(fileName, document.getName());
        contentInfo = document.getContent();
        assertNotNull(contentInfo);
        assertEquals(MimetypeMap.MIMETYPE_PDF, contentInfo.getMimeType());

        // Download binary content (as bytes) - without Content-Disposition header (attachment=false)
        Map<String, String> params = new LinkedHashMap<>();
        params.put("attachment", "false");

        response = getSingle(NodesEntityResource.class, user1, contentNodeId+"/content", params, 200);

        byte[] bytes = response.getResponseAsBytes();

        assertArrayEquals(originalBytes, bytes);
        assertNull(response.getHeaders().get("Content-Disposition"));
    }

    private String getChildrenUrl(NodeRef parentNodeRef)
    {
        return getChildrenUrl(parentNodeRef.getId());
    }

    private String getChildrenUrl(String parentId)
    {
        return URL_NODES + parentId + "/children";
    }

    private File getResourceFile(String fileName) throws FileNotFoundException
    {
        URL url = NodeApiTest.class.getClassLoader().getResource(RESOURCE_PREFIX + fileName);
        if (url == null)
        {
            fail("Cannot get the resource: " + fileName);
        }
        return ResourceUtils.getFile(url);
    }

    @Override
    public String getScope()
    {
        return "public";
    }

    // TODO move into RestApiUtil (and statically init the OM)
    private String toJsonAsStringNonNull(Object obj) throws IOException
    {
        ObjectMapper om = new ObjectMapper();
        om.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        return om.writeValueAsString(obj);
    }
}
