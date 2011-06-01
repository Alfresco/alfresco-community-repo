/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.workflow.jbpm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.dom4j.Element;
import org.jbpm.context.def.VariableAccess;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.Token;
import org.jbpm.jpdl.xml.JpdlXmlReader;
import org.jbpm.taskmgmt.def.Task;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.springframework.beans.factory.BeanFactory;
import org.xml.sax.InputSource;

/**
 * A jBPM Action Handler for executing Alfresco Script
 *
 * The configuration of this action is as follows:
 *  <script>
 *     <expression>
 *        the script to execute
 *     </expression>
 *     <variable name="watcha" access="write"/>
 *  </script>
 *  
 * It's exactly the same as jBPM's own script configuration.
 *  
 * @author davidc
 */
public class AlfrescoJavaScript extends JBPMSpringActionHandler
{
    private static final long serialVersionUID = -2908748080671212745L;
    
    private static JpdlXmlReader jpdlReader = new JpdlXmlReader((InputSource)null);
    private ServiceRegistry services;
    private Element script;
    private String runas;
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler#initialiseHandler(org.springframework.beans.factory.BeanFactory)
     */
    @Override
    protected void initialiseHandler(BeanFactory factory)
    {
        services = (ServiceRegistry)factory.getBean(ServiceRegistry.SERVICE_REGISTRY);
    }

    /* (non-Javadoc)
     * @see org.jbpm.graph.def.ActionHandler#execute(org.jbpm.graph.exe.ExecutionContext)
     */
    public void execute(final ExecutionContext executionContext) throws Exception
    {
        // validate script
        if (script == null)
        {
            throw new WorkflowException("Script has not been provided");
        }
        boolean isTextOnly = isScriptOnlyText();
        
        List<VariableAccess> variableAccesses = getVariableAccessors(isTextOnly);
        String expression = getExpression(isTextOnly);

        // execute
        Object result = executeExpression(expression, executionContext, variableAccesses);

        // map script return variable to process context
        VariableAccess returnVariable = getWritableVariable(variableAccesses);
        if (returnVariable != null)
        {
            ContextInstance contextInstance = executionContext.getContextInstance();
            Token token = executionContext.getToken();
            contextInstance.setVariable(returnVariable.getVariableName(), result, token);
        }
    }

	private Object executeExpression(String expression, ExecutionContext executionContext, List<VariableAccess> variableAccesses) {
		boolean userChanged = checkFullyAuthenticatedUser(executionContext);
		Object result = executeScript(expression, executionContext, variableAccesses);
		if(userChanged)
		{
			AuthenticationUtil.clearCurrentSecurityContext();
		}
		return result;
	}

	private Object executeScript(String expression,
			ExecutionContext executionContext,
			List<VariableAccess> variableAccesses) 
	{
		String user = AuthenticationUtil.getFullyAuthenticatedUser();
		if (runas == null && user !=null)
		{
             return executeScript(executionContext, services, expression, variableAccesses);
		}
		else
        {
			String runAsUser = runas;
    		if(runAsUser == null)
    		{
    			runAsUser = AuthenticationUtil.getSystemUserName();
    		} else
    		{
    			validateRunAsUser();
    		}
        	return executeScriptAs(runAsUser, expression, executionContext, services, variableAccesses);
        }
	}

