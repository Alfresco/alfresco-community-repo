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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.alfresco.repo.action.ActionConditionImpl;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.action.executer.ScriptActionExecuter;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.RuleType;
import org.assertj.core.api.Condition;
import org.junit.Test;

@Experimental
public class RuleTest
{
    private static final String RULE_ID = "fake-rule-id";
    private static final String RULE_NAME = "rule name";
    private static final String RULE_DESCRIPTION = "rule description";
    private static final boolean RULE_ENABLED = true;
    private static final boolean RULE_CASCADE = true;
    private static final boolean RULE_ASYNC = false;
    private static final boolean RULE_SHARED = true;
    private static final String ERROR_SCRIPT = "error-script-ref";

    @Test
    public void testFrom()
    {
        final org.alfresco.service.cmr.rule.Rule ruleModel = createRuleModel();
        final Rule expectedRule = createRuleWithDefaultValues();

        // when
        final Rule actualRule = Rule.from(ruleModel, RULE_SHARED);

        assertThat(actualRule).isNotNull().usingRecursiveComparison().isEqualTo(expectedRule);

    }

    @Test
    public void testFromRuleModelWithNullValues()
    {
        final org.alfresco.service.cmr.rule.Rule ruleModel = new org.alfresco.service.cmr.rule.Rule();
        final Rule expectedRule = Rule.builder().enabled(true).create();

        // when
        final Rule actualRule = Rule.from(ruleModel, false);

        assertThat(actualRule).isNotNull().usingRecursiveComparison().isEqualTo(expectedRule);

    }

    private static org.alfresco.service.cmr.rule.Rule createRuleModel() {
        final NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, RULE_ID);
        final org.alfresco.service.cmr.rule.Rule ruleModel = new org.alfresco.service.cmr.rule.Rule(nodeRef);
        ruleModel.setTitle(RULE_NAME);
        ruleModel.setDescription(RULE_DESCRIPTION);
        ruleModel.setRuleDisabled(!RULE_ENABLED);
        ruleModel.applyToChildren(RULE_CASCADE);
        ruleModel.setExecuteAsynchronously(RULE_ASYNC);
        ruleModel.setRuleTypes(List.of(RuleType.INBOUND, RuleType.UPDATE));
        final Action compensatingAction = new ActionImpl(nodeRef, "compensatingActionId", "compensatingActionDefName");
        compensatingAction.setParameterValue(ScriptActionExecuter.PARAM_SCRIPTREF, ERROR_SCRIPT);
        final ActionCondition actionCondition = new ActionConditionImpl("actionConditionId", "actionConditionDefName");
        final Action action = new ActionImpl(nodeRef, "actionId", "actionDefName");
        action.setCompensatingAction(compensatingAction);
        action.addActionCondition(actionCondition);
        ruleModel.setAction(action);

        return ruleModel;
    }

    private static Rule createRuleWithDefaultValues() {
        return Rule.builder()
            .id(RULE_ID)
            .name(RULE_NAME)
            .description(RULE_DESCRIPTION)
            .enabled(RULE_ENABLED)
            .cascade(RULE_CASCADE)
            .asynchronous(RULE_ASYNC)
            .shared(RULE_SHARED)
            .triggers(List.of(RuleTrigger.INBOUND, RuleTrigger.UPDATE))
            .errorScript(ERROR_SCRIPT)
            .create();
    }
}