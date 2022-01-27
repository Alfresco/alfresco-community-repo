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
package org.alfresco.repo.admin;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.NamespaceDAO;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.node.db.DbNodeServiceImpl;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.testing.category.LuceneTests;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;

import junit.framework.TestCase;

/**
 * @see RepoAdminServiceImpl
 * 
 * @author janv
 */
@Category({OwnJVMTestsCategory.class, LuceneTests.class})
public class RepoAdminServiceImplTest extends TestCase
{
    private static Log logger = LogFactory.getLog(RepoAdminServiceImplTest.class);
    
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private RepoAdminService repoAdminService;
    private DictionaryService dictionaryService;
    private TransactionService transactionService;
    private NodeService nodeService;
    private ContentService contentService;
    private SearchService searchService;
    private NamespaceService namespaceService;
    private BehaviourFilter behaviourFilter;
    private DictionaryDAO dictionaryDAO;
    private MessageService messageService;
    
    final String modelPrefix = "model-";
    final static String MKR = "{MKR}";
    
    public static final String MODEL_MKR_XML =
        "<model name='ratest-"+MKR+":testModel"+MKR+"' xmlns='http://www.alfresco.org/model/dictionary/1.0'>" +
        
        "   <description>Test model "+MKR+"</description>" +
        "   <author>Alfresco</author>" +
        "   <published>2005-05-30</published>" +
        "   <version>1.0</version>" +
        
        "   <imports>" +
        "      <import uri='http://www.alfresco.org/model/dictionary/1.0' prefix='d'/>" +
        "      <import uri='http://www.alfresco.org/model/content/1.0' prefix='cm'/>" +
        "   </imports>" +
        
        "   <namespaces>" +
        "      <namespace uri='http://www.alfresco.org/test/testmodel"+MKR+"/1.0' prefix='ratest-"+MKR+"'/>" +
        "   </namespaces>" +
        
        "   <types>" +
        
        "      <type name='ratest-"+MKR+":base'>" +
        "        <title>Base</title>" +
        "        <description>The Base Type</description>" +
        "        <parent>cm:content</parent>" +
        "        <properties>" +
        "           <property name='ratest-"+MKR+":prop1'>" +
        "              <type>d:text</type>" +
        "           </property>" +
        "        </properties>" +
        "      </type>" +
        
        "   </types>" +
        
        "</model>";
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        repoAdminService = (RepoAdminService) ctx.getBean("RepoAdminService");
        dictionaryService = (DictionaryService) ctx.getBean("DictionaryService");
        transactionService = (TransactionService) ctx.getBean("TransactionService");
        nodeService = (NodeService) ctx.getBean("NodeService");
        contentService = (ContentService) ctx.getBean("ContentService");
        searchService = (SearchService) ctx.getBean("SearchService");
        namespaceService = (NamespaceService) ctx.getBean("NamespaceService");
        behaviourFilter = (BehaviourFilter)ctx.getBean("policyBehaviourFilter");
        dictionaryDAO = (DictionaryDAO) ctx.getBean("dictionaryDAO");
        messageService = (MessageService) ctx.getBean("MessageService");
        