	private static Object executeScriptAs(String runAsUser,
			final String expression,
			final ExecutionContext executionContext,
			final ServiceRegistry services,
			final List<VariableAccess> variableAccesses) {
		// execute as specified runAsUser
		return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
		{
			public Object doWork() throws Exception
			{
				return executeScript(executionContext, services, expression, variableAccesses);
			}
		}, runAsUser);
	}

	/**
	 * Checks a valid Fully Authenticated User is set.
	 * If none is set then attempts to set the task assignee as the Fully Authenticated User.
	 * @param executionContext
	 * @return <code>true</code> if the Fully Authenticated User was changes, otherwise <code>false</code>.
	 */
	private boolean checkFullyAuthenticatedUser(final ExecutionContext executionContext) {
		if(AuthenticationUtil.getFullyAuthenticatedUser()!= null)
			return false;
		TaskInstance taskInstance = executionContext.getTaskInstance();
	    if(taskInstance!=null)
	    {
			String userName = taskInstance.getActorId();
			if (userName != null)
			{
				AuthenticationUtil.setFullyAuthenticatedUser(userName);
				return true;
			}
	    }
		return false;
	}

	/**
	 * Checks that the specified 'runas' field
	 * specifies a valid username.
	 */
	private void validateRunAsUser() {
		Boolean runAsExists = AuthenticationUtil.runAs(new RunAsWork<Boolean>()
		{	// Validate using System user to ensure sufficient permissions available to access person node.
			
			public Boolean doWork() throws Exception {
				return services.getPersonService().personExists(runas);
			}
		}, AuthenticationUtil.getSystemUserName());
		if (!runAsExists)
		{
			throw new WorkflowException("runas user '" + runas + "' does not exist.");
		}
	}

    /**
     * Gets the expression {@link String} from the script.
     * @param isTextOnly Is the script text only or is it XML?
     * @return the expression {@link String}.
     */
	private String getExpression(boolean isTextOnly) {
        if (isTextOnly)
        {
            return script.getText().trim();
        }
        else
        {
            Element expressionElement = script.element("expression");
            if (expressionElement == null)
            {
                throw new WorkflowException("Script expression has not been provided");
            }
            return expressionElement.getText().trim();
        }
	}

	@SuppressWarnings("unchecked")
	private List<VariableAccess> getVariableAccessors(boolean isTextOnly) {
        if(isTextOnly)
        {
        	return null;
        }
        else
        {
        	return jpdlReader.readVariableAccesses(script);
        }
	}

    /**
     * Is the script specified as text only, or as explicit expression, variable elements
     * @return
     */
	@SuppressWarnings("unchecked")
	private boolean isScriptOnlyText() {
        Iterator<Element> iter = script.elementIterator();
        while (iter.hasNext())
        {
           Element element = iter.next();
           if (element.getNodeType() == org.dom4j.Node.ELEMENT_NODE)
           {
              return false;
           }
        }
		return true;
	}

    
    /**
     * Execute a script
     * 
     * @param context  jBPM execution context
     * @param services  Alfresco service registry
     * @param expression  script to execute
     * @param variableAccesses  (optional) list of jBPM variables to map into script (all, if not supplied)
     * @return  script result
     */
    public static Object executeScript(ExecutionContext context, ServiceRegistry services, String expression, List<VariableAccess> variableAccesses)
    {
        Map<String, Object> inputMap = createInputMap(context, services, variableAccesses);
        ScriptService scriptService = services.getScriptService();
        scriptService.buildCoreModel(inputMap);
        Object result = scriptService.executeScriptString(expression, inputMap);
        result = convertForJBPM(result, services);
        return result;
    }
    
    /**
     * Convert values for JBPM Context
     * 
     * @param value
     * @param services
     * @return
     */
    @SuppressWarnings("unchecked")
    private static Object convertForJBPM(Object value, ServiceRegistry services)
    {
        if (value == null)
        {
            return null;
        }
        else if (value instanceof NodeRef)
        {
            value = new JBPMNode(((NodeRef)value), services);
        }
        else if (value instanceof Collection)
        {
            // recursively convert each value in the collection
            Collection<Object> collection = (Collection<Object>)value;
            
            // Note: this needs to be cleaned up - we need to create appropriate collection type based
            //       on collection contents
            boolean isNodeCollection = false;
            for (Object obj : collection)
            {
                if (obj instanceof NodeRef)
                {
                    isNodeCollection = true;
                    break;
                }
            }
            
            if (isNodeCollection)
            {
                JBPMNodeList converted = new JBPMNodeList();
                for (Object obj : collection)
                {
                    if (!(obj instanceof NodeRef))
                    {
                        throw new WorkflowException("Unable to convert script collection to JBPM value - mixed node/non-node collection");
                    }
                    converted.add((JBPMNode)convertForJBPM(obj, services));
                }
                value = converted;
            }
            else
            {
                Collection<Object> converted = new ArrayList<Object>();
                for (Object obj : collection)
                {
                    converted.add(convertForJBPM(obj, services));
                }
                value = converted;
            }
        }
        return value;
    }

    /**
     * Construct map of arguments to pass to script
     * 
     * Based on the <variable> elements of the action configuration.
     * 
     * @param executionContext  the execution context
     * @param variableAccesses  the variable configuration
     * @return  the map of script arguments
     */
    private static Map<String, Object> createInputMap(ExecutionContext executionContext, ServiceRegistry services, List<VariableAccess> variableAccesses)
    {
        Map<String, Object> inputMap = new HashMap<String, Object>();

        // initialise global script variables
        JBPMNode personNode = getPersonNode(executionContext, services);
        if (personNode != null)
        {
            inputMap.put("person", personNode );
            NodeRef homeSpace = (NodeRef)services.getNodeService().getProperty(personNode.getNodeRef(), ContentModel.PROP_HOMEFOLDER);
            if (homeSpace != null)
            {
                inputMap.put("userhome", new JBPMNode(homeSpace, services));
            }
        }
        
        // initialise process variables
        Token token = executionContext.getToken();
        inputMap.put("executionContext", executionContext);
        inputMap.put("token", token);
        Node node = executionContext.getNode();
		if (node != null)
        {
            inputMap.put("node", node);
        }
        Task task = executionContext.getTask();
        if (task != null)
        {
            inputMap.put("task", task);
        }
        TaskInstance taskInstance = executionContext.getTaskInstance();
		if (taskInstance != null)
        {
            inputMap.put("taskInstance", taskInstance);
        }

        // if no readable variableInstances are specified,
        ContextInstance contextInstance = executionContext.getContextInstance();
        if (!hasReadableVariable(variableAccesses))
        {
            // copy all the variableInstances of the context into the interpreter
            Map<?, ?> variables = contextInstance.getVariables(token);
            if (variables != null)
            {
                for (Map.Entry<?, ?> entry : variables.entrySet())
                {
                    String variableName = (String) entry.getKey();
                    Object variableValue = entry.getValue();
                    inputMap.put(variableName, variableValue);
                }
            }
        }
        else
        {
            // copy the specified variableInstances into the interpreterz
            for (VariableAccess variableAccess : variableAccesses)
            {
                if (variableAccess.isReadable())
                {
                    String variableName = variableAccess.getVariableName();
                    String mappedName = variableAccess.getMappedName();
                    Object variableValue = contextInstance.getVariable(variableName, token);
                    inputMap.put(mappedName, variableValue);
                }
            }
        }

        return inputMap;
    }


	private static JBPMNode getPersonNode(ExecutionContext executionContext, ServiceRegistry services) {
		String userName = AuthenticationUtil.getFullyAuthenticatedUser();
		if(userName != null)
		{
			NodeRef person = services.getPersonService().getPerson(userName);
			if(person !=null)
			{
				return new JBPMNode(person, services);
			}
		}
		return null;
	}
    
    
    /**
     * Determine if there are variables to read from the process context
     * 
     * @param variableAccesses  the variables configuration
     * @return  true => there are variables to read
     */
    private static boolean hasReadableVariable(List<VariableAccess> variableAccesses)
    {
        if (variableAccesses != null)
        {
            for (VariableAccess variableAccess : variableAccesses)
            {
                if (variableAccess.isReadable())
                {
                    return true;
                }
            }
        }
        return false;
    }

    
    /**
     * Determine if there is a variable to write back to the process context
     * 
     * @param variableAccesses  the variables configuration
     * @return  true => there is a variable to write
     */
    private static VariableAccess getWritableVariable(List<VariableAccess> variableAccesses)
    {
        VariableAccess writable = null;
        if (variableAccesses != null)
        {
            for (VariableAccess variableAccess : variableAccesses)
            {
                if (variableAccess.isWritable())
                {
                    if (writable != null)
                    {
                        throw new WorkflowException("AlfrescoJavaScript supports only one writable variable");
                    }
                    writable = variableAccess;
                }
            }
        }
        return writable;
    }
    
    public void setScript(Element script) {
		this.script = script;
	}
    
    public void setRunas(String runas) {
		this.runas = runas;
	}
    
}
