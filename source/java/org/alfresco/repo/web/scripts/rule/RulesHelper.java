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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.web.scripts.rule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.model.Repository;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.action.ParameterizedItem;
import org.alfresco.service.cmr.action.ParameterizedItemDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.scripts.Status;
import org.alfresco.web.scripts.WebScriptException;
import org.alfresco.web.scripts.WebScriptRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class to hold helper methods for the Rules / Actions
 * REST API
 * 
 * @author glen johnson at alfresco com
 */
public class RulesHelper
{    
    // public constants for ActionQueueItem Statuses
    public static final String ACTION_QUEUE_ITEM_STATUS_PENDING = "PENDING";
    public static final String ACTION_QUEUE_ITEM_STATUS_COMPLETE = "COMPLETE";
    
    // private constants
    private static final String COMPOSITE_ACTION_DEF_NAME = "composite-action";
    private static final String REQ_URL_PART_NODE_REF = "/api/node";
    private static final String REQ_URL_PART_NODE_PATH = "/api/path";
    private static final String REQ_URL_PART_RULE_NODE_REF = "/api/rules";
   
    // service dependencies 
    private ServiceRegistry serviceRegistry;
    
    // dependencies
    private Repository repositoryContext = null;
    
    // list of valid rule type names
    List<String> validRuleTypeNames = null;
    
    /**
     * Set the service registry
     * 
     * @param serviceRegistry  the service registry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry) 
    {
        this.serviceRegistry = serviceRegistry;
    }
    
    /**
     * Set the repository context
     * 
     * @param repositoryContext the repositoryContext to set
     */
    public void setRepositoryContext(Repository repositoryContext)
    {
        this.repositoryContext = repositoryContext;
    }

    /**
     * Init method.
     */
    public void init()
    {
        // Register a list of valid rule type names
        RuleService ruleService = this.serviceRegistry.getRuleService();
        validRuleTypeNames = new ArrayList<String>();
        
        List<RuleType> validRuleTypes = ruleService.getRuleTypes();
        for (RuleType ruleType : validRuleTypes)
        {
            validRuleTypeNames.add(ruleType.getName());
        }
    }
    
    /**
     * Return if the given rule type name is a valid rule type
     * 
     * @param ruleTypeName The rule type for which we want to test validity
     * 
     * @return Whether or not the given rule type name is valid 
     */
    public boolean isValidRuleTypeName(String ruleTypeName)
    {
        return validRuleTypeNames.contains(ruleTypeName);
    }

