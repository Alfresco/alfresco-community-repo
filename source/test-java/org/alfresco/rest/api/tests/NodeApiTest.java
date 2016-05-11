/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.ContentInfo;
import org.alfresco.rest.api.model.Document;
import org.alfresco.rest.api.model.Folder;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.PathInfo;
import org.alfresco.rest.api.model.PathInfo.ElementInfo;
import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.rest.api.nodes.NodesEntityResource;
import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.alfresco.rest.api.tests.RepoService.TestPerson;
import org.alfresco.rest.api.tests.RepoService.TestSite;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.alfresco.rest.api.tests.client.PublicApiClient.ExpectedPaging;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.data.SiteRole;
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
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
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
 * @author Jamal Kaabi-Mofrad
 */
public class NodeApiTest extends AbstractBaseApiTest
{
    private static final String RESOURCE_PREFIX = "publicapi/upload/";

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
    protected Repository repositoryHelper;
    protected JacksonUtil jacksonUtil;
    protected PermissionService permissionService;


    @Before
    public void setup() throws Exception
    {
        authenticationService = applicationContext.getBean("authenticationService", MutableAuthenticationService.class);
        personService = applicationContext.getBean("personService", PersonService.class);
        repositoryHelper = applicationContext.getBean("repositoryHelper", Repository.class);
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
        NodeRef myFilesNodeRef = repositoryHelper.getUserHome(personService.getPerson(user1));

        String folder1 = "folder" + System.currentTimeMillis() + "_1";
        repoService.createFolder(myFilesNodeRef, folder1);

        String folder2 = "folder" + System.currentTimeMillis() + "_2";
        repoService.createFolder(myFilesNodeRef, folder2);

        String content1 = "content" + System.currentTimeMillis() + "_1";
        NodeRef contentNodeRef = repoService.createDocument(myFilesNodeRef, content1, "The quick brown fox jumps over the lazy dog.");
        repoService.getNodeService().setProperty(contentNodeRef, ContentModel.PROP_OWNER, user1);
        repoService.getNodeService().setProperty(contentNodeRef, ContentModel.PROP_LAST_THUMBNAIL_MODIFICATION_DATA,
                    (Serializable) Collections.singletonList("doclib:1444660852296"));

        Paging paging = getPaging(0, Integer.MAX_VALUE);
        HttpResponse response = getAll(getChildrenUrl(myFilesNodeRef), user1, paging, 200);
        List<Document> nodes = jacksonUtil.parseEntries(response.getJsonResponse(), Document.class);
        assertEquals(3, nodes.size());

        // Order by folders and modified date first
        Map<String, String> orderBy = Collections.singletonMap("orderBy", "isFolder DESC,modifiedAt DESC");
        response = getAll(getChildrenUrl(myFilesNodeRef), user1, paging, orderBy, 200);
        nodes = jacksonUtil.parseEntries(response.getJsonResponse(), Document.class);
        assertEquals(3, nodes.size());
        assertEquals(folder2, nodes.get(0).getName());
        assertEquals(folder1, nodes.get(1).getName());
        Document node = (Document) nodes.get(2);
        assertEquals(content1, node.getName());
        assertEquals("cm:content", node.getNodeType());
        assertEquals(contentNodeRef.getId(), node.getId());
        UserInfo  createdByUser = node.getCreatedByUser();
        assertEquals(user1, createdByUser.getUserName());
        assertEquals(user1 + " " + user1, createdByUser.getDisplayName());
        UserInfo modifiedByUser = node.getModifiedByUser();
        assertEquals(user1, modifiedByUser.getUserName());
        assertEquals(user1 + " " + user1, modifiedByUser.getDisplayName());
        assertEquals(MimetypeMap.MIMETYPE_BINARY, node.getContent().getMimeType());
        assertNotNull(node.getContent().getMimeTypeName());
        assertNotNull(node.getContent().getEncoding());
        assertTrue(node.getContent().getSizeInBytes() > 0);

        // Invalid QName (Namespace prefix cm... is not mapped to a namespace URI) for the orderBy parameter.
        orderBy = Collections.singletonMap("orderBy", "isFolder DESC,cm" + System.currentTimeMillis() + ":modified DESC");
        getAll(getChildrenUrl(myFilesNodeRef), user1, paging, orderBy, 400);

        AuthenticationUtil.setFullyAuthenticatedUser(user2);
        // user2 tries to access user1's home folder
        getAll(getChildrenUrl(myFilesNodeRef), user2, paging, 403);

        AuthenticationUtil.setFullyAuthenticatedUser(user1);
        // request property via select
        Map<String, String> params = new LinkedHashMap<>();
        params.put("select", "cm_lastThumbnailModification");// TODO replace the underscore with colon when the framework is fixed.
        params.put("orderBy", "isFolder DESC,modifiedAt DESC");
        response = getAll(getChildrenUrl(myFilesNodeRef), user1, paging, params, 200);
        nodes = jacksonUtil.parseEntries(response.getJsonResponse(), Document.class);
        assertEquals(3, nodes.size());
        assertNull("There shouldn't be a 'properties' object in the response.", nodes.get(0).getProperties());
        assertNull("There shouldn't be a 'properties' object in the response.", nodes.get(1).getProperties());
        assertNotNull("There should be a 'properties' object in the response.", nodes.get(2).getProperties());
        Set<Entry<String, Object>> props = nodes.get(2).getProperties().entrySet();
        assertEquals(1, props.size());
        Entry<String, Object> entry = props.iterator().next();
        assertEquals("cm:lastThumbnailModification", entry.getKey());
        assertEquals("doclib:1444660852296", ((List<?>) entry.getValue()).get(0));
    }

