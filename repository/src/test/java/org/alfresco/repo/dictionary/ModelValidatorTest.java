/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.dictionary;

import static org.junit.Assert.assertEquals;
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
import org.alfresco.service.cmr.dictionary.CustomModelDefinition;
import org.alfresco.service.cmr.dictionary.CustomModelException;
import org.alfresco.service.cmr.dictionary.CustomModelService;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.NamespaceDefinition;
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
import org.alfresco.util.Pair;
import org.junit.AfterClass;
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
    private static final String TEST_MODEL_URI_PART1 = "http://www.alfresco.org/model/testmodelvalidatoramespace";
    private static final String TEST_MODEL_URI_PART2 = "/1.0";
    private static final String TEST_MODEL_DESC = "This is test custom model desc";
    private static final String TEST_MODEL_AUTHOR = "John Doe";

    private ApplicationContext ctx;

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
    private CustomModelService customModelService;

    private M2Model model;
    private QName modelQName;
    private QName typeQName;
    private M2Type type;
    private QName propertyQName;
    private M2Property property;

    @Before
    public void setUp() throws Exception
    {
        ctx = ApplicationContextHelper.getApplicationContext();
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
        this.customModelService = (CustomModelService)ctx.getBean("customModelService");

        this.modelName = "modelvalidatortest" + System.currentTimeMillis();
        addModel();
    }

    @AfterClass
    public static void cleanUp()
    {
        AuthenticationUtil.clearCurrentSecurityContext();
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

    /**
     * Tests that an unused imported namespace can be deleted.
     *
     * @throws Exception
     */
    @Test
    public void testDeleteNamespace() throws Exception
    {
        // authenticate
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        // Remove the only property (created in setup method)
        this.type.removeProperty(this.property.getName());
        // We don't have any property that references the imported dictionary namespace, so remove it.
        this.model.removeImport(NamespaceService.DICTIONARY_MODEL_1_0_URI);

        // Check that it compiles
        CompiledModel compiledModel = model.compile(dictionaryDAO, namespaceDAO, true);
        modelValidator.validateModel(compiledModel);

        // Remove the imported content model namespace
        this.model.removeImport(NamespaceService.CONTENT_MODEL_1_0_URI);
        try
        {
            model.compile(dictionaryDAO, namespaceDAO, true);
            fail("Should have failed as the model's type references the content model (cm:folder).");
        }
        catch (DictionaryException dx)
        {
            //expected
        }

        // Add the content model namespace back
        model.createImport(NamespaceService.CONTENT_MODEL_1_0_URI, NamespaceService.CONTENT_MODEL_PREFIX);
        model.compile(dictionaryDAO, namespaceDAO, true);

        // Remove the defined namespace
        this.model.removeNamespace(testNamespace);
        try
        {
            model.compile(dictionaryDAO, namespaceDAO, true);
            fail("Should have failed as the type's name references the namespace.");
        }
        catch (DictionaryException dx)
        {
            //expected
        }
    }

    /**
     * For ACS-701
     * Tests that validating the model namespace prefix succeeds for both inactive and active models with a
     * unique namespace prefix.
     */
    @Test
    public void testValidatePrefixForModelWithValidPrefix() throws Exception
    {
        // authenticate
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        // create and validate models
        final String inactiveModelName = "testCustomModel1" + System.currentTimeMillis();
        final String activeModelName = "testCustomModel2" + System.currentTimeMillis();

        // create inactive model with unique prefix
        final String inactiveModelPrefix = "testmodelvalidatorpfx1" + "-" + System.currentTimeMillis();
        createAndVerifyTestModel(inactiveModelName, inactiveModelPrefix, false);

        // create active model with unique preifx
        final String activeModelPrefix = "testmodelvalidatorpfx2" + "-" + System.currentTimeMillis();
        createAndVerifyTestModel(activeModelName, activeModelPrefix, true);

        // validate model prefixes
        validatePrefix(inactiveModelName);
        validatePrefix(activeModelName);
    }

    /**
     * For ACS-701
     * Tests that validating the model namespace prefix throws an error for a model with
     *  a prefix in use by another model.
     */
    @Test
    public void testValidatePrefixForModelWithDuplicatePrefix() throws Exception
    {
        // authenticate
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        // create and validate models
        final String customModel1Name = "testCustomModel1" + System.currentTimeMillis();
        final String customModel2Name = "testCustomModel2" + System.currentTimeMillis();
        final String modelPrefix = "acs701-prefix01" + "-" + System.currentTimeMillis();

        // create model with unique prefix
        createAndVerifyTestModel(customModel1Name, modelPrefix, false);
        validatePrefix(customModel1Name);

        try
        {
            // create model with duplicate prefix
            createAndVerifyTestModel(customModel2Name, modelPrefix, true);
            fail("Expected a CustomModelException for model with a duplicate namespace prefix");
        }
        catch (CustomModelException ex)
        {
            // expected exception
        }
    }

    private void createAndVerifyTestModel(String testModelName, String prefix, boolean activate)
    {
        // Create the M2Model
        String uri = TEST_MODEL_URI_PART1 + System.currentTimeMillis() + TEST_MODEL_URI_PART2;
        Pair<String, String> namespacePair = new Pair<String, String>(uri, prefix);
        M2Model model = M2Model.createModel(namespacePair.getSecond() + QName.NAMESPACE_PREFIX + testModelName);
        model.createNamespace(namespacePair.getFirst(), namespacePair.getSecond());
        model.setDescription(TEST_MODEL_DESC);
        model.setAuthor(TEST_MODEL_AUTHOR);

        // Create the model definition
        CustomModelDefinition modelDefinition = createModel(model, activate);

        // Assert model is created as expected
        assertNotNull(modelDefinition);
        assertEquals(testModelName, modelDefinition.getName().getLocalName());

        NamespaceDefinition namespaceDefinition = modelDefinition.getNamespaces().iterator().next();
        assertNotNull(namespaceDefinition);
        assertEquals(namespacePair.getFirst(), namespaceDefinition.getUri());
        assertEquals(namespacePair.getSecond(), namespaceDefinition.getPrefix());

        // Assert model activation status
        NodeRef modelNodeRef = customModelService.getModelNodeRef(testModelName);
        boolean isActive = Boolean.TRUE.equals(nodeService.getProperty(modelNodeRef, ContentModel.PROP_MODEL_ACTIVE));
        assertEquals(activate, isActive);
    }

    private CustomModelDefinition createModel(final M2Model m2Model, final boolean activate)
    {
        RetryingTransactionCallback<CustomModelDefinition> createModelCallback = new RetryingTransactionCallback<CustomModelDefinition>()
        {
            public CustomModelDefinition execute() throws Throwable
            {
                CustomModelDefinition cmd;
                cmd = customModelService.createCustomModel(m2Model, activate);
                return cmd;
            }
        };
        return transactionService.getRetryingTransactionHelper().doInTransaction(createModelCallback, false, true);
    }

    private void validatePrefix(String modelName)
    {
        // validate the model namespace prefix
        RetryingTransactionCallback<Void> validateInactiveModelPrefixCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                NodeRef modelNodeRef = customModelService.getModelNodeRef(modelName);
                modelValidator.validateModelNamespacePrefix(modelNodeRef);
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(validateInactiveModelPrefixCallback, false, true);
    }
}