    /**
     * Return a Rule object created/updated from a given rule JSON object (Rule Details sent).
     * 
     * If a node reference is passed into the <pre>ruleNodeRefToUpdate</pre> parameter,
     * then this indicates that an existing rule (identified by that node
     * reference) is to be updated from the given rule JSON object. If a 'null'
     * node reference is passed in, then this indicates that a new rule is to
     * be created from scratch.
     * 
     * @param ruleJson the rule JSON object to create/update the rule from
     * @param ruleNodeRefToUpdate The node reference of the rule to update.
     *              Set to <pre>null</pre> if a new rule is to be created from scratch. 
     *              
     * @return The rule created from the given rule JSON object
     */
    public Rule getRuleFromJson(JSONObject ruleJson, NodeRef ruleNodeRefToUpdate)
    {
        // get a reference to the rule service
        RuleService ruleService = this.serviceRegistry.getRuleService();
        
        // set a boolean flag indicating whether or not to update an existing rule
        boolean update = (ruleNodeRefToUpdate != null);
        
        //
        // update/create a rule object from the given rule JSON object
        //
        Rule rule = null;
        if (update == true)
        {
            // we are doing an update of an existing rule so get 
            // the rule identified by the given rule node reference
            // so that it can be updated with the rule details
            // in the given rule JSON object
            rule = ruleService.getRule(ruleNodeRefToUpdate);            
        }
        else
        {
            // we are not doing an update, so create a new rule from scratch 
            rule = new Rule();
        }
        
        try
        {
            //
            // set rule properties
            //
            
            if ((ruleJson.isNull("title") == true) && (update == false))
            {
                // the "title" field is mandatory, it is missing in the rule details,
                // and we are creating a new rule, so throw an exception
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "A new rule is being created but the 'title' "
                        + "field, which is mandatory, has not been included in the rule details"); 
            }
            // otherwise go ahead and set the value if the field is present
            else if (ruleJson.isNull("title") == false)
            {
                String ruleTitle = ruleJson.getString("title");
                rule.setTitle(ruleTitle);
            }
            
            if (ruleJson.isNull("description") == false)
            {
                rule.setDescription(ruleJson.getString("description"));
            }
            
            // set values from the respective Boolean fields below, but if the
            // if the value given for a field does not equate to either
            // 'true' or 'false', then set it to a default value of false
            
            if (ruleJson.isNull("executeAsync") == false)
            {
                rule.setExecuteAsynchronously(ruleJson.optBoolean("executeAsync", false));
            }
            
            
            if (ruleJson.isNull("ruleDisabled") == false)
            {
                rule.setRuleDisabled(ruleJson.optBoolean("ruleDisabled", false));
            }
                
            
            if (ruleJson.isNull("appliedToChildren") == false)
            {
                rule.applyToChildren(ruleJson.optBoolean("appliedToChildren", false));
            }
            
            //
            // set rule types present in the rule details onto the rule
            //
            
            if ((ruleJson.isNull("ruleTypes") == true) && (update == false))
            {
                // the "ruleTypes" field is mandatory,  it is missing in the rule details,
                // and we are creating a new rule so throw an exception
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "A new rule is being created but the 'ruleTypes' "
                        + "field, which is mandatory, has not been included in the rule details"); 
            }
            else if (ruleJson.isNull("ruleTypes") == false)
            {
                List<String> ruleTypes = new ArrayList<String>();
                JSONArray ruleTypesJson = ruleJson.getJSONArray("ruleTypes");
                int numRuleTypes = ruleTypesJson.length();
                
                // make sure that at least one rule type has been sent in the rule details
                if (numRuleTypes < 1)
                {
                    throw new WebScriptException(Status.STATUS_BAD_REQUEST, "At least one rule type needs to be present in "
                            + "in the rule details sent in the request content.");
                }
                            
                // add to the rule the rule type names sent in the rule details
                for (int i=0; i < numRuleTypes; i++)
                {
                    String ruleTypeNameJson = ruleTypesJson.getString(i);
                    if (isValidRuleTypeName(ruleTypeNameJson))
                    {
                        ruleTypes.add(ruleTypeNameJson);
                    }
                    else
                    {
                        throw new WebScriptException(Status.STATUS_BAD_REQUEST, "An invalid rule type name was given in the "
                                + "rule details sent in the request content. Invalid rule type name given is: '"
                                + ruleTypeNameJson + "'");
                    }
                }
                rule.setRuleTypes(ruleTypes);
            }
            
