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
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.rest.AbstractSingleNetworkSiteTest;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.data.ContentInfo;
import org.alfresco.rest.api.tests.client.data.Document;
import org.alfresco.rest.api.tests.client.data.Node;
import org.alfresco.rest.api.tests.client.data.Rendition;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import static org.alfresco.rest.api.tests.util.RestApiUtil.toJsonAsString;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.*;

/**
 * V1 REST API tests for Node Version Renditions
 *
 * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/{nodeId}/versions/{versionId}/renditions</li>
 *
 * @author janv
 */
public class NodeVersionRenditionsApiTest extends AbstractSingleNetworkSiteTest
{
    private final static long DELAY_IN_MS = 500;

    private static final List<String> DEFAULT_RENDITIONS_FOR_TXT =
            new ArrayList<>(List.of("avatar", "avatar32", "doclib", "imgpreview", "medium", "pdf"));

    /**
     * Upload some versions and then create and retrieve version renditions
     *
     * <p>POST:</p>
     * @literal <host>:<port>/alfresco/api/-default-/public/alfresco/versions/1/nodes/<nodeId>/versions/<versionId>/renditions}
     *
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/versions/<versionId>/renditions}
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/versions/<versionId>/renditions/<renditionId>}
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/versions/<versionId>/renditions/<renditionId>/content}
     *
     * @throws Exception
     */
    @Test
    public void testUpFileVersionRenditions() throws Exception
    {
        setRequestContext(user1);
        
        String myFolderNodeId = getMyNodeId();

        // create folder
        String f1Id = createFolder(myFolderNodeId, "f1").getId();

        try
        {
            int verCnt = 1;
            int cnt = 1;
            String versionLabel = "1.0";

            String textContentSuffix = "Amazingly few discotheques provide jukeboxes ";
            String contentName = "content-2-" + System.currentTimeMillis();
            String content = textContentSuffix + cnt;

            // request minor version on upload (& no pre-request for renditions for live node)
            Boolean majorVersion = true;
            Map<String, String> params = new HashMap<>();
            params.put("majorVersion", majorVersion.toString());

            // create a new file
            Document documentResp = createTextFile(f1Id, contentName, content, "UTF-8", params);
            String docId = documentResp.getId();
            assertTrue(documentResp.getAspectNames().contains("cm:versionable"));
            assertNotNull(documentResp.getProperties());
            assertEquals(versionLabel, documentResp.getProperties().get("cm:versionLabel"));

            cnt = 2;
            versionLabel = updateFileVersions(user1, docId, cnt, textContentSuffix, verCnt, majorVersion, versionLabel);
            verCnt = verCnt+cnt;

            assertEquals("3.0", versionLabel);
            assertEquals(3, verCnt);

            // check version history count
            HttpResponse response = getAll(getNodeVersionsUrl(docId), null, null, 200);
            List<Node> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(verCnt, nodes.size());

            // pause briefly
            Thread.sleep(DELAY_IN_MS);

            checkCreateAndGetVersionRendition(docId, "1.0", "doclib");
            checkCreateAndGetVersionRendition(docId, "3.0", "doclib");
            checkCreateAndGetVersionRendition(docId, "2.0", "doclib");

            // also live node
            checkCreateAndGetVersionRendition(docId, null, "doclib");
        }
        finally
        {
            // some cleanup
            setRequestContext(user1);
            deleteNode(f1Id, true, 204);
        }
    }

    @Test
    public void testUpFileVersionRenditionsWithDoclib() throws Exception
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

            // request minor version on upload & also pre-request "doclib" rendition (for live node)
            Boolean majorVersion = false;
            Map<String, String> params = new HashMap<>();
            params.put("majorVersion", majorVersion.toString());
            params.put("renditions", "doclib");

            // create a new file
            Document documentResp = createTextFile(f1Id, contentName, content, "UTF-8", params);
            String docId = documentResp.getId();
            assertTrue(documentResp.getAspectNames().contains("cm:versionable"));
            assertNotNull(documentResp.getProperties());
            assertEquals(versionLabel, documentResp.getProperties().get("cm:versionLabel"));

