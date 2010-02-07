/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.rule;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.service.cmr.action.ActionConditionDefinition;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author unknown
 *
 */
public abstract class AbstractRuleWebScript extends DeclarativeWebScript
{
    
    public static final SimpleDateFormat dateFormate = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
    
    protected NodeService nodeService;
    protected RuleService ruleService;
    protected DictionaryService dictionaryService;
    protected ActionService actionService;
    protected NamespaceService namespaceService;
    
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    private static Map<String, QName> propertyTypes = null;
    
    /**
     * Sets the node service instance
     * 
     * @param nodeService the node service to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Set rule service instance 
     * 
     * @param ruleService the rule service to set
     */
    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
    }
    
    /**
     * Set dictionary service instance
     * 
     * @param dictionaryService the dictionary service to set 
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    /**
     * Set action service instance
     * 
     * @param actionService the action service to set
     */
    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }
    
    /**
     * Set namespace service instance
     * 
     * @param namespaceService the namespace service to set
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    /**
     * Parses the request and providing it's valid returns the NodeRef.
     * 
     * @param req The webscript request
     * @return The NodeRef passed in the request
     * 
     */
    protected NodeRef parseRequestForNodeRef(WebScriptRequest req)
    {
        // get the parameters that represent the NodeRef, we know they are present
        // otherwise this webscript would not have matched
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String storeType = templateVars.get("store_type");
        String storeId = templateVars.get("store_id");
        String nodeId = templateVars.get("id");
        
        // create the NodeRef and ensure it is valid
        StoreRef storeRef = new StoreRef(storeType, storeId);
        NodeRef nodeRef = new NodeRef(storeRef, nodeId);
        
        if (!this.nodeService.exists(nodeRef))
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find node: " + 
                        nodeRef.toString());
        }
        
        return nodeRef;
    }
    
    protected QName getPropertyType(String propertyName)
    {
        if (propertyTypes == null)
        {            
            // no parameter types was cached
            propertyTypes = new HashMap<String, QName>();
            
            // get parameters for all action definitions
            List<ActionDefinition> actionDefinitions = actionService.getActionDefinitions();
            for (ActionDefinition actionDefinition : actionDefinitions)
            {
                List<ParameterDefinition> parameterDefinitions = actionDefinition.getParameterDefinitions();  
                
                for (ParameterDefinition parameterDefinition : parameterDefinitions)
                {
                    try
                    {
                        // cache parameter
                        lock.writeLock().lock();
                        propertyTypes.put(parameterDefinition.getName(), parameterDefinition.getType());
                    }
                    finally
                    {
                        lock.writeLock().unlock();
                    }
                }
            }
            
            // get parameters for all action condition definitions
            List<ActionConditionDefinition> actionConditionDefinitions = actionService.getActionConditionDefinitions();
            for (ActionConditionDefinition actionConditionDefinition : actionConditionDefinitions)
            {
                List<ParameterDefinition> parameterDefinitions = actionConditionDefinition.getParameterDefinitions();
                

                for (ParameterDefinition parameterDefinition : parameterDefinitions)
                {
                    try
                    {
                        // cache parameter
                        lock.writeLock().lock();
                        propertyTypes.put(parameterDefinition.getName(), parameterDefinition.getType());
                    }
                    finally
                    {
                        lock.writeLock().unlock();
                    }
                }
            }
        }
        
        QName result = null;
        try
        {
            // getting cached parameter type 
            lock.readLock().lock();
            result = propertyTypes.get(propertyName);
        }
        finally
        {
            lock.readLock().unlock();
        }
        
        return result;
    }
}
