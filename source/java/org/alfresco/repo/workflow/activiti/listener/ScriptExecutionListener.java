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

package org.alfresco.repo.workflow.activiti.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.pvm.delegate.ExecutionListener;
import org.activiti.engine.impl.pvm.delegate.ExecutionListenerExecution;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.WorkflowQNameConverter;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.repo.workflow.activiti.script.ActivitiScriptBase;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;

/**
 * 
 * @author Nick Smith
 * @author Frederik Heremans
 * @since 3.4.e
 */
public class ScriptExecutionListener extends ActivitiScriptBase implements ExecutionListener
{
	private static final String DELETED_FLAG = "deleted";
	private static final String CANCELLED_FLAG = "cancelled";

	@Override
	public void notify(ExecutionListenerExecution execution) throws Exception {
		if(script != null)
        {
        	String scriptString = getStringValue(script, execution);
        	String scriptProcessorName = getStringValue(scriptProcessor, execution);
        	String runAsUser = getStringValue(runAs, execution);
        	
 			// Make sure there is an authenticated user for the current thread, so when
        	// the script is executed using no 'runAs' from a job-executor thread, the workflow
        	// owner us used
        	boolean clearAuthenticationContext = checkFullyAuthenticatedUser(execution);
        	
        	// Get all activiti-defined objects
        	Map<String, Object> scriptModel = getInputMap(execution, runAsUser);
        	
        	// Add core alfresco objects to the input-map 
        	getServiceRegistry().getScriptService().buildCoreModel(scriptModel);
        	
        	try
        	{
        		Object scriptOutput = executeScript(scriptString, scriptModel, scriptProcessorName, runAsUser);
        		
        		// TODO: What to do with the script-output?
        		if(scriptOutput != null)
        		{
        			// delegateTask.setVariableLocal("scriptOutput", scriptOutput);
        		}
        	}
        	finally
        	{
        		if(clearAuthenticationContext)
        		{
        			// If the current user has been set to the Task's assignee, we should clear it agian
        			AuthenticationUtil.clearCurrentSecurityContext();
        		}
        	}
        }
        else
        {
        	throw new IllegalArgumentException("The field 'script' should be set on the TaskListener");
        }
	}
	
	protected Map<String, Object> getInputMap(ExecutionListenerExecution execution, String runAsUser) 
	{
		HashMap<String, Object> scriptModel = new HashMap<String, Object>(1);
        
        // Add current logged-in user and it's user home
        ActivitiScriptNode personNode = getPersonNode(runAsUser);
        if(personNode != null)
        {
        	ServiceRegistry registry = getServiceRegistry();
        	scriptModel.put(PERSON_BINDING_NAME, personNode);
        	NodeRef userHomeNode = (NodeRef) registry.getNodeService().getProperty(personNode.getNodeRef(), ContentModel.PROP_HOMEFOLDER);
            if (userHomeNode != null)
            {
            	scriptModel.put(USERHOME_BINDING_NAME, new ActivitiScriptNode(userHomeNode, registry));
            }
        }
        
        // Add activiti-specific objects
        scriptModel.put(EXECUTION_BINDING_NAME, execution);

        // Add all workflow variables to model
        Map<String, Object> variables = execution.getVariables();
        
        for(Entry<String, Object> varEntry : variables.entrySet())
        {
        	scriptModel.put(varEntry.getKey(), varEntry.getValue());
        }
        
        // Add deleted/cancelled flags
        boolean cancelled = false;
        boolean deleted = false;
        
        if(ActivitiConstants.DELETE_REASON_DELETED.equals(execution.getDeleteReason()))
        {
        	deleted = true;
        } 
        else if(ActivitiConstants.DELETE_REASON_CANCELLED.equals(execution.getDeleteReason()))
        {
        	cancelled = true;
        }
        scriptModel.put(DELETED_FLAG, deleted);
        scriptModel.put(CANCELLED_FLAG, cancelled);
        
	    return scriptModel;
	}
	
	/**
	 * Checks a valid Fully Authenticated User is set.
	 * If none is set then attempts to set the workflow owner
	 * @param execution the execution
	 * @return <code>true</code> if the Fully Authenticated User was changed, otherwise <code>false</code>.
	 */
	private boolean checkFullyAuthenticatedUser(final DelegateExecution execution) {
		if(AuthenticationUtil.getFullyAuthenticatedUser() == null)
		{
			NamespaceService namespaceService = getServiceRegistry().getNamespaceService();
			WorkflowQNameConverter qNameConverter = new WorkflowQNameConverter(namespaceService);
            String ownerVariableName = qNameConverter.mapQNameToName(ContentModel.PROP_OWNER);
			
			String userName = (String) execution.getVariable(ownerVariableName);
			if (userName != null)
			{
				AuthenticationUtil.setFullyAuthenticatedUser(userName);
				return true;
			}
		}
		return false;
	}

}
