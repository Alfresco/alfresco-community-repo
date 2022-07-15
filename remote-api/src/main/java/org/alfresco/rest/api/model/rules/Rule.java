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

import org.alfresco.rest.api.Nodes;
import org.alfresco.repo.action.executer.ScriptActionExecuter;
import org.alfresco.rest.framework.resource.UniqueId;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.action.CompositeAction;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.alfresco.service.cmr.repository.NodeRef;

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
    private CompositeCondition conditions;
    private List<Action> actions;

    /**
     * Converts service POJO rule to REST model rule.
     *
     * @param ruleModel - {@link org.alfresco.service.cmr.rule.Rule} service POJO
     * @return {@link Rule} REST model
     */
    public static Rule from(final org.alfresco.service.cmr.rule.Rule ruleModel)
    {
        if (ruleModel == null)
        {
            return null;
        }

        final Rule rule = new Rule();
        rule.id = ruleModel.getNodeRef().getId();
        rule.name = ruleModel.getTitle();
        rule.description = ruleModel.getDescription();
        rule.enabled = !ruleModel.getRuleDisabled();
        rule.cascade = ruleModel.isAppliedToChildren();
        rule.asynchronous = ruleModel.getExecuteAsynchronously();
        rule.triggers = ruleModel.getRuleTypes().stream().map(RuleTrigger::of).collect(Collectors.toList());
        if (ruleModel.getAction().getCompensatingAction() != null)
        {
            rule.errorScript = ruleModel.getAction().getCompensatingAction().getParameterValue(ScriptActionExecuter.PARAM_SCRIPTREF).toString();
        }
        rule.conditions = CompositeCondition.from(ruleModel.getAction().getActionConditions());
        if (ruleModel.getAction() instanceof CompositeAction)
        {
            rule.actions = ((CompositeAction) ruleModel.getAction()).getActions().stream().map(Action::from).collect(Collectors.toList());
        }
        /*return builder()
                .setId(ruleModel.getNodeRef().getId())
                .setName(ruleModel.getTitle())
                .createRule();*/
        return rule;
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
        NodeRef nodeRef = nodes.validateOrLookupNode(id, null);
        ruleModel.setNodeRef(nodeRef);
        ruleModel.setTitle(name);
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

    public CompositeCondition getConditions()
    {
        return conditions;
    }

    public void setConditions(CompositeCondition conditions)
    {
        this.conditions = conditions;
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
            + ", asynchronous=" + asynchronous + ", shared=" + shared + ", errorScript=" + errorScript + ", triggers=" + triggers + ", conditions=" + conditions + ", actions="
            + actions + '}';
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
            && Objects.equals(conditions, rule.conditions) && Objects.equals(actions, rule.actions);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, name, description, enabled, cascade, asynchronous, shared, errorScript, triggers, conditions, actions);
    }

    public static RuleBuilder builder()
    {
        return new RuleBuilder();
    }

    /** Builder class. */
    public static class RuleBuilder
    {
        private String id;
        private String name;

        public RuleBuilder setId(String id)
        {
            this.id = id;
            return this;
        }

        public RuleBuilder setName(String name)
        {
            this.name = name;
            return this;
        }

        public Rule createRule()
        {
            Rule rule = new Rule();
            rule.setId(id);
            rule.setName(name);
            return rule;
        }
    }
}
