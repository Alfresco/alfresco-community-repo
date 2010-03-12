/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.admin;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
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
    
    final String modelPrefix = "model-";
    final static String MKR = "{MKR}";
    
    public static final String MODEL_MKR_XML =
        "<model name='test"+MKR+":testModel"+MKR+"' xmlns='http://www.alfresco.org/model/dictionary/1.0'>" +
        
        "   <description>Test model "+MKR+"</description>" +
        "   <author>Alfresco</author>" +
        "   <published>2005-05-30</published>" +
        "   <version>1.0</version>" +
        
        "   <imports>" +
        "      <import uri='http://www.alfresco.org/model/dictionary/1.0' prefix='d'/>" +
        "      <import uri='http://www.alfresco.org/model/content/1.0' prefix='cm'/>" +
        "   </imports>" +
        
        "   <namespaces>" +
        "      <namespace uri='http://www.alfresco.org/test/testmodel"+MKR+"/1.0' prefix='test"+MKR+"'/>" +
        "   </namespaces>" +
        
        "   <types>" +
        
        "      <type name='test"+MKR+":base'>" +
        "        <title>Base</title>" +
        "        <description>The Base Type</description>" +
        "        <parent>cm:content</parent>" +
        "        <properties>" +
        "           <property name='test"+MKR+":prop1'>" +
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
    
    //
    // Test custom model management
    //
    
    public void testSimpleDynamicModel() throws Exception
    {
        final int X = 0;
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
            }
            
            // undeploy model
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
            }
        }
    }
    
    public void testConcurrentDynamicModelCreate() throws Exception
    {
        final int n = 5;
        
        undeployModels(n);
        
        int deployedModelCount = repoAdminService.getModels().size();
        logger.info("Existing deployed custom model count: "+deployedModelCount);
        
        int dictModelCount = getModelCount();
        logger.info("Existing dictionary model count: "+dictModelCount);
        
        // concurrently deploy N models
        runConcurrentOps(n, 1); 
        
        assertEquals(deployedModelCount+n, repoAdminService.getModels().size());
        
        for (int i = 1; i <= n; i++)
        {
            assertTrue(isModelDeployed(modelPrefix+i));
        }
        
        assertEquals(dictModelCount+n, getModelCount());
        
        undeployModels(n);
    }
    
    public void testConcurrentDynamicModelDelete() throws Exception
    {
        final int n = 5;
        
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
                            
                            logger.info("["+i+"] Deploying test model: "+modelPrefix+i+" ["+AlfrescoTransactionSupport.getTransactionId()+"]");
                        }
                        else if (opType == 2)
                        {
                            // Undeploy model
                            repoAdminService.undeployModel(modelPrefix+i);
                            
                            logger.info("["+i+"] Undeployed test model: "+modelPrefix+i+" ["+AlfrescoTransactionSupport.getTransactionId()+"]");
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
                
                logger.error("["+i+"] Failed to deploy test model: "+t);
            }
        }
    }
    
    //
    // TODO - Test custom message management
    //
}
