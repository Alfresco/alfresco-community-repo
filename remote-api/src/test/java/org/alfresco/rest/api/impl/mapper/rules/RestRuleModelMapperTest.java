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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;

import org.alfresco.repo.action.ActionConditionImpl;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.action.CompositeActionImpl;
import org.alfresco.repo.action.executer.ScriptActionExecuter;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.impl.rules.ActionParameterConverter;
import org.alfresco.rest.api.model.rules.Action;
import org.alfresco.rest.api.model.rules.CompositeCondition;
import org.alfresco.rest.api.model.rules.Rule;
import org.alfresco.rest.api.model.rules.RuleTrigger;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.RuleType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@Experimental
@RunWith(MockitoJUnitRunner.class)
public class RestRuleModelMapperTest
{
    private static final String RULE_ID = "fake-rule-id";
    private static final String RULE_NAME = "rule name";
    private static final String RULE_DESCRIPTION = "rule description";
    private static final boolean RULE_ENABLED = true;
    private static final boolean RULE_INHERITABLE = true;
    private static final boolean RULE_ASYNC = true;
    private static final String ACTION_DEFINITION_NAME = "action-def-name";
    private static final String ERROR_SCRIPT = "error-script-ref";

    @Mock
    private RestRuleActionModelMapper actionMapperMock;
    @Mock
    private RestRuleCompositeConditionModelMapper compositeConditionMapperMock;
    @Mock
    private Nodes nodesMock;
    @Mock
    private ActionParameterConverter actionParameterConverterMock;

    private RestRuleModelMapper objectUnderTest;

    @Before
    public void setUp()
    {
        objectUnderTest = new RestRuleModelMapper(compositeConditionMapperMock, actionMapperMock, nodesMock, actionParameterConverterMock);
    }

    @Test
    public void testToRestModel()
    {
        final org.alfresco.service.cmr.rule.Rule ruleModel = createRuleModel();
        given(actionParameterConverterMock.convertParamFromServiceModel(any())).willAnswer(a -> a.getArgument(0));
        given(actionMapperMock.toRestModel(createActionModel())).willReturn(createAction());
        given(compositeConditionMapperMock.toRestModel(List.of(createConditionModel()))).willReturn(createCondition());

        // when
        final Rule actualRule = objectUnderTest.toRestModel(ruleModel);

        then(compositeConditionMapperMock).should().toRestModel(ruleModel.getAction().getActionConditions());
        then(compositeConditionMapperMock).shouldHaveNoMoreInteractions();
        then(actionParameterConverterMock).should().convertParamFromServiceModel(ERROR_SCRIPT);
        then(actionParameterConverterMock).shouldHaveNoMoreInteractions();
        ((CompositeAction) ruleModel.getAction()).getActions().forEach(a -> then(actionMapperMock).should().toRestModel(a));
        final Rule expectedRule = createRuleWithDefaultValues();
        assertThat(actualRule).isNotNull().usingRecursiveComparison().isEqualTo(expectedRule);
    }

    @Test
    public void testToRestModelWithNullValues()
    {
        final org.alfresco.service.cmr.rule.Rule ruleModel = new org.alfresco.service.cmr.rule.Rule();
        final Rule expectedRule = Rule.builder().isEnabled(true).create();

        // when
        final Rule actualRule = objectUnderTest.toRestModel(ruleModel);

        assertThat(actualRule).isNotNull().usingRecursiveComparison().isEqualTo(expectedRule);

    }

