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

import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.action.executer.ScriptActionExecuter;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.mapper.RestModelMapper;
import org.alfresco.rest.api.model.rules.Action;
import org.alfresco.rest.api.model.rules.CompositeCondition;
import org.alfresco.rest.api.model.rules.Rule;
import org.alfresco.rest.api.model.rules.RuleTrigger;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.GUID;
import org.apache.commons.collections.CollectionUtils;

@Experimental
public class RestRuleModelMapper implements RestModelMapper<Rule, org.alfresco.service.cmr.rule.Rule>
{
    private final RestModelMapper<CompositeCondition, ActionCondition> compositeConditionMapper;
    private final RestModelMapper<Action, org.alfresco.service.cmr.action.Action> actionMapper;
    private final Nodes nodes;

    public RestRuleModelMapper(
            RestModelMapper<CompositeCondition, ActionCondition> compositeConditionMapper,
            RestModelMapper<Action, org.alfresco.service.cmr.action.Action> actionMapper,
            Nodes nodes)
    {
        this.compositeConditionMapper = compositeConditionMapper;
        this.actionMapper = actionMapper;
        this.nodes = nodes;
    }

    /**
     * Converts service POJO rule to REST model rule.
     *
     * @param serviceRule - {@link org.alfresco.service.cmr.rule.Rule} service POJO
     * @return {@link Rule} REST model
     */
    @Override
    public Rule toRestModel(org.alfresco.service.cmr.rule.Rule serviceRule)
    {
        if (serviceRule == null)
        {
            return null;
        }

        final Rule.Builder builder = Rule.builder()
                .name(serviceRule.getTitle())
                .description(serviceRule.getDescription())
                .enabled(!serviceRule.getRuleDisabled())
                .cascade(serviceRule.isAppliedToChildren())
                .asynchronous(serviceRule.getExecuteAsynchronously());

        if (serviceRule.getNodeRef() != null)
        {
            builder.id(serviceRule.getNodeRef().getId());
        }
        if (CollectionUtils.isNotEmpty(serviceRule.getRuleTypes()))
        {
            builder.triggers(serviceRule.getRuleTypes().stream().map(RuleTrigger::of).collect(Collectors.toList()));
        }
        if (serviceRule.getAction() != null)
        {
            builder.conditions(compositeConditionMapper.toRestModel(serviceRule.getAction().getActionConditions()));
            if (serviceRule.getAction().getCompensatingAction() != null &&
                    serviceRule.getAction().getCompensatingAction().getParameterValue(ScriptActionExecuter.PARAM_SCRIPTREF) != null)
            {
                builder.errorScript(
                        serviceRule.getAction().getCompensatingAction().getParameterValue(ScriptActionExecuter.PARAM_SCRIPTREF).toString());
            }
            if (serviceRule.getAction() instanceof CompositeAction && ((CompositeAction) serviceRule.getAction()).getActions() != null)
            {
                builder.actions(
                        ((CompositeAction) serviceRule.getAction()).getActions().stream()
                                .map(actionMapper::toRestModel)
                                .collect(Collectors.toList()));
            }
        }

        return builder.create();
    }

    /**
     * Convert the REST model object to the equivalent service POJO.
     *
     * @param restRuleModel {@link Rule} REST model.
     * @return The rule service POJO.
     */
    @Override
    public org.alfresco.service.cmr.rule.Rule toServiceModel(Rule restRuleModel)
    {
        final org.alfresco.service.cmr.rule.Rule serviceRule = new org.alfresco.service.cmr.rule.Rule();
        final NodeRef nodeRef = (restRuleModel.getId() != null) ? nodes.validateOrLookupNode(restRuleModel.getId(), null) : null;
        serviceRule.setNodeRef(nodeRef);
        serviceRule.setTitle(restRuleModel.getName());
        serviceRule.setDescription(restRuleModel.getDescription());
        serviceRule.setRuleDisabled(!restRuleModel.isEnabled());
        serviceRule.applyToChildren(restRuleModel.isCascade());
        serviceRule.setExecuteAsynchronously(restRuleModel.isAsynchronous());
        serviceRule.setRuleTypes(restRuleModel.getTriggers());
        serviceRule.setAction(actionMapper.toServiceModel(restRuleModel.getActions()));
        if (restRuleModel.getErrorScript() != null)
        {
            final org.alfresco.service.cmr.action.Action compensatingAction =
                    new ActionImpl(null, GUID.generate(), ScriptActionExecuter.NAME);
            compensatingAction.setParameterValue(ScriptActionExecuter.PARAM_SCRIPTREF, restRuleModel.getErrorScript());
            serviceRule.getAction().setCompensatingAction(compensatingAction);
        }
        if (restRuleModel.getConditions() != null)
        {
            compositeConditionMapper.toServiceModels(restRuleModel.getConditions())
                    .forEach(condition -> serviceRule.getAction().addActionCondition(condition));
        }

        return serviceRule;
    }
}
