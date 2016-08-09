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

import org.alfresco.repo.content.ContentLimitProvider.SimpleFixedLimitProvider;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.NodeTarget;
import org.alfresco.rest.api.model.Site;
import org.alfresco.rest.api.nodes.NodesEntityResource;
import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.alfresco.rest.api.tests.RepoService.TestPerson;
import org.alfresco.rest.api.tests.RepoService.TestSite;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.alfresco.rest.api.tests.client.PublicApiClient.ExpectedPaging;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.PublicApiHttpClient.BinaryPayload;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.rest.api.tests.client.data.Association;
import org.alfresco.rest.api.tests.client.data.ContentInfo;
import org.alfresco.rest.api.tests.client.data.Document;
import org.alfresco.rest.api.tests.client.data.Folder;
import org.alfresco.rest.api.tests.client.data.Node;
import org.alfresco.rest.api.tests.client.data.PathInfo;
import org.alfresco.rest.api.tests.client.data.PathInfo.ElementInfo;
import org.alfresco.rest.api.tests.client.data.SiteMember;
import org.alfresco.rest.api.tests.client.data.SiteRole;
import org.alfresco.rest.api.tests.client.data.UserInfo;
import org.alfresco.rest.api.tests.util.JacksonUtil;
import org.alfresco.rest.api.tests.util.MultiPartBuilder;
import org.alfresco.rest.api.tests.util.MultiPartBuilder.FileData;
import org.alfresco.rest.api.tests.util.MultiPartBuilder.MultiPartRequest;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.rest.framework.jacksonextensions.JacksonHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.TempFileProvider;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
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

/**
 * API tests for:
 * <ul>
 * <li> {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>} </li>
 * <li> {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/children} </li>
 * </ul>
 *
 * TODO
 * - improve test 'fwk' to enable api tests to be run against remote repo (rather than embedded jetty)
 * - requires replacement of non-remote calls with remote (preferably public) apis
 *   - eg. createUser (or any other usage of repoService), permissionService, node/archiveService
 *
 * @author Jamal Kaabi-Mofrad
 * @author janv
 */
public class NodeApiTest extends AbstractBaseApiTest
{
    private static final String PROP_OWNER = "cm:owner";

    TestNetwork networkOne;

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
    private Site userOneN1Site;

    private String user1;
    private String user2;
    private List<String> users = new ArrayList<>();

    protected MutableAuthenticationService authenticationService;
    protected PersonService personService;
    protected JacksonUtil jacksonUtil;
    protected PermissionService permissionService;

    protected NodeArchiveService nodeArchiveService;
    protected NodeService nodeService;

    private final String RUNID = System.currentTimeMillis()+"";


    @Before
    public void setup() throws Exception
    {
        authenticationService = applicationContext.getBean("authenticationService", MutableAuthenticationService.class);
        personService = applicationContext.getBean("personService", PersonService.class);
        jacksonUtil = new JacksonUtil(applicationContext.getBean("jsonHelper", JacksonHelper.class));
        permissionService = applicationContext.getBean("permissionService", PermissionService.class);

        // TODO replace with V1 REST API for Trashcan
        nodeArchiveService = applicationContext.getBean("nodeArchiveService", NodeArchiveService.class);
        nodeService = applicationContext.getBean("nodeService", NodeService.class);

        user1 = createUser("user1" + System.currentTimeMillis());
        user2 = createUser("user2" + System.currentTimeMillis());

        // to enable admin access via test calls
        getOrCreateUser("admin", "admin");

        // We just need to clean the on-premise-users,
        // so the tests for the specific network would work.
        users.add(user1);
        users.add(user2);

        getTestFixture(false);
        networkOne = getRepoService().createNetwork(NodeApiTest.class.getName().toLowerCase(), true);
        networkOne.create();
        userOneN1 = networkOne.createUser();
        userTwoN1 = networkOne.createUser();

        userOneN1Site = createSite(networkOne.getId(), userOneN1.getId(), SiteVisibility.PRIVATE);
    }

