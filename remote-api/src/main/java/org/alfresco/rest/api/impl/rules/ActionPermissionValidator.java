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

package org.alfresco.rest.api.impl.rules;

import static org.alfresco.repo.web.scripts.rule.AbstractRuleWebScript.CANNOT_CREATE_RULE;
import static org.alfresco.service.cmr.rule.RuleType.OUTBOUND;

import java.util.Collections;
import java.util.List;

import org.alfresco.repo.action.CompositeActionImpl;
import org.alfresco.repo.action.RuntimeActionService;
import org.alfresco.repo.action.access.ActionAccessRestriction;
import org.alfresco.repo.action.executer.CheckOutActionExecuter;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.rule.Rule;
import org.apache.commons.collections.CollectionUtils;

@Experimental
public class ActionPermissionValidator
{
    private final RuntimeActionService runtimeActionService;

    public ActionPermissionValidator(RuntimeActionService runtimeActionService)
    {
        this.runtimeActionService = runtimeActionService;
    }

    Rule validateRulePermissions(Rule rule)
    {
        final List<Action> actions = ((CompositeActionImpl) rule.getAction()).getActions();

        checkRestrictedAccessActions(actions);
        checkRuleOutboundHasNoCheckOutAction(rule, actions);
        return rule;
    }

    private void checkRestrictedAccessActions(List<Action> actions) {
        actions.forEach(action -> {
            ActionAccessRestriction.setActionContext(action, ActionAccessRestriction.RULE_ACTION_CONTEXT);
            runtimeActionService.verifyActionAccessRestrictions(action);
        });
    }

    private void checkRuleOutboundHasNoCheckOutAction(Rule rule, List<Action> actions) {
        //TODO: rule types should never be empty in final implementation
        if (CollectionUtils.isNotEmpty(rule.getRuleTypes()) && rule.getRuleTypes().contains(OUTBOUND))
        {
            for (Action action : actions)
            {
                if (action.getActionDefinitionName().equalsIgnoreCase(CheckOutActionExecuter.NAME))
                {
                    throw new InvalidArgumentException(CANNOT_CREATE_RULE);
                }
            }
        }
    }

}
