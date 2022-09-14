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

package org.alfresco.rest.api.impl.mapper.rules;

import java.util.stream.Collectors;

import org.alfresco.repo.action.executer.ScriptActionExecuter;
import org.alfresco.rest.api.model.mapper.RestModelMapper;
import org.alfresco.rest.api.model.rules.Action;
import org.alfresco.rest.api.model.rules.CompositeCondition;
import org.alfresco.rest.api.model.rules.Rule;
import org.alfresco.rest.api.model.rules.RuleTrigger;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.CompositeAction;

@Experimental
public class RestRuleModelMapper implements RestModelMapper<org.alfresco.rest.api.model.rules.Rule, org.alfresco.service.cmr.rule.Rule>
{
    private final RestModelMapper<Action, org.alfresco.service.cmr.action.Action> actionMapper;
    private final RestModelMapper<CompositeCondition, ActionCondition> conditionMapper;

    public RestRuleModelMapper(RestModelMapper<Action, org.alfresco.service.cmr.action.Action> actionMapper,
                               RestModelMapper<CompositeCondition, ActionCondition> conditionMapper)
    {
        this.actionMapper = actionMapper;
        this.conditionMapper = conditionMapper;
    }

    @Override
    public org.alfresco.rest.api.model.rules.Rule toRestModel(org.alfresco.service.cmr.rule.Rule serviceModel)
    {
        if (serviceModel == null)
        {
            return null;
        }

        final Rule.Builder builder = Rule.builder()
                .name(serviceModel.getTitle())
                .description(serviceModel.getDescription())
                .enabled(!serviceModel.getRuleDisabled())
                .cascade(serviceModel.isAppliedToChildren())
                .asynchronous(serviceModel.getExecuteAsynchronously());

        if (serviceModel.getNodeRef() != null) {
            builder.id(serviceModel.getNodeRef().getId());
        }
        if (serviceModel.getRuleTypes() != null)
        {
            builder.triggers(serviceModel.getRuleTypes().stream().map(RuleTrigger::of).collect(Collectors.toList()));
        }
        if (serviceModel.getAction() != null)
        {
            builder.conditions(conditionMapper.toRestModel(serviceModel.getAction().getActionConditions()));
            if (serviceModel.getAction().getCompensatingAction() != null && serviceModel.getAction().getCompensatingAction().getParameterValue(
                    ScriptActionExecuter.PARAM_SCRIPTREF) != null)
            {
                builder.errorScript(serviceModel.getAction().getCompensatingAction().getParameterValue(ScriptActionExecuter.PARAM_SCRIPTREF).toString());
            }
            if (serviceModel.getAction() instanceof CompositeAction && ((CompositeAction) serviceModel.getAction()).getActions() != null)
            {
                CompositeAction compositeAction = (CompositeAction) serviceModel.getAction();
                builder.actions(actionMapper.toRestModels(compositeAction.getActions()));
            }
        }

        return builder.create();
    }

    @Override
    public org.alfresco.service.cmr.rule.Rule toServiceModel(org.alfresco.rest.api.model.rules.Rule restModel)
    {
        return null;
    }
}