    @After
    public void tearDown() throws Exception
    {
        deleteSite(networkOne.getId(), userOneN1.getId(), userOneN1Site.getId(), 204);

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

    /**
     * Tests get document library children.
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/children}
     */
    @Test
    public void testListDocLibChildren() throws Exception
    {
        String userOneId = userOneN1.getId();
        String userTwoId = userTwoN1.getId();

        String docLibNodeId = getSiteContainerNodeId(networkOne.getId(), userOneId, userOneN1Site.getId(), "documentLibrary");

        String folder1 = "folder" + System.currentTimeMillis() + "_1";
        createFolder(userOneId, docLibNodeId, folder1, null).getId();

        String folder2 = "folder" + System.currentTimeMillis() + "_2";
        createFolder(userOneId, docLibNodeId, folder2, null).getId();

        String content1 = "content" + System.currentTimeMillis() + "_1";
        createTextFile(userOneId, docLibNodeId, content1, "The quick brown fox jumps over the lazy dog 1.").getId();

        String content2 = "content" + System.currentTimeMillis() + "_2";
        createTextFile(userOneId, docLibNodeId, content2, "The quick brown fox jumps over the lazy dog 2.").getId();

        String forum1 = "forum" + System.currentTimeMillis() + "_1";
        createNode(userOneId, docLibNodeId, forum1, "fm:topic", null);

        Paging paging = getPaging(0, 100);
        HttpResponse response = getAll(getNodeChildrenUrl(docLibNodeId), userOneId, paging, 200);
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
        response = getAll(getNodeChildrenUrl(docLibNodeId), userOneId, paging, orderBy, 200);
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
        response = getAll(getNodeChildrenUrl(docLibNodeId), userOneId, paging, orderBy, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        assertEquals(4, nodes.size());
        assertEquals(content2, nodes.get(0).getName());
        assertEquals(content1, nodes.get(1).getName());
        assertEquals(folder2, nodes.get(2).getName());
        assertEquals(folder1, nodes.get(3).getName());

        // Order by folders and modified date last
        orderBy = Collections.singletonMap("orderBy", "isFolder,modifiedAt");
        response = getAll(getNodeChildrenUrl(docLibNodeId), userOneId, paging, orderBy, 200);
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
        response = getAll(getNodeChildrenUrl(docLibNodeId), userOneId, paging, orderBy, 200);
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
        response = getAll(getNodeChildrenUrl(docLibNodeId), userOneId, paging, orderBy, 200);
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
        response = getAll(getNodeChildrenUrl(docLibNodeId), userOneId, paging, orderBy, 200);
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
        response = getAll(getNodeChildrenUrl(docLibNodeId), userOneId, paging, orderBy, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        assertEquals(2, nodes.size());
        assertEquals(content2, nodes.get(0).getName());
        assertEquals(content1, nodes.get(1).getName());
        expectedPaging = RestApiUtil.parsePaging(response.getJsonResponse());
        assertEquals(2, expectedPaging.getCount().intValue());
        assertEquals(2, expectedPaging.getSkipCount().intValue());
        assertEquals(100, expectedPaging.getMaxItems().intValue());
        assertFalse(expectedPaging.getHasMoreItems().booleanValue());


        // userTwoN1 tries to access userOneN1's docLib
        AuthenticationUtil.setFullyAuthenticatedUser(userTwoId);
        paging = getPaging(0, Integer.MAX_VALUE);
        getAll(getNodeChildrenUrl(docLibNodeId), userTwoId, paging, 403);

        // -ve test - paging (via list children) cannot have skipCount < 0
        paging = getPaging(-1, 4);
        getAll(getNodeChildrenUrl(docLibNodeId), userOneId, paging, orderBy, 400);


        // -ve test - paging (via list children) cannot have maxItems < 1
        paging = getPaging(0, 0);
        getAll(getNodeChildrenUrl(docLibNodeId), userOneId, paging, orderBy, 400);
    }

    /**
     * Tests get user's home children.
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/children}
     */
    @Test
    public void testListMyFilesChildren() throws Exception
    {
        String myNodeId = getMyNodeId(user1);

        String myChildrenUrl = getNodeChildrenUrl(Nodes.PATH_MY);
        String rootChildrenUrl = getNodeChildrenUrl(Nodes.PATH_ROOT);

        Map<String, Object> props = new HashMap<>(1);
        props.put("cm:title", "This is folder 1");
        String folder1 = "folder " + System.currentTimeMillis() + " 1";
        String folder1_Id = createFolder(user1, myNodeId, folder1, props).getId();

        String contentF1 = "content" + System.currentTimeMillis() + " in folder 1";
        String contentF1_Id = createTextFile(user1, folder1_Id, contentF1, "The quick brown fox jumps over the lazy dog 1.").getId();

        props = new HashMap<>(1);
        props.put("cm:title", "This is folder 2");
        String folder2 = "folder " + System.currentTimeMillis() + " 2";
        String folder2_Id = createFolder(user1, myNodeId, folder2, props).getId();

        String contentF2 = "content" + System.currentTimeMillis() + " in folder 2.txt";
        String contentF2_Id = createTextFile(user1, folder2_Id, contentF2, "The quick brown fox jumps over the lazy dog 2.").getId();

        String content1 = "content" + System.currentTimeMillis() + " 1.txt";
        String content1_Id = createTextFile(user1, myNodeId, content1, "The quick brown fox jumps over the lazy dog.").getId();

        props = new HashMap<>();
        props.put(PROP_OWNER, user1);
        props.put("cm:lastThumbnailModification", Collections.singletonList("doclib:1444660852296"));

        Node nodeUpdate = new Node();
        nodeUpdate.setProperties(props);
        put(URL_NODES, user1, content1_Id, toJsonAsStringNonNull(nodeUpdate), null, 200);

        List<String> folderIds = Arrays.asList(folder1_Id, folder2_Id);
        List<String> contentIds = Arrays.asList(content1_Id);


        Paging paging = getPaging(0, Integer.MAX_VALUE);
        HttpResponse response = getAll(myChildrenUrl, user1, paging, 200);
        List<Document> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Document.class);
        assertEquals(3, nodes.size());

        // Order by folders and modified date first
        Map<String, String> orderBy = Collections.singletonMap("orderBy", "isFolder DESC,modifiedAt DESC");
        response = getAll(myChildrenUrl, user1, paging, orderBy, 200);
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
        assertEquals(user1 + " " + user1, createdByUser.getDisplayName());
        UserInfo modifiedByUser = node.getModifiedByUser();
        assertEquals(user1, modifiedByUser.getId());
        assertEquals(user1 + " " + user1, modifiedByUser.getDisplayName());
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, node.getContent().getMimeType());
        assertNotNull(node.getContent().getMimeTypeName());
        assertNotNull(node.getContent().getEncoding());
        assertTrue(node.getContent().getSizeInBytes() > 0);

        // request without "include"
        Map<String, String> params = new HashMap<>();
        response = getAll(myChildrenUrl, user1, paging, params, 200);
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
        response = getAll(myChildrenUrl, user1, paging, params, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Document.class);
        for (Node n : nodes)
        {
            assertNotNull("There should be a 'isLink' object in the response.", n.getIsLink());
        }

        // request with include - example 2
        params = new HashMap<>();
        params.put("include", "aspectNames,properties,path,isLink");
        response = getAll(myChildrenUrl, user1, paging, params, 200);
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
        response = getAll(myChildrenUrl, user1, paging, params, 200);
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
        response = getAll(myChildrenUrl, user1, paging, params, 200);
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
        response = getAll(myChildrenUrl, user1, paging, params, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Document.class);
        assertEquals(1, nodes.size());
        assertFalse(nodes.get(0).getIsFolder());
        assertTrue(nodes.get(0).getIsFile());
        assertTrue(contentIds.contains(nodes.get(0).getId()));

        // list children via relativePath

        params = Collections.singletonMap(Nodes.PARAM_RELATIVE_PATH, folder1);
        response = getAll(myChildrenUrl, user1, paging, params, 200);
        JSONObject jsonResponse = response.getJsonResponse();
        nodes = RestApiUtil.parseRestApiEntries(jsonResponse, Document.class);
        assertEquals(1, nodes.size());
        assertEquals(contentF1_Id, nodes.get(0).getId());

        JSONObject jsonList = (JSONObject)jsonResponse.get("list");
        assertNotNull(jsonList);
        JSONObject jsonSrcObj = (JSONObject)jsonResponse.get("source");
        assertNull(jsonSrcObj);

        params = Collections.singletonMap(Nodes.PARAM_RELATIVE_PATH, "User Homes/" + user1 + "/" + folder2);
        response = getAll(rootChildrenUrl, user1, paging, params, 200);
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
        params.put(Nodes.PARAM_RELATIVE_PATH, "User Homes/" + user1 + "/" + folder2);
        params.put("includeSource", "true");
        params.put("include", "path,isLink");
        params.put("fields", "id");
        response = getAll(rootChildrenUrl, user1, paging, params, 200);
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
        getAll(myChildrenUrl, user1, paging, params, 400);

        paging = getPaging(0, 10);

        // -ve test - list folder children for unknown node should return 404
        getAll(getNodeChildrenUrl(UUID.randomUUID().toString()), user1, paging, 404);

        // -ve test - user2 tries to access user1's home folder
        AuthenticationUtil.setFullyAuthenticatedUser(user2);
        getAll(getNodeChildrenUrl(myNodeId), user2, paging, 403);

        // -ve test - try to list children using relative path to unknown node
        params = Collections.singletonMap(Nodes.PARAM_RELATIVE_PATH, "User Homes/" + user1 + "/unknown");
        getAll(rootChildrenUrl, user1, paging, params, 404);

        // -ve test - try to list children using relative path to node for which user does not have read permission (expect 404 instead of 403)
        params = Collections.singletonMap(Nodes.PARAM_RELATIVE_PATH, "User Homes/" + user2);
        getAll(rootChildrenUrl, user1, paging, params, 404);

        // -ve test - list folder children with relative path to unknown node should return 400
        params = Collections.singletonMap(Nodes.PARAM_RELATIVE_PATH, "/unknown");
        getAll(getNodeChildrenUrl(content1_Id), user1, paging, params, 400);
    }

    /**
     * Tests get node with path information.
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>?include=path}
     */
    @Test
    public void testGetPathElements_DocLib() throws Exception
    {
        String userId = userOneN1.getId();

        publicApiClient.setRequestContext(new RequestContext(userOneN1.getId()));
        PublicApiClient.Sites sitesProxy = publicApiClient.sites();
        sitesProxy.createSiteMember(userOneN1Site.getId(), new SiteMember(userTwoN1.getId(), SiteRole.SiteConsumer.toString()));

        String docLibNodeId = getSiteContainerNodeId(networkOne.getId(), userOneN1.getId(), userOneN1Site.getId(), "documentLibrary");

        // /Company Home/Sites/RandomSite<timestamp>/documentLibrary/folder<timestamp>_A
        String folderA = "folder" + System.currentTimeMillis() + "_A";
        String folderA_Id = createFolder(userId, docLibNodeId, folderA).getId();

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
        String content1_Id = createTextFile(userId, folderC_Id, content, "The quick brown fox jumps over the lazy dog.").getId();


        // TODO refactor with remote permission api calls (use v0 until we have v1 ?)
        AuthenticationUtil.setFullyAuthenticatedUser(userId);
        // Revoke folderB inherited permissions
        permissionService.setInheritParentPermissions(folderB_Ref, false);
        // Grant userTwoN1 permission for folderC
        permissionService.setPermission(folderC_Ref, userTwoN1.getId(), PermissionService.CONSUMER, true);

        //...nodes/nodeId?include=path
        Map<String, String> params = Collections.singletonMap("include", "path");
        HttpResponse response = getSingle(NodesEntityResource.class, userOneN1.getId(), content1_Id, params, 200);
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
        assertEquals(userOneN1Site.getId(), pathElements.get(2).getName());
        assertEquals("documentLibrary", pathElements.get(3).getName());
        assertEquals(folderA, pathElements.get(4).getName());
        assertEquals(folderB, pathElements.get(5).getName());
        assertEquals(folderC, pathElements.get(6).getName());

        // Try the above tests with userTwoN1 (site consumer)
        AuthenticationUtil.setFullyAuthenticatedUser(userTwoN1.getId());
        response = getSingle(NodesEntityResource.class, userTwoN1.getId(), content1_Id, params, 200);
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
        Node node = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        String rootNodeId = node.getId();

        response = getSingle(NodesEntityResource.class, user1, Nodes.PATH_MY, null, 200);
        node = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        String myFilesNodeId = node.getId();
        assertNotNull(myFilesNodeId);
        assertEquals(user1, node.getName());
        assertTrue(node.getIsFolder());
        assertFalse(node.getIsFile());

        String userHomesId = node.getParentId();

        // /Company Home/User Homes/user<timestamp>/folder<timestamp>_A
        String folderA = "folder" + System.currentTimeMillis() + "_A";
        String folderA_Id = createFolder(user1, myFilesNodeId, folderA).getId();

        // /Company Home/User Homes/user<timestamp>/folder<timestamp>_A/folder<timestamp>_B
        String folderB = "folder" + System.currentTimeMillis() + "_B";
        String folderB_Id = createFolder(user1, folderA_Id, folderB).getId();

        // /Company Home/User Homes/user<timestamp>/folder<timestamp>_A/folder<timestamp>_B/content<timestamp>
        String title = "test title";
        Map<String,String> docProps = new HashMap<>();
        docProps.put("cm:title", title);
        String contentName = "content " + System.currentTimeMillis() + ".txt";
        String content1Id = createTextFile(user1, folderB_Id, contentName, "The quick brown fox jumps over the lazy dog.", "UTF-8", docProps).getId();


        // get node info
        response = getSingle(NodesEntityResource.class, user1, content1Id, null, 200);
        Document documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        String content_Id = documentResp.getId();

        // Expected result ...
        UserInfo expectedUser = new UserInfo(user1, user1+" "+user1);

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

        d1.setProperties(props);
        d1.setAspectNames(Arrays.asList("cm:auditable","cm:titled","cm:versionable","cm:author"));

        // Note: Path is not part of the default info
        d1.expected(documentResp);

        // get node info + path
        //...nodes/nodeId?include=path
        Map<String, String> params = Collections.singletonMap("include", "path");
        response = getSingle(NodesEntityResource.class, user1, content1Id, params, 200);
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
        response = getSingle(NodesEntityResource.class, user1, Nodes.PATH_MY, params, 200);
        Folder folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertEquals(folderB_Id, folderResp.getId());

        params = Collections.singletonMap(Nodes.PARAM_RELATIVE_PATH, folderA+"/"+folderB+"/"+contentName);
        response = getSingle(NodesEntityResource.class, user1, Nodes.PATH_MY, params, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertEquals(content_Id, documentResp.getId());

        // test path with utf-8 encoded param (eg. ¢ => )
        String folderC = "folder" + System.currentTimeMillis() + " ¢";
        String folderC_Id = createFolder(user1, folderB_Id, folderC).getId();

        params = Collections.singletonMap(Nodes.PARAM_RELATIVE_PATH, "/"+folderA+"/"+folderB+"/"+folderC);
        response = getSingle(NodesEntityResource.class, user1, Nodes.PATH_MY, params, 200);
        folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertEquals(folderC_Id, folderResp.getId());

        // -ve test - get info for unknown node should return 404
        getSingle(NodesEntityResource.class, user1, UUID.randomUUID().toString(), null, 404);

        // -ve test - user2 tries to get node info about user1's home folder
        AuthenticationUtil.setFullyAuthenticatedUser(user2);
        getSingle(NodesEntityResource.class, user2, myFilesNodeId, null, 403);

        // -ve test - try to get node info using relative path to unknown node
        params = Collections.singletonMap(Nodes.PARAM_RELATIVE_PATH, folderA+"/unknown");
        getSingle(NodesEntityResource.class, user1, Nodes.PATH_MY, params, 404);

        // -ve test - try to get node info using relative path to node for which user does not have read permission (expect 404 instead of 403)
        params = Collections.singletonMap(Nodes.PARAM_RELATIVE_PATH, "User Homes/"+user2);
        getSingle(NodesEntityResource.class, user1, Nodes.PATH_ROOT, params, 404);

        // -ve test - attempt to get node info for non-folder node with relative path should return 400
        params = Collections.singletonMap(Nodes.PARAM_RELATIVE_PATH, "/unknown");
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
        Node node = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertEquals("Company Home", node.getName());
        assertNotNull(node.getId());
        assertNull(node.getPath());

        // unknown alias
        getSingle(NodesEntityResource.class, user1, "testSomeUndefinedAlias", null, 404);

        response = getSingle(NodesEntityResource.class, user1, Nodes.PATH_MY, null, 200);
        node = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        String myFilesNodeId = node.getId();
        assertNotNull(myFilesNodeId);
        assertEquals(user1, node.getName());
        assertTrue(node.getIsFolder());
        assertFalse(node.getIsFile());
        assertNull(node.getPath()); // note: path can be optionally "include"'ed - see separate test

        response = getSingle(NodesEntityResource.class, user1, Nodes.PATH_SHARED, null, 200);
        node = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        String sharedFilesNodeId = node.getId();
        assertNotNull(sharedFilesNodeId);
        assertEquals("Shared", node.getName());
        assertTrue(node.getIsFolder());
        assertFalse(node.getIsFile());
        assertNull(node.getPath());

        //Delete user1's home
        delete(URL_NODES, "admin", myFilesNodeId, 204);

        AuthenticationUtil.setFullyAuthenticatedUser(user1);
        getSingle(NodesEntityResource.class, user1, Nodes.PATH_MY, null, 404); // Not found
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
        String fId = createFolder(user1, getMyNodeId(user1), "test-folder-guess-"+System.currentTimeMillis()).getId();

        // create empty files

        Document d = new Document();
        d.setName("my doc");
        d.setNodeType(TYPE_CM_CONTENT);

        HttpResponse response = post(getNodeChildrenUrl(fId), user1, toJsonAsStringNonNull(d), 201);
        Document documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertEquals(MimetypeMap.MIMETYPE_BINARY, documentResp.getContent().getMimeType());
        assertEquals("Binary File (Octet Stream)", documentResp.getContent().getMimeTypeName());
        assertEquals(0L, documentResp.getContent().getSizeInBytes().longValue());
        assertEquals("UTF-8",  documentResp.getContent().getEncoding());

        d = new Document();
        d.setName("my doc.txt");
        d.setNodeType(TYPE_CM_CONTENT);

        response = post(getNodeChildrenUrl(fId), user1, toJsonAsStringNonNull(d), 201);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, documentResp.getContent().getMimeType());
        assertEquals("Plain Text", documentResp.getContent().getMimeTypeName());
        assertEquals(0L, documentResp.getContent().getSizeInBytes().longValue());
        assertEquals("UTF-8",  documentResp.getContent().getEncoding());

        d = new Document();
        d.setName("my doc.pdf");
        d.setNodeType(TYPE_CM_CONTENT);

        response = post(getNodeChildrenUrl(fId), user1, toJsonAsStringNonNull(d), 201);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertEquals(MimetypeMap.MIMETYPE_PDF, documentResp.getContent().getMimeType());
        assertEquals("Adobe PDF Document", documentResp.getContent().getMimeTypeName());
        assertEquals(0L, documentResp.getContent().getSizeInBytes().longValue());
        assertEquals("UTF-8",  documentResp.getContent().getEncoding());

        // upload files

        String fileName = "quick.pdf";
        File file = getResourceFile(fileName);

        MultiPartBuilder multiPartBuilder = MultiPartBuilder.create()
                .setFileData(new FileData(fileName, file));
        MultiPartRequest reqBody = multiPartBuilder.build();

        response = post(getNodeChildrenUrl(fId), user1, reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        ContentInfo contentInfo = document.getContent();
        assertEquals(MimetypeMap.MIMETYPE_PDF, contentInfo.getMimeType());
        assertEquals("UTF-8", contentInfo.getEncoding());

        fileName = "example-1.txt";
        file = getResourceFile(fileName);

        multiPartBuilder = MultiPartBuilder.create()
                .setFileData(new FileData(fileName, file));
        reqBody = multiPartBuilder.build();

        response = post(getNodeChildrenUrl(fId), user1, reqBody.getBody(), null, reqBody.getContentType(), 201);
        document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        contentInfo = document.getContent();
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, contentInfo.getMimeType());
        assertEquals("ISO-8859-1", contentInfo.getEncoding());

        fileName = "example-2.txt";
        file = getResourceFile(fileName);

        multiPartBuilder = MultiPartBuilder.create()
                .setFileData(new FileData(fileName, file));
        reqBody = multiPartBuilder.build();

        response = post(getNodeChildrenUrl(fId), user1, reqBody.getBody(), null, reqBody.getContentType(), 201);
        document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        contentInfo = document.getContent();
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, contentInfo.getMimeType());
        assertEquals("UTF-8", contentInfo.getEncoding());

        fileName = "shift-jis.txt";
        file = getResourceFile(fileName);

        multiPartBuilder = MultiPartBuilder.create()
                .setFileData(new FileData(fileName, file));
        reqBody = multiPartBuilder.build();

        response = post(getNodeChildrenUrl(fId), user1, reqBody.getBody(), null, reqBody.getContentType(), 201);
        document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        contentInfo = document.getContent();
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, contentInfo.getMimeType());
        assertEquals("Shift_JIS", contentInfo.getEncoding());

