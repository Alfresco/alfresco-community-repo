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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.action.executer.ScriptActionExecuter;
import org.alfresco.repo.action.executer.SetPropertyValueActionExecuter;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.framework.resource.UniqueId;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.GUID;

@Experimental
public class Rule
{
    private String id;
    private String name;
    private String description;
    private boolean enabled;
    private boolean cascade;
    private boolean asynchronous;
    private boolean shared;
    private String errorScript;
    private List<RuleTrigger> triggers;
    private List<Action> actions;

    /**
     * Converts service POJO rule to REST model rule.
     *
     * @param ruleModel - {@link org.alfresco.service.cmr.rule.Rule} service POJO
     * @return {@link Rule} REST model
     */
    public static Rule from(final org.alfresco.service.cmr.rule.Rule ruleModel, final boolean shared)
    {
        if (ruleModel == null)
        {
            return null;
        }

        final Rule.Builder builder = builder()
            .name(ruleModel.getTitle())
            .description(ruleModel.getDescription())
            .enabled(!ruleModel.getRuleDisabled())
            .cascade(ruleModel.isAppliedToChildren())
            .asynchronous(ruleModel.getExecuteAsynchronously())
            .shared(shared);

        if (ruleModel.getNodeRef() != null) {
            builder.id(ruleModel.getNodeRef().getId());
        }
        if (ruleModel.getRuleTypes() != null)
        {
            builder.triggers(ruleModel.getRuleTypes().stream().map(RuleTrigger::of).collect(Collectors.toList()));
        }
        if (ruleModel.getAction() != null)
        {
            if (ruleModel.getAction().getCompensatingAction() != null && ruleModel.getAction().getCompensatingAction().getParameterValue(ScriptActionExecuter.PARAM_SCRIPTREF) != null)
            {
                builder.errorScript(ruleModel.getAction().getCompensatingAction().getParameterValue(ScriptActionExecuter.PARAM_SCRIPTREF).toString());
            }
            if (ruleModel.getAction() instanceof CompositeAction && ((CompositeAction) ruleModel.getAction()).getActions() != null)
            {
                builder.actions(((CompositeAction) ruleModel.getAction()).getActions().stream().map(Action::from).collect(Collectors.toList()));
            }
        }

        return builder.create();
    }

    /**
     * Convert the REST model object to the equivalent service POJO.
     *
     * @param nodes The nodes API.
     * @return The rule service POJO.
     */
    public org.alfresco.service.cmr.rule.Rule toServiceModel(Nodes nodes)
    {
        org.alfresco.service.cmr.rule.Rule ruleModel = new org.alfresco.service.cmr.rule.Rule();
        if (id != null)
        {
            NodeRef nodeRef = nodes.validateOrLookupNode(id, null);
            ruleModel.setNodeRef(nodeRef);
        }
        ruleModel.setTitle(name);

        // TODO: Once we have actions working properly then this needs to be replaced.
        Map<String, Serializable> parameters = Map.of(
                SetPropertyValueActionExecuter.PARAM_PROPERTY, ContentModel.PROP_TITLE,
                SetPropertyValueActionExecuter.PARAM_VALUE, "UPDATED:" + GUID.generate());
        org.alfresco.service.cmr.action.Action action = new ActionImpl(null, GUID.generate(), SetPropertyValueActionExecuter.NAME, parameters);
        ruleModel.setAction(action);

        return ruleModel;
    }

    @UniqueId
    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public boolean isCascade()
    {
        return cascade;
    }

    public void setCascade(boolean cascade)
    {
        this.cascade = cascade;
    }

    public boolean isAsynchronous()
    {
        return asynchronous;
    }

    public void setAsynchronous(boolean asynchronous)
    {
        this.asynchronous = asynchronous;
    }

    public String getErrorScript()
    {
        return errorScript;
    }

    public void setErrorScript(String errorScript)
    {
        this.errorScript = errorScript;
    }

    public boolean isShared()
    {
        return shared;
    }

    public void setShared(boolean shared)
    {
        this.shared = shared;
    }

    public List<RuleTrigger> getTriggers()
    {
        return triggers;
    }

    public void setTriggers(List<RuleTrigger> triggers)
    {
        this.triggers = triggers;
    }

    public List<Action> getActions()
    {
        return actions;
    }

    public void setActions(List<Action> actions)
    {
        this.actions = actions;
    }

    @Override
    public String toString()
    {
        return "Rule{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", description='" + description + '\'' + ", enabled=" + enabled + ", cascade=" + cascade
            + ", asynchronous=" + asynchronous + ", shared=" + shared + ", errorScript='" + errorScript + '\'' + ", triggers=" + triggers + ", actions=" + actions + '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Rule rule = (Rule) o;
        return enabled == rule.enabled && cascade == rule.cascade && asynchronous == rule.asynchronous && shared == rule.shared && Objects.equals(id, rule.id) && Objects.equals(
            name, rule.name) && Objects.equals(description, rule.description) && Objects.equals(errorScript, rule.errorScript) && Objects.equals(triggers, rule.triggers)
            && Objects.equals(actions, rule.actions);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, name, description, enabled, cascade, asynchronous, shared, errorScript, triggers, actions);
    }

    public static Builder builder()
    {
        return new Builder();
    }

    /** Builder class. */
    public static class Builder
    {
        private String id;
        private String name;
        private String description;
        private boolean enabled;
        private boolean cascade;
        private boolean asynchronous;
        private boolean shared;
        private String errorScript;
        private List<RuleTrigger> triggers;
        private List<Action> actions;

        public Builder id(String id)
        {
            this.id = id;
            return this;
        }

        public Builder name(String name)
        {
            this.name = name;
            return this;
        }

        public Builder description(String description)
        {
            this.description = description;
            return this;
        }

        public Builder enabled(boolean enabled)
        {
            this.enabled = enabled;
            return this;
        }

        public Builder cascade(boolean cascade)
        {
            this.cascade = cascade;
            return this;
        }

        public Builder asynchronous(boolean asynchronous)
        {
            this.asynchronous = asynchronous;
            return this;
        }

        public Builder shared(boolean shared)
        {
            this.shared = shared;
            return this;
        }

        public Builder errorScript(String errorScript)
        {
            this.errorScript = errorScript;
            return this;
        }

        public Builder triggers(List<RuleTrigger> triggers)
        {
            this.triggers = triggers;
            return this;
        }

        public Builder actions(List<Action> actions)
        {
            this.actions = actions;
            return this;
        }

        public Rule create()
        {
            Rule rule = new Rule();
            rule.setId(id);
            rule.setName(name);
            rule.setDescription(description);
            rule.setEnabled(enabled);
            rule.setCascade(cascade);
            rule.setAsynchronous(asynchronous);
            rule.setShared(shared);
            rule.setErrorScript(errorScript);
            rule.setTriggers(triggers);
            rule.setActions(actions);
            return rule;
        }
    }
}
