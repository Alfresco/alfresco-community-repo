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

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.action.ActionConditionImpl;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.action.CompositeActionImpl;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author unknown
 *
 */
public class RulePost extends AbstractRuleWebScript
{    
    @SuppressWarnings("unused")
    private static Log logger = LogFactory.getLog(RulePost.class);

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {     
        Map<String, Object> model = new HashMap<String, Object>();
        
        // get request parameters
        NodeRef nodeRef = parseRequestForNodeRef(req);
        
        Rule rule = null;
        JSONObject json = null;
        
        try
        {
            // read request json
            json = new JSONObject(new JSONTokener(req.getContent().getContent()));
            
            // parse request json
            rule = parseJsonRule(json);
            
            // create rule
            ruleService.saveRule(nodeRef, rule);
            
            model.put("rule", rule);  
            model.put("storeType", nodeRef.getStoreRef().getProtocol());
            model.put("storeId", nodeRef.getStoreRef().getIdentifier());
            model.put("id", nodeRef.getId());
        }
        catch (IOException iox)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Could not read content from req.", iox);
        }
        catch (JSONException je)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                        "Could not parse JSON from req.", je);
        }
        
        return model;
    }
    
    protected Rule parseJsonRule(JSONObject jsonRule) throws JSONException
    {
        Rule result = new Rule();
        
        if (jsonRule.has("title") == false || jsonRule.getString("title").length() == 0)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Title missing when creating rule");
        }
        
        result.setTitle(jsonRule.getString("title"));
        
        result.setDescription(jsonRule.has("description") ? jsonRule.getString("description") : "");
        
        if (jsonRule.has("ruleType") == false || jsonRule.getJSONArray("ruleType").length() == 0)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
            "Rule type missing when creating rule");
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
            result.setParameterValues(parseJsonParameterValues(jsonParameterValues));
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
                ((CompositeActionImpl)result).addAction(innerAction);
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
        String id = jsonActionCondition.has("id") ? jsonActionCondition.getString("id"): GUID.generate();            
        
        ActionConditionImpl result = new ActionConditionImpl(id, jsonActionCondition.getString("conditionDefinitionName"));
        
        if (jsonActionCondition.has("invertCondition"))
        {
            result.setInvertCondition(jsonActionCondition.getBoolean("invertCondition"));
        }
        
        if (jsonActionCondition.has("parameterValues"))
        {
            JSONObject jsonParameterValues = jsonActionCondition.getJSONObject("parameterValues");
            
            result.setParameterValues(parseJsonParameterValues(jsonParameterValues));
        }
        
        return result;
    }
    
    protected Map<String, Serializable> parseJsonParameterValues(JSONObject jsonParameterValues) throws JSONException
    {
            Map<String, Serializable> parameterValues = new HashMap<String, Serializable>();
            
        // get parameters names
            JSONArray names = jsonParameterValues.names();
            
            for (int i = 0; i < names.length(); i++)
            {
            String propertyName = names.getString(i);
            Object propertyValue = jsonParameterValues.get(propertyName);
            
            // get parameter repository type
            QName typeQName = getPropertyType(propertyName);   
            
            if (typeQName == null)
            {
                if (propertyValue.toString().equals("true") || propertyValue.toString().equals("false"))
                {
                    typeQName = DataTypeDefinition.BOOLEAN;
                }
                else
                {
                    typeQName = DataTypeDefinition.TEXT;
                }
            }
            
            Serializable value = null;
            
            if (typeQName.equals(DataTypeDefinition.ANY))
            {
                try
                {
                    value = dateFormate.parse(propertyValue.toString());                    
                }
                catch (ParseException e)
                {
                    try
                    {
                        value = Long.valueOf(propertyValue.toString());
                    }
                    catch (NumberFormatException e1)
                    {
                        // do nothing
                    }
                }
            }
            
            if (value == null)
            {
            // convert to correct repository type
                value = (Serializable)DefaultTypeConverter.INSTANCE.convert(dictionaryService.getDataType(typeQName), propertyValue);
            }
            
            parameterValues.put(propertyName, value);
        }
        
        return parameterValues;
    }
}
