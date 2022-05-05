/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.repo.web.scripts.rule.util;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DefaultRuleServiceTestUtil implements RuleServiceTestUtil {

    public JSONObject buildTestRule(String title) throws JSONException
    {
        JSONObject result = new JSONObject();

        result.put("title", title);
        result.put("description", "this is description for test_rule");

        JSONArray ruleType = new JSONArray();
        ruleType.put("outbound");

        result.put("ruleType", ruleType);

        result.put("applyToChildren", true);

        result.put("executeAsynchronously", false);

        result.put("disabled", false);

        result.put("action", buildTestAction("composite-action", true, true));

        return result;
    }

    protected JSONObject buildTestAction(String actionName, boolean addActions, boolean addCompensatingAction) throws JSONException
    {
        JSONObject result = new JSONObject();

        result.put("actionDefinitionName", actionName);
        result.put("description", "this is description for " + actionName);
        result.put("title", "test_title");

        result.put("executeAsync", addActions);

        if (addActions)
        {
            JSONArray actions = new JSONArray();

            actions.put(buildTestAction("counter", false, false));

            result.put("actions", actions);
        }

        JSONArray conditions = new JSONArray();

        conditions.put(buildTestCondition("no-condition"));

        result.put("conditions", conditions);

        if (addCompensatingAction)
        {
            result.put("compensatingAction", buildTestAction("script", false, false));
        }

        return result;
    }

    protected JSONObject buildTestCondition(String conditionName) throws JSONException
    {
        JSONObject result = new JSONObject();

        result.put("conditionDefinitionName", conditionName);
        result.put("invertCondition", false);

        return result;
    }

    @Override
    public int getTestRuleExpectedStatus() {
        return 200;
    }

    public JSONObject buildCopyAction(NodeRef destination) throws JSONException
    {
        JSONObject result = new JSONObject();

        // add actionDefinitionName
        result.put("actionDefinitionName", "copy");

        // build parameterValues
        JSONObject parameterValues = new JSONObject();
        parameterValues.put("destination-folder", destination);

        // add parameterValues
        result.put("parameterValues", parameterValues);

        // add executeAsync
        result.put("executeAsync", false);

        return result;
    }
}
