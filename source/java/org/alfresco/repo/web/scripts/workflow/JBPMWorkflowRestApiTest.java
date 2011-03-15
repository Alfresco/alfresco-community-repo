package org.alfresco.repo.web.scripts.workflow;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

public class JBPMWorkflowRestApiTest extends AbstractWorkflowRestApiTest 
{
	private static final String ADHOC_WORKFLOW_DEFINITION_NAME = "jbpm$wf:adhoc";
	private static final String ADHOC_WORKFLOW_DEFINITION_TITLE = "Adhoc Workflow (JBPM)";
	private static final String ADHOC_WORKFLOW_DEFINITION_DESCRIPTION = "Assign arbitrary task to colleague using JBPM workflow engine.";
	private static final String REVIEW_WORKFLOW_DEFINITION_NAME = "jbpm$wf:review";
	private static final String REVIEW_POOLED_WORKFLOW_DEFINITION_NAME = "jbpm$wf:reviewPooled";

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
	protected String getReviewWorkflowDefinitionName() {
		return REVIEW_WORKFLOW_DEFINITION_NAME;
	}
	
	@Override
	protected String getReviewPooledWorkflowDefinitionName() {
		return REVIEW_POOLED_WORKFLOW_DEFINITION_NAME;
	}

	@Override
	protected void approveTask(String taskId) throws Exception
	{
		String transition = "approve";
		checkTransitionExists(transition, taskId);
		
		// Take transition
		workflowService.endTask(taskId, transition);
	}

	@Override
	protected void rejectTask(String taskId) throws Exception
	{
		String transition = "reject";
		checkTransitionExists(transition, taskId);
		
		// Take transition
		workflowService.endTask(taskId, transition);
	}
	
	private void checkTransitionExists(String transitionName, String taskId) throws Exception
	{
		// Transition to "Approve", check if transition is available
		Response response =  sendRequest(new GetRequest(URL_TASKS + "/" + taskId), Status.STATUS_OK);
        String jsonStr = response.getContentAsString();
        JSONObject json = new JSONObject(jsonStr);
        JSONObject result = json.getJSONObject("data");
        assertNotNull(result);
        
        JSONObject definition = result.getJSONObject("definition");
        assertNotNull(definition);
        JSONObject node = definition.getJSONObject("node");
        assertNotNull(node);
        JSONArray transitions = node.getJSONArray("transitions");
        assertNotNull(transitions);
        
        boolean found = false;
        // Find approve transition
        for(int i=0; i < transitions.length(); i++)
        {
        	String name = transitions.getJSONObject(i).getString("id");
        	if(name.equals("approve")) 
        	{
        		found = true;
        		break;
        	}
        }
		assertTrue("Transition 'Approve' not found", found);
	}

}
