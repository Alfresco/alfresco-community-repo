/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.rest.api.tests.client.data;

import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Map;

import org.json.simple.JSONObject;

public class Action extends org.alfresco.rest.api.model.Action implements Serializable, ExpectedComparison
{

    @Override
    public void expected(Object o)
    {
        assertTrue("o is an instance of " + o.getClass(), o instanceof Action);

        Action other = (Action) o;

        AssertUtil.assertEquals("id", getId(), other.getId());
        AssertUtil.assertEquals("actionDefinitionId", getActionDefinitionId(), other.getActionDefinitionId());
        AssertUtil.assertEquals("targetId", getTargetId(), other.getTargetId());
        AssertUtil.assertEquals("params", getParams(), other.getParams());
    }

    @SuppressWarnings("unchecked")
    public JSONObject toJSON()
    {
        JSONObject jsonObject = new JSONObject();
        if (getId() != null)
        {
            jsonObject.put("id", getId());
        }

        jsonObject.put("actionDefinitionId", getActionDefinitionId());

        if (getTargetId() != null)
        {
            jsonObject.put("targetId", getTargetId());
        }

        if (getParams() != null)
        {
            jsonObject.put("params", getParams());
        }

        return jsonObject;
    }

    @SuppressWarnings("unchecked")
    public static Action parseAction(JSONObject jsonObject)
    {
        String id = (String) jsonObject.get("id");
        String actionDefinitionId = (String) jsonObject.get("actionDefinitionId");
        String targetId = (String) jsonObject.get("targetId");
        Map<String, String> params = (Map<String, String>) jsonObject.get("params");

        Action action = new Action();
        action.setId(id);
        action.setActionDefinitionId(actionDefinitionId);
        action.setTargetId(targetId);
        action.setParams(params);

        return action;
    }

}
