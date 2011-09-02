/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

package org.alfresco.repo.workflow.activiti;

import java.io.InputStream;

import junit.framework.TestCase;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * @author Nick Smith
 * @since 3.4.e
 */
public class ActivitiSpringTransactionTest extends TestCase
{
    private static final QName PROP_CHECK_VALUE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "check_value");
    private static final String PROC_DEF_KEY = "testTask";
    private static final QName ASPECT = ContentModel.ASPECT_ATTACHABLE;

    private RuntimeService runtime;
    private RepositoryService repo;
    private Deployment deployment;
    private AuthenticationComponent authenticationComponent;
    private NodeService nodeService;
    private RetryingTransactionHelper txnHelper;
    private NodeRef workingNodeRef;

    public void testSmoke() throws Exception
    {
        assertNotNull(runtime);

        ProcessInstance instance = runtime.startProcessInstanceByKey(PROC_DEF_KEY);
        assertNotNull(instance);
        
        String instanceId = instance.getId();
        ProcessInstance instanceInDb = findProcessInstance(instanceId);
        assertNotNull(instanceInDb);
        runtime.deleteProcessInstance(instance.getId(), "");
        assertNotNull(instance);
    }

    /**
     * Start a process and then trigger a rollback by throwing an exception in Alfresco NodeService.
     * Check that the process instance was rolled back.
     */
    public void testRollbackFromAlfresco()
    {
        RetryingTransactionCallback<String> callback = new RetryingTransactionCallback<String>()
        {
            public String execute() throws Throwable
            {
                ProcessInstance instance = runtime.startProcessInstanceByKey(PROC_DEF_KEY);
                String id = instance.getId();
                try
                {
                    blowUp();
                }
                catch (InvalidNodeRefException e)
                {
                    // Expected, but absorbed
                }
                return id;
            }
        };
        String id = txnHelper.doInTransaction(callback);
        ProcessInstance instance = findProcessInstance(id);
        if (instance != null)
        {
            runtime.deleteProcessInstance(id, "For test");
            fail("The process instance creation should have been rolled back!");
        }
    }
    
    /**
     * Start a process and then trigger a rollback by throwing an exception in Alfresco NodeService.
     * Check that the process instance was rolled back.
     */
    public void testRollbackFromActiviti()
    {
        RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                nodeService.addAspect(workingNodeRef, ASPECT, null);
                assertTrue("The node should have the aspect!", nodeService.hasAspect(workingNodeRef, ASPECT));
                try
                {
                    runtime.signal("Fake Id");
                    fail("Should throw an Exception here!");
                }
                catch (ActivitiException e)
                {
                    // Expected, but absorbed
                }
                return null;
            }
        };
        txnHelper.doInTransaction(callback);
        assertFalse("The node should not have the aspect!", nodeService.hasAspect(workingNodeRef, ASPECT));
    }
    
    /**
     * Checks nesting of two transactions with <code>requiresNew == true</code>
     */
    public void testNestedWithoutPropogation()
    {
        RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                ProcessInstance instance = runtime.startProcessInstanceByKey(PROC_DEF_KEY);
                final String id = instance.getId();
                
                ProcessInstance instanceInDb = findProcessInstance(id);
                assertNotNull("Can't read process instance in same transaction!", instanceInDb);
                RetryingTransactionCallback<Void> callbackInner = new RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        ProcessInstance instanceInDb2 = findProcessInstance(id);
                        assertNull("Should not be able to read process instance in inner transaction!", instanceInDb2);
                        return null;
                    }
                };
                try
                {
                    txnHelper.doInTransaction(callbackInner, false, true);
                    return null;
                }
                finally
                {
                    runtime.deleteProcessInstance(id, "FOr test");
                }
            }
        };
        txnHelper.doInTransaction(callback);
    }
    
    private Long blowUp()
    {
        NodeRef invalidNodeRef = new NodeRef(workingNodeRef.getStoreRef(), "BOGUS");
        nodeService.setProperty(invalidNodeRef, PROP_CHECK_VALUE, null);
        fail("Expected to generate an InvalidNodeRefException");
        return null;
    }
    
    private ProcessInstance findProcessInstance(String instanceId)
    {
        return runtime.createProcessInstanceQuery()
            .processInstanceId(instanceId)
            .singleResult();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception
    {
        ApplicationContext appContext = ApplicationContextHelper.getApplicationContext();
        this.repo = (RepositoryService) appContext.getBean("activitiRepositoryService");
        this.runtime = (RuntimeService) appContext.getBean("activitiRuntimeService");
        
        ServiceRegistry serviceRegistry = (ServiceRegistry) appContext.getBean(ServiceRegistry.SERVICE_REGISTRY);
        authenticationComponent = (AuthenticationComponent) appContext.getBean("authenticationComponent");
        TransactionService transactionService = serviceRegistry.getTransactionService();
        nodeService = serviceRegistry.getNodeService();
        txnHelper = transactionService.getRetryingTransactionHelper();

        // authenticate
        authenticationComponent.setSystemUserAsCurrentUser();
        
        StoreRef storeRef = nodeService.createStore(
                StoreRef.PROTOCOL_WORKSPACE,
                "test-" + getName() + "-" + System.currentTimeMillis());
        NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
        // Create a node to work on
        workingNodeRef = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, getName()),
                ContentModel.TYPE_CMOBJECT).getChildRef();
    
        String resource = "activiti/testTransaction.bpmn20.xml";
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream input= classLoader.getResourceAsStream(resource);
        this.deployment = repo.createDeployment()
        .addInputStream(resource, input)
        .deploy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void tearDown() throws Exception
    {
        try
        {
            repo.deleteDeployment(deployment.getId(), true);
            authenticationComponent.clearCurrentSecurityContext();
        }
        catch (Exception e) 
        {
            // Do Nothing }
        }
    }
}
