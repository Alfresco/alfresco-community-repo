/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.rest.AbstractSingleNetworkSiteTest;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.VersionOptions;
import org.alfresco.rest.api.nodes.NodesEntityResource;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.PublicApiHttpClient;
import org.alfresco.rest.api.tests.client.data.ContentInfo;
import org.alfresco.rest.api.tests.client.data.Document;
import org.alfresco.rest.api.tests.client.data.Node;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.util.Pair;
import org.alfresco.util.TempFileProvider;
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
 * V1 REST API tests for Node Versions (File Version History)
 *
 * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/{nodeId}/versions</li>
 *
 * @author janv
 */
public class NodeVersionsApiTest extends AbstractSingleNetworkSiteTest
{
    private static final String URL_DELETED_NODES = "deleted-nodes";

    protected String getNodeVersionRevertUrl(String nodeId, String versionId)
    {
        return getNodeVersionsUrl(nodeId) + "/" + versionId + "/revert";
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
        setRequestContext(user1);
        
        String myFolderNodeId = getMyNodeId();

        // create folder
        String f1Id = createFolder(myFolderNodeId, "f1").getId();

        try
        {
            int verCnt = 1;

            String versionLabel = "1.0";

            // upload text file - versioning is currently auto enabled on upload (create file via multi-part/form-data)

            String textContentSuffix = "The quick brown fox jumps over the lazy dog ";
            String contentName = "content-1-" + System.currentTimeMillis();
            String content = textContentSuffix + verCnt;

            // create first version (ie. 1.0)
            Document documentResp = createTextFile(f1Id, contentName, content, "UTF-8", null);
            String docId = documentResp.getId();
            assertTrue(documentResp.getAspectNames().contains("cm:versionable"));
            assertNotNull(documentResp.getProperties());
            assertEquals(versionLabel, documentResp.getProperties().get("cm:versionLabel"));

            Map<String, String> params = null;

            // create some minor versions (note: majorVersion=null) (ie. 1.1, 1.2, 1.3)
            int cnt = 3;
            versionLabel = uploadTextFileVersions(user1, f1Id, contentName, cnt, textContentSuffix, verCnt, null, versionLabel).getFirst();
            verCnt = verCnt+cnt;

            // create some major versions  (ie. 2.0, 3.0)
            cnt = 2;
            versionLabel = uploadTextFileVersions(user1, f1Id, contentName, cnt, textContentSuffix, verCnt, true, versionLabel).getFirst();
            verCnt = verCnt+cnt;

            // create some more minor versions (ie. 3.1, 3.2, 3.3)
            cnt = 3;
            versionLabel = uploadTextFileVersions(user1, f1Id, contentName, cnt, textContentSuffix, verCnt, false, versionLabel).getFirst();
            verCnt = verCnt+cnt;

            assertEquals("3.3", versionLabel);
            assertEquals(9, verCnt);

            {
                // -ve test
                params = new HashMap<>();
                params.put(Nodes.PARAM_OVERWRITE, "true");
                params.put(Nodes.PARAM_AUTO_RENAME, "true");

                createTextFile(myFolderNodeId, contentName, content, "UTF-8", params, 400);
            }

            // remove versionable aspect
            List<String> aspectNames = documentResp.getAspectNames();
            aspectNames.remove("cm:versionable");
            Document dUpdate = new Document();
            dUpdate.setAspectNames(aspectNames);

            HttpResponse response = put(URL_NODES, docId, toJsonAsStringNonNull(dUpdate), null, 200);
            documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
            assertFalse(documentResp.getAspectNames().contains("cm:versionable"));
            assertNull(documentResp.getProperties()); // no properties (ie. no "cm:versionLabel")

            // check no version history
            response = getAll(getNodeVersionsUrl(docId), null, null, 200);
            List<Node> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(0, nodes.size());

            {
                // -ve test - do not allow overwrite (using POST upload) if the file is not versionable
                cnt++;
                content = textContentSuffix + cnt;

                params = new HashMap<>();
                params.put(Nodes.PARAM_OVERWRITE, "true");

                createTextFile(f1Id, contentName, content, "UTF-8", params, 409);
            }

            // we do allow update of binary content with no versioning (after removing versionable)
            textContentSuffix = "Amazingly few discotheques provide jukeboxes ";

            for (int i = 1; i <= 4; i++)
            {
                content = textContentSuffix + i;
                ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
                File txtFile = TempFileProvider.createTempFile(inputStream, getClass().getSimpleName(), ".txt");
                PublicApiHttpClient.BinaryPayload payload = new PublicApiHttpClient.BinaryPayload(txtFile);

                putBinary(getNodeContentUrl(docId), payload, null, null, 200);
            }

            // check no version history
            response = getAll(getNodeVersionsUrl(docId), null, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(0, nodes.size());
        }
        finally
        {
            // some cleanup
            setRequestContext(user1);
            deleteNode(f1Id, true, 204);
        }
    }

    /**
     * Test uploading a new file which starts with a minor version (0.1).
     *
     * @throws Exception
     */
    @Test
    public void testUploadFileVersionAsMinor() throws Exception
    {
        setRequestContext(user1);
        
        String myFolderNodeId = getMyNodeId();

        // create folder
        String f1Id = createFolder(myFolderNodeId, "f1").getId();

        try
        {
            int verCnt = 1;
            int cnt = 1;
            String versionLabel = "0.1";

            String textContentSuffix = "Amazingly few discotheques provide jukeboxes ";
            String contentName = "content-2-" + System.currentTimeMillis();
            String content = textContentSuffix + cnt;

            Map<String, String> params = new HashMap<>();
            params.put("majorVersion", "false");

            // create a new file with a minor version (ie. 0.1)
            Document documentResp = createTextFile(f1Id, contentName, content, "UTF-8", params);
            String docId = documentResp.getId();
            assertTrue(documentResp.getAspectNames().contains("cm:versionable"));
            assertNotNull(documentResp.getProperties());
            assertEquals(versionLabel, documentResp.getProperties().get("cm:versionLabel"));

            // also show that we continue with minor versions
            cnt = 2;
            versionLabel = updateFileVersions(user1, docId, cnt, textContentSuffix, verCnt, null, versionLabel);
            verCnt = verCnt+cnt;

            // now create some major versions
            cnt = 3;
            versionLabel = updateFileVersions(user1, docId, cnt, textContentSuffix, verCnt, true, versionLabel);
            verCnt = verCnt+cnt;

            assertEquals("3.0", versionLabel);
            assertEquals(6, verCnt);

            // check version history count
            HttpResponse response = getAll(getNodeVersionsUrl(docId), null, null, 200);
            List<Node> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(verCnt, nodes.size());
        }
        finally
        {
            // some cleanup
            setRequestContext(user1);
            deleteNode(f1Id, true, 204);
        }
    }

    /**
     * Test delete version
     * 
     * Note: see also VersionServiceImplTest.testDeleteLastVersion
     *
     * @throws Exception
     */
    @Test
    public void testDeleteVersion() throws Exception
    {
        setRequestContext(user1);
        
        String sharedFolderNodeId = getSharedNodeId();

        // create folder
        String f1Id = null;

        try
        {
            f1Id = createFolder(sharedFolderNodeId, "testDeleteVersion-f1").getId();

            String textContentSuffix = "Amazingly few discotheques provide jukeboxes ";
            String contentName = "content-1";

            int cnt = 4;
            Pair<String, String> pair = uploadTextFileVersions(user1, f1Id, contentName, cnt, textContentSuffix, 0, null, null);
            String versionLabel = pair.getFirst();
            String docId = pair.getSecond();

            assertEquals("1.3", versionLabel);

            // check version history count
            HttpResponse response = getAll(getNodeVersionsUrl(docId), null, null, 200);
            List<Node> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(cnt, nodes.size());

            // -ve tests:
            {
                setRequestContext(null);
                
                // -ve test - unauthenticated - belts-and-braces ;-)
                delete(getNodeVersionsUrl(docId), "1.0", null, 401);

                setRequestContext(user1);

                // -ve test - unknown nodeId
                delete(getNodeVersionsUrl("dummy"), "1.0", null, 404);

                // -ve test - unknown versionId
                delete(getNodeVersionsUrl(docId), "15.0", null, 404);

                setRequestContext(user2);

                // -ve test - permission denied (on version other than most recent)
                delete(getNodeVersionsUrl(docId), "1.0", null, 403);

                // -ve test - permission denied (on most recent version)
                delete(getNodeVersionsUrl(docId), "1.3", null, 403);
            }

            setRequestContext(user1);
            
            delete(getNodeVersionsUrl(docId), "1.0", null, 204);

            response = getAll(getNodeVersionsUrl(docId), null, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(cnt - 1, nodes.size());

            // check live node (does not change)
            response = getSingle(URL_NODES, docId, 200);
            Node nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
            assertEquals("1.3", nodeResp.getProperties().get("cm:versionLabel"));
            
            response = getSingle(getNodeContentUrl(docId), null, 200);
            assertEquals(textContentSuffix+"4", response.getResponse());
            
            // delete current (most recent) version
            delete(getNodeVersionsUrl(docId), "1.3", null, 204);

            response = getAll(getNodeVersionsUrl(docId), null, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(cnt - 2, nodes.size());

            // check live node (changes)
            response = getSingle(URL_NODES, docId, 200);
            nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
            assertEquals("1.2", nodeResp.getProperties().get("cm:versionLabel"));

            response = getSingle(getNodeContentUrl(docId), null, 200);
            assertEquals(textContentSuffix+"3", response.getResponse());

            // delete another version
            delete(getNodeVersionsUrl(docId), "1.1", null, 204);

            response = getAll(getNodeVersionsUrl(docId), null, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(cnt - 3, nodes.size());

            // check live node (does not change)
            response = getSingle(URL_NODES, docId, 200);
            nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
            assertEquals("1.2", nodeResp.getProperties().get("cm:versionLabel"));

            response = getSingle(getNodeContentUrl(docId), null, 200);
            assertEquals(textContentSuffix+"3", response.getResponse());

            // -ve test - cannot delete last version (via delete version api call) (see REPO-835 & REPO-834)
            delete(getNodeVersionsUrl(docId), "1.2", null, 422);

            /* note: currently we cannot delete last version so this is not applicable
            // check live node - removing last version does not (currently) remove versionable aspect
            response = getSingle(URL_NODES, docId, 200);
            nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
            assertTrue(nodeResp.getAspectNames().contains("cm:versionable"));
            Map<String, Object> props = nodeResp.getProperties();
            if (props != null)
            {
                assertNull(props.get("cm:versionLabel"));
                assertNull(props.get("cm:versionType")); // note: see special fix in delete version API (at least for now)
            }
            */

            response = getAll(getNodeVersionsUrl(docId), null, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(1, nodes.size());
            assertEquals("1.2", nodes.get(0).getId());

            // Update again ..
            String textContent = "more changes 0";
            ByteArrayInputStream inputStream = new ByteArrayInputStream(textContent.getBytes());
            File txtFile = TempFileProvider.createTempFile(inputStream, getClass().getSimpleName(), ".txt");
            PublicApiHttpClient.BinaryPayload payload = new PublicApiHttpClient.BinaryPayload(txtFile);

            response = putBinary(getNodeContentUrl(docId), payload, null,  null, 200);
            nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
            assertTrue(nodeResp.getAspectNames().contains("cm:versionable"));
            assertEquals("1.3", nodeResp.getProperties().get("cm:versionLabel"));
            assertEquals("MINOR", nodeResp.getProperties().get("cm:versionType"));
            
            // remove versionable aspect (this will clear the history and disable versioning)
            response = getSingle(URL_NODES, docId, 200);
            nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);

            List<String> aspectNamesToKeep = new ArrayList<>();
            aspectNamesToKeep.addAll(nodeResp.getAspectNames());
            aspectNamesToKeep.remove("cm:versionable");

            Node nUpdate = new Node();
            nUpdate.setAspectNames(aspectNamesToKeep);

            response = put(URL_NODES, docId, toJsonAsStringNonNull(nUpdate), null, 200);
            nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
            assertFalse(nodeResp.getAspectNames().contains("cm:versionable"));
            Map<String, Object> props = nodeResp.getProperties();
            if (props != null)
            {
                assertNull(props.get("cm:versionLabel"));
                assertNull(props.get("cm:versionType"));
            }
            
            response = getAll(getNodeVersionsUrl(docId), null, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(0, nodes.size());
            
            // Update again ..
            textContent = "more changes 1";
            inputStream = new ByteArrayInputStream(textContent.getBytes());
            txtFile = TempFileProvider.createTempFile(inputStream, getClass().getSimpleName(), ".txt");
            payload = new PublicApiHttpClient.BinaryPayload(txtFile);
            response = putBinary(getNodeContentUrl(docId), payload, null,  null, 200);
            nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
            assertFalse(nodeResp.getAspectNames().contains("cm:versionable"));
            
            // re-enable versioning (default model properties should cause initial version to be created as 1.0)
            response = getSingle(URL_NODES, docId, 200);
            nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
            aspectNamesToKeep = new ArrayList<>();
            aspectNamesToKeep.addAll(nodeResp.getAspectNames());
            aspectNamesToKeep.add("cm:versionable");

            nUpdate = new Node();
            nUpdate.setAspectNames(aspectNamesToKeep);

            response = put(URL_NODES, docId, toJsonAsStringNonNull(nUpdate), null, 200);
            nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
            assertTrue(nodeResp.getAspectNames().contains("cm:versionable"));
            props = nodeResp.getProperties();
            assertEquals("1.0", props.get("cm:versionLabel"));
            assertEquals("MAJOR", props.get("cm:versionType"));

            // check content
            response = getSingle(getNodeVersionsUrl(docId), "1.0/content", null, 200);
            assertEquals(textContent, response.getResponse());

            // Update again ..
            textContent = "more changes 2";
            inputStream = new ByteArrayInputStream(textContent.getBytes());
            txtFile = TempFileProvider.createTempFile(inputStream, getClass().getSimpleName(), ".txt");
            payload = new PublicApiHttpClient.BinaryPayload(txtFile);

            response = putBinary(getNodeContentUrl(docId), payload, null,  null, 200);
            nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
            assertTrue(nodeResp.getAspectNames().contains("cm:versionable"));
            props = nodeResp.getProperties();
            assertEquals("1.1", props.get("cm:versionLabel"));
            assertEquals("MINOR", props.get("cm:versionType"));

            // check content
            response = getSingle(getNodeVersionsUrl(docId), "1.1/content", null, 200);
            assertEquals(textContent, response.getResponse());
        }
        finally
        {
            if (f1Id != null)
            {
                // some cleanup
                setRequestContext(user1);
                deleteNode(f1Id, true, 204);
            }
        }
    }

    /**
     * This test helper method uses "overwrite=true" to create one or more new versions, including the initial create if needed.
     *
     * If the file does not already exist (currentVersionLabel should be null) and majorVersionIn is also null
     * then the first version is created as MAJOR (1.0) and subsequent versions are created as MINOR.
     *
     * @param userId
     * @param parentFolderNodeId - parent folder
     * @param fileName - file name
     * @param cnt - number of new versions (>= 1)
     * @param textContentPrefix - prefix for text content
     * @param currentVersionCounter - overall version counter, used as a suffix in text content and version comment
     * @param majorVersionIn - if null then false, if true then create MAJOR versions else if false create MINOR versions
     * @param currentVersionLabel - the current version label (if file already exists)
     * @return
     * @throws Exception
     */
    private Pair<String, String> uploadTextFileVersions(String userId, String parentFolderNodeId, String fileName, int cnt,
                                                        String textContentPrefix, int currentVersionCounter,
                                                        final Boolean majorVersionIn, String currentVersionLabel) throws Exception
    {
        Map<String, String> params = new HashMap<>();
        params.put(Nodes.PARAM_OVERWRITE, "true");

        if (majorVersionIn != null)
        {
            params.put(Nodes.PARAM_VERSION_MAJOR, majorVersionIn.toString());
        }

        String docId = null;
        for (int i = 1; i <= cnt; i++)
        {
            boolean expectedMajorVersion = (majorVersionIn != null ? majorVersionIn : false);

            if (currentVersionLabel == null)
            {
                currentVersionLabel = "0.0";

                // special case - 1st version is major (if not specified otherwise)
                if (majorVersionIn == null)
                {
                    expectedMajorVersion = true;
                }
            }

            String[] parts = currentVersionLabel.split("\\.");
            int majorVer = new Integer(parts[0]).intValue();
            int minorVer = new Integer(parts[1]).intValue();

            if (expectedMajorVersion)
            {
                majorVer++;
                minorVer = 0;
            } else
            {
                minorVer++;
            }

            currentVersionLabel = majorVer + "." + minorVer;

            currentVersionCounter++;

            params.put("comment", "my version " + currentVersionCounter);

            String textContent = textContentPrefix + currentVersionCounter;

            // uses upload with overwrite here ...
            Document documentResp = createTextFile(parentFolderNodeId, fileName, textContent, "UTF-8", params);
            docId = documentResp.getId();
            assertTrue(documentResp.getAspectNames().contains("cm:versionable"));
            assertNotNull(documentResp.getProperties());
            assertEquals(currentVersionLabel, documentResp.getProperties().get("cm:versionLabel"));

            // double-check - get version node info
            HttpResponse response = getSingle(getNodeVersionsUrl(docId), currentVersionLabel, null, 200);
            Node nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
            assertEquals(currentVersionLabel, nodeResp.getProperties().get("cm:versionLabel"));
            assertEquals((expectedMajorVersion ? "MAJOR" : "MINOR"), nodeResp.getProperties().get("cm:versionType"));
        }

        return new Pair<String,String>(currentVersionLabel, docId);
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
        setRequestContext(user1);
        
        String myFolderNodeId = getMyNodeId();

        // create folder
        String f1Id = createFolder(myFolderNodeId, "f1").getId();

        try
        {
            int majorVersion = 1;
            int minorVersion = 0;

            // Upload text file - versioning is currently auto enabled on upload (create file via multi-part/form-data)

            int verCnt = 1;

            String textContentSuffix = "The quick brown fox jumps over the lazy dog ";
            String contentName = "content " + System.currentTimeMillis();
            String content = textContentSuffix+verCnt;

            Document documentResp = createTextFile(myFolderNodeId, contentName, content, "UTF-8", null);
            String d1Id = documentResp.getId();

            String versionId = majorVersion+"."+minorVersion;

            HttpResponse response = getSingle(URL_NODES, d1Id, 200);
            Node nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
            assertTrue(nodeResp.getAspectNames().contains("cm:versionable"));
            assertEquals(versionId, nodeResp.getProperties().get("cm:versionLabel"));
            assertEquals("MAJOR", nodeResp.getProperties().get("cm:versionType"));

            Paging paging = getPaging(0, 100);

            Map<String, String> params = new HashMap<>();
            params.put("include", "properties");
            response = getAll(getNodeVersionsUrl(d1Id), paging, params, 200);
            List<Node> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(verCnt, nodes.size());
            assertEquals(versionId, nodes.get(0).getProperties().get("cm:versionLabel"));
            assertEquals("MAJOR", nodes.get(0).getProperties().get("cm:versionType"));

            // get version info
            response = getSingle(getNodeVersionsUrl(d1Id), versionId, null, 200);
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
                content = textContentSuffix+verCnt;
                ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
                File txtFile = TempFileProvider.createTempFile(inputStream, getClass().getSimpleName(), ".txt");
                PublicApiHttpClient.BinaryPayload payload = new PublicApiHttpClient.BinaryPayload(txtFile);

                putBinary(getNodeContentUrl(d1Id), payload, null, null, 200);

                versionId = majorVersion+"."+minorVersion;

                // get live node
                response = getSingle(URL_NODES, d1Id, 200);
                nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
                assertTrue(nodeResp.getAspectNames().contains("cm:versionable"));
                assertEquals(versionId, nodeResp.getProperties().get("cm:versionLabel"));
                assertEquals("MINOR", nodeResp.getProperties().get("cm:versionType"));

                // get version node info
                response = getSingle(getNodeVersionsUrl(d1Id), versionId, null, 200);
                node = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
                assertEquals(versionId, node.getProperties().get("cm:versionLabel"));
                assertEquals("MINOR", node.getProperties().get("cm:versionType"));

                // check version history count
                response = getAll(getNodeVersionsUrl(d1Id), paging, null, 200);
                nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
                assertEquals(verCnt, nodes.size());
            }

            int totalVerCnt = verCnt;

            // check total version count - also get properties so that we can check version label etc
            params = new HashMap<>();
            params.put("include", "properties");
            response = getAll(getNodeVersionsUrl(d1Id), paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(totalVerCnt, nodes.size());

            checkVersionHistoryAndContent(d1Id, nodes, verCnt, textContentSuffix, null, majorVersion, minorVersion, false);

            // delete to trashcan/archive ...
            deleteNode(d1Id);

            {
                // -ve tests
                getSingle(NodesEntityResource.class, d1Id, null, 404);
                getAll(getNodeVersionsUrl(d1Id), null, null, 404);
            }

            // ... and then restore again
            post(URL_DELETED_NODES+"/"+d1Id+"/restore", null, null, 200);

            response = getAll(getNodeVersionsUrl(d1Id), paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(totalVerCnt, nodes.size());

            {
                setRequestContext(null);
                
                // -ve test - unauthenticated - belts-and-braces ;-)
                getAll(getNodeVersionsUrl(d1Id), paging, null, 401);

                setRequestContext(user1);

                // -ve test - unknown nodeId
                getAll(getNodeVersionsUrl("dummy"), paging, null, 404);
            }
        }
        finally
        {
            // some cleanup
            setRequestContext(user1);
            deleteNode(f1Id, true, 204);
        }
    }

    /**
     * Tests revert (ie. promote older version to become the latest/most recent version).
     *
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/versions/<versionId>/revert}
     */
    @Test
    public void testRevert() throws Exception
    {
        // As user 1 ...
        setRequestContext(user1);
        
        String sharedFolderNodeId = getSharedNodeId();

        // create folder
        String f1Id = null;

        try
        {
            f1Id = createFolder(sharedFolderNodeId, "testRevert-f1-"+System.currentTimeMillis()).getId();

            int majorVersion = 1;
            int minorVersion = 0;
            int verCnt = 1;

            String textContentSuffix = "The quick brown fox jumps over the lazy dog ";
            String contentName = "content " + System.currentTimeMillis();
            String content = textContentSuffix+verCnt;

            String updateVerCommentSuffix = "Update comment ";
            Map<String, String> params = new HashMap<>();
            params.put(Nodes.PARAM_VERSION_COMMENT, updateVerCommentSuffix+verCnt);

            // Upload text file - versioning is currently auto enabled on upload (create file via multi-part/form-data)
            Document documentResp = createTextFile(f1Id, contentName, content, "UTF-8", params);
            String d1Id = documentResp.getId();

            // Update the content
            int updateCnt = 3;
            for (int i = 1; i <= updateCnt; i++)
            {
                verCnt++;
                minorVersion++;

                // Update
                content = textContentSuffix+verCnt;
                ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
                File txtFile = TempFileProvider.createTempFile(inputStream, getClass().getSimpleName(), ".txt");
                PublicApiHttpClient.BinaryPayload payload = new PublicApiHttpClient.BinaryPayload(txtFile);

                params = new HashMap<>();
                params.put(Nodes.PARAM_VERSION_COMMENT, updateVerCommentSuffix+verCnt);

                putBinary(getNodeContentUrl(d1Id), payload, null, params, 200);
            }

            // check version history count - also get properties so that we can check version label etc
            params = new HashMap<>();
            params.put("include", "properties");
            HttpResponse response = getAll(getNodeVersionsUrl(d1Id), null, params, 200);
            List<Node> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(verCnt, nodes.size());

            // check version labels and content
            checkVersionHistoryAndContent(d1Id, nodes, verCnt, textContentSuffix, updateVerCommentSuffix, majorVersion, minorVersion, false);

            int revertMajorVersion = 1;
            int revertMinorVersion = 0;

            String revertVerCommentSuffix = "Revert comment ";

            int revertCnt = 3;
            for (int i = 1; i <= revertCnt; i++)
            {
                String revertVersionId = revertMajorVersion+"."+revertMinorVersion;

                VersionOptions versionOptions = new VersionOptions();
                versionOptions.setMajorVersion(true);
                versionOptions.setComment(revertVerCommentSuffix+i);

                post(getNodeVersionRevertUrl(d1Id, revertVersionId), toJsonAsStringNonNull(versionOptions), null, 200);

                verCnt++;
                revertMinorVersion++;

                majorVersion++;
            }

            // check version history count - also get properties so that we can check version label etc
            params = new HashMap<>();
            params.put("include", "properties");
            response = getAll(getNodeVersionsUrl(d1Id), null, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(verCnt, nodes.size());

            //  check version labels and content - most recently reverted, eg. version labels 4.0, 3.0, 2.0
            List<Node> revertedNodes = nodes.subList(0, revertCnt);
            checkVersionHistoryAndContent(d1Id, revertedNodes, updateCnt, textContentSuffix, revertVerCommentSuffix, majorVersion, 0, true);

            // check version labels and content - the rest of the version history (prior to reverted), eg. version labels 1.3, 1.2, 1.1, 1.0
            minorVersion = 3;
            List<Node> originalUpdatedNodes = nodes.subList(revertCnt, nodes.size());
            checkVersionHistoryAndContent(d1Id, originalUpdatedNodes, updateCnt+1, textContentSuffix, updateVerCommentSuffix, 1, minorVersion, false);

            // Currently, we also allow the most recent version to be reverted (ie. not disallowed by underlying VersionService)
            post(getNodeVersionRevertUrl(d1Id, majorVersion+".0"), "{}", null, 200);

            {
                setRequestContext(null);
                
                // -ve test - unauthenticated - belts-and-braces ;-)
                post(getNodeVersionRevertUrl(d1Id, "1.0"), "{}", null, 401);

                setRequestContext(user1);

                // -ve test - unknown nodeId
                post(getNodeVersionRevertUrl("dummy", "1.0"), "{}", null, 404);

                // -ve test - unknown versionId
                post(getNodeVersionRevertUrl(d1Id, "15.0"), "{}", null, 404);

                // -ve test - permission denied
                setRequestContext(user2);
                post(getNodeVersionRevertUrl(d1Id, "1.0"), "{}", null, 403);
            }
        }
        finally
        {
            if (f1Id != null)
            {
                // some cleanup
                setRequestContext(user1);
                deleteNode(f1Id, true, 204);
            }
        }
    }

    private void checkVersionHistoryAndContent(String docId, List<Node> nodesWithProps, int verCnt, String textContentSuffix, String verCommentSuffix, int majorVersion, int minorVersion, boolean majorVersions) throws Exception
    {
        String versionId = null;

        // check version history - including default sort order (ie. time descending)
        // also download and check the versioned content
        for (Node versionNode : nodesWithProps)
        {
            versionId = majorVersion+"."+minorVersion;

            assertEquals(versionId, versionNode.getId());
            assertEquals(versionId, versionNode.getProperties().get("cm:versionLabel"));

            if (versionId.endsWith(".0"))
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

            assertEquals((verCommentSuffix != null ? verCommentSuffix+verCnt : null), versionNode.getVersionComment());

            // Download version content - by default with Content-Disposition header
            HttpResponse response = getSingle(getNodeVersionsUrl(docId), versionId+"/content", null, 200);
            String textContent = response.getResponse();
            assertEquals(textContentSuffix+verCnt, textContent);

            if (majorVersions)
            {
                majorVersion--;
            }
            else
            {
                minorVersion--;
            }

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
    public void testCreateEmptyFileVersionUpdate() throws Exception
    {
        // As user 1 ...
        setRequestContext(user1);
        
        String myFolderNodeId = getMyNodeId();

        // create folder
        String f1Id = createFolder(myFolderNodeId, "f1").getId();

        try
        {
            // create "empty" content node
            Node n = new Node();
            n.setName("d1");
            n.setNodeType(TYPE_CM_CONTENT);
            HttpResponse response = post(getNodeChildrenUrl(f1Id), toJsonAsStringNonNull(n), 201);
            String d1Id = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class).getId();

            response = getSingle(URL_NODES, d1Id, 200);
            Node nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
            assertFalse(nodeResp.getAspectNames().contains("cm:versionable"));

            Paging paging = getPaging(0, 100);

            // empty list - before

            response = getAll(getNodeVersionsUrl(d1Id), paging, null, 200);
            List<Node> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(0, nodes.size());

            // note: we do not disallow listing version history on non-content node - however currently no API method to version say a folder
            response = getAll(getNodeVersionsUrl(f1Id), paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(0, nodes.size());

            // Update the empty node's content a few times (before/without versioning)
            int cntBefore = 2;
            int verCnt = 1;

            String textContentSuffix = "The quick brown fox jumps over the lazy dog ";

            for (int i = 1; i <= cntBefore; i++)
            {
                String content = textContentSuffix + verCnt;
                ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
                File txtFile = TempFileProvider.createTempFile(inputStream, getClass().getSimpleName(), ".txt");
                PublicApiHttpClient.BinaryPayload payload = new PublicApiHttpClient.BinaryPayload(txtFile);

                putBinary(getNodeContentUrl(d1Id), payload, null, null, 200);

                verCnt++;

                response = getSingle(URL_NODES, d1Id, 200);
                nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
                assertFalse(nodeResp.getAspectNames().contains("cm:versionable"));

                response = getAll(getNodeVersionsUrl(d1Id), paging, null, 200);
                nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
                assertEquals(0, nodes.size());
            }

            // Enable versioning - done here by adding versionable aspect
            // note: alternatively can use version params ("comment" &/or "majorVersion") on update (see separate test below)
            Node nodeUpdate = new Node();
            nodeUpdate.setAspectNames(Collections.singletonList("cm:versionable"));
            put(URL_NODES, d1Id, toJsonAsStringNonNull(nodeUpdate), null, 200);

            String versionId = "1.0";

            Map<String, String> params = new HashMap<>();
            params.put("include", "properties");
            response = getAll(getNodeVersionsUrl(d1Id), paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(1, nodes.size());
            assertEquals(versionId, nodes.get(0).getProperties().get("cm:versionLabel"));
            assertEquals("MAJOR", nodes.get(0).getProperties().get("cm:versionType"));

            // get version info
            response = getSingle(getNodeVersionsUrl(d1Id), versionId, null, 200);
            Node node = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
            assertEquals(versionId, node.getProperties().get("cm:versionLabel"));
            assertEquals("MAJOR", node.getProperties().get("cm:versionType"));

            // Update the content a few more times (after/with versioning)
            int cntAfter = 3;
            for (int i = 1; i <= cntAfter; i++)
            {
                // Update again
                String content = textContentSuffix + verCnt;
                ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
                File txtFile = TempFileProvider.createTempFile(inputStream, getClass().getSimpleName(), ".txt");
                PublicApiHttpClient.BinaryPayload payload = new PublicApiHttpClient.BinaryPayload(txtFile);

                putBinary(getNodeContentUrl(d1Id), payload, null, null, 200);

                verCnt++;

                // get version info
                versionId = "1."+i;
                response = getSingle(getNodeVersionsUrl(d1Id), versionId, null, 200);
                node = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
                assertEquals(versionId, node.getProperties().get("cm:versionLabel"));
                assertEquals("MINOR", node.getProperties().get("cm:versionType"));

                response = getAll(getNodeVersionsUrl(d1Id), paging, null, 200);
                nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
                assertEquals(i+1, nodes.size());
            }

            int totalVerCnt = cntAfter+1;
            int minorVersion = totalVerCnt-1;
            verCnt = cntBefore+cntAfter;

            params = new HashMap<>();
            params.put("include", "properties");
            response = getAll(getNodeVersionsUrl(d1Id), paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(totalVerCnt, nodes.size());

            checkVersionHistoryAndContent(d1Id, nodes, verCnt, textContentSuffix, null, 1, minorVersion, false);

            // delete to trashcan/archive ...
            deleteNode(d1Id);

            // -ve tests
            {
                getSingle(NodesEntityResource.class, d1Id, null, 404);
                getAll(getNodeVersionsUrl(d1Id), null, null, 404);
            }

            // ... and then restore again
            post(URL_DELETED_NODES+"/"+d1Id+"/restore", null, null, 200);

            response = getAll(getNodeVersionsUrl(d1Id), paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(cntAfter+1, nodes.size());

            //
            // -ve tests
            //

            {
                setRequestContext(null);
                
                // -ve test - unauthenticated - belts-and-braces ;-)
                getAll(getNodeVersionsUrl(d1Id), paging, null, 401);

                setRequestContext(user1);

                // -ve test - unknown node
                getAll(getNodeVersionsUrl("dummy"), paging, null, 404);

                //
                // -ve tests for non-versionable node with no version history (could be content, folder or some other node type)
                //
                
                // folder node - no version history
                String f99Id = createFolder(f1Id, "f99").getId();
                getSingle(getNodeVersionsUrl(f99Id), "1.0", null, 404);
                getSingle(getNodeVersionsUrl(f99Id), "1.0/content", null, 404);
                
                //content node - no version history
                n = new Node();
                n.setName("z1");
                n.setNodeType(TYPE_CM_CONTENT);
                response = post(getNodeChildrenUrl(f1Id), toJsonAsStringNonNull(n), 201);
                String z1Id = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class).getId();

                getSingle(getNodeVersionsUrl(z1Id), "1.0", null, 404);
                getSingle(getNodeVersionsUrl(z1Id), "1.0/content", null, 404);
            }

        }
        finally
        {
            // some cleanup
            setRequestContext(user1);
            deleteNode(f1Id, true, 204);
        }
    }

    @Test
    public void testRequestVersionsContentDirectUrl() throws Exception
    {
        setRequestContext(user1);

        String myNodeId = getMyNodeId();

        Document d1 = new Document();
        d1.setName("d1.txt");
        d1.setNodeType(TYPE_CM_CONTENT);

        // create *empty* text file - as of now, versioning is not enabled by default
        HttpResponse response = post(getNodeChildrenUrl(myNodeId), toJsonAsStringNonNull(d1), 201);
        Document documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        String docId = documentResp.getId();
        assertFalse(documentResp.getAspectNames().contains("cm:versionable"));
        assertNull(documentResp.getProperties()); // no properties (ie. no "cm:versionLabel")

        int majorVersion = 1;
        int minorVersion = 0;

        String content = "The quick brown fox jumps over the lazy dog ";

        Map<String, String> params = new HashMap<>();
        params.put("comment", "my version ");

        documentResp = updateTextFile(docId, content, params);
        assertTrue(documentResp.getAspectNames().contains("cm:versionable"));
        assertNotNull(documentResp.getProperties());
        assertEquals(majorVersion+"."+minorVersion, documentResp.getProperties().get("cm:versionLabel"));

        final String contentNodeId = documentResp.getId();

        // Check the upload response
        assertNotNull(documentResp.getProperties());
        assertTrue(documentResp.getAspectNames().contains("cm:versionable"));
        ContentInfo contentInfo = documentResp.getContent();
        assertNotNull(contentInfo);
        assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, contentInfo.getMimeType());

        HttpResponse dauResponse = post(getRequestVersionDirectAccessUrl(contentNodeId, majorVersion+"."+minorVersion), null, null, null, null, 501);
    }

    /**
     * Test version creation when updating file binary content.
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
        setRequestContext(user1);
        
        String myNodeId = getMyNodeId();

        Document d1 = new Document();
        d1.setName("d1.txt");
        d1.setNodeType(TYPE_CM_CONTENT);

        // create *empty* text file - as of now, versioning is not enabled by default
        HttpResponse response = post(getNodeChildrenUrl(myNodeId), toJsonAsStringNonNull(d1), 201);
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
            documentResp = updateTextFile(docId, content, null);
            assertFalse(documentResp.getAspectNames().contains("cm:versionable"));
            assertNull(documentResp.getProperties()); // no properties (ie. no "cm:versionLabel")
        }

        // Update again - with version comment - versioning is enabled here by using version params ("comment" &/or "majorVersion")
        // note: alternatively could add versionable aspect before doing the update (see separate test above)
        cnt++;
        int majorVersion = 1;
        int minorVersion = 0;

        String content = "The quick brown fox jumps over the lazy dog "+cnt;

        Map<String, String> params = new HashMap<>();
        params.put("comment", "my version "+cnt);

        documentResp = updateTextFile(docId, content, params);
        assertTrue(documentResp.getAspectNames().contains("cm:versionable"));
        assertNotNull(documentResp.getProperties());

        assertEquals(majorVersion+"."+minorVersion, documentResp.getProperties().get("cm:versionLabel"));

        // Update again - with another version comment
        cnt++;
        minorVersion++;

        content = "The quick brown fox jumps over the lazy dog "+cnt;
        params = new HashMap<>();
        params.put("comment", "my version "+cnt);

        documentResp = updateTextFile(docId, content, params);
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

            documentResp = updateTextFile(docId, content, params);
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

            documentResp = updateTextFile(docId, content, params);
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

        documentResp = updateTextFile(docId, content, params);
        assertTrue(documentResp.getAspectNames().contains("cm:versionable"));
        assertNotNull(documentResp.getProperties());
        assertEquals(majorVersion+"."+minorVersion, documentResp.getProperties().get("cm:versionLabel"));

        // Update again - as another (minor) version
        // note: no version params (comment &/or majorVersion) needed since versioning is enabled on this content

        cnt++;
        minorVersion++;

        content = "The quick brown fox jumps over the lazy dog "+cnt;

        documentResp = updateTextFile(docId, content, null);
        assertTrue(documentResp.getAspectNames().contains("cm:versionable"));
        assertNotNull(documentResp.getProperties());
        assertEquals(majorVersion+"."+minorVersion, documentResp.getProperties().get("cm:versionLabel"));

        // Remove versionable aspect
        List<String> aspectNames = documentResp.getAspectNames();
        aspectNames.remove("cm:versionable");
        Document dUpdate = new Document();
        dUpdate.setAspectNames(aspectNames);

        response = put(URL_NODES, docId, toJsonAsStringNonNull(dUpdate), null, 200);
        documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        assertFalse(documentResp.getAspectNames().contains("cm:versionable"));
        assertNull(documentResp.getProperties()); // no properties (ie. no "cm:versionLabel")

        // Updates - no versions
        for (int i = 1; i <= 3; i++)
        {
            cnt++;

            // Update the empty node's content - no version created
            content = "The quick brown fox jumps over the lazy dog " + cnt;
            documentResp = updateTextFile(docId, content, null);
            assertFalse(documentResp.getAspectNames().contains("cm:versionable"));
            assertNull(documentResp.getProperties()); // no properties (ie. no "cm:versionLabel")
        }

        // TODO add tests to also check version comment (when we can list version history)
    }

    /**
     * Test version history paging.
     *
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/versions}
     */
    @Test
    public void testVersionHistoryPaging() throws Exception
    {
        // create folder
        setRequestContext(user1);
        
        String f1Id = null;

        try
        {
            f1Id = createFolder(Nodes.PATH_MY, "testVersionHistoryPaging-f1").getId();
            
            String textContentSuffix = "Amazingly few discotheques provide jukeboxes ";
            String contentName = "content-1";

            int cnt = 6;
            Pair<String, String> pair = uploadTextFileVersions(user1, f1Id, contentName, cnt, textContentSuffix, 0, null, null);
            String versionLabel = pair.getFirst();
            String docId = pair.getSecond();

            assertEquals("1.5", versionLabel); // 1.0, 1.1, ... 1.5

            // check version history count (note: no paging => default max items => 100)
            HttpResponse response = getAll(getNodeVersionsUrl(docId), null, null, 200);
            List<Node> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(cnt, nodes.size());

            // Sanity Test paging
            
            // SkipCount=0,MaxItems=2
            Paging paging = getPaging(0, 2);
            response = getAll(getNodeVersionsUrl(docId), paging, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(2, nodes.size());
            PublicApiClient.ExpectedPaging expectedPaging = RestApiUtil.parsePaging(response.getJsonResponse());
            assertEquals(2, expectedPaging.getCount().intValue());
            assertEquals(0, expectedPaging.getSkipCount().intValue());
            assertEquals(2, expectedPaging.getMaxItems().intValue());
            assertTrue(expectedPaging.getTotalItems() >= cnt);
            assertTrue(expectedPaging.getHasMoreItems());

            // SkipCount=2,MaxItems=3
            paging = getPaging(2, 3);
            response = getAll(getNodeVersionsUrl(docId), paging, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(3, nodes.size());
            expectedPaging = RestApiUtil.parsePaging(response.getJsonResponse());
            assertEquals(3, expectedPaging.getCount().intValue());
            assertEquals(2, expectedPaging.getSkipCount().intValue());
            assertEquals(3, expectedPaging.getMaxItems().intValue());
            assertTrue(expectedPaging.getTotalItems() >= cnt);
        }
        finally
        {
            if (f1Id != null)
            {
                // some cleanup
                setRequestContext(user1);
                deleteNode(f1Id, true, 204);
            }
        }
    }

    @Override
    public String getScope()
    {
        return "public";
    }
}
