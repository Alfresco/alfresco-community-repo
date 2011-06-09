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

import static junit.framework.Assert.*;
import static org.alfresco.model.ContentModel.*;
import static org.alfresco.repo.publishing.PublishingModel.*;
import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.repo.action.executer.ActionExecuter;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.publishing.PublishingEvent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:alfresco/application-context.xml",
            "classpath:test/alfresco/test-web-publishing--workflow-context.xml"})
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class PublishWebContentJbpmTest
{
    private static final String DEF_NAME = "jbpm$pubwf:publishWebContent";

    @Autowired
    private ServiceRegistry serviceRegistry;
    
    @Autowired
    private Repository repositoryHelper;
    
    @Resource(name="pub_publishEvent")
    private ActionExecuter publishEventAction;
    
    @Resource(name="pub_checkPublishingDependencies")
    private ActionExecuter checkPublishingDependenciesAction;
    
    private NodeService nodeService;
    private WorkflowService workflowService;
    private NodeRef event;
    private String instanceId;
    
//    @Test
    public void testProcessTimers() throws Exception
    {
        Calendar scheduledTime = Calendar.getInstance();
        scheduledTime.add(Calendar.SECOND, 10);
        
        WorkflowPath path = startWorkflow(scheduledTime);
        
        // End the Start task.
        List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(path.getId());
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        WorkflowTask startTask = tasks.get(0);
        workflowService.endTask(startTask.getId(), null);
        
        // Should be waiting for scheduled time.
        checkNode("waitForScheduledTime");
        
        // Wait for scheduled time to elapse.
        Thread.sleep(11000);
        
        // Should now be waiting to retry
        checkNode("waitAndRetry");
    }

    @Test
    public void testProcessPublish() throws Exception
    {
        //TODO
    }
    
    /**
     * @param scheduledTime
     * @return
     */
    private WorkflowPath startWorkflow(Calendar scheduledTime)
    {
        WorkflowDefinition definition = workflowService.getDefinitionByName(DEF_NAME);
        assertNotNull(definition);
        
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

    private void checkNode(String expNode)
    {
        WorkflowPath path;
        List<WorkflowPath> paths = workflowService.getWorkflowPaths(instanceId);
        assertEquals(1, paths.size());
        path = paths.get(0);
        assertEquals(expNode, path.getNode().getName());
    }
    
    @Before
    public void setUp()
    {
        ActionDefinition actionDef = mock(ActionDefinition.class);
        when(publishEventAction.getActionDefinition()).thenReturn(actionDef);
        when(checkPublishingDependenciesAction.getActionDefinition()).thenReturn(actionDef);
        
        this.workflowService = serviceRegistry.getWorkflowService();
        this.nodeService = serviceRegistry.getNodeService();
        AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
        NodeRef companyHome = repositoryHelper.getCompanyHome();
        String name = GUID.generate();
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(PROP_NAME, name);
        props.put(PROP_PUBLISHING_EVENT_STATUS, PublishingEvent.Status.SCHEDULED.name());
        
        QName assocName = QName.createQNameWithValidLocalName(PublishingModel.NAMESPACE, name);
        ChildAssociationRef eventAssoc = nodeService.createNode(companyHome, ASSOC_CONTAINS, assocName, TYPE_PUBLISHING_EVENT, props);
        this.event = eventAssoc.getChildRef();
    }
    
    @After
    public void tearDown()
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
