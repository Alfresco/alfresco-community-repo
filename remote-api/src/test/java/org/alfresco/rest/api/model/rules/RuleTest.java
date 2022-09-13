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
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.List;

import org.alfresco.repo.action.ActionConditionImpl;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.action.executer.ScriptActionExecuter;
import org.alfresco.rest.api.Nodes;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.namespace.NamespaceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@Experimental
@RunWith(MockitoJUnitRunner.class)
public class RuleTest
{
    private static final String RULE_ID = "fake-rule-id";
    private static final String RULE_NAME = "rule name";
    private static final String RULE_DESCRIPTION = "rule description";
    private static final boolean RULE_ENABLED = true;
    private static final boolean RULE_CASCADE = true;
    private static final boolean RULE_ASYNC = true;
    private static final boolean RULE_SHARED = true;
    private static final String ACTION_DEFINITION_NAME = "action-def-name";
    private static final String ERROR_SCRIPT = "error-script-ref";

    private final NamespaceService namespaceService = mock(NamespaceService.class);

    @Test
    public void testFrom()
    {
        final org.alfresco.service.cmr.rule.Rule ruleModel = createRuleModel();
        final Rule expectedRule = createRuleWithDefaultValues();

        // when
        final Rule actualRule = Rule.from(ruleModel, namespaceService);

        assertThat(actualRule).isNotNull().usingRecursiveComparison().isEqualTo(expectedRule);

    }

    @Test
    public void testFromRuleModelWithNullValues()
    {
        final org.alfresco.service.cmr.rule.Rule ruleModel = new org.alfresco.service.cmr.rule.Rule();
        final Rule expectedRule = Rule.builder().enabled(true).create();

        // when
        final Rule actualRule = Rule.from(ruleModel, namespaceService);

        assertThat(actualRule).isNotNull().usingRecursiveComparison().isEqualTo(expectedRule);

    }

    @Test
    public void testToServiceModel()
    {
        final Nodes nodesMock = mock(Nodes.class);
        final Rule rule = createRuleWithDefaultValues();
        rule.setActions(List.of(Action.builder().actionDefinitionId(ACTION_DEFINITION_NAME).create()));
        final org.alfresco.service.cmr.rule.Rule expectedRuleModel = createRuleModel();
        final org.alfresco.service.cmr.action.Action expectedCompensatingActionModel = createCompensatingActionModel();

        // when
        final org.alfresco.service.cmr.rule.Rule actualRuleModel = rule.toServiceModel(nodesMock, namespaceService);

        then(nodesMock).should().validateOrLookupNode(RULE_ID, null);
        then(nodesMock).shouldHaveNoMoreInteractions();
        assertThat(actualRuleModel)
            .isNotNull()
            .usingRecursiveComparison().ignoringFields("nodeRef", "action")
            .isEqualTo(expectedRuleModel);
        assertThat(actualRuleModel.getAction())
            .isNotNull();
        assertThat(actualRuleModel.getAction().getCompensatingAction())
            .isNotNull()
            .usingRecursiveComparison().ignoringFields("id")
            .isEqualTo(expectedCompensatingActionModel);
    }

    @Test
    public void testToServiceModel_withNullValues()
    {
        final Nodes nodesMock = mock(Nodes.class);
        final Rule rule = new Rule();
        final org.alfresco.service.cmr.rule.Rule expectedRuleModel = new org.alfresco.service.cmr.rule.Rule();
        expectedRuleModel.setRuleDisabled(true);

        // when
        final org.alfresco.service.cmr.rule.Rule actualRuleModel = rule.toServiceModel(nodesMock, namespaceService);

        then(nodesMock).shouldHaveNoInteractions();
        assertThat(actualRuleModel)
            .isNotNull()
            .usingRecursiveComparison()
            .ignoringFields("ruleTypes")
            .isEqualTo(expectedRuleModel);
    }

    private Rule createRuleWithDefaultValues() {
        return Rule.builder()
            .id(RULE_ID)
            .name(RULE_NAME)
            .description(RULE_DESCRIPTION)
            .enabled(RULE_ENABLED)
            .cascade(RULE_CASCADE)
            .asynchronous(RULE_ASYNC)
            .triggers(List.of(RuleTrigger.INBOUND, RuleTrigger.UPDATE))
            .errorScript(ERROR_SCRIPT)
            .conditions(CompositeCondition.from(Collections.emptyList(), namespaceService))
            .create();
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
        ruleModel.setAction(createActionModel());

        return ruleModel;
    }

    private static org.alfresco.service.cmr.action.Action createActionModel() {
        final ActionCondition actionCondition = new ActionConditionImpl("action-condition-id", "action-condition-def-name");
        final org.alfresco.service.cmr.action.Action actionModel = new ActionImpl(null, "action-id", ACTION_DEFINITION_NAME);
        actionModel.setCompensatingAction(createCompensatingActionModel());
        actionModel.addActionCondition(actionCondition);

        return actionModel;
    }

    private static org.alfresco.service.cmr.action.Action createCompensatingActionModel() {
        final org.alfresco.service.cmr.action.Action compensatingActionModel = new ActionImpl(null, "compensating-action-id", ScriptActionExecuter.NAME);
        compensatingActionModel.setParameterValue(ScriptActionExecuter.PARAM_SCRIPTREF, ERROR_SCRIPT);

        return compensatingActionModel;
    }
}