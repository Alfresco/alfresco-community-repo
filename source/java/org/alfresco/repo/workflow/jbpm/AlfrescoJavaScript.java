/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.workflow.jbpm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ValueConverter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.namespace.QName;
import org.dom4j.Element;
import org.jbpm.context.def.VariableAccess;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.Token;
import org.jbpm.jpdl.xml.JpdlXmlReader;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
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
    @SuppressWarnings("unchecked")
    public void execute(ExecutionContext executionContext) throws Exception
    {
        // validate script
        if (script == null)
        {
            throw new WorkflowException("Script has not been provided");
        }
        
        // extract action configuration
        String expression = null;
        List<VariableAccess> variableAccesses = null;        

        // is the script specified as text only, or as explicit expression, variable elements
        boolean isTextOnly = true;
        Iterator<Element> iter = script.elementIterator();
        while (iter.hasNext())
        {
           Element element = iter.next();
           if (element.getNodeType() == org.dom4j.Node.ELEMENT_NODE)
           {
              isTextOnly = false;
           }
        }
        
        // extract script and variables
        if (isTextOnly)
        {
            expression = script.getTextTrim();
        }
        else
        {
            variableAccesses = jpdlReader.readVariableAccesses(script);
            Element expressionElement = script.element("expression");
            if (expressionElement == null)
            {
                throw new WorkflowException("Script expression has not been provided");
            }
            expression = expressionElement.getTextTrim();
        }

        // execute
        Object result = executeScript(executionContext, services, expression, variableAccesses);

        // map script return variable to process context
        VariableAccess returnVariable = getWritableVariable(variableAccesses);
        if (returnVariable != null)
        {
            ContextInstance contextInstance = executionContext.getContextInstance();
            Token token = executionContext.getToken();
            contextInstance.setVariable(returnVariable.getVariableName(), result, token);            
        }
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
    @SuppressWarnings("unchecked")
    private static Map<String, Object> createInputMap(ExecutionContext executionContext, ServiceRegistry services, List<VariableAccess> variableAccesses)
    {
        Map<String, Object> inputMap = new HashMap<String, Object>();

        // initialise global script variables
        String userName = AuthenticationUtil.getCurrentUserName();
        NodeRef person = services.getPersonService().getPerson(userName);
        if (person != null)
        {
            inputMap.put("person", new JBPMNode(person, services));
            NodeRef homeSpace = (NodeRef)services.getNodeService().getProperty(person, ContentModel.PROP_HOMEFOLDER);
            if (homeSpace != null)
            {
                inputMap.put("userhome", new JBPMNode(homeSpace, services));
            }
        }
        
        // initialise process variables
        Token token = executionContext.getToken();
        inputMap.put("executionContext", executionContext);
        inputMap.put("token", token);
        if (executionContext.getNode() != null)
        {
            inputMap.put("node", executionContext.getNode());
        }
        if (executionContext.getTask() != null)
        {
            inputMap.put("task", executionContext.getTask());
        }
        if (executionContext.getTaskInstance() != null)
        {
            inputMap.put("taskInstance", executionContext.getTaskInstance());
        }

        // if no readable variableInstances are specified,
        ContextInstance contextInstance = executionContext.getContextInstance();
        if (!hasReadableVariable(variableAccesses))
        {
            // copy all the variableInstances of the context into the interpreter
            Map<String, Object> variables = contextInstance.getVariables(token);
            if (variables != null)
            {
                for (Map.Entry entry : variables.entrySet())
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
    
}
