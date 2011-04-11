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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.action.ActionConditionImpl;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.action.CompositeActionImpl;
import org.alfresco.repo.web.scripts.rule.ruleset.RuleRef;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.Rule;
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
public class RulePut extends RulePost
{
    @SuppressWarnings("unused")
    private static Log logger = LogFactory.getLog(RulePut.class);

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>();

        // get request parameters
        NodeRef nodeRef = parseRequestForNodeRef(req);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String ruleId = templateVars.get("rule_id");

        Rule ruleToUpdate = null;

        // get all rules for given nodeRef
        List<Rule> rules = ruleService.getRules(nodeRef);

        //filter by rule id
        for (Rule rule : rules)
        {
            if (rule.getNodeRef().getId().equalsIgnoreCase(ruleId))
            {
                ruleToUpdate = rule;
                break;
            }
        }

        if (ruleToUpdate == null)
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find rule with id: " + ruleId);
        }

        JSONObject json = null;

        try
        {
            // read request json
            json = new JSONObject(new JSONTokener(req.getContent().getContent()));

            // parse request json
            updateRuleFromJSON(json, ruleToUpdate);

            // save changes
            ruleService.saveRule(nodeRef, ruleToUpdate);

            RuleRef updatedRuleRef = new RuleRef(ruleToUpdate, fileFolderService.getFileInfo(ruleService.getOwningNodeRef(ruleToUpdate)));

            model.put("ruleRef", updatedRuleRef);
        }
        catch (IOException iox)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not read content from req.", iox);
        }
        catch (JSONException je)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Could not parse JSON from req.", je);
        }

        return model;
    }

    protected void updateRuleFromJSON(JSONObject jsonRule, Rule ruleToUpdate) throws JSONException
    {
        if (jsonRule.has("title"))
        {
            ruleToUpdate.setTitle(jsonRule.getString("title"));
        }

        if (jsonRule.has("description"))
        {
            ruleToUpdate.setDescription(jsonRule.getString("description"));
        }

        if (jsonRule.has("ruleType"))
        {
            JSONArray jsonTypes = jsonRule.getJSONArray("ruleType");
            List<String> types = new ArrayList<String>();

            for (int i = 0; i < jsonTypes.length(); i++)
            {
                types.add(jsonTypes.getString(i));
            }
            ruleToUpdate.setRuleTypes(types);
        }

        if (jsonRule.has("applyToChildren"))
        {
            ruleToUpdate.applyToChildren(jsonRule.getBoolean("applyToChildren"));
        }

        if (jsonRule.has("executeAsynchronously"))
        {
            ruleToUpdate.setExecuteAsynchronously(jsonRule.getBoolean("executeAsynchronously"));
        }

        if (jsonRule.has("disabled"))
        {
            ruleToUpdate.setRuleDisabled(jsonRule.getBoolean("disabled"));
        }

        if (jsonRule.has("action"))
        {
            JSONObject jsonAction = jsonRule.getJSONObject("action");

            // update rule action 
            Action action = updateActionFromJson(jsonAction, (ActionImpl) ruleToUpdate.getAction());

            ruleToUpdate.setAction(action);
        }
    }

    protected Action updateActionFromJson(JSONObject jsonAction, ActionImpl actionToUpdate) throws JSONException
    {
        ActionImpl result = null;

        if (jsonAction.has("id"))
        {
            // update existing action
            result = actionToUpdate;
        }
        else
        {
            // create new object as id was not sent by client
            result = parseJsonAction(jsonAction);
            return result;
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
            if (jsonActions.length() == 0)
            {
                // empty array was sent -> clear list
                ((CompositeActionImpl) result).getActions().clear();
            }
            else
            {
                List<Action> existingActions = ((CompositeActionImpl) result).getActions();
                List<Action> newActions = new ArrayList<Action>();

                for (int i = 0; i < jsonActions.length(); i++)
                {
                    JSONObject innerJsonAction = jsonActions.getJSONObject(i);

                    if (innerJsonAction.has("id"))
                    {
                        // update existing object
                        String actionId = innerJsonAction.getString("id");

                        Action existingAction = getAction(existingActions, actionId);
                        existingActions.remove(existingAction);

                        Action updatedAction = updateActionFromJson(innerJsonAction, (ActionImpl) existingAction);
                        newActions.add(updatedAction);
                    }
                    else
                    {
                        //create new action as id was not sent
                        newActions.add(parseJsonAction(innerJsonAction));
                    }
                }
                existingActions.clear();

                for (Action action : newActions)
                {
                    existingActions.add(action);
                }
            }
        }

        if (jsonAction.has("conditions"))
        {
            JSONArray jsonConditions = jsonAction.getJSONArray("conditions");

            if (jsonConditions.length() == 0)
            {
                // empty array was sent -> clear list
                result.getActionConditions().clear();
            }
            else
            {
                List<ActionCondition> existingConditions = result.getActionConditions();
                List<ActionCondition> newConditions = new ArrayList<ActionCondition>();

                for (int i = 0; i < jsonConditions.length(); i++)
                {
                    JSONObject jsonCondition = jsonConditions.getJSONObject(i);

                    if (jsonCondition.has("id"))
                    {
                        // update existing object
                        String conditionId = jsonCondition.getString("id");

                        ActionCondition existingCondition = getCondition(existingConditions, conditionId);
                        existingConditions.remove(existingCondition);

                        ActionCondition updatedActionCondition = updateActionConditionFromJson(jsonCondition, (ActionConditionImpl) existingCondition);
                        newConditions.add(updatedActionCondition);
                    }
                    else
                    {
                        // create new object as id was not sent
                        newConditions.add(parseJsonActionCondition(jsonCondition));
                    }
                }

                existingConditions.clear();

                for (ActionCondition condition : newConditions)
                {
                    existingConditions.add(condition);
                }
            }
        }

        if (jsonAction.has("compensatingAction"))
        {
            JSONObject jsonCompensatingAction = jsonAction.getJSONObject("compensatingAction");
            Action compensatingAction = updateActionFromJson(jsonCompensatingAction, (ActionImpl) actionToUpdate.getCompensatingAction());

            actionToUpdate.setCompensatingAction(compensatingAction);
        }
        return result;
    }

    protected ActionCondition updateActionConditionFromJson(JSONObject jsonCondition, ActionConditionImpl conditionToUpdate) throws JSONException
    {
        ActionConditionImpl result = null;

        if (jsonCondition.has("id"))
        {
            // update exiting object
            result = conditionToUpdate;
        }
        else
        {
            // create new onject as id was not sent
            result = parseJsonActionCondition(jsonCondition);
            return result;
        }

        if (jsonCondition.has("invertCondition"))
        {
            result.setInvertCondition(jsonCondition.getBoolean("invertCondition"));
        }

        if (jsonCondition.has("parameterValues"))
        {
            JSONObject jsonParameterValues = jsonCondition.getJSONObject("parameterValues");
            result.setParameterValues(parseJsonParameterValues(jsonParameterValues, result.getActionConditionDefinitionName(), false));
        }

        return result;
    }

    private Action getAction(List<Action> actions, String id)
    {
        Action result = null;
        for (Action action : actions)
        {
            if (action.getId().equalsIgnoreCase(id))
            {
                result = action;
                break;
            }
        }

        return result;
    }

    private ActionCondition getCondition(List<ActionCondition> conditions, String id)
    {
        ActionCondition result = null;
        for (ActionCondition condition : conditions)
        {
            if (condition.getId().equalsIgnoreCase(id))
            {
                result = condition;
                break;
            }
        }

        return result;
    }
}