            // check live node
            checkAndGetVersionRendition(docId, null, "doclib");

            cnt = 1;
            versionLabel = updateFileVersions(user1, docId, cnt, textContentSuffix, verCnt, majorVersion, versionLabel);
            verCnt = verCnt+cnt;

            assertEquals("0.2", versionLabel);
            assertEquals(2, verCnt);

            // check version history count
            HttpResponse response = getAll(getNodeVersionsUrl(docId), null, null, 200);
            List<Node> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(verCnt, nodes.size());

            // pause briefly
            Thread.sleep(DELAY_IN_MS);

            checkCreateAndGetVersionRendition(docId, "0.2", "doclib");

            // check live node
            checkAndGetVersionRendition(docId, null, "doclib");
        }
        finally
        {
            // some cleanup
            setRequestContext(user1);
            deleteNode(f1Id, true, 204);
        }
    }

    @Test
    public void testNegative() throws Exception
    {
        AuthenticationUtil.clearCurrentSecurityContext();
        setRequestContext(null);

        // -ve: not authenticated
        getAll(getNodeVersionRenditionsUrl("dummy", "1.0"), null, 401);
        getSingle(getNodeVersionRenditionsUrl("dummy", "1.0"), ("doclib"), null, 401);
        getSingle(getNodeVersionRenditionsUrl("dummy", "1.0"), ("doclib/content"), null, 401);

        Rendition renditionRequest = new Rendition();
        renditionRequest.setId("doclib");
        post(getNodeVersionRenditionsUrl("dummy", "1.0"), toJsonAsString(renditionRequest), 401);

        setRequestContext(user1);

        String myFolderNodeId = getMyNodeId();

        // create folder
        String f1Id = createFolder(myFolderNodeId, "f1").getId();

        try
        {
            int verCnt = 1;
            int cnt = 1;
            String versionLabel = "1.0";

            String textContentSuffix = "Amazingly few discotheques provide jukeboxes ";
            String contentName = "content-2-" + System.currentTimeMillis();
            String content = textContentSuffix + cnt;

            // create a new file
            Document documentResp = createTextFile(f1Id, contentName, content, "UTF-8", null);
            String docId = documentResp.getId();

            getAll(getNodeVersionRenditionsUrl(docId, "1.0"), null, 200);
            checkCreateAndGetVersionRendition(docId, "1.0", "doclib");


            // -ve: rendition already exits (409)
            renditionRequest.setId("doclib");
            post(getNodeVersionRenditionsUrl(docId, "1.0"), toJsonAsString(renditionRequest), 409);

            // -ve: no such rendition (404)
            getSingle(getNodeVersionRenditionsUrl("dummy", "1.0"), ("dummy"), null, 404);
            getSingle(getNodeVersionRenditionsUrl("dummy", "1.0"), ("dummy/content"), null, 404);

            renditionRequest.setId("dummy");
            post(getNodeVersionRenditionsUrl("dummy", "1.0"), toJsonAsString(renditionRequest), 404);


            // -ve: no such version (404)
            getAll(getNodeVersionRenditionsUrl(docId, "4.0"), null, 404);
            getSingle(getNodeVersionRenditionsUrl(docId, "4.0"), ("doclib"), null, 404);
            getSingle(getNodeVersionRenditionsUrl(docId, "4.0"), ("doclib/content"), null, 404);

            renditionRequest.setId("doclib");
            post(getNodeVersionRenditionsUrl(docId, "4.0"), toJsonAsString(renditionRequest), 404);


            // -ve: no such file (404)
            getAll(getNodeVersionRenditionsUrl("dummy", "1.0"), null, 404);
            getSingle(getNodeVersionRenditionsUrl("dummy", "1.0"), ("doclib"), null, 404);
            getSingle(getNodeVersionRenditionsUrl("dummy", "1.0"), ("doclib/content"), null, 404);

            renditionRequest.setId("doclib");
            post(getNodeVersionRenditionsUrl("dummy", "1.0"), toJsonAsString(renditionRequest), 404);
        }
        finally
        {
            // some cleanup
            setRequestContext(user1);
            deleteNode(f1Id, true, 204);
        }
    }

    private void checkCreateAndGetVersionRendition(String docId, String versionId, String renditionId) throws Exception
    {
        String getRenditionsUrl;
        if ((versionId != null) && (! versionId.isEmpty()))
        {
            getRenditionsUrl = getNodeVersionRenditionsUrl(docId, versionId);
        }
        else
        {
            getRenditionsUrl = getNodeRenditionsUrl(docId);
        }

        // List renditions for version
        Paging paging = getPaging(0, 50);
        HttpResponse response = getAll(getRenditionsUrl, paging, 200);
        List<Rendition> renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        assertTrue(renditions.size() == DEFAULT_RENDITIONS_FOR_TXT.size());

        for (Rendition rendition : renditions)
        {
            assertEquals(Rendition.RenditionStatus.NOT_CREATED, rendition.getStatus());
            assertTrue(DEFAULT_RENDITIONS_FOR_TXT.contains(rendition.getId()));
        }

        // Get rendition (not created yet) information for node
        response = getSingle(getRenditionsUrl, renditionId, 200);
        Rendition rendition = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Rendition.class);
        assertNotNull(rendition);
        assertEquals(Rendition.RenditionStatus.NOT_CREATED, rendition.getStatus());

        // -ve test: try to download non-existent rendition (and no placeholder)
        Map<String, String> params = new HashMap<>();
        params.put("placeholder", "false");
        getSingle(getRenditionsUrl, (renditionId+"/content"), params, 404);

        // +ve test: download placeholder instead
        params = new HashMap<>();
        params.put("placeholder", "true");
        response = getSingle(getRenditionsUrl, (renditionId+"/content"), params, 200);
        assertNotNull(response.getResponseAsBytes());

        // Create and get version rendition
        rendition = createAndGetRendition(docId, versionId, renditionId);
        assertNotNull(rendition);
        assertEquals(Rendition.RenditionStatus.CREATED, rendition.getStatus());
        ContentInfo contentInfo = rendition.getContent();
        assertNotNull(contentInfo);
        assertEquals(MimetypeMap.MIMETYPE_IMAGE_PNG, contentInfo.getMimeType());
        assertEquals("PNG Image", contentInfo.getMimeTypeName());
        assertNotNull(contentInfo.getEncoding());
        assertTrue(contentInfo.getSizeInBytes() > 0);
    }

    private void checkAndGetVersionRendition(String docId, String versionId, String renditionId) throws Exception
    {
        String getRenditionsUrl;
        if ((versionId != null) && (! versionId.isEmpty()))
        {
            getRenditionsUrl = getNodeVersionRenditionsUrl(docId, versionId);
        }
        else
        {
            getRenditionsUrl = getNodeRenditionsUrl(docId);
        }

        // List renditions for version
        Paging paging = getPaging(0, 50);
        HttpResponse response = getAll(getRenditionsUrl, paging, 200);
        List<Rendition> renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        assertTrue(renditions.size() == DEFAULT_RENDITIONS_FOR_TXT.size());

        for (Rendition rendition : renditions)
        {
            assertTrue(DEFAULT_RENDITIONS_FOR_TXT.contains(rendition.getId()));
        }

        // Get version rendition
        Rendition rendition = waitAndGetRendition(docId, versionId, renditionId);
        assertNotNull(rendition);
        assertEquals(Rendition.RenditionStatus.CREATED, rendition.getStatus());
        ContentInfo contentInfo = rendition.getContent();
        assertNotNull(contentInfo);
        assertEquals(MimetypeMap.MIMETYPE_IMAGE_PNG, contentInfo.getMimeType());
        assertEquals("PNG Image", contentInfo.getMimeTypeName());
        assertNotNull(contentInfo.getEncoding());
        assertTrue(contentInfo.getSizeInBytes() > 0);
    }

    @Override
    public String getScope()
    {
        return "public";
    }
}
