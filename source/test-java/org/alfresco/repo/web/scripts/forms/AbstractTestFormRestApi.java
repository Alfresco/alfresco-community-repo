/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
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
    protected TransactionService transactionService;

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
        this.transactionService = (TransactionService) getServer().getApplicationContext()
                .getBean("transactionService");

        this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>(){

            @Override
            public Void execute() throws Throwable
            {
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

                NodeRef companyHomeNodeRef = AbstractTestFormRestApi.this.repositoryHelper.getCompanyHome();

                String guid = GUID.generate();

                // Create a test file (not a folder)
                FileInfo referencingDoc = AbstractTestFormRestApi.this.fileFolderService.create(companyHomeNodeRef,
                        "referencingDoc" + guid + ".txt", ContentModel.TYPE_CONTENT);
                referencingDocNodeRef = referencingDoc.getNodeRef();

                // Add an aspect.
                Map<QName, Serializable> aspectProps = new HashMap<QName, Serializable>(2);
                aspectProps.put(ContentModel.PROP_TITLE, TEST_FORM_TITLE);
                aspectProps.put(ContentModel.PROP_DESCRIPTION, TEST_FORM_DESCRIPTION);
                nodeService.addAspect(referencingDocNodeRef, ContentModel.ASPECT_TITLED, aspectProps);

                // Write some content into the node.
                ContentWriter contentWriter = AbstractTestFormRestApi.this.contentService.getWriter(referencingDoc.getNodeRef(),
                        ContentModel.PROP_CONTENT, true);
                contentWriter.setEncoding("UTF-8");
                contentWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                contentWriter.putContent("The quick brown fox jumped over the lazy dog.");
                
                // Create a folder - will use this for child-association testing
                FileInfo associatedDocsFolder =
                    AbstractTestFormRestApi.this.fileFolderService.create(companyHomeNodeRef, "testFolder" + guid, ContentModel.TYPE_FOLDER);
                
                testFolderNodeRef = associatedDocsFolder.getNodeRef();
                
                AbstractTestFormRestApi.this.associatedDoc_A = createTestNode("associatedDoc_A" + guid);
                AbstractTestFormRestApi.this.associatedDoc_B = createTestNode("associatedDoc_B" + guid);
                AbstractTestFormRestApi.this.associatedDoc_C = createTestNode("associatedDoc_C" + guid);
                AbstractTestFormRestApi.this.associatedDoc_D = createTestNode("associatedDoc_D" + guid);
                AbstractTestFormRestApi.this.associatedDoc_E = createTestNode("associatedDoc_E" + guid);

                // Now create associations between the referencing and the two node refs.
                aspectProps.clear();
                AbstractTestFormRestApi.this.nodeService.addAspect(AbstractTestFormRestApi.this.referencingDocNodeRef, ContentModel.ASPECT_REFERENCING, aspectProps);
                AbstractTestFormRestApi.this.nodeService.createAssociation(AbstractTestFormRestApi.this.referencingDocNodeRef, associatedDoc_A, ContentModel.ASSOC_REFERENCES);
                AbstractTestFormRestApi.this.nodeService.createAssociation(AbstractTestFormRestApi.this.referencingDocNodeRef, associatedDoc_B, ContentModel.ASSOC_REFERENCES);
                // Leave the 3rd, 4th and 5th nodes without associations as they may be created in
                // other test code.

                // Create a container for the children.
                HashMap<QName, Serializable> containerProps = new HashMap<QName, Serializable>();
                AbstractTestFormRestApi.this.containerNodeRef = nodeService.createNode(companyHomeNodeRef, ContentModel.ASSOC_CONTAINS,
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testContainer" + guid),
                        ContentModel.TYPE_CONTAINER,
                        containerProps).getChildRef();
                
                AbstractTestFormRestApi.this.childDoc_A = createTestNode("childDoc_A" + guid);
                AbstractTestFormRestApi.this.childDoc_B = createTestNode("childDoc_B" + guid);
                AbstractTestFormRestApi.this.childDoc_C = createTestNode("childDoc_C" + guid);
                AbstractTestFormRestApi.this.childDoc_D = createTestNode("childDoc_D" + guid);
                AbstractTestFormRestApi.this.childDoc_E = createTestNode("childDoc_E" + guid);
                
                // Now create the pre-test child-associations.
                AbstractTestFormRestApi.this.nodeService.addChild(containerNodeRef, childDoc_A, ContentModel.ASSOC_CHILDREN, QName.createQName("childA"));
                AbstractTestFormRestApi.this.nodeService.addChild(containerNodeRef, childDoc_B, ContentModel.ASSOC_CHILDREN, QName.createQName("childB"));
                // The other childDoc nodes will be added as children over the REST API as part
                // of later test code.

                // Create and store the urls to the referencingNode
                StringBuilder builder = new StringBuilder();
                builder.append("/api/node/workspace/").append(referencingDocNodeRef.getStoreRef().getIdentifier())
                        .append("/").append(referencingDocNodeRef.getId()).append("/formprocessor");
                AbstractTestFormRestApi.this.referencingNodeUpdateUrl = builder.toString();
                
                builder = new StringBuilder();
                builder.append("/api/node/workspace/").append(containerNodeRef.getStoreRef().getIdentifier())
                        .append("/").append(containerNodeRef.getId()).append("/formprocessor");
                AbstractTestFormRestApi.this.containingNodeUpdateUrl = builder.toString();
                
                // Store the original properties of this node
                AbstractTestFormRestApi.this.refNodePropertiesAfterCreation = nodeService.getProperties(referencingDocNodeRef);
                
                refNodePropertiesAfterCreation.toString();
                return null;
            }});
    }

    @Override
    public void tearDown()
    {
        this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>(){

            @Override
            public Void execute() throws Throwable
            {
                nodeService.deleteNode(AbstractTestFormRestApi.this.referencingDocNodeRef);
                nodeService.deleteNode(AbstractTestFormRestApi.this.associatedDoc_A);
                nodeService.deleteNode(AbstractTestFormRestApi.this.associatedDoc_B);
                nodeService.deleteNode(AbstractTestFormRestApi.this.associatedDoc_C);
                nodeService.deleteNode(AbstractTestFormRestApi.this.associatedDoc_D);
                nodeService.deleteNode(AbstractTestFormRestApi.this.associatedDoc_E);
                nodeService.deleteNode(AbstractTestFormRestApi.this.childDoc_A);
                nodeService.deleteNode(AbstractTestFormRestApi.this.childDoc_B);
                nodeService.deleteNode(AbstractTestFormRestApi.this.childDoc_C);
                nodeService.deleteNode(AbstractTestFormRestApi.this.childDoc_D);
                nodeService.deleteNode(AbstractTestFormRestApi.this.childDoc_E);
                nodeService.deleteNode(AbstractTestFormRestApi.this.testFolderNodeRef);
                nodeService.deleteNode(AbstractTestFormRestApi.this.containerNodeRef);
                return null;
            }});
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