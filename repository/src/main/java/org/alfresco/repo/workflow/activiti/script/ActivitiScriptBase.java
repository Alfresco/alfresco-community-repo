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

package org.alfresco.repo.workflow.activiti.script;

import java.util.Map;

import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.Expression;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.workflow.WorkflowDeployer;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowException;

/**
 * Base class for execution scripts, using {@link ScriptService} as part of
 * activiti workflow.
 *
 * @author Frederik Heremans
 * @since 3.4.e
 */
public class ActivitiScriptBase 
{
    protected static final String PERSON_BINDING_NAME = "person";
    protected static final String USERHOME_BINDING_NAME = "userhome";
    protected static final String EXECUTION_BINDING_NAME = "execution";

    protected Expression script;
    protected Expression runAs;
    protected Expression scriptProcessor;

    protected Object executeScript(String theScript, Map<String, Object> model, String scriptProcessorName, String runAsUser)
    {
        String user = AuthenticationUtil.getFullyAuthenticatedUser();
        
        Object scriptResult = null;
        if (runAsUser == null && user != null)
        {
            // Just execute the script using the current user
            scriptResult = executeScript(theScript, model, scriptProcessorName);
        }
        else 
        {
            if (runAsUser != null)
            {
                // Check if the user used for running exists
                validateRunAsUser(runAsUser);
            }
            else
            {
                // No current user is authenticated, use the system-user to execute the script
                runAsUser = AuthenticationUtil.getSystemUserName();
            }
            executeScriptAsUser(theScript, model, scriptProcessorName, runAsUser);
        }
        return scriptResult;
    }
    
    protected Object executeScriptAsUser(final String theScript, final Map<String, Object> model, final String scriptProcessorName, final String runAsUser)
    {
        // execute as specified runAsUser
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                return executeScript(theScript, model, scriptProcessorName);
            }
        }, runAsUser);
    }
    
    protected Object executeScript(String theScript, Map<String, Object> model, String scriptProcessorName)
    {
        // Execute the script using the appropriate processor
        Object scriptResult = null;

        // Checks if current workflow is secure
        boolean secure = isSecure();

        if (scriptProcessorName != null)
        {
            scriptResult = getServiceRegistry().getScriptService().executeScriptString(scriptProcessorName, theScript, model, secure);
        }
        else
        {
            // Use default script-processor
            scriptResult = getServiceRegistry().getScriptService().executeScriptString(theScript, model, secure);
        }
        
        return scriptResult;
    }
    
    protected String getStringValue(Expression expression, VariableScope scope) 
    {
        if (expression != null)
        {
            return (String) expression.getValue(scope);
        }
        return null;
    }

    protected ServiceRegistry getServiceRegistry()
    {
        ProcessEngineConfigurationImpl config = Context.getProcessEngineConfiguration();
        if (config != null) 
        {
            // Fetch the registry that is injected in the activiti spring-configuration
            ServiceRegistry registry = (ServiceRegistry) config.getBeans().get(ActivitiConstants.SERVICE_REGISTRY_BEAN_KEY);
            if (registry == null)
            {
                throw new RuntimeException(
                            "Service-registry not present in ProcessEngineConfiguration beans, expected ServiceRegistry with key" + 
                            ActivitiConstants.SERVICE_REGISTRY_BEAN_KEY);
            }
            return registry;
        }
        throw new IllegalStateException("No ProcessEngineCOnfiguration found in active context");
    }

    /**
     * Checks whether the workflow must be considered secure or not - based on {@link DeploymentEntity} category.
     * If it is not considered secure, the workflow will be executed in sandbox context with more restrictions
     *
     * @return true if workflow is considered secure, false otherwise
     */
    private boolean isSecure()
    {
        String category = null;

        try
        {
            if (Context.isExecutionContextActive())
            {
                category = Context.getExecutionContext().getDeployment().getCategory();
            }
        }
        catch (Exception e)
        {
            // No action required
        }

        // If the workflow is considered secure, the deployment entity category matches the condition (either internal or full access)
        return category != null && (WorkflowDeployer.CATEGORY_ALFRESCO_INTERNAL.equals(category) || WorkflowDeployer.CATEGORY_FULL_ACCESS.equals(category));
    }

    /**
     * Checks that the specified 'runAs' field
     * specifies a valid username.
     */
    private void validateRunAsUser(final String runAsUser) 
    {
        Boolean runAsExists = AuthenticationUtil.runAs(new RunAsWork<Boolean>()
        {
            // Validate using System user to ensure sufficient permissions available to access person node.

            public Boolean doWork() throws Exception 
            {
                return getServiceRegistry().getPersonService().personExists(runAsUser);
            }
        }, AuthenticationUtil.getSystemUserName());

        if (!runAsExists)
        {
            throw new WorkflowException("runas user '" + runAsUser + "' does not exist.");
        }
    }
    
    protected ActivitiScriptNode getPersonNode(String runAsUser)
    {
        String userName = null;
        if (runAsUser != null) 
        {
            userName = runAsUser;
        }
        else 
        {
            userName = AuthenticationUtil.getFullyAuthenticatedUser();
        }
        
        // The "System" user is a special case, which has no person object associated with it.
        if(userName != null && !AuthenticationUtil.SYSTEM_USER_NAME.equals(userName))
        {
            ServiceRegistry services = getServiceRegistry();
            PersonService personService = services.getPersonService();
            if (personService.personExists(userName))
            {
                NodeRef person = personService.getPerson(userName);
                return new ActivitiScriptNode(person, services);
            }
        }
        return null;
    }
    
    public void setScript(Expression script) 
    {
        this.script = script;
    }

    public void setRunAs(Expression runAs) 
    {
        this.runAs = runAs;
    }

    public void setScriptProcessor(Expression scriptProcessor) 
    {
        this.scriptProcessor = scriptProcessor;
    }
}
