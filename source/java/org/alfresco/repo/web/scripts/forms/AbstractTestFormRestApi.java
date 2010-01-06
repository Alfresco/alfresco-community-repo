/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.web.scripts.forms;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

public abstract class AbstractTestFormRestApi extends BaseWebScriptTest
{
    protected static final String APPLICATION_JSON = "application/json";
    protected static final String TEST_FORM_DESCRIPTION = "Test form description";
    protected static final String TEST_FORM_TITLE = "Test form title";
    protected static final String FORM_DEF_URL = "/api/formdefinitions";
    protected String referencingNodeUpdateUrl;
    protected String containingNodeUpdateUrl;
    protected String containingNodeUrl;
    protected NodeRef referencingDocNodeRef;
    protected Map<QName, Serializable> refNodePropertiesAfterCreation;
    protected NodeRef associatedDoc_A;
    protected NodeRef associatedDoc_B;
    protected NodeRef associatedDoc_C;
    protected NodeRef associatedDoc_D;
    protected NodeRef associatedDoc_E;
    protected NodeRef childDoc_A;
    protected NodeRef childDoc_B;
    protected NodeRef childDoc_C;
    protected NodeRef childDoc_D;
    protected NodeRef childDoc_E;
    protected NodeRef testFolderNodeRef;

    protected NodeService nodeService;
    private FileFolderService fileFolderService;
    private ContentService contentService;
    private Repository repositoryHelper;
    protected NodeRef containerNodeRef;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        this.fileFolderService = (FileFolderService) getServer().getApplicationContext().getBean(
                "FileFolderService");
        this.contentService = (ContentService) getServer().getApplicationContext().getBean(
                "ContentService");
        this.repositoryHelper = (Repository) getServer().getApplicationContext().getBean(
                "repositoryHelper");
        this.nodeService = (NodeService) getServer().getApplicationContext().getBean("NodeService");

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

        NodeRef companyHomeNodeRef = this.repositoryHelper.getCompanyHome();

        String guid = GUID.generate();

        // Create a test file (not a folder)
        FileInfo referencingDoc = this.fileFolderService.create(companyHomeNodeRef,
                "referencingDoc" + guid + ".txt", ContentModel.TYPE_CONTENT);
        referencingDocNodeRef = referencingDoc.getNodeRef();

        // Add an aspect.
        Map<QName, Serializable> aspectProps = new HashMap<QName, Serializable>(2);
        aspectProps.put(ContentModel.PROP_TITLE, TEST_FORM_TITLE);
        aspectProps.put(ContentModel.PROP_DESCRIPTION, TEST_FORM_DESCRIPTION);
        nodeService.addAspect(referencingDocNodeRef, ContentModel.ASPECT_TITLED, aspectProps);

        // Write some content into the node.
        ContentWriter contentWriter = this.contentService.getWriter(referencingDoc.getNodeRef(),
                ContentModel.PROP_CONTENT, true);
        contentWriter.setEncoding("UTF-8");
        contentWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        contentWriter.putContent("The quick brown fox jumped over the lazy dog.");
        
        // Create a folder - will use this for child-association testing
        FileInfo associatedDocsFolder =
            this.fileFolderService.create(companyHomeNodeRef, "testFolder" + guid, ContentModel.TYPE_FOLDER);
        
        testFolderNodeRef = associatedDocsFolder.getNodeRef();
        
        this.associatedDoc_A = createTestNode("associatedDoc_A" + guid);
        this.associatedDoc_B = createTestNode("associatedDoc_B" + guid);
        this.associatedDoc_C = createTestNode("associatedDoc_C" + guid);
        this.associatedDoc_D = createTestNode("associatedDoc_D" + guid);
        this.associatedDoc_E = createTestNode("associatedDoc_E" + guid);