        DbNodeServiceImpl dbNodeService = (DbNodeServiceImpl)ctx.getBean("dbNodeService");
        dbNodeService.setEnableTimestampPropagation(false);
        
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }
    
    public void testSetup() throws Exception
    {
        // NOOP
    }
    
    public void testConsequentDeploy() throws Exception
    {
        // NOTE: X and Y must create models with unique namespaces for this test
        final String X = "MNT-8930-1";
        final String modelFileName1 = modelPrefix + X + ".xml";
        final String Y = "MNT-8930-2";
        final String modelFileName2 = modelPrefix + Y + ".xml";
        final String[] modelFileNames = { modelFileName1, modelFileName2 };

        for (String modelFileName : modelFileNames)
        {
            if (isModelDeployed(modelFileName))
            {
                // undeploy model
                repoAdminService.undeployModel(modelFileName);
            }
        }

        // deploy first custom model
        String model = MODEL_MKR_XML.replace(MKR, X + "");
        InputStream modelStream = new ByteArrayInputStream(model.getBytes("UTF-8"));
        repoAdminService.deployModel(modelStream, modelFileName1);

        final QName typeName = QName.createQName("{http://www.alfresco.org/test/testmodel" + X + "/1.0}base");
        // getModelsForUri creates NamespaceLocal that is not cleared in MNT-8930 issue
        dictionaryService.getProperty(typeName);

        final NamespaceDAO namespaceDAO = (NamespaceDAO) ctx.getBean("namespaceDAO");
        String uri = namespaceDAO.getNamespaceURI("ratest-" + X);
        assertNotNull(uri);

        class AnotherDeployThread extends Thread
        {
            private String errorStackTrace = null;

            public String getErrorStackTrace()
            {
                return errorStackTrace;
            }

            @Override
            public void run()
            {
                try
                {
                    AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

                    // deploy second custom model in separate thread
                    String model2 = MODEL_MKR_XML.replace(MKR, Y + "");
                    InputStream modelStream = new ByteArrayInputStream(model2.getBytes("UTF-8"));
                    repoAdminService.deployModel(modelStream, modelFileName2);
                    assertTrue(isModelDeployed(modelFileName2));
                    // NamespaceLocal in this thread contains newly deployed model
                    assertNotNull(namespaceDAO.getNamespaceURI("ratest-" + Y));
                }
                catch (Throwable t)
                {
                    StringWriter sw = new StringWriter();
                    t.printStackTrace(new PrintWriter(sw));
                    errorStackTrace = sw.toString();

                    logger.error("Failed to run AnotherDeployThread");
                }
            }
        }
        ;

        AnotherDeployThread anotherDeploy = new AnotherDeployThread();
        anotherDeploy.start();
        anotherDeploy.join();
        if (anotherDeploy.getErrorStackTrace() != null)
        {
            fail(anotherDeploy.getErrorStackTrace());
        }

        // In MNT-8930 issue NamespaceLocal in this thread does NOT contain newly deployed model
        uri = namespaceDAO.getNamespaceURI("ratest-" + Y);
        assertNotNull(uri);
    }

    public void xtestRepeat() throws Exception
    {
        int cnt = 10;
        
        for (int i = 1; i <= cnt; i++)
        {
            System.out.println("Itr: "+i+" out of "+cnt);
            
            testSimpleDynamicModelViaNodeService();
            testSimpleDynamicModelViaRepoAdminService();
            testConcurrentDynamicModelCreate();
            testConcurrentDynamicModelDelete();
        }
    }
    
    //
    // Test custom model management
    //
    
    public void testSimpleDynamicModelViaNodeService() throws Exception
    {
        final String X = "A";
        final String modelFileName = modelPrefix+X+".xml";
        final QName typeName = QName.createQName("{http://www.alfresco.org/test/testmodel"+X+"/1.0}base");
        final QName modelName = QName.createQName("{http://www.alfresco.org/test/testmodel"+X+"/1.0}testModel"+X);
        
        try
        {
            if (isModelDeployed(modelFileName))
            {
                // undeploy model
                repoAdminService.undeployModel(modelFileName);
            }
            
            StoreRef storeRef = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
            NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
            
            assertNull(dictionaryService.getClass(typeName));
            
            final int defaultModelCnt = dictionaryService.getAllModels().size();
            
            // deploy custom model
            String model = MODEL_MKR_XML.replace(MKR, X+"");
            InputStream modelStream = new ByteArrayInputStream(model.getBytes("UTF-8"));
            
            List<NodeRef> nodeRefs = searchService.selectNodes(rootNodeRef, "/app:company_home/app:dictionary/app:models", null, namespaceService, false);
            assertEquals(1, nodeRefs.size());
            NodeRef modelsNodeRef = nodeRefs.get(0);
            
            // create model node
            
            Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>();
            contentProps.put(ContentModel.PROP_NAME, modelFileName);
            
            NodeRef model1 = nodeService.createNode(
                        modelsNodeRef,
                        ContentModel.ASSOC_CONTAINS,
                        modelName,
                        ContentModel.TYPE_DICTIONARY_MODEL,
                        contentProps).getChildRef();
            
            // add titled aspect (for Web Client display)
            Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>();
            titledProps.put(ContentModel.PROP_TITLE, modelFileName);
            titledProps.put(ContentModel.PROP_DESCRIPTION, modelFileName);
            nodeService.addAspect(model1, ContentModel.ASPECT_TITLED, titledProps);
            
            ContentWriter writer = contentService.getWriter(model1, ContentModel.PROP_CONTENT, true);
            
            writer.setMimetype(MimetypeMap.MIMETYPE_XML);
            writer.setEncoding("UTF-8");
            
            writer.putContent(modelStream); // also invokes policies for DictionaryModelType - e.g. onContentUpdate
            modelStream.close();
            
            // activate the model
            nodeService.setProperty(model1, ContentModel.PROP_MODEL_ACTIVE, new Boolean(true));
            
            assertEquals(defaultModelCnt+1, dictionaryService.getAllModels().size());
            
            ClassDefinition myType = dictionaryService.getClass(typeName);
            assertNotNull(myType);
            assertEquals(modelName, myType.getModel().getName());
            
            // create node with custom type
            NodeRef node1 = nodeService.createNode(
                        rootNodeRef,
                        ContentModel.ASSOC_CHILDREN,
                        QName.createQName("http://www.alfresco.org/model/system/1.0", "node1"),
                        typeName,
                        null).getChildRef();
            
            // try to delete the model
            try
            {
                nodeService.deleteNode(model1);
                fail();
            } 
            catch (AlfrescoRuntimeException are)
            {
                // expected
                assertTrue(
                        "Incorrect exception message: " + are.getMessage(),
                        are.getMessage().contains(" is in use and cannot be deleted or deactivated."));
            }
            
            nodeService.deleteNode(node1);
            assertFalse(nodeService.exists(node1));
            
            NodeRef archiveRootNode = nodeService.getStoreArchiveNode(storeRef);
            NodeRef archiveNode1 = new NodeRef(archiveRootNode.getStoreRef(), node1.getId());
            assertTrue(nodeService.exists(archiveNode1));
            
            // try to delete the model
            try
            {
                nodeService.deleteNode(model1);
                fail();
            } 
            catch (AlfrescoRuntimeException are)
            {
                // expected
                assertTrue(are.getMessage().contains("is in use and cannot be deleted or deactivated."));
            }
            
            nodeService.deleteNode(archiveNode1);
            assertFalse(nodeService.exists(archiveNode1));
            
            // delete model
            nodeService.deleteNode(model1);
           
            assertEquals(defaultModelCnt, dictionaryService.getAllModels().size());
            assertNull(dictionaryService.getClass(typeName));
            
            NodeRef archiveModel1 = new NodeRef(archiveRootNode.getStoreRef(), model1.getId());
            
            // restore model
            nodeService.restoreNode(archiveModel1, null, null, null);
            assertEquals(defaultModelCnt+1, dictionaryService.getAllModels().size());
            assertNotNull(dictionaryService.getClass(typeName));
            
            // delete for good
            nodeService.deleteNode(model1);
            nodeService.deleteNode(archiveModel1);
        }
        finally
        {
            // NOOP
        }
    }
    
    public void testSimpleDynamicModelViaRepoAdminService() throws Exception
    {
        final String X = "B";
        final String modelFileName = modelPrefix+X+".xml";
        final QName typeName = QName.createQName("{http://www.alfresco.org/test/testmodel"+X+"/1.0}base");
        final QName modelName = QName.createQName("{http://www.alfresco.org/test/testmodel"+X+"/1.0}testModel"+X);
        
        try
        {
            if (isModelDeployed(modelFileName))
            {
                // undeploy model
                repoAdminService.undeployModel(modelFileName);
            }
            
            assertFalse(isModelDeployed(modelFileName));
            
            assertNull(dictionaryService.getClass(typeName));
            
            final int defaultModelCnt = dictionaryService.getAllModels().size();
            
            // deploy custom model
            String model = MODEL_MKR_XML.replace(MKR, X+"");
            InputStream modelStream = new ByteArrayInputStream(model.getBytes("UTF-8"));
            
            repoAdminService.deployModel(modelStream, modelFileName);
            
            assertTrue(isModelDeployed(modelFileName));
            assertEquals(defaultModelCnt+1, dictionaryService.getAllModels().size());
            
            ClassDefinition myType = dictionaryService.getClass(typeName);
            assertNotNull(myType);
            assertEquals(modelName, myType.getModel().getName());
            
            // can re-deploy a deployed model (note: re-deploying the same model is a valid incremental update)
            modelStream = new ByteArrayInputStream(model.getBytes("UTF-8"));
            repoAdminService.deployModel(modelStream, modelFileName);
            
            // deactivate model
            repoAdminService.deactivateModel(modelFileName);
            
            assertTrue(isModelDeployed(modelFileName)); // still deployed, although not active
            assertEquals(defaultModelCnt, dictionaryService.getAllModels().size());
            assertNull(dictionaryService.getClass(typeName));
            
            // try to deactivate an already deactivated model
            try
            {
                repoAdminService.deactivateModel(modelFileName);
                fail();
            } 
            catch (AlfrescoRuntimeException are)
            {
                // expected
                assertTrue(are.getMessage().contains("Model deactivation failed"));
                assertTrue(are.getCause().getMessage().contains("is already deactivated"));
            }
            
            // re-activate model
            repoAdminService.activateModel(modelFileName);
            
            assertTrue(isModelDeployed(modelFileName));
            assertEquals(defaultModelCnt+1, dictionaryService.getAllModels().size());
            
            myType = dictionaryService.getClass(typeName);
            assertNotNull(myType);
            assertEquals(modelName, myType.getModel().getName());
            
            // try to activate an already activated model
            try
            {
                repoAdminService.activateModel(modelFileName);
                fail();
            } 
            catch (AlfrescoRuntimeException are)
            {
                // expected
                assertTrue(are.getMessage().contains("Model activation failed"));
                assertTrue(are.getCause().getMessage().contains("is already activated"));
            }
            
            StoreRef storeRef = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
            NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
            
            // create node with custom type
            NodeRef node1 = nodeService.createNode(
                        rootNodeRef,
                        ContentModel.ASSOC_CHILDREN,
                        QName.createQName("http://www.alfresco.org/model/system/1.0", "node1"),
                        typeName,
                        null).getChildRef();
            
            // try to undeploy model
            try
            {
                repoAdminService.undeployModel(modelFileName);
                fail();
            } 
            catch (AlfrescoRuntimeException are)
            {
                // expected
                assertTrue(are.getMessage().contains("Model undeployment failed"));
                assertTrue(are.getCause().getMessage().contains("is in use and cannot be deleted or deactivated."));
            }
            
            nodeService.deleteNode(node1);
            assertFalse(nodeService.exists(node1));
            
            NodeRef archiveRootNode = nodeService.getStoreArchiveNode(storeRef);
            NodeRef archiveNode1 = new NodeRef(archiveRootNode.getStoreRef(), node1.getId());
            assertTrue(nodeService.exists(archiveNode1));
            
            // try to undeploy model
            try
            {
                repoAdminService.undeployModel(modelFileName);
                fail();
            } 
            catch (AlfrescoRuntimeException are)
            {
                // expected
                assertTrue(are.getMessage().contains("Model undeployment failed"));
                assertTrue(are.getCause().getMessage().contains("is in use and cannot be deleted or deactivated."));
            }
            
            nodeService.deleteNode(archiveNode1);
            assertFalse(nodeService.exists(archiveNode1));
            
            // undeploy
            repoAdminService.undeployModel(modelFileName);
            
            assertFalse(isModelDeployed(modelFileName));
            assertEquals(defaultModelCnt, dictionaryService.getAllModels().size());
            assertNull(dictionaryService.getClass(typeName));
            
            // try to undeploy an already undeployed (or non-existant) model
            try
            {
                repoAdminService.undeployModel(modelFileName);
                fail();
            } 
            catch (AlfrescoRuntimeException are)
            {
                // expected
                assertTrue(are.getMessage().contains("Model undeployment failed"));
                assertTrue(are.getCause().getMessage().contains("Could not find custom model"));
            }
        }
        finally
        {
            if (isModelDeployed(modelFileName))
            {
                // undeploy model
                repoAdminService.undeployModel(modelFileName);
            }
            
            assertNull(dictionaryService.getClass(typeName));
        }
    }
    
    public void testCreateAndDeleteModel() throws Exception
    {
        final String X = "C";
        final String modelFileName = modelPrefix+X+".xml";
        final QName typeName = QName.createQName("{http://www.alfresco.org/test/testmodel"+X+"/1.0}base");
        final QName modelName = QName.createQName("{http://www.alfresco.org/test/testmodel"+X+"/1.0}testModel"+X);
        
        try
        {
            if (isModelDeployed(modelFileName))
            {
                // undeploy model
                repoAdminService.undeployModel(modelFileName);
            }
            
            StoreRef storeRef = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
            NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
            
            assertNull(dictionaryService.getClass(typeName));
            
            final int defaultModelCnt = dictionaryService.getAllModels().size();
            
            // deploy custom model
            String model = MODEL_MKR_XML.replace(MKR, X+"");
            InputStream modelStream = new ByteArrayInputStream(model.getBytes("UTF-8"));
            
            List<NodeRef> nodeRefs = searchService.selectNodes(rootNodeRef, "/app:company_home/app:dictionary/app:models", null, namespaceService, false);
            assertEquals(1, nodeRefs.size());
            NodeRef modelsNodeRef = nodeRefs.get(0);
            
            // create model node
            
            Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>();
            contentProps.put(ContentModel.PROP_NAME, modelFileName);
            
            final NodeRef model1 = nodeService.createNode(
                        modelsNodeRef,
                        ContentModel.ASSOC_CONTAINS,
                        modelName,
                        ContentModel.TYPE_DICTIONARY_MODEL,
                        contentProps).getChildRef();
            
            // add titled aspect (for Web Client display)
            Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>();
            titledProps.put(ContentModel.PROP_TITLE, modelFileName);
            titledProps.put(ContentModel.PROP_DESCRIPTION, modelFileName);
            nodeService.addAspect(model1, ContentModel.ASPECT_TITLED, titledProps);
            
            ContentWriter writer = contentService.getWriter(model1, ContentModel.PROP_CONTENT, true);
            
            writer.setMimetype(MimetypeMap.MIMETYPE_XML);
            writer.setEncoding("UTF-8");
            
            writer.putContent(modelStream); // also invokes policies for DictionaryModelType - e.g. onContentUpdate
            modelStream.close();
            
            // activate the model
            nodeService.setProperty(model1, ContentModel.PROP_MODEL_ACTIVE, new Boolean(true));
            
            assertEquals(defaultModelCnt+1, dictionaryService.getAllModels().size());
            
            ClassDefinition myType = dictionaryService.getClass(typeName);
            assertNotNull(myType);
            assertEquals(modelName, myType.getModel().getName());
            
            // create node with custom type
            NodeRef node1 = nodeService.createNode(
                        rootNodeRef,
                        ContentModel.ASSOC_CHILDREN,
                        QName.createQName("http://www.alfresco.org/model/system/1.0", "node1"),
                        typeName,
                        null).getChildRef();
            
            
            
            transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Exception
                {
                    // try to delete the model
                    try
                    {
                        behaviourFilter.disableBehaviour(ContentModel.TYPE_DICTIONARY_MODEL);
                        dictionaryDAO.removeModel(modelName);
                        nodeService.deleteNode(model1);
                    } 
                    finally
                    {
                        behaviourFilter.enableBehaviour(ContentModel.TYPE_DICTIONARY_MODEL);
                    }
                    return null;
                };
            });
            
            assertFalse(nodeService.exists(model1));
            
            // ReadProperty permission.
            nodeService.getProperties(node1);
            
            nodeService.deleteNode(node1);
            assertFalse(nodeService.exists(node1));
            
            
        }
        finally
        {
            // NOOP
        }
    }
    
    private boolean isModelDeployed(String modelFileName)
    {
        for (RepoModelDefinition modelDef : repoAdminService.getModels())
        {
            if (modelDef.getRepoName().equals(modelFileName))
            {
                return true;
            }
        }
        
        return false;
    }
    
    private void undeployModels(int modelCnt)
    {
        for (int i = 1; i <= modelCnt; i++)
        {
            if (isModelDeployed(modelPrefix+i))
            {
                repoAdminService.undeployModel(modelPrefix+i);
            }
        }
    }
    
    private void deployModels(int modelCnt) throws UnsupportedEncodingException
    {
        for (int i = 1; i <= modelCnt; i++)
        {
            if (! isModelDeployed(modelPrefix+i))
            {
                String model = MODEL_MKR_XML.replace(MKR, i+"");
                InputStream modelStream = new ByteArrayInputStream(model.getBytes("UTF-8"));
                repoAdminService.deployModel(modelStream, modelPrefix+i);
                
                logger.info("["+i+"] Deployed - test model: "+modelPrefix+i);
            }
            else
            {
                logger.warn("["+i+"] Already deployed - test model: "+modelPrefix+i);
            }
        }
    }
    
    public void testConcurrentDynamicModelCreate() throws Exception
    {
        final int n = 2;
        
        undeployModels(n);
        
        int deployedModelCount = repoAdminService.getModels().size();
        logger.info("Before deploy: deployed custom model count: "+deployedModelCount);
        
        int dictModelCount = getModelCount();
        logger.info("Before deploy: dictionary model count: "+dictModelCount);
        
        // concurrently deploy N models
        runConcurrentOps(n, 1); 
        
        int newDeployedModelCount = repoAdminService.getModels().size();
        logger.info("After deploy: deployed custom model count: "+newDeployedModelCount);
        assertEquals(deployedModelCount+n, newDeployedModelCount);
        
        for (int i = 1; i <= n; i++)
        {
            assertTrue(isModelDeployed(modelPrefix+i));
        }
        
        int newDictModelCount = getModelCount();
        logger.info("After deploy: dictionary model count: "+newDictModelCount);
        assertEquals(dictModelCount+n, newDictModelCount);
        
        undeployModels(n);
        
        newDeployedModelCount = repoAdminService.getModels().size();
        logger.info("After undeploy: deployed custom model count: "+newDeployedModelCount);
        assertEquals(deployedModelCount, newDeployedModelCount);
        
        newDictModelCount = getModelCount();
        logger.info("After undeploy: dictionary model count: "+newDictModelCount);
        assertEquals(dictModelCount, newDictModelCount);
    }
    
    public void testConcurrentDynamicModelDelete() throws Exception
    {
        final int n = 2;
        
        undeployModels(n);
        
        int deployedModelCount = repoAdminService.getModels().size();
        logger.info("Existing deployed custom model count: "+deployedModelCount);
        
        int dictModelCount = getModelCount();
        logger.info("Existing dictionary model count: "+dictModelCount);
        
        deployModels(n);
        
        assertEquals("assert A: deployed model count not equal to the repoAdminService", deployedModelCount+n, repoAdminService.getModels().size());
        
        for (int i = 1; i <= n; i++)
        {
            assertTrue(isModelDeployed(modelPrefix+i));
        }
        
        assertEquals(dictModelCount+n, getModelCount());
        
        // concurrently undeploy N models
        runConcurrentOps(n, 2);
        
        assertEquals("assert after concurrent undeploy", deployedModelCount, repoAdminService.getModels().size());
        
        for (int i = 1; i <= n; i++)
        {
            assertFalse(isModelDeployed(modelPrefix+i));
        }
        
        assertEquals(dictModelCount, getModelCount());
    }
    
    private int getModelCount()
    {
        return dictionaryService.getAllModels().size();
    }
    
    private void runConcurrentOps(int threadCnt, int opType) throws InterruptedException
    {
        Thread[] threads = new Thread[threadCnt];
        Tester[] testers = new Tester[threadCnt];
        
        for (int i = 0; i < threadCnt; i++)
        {
            Tester tester = new Tester(i+1, opType);
            testers[i] = tester;
            
            threads[i] = new Thread(tester);
            threads[i].start();
        }
        
        for (int i = 0; i < threadCnt; i++)
        {
            threads[i].join();
            
            if (testers[i].getErrorStackTrace() != null)
            {
                fail(testers[i].getErrorStackTrace());
            }
        }
    }
    
    private class Tester implements Runnable
    {
        private int i;
        private int opType;
        private String errorStackTrace = null;
        
        public Tester(int i, int opType)
        {
            this.i = i;
            this.opType = opType;
        }
        
        public String getErrorStackTrace()
        {
            return errorStackTrace;
        }
        
        public void run()
        {
            try
            {
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
                
                transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
                {
                    public Object execute() throws Throwable
                    {
                        if (opType == 1)
                        {
                            // Deploy model
                            String model = MODEL_MKR_XML.replace(MKR, i+"");
                            InputStream modelStream = new ByteArrayInputStream(model.getBytes("UTF-8"));
                            repoAdminService.deployModel(modelStream, modelPrefix+i);
                            
                            logger.info("["+i+"] Deploying - test model: "+modelPrefix+i);
                        }
                        else if (opType == 2)
                        {
                            // Undeploy model
                            repoAdminService.undeployModel(modelPrefix+i);
                            
                            logger.info("["+i+"] Undeployed - test model: "+modelPrefix+i);
                        }
                        
                        return null;
                    }
                });
            }
            catch (Throwable t)
            {
                StringWriter sw = new StringWriter();
                t.printStackTrace(new PrintWriter(sw));
                errorStackTrace = sw.toString();
                
                logger.error("["+i+"] Failed to "+(opType == 1 ? "deploy" : "undeploy")+" test model: "+t);
            }
        }
    }

    // Test deploy bundle from classpath
    public void testDeployMessageBundleFromClasspath() throws Exception
    {
        String bundleBaseName = "mycustommessages";
        String resourceClasspath = "alfresco/extension/messages/" + bundleBaseName;

        final String message_key = "mycustommessages.key1";
        final String message_value_default = "This is a custom message";
        final String message_value_fr = "Ceci est un message personnalis\\u00e9";
        final String message_value_de = "Dies ist eine benutzerdefinierte Nachricht";

        // Undeploy the bundle
        if (repoAdminService.getMessageBundles().contains(bundleBaseName))
        {
            repoAdminService.undeployMessageBundle(bundleBaseName);
        }

        // Verify the custom bundle is registered
        assertFalse("The custom bundle should not be deployed", repoAdminService.getMessageBundles().contains(bundleBaseName));

        // Depoly the message bundle
        repoAdminService.deployMessageBundle(resourceClasspath);

        // Reload the messages
        repoAdminService.reloadMessageBundle(bundleBaseName);

        // Verify the custom bundle is registered
        assertTrue("The custom bundle should be deployed", repoAdminService.getMessageBundles().contains(bundleBaseName));

        // Verify we have the messages for each locale
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        assertMessageValue("Cannot retrieve default message value", message_value_default, message_key, Locale.getDefault());
        assertMessageValue("Cannot retrieve french message value", message_value_fr, message_key, Locale.FRANCE);
        assertMessageValue("Cannot retrieve german message value", message_value_de, message_key, Locale.GERMANY);

        // Test deploy a non existent bundle
        try
        {
            repoAdminService.deployMessageBundle("alfresco/extension/messages/inexistentbundle");
            fail("Bundle was not supposed to be deployed");
        }
        catch (Exception e)
        {
            // Expected to fail
        }

    }

    // Test deploy bundle from repo and reload bundles
    public void testDeployMessageBundleFromRepo() throws Exception
    {
        final String bundleBaseName = "repoBundle";
        final String message_key = "repoBundle.key1";
        final String message_value = "Value 1";
        final String message_value_fr = "Value FR";
        final String message_value_de = "Value DE";
        final String message_value_new = "New Value 1";

        // Set location
        NodeRef rootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        List<NodeRef> nodeRefs = searchService.selectNodes(rootNodeRef, "/app:company_home/app:dictionary/app:messages", null,
                namespaceService, false);
        assertEquals(1, nodeRefs.size());
        NodeRef messagesNodeRef = nodeRefs.get(0);

        // Clear messages of this bundle if they exist and are registered
        clearRepoBundles(messagesNodeRef);

        assertEquals(0, repoAdminService.getMessageBundles().size());

        // Create and upload the message files
        NodeRef messageNode_default = createMessagesNodeWithSingleKey(messagesNodeRef, bundleBaseName, message_key, null,
                message_value);
        createMessagesNodeWithSingleKey(messagesNodeRef, bundleBaseName, message_key, Locale.FRANCE.toString(), message_value_fr);
        createMessagesNodeWithSingleKey(messagesNodeRef, bundleBaseName, message_key, Locale.GERMANY.toString(),
                message_value_de);

        // Reload the messages
        repoAdminService.reloadMessageBundle(bundleBaseName);

        // Verify we have the messages for each locale
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        assertMessageValue("Cannot retrieve default message value", message_value, message_key, Locale.getDefault());
        assertMessageValue("Cannot retrieve french message value", message_value_fr, message_key, Locale.FRANCE);
        assertMessageValue("Cannot retrieve german message value", message_value_de, message_key, Locale.GERMANY);

        // Change the values
        putContentInMessageNode(messageNode_default, message_key, message_value_new);

        // Verify we still have the old value
        assertMessageValue("Unexpected change of message value", message_value, message_key, Locale.getDefault());

        // Reload the messages
        repoAdminService.reloadMessageBundle(bundleBaseName);

        // Verify new values
        assertMessageValue("Change of message value not reflected", message_value_new, message_key, Locale.getDefault());
    }

    /**
     * Create messages node
     */
    private NodeRef createMessagesNodeWithSingleKey(NodeRef parentNode, String bundleName, String key, String locale,
            String localeValue)
    {
        String msg_extension = ".properties";
        String filename = bundleName + msg_extension;
        String messageValue = localeValue;

        if (locale != null)
        {
            filename = bundleName + "_" + locale + msg_extension;
        }
        // Create a model node
        NodeRef messageNode = this.nodeService.createNode(
                parentNode, 
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, filename), 
                ContentModel.TYPE_CONTENT,
                Collections.<QName, Serializable> singletonMap(ContentModel.PROP_NAME, filename)
                ).getChildRef();

        putContentInMessageNode(messageNode, key, messageValue);

        return messageNode;

    }

    /**
     * Write content of message node
     */
    private void putContentInMessageNode(NodeRef nodeRef, String key, String value)
    {
        ContentWriter contentWriter = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        contentWriter.setEncoding("UTF-8");
        contentWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        String messagesString = key + "=" + value;
        contentWriter.putContent(messagesString);
    }

    /**
     * Clear Repo Bundle
     */
    private void clearRepoBundles(NodeRef parentNode)
    {
        List<String> repoBundles = repoAdminService.getMessageBundles();
        for (String repoBundle : repoBundles)
        {
            repoAdminService.undeployMessageBundle(repoBundle);
        }

        List<ChildAssociationRef> messageNodes = nodeService.getChildAssocs(parentNode);
        for (ChildAssociationRef messageChildRef : messageNodes)
        {
            NodeRef messageNode = messageChildRef.getChildRef();
            nodeService.deleteNode(messageNode);
        }
    }

    /**
     * Clear Repo Bundle
     */
    private void assertMessageValue(String errorMessage, String expectedValue, String key, Locale locale)
    {
        transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        assertEquals(errorMessage, expectedValue, messageService.getMessage(key, locale));
                        return null;
                    }
                });
    }

}