            if ((ruleJson.isNull("action") == true) && (update == false))
            {
                // the "action" field is mandatory, it is missing in the rule details,
                // and we are creating a new rule so throw an exception
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "A new rule is being created but the 'action' "
                        + "field, which is mandatory, has not been included in the rule details"); 
            }
            else
            {
                // set the action supplied in the rule details onto the rule
                JSONObject ruleActionJson = ruleJson.getJSONObject("action");
                
                // if we're doing an update then the rule should already have
                // this action set on it, so get the action object already on 
                // the rule
                Action ruleActionToUpdate = null;
                if (update == true)
                {
                    String ruleActionJsonId = ruleActionJson.getString("id");
                    ruleActionToUpdate = rule.getAction();
                    
                    // throw a web script exception if the ID of the rule's action,
                    // already persisted to the repository, is not the same as the one
                    // given for the rule action's ID in the rule details that we
                    // wish to perform the rule update with
                    if (ruleActionToUpdate.getId().equals(ruleActionJsonId) == false)
                    {
                        throw new WebScriptException(Status.STATUS_BAD_REQUEST, "The ID sent in the rule details "
                                + "of the action directly associated with the rule we wish to update does not match "
                                + " the rule's action already persisted in the repository. The rule's nodeRef is: '"
                                + ruleNodeRefToUpdate + "', the action ID provided in the rule details is '"
                                + ruleActionJsonId + "', and the ID for the rule's action already persisted in the"
                                + " repository is '" + ruleActionToUpdate.getId() + "'");
                    }
                }
                
                
                Action action = getActionFromJson(ruleActionJson, ruleActionToUpdate);
                rule.setAction(action);
            }
        }
        catch (JSONException je)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Problem creating rule from Rule Details sent in the request content.", je); 
        }
        
        return rule;
    }
    
    /**
     * Return an Action object created/updated from a given action JSON object (Action Details sent).
     * 
     * If an action object is passed into the <pre>actionToUpdate</pre> parameter,
     * then this indicates that this action is to be updated from the action details
     * provided in the given JSON object and then returned.
     * If a 'null' is passed into this parameter, then this indicates that a new action is to
     * be created from scratch and returned.
     * 
     * @param actionJson the action JSON object used to create/update the action with
     * @param actionToUpdate The action to be updated.
     *              Set to <pre>null</pre> if a new action is to be created from scratch. 
     *              
     * @return The action created/updated from the given action JSON object
     */
    @SuppressWarnings("unchecked")
    public Action getActionFromJson(JSONObject actionJson, Action actionToUpdate)
    {
        ActionService actionService = this.serviceRegistry.getActionService();
        
        //
        // if we are doing an update then set the action, to be returned, to
        // the given actionToUpdate (which will then be updated by the action
        // details provided in the action JSON - containing the action details sent)
        //
        
        Action action = null;
        if (actionToUpdate != null)
        {
            action = actionToUpdate;
        }
        
        try
        {
            //    
            // create an action object from an "action" JSON object
            //
            
            String actionDefinitionName = actionJson.getString("actionDefinitionName");
            JSONArray nestedActionsJson = actionJson.optJSONArray("actions");
            
            // if action's definition name denotes that it is a composite action and the
            // action JSON object has nested actions, then treat it as a composite action
            if ((actionDefinitionName.equals(COMPOSITE_ACTION_DEF_NAME)) == true && (nestedActionsJson != null))
            {
                // if we are updating an existing action and the given
                // actionToUpdate is not a composite action, then throw a
                // web script exception
                if ((actionToUpdate != null) && ((actionToUpdate instanceof CompositeAction) == false))
                {
                    throw new WebScriptException(Status.STATUS_BAD_REQUEST, "The action directly associated with the "
                            + "rule you wish to update is not a composite action. Thus this action could not be updated with "
                            + "the given action JSON - the action details sent");
                }
                
                // TODO remove this look-up map when the back-end provides an easy way to lookup actions
                // on a composite action by ID
                
                // if we are updating an existing composite action then create a map to easily look up the
                // nested action objects for each nested action details provided in the action JSON object
                Map<String, Action> nestedActionsMap = new HashMap<String, Action>();  
                if ((actionToUpdate != null) && ((actionToUpdate instanceof CompositeAction) == true))
                {
                    List<Action> nestedActions = ((CompositeAction)action).getActions();
                    for (Action nestedAction: nestedActions)
                    {
                        nestedActionsMap.put(nestedAction.getId(), nestedAction);
                    }
                }
                // else if we are not updating then create composite action object
                // for scratch
                else if (actionToUpdate == null)
                {
                    action = actionService.createCompositeAction();
                }
                
                // recursively add nested actions to this composite action
                // as some of those nested actions could also be composite actions
                int numNestedActionsJson = nestedActionsJson.length();
                for (int i=0; i < numNestedActionsJson; i++)
                {
                    JSONObject nestedActionJson = nestedActionsJson.optJSONObject(i);
                    
                    if (nestedActionJson != null)
                    {
                        Action nestedAction = null;
                        
                        // if we are doing an action update, then update the nested actions from 
                        // the nested action JSON 
                        if (actionToUpdate != null)
                        {    
                            String nestedActionJsonID = nestedActionJson.getString("id");
                            
                            // lookup to see if nested action from nested action JSON
                            // already exists on composite action, in which case, update
                            // the nested action with the nested action details provided in the
                            // nested action JSON
                            Action nestedActionToUpdate = nestedActionsMap.get(nestedActionJsonID);
                            if (nestedActionToUpdate != null)
                            {
                                // remove the existing nested action to then be updated below with the
                                // updated one
                                
                                nestedAction = getActionFromJson(nestedActionJson, nestedActionToUpdate);
                            }
                        }
                        // else we are not doing an action update so just pass in the
                        // actionToUpdate as 'null'
                        else
                        {
                            nestedAction = getActionFromJson(nestedActionJson, null);
                        }
                        
                        ((CompositeAction)action).addAction(nestedAction);
                    }
                }
            }
            // else if the action definition name denotes that this is not a composite action,
            // but nested actions have been provided in the action JSON Object
            // then throw a Web Script Exception
            else if ((actionDefinitionName.equals(COMPOSITE_ACTION_DEF_NAME) == false) && (nestedActionsJson != null))
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Nested actions were sent in the action details "
                        + "having Title: '" + actionJson.optString("title") + "', but the action definition "
                        + "name thereof is not '" + COMPOSITE_ACTION_DEF_NAME + "' as expected. Instead, the action's "
                        + "definition name is '" + actionDefinitionName + "'.");
            }
            // else the action's definition name is 'composite-action' but no nested actions were provided in the action JSON
            // (in which case we will just treat the action as a non-composite action anyway), otherwise the action is not 
            // defined as a composite action, and no nested actions were sent in the action JSON, so just create it as a
            // non-composite action
            else
            {
                action = actionService.createAction(actionDefinitionName); 
            }
            
            //
            // set action properties
            //
            
            
            if ((actionJson.isNull("title") == true) && (actionToUpdate == null))
            {
                // the "title" field is mandatory, it is missing in the rule details,
                // and we are creating a new rule, so throw an exception
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "A new rule is being created but the 'title' "
                        + "field, which is mandatory, has not been included in the rule details"); 
            }
            // otherwise go ahead and set the value if the field is present
            else if (actionJson.isNull("title") == false)
            {
                action.setTitle(actionJson.getString("title"));
            }
            
            if (actionJson.isNull("description") == false)
            {
                action.setDescription(actionJson.optString("description"));
            }
            
            // If a value has been provided in the action details for the "executeAsync" field
            // then set it to the action object.
            // If the value given for a field does not equate to either
            // 'true' or 'false', then set it to a default value of false
            
            if (actionJson.isNull("executeAsync") == false)
            {
                action.setExecuteAsynchronously(actionJson.optBoolean("executeAsync", false));
            }
            
            // set compensating action on current action if a compensating action is present
            // in the action JSON Object
            if (actionJson.isNull("compensatingAction") == false)
            {
                JSONObject compActionJson = actionJson.getJSONObject("compensatingAction");
                Action compActionToUpdate = null;
                
                if (actionToUpdate != null)
                {
                    compActionToUpdate = action.getCompensatingAction();
                }
                Action compAction = getActionFromJson(compActionJson, compActionToUpdate);
                    
                action.setCompensatingAction(compAction);
            }
            
            // get the action's definition
            ParameterizedItemDefinition actionDef = actionService.getActionDefinition(actionDefinitionName);
            
            // if there are parameter values in the action JSON object then
            // set them onto the action object
            //
            JSONObject actionParamValuesJson = actionJson.optJSONObject("parameterValues");
            if (actionParamValuesJson != null)
            {
                setParameterValuesOnParameterizedItemFromJson(action, actionDef, actionParamValuesJson);
            }
            
            //
            // set conditions on the current action
            //
            
            if (actionJson.isNull("conditions") == false)
            {
                JSONArray conditionsJson = actionJson.getJSONArray("conditions");
                
                // if we are doing an update then build up a condition map
                // do be able to do a condition look-up by ID for each condition included 
                // in the condition JSON - the condition details
                
                Map<String, ActionCondition> conditionsMap = new HashMap<String, ActionCondition>();
                if (actionToUpdate != null)
                {
                    List<ActionCondition> actionConditions = actionToUpdate.getActionConditions();
                    for (ActionCondition actionCondition : actionConditions)
                    {
                        conditionsMap.put(actionCondition.getId(), actionCondition);
                    }
                }
                
                // get each condition and add it to the action
                
                int numConditionsJson = conditionsJson.length();
                for (int conditionJsonIndex = 0; conditionJsonIndex < numConditionsJson; conditionJsonIndex++)
                {
                    ActionCondition condition = null;
                    JSONObject conditionJson = conditionsJson.getJSONObject(conditionJsonIndex);
                    String conditionDefName = null;
                    
                    // if we are doing an update, then get the existing condition matching
                    // the condition ID given in the condition JSON, and update that with
                    // the condition fields given therein
                    if (actionToUpdate != null)
                    {
                        String conditionJsonId = conditionJson.getString("id");
                        condition = conditionsMap.get(conditionJsonId);
                        conditionDefName = condition.getActionConditionDefinitionName();
                    }
                    // we are not doing an update, so create the condition using the given condition
                    // definition name and then populate this new condition from the fields given in
                    // the condition JSON
                    else
                    {
                        // we are not doing an update, so if the conditionDefinitionName has not been provided 
                        // in the condition JSON then throw a web script exception
                        if (conditionJson.isNull("conditionDefinitionName"))
                        {
                            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "A condition details at index: '"
                                    + conditionJsonIndex + "' for action ID '" + action.getId() + " could not be created "
                                    + "because the 'conditionDefinitionName' field is missing from the condition details sent");
                        }
                        // else condition def name has been provided in the condition details
                        else
                        {
                            conditionDefName = conditionJson.getString("conditionDefinitionName");
                        }
                        
                        condition = actionService.createActionCondition(conditionDefName);
                    }
                    
                    // get the condition definition object
                    ParameterizedItemDefinition conditionDef = actionService.getActionConditionDefinition(conditionDefName);
                    
                    //
                    // set the condition's properties
                    //
                    
                    // Set the value for the 'invertCondition' field if that field is sent
                    // in the condition JSON. If the value is sent, but it does not equate to 
                    // either 'true' or 'false', then set it to a default value of false
                    if (conditionJson.isNull("invertCondition") == false)
                    {
                        condition.setInvertCondition(conditionJson.getBoolean("invertCondition"));
                    }
                    
                    //
                    // if there are parameter values on the condition JSON object
                    // then apply them to the condition object
                    //
                    JSONObject condParamValuesJson = conditionJson.optJSONObject("parameterValues");
                    if (condParamValuesJson != null)
                    {
                        setParameterValuesOnParameterizedItemFromJson(condition, conditionDef, condParamValuesJson);
                    }
                    
                    // add condition to action object
                    action.addActionCondition(condition);
                    
                    // increment the condition JSON index
                    conditionJsonIndex++;
                }
            }
        }
        catch (JSONException je)
        {
            
            
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR,
                    "Problem creating rule from JSON passed into Web Script.", je); 
        }
        
        return action;
    }
    
    /**
     * Create and return a multi-valued parameter value from the given JSON object and
     * parameter content type (QName)
     * 
     * @param jsonObject The JSON object we want to convert to a multi-valued parameter value
     * @param paramName The parameter name
     * @param paramDataTypeDef The parameter's content type definition
     * @return
     */
    @SuppressWarnings("unchecked")
    public Serializable getMultiValuedParameterValueFromJsonObject(JSONObject jsonObject, String paramName, 
            DataTypeDefinition paramDataTypeDef)
    {
        ArrayList multiParamValue = new ArrayList<Serializable>(jsonObject.length());
        
        // 
        // convert each parameter item String value from the given JSON object to a value 
        // corresponding to the given parameter content type and add that converted value 
        // to the multi-parameter array list
        //
        Iterator<String> keysIterator = jsonObject.keys();
        while (keysIterator.hasNext())
        {
            String key = keysIterator.next();
            String paramValueItem = jsonObject.optString(key);
            
            // if this keyed object from the given JSON object
            // is present and can be represented as a String,
            // then convert the string to the given parameter content
            // type and add it to the multi-parameter array list
            if (paramValueItem != null)
            {
                Object paramValueItemObj = DefaultTypeConverter.INSTANCE.convert(paramDataTypeDef, paramValueItem);
                // if the parameter value object, converted using the parameter's data type, is of type Serializable
                // then we can go ahead and add it as one of the parameter's values
                if (paramValueItemObj instanceof Serializable)
                {
                    multiParamValue.add(paramValueItemObj);
                }
                else
                // otherwise the converted parameter value can't be added
                {
                    throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR,
                            "Whilst building up the multi-valued parameter with name: '"
                        +   paramName + "' and type: '" + paramDataTypeDef.getName() + "' an object was encountered "
                        +   "that doesn't implement Serializable. Object value is: '" + paramValueItemObj
                        +   "' and is of class name '" + paramValueItemObj.getClass().getName() +"'");
                }
            }
        }
        
        return multiParamValue;
    }
    
    /**
     * Sets the parameter values from the given parameter values JSON object onto the given parameterized item object 
     * 
     * @param parameterizedItem The parameterized item object onto which we want to set the parameter values from
     *                              the given parameter values JSON object
     * @param parameterizedItemDef The definition for the given parameterized item
     * @param paramValuesJson The JSON object containing the parameter values to set into the parameterized item object
     */
    @SuppressWarnings("unchecked")
    public void setParameterValuesOnParameterizedItemFromJson(ParameterizedItem parameterizedItem,
            ParameterizedItemDefinition parameterizedItemDef, JSONObject paramValuesJson)
    {
        // get a reference to the dictionary service
        DictionaryService dictionaryService = this.serviceRegistry.getDictionaryService();
        
        Iterator<String> paramIterator = paramValuesJson.keys();
        while (paramIterator.hasNext())
        {
            String paramName = paramIterator.next();
            ParameterDefinition paramDef = parameterizedItemDef.getParameterDefintion(paramName);
            QName paramType = paramDef.getType();
            DataTypeDefinition paramDataTypeDef = dictionaryService.getDataType(paramType);
            boolean isMultiValued = paramDef.isMultiValued();
            boolean isMandatory = paramDef.isMandatory();
            
            // throw web script exception if mandatory parameter value not provided
            if (paramValuesJson.isNull(paramName) && (isMandatory == true))
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Mandatory parameter with name: '"
                        + paramName + "' expected in sent content, but it was not present. Parameter belongs to "
                        + "parameterized item with ID: '" + parameterizedItem.getId() + "' and definition name: '"
                        + parameterizedItemDef.getName() + "'"); 
            }
            // else if a parameter value has been provided then handle this case
            else if (paramValuesJson.isNull(paramName) == false)
            {
                // Try and get the parameter value as a JSON object (multi-valued parameter value).
                JSONObject paramValueJsonObj = paramValuesJson.optJSONObject(paramName);
                
                // throw web script exception if parameter value is not a JSONObject (optJSONObject() returned 'null')
                // and parameter's definition says that it should be multi-valued
                if ((paramValueJsonObj == null) && (isMultiValued == true))
                {
                    throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Multi-valued parameter with name: '"
                            + paramName + "' expected in sent content, but value provided was not multi-valued."
                            + "Parameter value given is: " + paramValuesJson.opt(paramName) + ". "
                            + "Parameter belongs to parameterized item with ID: '" + parameterizedItem.getId()
                            + "' and definition name: '" + parameterizedItemDef.getName() + "'"); 
                }
                // else throw web script exception if value given is a JSON object
                // (optJSONObject(paramName) didn't return 'null'), but parameter's definition says that it shouldn't
                // be multi-valued 
                else if ((paramValueJsonObj != null) && (isMultiValued == false))
                {
                    throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Multi-valued parameter with name: '"
                            + paramName + "' was sent in content, but parameter's definition says that it should not "
                            + "be multi-valued. Parameter value given is: " + paramValuesJson.opt(paramName) + ". "
                            + "Parameter belongs to parameterized item with ID: '" + parameterizedItem.getId()
                            + "' and definition name: '" + parameterizedItemDef.getName() + "'"); 
                }
                // else if value given is a JSON object (optJSONObject(paramName) didn't return 'null'),
                // and the parameter's definition says that it should be multi-valued
                // then go ahead and add the multi-valued parameter value to the parameterized item  
                else if ((paramValueJsonObj != null) && (isMultiValued == true))
                {
                    Serializable multiParamValue = getMultiValuedParameterValueFromJsonObject(paramValueJsonObj,
                            paramName, paramDataTypeDef);
                    parameterizedItem.setParameterValue(paramName, multiParamValue);
                }
                // else if parameter value provided is not a JSON Object (optJSONObject(paramName) returned 'null')
                // and the parameter's definition says that the parameter value should not be multi-valued, try and 
                // retrieve the parameter value as a String instead 
                else if ((paramValueJsonObj == null) && (isMultiValued == false))
                {
                    String paramValueStr = paramValuesJson.optString(paramName);
                    // if parameter value can be retrieved as a String, then add that String parameter
                    // value to the parameterized item
                    if (paramValueStr != null)
                    {
                        Object paramValueObj = DefaultTypeConverter.INSTANCE.convert(paramDataTypeDef, paramValueStr);
                        if (paramValueObj instanceof Serializable)
                        {
                            parameterizedItem.setParameterValue(paramName, (Serializable)paramValueObj);
                        }
                        else
                        {
                            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR,
                                    "Whilst adding parameters to a parameterized item (action or condition) "
                                +   "with ID '" + parameterizedItem.getId() + "' the parameter with name: '"
                                +   paramName + "' and type: '" + paramType + "' has a value that doesn't "
                                +   "implement Serializable. Object value is: '" + paramValueObj
                                +   "' and is of class name '" + paramValueObj.getClass().getName() +"'");
                        }
                    }
                    // else parameter value could not be retrieved as a String value (optString(paramName) returned 'null')
                    // so give up with trying to process it and throw a web script exception
                    else
                    {
                        throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Parameter with name: '"
                                + paramName + "' was sent in the request content, but it could not be retrieved and "
                                + "added to the parameterized item. Parameter value given is: " + paramValuesJson.opt(paramName) + ". "
                                + "Parameter belongs to parameterized item with ID: '" + parameterizedItem.getId()
                                + "' and definition name: '" + parameterizedItemDef.getName() + "'"); 
                    }
                }
            }
        }
    }
    
    /**
     * Return a node reference to the node represented on a Web Script URL
     * by the given store type, store id and id (which is either a node id or node path) 
     * 
     * @param request The Web Script request object
     * @param storeType The store type associated with the node
     * @param storeId The store id associated with the node
     * @param id The id (or path) associated with the node
     * 
     * @return The node reference associated with the given store type, store id and id
     */
    NodeRef getNodeRefFromWebScriptUrl(WebScriptRequest req, String storeType, String storeId, String id)
    {
        // work out which of storeType, storeId, id have been passed in
        boolean storeTypeGiven = (storeType != null) && (storeType.length() > 0);
        boolean storeIdGiven = (storeId != null) && (storeId.length() > 0);
        boolean idGiven = (id != null) && (id.length() > 0);

        NodeRef nodeRef = null;
        // get the node reference from the storeType, storeId, id
        if ((storeTypeGiven && storeIdGiven && idGiven))
        {
            // see if given ID is part of either a node reference or a node path
            String urlTemplateMatch = req.getServiceMatch().getPath();
            boolean nodeRefGiven = urlTemplateMatch.startsWith(REQ_URL_PART_NODE_REF) ||
                urlTemplateMatch.startsWith(REQ_URL_PART_RULE_NODE_REF);
            boolean nodePathGiven = urlTemplateMatch.startsWith(REQ_URL_PART_NODE_PATH);
            
            String referenceType = null;
            if (nodeRefGiven)
            {
                referenceType = "node";
            }
            else if (nodePathGiven)
            {
                referenceType = "path";
            }
            else
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                        "{store_type}/{store_id}/{id} in URL is not preceded by either "
                    +   "'" + REQ_URL_PART_NODE_REF + "' or '" + REQ_URL_PART_NODE_PATH + "'");
            }
            
            String[] reference = new String[3];
            reference[0] = storeType;
            reference[1] = storeId;
            reference[2] = id;
            
            nodeRef = this.repositoryContext.findNodeRef(referenceType, reference);
        }
        
        return nodeRef;
    }
}
