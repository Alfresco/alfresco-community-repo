package org.alfresco.repo.web.scripts.workflow;

import java.io.Serializable;
import java.util.HashMap;

import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * @author Nick Smith
 * @author Frederik Heremans
 * @since 3.4.e
 */
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