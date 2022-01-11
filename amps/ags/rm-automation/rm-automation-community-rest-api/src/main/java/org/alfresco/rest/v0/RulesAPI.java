/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rest.v0;

import static java.util.Arrays.asList;

import static org.apache.http.HttpStatus.SC_OK;
import static org.testng.AssertJUnit.assertTrue;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.alfresco.rest.core.v0.BaseAPI;
import org.alfresco.rest.rm.community.model.rules.RuleDefinition;
import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Covers CRUD API operations on rules
 */
@Component
public class RulesAPI extends BaseAPI
{

    public static final String RULES_API = "{0}node/{1}/ruleset/rules";
    public static final String RULE_API = "{0}node/{1}/ruleset/rules/{2}";
    public static final String INHERIT_RULES_API = "{0}node/{1}/ruleset/inheritrules/toggle";
    public static final String INHERIT_RULES_STATE_API = "{0}node/{1}/ruleset/inheritrules/state";
    // logger
    public static final Logger LOGGER = LoggerFactory.getLogger(RulesAPI.class);

    /**
     * Creates a rule for the specified container with given rule properties
     *
     * @param containerNodeRef the container to have the rule created on
     * @param ruleProperties   the rule properties
     * @return The HTTP Response (or null if the response could not be understood).
     */
    public HttpResponse createRule(String username, String password, String containerNodeRef, RuleDefinition ruleProperties)
    {
        try
        {
            return doPostJsonRequest(username, password, SC_OK, getRuleRequest(ruleProperties), MessageFormat.format(RULES_API, "{0}", containerNodeRef));
        }
        catch (JSONException error)
        {
            LOGGER.error("Unable to extract response parameter.", error);
        }
        return null;
    }

    /**
     * Updates a rule for the specified container with given rule properties
     *
     * @param containerNodeRef the container to have the rule created on
     * @param ruleProperties   the rule properties
     * @return true if the rule has been updated successfully, false otherwise
     */
    public JSONObject updateRule(String username, String password, String containerNodeRef, RuleDefinition ruleProperties)
    {
        String ruleId = ruleProperties.getId();
        if (ruleId == null || ruleId.isEmpty())
        {
            throw new RuntimeException("Can not update a rule without id.");
        }
        try
        {
            return doPutRequest(username, password, getRuleRequest(ruleProperties), MessageFormat.format(RULE_API, "{0}", containerNodeRef, ruleId));
        }
        catch (JSONException error)
        {
           LOGGER.error("Unable to extract response parameter.", error);
        }
        return null;
    }

    /**
     * Deletes a rule on a container and checks it doesn't exist anymore
     *
     * @param username         the user performing the request
     * @param password         the password
     * @param containerNodeRef the container on which the rule has been created
     * @param ruleId           the rule id
     * @throws AssertionError if the rule could not be deleted.
     */
    public void deleteRule(String username, String password, String containerNodeRef, String ruleId)
    {
        doDeleteRequest(username, password, MessageFormat.format(RULE_API, "{0}", containerNodeRef, ruleId));
        boolean success = !getRulesIdsSetOnContainer(username, password, containerNodeRef).contains(ruleId);
        assertTrue("Rule " + ruleId + " was not deleted successfully from " + containerNodeRef, success);
    }

    /**
     * Deletes all the rules on a container and checks they don't exist anymore
     *
     * @param username         the user performing the request
     * @param password         the password
     * @param containerNodeRef the container on which the rules have been created
     * @throws AssertionError if at least one of the rules could not be deleted.
     */
    public void deleteAllRulesOnContainer(String username, String password, String containerNodeRef)
    {
        List<String> ruleIds = getRulesIdsSetOnContainer(username, password, containerNodeRef);
        for (String ruleId : ruleIds)
        {
            deleteRule(username, password, containerNodeRef, ruleId);
        }
    }

    /**
     * Gets all the rules for the specified container with given rule properties
     *
     * @param username the user performing the request
     * @param password the password
     * @param containerNodeRef the container to get the rules from
     *
     * @return list of rules on container
     */

    public List<RuleDefinition> getRulesSetOnContainer(String username, String password, String containerNodeRef)
    {
        List<RuleDefinition> rulesDefinitions = new ArrayList<>();

        // get the rules set on the container
        JSONObject rulesJson = doGetRequest(username, password, MessageFormat.format(RULES_API, "{0}", containerNodeRef));
        if (rulesJson != null)
        {
            try
            {
                JSONArray rules = rulesJson.getJSONArray("data");
                for (int i = 0; i < rules.length(); i++)
                {
                    RuleDefinition ruleDefinition = new RuleDefinition();
                    JSONObject rule = rules.getJSONObject(i);
                    ruleDefinition.id(rule.getString("id"));
                    ruleDefinition.title(rule.getString("title"));
                    ruleDefinition.description(rule.getString("description"));
                    ruleDefinition.ruleType(rule.getJSONArray("ruleType").get(0).toString());
                    ruleDefinition.disabled(rule.getBoolean("disabled"));
                    rulesDefinitions.add(ruleDefinition);
                }
            }
            catch (JSONException error)
            {
                LOGGER.error("Unable to parse rules.", error);
            }
        }
        return rulesDefinitions;
    }

    /**
     * Retrieves all the ids of the rules set on the container
     *
     * @param username         the user performing the request
     * @param password         the password
     * @param containerNodeRef the container's noderef to get set rules for
     * @return the list of rules ids that the container has
     */
    public List<String> getRulesIdsSetOnContainer(String username, String password, String containerNodeRef)
    {
        return getRulesSetOnContainer(username, password, containerNodeRef).stream().map(RuleDefinition::getId).collect(Collectors.toList());
    }

