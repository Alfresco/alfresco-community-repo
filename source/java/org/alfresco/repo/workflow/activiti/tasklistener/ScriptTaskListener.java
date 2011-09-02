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

package org.alfresco.repo.workflow.activiti.tasklistener;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.repo.workflow.activiti.script.ActivitiScriptBase;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.ScriptService;

/**
 * A {@link TaskListener} that runs the script against the {@link ScriptService}. 
 * 
 * The script that is executed can be set using field 'script'. A non-default 
 * script-processor can be set in the field 'scriptProcessor'. Optionally, you can run 
 * the script as a different user than the default by setting the field 'runAs'. 
 * By default, the user this script is executed with is the task's assignee. If no 
 * assignee is set, the current logged-in user is used. If no user is currently logged in
 * (eg. flow triggered by timer) the system user will be used instead.
 * 
 * @author Frederik Heremans
 * @since 3.4.e
 */
public class ScriptTaskListener extends ActivitiScriptBase implements TaskListener
{
    private static final String TASK_BINDING_NAME = "task";
    
    @Override
    public void notify(DelegateTask delegateTask)
    {
        if (script != null)
        {
            String scriptString = getStringValue(script, delegateTask);
            String scriptProcessorName = getStringValue(scriptProcessor, delegateTask);
            String runAsUser = getStringValue(runAs, delegateTask);
            
            
            // Make sure there is an authenticated user for the current thread, so when
            // the script is executed using no 'runAs' from a job-executor thread, the task's assignee
            // will be the authenticated user.
            boolean clearAuthenticationContext = checkFullyAuthenticatedUser(delegateTask);
            
            // Get all activiti-defined objects
            Map<String, Object> scriptModel = getInputMap(delegateTask, runAsUser);
            
            // Add core alfresco objects to the input-map 
            getServiceRegistry().getScriptService().buildCoreModel(scriptModel);
            
            try
            {
                Object scriptOutput = executeScript(scriptString, scriptModel, scriptProcessorName, runAsUser);

                // TODO: What to do with the script-output?
                if (scriptOutput != null)
                {
                    // delegateTask.setVariableLocal("scriptOutput", scriptOutput);
                }
            }
            finally
            {
                if (clearAuthenticationContext)
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

    protected Map<String, Object> getInputMap(DelegateTask delegateTask, String runAsUser) 
    {
        HashMap<String, Object> scriptModel = new HashMap<String, Object>(1);
        
        // Add current logged-in user and it's user home
        ActivitiScriptNode personNode = getPersonNode(runAsUser);
        if (personNode != null)
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
        scriptModel.put(TASK_BINDING_NAME, delegateTask);
        scriptModel.put(EXECUTION_BINDING_NAME, delegateTask.getExecution());
        
        // Add all workflow variables to model
        Map<String, Object> variables = delegateTask.getExecution().getVariables();
        
        for (Entry<String, Object> varEntry : variables.entrySet())
        {
            scriptModel.put(varEntry.getKey(), varEntry.getValue());
        }
        return scriptModel;
    }

    /**
     * Checks a valid Fully Authenticated User is set.
     * If none is set then attempts to set the task assignee as the Fully Authenticated User.
     * @param delegateTask the delegate task
     * @return <code>true</code> if the Fully Authenticated User was changed, otherwise <code>false</code>.
     */
    private boolean checkFullyAuthenticatedUser(final DelegateTask delegateTask) 
    {
        if (AuthenticationUtil.getFullyAuthenticatedUser() == null)
        {
            String userName = delegateTask.getAssignee();
            if (userName != null)
            {
                AuthenticationUtil.setFullyAuthenticatedUser(userName);
                return true;
            }
        }
        return false;
    }
}