        fileName = "example-1.xml";
        file = getResourceFile(fileName);

        multiPartBuilder = MultiPartBuilder.create()
                .setFileData(new FileData(fileName, file));
        reqBody = multiPartBuilder.build();

        response = post(getNodeChildrenUrl(fId), user1, reqBody.getBody(), null, reqBody.getContentType(), 201);
        document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        contentInfo = document.getContent();
        assertEquals(MimetypeMap.MIMETYPE_XML, contentInfo.getMimeType());
        assertEquals("ISO-8859-1", contentInfo.getEncoding());

        fileName = "example-2.xml";
        file = getResourceFile(fileName);

        multiPartBuilder = MultiPartBuilder.create()
                .setFileData(new FileData(fileName, file));
        reqBody = multiPartBuilder.build();

        response = post(getNodeChildrenUrl(fId), user1, reqBody.getBody(), null, reqBody.getContentType(), 201);
        document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        contentInfo = document.getContent();
        assertEquals(MimetypeMap.MIMETYPE_XML, contentInfo.getMimeType());
        assertEquals("UTF-8", contentInfo.getEncoding());

        // cleanup
        delete(URL_NODES, user1, fId, 204);
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
        HttpResponse response = getAll(getNodeChildrenUrl(Nodes.PATH_MY), user1, paging, 200);
        PublicApiClient.ExpectedPaging pagingResult = parsePaging(response.getJsonResponse());
        assertNotNull(paging);
        final int numOfNodes = pagingResult.getCount();

        MultiPartBuilder multiPartBuilder = MultiPartBuilder.create()
                    .setFileData(new FileData(fileName, file));
        MultiPartRequest reqBody = multiPartBuilder.build();

        // Try to upload into a non-existent folder
        post(getNodeChildrenUrl(UUID.randomUUID().toString()), user1, reqBody.getBody(), null, reqBody.getContentType(), 404);

        // Upload
        response = post(getNodeChildrenUrl(Nodes.PATH_MY), user1, reqBody.getBody(), null, reqBody.getContentType(), 201);
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
        response = getSingle(NodesEntityResource.class, user1, document.getId(), null, 200);
        document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertEquals(fileName, document.getName());
        contentInfo = document.getContent();
        assertNotNull(contentInfo);
        assertEquals(MimetypeMap.MIMETYPE_PDF, contentInfo.getMimeType());

        // Check 'get children' is confirming the upload
        response = getAll(getNodeChildrenUrl(Nodes.PATH_MY), user1, paging, 200);
        pagingResult = parsePaging(response.getJsonResponse());
        assertNotNull(paging);
        assertEquals(numOfNodes + 1, pagingResult.getCount().intValue());

        // Upload the same file again to check the name conflicts handling
        post(getNodeChildrenUrl(Nodes.PATH_MY), user1, reqBody.getBody(), null, reqBody.getContentType(), 409);

        response = getAll(getNodeChildrenUrl(Nodes.PATH_MY), user1, paging, 200);
        pagingResult = parsePaging(response.getJsonResponse());
        assertNotNull(paging);
        assertEquals("Duplicate file name. The file shouldn't have been uploaded.", numOfNodes + 1, pagingResult.getCount().intValue());

        // Set autoRename=true and upload the same file again
        reqBody = MultiPartBuilder.copy(multiPartBuilder)
                          .setAutoRename(true)
                          .build();

        response = post(getNodeChildrenUrl(Nodes.PATH_MY), user1, reqBody.getBody(), null, reqBody.getContentType(), 201);
        document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        // Check the upload response
        assertEquals("quick-1.pdf", document.getName());

        // upload the same file again, and request the path info to be present in the response
        response = post(getNodeChildrenUrl(Nodes.PATH_MY), user1, reqBody.getBody(), "?include=path", reqBody.getContentType(), 201);
        document = jacksonUtil.parseEntry(response.getJsonResponse(), Document.class);
        // Check the upload response
        assertEquals("quick-2.pdf", document.getName());
        assertNotNull(document.getPath());

        response = getAll(getNodeChildrenUrl(Nodes.PATH_MY), user1, paging, 200);
        pagingResult = parsePaging(response.getJsonResponse());
        assertNotNull(paging);
        assertEquals(numOfNodes + 3, pagingResult.getCount().intValue());

