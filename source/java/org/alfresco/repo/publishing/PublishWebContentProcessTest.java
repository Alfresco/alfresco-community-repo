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

package org.alfresco.repo.publishing;

import static org.alfresco.model.ContentModel.ASSOC_CONTAINS;
import static org.alfresco.model.ContentModel.PROP_NAME;
import static org.alfresco.repo.publishing.PublishingModel.PROP_PUBLISHING_EVENT_STATUS;
import static org.alfresco.repo.publishing.PublishingModel.PROP_WF_PUBLISHING_EVENT;
import static org.alfresco.repo.publishing.PublishingModel.PROP_WF_SCHEDULED_PUBLISH_DATE;
import static org.alfresco.repo.publishing.PublishingModel.TYPE_PUBLISHING_EVENT;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.action.executer.ActionExecuter;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.publishing.Status;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Nick Smith
 * @author Frederik Heremans
 * 
 * @since 4.0
 */
public abstract class PublishWebContentProcessTest extends BaseSpringTest
{
    protected ServiceRegistry serviceRegistry;
    protected Repository repositoryHelper;
    protected ActionExecuter publishEventAction;
    
    protected NodeService nodeService;
    protected WorkflowService workflowService;
    protected RetryingTransactionHelper transactionHelper;
    protected NodeRef event;
    protected String instanceId;
    protected long threadId;
    
    /**
     * Get definition to use 
     */
    protected abstract String getWorkflowDefinitionName();
    
    @Test
    public void testProcessTimers() throws Exception
    {
        final Calendar scheduledTime = Calendar.getInstance();
        scheduledTime.add(Calendar.SECOND, 5);
        
        startWorkflowAndCommit(scheduledTime);
        
        // Should be waiting for scheduled time.
        checkNode("waitForScheduledTime");

        // Wait for scheduled time to elapse.
        Thread.sleep(10000);
        
        // Should have ended
        checkEnded(instanceId);
        
        // Check the Publish Event Action was called
        verify(publishEventAction).execute(any(Action.class), any(NodeRef.class));
        
        assertFalse("The action should be run from a different Thread!", Thread.currentThread().getId()==threadId);
    }
    
    public void testProcessNoSchedule() throws Exception
    {
        startWorkflowAndCommit(null);

        // Wait for async action to execute
        Thread.sleep(500);
        
        // Should have ended
        checkEnded(instanceId);
        
        // Check the Publish Event Action was called
        verify(publishEventAction).execute(any(Action.class), any(NodeRef.class));
        
        assertFalse("The action should be run from a different Thread!", Thread.currentThread().getId()==threadId);
    }
    
    private void checkEnded(String instanceId2)
    {
        WorkflowInstance instance = workflowService.getWorkflowById(instanceId2);
        if (instance.isActive())
        {
            List<WorkflowPath> paths = workflowService.getWorkflowPaths(instance.getId());
            String nodeName = paths.get(0).getNode().getName();
            fail("Workflow should have ended! At node: " +nodeName);
        }
    }

    private void startWorkflowAndCommit(final Calendar scheduledTime)
    {
        RetryingTransactionCallback<Void> startWorkflowCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                WorkflowPath path = startWorkflow(scheduledTime);
                
                // End the Start task.
                WorkflowTask startTask = workflowService.getStartTask(path.getInstance().getId());
                workflowService.endTask(startTask.getId(), null);
                return null;
            }
        };
        transactionHelper.doInTransaction(startWorkflowCallback);
    }
    
    private WorkflowPath startWorkflow(Calendar scheduledTime)
    {
        WorkflowDefinition definition = workflowService.getDefinitionByName(getWorkflowDefinitionName());
        assertNotNull("The definition is null!", definition);
        
        NodeRef pckg = workflowService.createPackage(null);
        
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        params.put(PROP_WF_PUBLISHING_EVENT, event);
        params.put(PROP_WF_SCHEDULED_PUBLISH_DATE, scheduledTime);
        params.put(WorkflowModel.ASSOC_PACKAGE, pckg);
        
        WorkflowPath path = workflowService.startWorkflow(definition.getId(), params);
        assertNotNull(path);
        this.instanceId = path.getInstance().getId();
        return path;
    }

    protected void checkNode(String expNode)
    {
        List<WorkflowPath> paths = workflowService.getWorkflowPaths(instanceId);
        assertEquals(1, paths.size());
        WorkflowPath path = paths.get(0);
        assertEquals(expNode, path.getNode().getName());
    }
    
    @Before
    public void onSetUp()
    {
        serviceRegistry = (ServiceRegistry)getApplicationContext().getBean("ServiceRegistry");
        repositoryHelper = (Repository) getApplicationContext().getBean("repositoryHelper");
        publishEventAction = (ActionExecuter) getApplicationContext().getBean("pub_publishEvent");
        
        reset(publishEventAction);
        ActionDefinition actionDef = mock(ActionDefinition.class);
        when(publishEventAction.getActionDefinition()).thenReturn(actionDef);
        // Record thread action is run in.
        Mockito.doAnswer(new Answer<Void>()
        {
            public Void answer(InvocationOnMock invocation) throws Throwable
            {
                threadId = Thread.currentThread().getId(); 
                return null;
            }
        }).when(publishEventAction).execute(any(Action.class), any(NodeRef.class));

        this.workflowService = serviceRegistry.getWorkflowService();
        this.nodeService = serviceRegistry.getNodeService();
        this.transactionHelper = serviceRegistry.getRetryingTransactionHelper();
        AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
        NodeRef companyHome = repositoryHelper.getCompanyHome();
        String name = GUID.generate();
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(PROP_NAME, name);
        props.put(PROP_PUBLISHING_EVENT_STATUS, Status.SCHEDULED.name());
        
        QName assocName = QName.createQNameWithValidLocalName(PublishingModel.NAMESPACE, name);
        ChildAssociationRef eventAssoc = nodeService.createNode(companyHome, ASSOC_CONTAINS, assocName, TYPE_PUBLISHING_EVENT, props);
        this.event = eventAssoc.getChildRef();
    }

    @Override
    protected String[] getConfigLocations()
    {
        return new String[]
        {
            ApplicationContextHelper.CONFIG_LOCATIONS[0], "classpath:test/alfresco/test-web-publishing--workflow-context.xml"
        };
    }
    
    @After
    public void onTearDown()
    {
        try
        {
            workflowService.cancelWorkflow(instanceId);
        }
        catch (Exception e)
        {
            // NOOP
        }
        nodeService.deleteNode(event);
        AuthenticationUtil.clearCurrentSecurityContext();
    }
}
