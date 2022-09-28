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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.alfresco.rest.framework.resource.UniqueId;
import org.alfresco.service.Experimental;

@Experimental
public class Rule
{
    private String id;
    private String name;
    private String description;
    private boolean isEnabled;
    private boolean isInheritable;
    private boolean isAsynchronous;
    private Boolean isShared;
    private String errorScript;
    private List<RuleTrigger> triggers = List.of(RuleTrigger.INBOUND);
    private CompositeCondition conditions;
    private List<Action> actions;

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

    public boolean getIsEnabled()
    {
        return isEnabled;
    }

    public void setIsEnabled(boolean isEnabled)
    {
        this.isEnabled = isEnabled;
    }

    public boolean getIsInheritable()
    {
        return isInheritable;
    }

    public void setIsInheritable(boolean isInheritable)
    {
        this.isInheritable = isInheritable;
    }

    public boolean getIsAsynchronous()
    {
        return isAsynchronous;
    }

    public void setIsAsynchronous(boolean isAsynchronous)
    {
        this.isAsynchronous = isAsynchronous;
    }

    public String getErrorScript()
    {
        return errorScript;
    }

    public void setErrorScript(String errorScript)
    {
        this.errorScript = errorScript;
    }

    public Boolean isIsShared()
    {
        return isShared;
    }

    public void setIsShared(Boolean shared)
    {
        this.isShared = shared;
    }

    public List<String> getTriggers()
    {
        if (triggers == null)
        {
            return null;
        }
        return triggers.stream().map(RuleTrigger::getValue).collect(Collectors.toList());
    }

    public void setTriggers(List<String> triggers)
    {
        if (triggers != null)
        {
            this.triggers = triggers.stream().map(RuleTrigger::of).collect(Collectors.toList());
        }
    }

    public void setRuleTriggers(List<RuleTrigger> triggers)
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
        return "Rule{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", description='" + description + '\'' + ", isEnabled=" + isEnabled + ", isInheritable=" + isInheritable
            + ", isAsynchronous=" + isAsynchronous + ", isShared=" + isShared + ", errorScript='" + errorScript + '\'' + ", triggers=" + triggers + ", conditions=" + conditions
            + ", actions=" + actions + '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Rule rule = (Rule) o;
        return isEnabled == rule.isEnabled
                && isInheritable == rule.isInheritable
                && isAsynchronous == rule.isAsynchronous
                && Objects.equals(isShared, rule.isShared)
                && Objects.equals(id, rule.id)
                && Objects.equals(name, rule.name)
                && Objects.equals(description, rule.description)
                && Objects.equals(errorScript, rule.errorScript)
                && Objects.equals(triggers, rule.triggers)
                && Objects.equals(conditions, rule.conditions)
                && Objects.equals(actions, rule.actions);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, name, description, isEnabled, isInheritable, isAsynchronous, isShared, errorScript, triggers, conditions, actions);
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
        private boolean isEnabled;
        private boolean isInheritable;
        private boolean isAsynchronous;
        private Boolean isShared;
        private String errorScript;
        private List<RuleTrigger> triggers = List.of(RuleTrigger.INBOUND);
        private CompositeCondition conditions;
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

        public Builder isEnabled(boolean isEnabled)
        {
            this.isEnabled = isEnabled;
            return this;
        }

        public Builder isInheritable(boolean isInheritable)
        {
            this.isInheritable = isInheritable;
            return this;
        }

        public Builder isAsynchronous(boolean isAsynchronous)
        {
            this.isAsynchronous = isAsynchronous;
            return this;
        }

        public Builder isShared(Boolean isShared)
        {
            this.isShared = isShared;
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

        public Builder conditions(CompositeCondition conditions)
        {
            this.conditions = conditions;
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
            rule.setIsEnabled(isEnabled);
            rule.setIsInheritable(isInheritable);
            rule.setIsAsynchronous(isAsynchronous);
            rule.setIsShared(isShared);
            rule.setErrorScript(errorScript);
            rule.setRuleTriggers(triggers);
            rule.setConditions(conditions);
            rule.setActions(actions);
            return rule;
        }
    }
}
