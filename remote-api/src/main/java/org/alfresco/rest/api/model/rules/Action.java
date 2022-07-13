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
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rest.api.model.rules;

import org.alfresco.service.Experimental;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Experimental
public class Action
{

    private String actionDefinitionId;
    private Map<String, Serializable> params;

    public static Action from(final org.alfresco.service.cmr.action.Action actionModel) {
        if (actionModel == null) {
            return null;
        }

        final Action action = new Action();
        action.actionDefinitionId = actionModel.getActionDefinitionName();
        action.params = new HashMap<>(actionModel.getParameterValues());

        return action;
    }

    public String getActionDefinitionId()
    {
        return actionDefinitionId;
    }

    public void setActionDefinitionId(String actionDefinitionId)
    {
        this.actionDefinitionId = actionDefinitionId;
    }

    public Map<String, Serializable> getParams()
    {
        return params;
    }

    public void setParams(Map<String, Serializable> params)
    {
        this.params = params;
    }

    @Override
    public String toString()
    {
        return "Action{" + "actionDefinitionId='" + actionDefinitionId + '\'' + ", params=" + params + '}';
    }
}
