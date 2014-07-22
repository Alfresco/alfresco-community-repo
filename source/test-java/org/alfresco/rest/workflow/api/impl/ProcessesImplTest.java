/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.rest.workflow.api.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.rest.framework.resource.parameters.Params.RecognizedParams;
import org.alfresco.rest.framework.resource.parameters.where.Query;
import org.alfresco.rest.framework.webscripts.ResourceWebScriptHelper;
import org.alfresco.rest.workflow.api.Processes;
import org.alfresco.rest.workflow.api.model.ProcessInfo;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Test for {@link ProcessesImpl} class
 * 
 * @author Dmitry Velichkevich
 */
public class ProcessesImplTest extends TestCase
{
    private static final int ACTIVE_WORKFLOWS_INITIAL_AMOUNT = 25;


    private static final String PROCESSES_BEAN_NAME = "processes";

    private static final String DESIRED_WORKFLOW_ID_PREFIX = "activiti$activitiReview";


    private static final String QUERY_STATUS_ACTIVE = "(status=active)";


    private ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();

    private WorkflowService workflowService;

    private PersonService personService;

    private Processes processes;


    private UserTransaction transaction;


    @Before
    public void setUp() throws Exception
    {
        processes = (Processes) applicationContext.getBean(PROCESSES_BEAN_NAME);

        ServiceRegistry registry = (ServiceRegistry) applicationContext.getBean(ServiceRegistry.SERVICE_REGISTRY);
        workflowService = registry.getWorkflowService();
        personService = registry.getPersonService();

        transaction = registry.getTransactionService().getUserTransaction();
        transaction.begin();

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        AuthenticationUtil.setRunAsUser(AuthenticationUtil.getAdminUserName());

        NodeRef adminUserNodeRef = personService.getPerson(AuthenticationUtil.getAdminUserName());

        WorkflowDefinition workflowDefinition = findAppropriateWorkflowDefinitionId();

        for (int i = 0; i < ACTIVE_WORKFLOWS_INITIAL_AMOUNT; i++)
        {
            startWorkflow(workflowDefinition, adminUserNodeRef);
        }
    }

    private void startWorkflow(WorkflowDefinition neededDefinition, NodeRef assignee)
    {
        Map<QName, Serializable> parameters = new HashMap<QName, Serializable>();
        parameters.put(WorkflowModel.ASSOC_ASSIGNEE, (Serializable) Collections.singletonList(assignee));
        parameters.put(WorkflowModel.ASSOC_PACKAGE, workflowService.createPackage(null));

        workflowService.startWorkflow(neededDefinition.getId(), parameters);
    }

    private WorkflowDefinition findAppropriateWorkflowDefinitionId()
    {
        WorkflowDefinition result = null;
        List<WorkflowDefinition> definitions = workflowService.getDefinitions();

        for (WorkflowDefinition definition : definitions)
        {
            if (definition.getId().startsWith(DESIRED_WORKFLOW_ID_PREFIX))
            {
                result = definition;
                break;
            }
        }

        return (null != result) ? (result) : (definitions.iterator().next());
    }

    @After
    public void tearDown() throws Exception
    {
        AuthenticationUtil.clearCurrentSecurityContext();

        if ((null != transaction) && (Status.STATUS_COMMITTED != transaction.getStatus()) && (Status.STATUS_ROLLEDBACK != transaction.getStatus()))
        {
            transaction.rollback();
        }
    }

    @Test
    public void testHasMoreItemsTrue() throws Exception
    {
        CollectionWithPagingInfo<ProcessInfo> actualProcesses = queryActiveProcessesAndAssertResult(0, (ACTIVE_WORKFLOWS_INITIAL_AMOUNT - 5));
        assertTrue(actualProcesses.hasMoreItems());

        actualProcesses = queryActiveProcessesAndAssertResult(10, (ACTIVE_WORKFLOWS_INITIAL_AMOUNT - 15));
        assertTrue(actualProcesses.hasMoreItems());
    }

    @Test
    public void testHasMoreFalseAsPerMnt10977() throws Exception
    {
        CollectionWithPagingInfo<ProcessInfo> actualProcesses = queryActiveProcessesAndAssertResult(5, ACTIVE_WORKFLOWS_INITIAL_AMOUNT);
        assertFalse(actualProcesses.hasMoreItems());
    }

    private CollectionWithPagingInfo<ProcessInfo> queryActiveProcessesAndAssertResult(int skipCount, int maxItems)
    {
        Query query = ResourceWebScriptHelper.getWhereClause(QUERY_STATUS_ACTIVE);
        Parameters parameters = Params.valueOf(new RecognizedParams(null, Paging.valueOf(skipCount, maxItems), null, null, null, query, null), null, null);

        CollectionWithPagingInfo<ProcessInfo> result = processes.getProcesses(parameters);

        assertNotNull(result);
        assertNotNull(result.getCollection());

        int remainingProcessesAmount = ACTIVE_WORKFLOWS_INITIAL_AMOUNT - skipCount;
        if (maxItems >= remainingProcessesAmount)
        {
            assertEquals(remainingProcessesAmount, result.getCollection().size());
        }
        else
        {
            assertEquals(maxItems, result.getCollection().size());
        }

        return result;
    }
}
