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

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.rest.api.nodes.NodesEntityResource;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.PublicApiHttpClient;
import org.alfresco.rest.api.tests.client.data.Document;
import org.alfresco.rest.api.tests.client.data.Node;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.TempFileProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.alfresco.rest.api.tests.util.RestApiUtil.toJsonAsStringNonNull;
import static org.junit.Assert.*;

/**
 * API tests for Node Versions (File Version History)
 *
 * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/{nodeId}/versions</li>
 *
 * @author janv
 */
public class NodeVersionsApiTest extends AbstractBaseApiTest
{
    private static final String URL_DELETED_NODES = "deleted-nodes";
    private static final String URL_VERSIONS = "versions";

    private String user1;
    private String user2;
    private List<String> users = new ArrayList<>();

    private final String RUNID = System.currentTimeMillis()+"";

    protected MutableAuthenticationService authenticationService;
    protected PermissionService permissionService;
    protected PersonService personService;

    @Before
    public void setup() throws Exception
    {
        authenticationService = applicationContext.getBean("authenticationService", MutableAuthenticationService.class);
        permissionService = applicationContext.getBean("permissionService", PermissionService.class);
        personService = applicationContext.getBean("personService", PersonService.class);

        // note: createUser currently relies on repoService
        user1 = createUser("user1-" + RUNID);
        user2 = createUser("user2-" + RUNID);

        // We just need to clean the on-premise-users,
        // so the tests for the specific network would work.
        users.add(user1);
        users.add(user2);
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

    protected String getNodeVersionsUrl(String nodeId)
    {
        return URL_NODES + "/" + nodeId + "/" + URL_VERSIONS;
    }

    /**
     * Test version creation when uploading files (via multi-part/form-data with overwrite=true)
     *
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/children}
     *
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/versions}
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/versions/<versionId>}
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/versions/<versionId>/content}
     */
    @Test
    public void testUploadFileVersionCreateWithOverwrite() throws Exception
    {
        String myNodeId = getMyNodeId(user1);

        int cnt = 1;

        int majorVersion = 1;
        int minorVersion = 0;

        // Upload text file - versioning is currently auto enabled on upload (create file via multi-part/form-data)

        String contentName = "content " + System.currentTimeMillis();
        String content = "The quick brown fox jumps over the lazy dog "+cnt;

        Document documentResp = createTextFile(user1, myNodeId, contentName, content, "UTF-8", null);
        String docId = documentResp.getId();
        assertTrue(documentResp.getAspectNames().contains("cm:versionable"));
        assertNotNull(documentResp.getProperties());
        assertEquals(majorVersion+"."+minorVersion, documentResp.getProperties().get("cm:versionLabel"));

        Map<String, String> params = null;

        // Upload text file with same name - with overwrite=true
        for (int i = 1; i <= 3; i++)
        {
            cnt++;
            minorVersion++;

            content = "The quick brown fox jumps over the lazy dog " + cnt;

            params = new HashMap<>();
            params.put("overwrite", "true");

            documentResp = createTextFile(user1, myNodeId, contentName, content, "UTF-8", params);
            assertTrue(documentResp.getAspectNames().contains("cm:versionable"));
            assertNotNull(documentResp.getProperties());
            assertEquals(majorVersion+"."+minorVersion, documentResp.getProperties().get("cm:versionLabel"));
        }

        minorVersion = 0;

        // Updates - major versions
        for (int i = 1; i <= 3; i++)
        {
            cnt++;
            majorVersion++;

            content = "The quick brown fox jumps over the lazy dog "+cnt;

            params = new HashMap<>();
            params.put("overwrite", "true");
            params.put("comment", "my version "+cnt);
            params.put("majorVersion", "true");

            documentResp = createTextFile(user1, myNodeId, contentName, content, "UTF-8", params);
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
            params.put("overwrite", "true");
            params.put("comment", "my version "+cnt);
            params.put("majorVersion", "false");

            documentResp = createTextFile(user1, myNodeId, contentName, content, "UTF-8", params);
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
        params.put("overwrite", "true");
        params.put("majorVersion", "true");

        documentResp = createTextFile(user1, myNodeId, contentName, content, "UTF-8", params);
        assertEquals(majorVersion+"."+minorVersion, documentResp.getProperties().get("cm:versionLabel"));

        // Update again - as another (minor) version
        cnt++;
        minorVersion++;

        content = "The quick brown fox jumps over the lazy dog "+cnt;

        params = new HashMap<>();
        params.put("overwrite", "true");

        documentResp = createTextFile(user1, myNodeId, contentName, content, "UTF-8", params);
        assertEquals(majorVersion+"."+minorVersion, documentResp.getProperties().get("cm:versionLabel"));


        // -ve test
        params = new HashMap<>();
        params.put("overwrite", "true");
        params.put("autorename", "true");

        createTextFile(user1, myNodeId, contentName, content, "UTF-8", params, 400);


        // Remove versionable aspect
        List<String> aspectNames = documentResp.getAspectNames();
        aspectNames.remove("cm:versionable");
        Document dUpdate = new Document();
        dUpdate.setAspectNames(aspectNames);

        HttpResponse response = put(URL_NODES, user1, docId, toJsonAsStringNonNull(dUpdate), null, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertFalse(documentResp.getAspectNames().contains("cm:versionable"));
        assertNull(documentResp.getProperties()); // no properties (ie. no "cm:versionLabel")

        // TODO review consistency - for example, we do allow update binary content (after removing versionable)

        // -ve test - do not allow overwrite (using POST upload) if the file is not versionable
        cnt++;
        content = "The quick brown fox jumps over the lazy dog "+cnt;

        params = new HashMap<>();
        params.put("overwrite", "true");

        createTextFile(user1, myNodeId, contentName, content, "UTF-8", params, 409);

        // TODO add checks for version comment (eg. when we can list version history)
    }


    /**
     * Tests api when uploading a file and then updating with a new version
     *
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/children}
     *
     * <p>PUT:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/content}
     *
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/versions}
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/versions/<versionId>}
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/versions/<versionId>/content}
     */
    @Test
    public void testUploadFileVersionUpdate() throws Exception
    {
        // As user 1 ...
        String myFolderNodeId = getMyNodeId(user1);

        // create folder
        String f1Id = createFolder(user1, myFolderNodeId, "f1").getId();

        try
        {
            int majorVersion = 1;
            int minorVersion = 0;

            // Upload text file - versioning is currently auto enabled on upload (create file via multi-part/form-data)

            int verCnt = 1;

            String contentName = "content " + System.currentTimeMillis();
            String content = "The quick brown fox jumps over the lazy dog "+verCnt;

            Document documentResp = createTextFile(user1, myFolderNodeId, contentName, content, "UTF-8", null);
            String d1Id = documentResp.getId();

            String versionId = majorVersion+"."+minorVersion;

            HttpResponse response = getSingle(URL_NODES, user1, d1Id, 200);
            Node nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
            assertTrue(nodeResp.getAspectNames().contains("cm:versionable"));
            assertEquals(versionId, nodeResp.getProperties().get("cm:versionLabel"));
            assertEquals("MAJOR", nodeResp.getProperties().get("cm:versionType"));

            Paging paging = getPaging(0, 100);

            Map<String, String> params = new HashMap<>();
            params.put("include", "properties");
            response = getAll(getNodeVersionsUrl(d1Id), user1, paging, params, 200);
            List<Node> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(verCnt, nodes.size());
            assertEquals(versionId, nodes.get(0).getProperties().get("cm:versionLabel"));
            assertEquals("MAJOR", nodes.get(0).getProperties().get("cm:versionType"));

            // get version info
            response = getSingle(getNodeVersionsUrl(d1Id), user1, versionId, null, 200);
            Node node = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
            assertEquals(versionId, node.getProperties().get("cm:versionLabel"));
            assertEquals("MAJOR", node.getProperties().get("cm:versionType"));

            // Update the content
            int updateCnt = 3;
            for (int i = 1; i <= updateCnt; i++)
            {
                verCnt++;
                minorVersion++;

                // Update
                content = "The quick brown fox jumps over the lazy dog "+verCnt;
                ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
                File txtFile = TempFileProvider.createTempFile(inputStream, getClass().getSimpleName(), ".txt");
                PublicApiHttpClient.BinaryPayload payload = new PublicApiHttpClient.BinaryPayload(txtFile);

                putBinary(getNodeContentUrl(d1Id), user1, payload, null, null, 200);

                versionId = majorVersion+"."+minorVersion;

                // get live node
                response = getSingle(URL_NODES, user1, d1Id, 200);
                nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
                assertTrue(nodeResp.getAspectNames().contains("cm:versionable"));
                assertEquals(versionId, nodeResp.getProperties().get("cm:versionLabel"));
                assertEquals("MINOR", nodeResp.getProperties().get("cm:versionType"));

                // get version node info
                response = getSingle(getNodeVersionsUrl(d1Id), user1, versionId, null, 200);
                node = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
                assertEquals(versionId, node.getProperties().get("cm:versionLabel"));
                assertEquals("MINOR", node.getProperties().get("cm:versionType"));

                // check version history count
                response = getAll(getNodeVersionsUrl(d1Id), user1, paging, null, 200);
                nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
                assertEquals(verCnt, nodes.size());
            }

            int totalVerCnt = verCnt;

            params = new HashMap<>();
            params.put("include", "properties");
            response = getAll(getNodeVersionsUrl(d1Id), user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(totalVerCnt, nodes.size());

            checkVersionHistoryAndContent(d1Id, nodes, verCnt, majorVersion, minorVersion);

            // delete to trashcan/archive ...
            delete(URL_NODES, user1, d1Id, null, 204);

            {
                // -ver tests
                getSingle(NodesEntityResource.class, user1, d1Id, null, 404);
                getAll(getNodeVersionsUrl(d1Id), user1, null, null, 404);
            }

            // ... and then restore again
            post(URL_DELETED_NODES+"/"+d1Id+"/restore", user1, null, null, 200);

            response = getAll(getNodeVersionsUrl(d1Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(totalVerCnt, nodes.size());

            {
                // -ve test - unauthenticated - belts-and-braces ;-)
                getAll(getNodeVersionsUrl(d1Id), null, paging, null, 401);

                // -ve test - unauthenticated - belts-and-braces ;-)
                getAll(getNodeVersionsUrl("dummy"), user1, paging, null, 404);
            }
        }
        finally
        {
            // some cleanup
            Map<String, String> params = Collections.singletonMap("permanent", "true");
            delete(URL_NODES, user1, f1Id, params, 204);
        }
    }

    private void checkVersionHistoryAndContent(String docId, List<Node> nodesWithProps, int verCnt, int majorVersion, int minorVersion) throws Exception
    {
        String versionId = null;

        // check version history - including default sort order (ie. time descending)
        // also download and check the versioned content
        for (Node versionNode : nodesWithProps)
        {
            versionId = majorVersion+"."+minorVersion;

            assertEquals(versionId, versionNode.getId());
            assertEquals(versionId, versionNode.getProperties().get("cm:versionLabel"));
            if (versionId.equals("1.0"))
            {
                assertEquals("MAJOR", versionNode.getProperties().get("cm:versionType"));
            }
            else
            {
                assertEquals("MINOR", versionNode.getProperties().get("cm:versionType"));
            }
            assertNull(versionNode.getParentId());
            assertNull(versionNode.getCreatedByUser());
            assertNull(versionNode.getCreatedAt());

            // Download version content - by default with Content-Disposition header
            HttpResponse response = getSingle(getNodeVersionsUrl(docId), user1, versionId+"/content", null, 200);
            String textContent = response.getResponse();
            assertEquals("The quick brown fox jumps over the lazy dog "+verCnt, textContent);

            minorVersion--;
            verCnt--;
        }

    }

    /**
     * Tests api when uploading a file and then updating with a new version
     *
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/children}
     *
     * <p>PUT:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/content}
     *
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/versions}
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/versions/<versionId>}
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/versions/<versionId>/content}
     */
    @Test
    public void testCreateEmtpyFileVersionUpdate() throws Exception
    {
        // As user 1 ...
        String myFolderNodeId = getMyNodeId(user1);

        // create folder
        String f1Id = createFolder(user1, myFolderNodeId, "f1").getId();

        try
        {
            // create "empty" content node
            Node n = new Node();
            n.setName("d1");
            n.setNodeType(TYPE_CM_CONTENT);
            HttpResponse response = post(getNodeChildrenUrl(f1Id), user1, toJsonAsStringNonNull(n), 201);
            String d1Id = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class).getId();

            response = getSingle(URL_NODES, user1, d1Id, 200);
            Node nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
            assertFalse(nodeResp.getAspectNames().contains("cm:versionable"));

            Paging paging = getPaging(0, 100);

            // empty list - before

            response = getAll(getNodeVersionsUrl(d1Id), user1, paging, null, 200);
            List<Node> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(0, nodes.size());

            // note: we do not disallow listing version history on non-content node - however currently no API method to version say a folder
            response = getAll(getNodeVersionsUrl(f1Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(0, nodes.size());

            // Update the empty node's content a few times (before/without versioning)
            int cntBefore = 2;
            int verCnt = 1;

            for (int i = 1; i <= cntBefore; i++)
            {
                String content = "The quick brown fox jumps over the lazy dog " + verCnt;
                ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
                File txtFile = TempFileProvider.createTempFile(inputStream, getClass().getSimpleName(), ".txt");
                PublicApiHttpClient.BinaryPayload payload = new PublicApiHttpClient.BinaryPayload(txtFile);

                putBinary(getNodeContentUrl(d1Id), user1, payload, null, null, 200);

                verCnt++;

                response = getSingle(URL_NODES, user1, d1Id, 200);
                nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
                assertFalse(nodeResp.getAspectNames().contains("cm:versionable"));

                response = getAll(getNodeVersionsUrl(d1Id), user1, paging, null, 200);
                nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
                assertEquals(0, nodes.size());
            }

            // Enable versioning
            Node nodeUpdate = new Node();
            nodeUpdate.setAspectNames(Collections.singletonList("cm:versionable"));
            put(URL_NODES, user1, d1Id, toJsonAsStringNonNull(nodeUpdate), null, 200);

            String versionId = "1.0";

            Map<String, String> params = new HashMap<>();
            params.put("include", "properties");
            response = getAll(getNodeVersionsUrl(d1Id), user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(1, nodes.size());
            assertEquals(versionId, nodes.get(0).getProperties().get("cm:versionLabel"));
            assertEquals("MAJOR", nodes.get(0).getProperties().get("cm:versionType"));

            // get version info
            response = getSingle(getNodeVersionsUrl(d1Id), user1, versionId, null, 200);
            Node node = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
            assertEquals(versionId, node.getProperties().get("cm:versionLabel"));
            assertEquals("MAJOR", node.getProperties().get("cm:versionType"));

            // Update the content a few more times (after/with versioning)
            int cntAfter = 3;
            for (int i = 1; i <= cntAfter; i++)
            {
                // Update again
                String content = "The quick brown fox jumps over the lazy dog " + verCnt;
                ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
                File txtFile = TempFileProvider.createTempFile(inputStream, getClass().getSimpleName(), ".txt");
                PublicApiHttpClient.BinaryPayload payload = new PublicApiHttpClient.BinaryPayload(txtFile);

                putBinary(getNodeContentUrl(d1Id), user1, payload, null, null, 200);

                verCnt++;

                // get version info
                versionId = "1."+i;
                response = getSingle(getNodeVersionsUrl(d1Id), user1, versionId, null, 200);
                node = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
                assertEquals(versionId, node.getProperties().get("cm:versionLabel"));
                assertEquals("MINOR", node.getProperties().get("cm:versionType"));

                response = getAll(getNodeVersionsUrl(d1Id), user1, paging, null, 200);
                nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
                assertEquals(i+1, nodes.size());
            }

            int totalVerCnt = cntAfter+1;
            int minorVersion = totalVerCnt-1;
            verCnt = cntBefore+cntAfter;

            params = new HashMap<>();
            params.put("include", "properties");
            response = getAll(getNodeVersionsUrl(d1Id), user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(totalVerCnt, nodes.size());

            checkVersionHistoryAndContent(d1Id, nodes, verCnt, 1, minorVersion);

            // delete to trashcan/archive ...
            delete(URL_NODES, user1, d1Id, null, 204);

            // -ve tests
            {
                getSingle(NodesEntityResource.class, user1, d1Id, null, 404);
                getAll(getNodeVersionsUrl(d1Id), user1, null, null, 404);
            }

            // ... and then restore again
            post(URL_DELETED_NODES+"/"+d1Id+"/restore", user1, null, null, 200);

            response = getAll(getNodeVersionsUrl(d1Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(cntAfter+1, nodes.size());

            //
            // -ve tests
            //

            {
                // -ve test - unauthenticated - belts-and-braces ;-)
                getAll(getNodeVersionsUrl(d1Id), null, paging, null, 401);

                // -ve test - unauthenticated - belts-and-braces ;-)
                getAll(getNodeVersionsUrl("dummy"), user1, paging, null, 404);
            }
        }
        finally
        {
            // some cleanup
            Map<String, String> params = Collections.singletonMap("permanent", "true");
            delete(URL_NODES, user1, f1Id, params, 204);
        }
    }

    @Override
    public String getScope()
    {
        return "public";
    }
}
