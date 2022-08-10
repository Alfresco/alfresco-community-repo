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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.action.CompositeActionImpl;
import org.alfresco.repo.action.executer.SetPropertyValueActionExecuter;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.action.ParameterizedItemDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Experimental
public class Action
{
    private String actionDefinitionId;
    private Map<String, Serializable> params;

    /**
     * Converts service POJO action to REST model action.
     *
     * @param actionModel - {@link org.alfresco.service.cmr.action.Action} service POJO
     * @return {@link Action} REST model
     */
    public static Action from(final org.alfresco.service.cmr.action.Action actionModel)
    {
        if (actionModel == null)
        {
            return null;
        }

        final Action.Builder builder = builder().actionDefinitionId(actionModel.getActionDefinitionName());
        if (actionModel.getParameterValues() != null)
        {
            builder.params(new HashMap<>(actionModel.getParameterValues()));
        }

        return builder.create();
    }

    /**
     * Convert the REST model object to the equivalent service POJO.
     *
     * @param nodeRef The node reference.
     * @return The action service POJO.
     */
    public org.alfresco.service.cmr.action.Action toServiceModel(final NodeRef nodeRef)
    {
        return new ActionImpl(nodeRef, GUID.generate(), this.actionDefinitionId, params);
    }

    /**
     * Convert the REST model objects to composite action service POJO.
     *
     * @param actions List of actions.
     * @return The composite action service POJO.
     */
    public static org.alfresco.service.cmr.action.Action toCompositeAction(final List<Action> actions) {
        if (actions == null)
        {
            return null;
        }

        final org.alfresco.service.cmr.action.CompositeAction compositeAction = new CompositeActionImpl(null, GUID.generate());
        actions.forEach(action -> compositeAction.addAction(action.toServiceModel(null)));
        return compositeAction;
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

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Action action = (Action) o;
        return Objects.equals(actionDefinitionId, action.actionDefinitionId) && Objects.equals(params, action.params);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(actionDefinitionId, params);
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder
    {
        private String actionDefinitionId;
        private Map<String, Serializable> params;

        public Builder actionDefinitionId(String actionDefinitionId)
        {
            this.actionDefinitionId = actionDefinitionId;
            return this;
        }

        public Builder params(Map<String, Serializable> params)
        {
            this.params = params;
            return this;
        }

        public Action create() {
            final Action action = new Action();
            action.setActionDefinitionId(actionDefinitionId);
            action.setParams(params);
            return action;
        }
    }
}