    /**
     * Prepares a request object for rules with given properties
     *
     * @param ruleProperties the rule properties
     * @return a object containing the rule properties for the request
     *
     * @throws JSONException
     */
    private JSONObject getRuleRequest(RuleDefinition ruleProperties) throws JSONException
    {
        JSONObject requestParams = new JSONObject();

        // the id has to be sent as empty string no matter the request
        requestParams.put("id", "");
        requestParams.put("action", addRulesActions(ruleProperties));
        requestParams.put("title", ruleProperties.getTitle());
        requestParams.put("description", ruleProperties.getDescription());
        requestParams.put("disabled", ruleProperties.isDisabled());
        requestParams.put("applyToChildren", ruleProperties.isApplyToChildren());
        requestParams.put("executeAsynchronously", ruleProperties.getRunInBackground());
        requestParams.put("ruleType", asList(ruleProperties.getRuleType()));

        return requestParams;
    }

    /**
     * Adds rules actions to the request
     *
     * @param ruleProperties the rules properties to extract actions from
     *
     * @return the object with actions set
     *
     * @throws JSONException
     */
    private JSONObject addRulesActions(RuleDefinition ruleProperties) throws JSONException
    {
        JSONObject action = new JSONObject();
        action.put("actionDefinitionName", "composite-action");
        JSONObject conditions = new JSONObject();
        conditions.put("conditionDefinitionName", "no-condition");
        conditions.put("parameterValues", new JSONObject());
        action.put("conditions", asList(conditions));
        action.put("actions", getRuleActionsList(ruleProperties));
        return action;
    }

    /**
     * Creates the actions list for request
     *
     * @param ruleProperties given rule properties
     *
     * @return the list of rule actions objects
     */
    private List<JSONObject> getRuleActionsList(RuleDefinition ruleProperties) throws JSONException
    {
        List<JSONObject> ruleActionsList = new ArrayList<>();

        for (String ruleAction : ruleProperties.getActions())
        {
            JSONObject ruleActionObj = new JSONObject();
            ruleActionObj.put("actionDefinitionName", ruleAction);
            JSONObject parameters = new JSONObject();
            if (ruleProperties.getPath() != null)
            {
                if(ruleProperties.getCreateRecordPath() != null)
                {
                    parameters.put("createRecordPath", ruleProperties.getCreateRecordPath());
                }
                parameters.put("path", ruleProperties.getPath());
            }
            if (ruleProperties.getContentTitle() != null)
            {
                parameters.put("property", "cm:title");
                parameters.put("value", ruleProperties.getContentTitle());
                parameters.put("prop_type", "d:mltext");
            }
            if (ruleProperties.getContentDescription() != null)
            {
                parameters.put("property", "cm:description");
                parameters.put("value", ruleProperties.getContentDescription());
                parameters.put("prop_type", "d:mltext");
            }
            if (ruleProperties.getRejectReason() != null)
            {
                parameters.put("reason", ruleProperties.getRejectReason());
            }
            ruleActionObj.put("parameterValues", parameters);
            ruleActionsList.add(ruleActionObj);
        }
        return ruleActionsList;
    }

    /**
     * Returns the rule id for the give rule title set on a container
     *
     * @param username the user performing the request
     * @param password the password
     * @param containerNodeRef container nodeRef
     *
     * @return the rule id
     */
    public String getRuleIdWithTitle(String username, String password, String containerNodeRef, String title)
    {
        return getRulesSetOnContainer(username, password, containerNodeRef).stream().filter(
                rule -> rule.getTitle().equals(title)).findAny().get().getId();
    }

    /**
     * Disable inheritance on specific container
     *
     * @param username the username
     * @param password the password
     * @param containerNodeRef the container nodeRef
     *
     * @return The HTTP Response (or null if the current state is disabled).
     */
    public HttpResponse disableRulesInheritance(String username, String password, String containerNodeRef)
    {
        if(containerInheritsRulesFromParent(username, password, containerNodeRef))
        {
            return doPostJsonRequest(username, password, SC_OK, new JSONObject(), MessageFormat.format(INHERIT_RULES_API, "{0}", containerNodeRef));
        }
        return null;
    }

    /**
     * Enable inheritance on specific container
     *
     * @param username         the username
     * @param password         the password
     * @param containerNodeRef the container nodeRef
     * @return The HTTP Response (or null if the current state is disabled).
     */
    public HttpResponse enableRulesInheritance(String username, String password, String containerNodeRef)
    {
        if (!containerInheritsRulesFromParent(username, password, containerNodeRef))
        {
            return doPostJsonRequest(username, password, SC_OK, new JSONObject(), MessageFormat.format(INHERIT_RULES_API, "{0}", containerNodeRef));
        }
        return null;
    }

    /**
     * Returns the rules inheritance state of the container
     *
     * @param username the username
     * @param password the password
     * @param containerNodeRef the container nodeRef
     *
     * @return a boolean specifying if the container inherits rules from parent
     * @throws JSONException
     */
    public boolean containerInheritsRulesFromParent(String username, String password, String containerNodeRef) throws JSONException
    {
        JSONObject rulesInheritanceInfo = doGetRequest(username, password, MessageFormat.format(INHERIT_RULES_STATE_API, "{0}", containerNodeRef));
        return rulesInheritanceInfo.getJSONObject("data").getBoolean("inheritRules");
    }
}