    /**
     * Tests get node with path information.
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>?select=path}
     */
    @Test
    public void testGetPathElements_DocLib() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(userOneN1.getId());
        userOneN1Site.inviteToSite(userTwoN1.getEmail(), SiteRole.SiteConsumer);

        NodeRef docLibNodeRef = userOneN1Site.getContainerNodeRef(("documentLibrary"));

        // /Company Home/Sites/RandomSite<timestamp>/documentLibrary/folder<timestamp>_A
        String folderA = "folder" + System.currentTimeMillis() + "_A";
        NodeRef folderA_Ref = repoService.createFolder(docLibNodeRef, folderA);

        // /Company Home/Sites/RandomSite<timestamp>/documentLibrary/folder<timestamp>_A/folder<timestamp>_B
        String folderB = "folder" + System.currentTimeMillis() + "_B";
        NodeRef folderB_Ref = repoService.createFolder(folderA_Ref, folderB);

        // /Company Home/Sites/RandomSite<timestamp>/documentLibrary/folder<timestamp>_A/folder<timestamp>_B/folder<timestamp>_C
        String folderC = "folder" + System.currentTimeMillis() + "_C";
        NodeRef folderC_Ref = repoService.createFolder(folderB_Ref, folderC);

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
     * Tests get node with path information.
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>?select=path}
     */
    @Test
    public void testGetPathElements_MyFiles() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(user1);
        HttpResponse response = getSingle(NodesEntityResource.class, user1, Nodes.PATH_MY, null, 200);
        Node node = jacksonUtil.parseEntry(response.getJsonResponse(), Node.class);
        String myFilesNodeId = node.getId();
        assertNotNull(myFilesNodeId);
        assertEquals(user1, node.getName());
        assertTrue(node.getIsFolder());

        // /Company Home/User Homes/user<timestamp>/folder<timestamp>_A
        String folderA = "folder" + System.currentTimeMillis() + "_A";
        NodeRef folderA_Ref = repoService.createFolder(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, myFilesNodeId), folderA);

        // /Company Home/User Homes/user<timestamp>/folder<timestamp>_A/folder<timestamp>_B
        String folderB = "folder" + System.currentTimeMillis() + "_B";
        NodeRef folderB_Ref = repoService.createFolder(folderA_Ref, folderB);

        // /Company Home/User Homes/user<timestamp>/folder<timestamp>_A/folder<timestamp>_B/folder<timestamp>_C
        String folderC = "folder" + System.currentTimeMillis() + "_C";
        NodeRef folderC_Ref = repoService.createFolder(folderB_Ref, folderC);

        //...nodes/nodeId?select=pathInfo
        Map<String, String> params = Collections.singletonMap("select", "path");
        response = getSingle(NodesEntityResource.class, user1, folderC_Ref.getId(), params, 200);
        node = jacksonUtil.parseEntry(response.getJsonResponse(), Node.class);
        PathInfo pathInfo = node.getPath();
        assertNotNull(pathInfo);
        assertTrue(pathInfo.getIsComplete());
        assertNotNull(pathInfo.getName());
        // the pathInfo should only include the parents (not the requested node)
        assertFalse(pathInfo.getName().endsWith(folderC));
        assertTrue(pathInfo.getName().startsWith("/Company Home"));
        List<ElementInfo> pathElements = pathInfo.getElements();
        assertEquals(5, pathElements.size());
        assertEquals("Company Home", pathElements.get(0).getName());
        assertNotNull(pathElements.get(0).getId());
        assertEquals("User Homes", pathElements.get(1).getName());
        assertNotNull(pathElements.get(1).getId());
        assertEquals(user1, pathElements.get(2).getName());
        assertNotNull(pathElements.get(2).getId());
        assertEquals(folderA, pathElements.get(3).getName());
        assertNotNull(pathElements.get(3).getId());
        assertEquals(folderB, pathElements.get(4).getName());
        assertNotNull(pathElements.get(4).getId());
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
        final int numOfNodes = pagingResult.getCount().intValue();

        MultiPartBuilder multiPartBuilder = MultiPartBuilder.create()
                    .setFileData(new FileData(fileName, file, MimetypeMap.MIMETYPE_PDF));
        MultiPartRequest reqBody = multiPartBuilder.build();

        // Try to upload
        response = post(getChildrenUrl(Nodes.PATH_MY), user1, new String(reqBody.getBody()), null, reqBody.getContentType(), 201);
        Document document = jacksonUtil.parseEntry(response.getJsonResponse(), Document.class);
        // Check the upload response
        assertEquals(fileName, document.getName());
        ContentInfo contentInfo = document.getContent();
        assertNotNull(contentInfo);
        assertEquals(MimetypeMap.MIMETYPE_PDF, contentInfo.getMimeType());

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
        post(getChildrenUrl(Nodes.PATH_MY), user1, new String(reqBody.getBody()), null, reqBody.getContentType(), 409);

        response = getAll(getChildrenUrl(Nodes.PATH_MY), user1, paging, 200);
        pagingResult = parsePaging(response.getJsonResponse());
        assertNotNull(paging);
        assertEquals("Duplicate file name. The file shouldn't have been uploaded.", numOfNodes + 1, pagingResult.getCount().intValue());

        // User2 tries to upload a new file into the user1's home folder.
        response = getSingle(NodesEntityResource.class, user1, Nodes.PATH_MY, null, 200);
        Folder user1Home = jacksonUtil.parseEntry(response.getJsonResponse(), Folder.class);
        final String fileName2 = "quick-2.txt";
        final File file2 = getResourceFile(fileName2);
        reqBody = MultiPartBuilder.create()
                    .setFileData(new FileData(fileName2, file2, MimetypeMap.MIMETYPE_TEXT_PLAIN))
                    .build();
        post(getChildrenUrl(user1Home.getId()), user2, new String(reqBody.getBody()), null, reqBody.getContentType(), 403);

        response = getAll(getChildrenUrl(Nodes.PATH_MY), user1, paging, 200);
        pagingResult = parsePaging(response.getJsonResponse());
        assertNotNull(paging);
        assertEquals("Access Denied. The file shouldn't have been uploaded.", numOfNodes + 1, pagingResult.getCount().intValue());

        // User1 tries to upload a file into a document rather than a folder!
        post(getChildrenUrl(document.getId()), user1, new String(reqBody.getBody()), null, reqBody.getContentType(), 400);

        // Try to upload a file without defining the required formData
        reqBody = MultiPartBuilder.create().build();
        post(getChildrenUrl(Nodes.PATH_MY), user1, new String(reqBody.getBody()), null, reqBody.getContentType(), 400);
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
        final int numOfNodes = pagingResult.getCount().intValue();

        MultiPartBuilder multiPartBuilder = MultiPartBuilder.create()
                    .setFileData(new FileData(fileName, file, MimetypeMap.MIMETYPE_TEXT_PLAIN));
        MultiPartRequest reqBody = multiPartBuilder.build();
        // Try to upload
        response = post(getChildrenUrl(folderA_Ref), userOneN1.getId(), new String(reqBody.getBody()), null, reqBody.getContentType(), 201);
        Document document = jacksonUtil.parseEntry(response.getJsonResponse(), Document.class);
        // Check the upload response
        assertEquals(fileName, document.getName());
        ContentInfo contentInfo = document.getContent();
        assertNotNull(contentInfo);
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
        post(getChildrenUrl(folderA_Ref), userOneN1.getId(), new String(reqBody.getBody()), null, reqBody.getContentType(), 409);

        // Set overwrite=true and upload the same file again
        reqBody = MultiPartBuilder.copy(multiPartBuilder)
                    .setOverwrite(true)
                    .build();
        post(getChildrenUrl(folderA_Ref), userOneN1.getId(), new String(reqBody.getBody()), null, reqBody.getContentType(), 201);

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
        post(getChildrenUrl(folderA_Ref), userTwoN1.getId(), new String(reqBody.getBody()), null, reqBody.getContentType(), 403);
    }

    /**
     * Tests delete (folder or file).
     * <p>DELETE:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>}
     */
    @Test
    public void testDelete() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(user1);
        NodeRef myFilesNodeRef = repositoryHelper.getUserHome(personService.getPerson(user1));

        String content1 = "content" + System.currentTimeMillis() + "_1";
        NodeRef content1Ref = repoService.createDocument(myFilesNodeRef, content1, "The quick brown fox jumps over the lazy dog.");

        // delete file
        delete("nodes", user1, content1Ref.getId(), 204);

        // -ve test
        delete("nodes", user1, content1Ref.getId(), 404);

        String folder1 = "folder" + System.currentTimeMillis() + "_1";
        NodeRef folder1Ref = repoService.createFolder(myFilesNodeRef, folder1);

        String folder2 = "folder" + System.currentTimeMillis() + "_2";
        NodeRef folder2Ref = repoService.createFolder(folder1Ref, folder2);

        String content2 = "content" + System.currentTimeMillis() + "_2";
        NodeRef content2Ref = repoService.createDocument(folder2Ref, content2, "The quick brown fox jumps over the lazy dog.");

        // cascade delete folder
        delete("nodes", user1, folder1Ref.getId(), 204);

        // -ve test
        delete("nodes", user1, folder2Ref.getId(), 404);
        delete("nodes", user1, content2Ref.getId(), 404);

        // -ve test
        NodeRef chNodeRef = repositoryHelper.getCompanyHome();
        delete("nodes", user1, chNodeRef.getId(), 403);
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

        NodeRef personNodeRef = personService.getPerson(user1);
        NodeRef myFilesNodeRef = repositoryHelper.getUserHome(personNodeRef);

        String postUrl = "nodes/"+myFilesNodeRef.getId()+"/children";

        Folder f1 = new Folder();
        f1.setName("f1");
        f1.setNodeType("cm:folder");

        // create folder
        HttpResponse response = post(postUrl, user1, toJsonAsStringNonNull(f1), 201);

        Folder f1Created = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertNotNull(f1Created.getId());
        assertEquals("f1",f1Created.getName());
        assertEquals("cm:folder",f1Created.getNodeType());
        assertEquals(true, f1Created.getIsFolder());
        assertNotNull(f1Created.getCreatedAt());
        assertEquals(user1,f1Created.getCreatedByUser().getUserName());
        assertNotNull(f1Created.getModifiedAt());
        assertEquals(user1,f1Created.getModifiedByUser().getUserName());
        assertEquals(myFilesNodeRef.getId(), f1Created.getParentId());
        assertTrue(f1Created.getAspectNames().contains("cm:auditable"));
        assertNull(f1Created.getProperties());
        assertNull(f1Created.getPath());
        assertNull(f1Created.getIsLink());

        // create folder with properties
        Map<String,Object> props = new HashMap<>();
        props.put("cm:title","my folder title");
        props.put("cm:description","my folder description");

        Folder f2 = new Folder();
        f2.setName("f2");
        f2.setNodeType("cm:folder");
        f2.setProperties(props);

        response = post(postUrl, user1, toJsonAsStringNonNull(f2), 201);
        Folder f2Created = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Folder.class);
        assertNotNull(f2Created.getId());

        assertTrue(f2Created.getAspectNames().contains("cm:auditable"));
        assertTrue(f2Created.getAspectNames().contains("cm:titled"));
        assertEquals(f2Created.getProperties().get("cm:title"),"my folder title");
        assertEquals(f2Created.getProperties().get("cm:description"),"my folder description");

        // -ve test - name is mandatory
        Folder invalid = new Folder();
        invalid.setNodeType("cm:folder");
        post(postUrl, user1, toJsonAsStringNonNull(invalid), 400);

        // -ve test - node type is mandatory
        invalid = new Folder();
        invalid.setName("my folder");
        post(postUrl, user1, toJsonAsStringNonNull(invalid), 400);

        // -ve test - invalid (eg. not a folder) parent id
        Folder f3 = new Folder();
        f3.setName("f3");
        f3.setNodeType("cm:folder");
        post("nodes/"+personNodeRef.getId()+"/children", user1, toJsonAsStringNonNull(f3), 400);

        // -ve test - unknown parent folder node id
        post("nodes/"+UUID.randomUUID().toString()+"/children", user1, toJsonAsStringNonNull(f3), 404);

        // -ve test - duplicate name
        post(postUrl, user1, toJsonAsStringNonNull(f1), 409);
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

        NodeRef personNodeRef = personService.getPerson(user1);
        NodeRef myFilesNodeRef = repositoryHelper.getUserHome(personNodeRef);

        String postUrl = "nodes/"+myFilesNodeRef.getId()+"/children";

        Document d1 = new Document();
        d1.setName("d1.txt");
        d1.setNodeType("cm:content");
        ContentInfo ci = new ContentInfo();
        ci.setMimeType("text/plain");
        d1.setContent(ci);

        // create empty file
        HttpResponse response = post(postUrl, user1, toJsonAsStringNonNull(d1), 201);

        Document d1Created = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertNotNull(d1Created.getId());
        assertEquals("d1.txt",d1Created.getName());
        assertEquals("cm:content",d1Created.getNodeType());
        assertEquals(false, d1Created.getIsFolder());
        assertNotNull(d1Created.getCreatedAt());
        assertEquals(user1,d1Created.getCreatedByUser().getUserName());
        assertNotNull(d1Created.getModifiedAt());
        assertEquals(user1,d1Created.getModifiedByUser().getUserName());
        assertEquals(myFilesNodeRef.getId(), d1Created.getParentId());
        assertTrue(d1Created.getAspectNames().contains("cm:auditable"));
        assertEquals(0L, d1Created.getContent().getSizeInBytes());
        assertEquals("text/plain", d1Created.getContent().getMimeType());
        assertEquals("Plain Text", d1Created.getContent().getMimeTypeName());
        assertEquals("UTF-8", d1Created.getContent().getEncoding());
        assertNull(d1Created.getProperties());
        assertNull(d1Created.getPath());
        assertNull(d1Created.getIsLink());

        // create empty file with properties
        Map<String,Object> props = new HashMap<>();
        props.put("cm:title","my file title");
        props.put("cm:description","my file description");

        Document d2 = new Document();
        d2.setName("d2.txt");
        d2.setNodeType("cm:content");
        d2.setProperties(props);

        response = post(postUrl, user1, toJsonAsStringNonNull(d2), 201);
        Document d2Created = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertNotNull(d2Created.getId());

        assertTrue(d2Created.getAspectNames().contains("cm:auditable"));
        assertTrue(d2Created.getAspectNames().contains("cm:titled"));
        assertEquals(d2Created.getProperties().get("cm:title"),"my file title");
        assertEquals(d2Created.getProperties().get("cm:description"),"my file description");

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
        post("nodes/"+personNodeRef.getId()+"/children", user1, toJsonAsStringNonNull(d3), 400);

        // -ve test - unknown parent folder node id
        post("nodes/"+UUID.randomUUID().toString()+"/children", user1, toJsonAsStringNonNull(d3), 404);

        // -ve test - duplicate name
        post(postUrl, user1, toJsonAsStringNonNull(d1), 409);
    }

    // TODO add test to create multiple folders & empty files (within same parent folder)

    // TODO add test for file/folder links - creating, getting, listing, deleting

    private String getChildrenUrl(NodeRef parentNodeRef)
    {
        return getChildrenUrl(parentNodeRef.getId());
    }

    private String getChildrenUrl(String parentId)
    {
        return "nodes/" + parentId + "/children";
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
