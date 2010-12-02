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
package org.alfresco.repo.admin;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

/**
 * @see RepoAdminServiceImpl
 * 
 * @author janv
 */
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
                assertTrue(are.getMessage().contains("Failed to validate model delete"));
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
                assertTrue(are.getMessage().contains("Failed to validate model delete"));
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
                assertTrue(are.getCause().getMessage().contains("Failed to validate model delete"));
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
                assertTrue(are.getCause().getMessage().contains("Failed to validate model delete"));
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
        
        assertEquals(deployedModelCount+n, repoAdminService.getModels().size());
        
        for (int i = 1; i <= n; i++)
        {
            assertTrue(isModelDeployed(modelPrefix+i));
        }
        
        assertEquals(dictModelCount+n, getModelCount());
        
        // concurrently undeploy N models
        runConcurrentOps(n, 2);
        
        assertEquals(deployedModelCount, repoAdminService.getModels().size());
        
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
    
    //
    // TODO - Test custom message management
    //
}
