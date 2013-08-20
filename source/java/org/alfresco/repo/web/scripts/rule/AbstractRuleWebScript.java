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
package org.alfresco.repo.web.scripts.rule;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.action.ActionConditionImpl;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.action.CompositeActionImpl;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.action.ParameterizedItemDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author unknown
 *
 */
public abstract class AbstractRuleWebScript extends DeclarativeWebScript
{

    public static final SimpleDateFormat dateFormate = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");

    private static final String RULE_OUTBOUND = "outbound";
    private static final String ACTION_CHECK_OUT = "check-out";

    private static final String CANNOT_CREATE_RULE = "cannot.create.rule.checkout.outbound";
    
    protected NodeService nodeService;
    protected RuleService ruleService;
    protected DictionaryService dictionaryService;
    protected ActionService actionService;
    protected FileFolderService fileFolderService;
    protected NamespaceService namespaceService;

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
     * Set file folder service instance
     * 
     * @param fileFolderService the fileFolderService to set
     */
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
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
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find node: " + nodeRef.toString());
        }

        return nodeRef;
    }

    protected Rule parseJsonRule(JSONObject jsonRule) throws JSONException
    {
        Rule result = new Rule();

        if (jsonRule.has("title") == false || jsonRule.getString("title").length() == 0)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Title missing when creating rule");
        }

        result.setTitle(jsonRule.getString("title"));

        result.setDescription(jsonRule.has("description") ? jsonRule.getString("description") : "");

        if (jsonRule.has("ruleType") == false || jsonRule.getJSONArray("ruleType").length() == 0)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Rule type missing when creating rule");
        }

        JSONArray types = jsonRule.getJSONArray("ruleType");
        List<String> ruleTypes = new ArrayList<String>();

        for (int i = 0; i < types.length(); i++)
        {
            ruleTypes.add(types.getString(i));
        }

        result.setRuleTypes(ruleTypes);

        result.applyToChildren(jsonRule.has("applyToChildren") ? jsonRule.getBoolean("applyToChildren") : false);

        result.setExecuteAsynchronously(jsonRule.has("executeAsynchronously") ? jsonRule.getBoolean("executeAsynchronously") : false);

        result.setRuleDisabled(jsonRule.has("disabled") ? jsonRule.getBoolean("disabled") : false);

        JSONObject jsonAction = jsonRule.getJSONObject("action");

        // parse action object
        Action ruleAction = parseJsonAction(jsonAction);

        result.setAction(ruleAction);

        return result;
    }

    protected ActionImpl parseJsonAction(JSONObject jsonAction) throws JSONException
    {
        ActionImpl result = null;

        String actionId = jsonAction.has("id") ? jsonAction.getString("id") : GUID.generate();

        if (jsonAction.getString("actionDefinitionName").equalsIgnoreCase("composite-action"))
        {
            result = new CompositeActionImpl(null, actionId);
        }
        else
        {
            result = new ActionImpl(null, actionId, jsonAction.getString("actionDefinitionName"));
        }

        // Post Action Queue parameter
        if (jsonAction.has("actionedUponNode"))
        {
            NodeRef actionedUponNode = new NodeRef(jsonAction.getString("actionedUponNode"));
            result.setNodeRef(actionedUponNode);
        }

        if (jsonAction.has("description"))
        {
            result.setDescription(jsonAction.getString("description"));
        }

        if (jsonAction.has("title"))
        {
            result.setTitle(jsonAction.getString("title"));
        }

        if (jsonAction.has("parameterValues"))
        {
            JSONObject jsonParameterValues = jsonAction.getJSONObject("parameterValues");
            result.setParameterValues(parseJsonParameterValues(jsonParameterValues, result.getActionDefinitionName(), true));
        }

        if (jsonAction.has("executeAsync"))
        {
            result.setExecuteAsynchronously(jsonAction.getBoolean("executeAsync"));
        }

        if (jsonAction.has("runAsUser"))
        {
            result.setRunAsUser(jsonAction.getString("runAsUser"));
        }

        if (jsonAction.has("actions"))
        {
            JSONArray jsonActions = jsonAction.getJSONArray("actions");

            for (int i = 0; i < jsonActions.length(); i++)
            {
                JSONObject innerJsonAction = jsonActions.getJSONObject(i);

                Action innerAction = parseJsonAction(innerJsonAction);

                // we assume that only composite-action contains actions json array, so should be no cast exception
                ((CompositeActionImpl) result).addAction(innerAction);
            }
        }

        if (jsonAction.has("conditions"))
        {
            JSONArray jsonConditions = jsonAction.getJSONArray("conditions");

            for (int i = 0; i < jsonConditions.length(); i++)
            {
                JSONObject jsonCondition = jsonConditions.getJSONObject(i);

                // parse action conditions
                ActionCondition actionCondition = parseJsonActionCondition(jsonCondition);

                result.getActionConditions().add(actionCondition);
            }
        }

        if (jsonAction.has("compensatingAction"))
        {
            Action compensatingAction = parseJsonAction(jsonAction.getJSONObject("compensatingAction"));
            result.setCompensatingAction(compensatingAction);
        }

        return result;
    }

    protected ActionConditionImpl parseJsonActionCondition(JSONObject jsonActionCondition) throws JSONException
    {
        String id = jsonActionCondition.has("id") ? jsonActionCondition.getString("id") : GUID.generate();

        ActionConditionImpl result = new ActionConditionImpl(id, jsonActionCondition.getString("conditionDefinitionName"));

        if (jsonActionCondition.has("invertCondition"))
        {
            result.setInvertCondition(jsonActionCondition.getBoolean("invertCondition"));
        }

        if (jsonActionCondition.has("parameterValues"))
        {
            JSONObject jsonParameterValues = jsonActionCondition.getJSONObject("parameterValues");

            result.setParameterValues(parseJsonParameterValues(jsonParameterValues, result.getActionConditionDefinitionName(), false));
        }

        return result;
    }

    protected Map<String, Serializable> parseJsonParameterValues(JSONObject jsonParameterValues, String name, boolean isAction) throws JSONException
    {
        Map<String, Serializable> parameterValues = new HashMap<String, Serializable>();

        // get parameters names
        JSONArray names = jsonParameterValues.names();
        if (names == null)
        {
            return null;
        }

        // Get the action or condition definition
        ParameterizedItemDefinition definition = null;
        if (isAction == true)
        {
            definition = actionService.getActionDefinition(name);
        }
        else
        {
            definition = actionService.getActionConditionDefinition(name);
        }
        if (definition == null)
        {
            throw new AlfrescoRuntimeException("Could not find defintion for action/condition " + name);
        }

        for (int i = 0; i < names.length(); i++)
        {
            String propertyName = names.getString(i);
            Object propertyValue = jsonParameterValues.get(propertyName);
            
            // Get the parameter definition we care about
            ParameterDefinition paramDef = definition.getParameterDefintion(propertyName);
            if (paramDef == null && !definition.getAdhocPropertiesAllowed())
            {
                throw new AlfrescoRuntimeException("Invalid parameter " + propertyName + " for action/condition " + name);
            }
            if (paramDef != null)
            {
                QName typeQName = paramDef.getType();

                // Convert the property value
                Serializable value = convertValue(typeQName, propertyValue);
                parameterValues.put(propertyName, value);
            }
            else
            {
                // If there is no parameter definition we can only rely on the .toString() representation of the ad-hoc property
                parameterValues.put(propertyName, propertyValue.toString());
            }
            
        }

        return parameterValues;
    }
    
    private Serializable convertValue(QName typeQName, Object propertyValue) throws JSONException
    {        
        Serializable value = null;
        
        DataTypeDefinition typeDef = dictionaryService.getDataType(typeQName);
        if (typeDef == null)
        {
            throw new AlfrescoRuntimeException("Action property type definition " + typeQName.toPrefixString() + " is unknown.");
        }
        
        if (propertyValue instanceof JSONArray)
        {
            // Convert property type to java class
            Class<?> javaClass = null;
            
            String javaClassName = typeDef.getJavaClassName();
            try
            {
                javaClass = Class.forName(javaClassName);
            }
            catch (ClassNotFoundException e)
            {
                throw new DictionaryException("Java class " + javaClassName + " of property type " + typeDef.getName() + " is invalid", e);
            }
            
            int length = ((JSONArray)propertyValue).length();
            List<Serializable> list = new ArrayList<Serializable>(length);
            for (int i = 0; i < length; i++)
            {
                list.add(convertValue(typeQName, ((JSONArray)propertyValue).get(i)));
            }
            value = (Serializable)list;
        }
        else
        {
            if (typeQName.equals(DataTypeDefinition.QNAME) == true && 
                typeQName.toString().contains(":") == true)
            {
                 value = QName.createQName(propertyValue.toString(), namespaceService);
            }
            else
            {
                value = (Serializable)DefaultTypeConverter.INSTANCE.convert(dictionaryService.getDataType(typeQName), propertyValue);
            }
        }
        
        return value;
    }

    protected void checkRule(Rule rule)
    {
        List<String> ruleTypes = rule.getRuleTypes();
        if (ruleTypes.contains(RULE_OUTBOUND))
        {
            List<Action> actions = ((CompositeActionImpl) rule.getAction()).getActions();
            for (Action action : actions)
            {
                if (action.getActionDefinitionName().equalsIgnoreCase(ACTION_CHECK_OUT))
                {
                    throw new WebScriptException(CANNOT_CREATE_RULE);
                }
            }
        }
    }
}
