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

import org.alfresco.service.Experimental;

@Experimental
public class CompositeCondition
{
    private boolean inverted;
    private ConditionOperator booleanMode = ConditionOperator.AND;
    private List<CompositeCondition> compositeConditions;
    private List<SimpleCondition> simpleConditions;

    public boolean isInverted()
    {
        return inverted;
    }

    public void setInverted(boolean inverted)
    {
        this.inverted = inverted;
    }

    public String getBooleanMode()
    {
        if (booleanMode == null)
        {
            return null;
        }
        return booleanMode.name().toLowerCase();
    }

    public void setBooleanMode(String booleanMode)
    {
        if (booleanMode != null)
        {
            this.booleanMode = ConditionOperator.valueOf(booleanMode.toUpperCase());
        }
    }

    public void setBooleanMode(ConditionOperator booleanMode)
    {
        this.booleanMode = booleanMode;
    }

    public List<CompositeCondition> getCompositeConditions()
    {
        return compositeConditions;
    }

    public void setCompositeConditions(List<CompositeCondition> compositeConditions)
    {
        this.compositeConditions = compositeConditions;
    }

    public List<SimpleCondition> getSimpleConditions()
    {
        return simpleConditions;
    }

    public void setSimpleConditions(List<SimpleCondition> simpleConditions)
    {
        this.simpleConditions = simpleConditions;
    }

    @Override
    public String toString()
    {
        return "CompositeCondition{" + "inverted=" + inverted + ", booleanMode=" + booleanMode + ", compositeConditions=" + compositeConditions + ", simpleConditions="
            + simpleConditions + '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CompositeCondition that = (CompositeCondition) o;
        return inverted == that.inverted && booleanMode == that.booleanMode && Objects.equals(compositeConditions, that.compositeConditions) && Objects.equals(
            simpleConditions, that.simpleConditions);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(inverted, booleanMode, compositeConditions, simpleConditions);
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder
    {
        private boolean inverted;
        private ConditionOperator booleanMode = ConditionOperator.AND;
        private List<CompositeCondition> compositeConditions;
        private List<SimpleCondition> simpleConditions;

        public Builder inverted(boolean inverted)
        {
            this.inverted = inverted;
            return this;
        }

        public Builder booleanMode(ConditionOperator booleanMode)
        {
            this.booleanMode = booleanMode;
            return this;
        }

        public Builder compositeConditions(List<CompositeCondition> compositeConditions)
        {
            this.compositeConditions = compositeConditions;
            return this;
        }

        public Builder simpleConditions(List<SimpleCondition> simpleConditions)
        {
            this.simpleConditions = simpleConditions;
            return this;
        }

        public CompositeCondition create()
        {
            final CompositeCondition condition = new CompositeCondition();
            condition.setInverted(inverted);
            condition.setBooleanMode(booleanMode);
            condition.setCompositeConditions(compositeConditions);
            condition.setSimpleConditions(simpleConditions);
            return condition;
        }
    }
}
