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
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ParameterizedItem;

import java.util.List;
import java.util.stream.Collectors;

@Experimental
public class CompositeCondition
{

    private String conditionDefinitionId;
    private boolean inverted;
    private ConditionOperator booleanMode = ConditionOperator.AND;
    private List<CompositeCondition> compositeConditions;
    private List<SimpleCondition> simpleConditions;

    public static CompositeCondition from(final List<ActionCondition> conditionModels) {
        if (conditionModels == null) {
            return null;
        }

        final CompositeCondition condition = new CompositeCondition();
        conditionModels.forEach(conditionModel -> {
            condition.conditionDefinitionId = conditionModel.getActionConditionDefinitionName();
            condition.inverted = conditionModel.getInvertCondition();
            condition.simpleConditions = conditionModels.stream()
                .map(ParameterizedItem::getParameterValues)
                .map(SimpleCondition::from)
                .collect(Collectors.toList());
        });

        return condition;
    }

    public String getConditionDefinitionId()
    {
        return conditionDefinitionId;
    }

    public void setConditionDefinitionId(String conditionDefinitionId)
    {
        this.conditionDefinitionId = conditionDefinitionId;
    }

    public boolean isInverted()
    {
        return inverted;
    }

    public void setInverted(boolean inverted)
    {
        this.inverted = inverted;
    }

    public ConditionOperator getBooleanMode()
    {
        return booleanMode;
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
        return "CompositeCondition{" + "conditionDefinitionId='" + conditionDefinitionId + '\'' + ", inverted=" + inverted + ", booleanMode=" + booleanMode
            + ", compositeConditions=" + compositeConditions + ", simpleConditions=" + simpleConditions + '}';
    }
}
