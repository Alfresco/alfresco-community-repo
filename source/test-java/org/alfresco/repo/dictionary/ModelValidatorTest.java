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
package org.alfresco.repo.dictionary;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * 
 * @author sglover
 *
 */
public class ModelValidatorTest
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private String testNamespace;
    private String modelName;

    private ModelValidator modelValidator;
    private DictionaryDAO dictionaryDAO;
    private QNameDAO qnameDAO;
    private NamespaceDAO namespaceDAO;
    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private ContentService contentService;
    private VersionService versionService;
    private TransactionService transactionService;
    private NodeArchiveService nodeArchiveService;

    private M2Model model;
    private QName modelQName;
    private QName typeQName;
    private M2Type type;
    private QName propertyQName;
    private M2Property property;

    @Before
    public void setUp() throws Exception
    {
        this.modelValidator = (ModelValidator)ctx.getBean("modelValidator");
        this.dictionaryDAO = (DictionaryDAO)ctx.getBean("dictionaryDAO");
        this.qnameDAO = (QNameDAO)ctx.getBean("qnameDAO");
        this.namespaceDAO = (NamespaceDAO)ctx.getBean("namespaceDAO");
        this.nodeService = (NodeService)ctx.getBean("NodeService");
        this.fileFolderService = (FileFolderService)ctx.getBean("FileFolderService");
        this.contentService = (ContentService)ctx.getBean("contentService");
        this.versionService = (VersionService)ctx.getBean("VersionService");
        this.transactionService = (TransactionService)ctx.getBean("TransactionService");
        this.nodeArchiveService = (NodeArchiveService)ctx.getBean("nodeArchiveService");

        this.modelName = "modelvalidatortest" + System.currentTimeMillis();
        addModel();
    }

    private QName addModel()
    {
        this.testNamespace = "http://www.alfresco.org/test/" + modelName;
        this.modelQName = QName.createQName(testNamespace, modelName);

        // Create a model
        this.model = M2Model.createModel(modelName + ":" + modelName);
        model.createNamespace(testNamespace, modelName);
        model.createImport(NamespaceService.DICTIONARY_MODEL_1_0_URI, "d");
        model.createImport(NamespaceService.CONTENT_MODEL_1_0_URI, NamespaceService.CONTENT_MODEL_PREFIX);

        this.typeQName = QName.createQName(testNamespace, "type1");
        this.type = model.createType(modelName + ":" + typeQName.getLocalName());
        type.setParentName("cm:folder");

        this.propertyQName = QName.createQName(testNamespace, "prop1");
        this.property = type.createProperty(modelName + ":" + propertyQName.getLocalName());
        property.setType("d:text");

        dictionaryDAO.putModel(model);

        return modelQName;
    }

    private NodeRef getParentNodeRef()
    {
        NodeRef rootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        // create temporary folder
        NodeRef parentNodeRef = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN,
                QName.createQName(NamespaceService.ALFRESCO_URI, "working root" + GUID.generate()),
                ContentModel.TYPE_FOLDER).getChildRef();
        return parentNodeRef;
    }

    /**
     * Test that a model cannot be deleted if nodes and properties exist that reference it.
     * 
     * @throws Exception
     */
    @Test
    public void testInvalidModelDelete() throws Exception
    {
        // authenticate
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        final QName modelQName = QName.createQName(testNamespace, modelName);

        // Create a node that uses the new type (type is created in setUp)
        RetryingTransactionCallback<NodeRef> createNodeCallback = new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                // Create a node that uses it
                NodeRef parentNodeRef = getParentNodeRef();
                FileInfo folder = fileFolderService.create(parentNodeRef, GUID.generate(), ContentModel.TYPE_FOLDER);
                assertNotNull(folder);
                NodeRef folderNodeRef = folder.getNodeRef();
                FileInfo node = fileFolderService.create(folderNodeRef, GUID.generate(), typeQName);
                assertNotNull(node);
                NodeRef nodeRef = node.getNodeRef();

                ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
                writer.putContent("Test");

                Map<QName, Serializable> properties = new HashMap<>();
                properties.put(propertyQName, "Test");
                nodeService.setProperties(nodeRef, properties);

                versionService.createVersion(nodeRef, null);

                return nodeRef;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(createNodeCallback, false, true);

        // try to delete the model
        RetryingTransactionCallback<Void> deleteModelCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                if(modelValidator.canDeleteModel(modelQName))
                {
                    fail("Model delete should have failed");
                }

                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(deleteModelCallback, false, true);
    }

    /**
     * Tests that a model cannot be deleted/made inactive when there are any archived nodes
     * still in the repository that use it.
     * 
     * Test case for MNT-13820
     * 
     * @throws Exception
     */
    @Test
    public void testInvalidModelDeleteArchivedNode() throws Exception
    {
        // authenticate
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        final QName modelQName = QName.createQName(testNamespace, modelName);

        // Create a node that uses the new type (type is created in setUp)
        RetryingTransactionCallback<NodeRef> createNodeCallback = new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                // Create a node that uses it
                NodeRef parentNodeRef = getParentNodeRef();
                FileInfo folder = fileFolderService.create(parentNodeRef, GUID.generate(), ContentModel.TYPE_FOLDER);
                assertNotNull(folder);
                NodeRef folderNodeRef = folder.getNodeRef();
                FileInfo node = fileFolderService.create(folderNodeRef, GUID.generate(), typeQName);
                assertNotNull(node);
                NodeRef nodeRef = node.getNodeRef();

                ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
                writer.putContent("Test");

                Map<QName, Serializable> properties = new HashMap<>();
                properties.put(propertyQName, "Test");
                nodeService.setProperties(nodeRef, properties);

                versionService.createVersion(nodeRef, null);

                return nodeRef;
            }
        };
        final NodeRef nodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(createNodeCallback, false, true);

        RetryingTransactionCallback<Void> deleteNodeCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                nodeService.deleteNode(nodeRef);

                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(deleteNodeCallback, false, true);

        // try to delete the model
        RetryingTransactionCallback<Void> deleteModelCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                if(modelValidator.canDeleteModel(modelQName))
                {
                    fail("Model delete should have failed");
                }
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(deleteModelCallback, false, true);
    }

    /**
     * Tests that a model can be deleted when nodes are deleted.
     * 
     * Test case for MNT-13820
     * 
     * @throws Exception
     */
    @Test
    public void testModelDelete() throws Exception
    {
        // authenticate
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        final QName modelQName = QName.createQName(testNamespace, modelName);

        // Create a node that uses the new type (type is created in setUp)
        RetryingTransactionCallback<NodeRef> createNodeCallback = new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                // Create a node that uses it
                NodeRef parentNodeRef = getParentNodeRef();
                FileInfo folder = fileFolderService.create(parentNodeRef, GUID.generate(), ContentModel.TYPE_FOLDER);
                assertNotNull(folder);
                NodeRef folderNodeRef = folder.getNodeRef();
                FileInfo node = fileFolderService.create(folderNodeRef, GUID.generate(), typeQName);
                assertNotNull(node);
                NodeRef nodeRef = node.getNodeRef();

                ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
                writer.putContent("Test");

                Map<QName, Serializable> properties = new HashMap<>();
                properties.put(propertyQName, "Test");
                nodeService.setProperties(nodeRef, properties);

                versionService.createVersion(nodeRef, null);

                return nodeRef;
            }
        };
        final NodeRef nodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(createNodeCallback, false, true);

        RetryingTransactionCallback<Void> deleteNodeCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                nodeService.addAspect(nodeRef, ContentModel.ASPECT_TEMPORARY, null);
                nodeService.deleteNode(nodeRef);

                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(deleteNodeCallback, false, true);

        // try to delete the model
        RetryingTransactionCallback<Void> deleteModelCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                if(!modelValidator.canDeleteModel(modelQName))
                {
                    fail("Model delete should have succeeded");
                }

                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(deleteModelCallback, false, true);

        // Check that the qnames are still there
        // try to delete the model
        RetryingTransactionCallback<Void> checkQNamesCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                assertNotNull(qnameDAO.getQName(propertyQName));
                assertNotNull(qnameDAO.getQName(typeQName));
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(checkQNamesCallback, false, true);
    }

    /**
     * Test that a model cannot be deleted if there are node properties referencing it.
     * 
     * @throws Exception
     */
    @Test
    public void testInvalidPropertyDelete() throws Exception
    {
        // authenticate
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        // Create a node that uses the new type
        RetryingTransactionCallback<NodeRef> createNodeCallback = new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                // Create a node that uses it
                NodeRef parentNodeRef = getParentNodeRef();
                FileInfo folder = fileFolderService.create(parentNodeRef, GUID.generate(), ContentModel.TYPE_FOLDER);
                assertNotNull(folder);
                NodeRef folderNodeRef = folder.getNodeRef();
                FileInfo node = fileFolderService.create(folderNodeRef, GUID.generate(), typeQName);
                assertNotNull(node);
                NodeRef nodeRef = node.getNodeRef();

                ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
                writer.putContent("Test");

                Map<QName, Serializable> properties = new HashMap<>();
                properties.put(propertyQName, "Test");
                nodeService.setProperties(nodeRef, properties);

                versionService.createVersion(nodeRef, null);

                return nodeRef;
            }
        };
        final NodeRef nodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(createNodeCallback, false, true);

        RetryingTransactionCallback<Void> deleteModelCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                type.removeProperty(modelName + ":" + propertyQName.getLocalName());
                CompiledModel compiledModel = model.compile(dictionaryDAO, namespaceDAO, true);
                try
                {
                    modelValidator.validateModel(compiledModel);
                    fail("Property delete should have failed");
                }
                catch(ModelInUseException e)
                {
                    System.out.println("help");
                    // ok
                }
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(deleteModelCallback, false, true);

        // delete the node that is using properties in the model
        RetryingTransactionCallback<Void> deleteNodeCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                nodeService.deleteNode(nodeRef);

                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(deleteNodeCallback, false, true);

        // make sure that the archive store is purged
        RetryingTransactionCallback<Void> purgeArchiveCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                nodeArchiveService.purgeAllArchivedNodes(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(purgeArchiveCallback, false, true);

        // try to delete model again - should work now
        RetryingTransactionCallback<Void> deleteModelAgainCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                type.removeProperty(modelName + ":" + propertyQName.getLocalName());
                CompiledModel compiledModel = model.compile(dictionaryDAO, namespaceDAO, true);
                modelValidator.validateModel(compiledModel);

                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(deleteModelAgainCallback, false, true);
    }
}
