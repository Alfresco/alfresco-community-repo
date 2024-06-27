/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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
import org.alfresco.rest.api.model.ContentInfo;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.NodeTarget;
import org.alfresco.rest.api.model.Site;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.alfresco.rest.api.tests.util.RestApiUtil.toJsonAsStringNonNull;
import static org.junit.Assert.assertNotNull;

/**
 * V1 REST API tests for Folder size
 */
@FixMethodOrder (MethodSorters.NAME_ASCENDING)
public class NodeFolderSizeApiTest extends AbstractBaseApiTest
{

    /**
     * Private site of user two from network one.
     */
    private Site userOneN1Site;

    protected PermissionService permissionService;

    private NodeService nodeService;

    private MimetypeService mimeTypeService;

    private String folderName;

    private static  String folderId;

    /**
     * The logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(NodeFolderSizeApiTest.class);

    private String addToDocumentLibrary(Site testSite, String name, String nodeType)
    {
        String parentId;
        try
        {
            parentId = getSiteContainerNodeId(testSite.getId(), "documentLibrary");
            return createNode(parentId, name, nodeType, null).getId();
        }
        catch (Exception e)
        {
            LOG.error("Exception occured in NodeFolderSizeApiTest:addToDocumentLibrary {}", e.getMessage());
        }
        return null;
    }

    @Before
    public void setup() throws Exception
    {
        super.setup();
        permissionService = applicationContext.getBean("permissionService", PermissionService.class);
        nodeService = applicationContext.getBean("NodeService", NodeService.class);
        mimeTypeService = applicationContext.getBean("MimetypeService", MimetypeService.class);
    }

    /**
     * Test case for POST/calculateSize, which calculates Folder Size.
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/calculateSize}
     */
    @Test
    public void testAPostCalculateFolderSize() throws Exception
    {
        setRequestContext(user1);

        String siteTitle = "RandomSite" + System.currentTimeMillis();
        this.userOneN1Site = createSite("RN"+RUNID, siteTitle, siteTitle, SiteVisibility.PRIVATE, 201);

        // Create a folder within the site document's library.
        this.folderName = "folder" + System.currentTimeMillis();
        this.folderId = addToDocumentLibrary(userOneN1Site, folderName, TYPE_CM_FOLDER);

        Map<String, String> params = new HashMap<>();
        params.put("nodeId",folderId);
        params.put("maxItems","100");

        HttpResponse response = post(getFolderSizeUrl(folderId), toJsonAsStringNonNull(params), 202);
        Object document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Object.class);
        String contentNodeId = document.toString();
        assertNotNull(contentNodeId);
    }

    /**
     * Test case to trigger POST/calculateSize with >200 Children Nodes.
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/calculateSize}
     */
    @Test
    public void testPerformance() throws Exception
    {
        setRequestContext(user1);
        Node parentNodes;

        // Logging initial time.
        LocalDateTime eventTimestamp = LocalDateTime.now();
        String formattedTimestamp = eventTimestamp.format(DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss"));
        LOG.info(" ********** In NodeFolderSizeApiTest:testPerformance Initial Time :{}", formattedTimestamp);

        String siteTitle = "RandomSite" + System.currentTimeMillis();
        userOneN1Site = createSite("RN"+RUNID, siteTitle, siteTitle, SiteVisibility.PRIVATE, 201);

        // Create a folder within the site document's library.
        String folderName = "folder" + System.currentTimeMillis();
        String folderId = addToDocumentLibrary(userOneN1Site, folderName, TYPE_CM_FOLDER);
        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,folderId);
        QName qName =  QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(nodeRef.getId()));

        for(int i =0;i<300;i++)
        {
            parentNodes = new Node();
            parentNodes.setName("c1" + RUNID);
            parentNodes.setNodeType(TYPE_CM_FOLDER);
            QName assocChildQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(parentNodes.getName()));

            if(i%7==0)
            {
                for(int j =0 ; j<=5; j++)
                {
                    Node childNodes = new Node();
                    childNodes.setName("c2" + RUNID);
                    childNodes.setNodeType(TYPE_CM_CONTENT);
                    ContentData contentData = new ContentData(null, MimetypeMap.MIMETYPE_TEXT_PLAIN, 10L, null);
                    String mimeType = contentData.getMimetype();
                    String mimeTypeName = mimeTypeService.getDisplaysByMimetype().get(mimeType);
                    ContentInfo contentInfo = new ContentInfo(mimeType, mimeTypeName, contentData.getSize(),contentData.getEncoding());
                    childNodes.setContent(contentInfo);
                    QName assocChildQNameInternal = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(childNodes.getName()));
                    nodeService.addChild(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,parentNodes.getNodeId()), new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, childNodes.getName()), qName, assocChildQNameInternal);
                }
            }
            nodeService.addChild(nodeRef, new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, parentNodes.getName()), qName, assocChildQName);
        }
        Map<String, String> params = new HashMap<>();
        params.put("nodeId",folderId);
        params.put("maxItems","100");

        HttpResponse response = post(getFolderSizeUrl(folderId), toJsonAsStringNonNull(params), 202);
        Object document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Object.class);
        String contentNodeId = document.toString();
        if(contentNodeId != null)
        {
            eventTimestamp = LocalDateTime.now();
            formattedTimestamp = eventTimestamp.format(DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss"));
            LOG.info(" ********** In NodeFolderSizeApiTest:testPerformance Completed Time :{}", formattedTimestamp);
        }
        assertNotNull(contentNodeId);
    }

    /**
     * Test case for GET/calculateSize, to retrieve FolderSize.
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/calculateSize}
     */
    @Test
    public void testBGetCalculateFolderSize() throws Exception
    {
        setRequestContext(user1);

        AuthenticationUtil.setFullyAuthenticatedUser(user1);

        HttpResponse response = getSingle(getFolderSizeUrl(this.folderId), null, 200);
        Object document = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Object.class);
        String contentNodeId = document.toString();
        assertNotNull(contentNodeId);
    }

    /**
     * Test case for others HTTP status codes.
     */
    @Test
    public void testHTTPStatus() throws Exception
    {

        setRequestContext(user1);
        String siteTitle = "RandomSite" + System.currentTimeMillis();
        userOneN1Site = createSite("RN"+RUNID, siteTitle, siteTitle, SiteVisibility.PRIVATE, 201);

        // Create a folder within the site document's library.
        String folderName = "folder" + System.currentTimeMillis();
        String folderId = addToDocumentLibrary(userOneN1Site, folderName, TYPE_CM_FOLDER);

        setRequestContext(null);
        delete(getFolderSizeUrl(folderId), folderId, null, 401);

        setRequestContext(user1);
        NodeTarget tgt = new NodeTarget();
        tgt.setTargetParentId(folderId);
        HttpResponse response = post(getFolderSizeUrl(UUID.randomUUID().toString()), toJsonAsStringNonNull(tgt), null, 404);
        assertNotNull(response);
    }

    @After
    public void tearDown() throws Exception
    {
        deleteSite(userOneN1Site.getId(), true, 204);
    }

    @Override
    public String getScope()
    {
        return "public";
    }
}
