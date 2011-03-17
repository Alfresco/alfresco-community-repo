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
	private static final String ADHOC_WORKFLOW_DEFINITION_DESCRIPTION = "Assign arbitrary task to colleague using JBPM workflow engine";
	private static final String REVIEW_WORKFLOW_DEFINITION_NAME = "jbpm$wf:review";
	private static final String REVIEW_POOLED_WORKFLOW_DEFINITION_NAME = "jbpm$wf:reviewpooled";

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