        // Now create associations between the referencing and the two node refs.
        aspectProps.clear();
        this.nodeService.addAspect(this.referencingDocNodeRef, ContentModel.ASPECT_REFERENCING, aspectProps);
        this.nodeService.createAssociation(this.referencingDocNodeRef, associatedDoc_A, ContentModel.ASSOC_REFERENCES);
        this.nodeService.createAssociation(this.referencingDocNodeRef, associatedDoc_B, ContentModel.ASSOC_REFERENCES);
        // Leave the 3rd, 4th and 5th nodes without associations as they may be created in
        // other test code.

        // Create a container for the children.
        HashMap<QName, Serializable> containerProps = new HashMap<QName, Serializable>();
        this.containerNodeRef = nodeService.createNode(companyHomeNodeRef, ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testContainer" + guid),
                ContentModel.TYPE_CONTAINER,
                containerProps).getChildRef();
        
        this.childDoc_A = createTestNode("childDoc_A" + guid);
        this.childDoc_B = createTestNode("childDoc_B" + guid);
        this.childDoc_C = createTestNode("childDoc_C" + guid);
        this.childDoc_D = createTestNode("childDoc_D" + guid);
        this.childDoc_E = createTestNode("childDoc_E" + guid);
        
        // Now create the pre-test child-associations.
        this.nodeService.addChild(containerNodeRef, childDoc_A, ContentModel.ASSOC_CHILDREN, QName.createQName("childA"));
        this.nodeService.addChild(containerNodeRef, childDoc_B, ContentModel.ASSOC_CHILDREN, QName.createQName("childB"));
        // The other childDoc nodes will be added as children over the REST API as part
        // of later test code.

        // Create and store the urls to the referencingNode
        StringBuilder builder = new StringBuilder();
        builder.append("/api/node/workspace/").append(referencingDocNodeRef.getStoreRef().getIdentifier())
                .append("/").append(referencingDocNodeRef.getId()).append("/formprocessor");
        this.referencingNodeUpdateUrl = builder.toString();
        
        builder = new StringBuilder();
        builder.append("/api/node/workspace/").append(containerNodeRef.getStoreRef().getIdentifier())
                .append("/").append(containerNodeRef.getId()).append("/formprocessor");
        this.containingNodeUpdateUrl = builder.toString();
        
        // Store the original properties of this node
        this.refNodePropertiesAfterCreation = nodeService.getProperties(referencingDocNodeRef);
        
        refNodePropertiesAfterCreation.toString();
    }

    @Override
    public void tearDown()
    {
        nodeService.deleteNode(this.referencingDocNodeRef);
        nodeService.deleteNode(this.associatedDoc_A);
        nodeService.deleteNode(this.associatedDoc_B);
        nodeService.deleteNode(this.associatedDoc_C);
        nodeService.deleteNode(this.associatedDoc_D);
        nodeService.deleteNode(this.associatedDoc_E);
        nodeService.deleteNode(this.childDoc_A);
        nodeService.deleteNode(this.childDoc_B);
        nodeService.deleteNode(this.childDoc_C);
        nodeService.deleteNode(this.childDoc_D);
        nodeService.deleteNode(this.childDoc_E);
        nodeService.deleteNode(this.testFolderNodeRef);
        nodeService.deleteNode(this.containerNodeRef);
    }

    protected Response sendGetReq(String url, int expectedStatusCode)
            throws IOException, UnsupportedEncodingException
    {
        Response result = sendRequest(new GetRequest(url), expectedStatusCode);
        return result;
    }

    protected NodeRef createTestNode(String associatedDocName)
    {
        Map<QName, Serializable> docProps = new HashMap<QName, Serializable>(1);
        docProps.put(ContentModel.PROP_NAME, associatedDocName + ".txt");
        return this.nodeService.createNode(
                    testFolderNodeRef, 
                    ContentModel.ASSOC_CONTAINS, 
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, associatedDocName + ".txt"), 
                    ContentModel.TYPE_CONTENT,
                    docProps).getChildRef();
    }
}