    @Test
    public void testToServiceModel()
    {
        final Rule rule = createRuleWithDefaultValues();
        final Action action = Action.builder().actionDefinitionId(ACTION_DEFINITION_NAME).create();
        rule.setActions(List.of(action));
        final CompositeCondition compositeCondition = CompositeCondition.builder().create();
        final org.alfresco.service.cmr.rule.Rule expectedRuleModel = createRuleModel();
        rule.setConditions(compositeCondition);
        final org.alfresco.service.cmr.action.Action actionModel = createCompositeActionModel();
        given(actionMapperMock.toServiceModel(List.of(action))).willReturn(actionModel);
        given(compositeConditionMapperMock.toServiceModels(compositeCondition)).willCallRealMethod();
        given(actionParameterConverterMock.getConvertedParams(any(), any())).willAnswer(a -> a.getArgument(0));
        // when
        final org.alfresco.service.cmr.rule.Rule actualRuleModel = objectUnderTest.toServiceModel(rule);

        then(nodesMock).should().validateOrLookupNode(RULE_ID);
        then(nodesMock).shouldHaveNoMoreInteractions();
        then(actionMapperMock).should().toServiceModel(List.of(action));
        then(actionMapperMock).shouldHaveNoMoreInteractions();
        then(compositeConditionMapperMock).should().toServiceModels(compositeCondition);
        then(compositeConditionMapperMock).shouldHaveNoMoreInteractions();
        assertThat(actualRuleModel)
                .isNotNull()
                .usingRecursiveComparison().ignoringFields("nodeRef", "action")
                .isEqualTo(expectedRuleModel);
        assertThat(actualRuleModel.getAction())
                .isNotNull();
        final org.alfresco.service.cmr.action.Action expectedCompensatingActionModel = createCompensatingActionModel();
        assertThat(actualRuleModel.getAction().getCompensatingAction())
                .isNotNull()
                .usingRecursiveComparison().ignoringFields("id")
                .isEqualTo(expectedCompensatingActionModel);
    }

    @Test
    public void testToServiceModel_withNullValues()
    {
        final Rule rule = new Rule();
        final org.alfresco.service.cmr.rule.Rule expectedRuleModel = new org.alfresco.service.cmr.rule.Rule();
        expectedRuleModel.setRuleDisabled(true);

        // when
        final org.alfresco.service.cmr.rule.Rule actualRuleModel = objectUnderTest.toServiceModel(rule);

        then(nodesMock).shouldHaveNoInteractions();
        assertThat(actualRuleModel)
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringFields("ruleTypes")
                .isEqualTo(expectedRuleModel);
    }

    private Rule createRuleWithDefaultValues()
    {
        return Rule.builder()
                .id(RULE_ID)
                .name(RULE_NAME)
                .description(RULE_DESCRIPTION)
                .isEnabled(RULE_ENABLED)
                .isInheritable(RULE_INHERITABLE)
                .isAsynchronous(RULE_ASYNC)
                .triggers(List.of(RuleTrigger.INBOUND, RuleTrigger.UPDATE))
                .errorScript(ERROR_SCRIPT)
                .actions(List.of(createAction()))
                .conditions(createCondition())
                .create();
    }

    private CompositeCondition createCondition()
    {
        return CompositeCondition.builder().create();
    }

    private Action createAction() {
        return Action.builder().actionDefinitionId(ACTION_DEFINITION_NAME).create();
    }

    private static org.alfresco.service.cmr.rule.Rule createRuleModel()
    {
        final NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, RULE_ID);
        final org.alfresco.service.cmr.rule.Rule ruleModel = new org.alfresco.service.cmr.rule.Rule(nodeRef);
        ruleModel.setTitle(RULE_NAME);
        ruleModel.setDescription(RULE_DESCRIPTION);
        ruleModel.setRuleDisabled(!RULE_ENABLED);
        ruleModel.applyToChildren(RULE_INHERITABLE);
        ruleModel.setExecuteAsynchronously(RULE_ASYNC);
        ruleModel.setRuleTypes(List.of(RuleType.INBOUND, RuleType.UPDATE));
        ruleModel.setAction(createCompositeActionModel());

        return ruleModel;
    }

    private static org.alfresco.service.cmr.action.Action createCompositeActionModel()
    {
        final ActionCondition actionCondition = createConditionModel();
        final org.alfresco.service.cmr.action.CompositeAction compositeActionModel = new CompositeActionImpl(null, "composite-action");
        compositeActionModel.addAction(createActionModel());
        compositeActionModel.setCompensatingAction(createCompensatingActionModel());
        compositeActionModel.addActionCondition(actionCondition);

        return compositeActionModel;
    }

    private static ActionConditionImpl createConditionModel()
    {
        return new ActionConditionImpl("action-condition-id", "action-condition-def-name");
    }

    private static ActionImpl createActionModel()
    {
        return new ActionImpl(null, "action-id", ACTION_DEFINITION_NAME);
    }

    private static org.alfresco.service.cmr.action.Action createCompensatingActionModel()
    {
        final org.alfresco.service.cmr.action.Action compensatingActionModel =
                new ActionImpl(null, "compensating-action-id", ScriptActionExecuter.NAME);
        compensatingActionModel.setParameterValue(ScriptActionExecuter.PARAM_SCRIPTREF, ERROR_SCRIPT);

        return compensatingActionModel;
    }

}
