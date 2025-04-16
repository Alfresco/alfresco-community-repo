/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.repo.web.scripts.workflow;

import java.io.Serializable;
import java.util.HashMap;

import org.junit.experimental.categories.Category;

import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.testing.category.LuceneTests;

/**
 * @author Nick Smith
 * @author Frederik Heremans
 * @since 3.4.e
 */
@Category(LuceneTests.class)
public class ActivitiWorkflowRestApiTest extends AbstractWorkflowRestApiTest
{
    private static final String ADHOC_WORKFLOW_DEFINITION_NAME = "activiti$activitiAdhoc";
    private static final String ADHOC_WORKFLOW_DEFINITION_TITLE = "New Task";
    private static final String ADHOC_WORKFLOW_DEFINITION_DESCRIPTION = "Assign a new task to yourself or a colleague";
    private static final String REVIEW_WORKFLOW_DEFINITION_NAME = "activiti$activitiReview";
    private static final String REVIEW_POOLED_WORKFLOW_DEFINITION_NAME = "activiti$activitiReviewPooled";

    @Override
    protected String getAdhocWorkflowDefinitionName()
    {
        return ADHOC_WORKFLOW_DEFINITION_NAME;
    }

    @Override
    protected String getAdhocWorkflowDefinitionTitle()
    {
        return ADHOC_WORKFLOW_DEFINITION_TITLE;
    }

    @Override
    protected String getAdhocWorkflowDefinitionDescription()
    {
        return ADHOC_WORKFLOW_DEFINITION_DESCRIPTION;
    }

    @Override
    protected String getReviewWorkflowDefinitionName()
    {
        return REVIEW_WORKFLOW_DEFINITION_NAME;
    }

    @Override
    protected String getReviewPooledWorkflowDefinitionName()
    {
        return REVIEW_POOLED_WORKFLOW_DEFINITION_NAME;
    }

    @Override
    protected void approveTask(String taskId)
    {
        HashMap<QName, Serializable> params = new HashMap<QName, Serializable>();
        params.put(QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "reviewOutcome"), "Approve");
        workflowService.updateTask(taskId, params, null, null);
        workflowService.endTask(taskId, null);
    }

    @Override
    protected void rejectTask(String taskId)
    {
        HashMap<QName, Serializable> params = new HashMap<QName, Serializable>();
        params.put(QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "reviewOutcome"), "Reject");
        workflowService.updateTask(taskId, params, null, null);
        workflowService.endTask(taskId, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getEngine()
    {
        return ActivitiConstants.ENGINE_ID;
    }
}
