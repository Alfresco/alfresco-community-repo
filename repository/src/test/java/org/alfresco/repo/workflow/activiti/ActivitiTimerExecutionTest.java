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

package org.alfresco.repo.workflow.activiti;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.persistence.entity.TimerEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Job;
import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.person.TestPersonManager;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.workflow.BPMEngineRegistry;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowDeployment;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;

/**
 * Test to verify timer execution autentication and transaction behaviour.
 *
 * @author Frederik Heremans
 * @since 3.4.e
 */
@Transactional
public class ActivitiTimerExecutionTest extends BaseSpringTest
{

    private static final String USER1 = "User1" + GUID.generate();

    private RetryingTransactionHelper transactionHelper;

    private WorkflowService workflowService;

    private AuthenticationComponent authenticationComponent;

    private NodeService nodeService;

    private ProcessEngine activitiProcessEngine;

    private TestPersonManager personManager;

    @SuppressWarnings("deprecation")
    @Test
    public void testTimerExecutionAuthentication() throws Exception
    {
        try
        {
            WorkflowInstance taskAssigneeWorkflowInstance = transactionHelper
                    .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<WorkflowInstance>() {
                        public WorkflowInstance execute() throws Throwable
                        {
                            // Create test person
                            personManager.createPerson(USER1);

                            WorkflowDefinition definition = deployDefinition("activiti/testTimerTransaction.bpmn20.xml");

                            // Start the test timer transaction process, with 'error' = false, expecting a timer job
                            // to be executed without an error, with task timer is assigned to assigned to USER1
                            Map<QName, Serializable> params = new HashMap<QName, Serializable>();
                            params.put(QName.createQName("error"), Boolean.FALSE);
                            params.put(QName.createQName("theTaskAssignee"), USER1);

                            WorkflowPath path = workflowService.startWorkflow(definition.getId(), params);
                            // End start-task
                            workflowService.endTask(workflowService.getStartTask(path.getInstance().getId()).getId(), null);

                            return path.getInstance();
                        }
                    }, false, true);

            // No timers should be available after a while they should have been executed, otherwise test fails
            waitForTimersToBeExecuted(taskAssigneeWorkflowInstance.getId());

            // Test assigned task
            WorkflowPath path = workflowService.getWorkflowPaths(taskAssigneeWorkflowInstance.getId()).get(0);

            // Check if job executed without exception, process should be waiting in "waitTask"
            List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(path.getId());
            assertNotNull(tasks);
            assertEquals(1, tasks.size());
            assertEquals("waitTask", tasks.get(0).getDefinition().getNode().getName());

            // Check if timer was executed as task assignee, was set while executing timer
            Map<QName, Serializable> pathProps = workflowService.getPathProperties(path.getId());
            assertEquals(USER1, pathProps.get(QName.createQName("timerExecutedAs")));
        }
        finally
        {
            cleanUp();

        }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testTimerExecutionTransactionRollback() throws Exception
    {
        try
        {
            WorkflowInstance workflowInstance = transactionHelper
                    .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<WorkflowInstance>() {
                        public WorkflowInstance execute() throws Throwable
                        {
                            // Create test person
                            NodeRef person = personManager.createPerson(USER1);
                            assertNotNull(person);

                            WorkflowDefinition definition = deployDefinition("activiti/testTimerTransaction.bpmn20.xml");

                            // Start the test timer transaction process, with 'error' = true, expecting a timer job
                            // to be executed with an error, with task timer is assigned to assigned to USER1
                            Map<QName, Serializable> params = new HashMap<QName, Serializable>();
                            params.put(QName.createQName("error"), Boolean.TRUE);
                            params.put(QName.createQName("theTaskAssignee"), USER1);

                            WorkflowPath path = workflowService.startWorkflow(definition.getId(), params);
                            // End start-task
                            workflowService.endTask(workflowService.getStartTask(path.getInstance().getId()).getId(), null);

                            return path.getInstance();
                        }
                    }, false, true);

            String processInstanceId = BPMEngineRegistry.getLocalId(workflowInstance.getId());

            // Check the timer, should have "error" set in it
            TimerEntity timer = (TimerEntity) activitiProcessEngine.getManagementService()
                    .createJobQuery().timers()
                    .processInstanceId(processInstanceId).singleResult();

            int numberOfRetries = 5;
            for (int i = 0; i < numberOfRetries; i++)
            {
                if (timer.getExceptionMessage() != null && timer.getRetries() == 0)
                {
                    break;
                }
                Thread.sleep(1000);
                timer = (TimerEntity) activitiProcessEngine.getManagementService()
                        .createJobQuery().timers()
                        .processInstanceId(processInstanceId).singleResult();
            }
            assertNotNull("Job should have exception message set", timer.getExceptionMessage());

            // Check if exception is the one we deliberately caused
            String fullExceptionStacktrace = activitiProcessEngine.getManagementService().getJobExceptionStacktrace(timer.getId());
            assertTrue(fullExceptionStacktrace.contains("Activiti engine rocks!"));

            // Check if alfresco-changes that were performed are rolled back
            transactionHelper.doInTransaction((RetryingTransactionHelper.RetryingTransactionCallback<Void>) () -> {
                AuthenticationUtil.runAsSystem(() -> {
                    NodeRef personNode = personManager.get(USER1);
                    NodeRef userHomeNode = (NodeRef) nodeService.getProperty(personNode, ContentModel.PROP_HOMEFOLDER);

                    String homeFolderName = (String) nodeService.getProperty(userHomeNode, ContentModel.PROP_NAME);
                    assertNotSame("User home changed", homeFolderName);
                    return null;
                });
                return null;
            }, false, true);
        }
        finally
        {
            cleanUp();
        }
    }

    /**
     * Delete the deployment, cascading all related processes/history
     */
    private void cleanUp()
    {
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                try
                {
                    personManager.clearPeople();
                }
                finally
                {
                    // Make sure process-definition is still deleted, even when clearing people fails.
                    ProcessDefinition procDef = activitiProcessEngine.getRepositoryService()
                            .createProcessDefinitionQuery()
                            .processDefinitionKey("testTimerTransaction")
                            .latestVersion()
                            .singleResult();

                    if (procDef != null)
                    {
                        activitiProcessEngine.getRepositoryService().deleteDeployment(procDef.getDeploymentId(), true);
                    }
                }
                return null;
            }
        });
    }

    @Before
    public void before() throws Exception
    {
        ServiceRegistry registry = (ServiceRegistry) applicationContext.getBean(ServiceRegistry.SERVICE_REGISTRY);
        this.workflowService = registry.getWorkflowService();
        this.authenticationComponent = (AuthenticationComponent) applicationContext.getBean("authenticationComponent");
        this.nodeService = registry.getNodeService();

        this.transactionHelper = (RetryingTransactionHelper) this.applicationContext
                .getBean("retryingTransactionHelper");

        this.activitiProcessEngine = (ProcessEngine) this.applicationContext.getBean("activitiProcessEngine");

        MutableAuthenticationService authenticationService = registry.getAuthenticationService();
        PersonService personService = registry.getPersonService();

        this.personManager = new TestPersonManager(authenticationService, personService, nodeService);

        authenticationComponent.setSystemUserAsCurrentUser();
    }

    private void waitForTimersToBeExecuted(String workflowInstanceId) throws Exception
    {
        String processInstanceId = BPMEngineRegistry.getLocalId(workflowInstanceId);
        // Job-executor should finish the job, no timers should be available for WF
        List<Job> timers = null;
        int numberOfRetries = 5;
        for (int i = 0; i < numberOfRetries; i++)
        {
            Thread.sleep(1500);
            timers = activitiProcessEngine.getManagementService().createJobQuery()
                    .timers()
                    .processInstanceId(processInstanceId)
                    .list();
            if (timers.size() == 0)
            {
                break;
            }
        }

        if (timers.size() > 0)
        {
            fail("There are still timers available for the process: " + processInstanceId);
        }
    }

    protected WorkflowDefinition deployDefinition(String resource)
    {
        InputStream input = getInputStream(resource);
        WorkflowDeployment deployment = workflowService.deployDefinition(ActivitiConstants.ENGINE_ID, input, MimetypeMap.MIMETYPE_XML);
        WorkflowDefinition definition = deployment.getDefinition();
        return definition;
    }

    private InputStream getInputStream(String resource)
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream input = classLoader.getResourceAsStream(resource);
        return input;
    }

}