        // upload without specifying content type or without overriding filename - hence guess mimetype and use file's name
        final String fileName1 = "quick-1.txt";
        final File file1 = getResourceFile(fileName1);
        reqBody = MultiPartBuilder.create()
                .setFileData(new FileData(null, file1))
                .build();
        response = post(getNodeChildrenUrl(Nodes.PATH_MY), user1, reqBody.getBody(), null, reqBody.getContentType(), 201);
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
        response = post(getNodeChildrenUrl(Nodes.PATH_MY), user1, reqBody.getBody(), null, reqBody.getContentType(), 201);
        document = jacksonUtil.parseEntry(response.getJsonResponse(), Document.class);
        // Check the upload response
        assertEquals(fileName2b, document.getName());
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, document.getContent().getMimeType());


        // User2 tries to upload a new file into the user1's home folder.
        response = getSingle(NodesEntityResource.class, user1, Nodes.PATH_MY, null, 200);
        Folder user1Home = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        final File file3 = getResourceFile(fileName2);
        reqBody = MultiPartBuilder.create()
                    .setFileData(new FileData(fileName2, file3))
                    .build();
        post(getNodeChildrenUrl(user1Home.getId()), user2, reqBody.getBody(), null, reqBody.getContentType(), 403);

        response = getAll(getNodeChildrenUrl(Nodes.PATH_MY), user1, paging, 200);
        pagingResult = parsePaging(response.getJsonResponse());
        assertNotNull(paging);
        assertEquals("Access Denied. The file shouldn't have been uploaded.", numOfNodes + 5, pagingResult.getCount().intValue());

        // User1 tries to upload a file into a document rather than a folder!
        post(getNodeChildrenUrl(document.getId()), user1, reqBody.getBody(), null, reqBody.getContentType(), 400);

        // Try to upload a file without defining the required formData
        reqBody = MultiPartBuilder.create().setAutoRename(true).build();
        post(getNodeChildrenUrl(Nodes.PATH_MY), user1, reqBody.getBody(), null, reqBody.getContentType(), 400);

        // Test unsupported node type
        reqBody = MultiPartBuilder.create()
                    .setFileData(new FileData(fileName2, file2))
                    .setAutoRename(true)
                    .setNodeType("cm:link")
                    .build();
        post(getNodeChildrenUrl(user1Home.getId()), user1, reqBody.getBody(), null, reqBody.getContentType(), 400);

        // User1 uploads a new file
        reqBody = MultiPartBuilder.create()
                    .setFileData(new FileData(fileName2, file2))
                    .build();
        response = post(getNodeChildrenUrl(user1Home.getId()), user1, reqBody.getBody(), null, reqBody.getContentType(), 201);
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
            post(getNodeChildrenUrl(Nodes.PATH_MY), user1, reqBody.getBody(), null, reqBody.getContentType(), 413);
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
        final String fileName = "quick-1.txt";
        final File file = getResourceFile(fileName);

        String docLibNodeId = getSiteContainerNodeId(networkOne.getId(), userOneN1.getId(), userOneN1Site.getId(), "documentLibrary");

        String folderA = "folder" + System.currentTimeMillis() + "_A";
        String folderA_id = createFolder(userOneN1.getId(), docLibNodeId, folderA).getId();

        Paging paging = getPaging(0, Integer.MAX_VALUE);
        HttpResponse response = getAll(getNodeChildrenUrl(folderA_id), userOneN1.getId(), paging, 200);
        PublicApiClient.ExpectedPaging pagingResult = parsePaging(response.getJsonResponse());
        assertNotNull(paging);
        final int numOfNodes = pagingResult.getCount();

        MultiPartBuilder multiPartBuilder = MultiPartBuilder.create()
                    .setFileData(new FileData(fileName, file));
        MultiPartRequest reqBody = multiPartBuilder.build();
        // Try to upload
        response = post(getNodeChildrenUrl(folderA_id), userOneN1.getId(), reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        // Check the upload response
        assertEquals(fileName, document.getName());
        ContentInfo contentInfo = document.getContent();
        assertNotNull(contentInfo);
        // As the client didn't set the mimeType, the API must guess it.
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, contentInfo.getMimeType());

        // Retrieve the uploaded file
        response = getSingle(NodesEntityResource.class, userOneN1.getId(), document.getId(), null, 200);
        document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertEquals(fileName, document.getName());
        contentInfo = document.getContent();
        assertNotNull(contentInfo);
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, contentInfo.getMimeType());

        // Check 'get children' is confirming the upload
        response = getAll(getNodeChildrenUrl(folderA_id), userOneN1.getId(), paging, 200);
        pagingResult = parsePaging(response.getJsonResponse());
        assertNotNull(paging);
        assertEquals(numOfNodes + 1, pagingResult.getCount().intValue());

        // Upload the same file again to check the name conflicts handling
        post(getNodeChildrenUrl(folderA_id), userOneN1.getId(), reqBody.getBody(), null, reqBody.getContentType(), 409);

        response = getAll(getNodeChildrenUrl(folderA_id), userOneN1.getId(), paging, 200);
        pagingResult = parsePaging(response.getJsonResponse());
        assertNotNull(paging);
        assertEquals(numOfNodes + 1, pagingResult.getCount().intValue());

        final String fileName2 = "quick-2.txt";
        final File file2 = getResourceFile(fileName2);
        reqBody = MultiPartBuilder.create()
                    .setFileData(new FileData(fileName2, file2))
                    .build();
        // userTwoN1 tries to upload a new file into the folderA of userOneN1
        post(getNodeChildrenUrl(folderA_id), userTwoN1.getId(), reqBody.getBody(), null, reqBody.getContentType(), 403);

        // Test upload with properties
        response = post(getNodeChildrenUrl(folderA_id), userOneN1.getId(), reqBody.getBody(), null, reqBody.getContentType(), 201);
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

        response = post(getNodeChildrenUrl(folderA_id), userOneN1.getId(), reqBody.getBody(), null, reqBody.getContentType(), 201);
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
        post(getNodeChildrenUrl(folderA_id), userOneN1.getId(), reqBody.getBody(), null, reqBody.getContentType(), 400);

        // Test relativePath multi-part field.
        // Any folders in the relativePath that do not exist, are created before the content is created.
        multiPartBuilder = MultiPartBuilder.create()
                    .setFileData(new FileData(fileName, file))
                    .setRelativePath("X/Y/Z");
        reqBody = multiPartBuilder.build();

        response = post(getNodeChildrenUrl(folderA_id), userOneN1.getId(), reqBody.getBody(), "?include=path", reqBody.getContentType(), 201);
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
        post(getNodeChildrenUrl(folderA_id), userOneN1.getId(), reqBody.getBody(), null, reqBody.getContentType(), 409);

        // Test the same functionality as "mkdir -p x/y/z" which the folders should be created
        // as needed but no errors thrown if the path or any part of the path already exists.
        // NOTE: white spaces, leading and trailing "/" are ignored.
        reqBody = MultiPartBuilder.copy(multiPartBuilder)
                    .setRelativePath("/X/ Y/Z /CoolFolder/")
                    .build();
        response = post(getNodeChildrenUrl(folderA_id), userOneN1.getId(), reqBody.getBody(), null, reqBody.getContentType(), 201);
        document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        // Check the upload response
        assertEquals(fileName, document.getName());
        contentInfo = document.getContent();
        assertNotNull(contentInfo);
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, contentInfo.getMimeType());

        // Retrieve the uploaded file parent folder
        response = getSingle(NodesEntityResource.class, userOneN1.getId(), document.getParentId(), null, 200);
        Folder coolFolder = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertEquals(document.getParentId(), coolFolder.getId());
        assertEquals("CoolFolder", coolFolder.getName());

        // Try to upload quick-1.txt within coolFolder and set the relativePath to a blank string.
        reqBody = MultiPartBuilder.copy(multiPartBuilder)
                    .setRelativePath("  ")// blank
                    .build();
        // 409 -> as the blank string is ignored and quick-1.txt already exists in the coolFolder
        post(getNodeChildrenUrl(coolFolder.getId()), userOneN1.getId(), reqBody.getBody(), null, reqBody.getContentType(), 409);

        // userTwoN1 tries to upload the same file by creating sub-folders in the folderA of userOneN1
        reqBody = MultiPartBuilder.copy(multiPartBuilder)
                    .setRelativePath("userTwoFolder1/userTwoFolder2")
                    .build();
        post(getNodeChildrenUrl(folderA_id), userTwoN1.getId(), reqBody.getBody(), null, reqBody.getContentType(), 403);
    }

    /**
     * Tests delete (file or folder)
     * <p>DELETE:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>}
     */
    @Test
    public void testDelete() throws Exception
    {
        long runId = System.currentTimeMillis();

        String myNodeId = getMyNodeId(user1);

        String content1Id = createTextFile(user1, myNodeId, "content" + runId + "_1", "The quick brown fox jumps over the lazy dog.").getId();

        // delete file
        delete(URL_NODES, user1, content1Id, 204);

        assertTrue(existsArchiveNode(user1, content1Id));

        // -ve test
        delete(URL_NODES, user1, content1Id, 404);

        String folder1Id = createFolder(user1, myNodeId, "folder " + runId + "_1").getId();
        String folder2Id = createFolder(user1, folder1Id, "folder " + runId + "_2").getId();

        String content2Id = createTextFile(user1, folder2Id, "content" + runId + "_2", "The quick brown fox jumps over the lazy dog.").getId();

        // cascade delete folder
        delete(URL_NODES, user1, folder1Id, 204);

        assertTrue(existsArchiveNode(user1, folder1Id));
        assertTrue(existsArchiveNode(user1, folder2Id));
        assertTrue(existsArchiveNode(user1, content2Id));

        // -ve test
        delete(URL_NODES, user1, folder2Id, 404);
        delete(URL_NODES, user1, content2Id, 404);

        // -ve test
        String rootNodeId = getRootNodeId(user1);
        delete(URL_NODES, user1, rootNodeId, 403);

        //
        // permanently delete - ie. bypass trashcan (archive store)
        //

        String folder3Id = createFolder(user1, myNodeId, "folder " + runId + "_3").getId();
        String folder4Id = createFolder(user1, folder3Id, "folder " + runId + "_4").getId();

        Map<String, String> params = Collections.singletonMap("permanent", "true");
        delete(URL_NODES, user1, folder3Id, params, 204);

        assertFalse(existsArchiveNode(user1, folder3Id));
        assertFalse(existsArchiveNode(user1, folder4Id));

        String sharedNodeId = getSharedNodeId(user1);
        String folder5Id = createFolder(user1, sharedNodeId, "folder " + runId + "_5").getId();


        // -ve test - another user cannot delete
        delete(URL_NODES, user2, folder5Id, 403);


        Map<String, Object> props = new HashMap<>();
        props.put(PROP_OWNER, user2);
        Node nUpdate = new Node();
        nUpdate.setProperties(props);

        HttpResponse response = put(URL_NODES, user1, folder5Id, toJsonAsStringNonNull(nUpdate), null, 200);
        Node nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertEquals(user2, ((Map)nodeResp.getProperties().get(PROP_OWNER)).get("id"));

        // -ve test - user1 can no longer delete
        delete(URL_NODES, user1, folder5Id, 403);

        // TODO refactor with remote permission api calls (use v0 until we have v1 ?)
        permissionService.setPermission(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, folder5Id), user1, PermissionService.DELETE, true);

        // -ve test - non-owner cannot bypass trashcan
        params = Collections.singletonMap("permanent", "true");
        delete(URL_NODES, user1, folder5Id, params, 403);

        // user1 has permission to delete (via trashcan)
        delete(URL_NODES, user1, folder5Id, 204);

        // admin can permanently delete
        String folder6Id = createFolder(user1, sharedNodeId, "folder " + runId + "_6").getId();

        params = Collections.singletonMap("permanent", "true");

        // TODO improve - admin-related tests
        publicApiClient.setRequestContext(new RequestContext("-default-", "admin", "admin"));
        response = publicApiClient.delete(getScope(), 1, URL_NODES, folder6Id, null, null, params);
        checkStatus(204, response.getStatusCode());

        // -ve - cannot delete Company Home root node
        publicApiClient.setRequestContext(new RequestContext("-default-", "admin", "admin"));
        response = publicApiClient.delete(getScope(), 1, URL_NODES, rootNodeId, null, null, params);
        checkStatus(403, response.getStatusCode());

        params = new HashMap<>();
        params.put(Nodes.PARAM_RELATIVE_PATH, "/Sites");
        response = getSingle(NodesEntityResource.class, user1, rootNodeId, params, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        String sitesNodeId = nodeResp.getId();

        // -ve - cannot delete Sites node
        publicApiClient.setRequestContext(new RequestContext("-default-", "admin", "admin"));
        response = publicApiClient.delete(getScope(), 1, URL_NODES, sitesNodeId, null, null, params);
        checkStatus(403, response.getStatusCode());

        params = new HashMap<>();
        params.put(Nodes.PARAM_RELATIVE_PATH, "/Data Dictionary");
        response = getSingle(NodesEntityResource.class, user1, rootNodeId, params, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        String ddNodeId = nodeResp.getId();

        // -ve - cannot delete Data Dictionary node
        publicApiClient.setRequestContext(new RequestContext("-default-", "admin", "admin"));
        response = publicApiClient.delete(getScope(), 1, URL_NODES, ddNodeId, null, null, params);
        checkStatus(403, response.getStatusCode());
    }

    private boolean existsArchiveNode(String userId, String nodeId)
    {
        // TODO replace with calls to future V1 REST API for Trashcan
        try
        {
            AuthenticationUtil.setFullyAuthenticatedUser(userId);
            NodeRef originalNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId);
            NodeRef archiveNodeRef = nodeArchiveService.getArchivedNode(originalNodeRef);
            return nodeService.exists(archiveNodeRef);
        }
        finally
        {
            AuthenticationUtil.clearCurrentSecurityContext();
        }
    }

    /**
     * Tests move (file or folder)
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/move}
     */
    @Test
    public void testMove() throws Exception
    {
        // create folder f1
        Folder folderResp = createFolder(user1, Nodes.PATH_MY, "f1");
        String f1Id = folderResp.getId();

        // create folder f2
        folderResp = createFolder(user1, Nodes.PATH_MY, "f2");
        String f2Id = folderResp.getId();

        // create doc d1
        String d1Name = "content" + System.currentTimeMillis() + "_1";
        String d1Id = createTextFile(user1, f1Id, d1Name, "The quick brown fox jumps over the lazy dog 1.").getId();

        // create doc d2
        String d2Name = "content" + System.currentTimeMillis() + "_2";
        String d2Id = createTextFile(user1, f2Id, d2Name, "The quick brown fox jumps over the lazy dog 2.").getId();

        // move file (without rename)

        NodeTarget tgt = new NodeTarget();
        tgt.setTargetParentId(f2Id);

        HttpResponse response = post("nodes/"+d1Id+"/move", user1, toJsonAsStringNonNull(tgt), null, 200);
        Document documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        assertEquals(d1Name, documentResp.getName());
        assertEquals(f2Id, documentResp.getParentId());

        // move file (with rename)

        String d1NewName = d1Name+" updated !!";

        tgt = new NodeTarget();
        tgt.setName(d1NewName);
        tgt.setTargetParentId(f1Id);

        response = post("nodes/"+d1Id+"/move", user1, toJsonAsStringNonNull(tgt), null, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        assertEquals(d1NewName, documentResp.getName());
        assertEquals(f1Id, documentResp.getParentId());

        // -ve tests

        // missing target
        tgt = new NodeTarget();
        tgt.setName("new name");
        post("nodes/"+d1Id+"/move", user1, toJsonAsStringNonNull(tgt), null, 400);

        // name already exists
        tgt = new NodeTarget();
        tgt.setName(d2Name);
        tgt.setTargetParentId(f2Id);
        post("nodes/"+d1Id+"/move", user1, toJsonAsStringNonNull(tgt), null, 409);

        // unknown source nodeId
        tgt = new NodeTarget();
        tgt.setTargetParentId(f2Id);
        post("nodes/"+UUID.randomUUID().toString()+"/move", user1, toJsonAsStringNonNull(tgt), null, 404);

        // unknown target nodeId
        tgt = new NodeTarget();
        tgt.setTargetParentId(UUID.randomUUID().toString());
        post("nodes/"+d1Id+"/move", user1, toJsonAsStringNonNull(tgt), null, 404);

        // target is not a folder
        tgt = new NodeTarget();
        tgt.setTargetParentId(d2Id);
        post("nodes/"+d1Id+"/move", user1, toJsonAsStringNonNull(tgt), null, 400);

        String rootNodeId = getRootNodeId(user1);

        // create folder f3 (sub-folder of f2)
        folderResp = createFolder(user1, f2Id, "f3");
        String f3Id = folderResp.getId();

        // can't create cycle (move into own subtree)
        tgt = new NodeTarget();
        tgt.setTargetParentId(f3Id);
        post("nodes/"+f2Id+"/move", user1, toJsonAsStringNonNull(tgt), null, 400);

        // no (write/create) permissions to move to target
        tgt = new NodeTarget();
        tgt.setTargetParentId(rootNodeId);
        post("nodes/"+d1Id+"/move", user1, toJsonAsStringNonNull(tgt), null, 403);

        AuthenticationUtil.setFullyAuthenticatedUser(user2);

        String my2NodeId = getMyNodeId(user2);

        // no (write/delete) permissions to move source
        tgt = new NodeTarget();
        tgt.setTargetParentId(my2NodeId);
        post("nodes/"+f1Id+"/move", user2, toJsonAsStringNonNull(tgt), null, 403);

        // TODO improve - admin-related tests

        // -ve - cannot move (delete) Company Home root node
        publicApiClient.setRequestContext(new RequestContext("-default-", "admin", "admin"));
        response = publicApiClient.post(getScope(), "nodes/"+rootNodeId+"/move", null, null, null, toJsonAsStringNonNull(tgt));
        checkStatus(403, response.getStatusCode());

        Map params = new HashMap<>();
        params.put(Nodes.PARAM_RELATIVE_PATH, "/Sites");
        response = getSingle(NodesEntityResource.class, user1, rootNodeId, params, 200);
        Node nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        String sitesNodeId = nodeResp.getId();

        // -ve - cannot move (delete) Sites node
        publicApiClient.setRequestContext(new RequestContext("-default-", "admin", "admin"));
        response = publicApiClient.post(getScope(), "nodes/"+sitesNodeId+"/move", null, null, null, toJsonAsStringNonNull(tgt));
        checkStatus(403, response.getStatusCode());

        params = new HashMap<>();
        params.put(Nodes.PARAM_RELATIVE_PATH, "/Data Dictionary");
        response = getSingle(NodesEntityResource.class, user1, rootNodeId, params, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        String ddNodeId = nodeResp.getId();

        // -ve - cannot move (delete) Data Dictionary node
        publicApiClient.setRequestContext(new RequestContext("-default-", "admin", "admin"));
        response = publicApiClient.post(getScope(), "nodes/"+ddNodeId+"/move", null, null, null, toJsonAsStringNonNull(tgt));
        checkStatus(403, response.getStatusCode());
    }

    /**
     * Tests copy (file or folder)
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/copy}
     */
    @Test
    public void testCopy() throws Exception
    {
        // create folder
        Folder folderResp = createFolder(user1, Nodes.PATH_MY, "fsource");
        String sourceId = folderResp.getId();

        // create folder
        folderResp = createFolder(user1, Nodes.PATH_MY, "ftarget");
        String targetId = folderResp.getId();

        // create doc d1
        String d1Name = "content" + System.currentTimeMillis() + "_1";
        String d1Id = createTextFile(user1, sourceId, d1Name, "The quick brown fox jumps over the lazy dog 1.").getId();

        // create doc d2
        String d2Name = "content" + System.currentTimeMillis() + "_2";
        String d2Id = createTextFile(user1, sourceId, d2Name, "The quick brown fox jumps over the lazy dog 2.").getId();

        Map<String, String> body = new HashMap<>();
        body.put("targetParentId", targetId);

        HttpResponse response = post(user1, URL_NODES, d1Id, "copy", toJsonAsStringNonNull(body).getBytes(), null, null, 201);
        Document documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        assertEquals(d1Name, documentResp.getName());
        assertEquals(targetId, documentResp.getParentId());

        // copy file (with rename)
        String newD2Name = d2Name + " updated !!";

        body = new HashMap<>();
        body.put("targetParentId", targetId);
        body.put("name", newD2Name);

        response = post(user1, URL_NODES, d2Id, "copy", toJsonAsStringNonNull(body).getBytes(), null, null, 201);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        assertEquals(newD2Name, documentResp.getName());
        assertEquals(targetId, documentResp.getParentId());

        // -ve tests

        // missing target
        NodeTarget tgt = new NodeTarget();
        tgt.setName("new name");
        post("nodes/"+d1Id+"/copy", user1, toJsonAsStringNonNull(tgt), null, 400);

        // name already exists - different parent
        tgt = new NodeTarget();
        tgt.setName(newD2Name);
        tgt.setTargetParentId(targetId);
        post("nodes/"+d1Id+"/copy", user1, toJsonAsStringNonNull(tgt), null, 409);

        // name already exists - same parent
        tgt = new NodeTarget();
        tgt.setTargetParentId(sourceId);
        post("nodes/"+d1Id+"/copy", user1, toJsonAsStringNonNull(tgt), null, 409);

        // unknown source nodeId
        tgt = new NodeTarget();
        tgt.setTargetParentId(targetId);
        post("nodes/"+UUID.randomUUID().toString()+"/copy", user1, toJsonAsStringNonNull(tgt), null, 404);

        // unknown target nodeId
        tgt = new NodeTarget();
        tgt.setTargetParentId(UUID.randomUUID().toString());
        post("nodes/"+d1Id+"/copy", user1, toJsonAsStringNonNull(tgt), null, 404);

        // target is not a folder
        tgt = new NodeTarget();
        tgt.setTargetParentId(d2Id);
        post("nodes/"+d1Id+"/copy", user1, toJsonAsStringNonNull(tgt), null, 400);

        String rootNodeId = getRootNodeId(user1);

        // no (write/create) permissions to copy to target
        tgt = new NodeTarget();
        tgt.setTargetParentId(rootNodeId);
        post("nodes/"+d1Id+"/copy", user1, toJsonAsStringNonNull(tgt), null, 403);
    }

    @Test
    public void testCopySite() throws Exception
    {
        // create folder
        Folder folderResp = createFolder(userOneN1.getId(), Nodes.PATH_MY, "siteCopytarget");
        String targetId = folderResp.getId();

        Map<String, String> body = new HashMap<>();
        body.put("targetParentId", targetId);

        //test that you can't copy a site
        post("nodes/"+userOneN1Site.getGuid()+"/copy", userOneN1.getId(), toJsonAsStringNonNull(body), null, 422);

        String docLibNodeId = getSiteContainerNodeId(networkOne.getId(), userOneN1.getId(), userOneN1Site.getId(), "documentLibrary");

        //test that you can't copy a site doclib
        post("nodes/"+docLibNodeId+"/copy", userOneN1.getId(), toJsonAsStringNonNull(body), null, 422);

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
        /*
         * Precondition - create two sites, invite users, create folders
         */
        AuthenticationUtil.setFullyAuthenticatedUser(userOneN1.getId());
        // userOneN1 creates a public site and adds userTwoN1 as a site collaborator
        TestSite user1Site = createSite(userOneN1.getDefaultAccount(), userOneN1, SiteVisibility.PUBLIC);
        inviteToSite(user1Site, userTwoN1, SiteRole.SiteCollaborator);

        // Get user1Site's docLib node id
        final String user1SiteDocLibNodeId = getSiteDocLib(user1Site).getId();

        // userOneN1 creates a folder in the docLib of his site (user1Site)
        String user1Folder = "folder" + System.currentTimeMillis() + "_user1";
        String user1FolderNodeId = createFolder(userOneN1.getId(), user1SiteDocLibNodeId, user1Folder, null).getId();

        AuthenticationUtil.setFullyAuthenticatedUser(userTwoN1.getId());
        // userTwoN1 creates a public site and adds userOneN1 as a site collaborator
        TestSite user2Site = createSite(userTwoN1.getDefaultAccount(), userTwoN1, SiteVisibility.PUBLIC);
        inviteToSite(user2Site, userOneN1, SiteRole.SiteCollaborator);

        // Get user2Site's docLib node id
        final String user2SiteDocLibNodeId = getSiteDocLib(user2Site).getId();

        // userTwoN1 creates 2 folders within the docLib of the user1Site
        String user2Folder1 = "folder1" + System.currentTimeMillis() + "_user2";
        String user2FolderNodeId = createFolder(userTwoN1.getId(), user1SiteDocLibNodeId, user2Folder1, null).getId();

        String user2Folder2 = "folder2" + System.currentTimeMillis() + "_user2";
        String user2Folder2NodeId = createFolder(userTwoN1.getId(), user1SiteDocLibNodeId, user2Folder2, null).getId();

        /*
         * Test move between sites
         */
        // userOneN1 moves the folder created by userTwoN1 to the user2Site's docLib
        AuthenticationUtil.setFullyAuthenticatedUser(userOneN1.getId());
        NodeTarget target = new NodeTarget();
        target.setTargetParentId(user2SiteDocLibNodeId);
        HttpResponse response = post("nodes/" + user2FolderNodeId + "/move", userOneN1.getId(), toJsonAsStringNonNull(target), null, 200);
        Folder moveFolderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertEquals(user2SiteDocLibNodeId, moveFolderResp.getParentId());

        // userOneN1 tries to undo the move (moves back the folder to its original place)
        // as userOneN1 is just a SiteCollaborator in the user2Site, he can't move the folder which he doesn't own - ACL access permission.
        target = new NodeTarget();
        target.setTargetParentId(user1SiteDocLibNodeId);
        post("nodes/" + user2FolderNodeId + "/move", userOneN1.getId(), toJsonAsStringNonNull(target), null, 403);

        // userOneN1 moves the folder created by himself to the docLib of the user2Site
        target = new NodeTarget();
        target.setTargetParentId(user2SiteDocLibNodeId);
        response = post("nodes/" + user1FolderNodeId + "/move", userOneN1.getId(), toJsonAsStringNonNull(target), null, 200);
        moveFolderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertEquals(user2SiteDocLibNodeId, moveFolderResp.getParentId());

        // userOneN1 tries to undo the move (moves back the folder to its original place)
        // The undo should be successful as userOneN1 owns the folder
        target = new NodeTarget();
        target.setTargetParentId(user1SiteDocLibNodeId);
        response = post("nodes/" + user1FolderNodeId + "/move", userOneN1.getId(), toJsonAsStringNonNull(target), null, 200);
        moveFolderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertEquals(user1SiteDocLibNodeId, moveFolderResp.getParentId());


        /*
         * Test copy between sites
         */
        // userOneN1 copies the folder created by userTwoN1 to the user2Site's docLib
        target = new NodeTarget();
        target.setTargetParentId(user2SiteDocLibNodeId);
        response = post("nodes/" + user2Folder2NodeId + "/copy", userOneN1.getId(), toJsonAsStringNonNull(target), null, 201);
        Folder copyFolderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertEquals(user2SiteDocLibNodeId, copyFolderResp.getParentId());

        // userOneN1 tries to undo the copy (hard deletes the created copy)
        Map<String, String> params = Collections.singletonMap("permanent", "true");
        delete("nodes", userOneN1.getId(), copyFolderResp.getId(), params, 204);
        // Check it's deleted
        getSingle("nodes", userOneN1.getId(), copyFolderResp.getId(), 404);
    }

    /**
     * Tests create folder.
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/children}
     */
    @Test
    public void testCreateFolder() throws Exception
    {
        String myNodeId = getMyNodeId(user1);

        UserInfo expectedUser = new UserInfo(user1, user1+" "+user1);

        String postUrl = getNodeChildrenUrl(myNodeId);

        // create folder
        Folder folderResp = createFolder(user1, myNodeId, "f1");
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

        folderResp = createFolder(user1, f1Id, "f2", props);
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
        HttpResponse response = post(getNodeChildrenUrl(myNodeId), user1, RestApiUtil.toJsonAsStringNonNull(n), 201);
        folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);

        // check parent hierarchy ...
        response = getSingle(NodesEntityResource.class, user1, folderResp.getId(), null, 200);
        folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertEquals(folderResp.getName(),"fZ");

        response = getSingle(NodesEntityResource.class, user1, folderResp.getParentId(), null, 200);
        folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertEquals(folderResp.getName(),"f4");

        response = getSingle(NodesEntityResource.class, user1, folderResp.getParentId(), null, 200);
        folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertEquals(folderResp.getName(),"f3");

        response = getSingle(NodesEntityResource.class, user1, folderResp.getParentId(), null, 200);
        folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertEquals(folderResp.getName(),"f2");
        assertEquals(folderResp.getId(), f2Id);

        // -ve test - name is mandatory
        Folder invalid = new Folder();
        invalid.setNodeType(TYPE_CM_FOLDER);
        post(postUrl, user1, toJsonAsStringNonNull(invalid), 400);

        // -ve test - invalid name
        invalid = new Folder();
        invalid.setName("inv:alid");
        invalid.setNodeType(TYPE_CM_FOLDER);
        post(postUrl, user1, toJsonAsStringNonNull(invalid), 422);

        // -ve test - node type is mandatory
        invalid = new Folder();
        invalid.setName("my folder");
        post(postUrl, user1, toJsonAsStringNonNull(invalid), 400);

        // create empty file - used in -ve test below
        Document d1 = new Document();
        d1.setName("d1.txt");
        d1.setNodeType(TYPE_CM_CONTENT);

        response = post(postUrl, user1, toJsonAsStringNonNull(d1), 201);
        Document documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        String d1Id = documentResp.getId();

        // -ve test - invalid (model integrity exception)
        Folder f3 = new Folder();
        f3.setName("f3");
        f3.setNodeType(TYPE_CM_FOLDER);
        post(getNodeChildrenUrl(d1Id), user1, toJsonAsStringNonNull(f3), 422);

        // -ve test - it should not be possible to create a "system folder"
        invalid = new Folder();
        invalid.setName("my sys folder");
        invalid.setNodeType("cm:systemfolder");
        post(postUrl, user1, toJsonAsStringNonNull(invalid), 400);

        // -ve test - unknown parent folder node id
        post(getNodeChildrenUrl(UUID.randomUUID().toString()), user1, toJsonAsStringNonNull(f3), 404);

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

        // -ve test - invalid relative path
        n = new Node();
        n.setName("fX");
        n.setNodeType(TYPE_CM_FOLDER);
        n.setRelativePath("/f1/inv:alid");
        post(getNodeChildrenUrl(f2Id), user1, RestApiUtil.toJsonAsStringNonNull(n), 422);

        // -ve test - invalid relative path - points to existing node that is not a folder
        n = new Node();
        n.setName("fY");
        n.setNodeType(TYPE_CM_FOLDER);
        n.setRelativePath("d1.txt");
        post(getNodeChildrenUrl(myNodeId), user1, RestApiUtil.toJsonAsStringNonNull(n), 409);

        // -ve test - minor: error code if trying to create with property with invalid format (REPO-473)
        props = new HashMap<>();
        props.put("exif:pixelYDimension", "my unknown property");
        n = new Folder();
        n.setName("fZ");
        n.setNodeType(TYPE_CM_FOLDER);
        n.setProperties(props);
        post(getNodeChildrenUrl(myNodeId), user1, RestApiUtil.toJsonAsStringNonNull(n), 400);
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
        String myNodeId = getMyNodeId(user1);

        String fId = null;

        try
        {
            fId = createFolder(user1, myNodeId, "testChildrenAssocType folder").getId();

            Node nodeUpdate = new Node();
            nodeUpdate.setAspectNames(Collections.singletonList(ASPECT_CM_PREFERENCES));

            put(URL_NODES, user1, fId, toJsonAsStringNonNull(nodeUpdate), null, 200);

            HttpResponse response = getAll(getNodeChildrenUrl(fId), user1, null, null, 200);
            List<Node> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(0, nodes.size());

            Node obj = new Node();
            obj.setName("c1");
            obj.setNodeType(TYPE_CM_CONTENT);

            // assoc type => cm:contains
            response = post(getNodeChildrenUrl(fId), user1, toJsonAsStringNonNull(obj), 201);
            Node nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
            String c1Id = nodeResp.getId();
            assertEquals(fId, nodeResp.getParentId());

            obj = new Node();
            obj.setName("c2");
            obj.setNodeType(TYPE_CM_CONTENT);
            Association assoc = new Association();
            assoc.setAssocType(ASSOC_TYPE_CM_PREFERENCE_IMAGE);
            obj.setAssociation(assoc);

            // assoc type => cm:preferenceImage
            response = post(getNodeChildrenUrl(fId), user1, toJsonAsStringNonNull(obj), 201);
            nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
            String c2Id = nodeResp.getId();
            assertEquals(fId, nodeResp.getParentId());

            response = getAll(getNodeChildrenUrl(fId), user1, null, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(2, nodes.size());

            Map<String, String> params = new HashMap<>();
            params.put("where", "(assocType='"+ASSOC_TYPE_CM_CONTAINS+"')");
            params.put("include", "association");
            response = getAll(getNodeChildrenUrl(fId), user1, null, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(1, nodes.size());
            assertEquals(c1Id, nodes.get(0).getId());
            assertTrue(nodes.get(0).getAssociation().getIsPrimary());

            params = new HashMap<>();
            params.put("where", "(assocType='"+ASSOC_TYPE_CM_PREFERENCE_IMAGE+"')");
            params.put("include", "association");
            response = getAll(getNodeChildrenUrl(fId), user1, null, params, 200);
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
            response = post(getNodeChildrenUrl(fId), user1, toJsonAsStringNonNull(obj), 201);
            nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
            String c3Id = nodeResp.getId();

            obj = new Node();
            obj.setName("c4");
            obj.setNodeType(TYPE_CM_CONTENT);
            assoc = new Association();
            assoc.setAssocType(ASSOC_TYPE_CM_PREFERENCE_IMAGE);
            obj.setAssociation(assoc);

            // assoc type => cm:preferenceImage
            response = post(getNodeChildrenUrl(c3Id), user1, toJsonAsStringNonNull(obj), 201);
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
            post(getNodeChildrenUrl(c3Id), user1, toJsonAsStringNonNull(obj), 422);
        }
        finally
        {
            // some cleanup
            if (fId != null)
            {
                delete(URL_NODES, user1, fId, 204);
            }
        }
    }

    // TODO test custom types with properties (sub-type of cm:cmobject)

    @Test
    public void testListChildrenIsFileIsFolderFilter() throws Exception
    {
        String myNodeId = getMyNodeId(user1);
        String myChildrenUrl = getNodeChildrenUrl(myNodeId);

        long timeNow = System.currentTimeMillis();

        int folderCnt = 2;
        int fileCnt = 3;
        int objCnt = 4;

        // create some folders
        List<String> folderIds = new ArrayList<>(folderCnt);

        for (int i = 1; i <= folderCnt; i++)
        {
            folderIds.add(createFolder(user1, myNodeId, "folder "+i+" "+timeNow).getId());
        }

        // create some files
        List<String> fileIds = new ArrayList<>(fileCnt);
        for (int i = 1; i <= fileCnt; i++)
        {
            fileIds.add(createTextFile(user1, myNodeId, "file "+i+" "+timeNow, "The quick brown fox jumps over the lazy dog "+i).getId());
        }

        // create some nodes (cmobject)
        List<String> objIds = new ArrayList<>(objCnt);
        for (int i = 1; i <= objCnt; i++)
        {
            Node obj = new Node();
            obj.setName("obj "+i+" "+timeNow);
            obj.setNodeType(TYPE_CM_OBJECT);

            // create node/object
            HttpResponse response = post(myChildrenUrl, user1, toJsonAsStringNonNull(obj), 201);
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

        HttpResponse response = getAll(myChildrenUrl, user1, paging, null, 200);
        List<Node> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        checkNodeIds(nodes, allIds);

        // filtering, via where clause - folders

        Map<String, String> params = new HashMap<>();
        params.put("where", "(nodeType='"+TYPE_CM_FOLDER+"')");

        response = getAll(myChildrenUrl, user1, paging, params, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        checkNodeIds(nodes, folderIds);

        params = new HashMap<>();
        params.put("where", "(isFolder=true)");

        response = getAll(myChildrenUrl, user1, paging, params, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        checkNodeIds(nodes, folderIds);

        params = new HashMap<>();
        params.put("where", "(isFolder=true AND isFile=false)");

        response = getAll(myChildrenUrl, user1, paging, params, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        checkNodeIds(nodes, folderIds);

        // filtering, via where clause - files

        params = new HashMap<>();
        params.put("where", "(nodeType='"+TYPE_CM_CONTENT+"')");

        response = getAll(myChildrenUrl, user1, paging, params, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        checkNodeIds(nodes, fileIds);

        params = new HashMap<>();
        params.put("where", "(isFile=true)");

        response = getAll(myChildrenUrl, user1, paging, params, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        checkNodeIds(nodes, fileIds);

        params = new HashMap<>();
        params.put("where", "(isFile=true AND isFolder=false)");

        response = getAll(myChildrenUrl, user1, paging, params, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        checkNodeIds(nodes, fileIds);

        // filtering, via where clause - non-folders / non-files

        params = new HashMap<>();
        params.put("where", "(nodeType='"+TYPE_CM_OBJECT+"')");

        response = getAll(myChildrenUrl, user1, paging, params, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        checkNodeIds(nodes, objIds);

        params = new HashMap<>();
        params.put("where", "(nodeType='cm:cmobject INCLUDESUBTYPES')");

        response = getAll(myChildrenUrl, user1, paging, params, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        checkNodeIds(nodes, allIds);

        params = new HashMap<>();
        params.put("where", "(isFile=false AND isFolder=false)");

        response = getAll(myChildrenUrl, user1, paging, params, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        checkNodeIds(nodes, objIds);

        // filtering, via where clause - not files
        params = new HashMap<>();
        params.put("where", "(isFile=false)");

        response = getAll(myChildrenUrl, user1, paging, params, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        checkNodeIds(nodes, notFileIds);

        // filtering, via where clause - not folders
        params = new HashMap<>();
        params.put("where", "(isFolder=false)");

        response = getAll(myChildrenUrl, user1, paging, params, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        checkNodeIds(nodes, notFolderIds);

        // -ve - node cannot be both a file and a folder
        params = new HashMap<>();
        params.put("where", "(isFile=true AND isFolder=true)");
        getAll(myChildrenUrl, user1, paging, params, 400);

        // -ve - nodeType and isFile/isFolder are mutually exclusive
        params = new HashMap<>();
        params.put("where", "(nodeType='cm:object' AND isFolder=true)");
        getAll(myChildrenUrl, user1, paging, params, 400);

        params = new HashMap<>();
        params.put("where", "(nodeType='cm:object' AND isFile=true)");
        getAll(myChildrenUrl, user1, paging, params, 400);
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
        AuthenticationUtil.setFullyAuthenticatedUser(user1);

        String myNodeId = getMyNodeId(user1);
        UserInfo expectedUser = new UserInfo(user1, user1+" "+user1);
        String myChildrenUrl = getNodeChildrenUrl(myNodeId);

        long timeNow = System.currentTimeMillis();

        // create folder f1
        Folder folderResp = createFolder(user1, myNodeId, "f1 "+timeNow);
        String f1Id = folderResp.getId();

        // create empty file d1 in f1
        Document d1 = new Document();
        d1.setName("d1.txt");
        d1.setNodeType(TYPE_CM_CONTENT);

        HttpResponse response = post(getNodeChildrenUrl(f1Id), user1, toJsonAsStringNonNull(d1), 201);
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
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);

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
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);

        n2.expected(nodeResp);


        // update node - rename

        String updatedName = "f1 link renamed";

        Node nUpdate = new Node();
        nUpdate.setName(updatedName);

        response = put(URL_NODES, user1, n1Id, toJsonAsStringNonNull(nUpdate), null, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        n1.setName(updatedName);
        n1.expected(nodeResp);


        // filtering, via where clause (nodeType + optionally including sub-types)

        List<String> linkIds = Arrays.asList(n1Id, n2Id);

        Map<String, String> params = new HashMap<>();
        params.put("where", "(nodeType='cm:link')");

        Paging paging = getPaging(0, Integer.MAX_VALUE);

        response = getAll(getNodeChildrenUrl(f2Id), user1, paging, params, 200);
        List<Node> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        assertEquals(0, nodes.size());

        // filter by including sub-types - note: includesubtypes is case-insensitive

        params = new HashMap<>();
        params.put("where", "(nodeType='cm:link INCLUDESUBTYPES')");

        paging = getPaging(0, Integer.MAX_VALUE);

        response = getAll(getNodeChildrenUrl(f2Id), user1, paging, params, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        assertEquals(linkIds.size(), nodes.size());
        assertTrue(linkIds.contains(nodes.get(0).getId()));
        assertTrue(linkIds.contains(nodes.get(1).getId()));

        params = new HashMap<>();
        params.put("where", "(nodeType='cm:link includeSubTypes')");

        paging = getPaging(0, Integer.MAX_VALUE);

        response = getAll(getNodeChildrenUrl(f2Id), user1, paging, params, 200);
        nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        assertEquals(linkIds.size(), nodes.size());
        assertTrue(linkIds.contains(nodes.get(0).getId()));
        assertTrue(linkIds.contains(nodes.get(1).getId()));


        // delete link
        delete(URL_NODES, user1, n1Id, 204);

        // -ve test - delete - cannot delete nonexistent link
        delete(URL_NODES, user1, n1Id, 404);

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
        post(getNodeChildrenUrl(f2Id), user1, toJsonAsStringNonNull(n2), 409);

        // -ve test - unknown nodeType when filtering
        params = new HashMap<>();
        params.put("where", "(nodeType='my:unknown'");
        getAll(getNodeChildrenUrl(f2Id), user1, paging, params, 400);

        // -ver test - invalid node type localname format and suffix is not ' includesubtypes'
        params = new HashMap<>();
        params.put("where", "(nodeType='cm:link ')");
        getAll(getNodeChildrenUrl(f2Id), user1, paging, params, 400);

        params = new HashMap<>();
        params.put("where", "(nodeType='cm:link blah')");
        getAll(getNodeChildrenUrl(f2Id), user1, paging, params, 400);
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

        String postUrl = getNodeChildrenUrl(myNodeId);

        Document d1 = new Document();
        d1.setName("d1.txt");
        d1.setNodeType(TYPE_CM_CONTENT);

        // create empty file
        HttpResponse response = post(postUrl, user1, toJsonAsStringNonNull(d1), 201);
        Document documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        String d1Id = documentResp.getId();

        d1.setIsFolder(false);
        d1.setParentId(myNodeId);
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

        response = post(postUrl, user1, toJsonAsStringNonNull(d2), 201);

        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        d2.setIsFolder(false);
        d2.setParentId(myNodeId);
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
        response = post(getNodeChildrenUrl(myNodeId), user1, RestApiUtil.toJsonAsStringNonNull(n), 201);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        // check parent hierarchy ...
        response = getSingle(NodesEntityResource.class, user1, documentResp.getId(), null, 200);
        Folder folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertEquals(folderResp.getName(),"d3.txt");

        response = getSingle(NodesEntityResource.class, user1, folderResp.getParentId(), null, 200);
        folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertEquals(folderResp.getName(),"f2");

        response = getSingle(NodesEntityResource.class, user1, folderResp.getParentId(), null, 200);
        folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertEquals(folderResp.getName(),"f1");

        response = getSingle(NodesEntityResource.class, user1, folderResp.getParentId(), null, 200);
        folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertEquals(myNodeId, folderResp.getId());

        // -ve test - name is mandatory
        Document invalid = new Document();
        invalid.setNodeType(TYPE_CM_CONTENT);
        post(postUrl, user1, toJsonAsStringNonNull(invalid), 400);

        // -ve test - node type is mandatory
        invalid = new Document();
        invalid.setName("my file.txt");
        post(postUrl, user1, toJsonAsStringNonNull(invalid), 400);

        // -ve test - invalid (model integrity exception)
        Document d3 = new Document();
        d3.setName("d3.txt");
        d3.setNodeType(TYPE_CM_CONTENT);
        post(getNodeChildrenUrl(d1Id), user1, toJsonAsStringNonNull(d3), 422);

        // -ve test - unknown parent folder node id
        post(getNodeChildrenUrl(UUID.randomUUID().toString()), user1, toJsonAsStringNonNull(d3), 404);

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

        String postUrl = getNodeChildrenUrl(myNodeId);

        String folderName = "My Folder";

        // create folder
        Folder folderResp = createFolder(user1, myNodeId, folderName);

        String fId = folderResp.getId();

        Folder f1 = new Folder();
        f1.setName(folderName);
        f1.setNodeType(TYPE_CM_FOLDER);

        f1.setIsFolder(true);
        f1.setParentId(myNodeId);
        f1.setAspectNames(Collections.singletonList("cm:auditable"));

        f1.setCreatedByUser(expectedUser);
        f1.setModifiedByUser(expectedUser);

        f1.expected(folderResp);

        // create empty file

        Document d1 = new Document();
        d1.setName("d1.txt");
        d1.setNodeType(TYPE_CM_CONTENT);

        HttpResponse response = post(postUrl, user1, toJsonAsStringNonNull(d1), 201);
        Document documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        String dId = documentResp.getId();

        d1.setIsFolder(false);
        d1.setParentId(myNodeId);
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

        response = put(URL_NODES, user1, dId, toJsonAsStringNonNull(dUpdate), null, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        d1.setName("d1b.txt");
        d1.expected(documentResp);

        // update file - add some properties

        Map<String,Object> props = new HashMap<>();
        props.put("cm:title","my file title");
        props.put("cm:description","my file description");

        dUpdate = new Document();
        dUpdate.setProperties(props);

        response = put(URL_NODES, user1, dId, toJsonAsStringNonNull(dUpdate), null, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        d1.setProperties(props);
        d1.setAspectNames(Arrays.asList("cm:auditable","cm:titled"));
        d1.expected(documentResp);

        // update file - add versionable aspect

        dUpdate = new Document();
        dUpdate.setAspectNames(Arrays.asList("cm:auditable","cm:titled","cm:versionable"));

        response = put(URL_NODES, user1, dId, toJsonAsStringNonNull(dUpdate), null, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        //d1.getProperties().put("cm:versionLabel","1.0"); // TODO ... fix api ?!
        d1.setAspectNames(Arrays.asList("cm:auditable","cm:titled","cm:versionable"));
        d1.expected(documentResp);

        response = getSingle(URL_NODES, user1, dId, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        d1.getProperties().put("cm:versionLabel", "1.0");
        d1.getProperties().put("cm:versionType", "MAJOR");
        d1.expected(documentResp);

        // update file - remove titled aspect (and it's related aspect properties)

        dUpdate = new Document();
        dUpdate.setAspectNames(Arrays.asList("cm:auditable","cm:versionable"));

        response = put(URL_NODES, user1, dId, toJsonAsStringNonNull(dUpdate), null, 200);
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

        response = put(URL_NODES, user1, fId, toJsonAsStringNonNull(fUpdate), null, 200);
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

        response = put(URL_NODES, user1, fId, toJsonAsStringNonNull(fUpdate), null, 200);
        folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);

        f1.getProperties().remove("cm:title");
        f1.expected(folderResp);

        // update folder - specialise node type

        fUpdate = new Folder();
        fUpdate.setNodeType("app:glossary");

        response = put(URL_NODES, user1, fId, toJsonAsStringNonNull(fUpdate), null, 200);
        folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);

        f1.setNodeType("app:glossary");
        f1.expected(folderResp);

        // -ve test - fail on unknown property
        props = new HashMap<>();
        props.put("cm:xyz","my unknown property");
        dUpdate = new Document();
        dUpdate.setProperties(props);
        put(URL_NODES, user1, dId, toJsonAsStringNonNull(dUpdate), null, 400);

        // -ve test - fail on unknown aspect
        List<String> aspects = new ArrayList<>(d1.getAspectNames());
        aspects.add("cm:unknownAspect");
        dUpdate = new Document();
        dUpdate.setAspectNames(aspects);
        put(URL_NODES, user1, dId, toJsonAsStringNonNull(dUpdate), null, 400);

        // -ve test - duplicate name
        dUpdate = new Document();
        dUpdate.setName(folderName);
        put(URL_NODES, user1, dId, toJsonAsStringNonNull(dUpdate), null, 409);

        // -ve test - unknown node id
        dUpdate = new Document();
        dUpdate.setName("some.txt");
        put(URL_NODES, user1, UUID.randomUUID().toString(), toJsonAsStringNonNull(dUpdate), null, 404);

        // -ve test - generalise node type
        fUpdate = new Folder();
        fUpdate.setNodeType(TYPE_CM_FOLDER);
        put(URL_NODES, user1, fId, toJsonAsStringNonNull(fUpdate), null, 400);

        // -ve test - try to move to a different parent using PUT (note: should use new POST /nodes/{nodeId}/move operation instead)

        folderResp = createFolder(user1, myNodeId, "folder 2");
        String f2Id = folderResp.getId();

        fUpdate = new Folder();
        fUpdate.setParentId(f2Id);
        put(URL_NODES, user1, fId, toJsonAsStringNonNull(fUpdate), null, 400);

        // ok - if parent does not change
        fUpdate = new Folder();
        fUpdate.setParentId(myNodeId);
        put(URL_NODES, user1, fId, toJsonAsStringNonNull(fUpdate), null, 200);

        // -ve test - minor: error code if trying to update property with invalid format (REPO-473)
        props = new HashMap<>();
        props.put("exif:pixelYDimension", "my unknown property");
        fUpdate = new Folder();
        fUpdate.setProperties(props);
        put(URL_NODES, user1, f2Id, toJsonAsStringNonNull(fUpdate), null, 400);
    }

    /**
     * Tests update owner (file or folder)
     * <p>PUT:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>}
     */
    @Test
    public void testUpdateOwner() throws Exception
    {
        // create folder f1
        String folderName = "f1 "+System.currentTimeMillis();
        Folder folderResp = createFolder(user1, Nodes.PATH_SHARED, folderName);
        String f1Id = folderResp.getId();

        assertNull(user1, folderResp.getProperties()); // owner is implied

        // explicitly set owner to oneself
        Map<String, Object> props = new HashMap<>();
        props.put(PROP_OWNER, user1);
        Folder fUpdate = new Folder();
        fUpdate.setProperties(props);

        HttpResponse response = put(URL_NODES, user1, f1Id, toJsonAsStringNonNull(fUpdate), null, 200);
        folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);

        assertEquals(user1, ((Map)folderResp.getProperties().get(PROP_OWNER)).get("id"));

        // create doc d1
        String d1Name = "content1 " + System.currentTimeMillis();
        String d1Id = createTextFile(user1, f1Id, d1Name, "The quick brown fox jumps over the lazy dog.").getId();

        // get node info
        response = getSingle(NodesEntityResource.class, user1, d1Id, null, 200);
        Document documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        // note: owner is implied
        assertEquals(2, documentResp.getProperties().size());
        assertEquals("1.0", documentResp.getProperties().get("cm:versionLabel"));
        assertEquals("MAJOR", documentResp.getProperties().get("cm:versionType"));

        props = new HashMap<>();
        props.put(PROP_OWNER, user1);
        Document dUpdate = new Document();
        dUpdate.setProperties(props);

        response = put(URL_NODES, user1, d1Id, toJsonAsStringNonNull(dUpdate), null, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        assertEquals(user1, ((Map)documentResp.getProperties().get(PROP_OWNER)).get("id"));

        // -ve test - cannot set owner to a nonexistent user

        props = new HashMap<>();
        props.put(PROP_OWNER, "unknownusernamedoesnotexist");
        dUpdate = new Document();
        dUpdate.setProperties(props);

        put(URL_NODES, user1, d1Id, toJsonAsStringNonNull(dUpdate), null, 400);

        AuthenticationUtil.setFullyAuthenticatedUser(user2);

        response = getSingle(URL_NODES, user1, d1Id, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        assertEquals(user1, ((Map)documentResp.getProperties().get(PROP_OWNER)).get("id"));

        // -ve test - cannot take/change ownership

        props = new HashMap<>();
        props.put(PROP_OWNER, user2);
        dUpdate = new Document();
        dUpdate.setProperties(props);

        put(URL_NODES, user2, d1Id, toJsonAsStringNonNull(dUpdate), null, 403);

        props = new HashMap<>();
        props.put(PROP_OWNER, user1);
        dUpdate = new Document();
        dUpdate.setProperties(props);

        put(URL_NODES, user2, d1Id, toJsonAsStringNonNull(dUpdate), null, 403);

        AuthenticationUtil.setFullyAuthenticatedUser(user1);

        props = new HashMap<>();
        props.put(PROP_OWNER, user2);
        dUpdate = new Document();
        dUpdate.setProperties(props);

        response = put(URL_NODES, user1, d1Id, toJsonAsStringNonNull(dUpdate), null, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        assertEquals(user2, ((Map)documentResp.getProperties().get(PROP_OWNER)).get("id"));

        AuthenticationUtil.setFullyAuthenticatedUser(user2);

        response = getSingle(URL_NODES, user2, d1Id, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        assertEquals(user2, ((Map)documentResp.getProperties().get(PROP_OWNER)).get("id"));

        // -ve test - user2 cannot delete the test folder/file - TODO is that expected ?
        delete(URL_NODES, user2, f1Id, 403);

        AuthenticationUtil.setFullyAuthenticatedUser(user1);

        delete(URL_NODES, user1, f1Id, 204);
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

        Folder f1 = new Folder();
        f1.setName("F1");
        f1.setNodeType(TYPE_CM_FOLDER);

        HttpResponse response = post(getNodeChildrenUrl(myNodeId), user1, toJsonAsStringNonNull(f1), 201);
        Folder folderResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertEquals(f1.getName(), folderResp.getName());
        final String f1_nodeId = folderResp.getId();
        assertNotNull(f1_nodeId);

        Document doc = new Document();
        final String docName = "testdoc.txt";
        doc.setName(docName);
        doc.setNodeType(TYPE_CM_CONTENT);
        doc.setProperties(Collections.singletonMap("cm:title", (Object)"test title"));
        ContentInfo contentInfo = new ContentInfo();
        doc.setContent(contentInfo);

        // create an empty file within F1 folder
        response = post(getNodeChildrenUrl(f1_nodeId), user1, toJsonAsStringNonNull(doc), 201);
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
        putBinary(getNodeContentUrl(f1_nodeId), user1, payload, null, null, 400);

        // Try to update a non-existent file
        putBinary(getNodeContentUrl(UUID.randomUUID().toString()), user1, payload, null, null, 404);

        final String url = getNodeContentUrl(docResp.getId());

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

        response = putBinary(url + "?include=path", user1, payload, null, null, 200);
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
        assertEquals(f1.getName(), pathElements.get(pathElements.size() - 1).getName());

        // Download the file
        response = getSingle(url, user1, null, 200);
        assertNotNull(content, response.getResponse());

        // -ve - try to  update content using multi-part form data
        payload = new BinaryPayload(txtFile, "multipart/form-data", null);
        putBinary(url, user1, payload, null, null, 415);

        // -ve - try to invalid media type argument (when parsing request)
        payload = new BinaryPayload(txtFile, "/jpeg", null);
        putBinary(url, user1, payload, null, null, 415);
    }

    /**
     * Test version creation when updating file binary content.
     *
     * TODO also relates to future v1 api to list version history, etc
     *
     * <p>PUT:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/content}
     *
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/children}
     */
    @Test
    public void testUpdateFileVersionCreate() throws Exception
    {
        String myNodeId = getMyNodeId(user1);

        Document d1 = new Document();
        d1.setName("d1.txt");
        d1.setNodeType(TYPE_CM_CONTENT);

        // create *empty* text file - as of now, versioning is not enabled by default
        HttpResponse response = post(getNodeChildrenUrl(myNodeId), user1, toJsonAsStringNonNull(d1), 201);
        Document documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        String docId = documentResp.getId();
        assertFalse(documentResp.getAspectNames().contains("cm:versionable"));
        assertNull(documentResp.getProperties()); // no properties (ie. no "cm:versionLabel")

        int cnt = 0;

        // updates - no versions
        for (int i = 1; i <= 3; i++)
        {
            cnt++;

            // Update the empty node's content - no version created
            String content = "The quick brown fox jumps over the lazy dog " + cnt;
            documentResp = updateTextFile(user1, docId, content, null);
            assertFalse(documentResp.getAspectNames().contains("cm:versionable"));
            assertNull(documentResp.getProperties()); // no properties (ie. no "cm:versionLabel")
        }

        // Update again - with version comment (note: either "comment" &/or "majorVersion" will enable versioning)
        cnt++;
        int majorVersion = 1;
        int minorVersion = 0;

        String content = "The quick brown fox jumps over the lazy dog "+cnt;

        Map<String, String> params = new HashMap<>();
        params.put("comment", "my version "+cnt);

        documentResp = updateTextFile(user1, docId, content, params);
        assertTrue(documentResp.getAspectNames().contains("cm:versionable"));
        assertNotNull(documentResp.getProperties());

        assertEquals(majorVersion+"."+minorVersion, documentResp.getProperties().get("cm:versionLabel"));

        // Update again - with another version comment
        cnt++;
        minorVersion++;

        content = "The quick brown fox jumps over the lazy dog "+cnt;
        params = new HashMap<>();
        params.put("comment", "my version "+cnt);

        documentResp = updateTextFile(user1, docId, content, params);
        assertTrue(documentResp.getAspectNames().contains("cm:versionable"));
        assertNotNull(documentResp.getProperties());
        assertEquals(majorVersion+"."+minorVersion, documentResp.getProperties().get("cm:versionLabel"));

        minorVersion = 0;

        // Updates - major versions
        for (int i = 1; i <= 3; i++)
        {
            cnt++;
            majorVersion++;

            content = "The quick brown fox jumps over the lazy dog "+cnt;

            params = new HashMap<>();
            params.put("comment", "my version "+cnt);
            params.put("majorVersion", "true");

            documentResp = updateTextFile(user1, docId, content, params);
            assertTrue(documentResp.getAspectNames().contains("cm:versionable"));
            assertNotNull(documentResp.getProperties());
            assertEquals(majorVersion+"."+minorVersion, documentResp.getProperties().get("cm:versionLabel"));
        }

        // Updates - minor versions
        for (int i = 1; i <= 3; i++)
        {
            cnt++;
            minorVersion++;

            content = "The quick brown fox jumps over the lazy dog "+cnt;

            params = new HashMap<>();
            params.put("comment", "my version "+cnt);
            params.put("majorVersion", "false");

            documentResp = updateTextFile(user1, docId, content, params);
            assertTrue(documentResp.getAspectNames().contains("cm:versionable"));
            assertNotNull(documentResp.getProperties());
            assertEquals(majorVersion+"."+minorVersion, documentResp.getProperties().get("cm:versionLabel"));
        }

        // Update again - as another major version
        cnt++;
        majorVersion++;
        minorVersion = 0;

        content = "The quick brown fox jumps over the lazy dog "+cnt;

        params = new HashMap<>();
        params.put("comment", "my version "+cnt);
        params.put("majorVersion", "true");

        documentResp = updateTextFile(user1, docId, content, params);
        assertTrue(documentResp.getAspectNames().contains("cm:versionable"));
        assertNotNull(documentResp.getProperties());
        assertEquals(majorVersion+"."+minorVersion, documentResp.getProperties().get("cm:versionLabel"));

        // Update again - as another (minor) version
        // note: no version params (comment &/or majorVersion) needed since versioning is enabled on this content

        cnt++;
        minorVersion++;

        content = "The quick brown fox jumps over the lazy dog "+cnt;

        documentResp = updateTextFile(user1, docId, content, null);
        assertTrue(documentResp.getAspectNames().contains("cm:versionable"));
        assertNotNull(documentResp.getProperties());
        assertEquals(majorVersion+"."+minorVersion, documentResp.getProperties().get("cm:versionLabel"));

        // Remove versionable aspect
        List<String> aspectNames = documentResp.getAspectNames();
        aspectNames.remove("cm:versionable");
        Document dUpdate = new Document();
        dUpdate.setAspectNames(aspectNames);

        response = put(URL_NODES, user1, docId, toJsonAsStringNonNull(dUpdate), null, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertFalse(documentResp.getAspectNames().contains("cm:versionable"));
        assertNull(documentResp.getProperties()); // no properties (ie. no "cm:versionLabel")

        // Updates - no versions
        for (int i = 1; i <= 3; i++)
        {
            cnt++;

            // Update the empty node's content - no version created
            content = "The quick brown fox jumps over the lazy dog " + cnt;
            documentResp = updateTextFile(user1, docId, content, null);
            assertFalse(documentResp.getAspectNames().contains("cm:versionable"));
            assertNull(documentResp.getProperties()); // no properties (ie. no "cm:versionLabel")
        }

        // TODO add tests to also check version comment (when we can list version history)
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
                .setFileData(new FileData(fileName, file));
        MultiPartRequest reqBody = multiPartBuilder.build();

        // Upload text content
        HttpResponse response = post(getNodeChildrenUrl(Nodes.PATH_MY), user1, reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

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
        getSingle(getNodeContentUrl(contentNodeId), user1, null, null, headers, 304);

        // Update the content to change the node's modified date
        Document docUpdate = new Document();
        docUpdate.setProperties(Collections.singletonMap("cm:description", (Object) "desc updated!"));
        // Wait a second then update, as the dates will be rounded to
        // ignore millisecond when checking for If-Modified-Since
        Thread.sleep(1000L);
        response = put(URL_NODES, user1, contentNodeId, toJsonAsStringNonNull(docUpdate), null, 200);
        Document updatedDocument = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertEquals(contentNodeId, updatedDocument.getId());

        // The requested "If-Modified-Since" date is older than node's modified date
        response = getSingle(getNodeContentUrl(contentNodeId), user1, null, null, headers, 200);
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
        response = post(getNodeChildrenUrl(Nodes.PATH_MY), user1, reqBody.getBody(), null, reqBody.getContentType(), 201);
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

        response = getSingle(NodesEntityResource.class, user1, contentNodeId + "/content", params, 200);
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
        getSingle(getNodeContentUrl(contentNodeId), user1, null, null, headers, 304);
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

        String rootNodeId = getRootNodeId(user1);
        String sharedNodeId = getSharedNodeId(user1);

        Map params = new HashMap<>();
        params.put(Nodes.PARAM_RELATIVE_PATH, "/Sites");
        HttpResponse response = getSingle(NodesEntityResource.class, user1, rootNodeId, params, 200);
        Node nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        String sitesNodeId = nodeResp.getId();

        params = new HashMap<>();
        params.put(Nodes.PARAM_RELATIVE_PATH, "/Data Dictionary");
        response = getSingle(NodesEntityResource.class, user1, rootNodeId, params, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        String ddNodeId = nodeResp.getId();


        params = new HashMap<>();
        params.put("include", "allowableOperations");

        response = getSingle(NodesEntityResource.class, user1, rootNodeId, params, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNull(nodeResp.getAllowableOperations());

        response = getSingle(NodesEntityResource.class, user1, sharedNodeId, params, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNotNull(nodeResp.getAllowableOperations());
        assertEquals(1, nodeResp.getAllowableOperations().size());
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_CREATE));

        // -ve
        delete(URL_NODES, user1, sharedNodeId, 403);

        response = getSingle(NodesEntityResource.class, user1, getMyNodeId(user1), params, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNotNull(nodeResp.getAllowableOperations());
        assertEquals(3, nodeResp.getAllowableOperations().size());
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_DELETE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_CREATE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_UPDATE));

        // create folder
        nodeResp = createFolder(user1, sharedNodeId, "folder 1 - "+RUNID);
        String folderId = nodeResp.getId();
        assertNull(nodeResp.getAllowableOperations());

        response = getSingle(NodesEntityResource.class, user1, folderId, null, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNull(nodeResp.getAllowableOperations());

        response = getSingle(NodesEntityResource.class, user1, folderId, params, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNotNull(nodeResp.getAllowableOperations());
        assertEquals(3, nodeResp.getAllowableOperations().size());
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_DELETE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_CREATE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_UPDATE));

        // create file
        nodeResp = createTextFile(user1, folderId, "my file - "+RUNID+".txt", "The quick brown fox jumps over the lazy dog");
        String fileId = nodeResp.getId();
        assertNull(nodeResp.getAllowableOperations());

        response = getSingle(NodesEntityResource.class, user1, fileId, null, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNull(nodeResp.getAllowableOperations());

        // a file - no create
        response = getSingle(NodesEntityResource.class, user1, fileId, params, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNotNull(nodeResp.getAllowableOperations());
        assertEquals(2, nodeResp.getAllowableOperations().size());
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_DELETE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_UPDATE));


        // as user2 ...

        response = getSingle(NodesEntityResource.class, user2, folderId, params, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNotNull(nodeResp.getAllowableOperations());
        assertEquals(1, nodeResp.getAllowableOperations().size());
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_CREATE));

        // -ve
        delete(URL_NODES, user2, folderId, 403);

        response = getSingle(NodesEntityResource.class, user2, fileId, params, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNull(nodeResp.getAllowableOperations());

        // -ve
        delete(URL_NODES, user2, fileId, 403);

        // as admin ...

        // TODO improve - admin-related tests
        publicApiClient.setRequestContext(new RequestContext("-default-", "admin", "admin"));

        response = publicApiClient.get(NodesEntityResource.class, folderId, null, params);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNotNull(nodeResp.getAllowableOperations());
        assertEquals(3, nodeResp.getAllowableOperations().size());
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_DELETE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_CREATE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_UPDATE));

        // a file - no create
        response = publicApiClient.get(NodesEntityResource.class, fileId, null, params);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNotNull(nodeResp.getAllowableOperations());
        assertEquals(2, nodeResp.getAllowableOperations().size());
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_DELETE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_UPDATE));

        response = publicApiClient.get(NodesEntityResource.class, sharedNodeId, null, params);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNotNull(nodeResp.getAllowableOperations());
        assertEquals(3, nodeResp.getAllowableOperations().size());
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_CREATE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_UPDATE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_DELETE));

        // Company Home - no delete
        response = publicApiClient.get(NodesEntityResource.class, rootNodeId, null, params);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNotNull(nodeResp.getAllowableOperations());
        assertEquals(2, nodeResp.getAllowableOperations().size());
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_CREATE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_UPDATE));

        // -ve
        response = publicApiClient.delete(getScope(), 1, URL_NODES, rootNodeId, null, null, params);
        checkStatus(403, response.getStatusCode());

        // Sites - no delete
        response = publicApiClient.get(NodesEntityResource.class, sitesNodeId, null, params);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNotNull(nodeResp.getAllowableOperations());
        assertEquals(2, nodeResp.getAllowableOperations().size());
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_CREATE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_UPDATE));

        // -ve
        response = publicApiClient.delete(getScope(), 1, URL_NODES, sitesNodeId, null, null, params);
        checkStatus(403, response.getStatusCode());

        // Data Dictionary - no delete
        response = publicApiClient.get(NodesEntityResource.class, ddNodeId, null, params);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNotNull(nodeResp.getAllowableOperations());
        assertEquals(2, nodeResp.getAllowableOperations().size());
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_CREATE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_UPDATE));

        // -ve
        response = publicApiClient.delete(getScope(), 1, URL_NODES, ddNodeId, null, null, params);
        checkStatus(403, response.getStatusCode());

        publicApiClient.setRequestContext(null);

        // as userOneN1 ...
        String userId = userOneN1.getId();
        AuthenticationUtil.setFullyAuthenticatedUser(userId);
        String siteNodeId = userOneN1Site.getGuid();
        AuthenticationUtil.clearCurrentSecurityContext();

        response = getSingle(NodesEntityResource.class, userId, siteNodeId, params, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertEquals(userId, nodeResp.getCreatedByUser().getId());
        assertNotNull(nodeResp.getAllowableOperations());
        assertEquals(2, nodeResp.getAllowableOperations().size());
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_CREATE));
        assertTrue(nodeResp.getAllowableOperations().contains(Nodes.OP_UPDATE));

        // -ve
        delete(URL_NODES, userId, siteNodeId, 403);

        // fix for REPO-514 (NPE for a node that was neither a file/document nor a folder)
        Node n = new Node();
        n.setName("o1");
        n.setNodeType(TYPE_CM_OBJECT);
        response = post(getNodeChildrenUrl(folderId), user1, toJsonAsStringNonNull(n), 201);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        String o1Id = nodeResp.getId();

        params = new HashMap<>();
        params.put("include", "allowableOperations");
        response = getSingle(NodesEntityResource.class, user1, o1Id, params, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertNotNull(nodeResp.getAllowableOperations());

        // some cleanup
        params = Collections.singletonMap("permanent", "true");
        delete(URL_NODES, user1, folderId, params, 204);
    }

    @Override
    public String getScope()
    {
        return "public";
    }
}

