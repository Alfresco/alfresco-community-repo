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

import static java.util.Collections.emptyList;

import static org.alfresco.rest.api.model.rules.RuleSet.DEFAULT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.rules.CompositeCondition;
import org.alfresco.rest.api.model.rules.Rule;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.core.exceptions.RelationshipResourceNotFoundException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@Experimental
@RunWith(MockitoJUnitRunner.class)
public class RulesImplTest extends TestCase
{
    private static final String FOLDER_NODE_ID = "dummy-folder-node-id";
    private static final String RULE_SET_ID = "dummy-rule-set-id";
    private static final String RULE_ID = "dummy-rule-id";
    private static final NodeRef folderNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, FOLDER_NODE_ID);
    private static final NodeRef ruleSetNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, RULE_SET_ID);
    private static final NodeRef ruleNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, RULE_ID);
    private static final Paging paging = Paging.DEFAULT;
    private static final String ACTION_DEFINITION_NAME = "actionDefinitionName";
    private static final Map<String, Serializable> DUMMY_PARAMS = Map.of("dummy-key", "dummy-value");
    private static final Action action = new ActionImpl(folderNodeRef, "actionId", ACTION_DEFINITION_NAME, DUMMY_PARAMS);

    @Mock
    private Nodes nodesMock;

    @Mock
    private NodeValidator nodeValidatorMock;

    @Mock
    private RuleService ruleServiceMock;

    @Mock
    private ActionParameterConverter actionParameterConverterMock;

    @Mock
    private ActionPermissionValidator actionPermissionValidatorMock;

    @Mock
    private Rule ruleBodyMock;
    @Mock
    private org.alfresco.service.cmr.rule.Rule serviceRuleMock;

    @InjectMocks
    private RulesImpl rules;


    @Before
    @Override
    public void setUp() throws Exception
    {
        given(nodeValidatorMock.validateFolderNode(any(), anyBoolean())).willReturn(folderNodeRef);
        given(nodeValidatorMock.validateRuleSetNode(any(), any())).willReturn(ruleSetNodeRef);
        given(nodeValidatorMock.validateRuleNode(any(), any())).willReturn(ruleNodeRef);
    }

    @Test
    public void testGetRules()
    {
        given(ruleServiceMock.getRules(any())).willReturn(List.of(createRule(RULE_ID)));

        // when
        final CollectionWithPagingInfo<Rule> rulesPage = rules.getRules(FOLDER_NODE_ID, RULE_SET_ID, paging);

        then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, false);
        then(nodeValidatorMock).should().validateRuleSetNode(RULE_SET_ID, folderNodeRef);
        then(nodeValidatorMock).should().isRuleSetNotNullAndShared(ruleSetNodeRef);
        then(nodeValidatorMock).shouldHaveNoMoreInteractions();
        then(ruleServiceMock).should().getRules(folderNodeRef);
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
        assertThat(rulesPage)
                .isNotNull()
            .extracting(CollectionWithPagingInfo::getCollection)
                .isNotNull()
            .extracting(Collection::size)
                .isEqualTo(1);
        assertThat(rulesPage.getCollection().stream().findFirst().orElse(null))
                .isNotNull()
            .extracting(Rule::getId)
                .isEqualTo(RULE_ID);
    }

    @Test
    public void testGetRules_emptyResult()
    {
        given(ruleServiceMock.getRules(any())).willReturn(emptyList());

        // when
        final CollectionWithPagingInfo<Rule> rulesPage = rules.getRules(FOLDER_NODE_ID, RULE_SET_ID, paging);

        then(ruleServiceMock).should().getRules(folderNodeRef);
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
        assertThat(rulesPage)
            .isNotNull()
            .extracting(CollectionWithPagingInfo::getCollection)
            .isNotNull()
            .extracting(Collection::isEmpty)
            .isEqualTo(true);
    }

    @Test
    public void testGetRules_invalidFolder()
    {
        for (Exception exception : folderValidationExceptions())
        {
            Mockito.reset(nodeValidatorMock);
            given(nodeValidatorMock.validateFolderNode(any(), anyBoolean())).willThrow(exception);

            // when
            assertThatExceptionOfType(exception.getClass()).isThrownBy(
                () -> rules.getRules(FOLDER_NODE_ID, RULE_SET_ID, paging));

            then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, false);
            then(nodeValidatorMock).shouldHaveNoMoreInteractions();
            then(ruleServiceMock).shouldHaveNoInteractions();
        }
    }

    @Test
    public void testGetRules_invalidRuleSet()
    {
        for (Exception exception : ruleSetValidationExceptions())
        {
            Mockito.reset(nodeValidatorMock);
            given(nodeValidatorMock.validateFolderNode(any(), anyBoolean())).willReturn(folderNodeRef);
            given(nodeValidatorMock.validateRuleSetNode(any(), any())).willThrow(exception);

            // when
            assertThatExceptionOfType(exception.getClass()).isThrownBy(
                () -> rules.getRules(FOLDER_NODE_ID, RULE_SET_ID, paging));

            then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, false);
            then(nodeValidatorMock).should().validateRuleSetNode(RULE_SET_ID, folderNodeRef);
            then(nodeValidatorMock).shouldHaveNoMoreInteractions();
            then(ruleServiceMock).shouldHaveNoInteractions();
        }
    }

    @Test
    public void testGetRuleById()
    {
        given(ruleServiceMock.getRule(any())).willReturn(createRule(RULE_ID));

        // when
        final Rule rule = rules.getRuleById(FOLDER_NODE_ID, RULE_SET_ID, RULE_ID);

        then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, false);
        then(nodeValidatorMock).should().validateRuleSetNode(RULE_SET_ID, folderNodeRef);
        then(nodeValidatorMock).should().validateRuleNode(RULE_ID, ruleSetNodeRef);
        then(nodeValidatorMock).should().isRuleSetNotNullAndShared(ruleSetNodeRef);
        then(nodeValidatorMock).shouldHaveNoMoreInteractions();
        then(nodesMock).shouldHaveNoInteractions();
        then(ruleServiceMock).should().getRule(ruleNodeRef);
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
        assertThat(rule)
                .isNotNull()
            .extracting(Rule::getId)
                .isEqualTo(RULE_ID);
    }

    @Test
    public void testGetRuleById_invalidFolder()
    {
        for (Exception exception : folderValidationExceptions())
        {
            Mockito.reset(nodeValidatorMock);
            given(nodeValidatorMock.validateFolderNode(any(), anyBoolean())).willThrow(exception);

            // when
            assertThatExceptionOfType(exception.getClass()).isThrownBy(
                () -> rules.getRuleById(FOLDER_NODE_ID, RULE_SET_ID, RULE_ID));

            then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, false);
            then(nodeValidatorMock).shouldHaveNoMoreInteractions();
            then(ruleServiceMock).shouldHaveNoInteractions();
        }
    }

    @Test
    public void testGetRuleById_invalidRuleSet()
    {
        for (Exception exception : ruleSetValidationExceptions())
        {
            Mockito.reset(nodeValidatorMock);
            given(nodeValidatorMock.validateFolderNode(any(), anyBoolean())).willReturn(folderNodeRef);
            given(nodeValidatorMock.validateRuleSetNode(any(), any())).willThrow(exception);

            // when
            assertThatExceptionOfType(exception.getClass()).isThrownBy(
                () -> rules.getRuleById(FOLDER_NODE_ID, RULE_SET_ID, RULE_ID));

            then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, false);
            then(nodeValidatorMock).should().validateRuleSetNode(RULE_SET_ID, folderNodeRef);
            then(nodeValidatorMock).shouldHaveNoMoreInteractions();
            then(ruleServiceMock).shouldHaveNoInteractions();
        }
    }

    @Test
    public void testGetRuleById_invalidRule()
    {
        for (Exception exception : ruleValidationExceptions())
        {
            Mockito.reset(nodeValidatorMock);
            given(nodeValidatorMock.validateFolderNode(any(), anyBoolean())).willReturn(folderNodeRef);
            given(nodeValidatorMock.validateRuleSetNode(any(), any())).willReturn(ruleSetNodeRef);
            given(nodeValidatorMock.validateRuleNode(any(), any())).willThrow(exception);

            // when
            assertThatExceptionOfType(exception.getClass()).isThrownBy(
                () -> rules.getRuleById(FOLDER_NODE_ID, RULE_SET_ID, RULE_ID));

            then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, false);
            then(nodeValidatorMock).should().validateRuleSetNode(RULE_SET_ID, folderNodeRef);
            then(nodeValidatorMock).should().validateRuleNode(RULE_ID, ruleSetNodeRef);
            then(nodeValidatorMock).shouldHaveNoMoreInteractions();
            then(ruleServiceMock).shouldHaveNoInteractions();
        }
    }

    /** Create a single rule. */
    @Test
    public void testCreateRules()
    {
        List<Rule> ruleList = List.of(ruleBodyMock);
        given(ruleBodyMock.toServiceModel(nodesMock)).willReturn(serviceRuleMock);
        final org.alfresco.rest.api.model.rules.Action ruleAction = new org.alfresco.rest.api.model.rules.Action();
        ruleAction.setActionDefinitionId(ACTION_DEFINITION_NAME);
        ruleAction.setParams(DUMMY_PARAMS);
        given(ruleBodyMock.getActions()).willReturn(List.of(ruleAction));
        given(ruleServiceMock.saveRule(folderNodeRef, serviceRuleMock)).willAnswer(arg -> arg.getArguments()[1]);
        given(serviceRuleMock.getNodeRef()).willReturn(ruleNodeRef);
        given(serviceRuleMock.getAction()).willReturn(action);
        given(actionPermissionValidatorMock.validateRulePermissions(any())).willAnswer(arg -> arg.getArguments()[0]);

        // when
        List<Rule> actual = rules.createRules(folderNodeRef.getId(), ruleSetNodeRef.getId(), ruleList);

        then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, true);
        then(nodeValidatorMock).should().validateRuleSetNode(RULE_SET_ID, folderNodeRef);
        then(nodeValidatorMock).should().isRuleSetNotNullAndShared(ruleSetNodeRef, folderNodeRef);
        then(nodeValidatorMock).shouldHaveNoMoreInteractions();
        then(nodeValidatorMock).should().isRuleSetNotNullAndShared(ruleSetNodeRef, folderNodeRef);
        then(actionParameterConverterMock).should().convertParameters(DUMMY_PARAMS, ACTION_DEFINITION_NAME);
        then(actionParameterConverterMock).shouldHaveNoMoreInteractions();
        then(actionPermissionValidatorMock).should().validateRulePermissions(serviceRuleMock);
        then(actionPermissionValidatorMock).shouldHaveNoMoreInteractions();
        then(ruleServiceMock).should().saveRule(folderNodeRef, ruleBodyMock.toServiceModel(nodesMock));
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
        List<Rule> expected = List.of(Rule.from(serviceRuleMock, false));
        assertThat(actual).isEqualTo(expected);
    }

    /** Check that when passing the default rule set then we don't perform any validation around the rule set node. */
    @Test
    public void testCreateRules_defaultRuleSet()
    {
        List<Rule> ruleList = List.of(ruleBodyMock);
        given(ruleBodyMock.toServiceModel(nodesMock)).willReturn(serviceRuleMock);
        final org.alfresco.rest.api.model.rules.Action ruleAction = new org.alfresco.rest.api.model.rules.Action();
        ruleAction.setActionDefinitionId(ACTION_DEFINITION_NAME);
        ruleAction.setParams(DUMMY_PARAMS);
        given(ruleBodyMock.getActions()).willReturn(List.of(ruleAction));
        given(ruleServiceMock.saveRule(folderNodeRef, serviceRuleMock)).willAnswer(arg -> arg.getArguments()[1]);
        given(serviceRuleMock.getNodeRef()).willReturn(ruleNodeRef);
        given(serviceRuleMock.getAction()).willReturn(action);
        given(actionPermissionValidatorMock.validateRulePermissions(any())).willAnswer(arg -> arg.getArguments()[0]);

        // when
        List<Rule> actual = rules.createRules(folderNodeRef.getId(), DEFAULT_ID, ruleList);

        then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, true);
        then(nodeValidatorMock).should().isRuleSetNotNullAndShared(null, folderNodeRef);
        then(nodeValidatorMock).shouldHaveNoMoreInteractions();
        then(actionParameterConverterMock).should().convertParameters(DUMMY_PARAMS, ACTION_DEFINITION_NAME);
        then(actionParameterConverterMock).shouldHaveNoMoreInteractions();
        then(actionPermissionValidatorMock).should().validateRulePermissions(serviceRuleMock);
        then(actionPermissionValidatorMock).shouldHaveNoMoreInteractions();
        then(ruleServiceMock).should().saveRule(folderNodeRef, ruleBodyMock.toServiceModel(nodesMock));
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
        List<Rule> expected = List.of(Rule.from(serviceRuleMock, false));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testCreateRules_emptyRuleList()
    {
        List<Rule> ruleList = emptyList();

        // when
        List<Rule> actual = rules.createRules(folderNodeRef.getId(), ruleSetNodeRef.getId(), ruleList);

        then(ruleServiceMock).shouldHaveNoInteractions();
        assertThat(actual).isEqualTo(emptyList());
    }

    /** Create three rules in a single call and check they are all passed to the RuleService. */
    @Test
    public void testCreateRules_createMultipleRules()
    {
        List<Rule> ruleBodyList = new ArrayList<>();
        List<Rule> expected = new ArrayList<>();
        for (String ruleId : List.of("A", "B", "C"))
        {
            Rule ruleBody = mock(Rule.class);
            final org.alfresco.rest.api.model.rules.Action ruleAction = new org.alfresco.rest.api.model.rules.Action();
            ruleAction.setActionDefinitionId(ACTION_DEFINITION_NAME);
            ruleAction.setParams(DUMMY_PARAMS);
            given(ruleBody.getActions()).willReturn(List.of(ruleAction));
            ruleBodyList.add(ruleBody);
            org.alfresco.service.cmr.rule.Rule serviceRule = mock(org.alfresco.service.cmr.rule.Rule.class);
            given(ruleBody.toServiceModel(nodesMock)).willReturn(serviceRule);
            given(ruleServiceMock.saveRule(folderNodeRef, serviceRule)).willAnswer(arg -> arg.getArguments()[1]);
            NodeRef ruleNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, ruleId);
            given(serviceRule.getNodeRef()).willReturn(ruleNodeRef);
            given(serviceRule.getAction()).willReturn(action);
            expected.add(Rule.from(serviceRule, false));
            given(actionPermissionValidatorMock.validateRulePermissions(any())).willAnswer(arg -> arg.getArguments()[0]);
        }

        // when
        List<Rule> actual = rules.createRules(folderNodeRef.getId(), ruleSetNodeRef.getId(), ruleBodyList);

        then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, true);
        then(nodeValidatorMock).should().validateRuleSetNode(RULE_SET_ID, folderNodeRef);
        then(nodeValidatorMock).should(times(ruleBodyList.size())).isRuleSetNotNullAndShared(ruleSetNodeRef, folderNodeRef);
        then(nodeValidatorMock).shouldHaveNoMoreInteractions();
        for (Rule ruleBody : ruleBodyList)
        {
            then(actionPermissionValidatorMock).should().validateRulePermissions(ruleBody.toServiceModel(nodesMock));
            then(ruleServiceMock).should().saveRule(folderNodeRef, ruleBody.toServiceModel(nodesMock));
        }
        then(actionParameterConverterMock).should(times(3)).convertParameters(DUMMY_PARAMS, ACTION_DEFINITION_NAME);
        then(actionParameterConverterMock).shouldHaveNoMoreInteractions();
        then(actionPermissionValidatorMock).shouldHaveNoMoreInteractions();
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testCreateRules_invalidFolder()
    {
        for (Exception exception : folderValidationExceptions())
        {
            Mockito.reset(nodeValidatorMock);
            given(nodeValidatorMock.validateFolderNode(any(), anyBoolean())).willThrow(exception);

            // when
            assertThatExceptionOfType(exception.getClass()).isThrownBy(
                () -> rules.createRules(folderNodeRef.getId(), ruleSetNodeRef.getId(), emptyList()));

            then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, true);
            then(nodeValidatorMock).shouldHaveNoMoreInteractions();
            then(ruleServiceMock).shouldHaveNoInteractions();
        }
    }

    @Test
    public void testCreateRules_invalidRuleSet()
    {
        for (Exception exception : ruleSetValidationExceptions())
        {
            Mockito.reset(nodeValidatorMock);
            given(nodeValidatorMock.validateFolderNode(any(), anyBoolean())).willReturn(folderNodeRef);
            given(nodeValidatorMock.validateRuleSetNode(any(), any())).willThrow(exception);

            // when
            assertThatExceptionOfType(exception.getClass()).isThrownBy(
                () -> rules.createRules(folderNodeRef.getId(), ruleSetNodeRef.getId(), emptyList()));

            then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, true);
            then(nodeValidatorMock).should().validateRuleSetNode(RULE_SET_ID, folderNodeRef);
            then(nodeValidatorMock).shouldHaveNoMoreInteractions();
            then(ruleServiceMock).shouldHaveNoInteractions();
        }
    }

    /** Check that we can update a rule. */
    @Test
    public void testUpdateRuleById()
    {
        given(nodeValidatorMock.isRuleSetNotNullAndShared(any(), any())).willReturn(true);
        given(ruleBodyMock.toServiceModel(nodesMock)).willReturn(serviceRuleMock);
        given(ruleServiceMock.saveRule(folderNodeRef, serviceRuleMock)).willAnswer(arg -> arg.getArguments()[1]);
        given(serviceRuleMock.getNodeRef()).willReturn(ruleNodeRef);
        given(serviceRuleMock.getAction()).willReturn(action);

        // when
        Rule updatedRule = rules.updateRuleById(folderNodeRef.getId(), ruleSetNodeRef.getId(), RULE_ID, ruleBodyMock);

        then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, true);
        then(nodeValidatorMock).should().validateRuleSetNode(RULE_SET_ID, folderNodeRef);
        then(nodeValidatorMock).should().validateRuleNode(RULE_ID, ruleSetNodeRef);
        then(nodeValidatorMock).should().isRuleSetNotNullAndShared(ruleSetNodeRef, folderNodeRef);
        then(nodeValidatorMock).shouldHaveNoMoreInteractions();
        then(ruleServiceMock).should().saveRule(folderNodeRef, serviceRuleMock);
        then(ruleServiceMock).shouldHaveNoMoreInteractions();


        Rule expected = Rule.builder().id(RULE_ID)
                                      .enabled(true)
                                      .shared(true)
                                      .triggers(emptyList())
                                      .conditions(CompositeCondition.builder().inverted(false).create())
                                      .create();
        assertThat(updatedRule).isEqualTo(expected);
    }

    @Test
    public void testUpdateRuleById_invalidFolder()
    {
        for (Exception exception : folderValidationExceptions())
        {
            Mockito.reset(nodeValidatorMock);
            given(nodeValidatorMock.validateFolderNode(any(), anyBoolean())).willThrow(exception);

            // when
            assertThatExceptionOfType(exception.getClass()).isThrownBy(
                () -> rules.updateRuleById(folderNodeRef.getId(), ruleSetNodeRef.getId(), RULE_ID, mock(Rule.class)));

            then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, true);
            then(nodeValidatorMock).shouldHaveNoMoreInteractions();
            then(ruleServiceMock).shouldHaveNoInteractions();
        }
    }

    @Test
    public void testUpdateRuleById_invalidRuleSet()
    {
        for (Exception exception : ruleSetValidationExceptions())
        {
            Mockito.reset(nodeValidatorMock);
            given(nodeValidatorMock.validateFolderNode(any(), anyBoolean())).willReturn(folderNodeRef);
            given(nodeValidatorMock.validateRuleSetNode(any(), any())).willThrow(exception);

            // when
            assertThatExceptionOfType(exception.getClass()).isThrownBy(
                () -> rules.updateRuleById(folderNodeRef.getId(), ruleSetNodeRef.getId(), RULE_ID, mock(Rule.class)));

            then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, true);
            then(nodeValidatorMock).should().validateRuleSetNode(RULE_SET_ID, folderNodeRef);
            then(nodeValidatorMock).shouldHaveNoMoreInteractions();
            then(ruleServiceMock).shouldHaveNoInteractions();
        }
    }

    @Test
    public void testUpdateRuleById_invalidRule()
    {
        for (Exception exception : ruleValidationExceptions())
        {
            Mockito.reset(nodeValidatorMock);
            given(nodeValidatorMock.validateFolderNode(any(), anyBoolean())).willReturn(folderNodeRef);
            given(nodeValidatorMock.validateRuleSetNode(any(), any())).willReturn(ruleSetNodeRef);
            given(nodeValidatorMock.validateRuleNode(any(), any())).willThrow(exception);

            // when
            assertThatExceptionOfType(exception.getClass()).isThrownBy(
                () -> rules.updateRuleById(folderNodeRef.getId(), ruleSetNodeRef.getId(), RULE_ID, mock(Rule.class)));

            then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, true);
            then(nodeValidatorMock).should().validateRuleSetNode(RULE_SET_ID, folderNodeRef);
            then(nodeValidatorMock).should().validateRuleNode(RULE_ID, ruleSetNodeRef);
            then(nodeValidatorMock).shouldHaveNoMoreInteractions();
            then(ruleServiceMock).shouldHaveNoInteractions();
        }
    }

    @Test
    public void testDeleteRuleById() {
        org.alfresco.service.cmr.rule.Rule rule = createRule(RULE_ID);
        given(ruleServiceMock.getRule(any())).willReturn(rule);

        //when
        rules.deleteRuleById(FOLDER_NODE_ID, RULE_SET_ID, RULE_ID);

        then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, true);
        then(nodeValidatorMock).should().validateRuleSetNode(RULE_SET_ID, folderNodeRef);
        then(nodeValidatorMock).should().validateRuleNode(RULE_ID, ruleSetNodeRef);
        then(nodeValidatorMock).shouldHaveNoMoreInteractions();
        then(nodesMock).shouldHaveNoInteractions();
        then(ruleServiceMock).should().getRule(ruleNodeRef);
        then(ruleServiceMock).should().removeRule(folderNodeRef, rule);
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testDeleteRuleById_invalidFolder()
    {
        for (Exception exception : folderValidationExceptions())
        {
            Mockito.reset(nodeValidatorMock);
            given(nodeValidatorMock.validateFolderNode(any(), anyBoolean())).willThrow(exception);

            // when
            assertThatExceptionOfType(exception.getClass()).isThrownBy(
                () -> rules.deleteRuleById(FOLDER_NODE_ID, RULE_SET_ID, RULE_ID));

            then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, true);
            then(nodeValidatorMock).shouldHaveNoMoreInteractions();
            then(ruleServiceMock).shouldHaveNoInteractions();
        }
    }

    @Test
    public void testDeleteRuleById_invalidRuleSet()
    {
        for (Exception exception : ruleSetValidationExceptions())
        {
            Mockito.reset(nodeValidatorMock);
            given(nodeValidatorMock.validateFolderNode(any(), anyBoolean())).willReturn(folderNodeRef);
            given(nodeValidatorMock.validateRuleSetNode(any(), any())).willThrow(exception);

            // when
            assertThatExceptionOfType(exception.getClass()).isThrownBy(
                () -> rules.deleteRuleById(FOLDER_NODE_ID, RULE_SET_ID, RULE_ID));

            then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, true);
            then(nodeValidatorMock).should().validateRuleSetNode(RULE_SET_ID, folderNodeRef);
            then(nodeValidatorMock).shouldHaveNoMoreInteractions();
            then(ruleServiceMock).shouldHaveNoInteractions();
        }
    }

    @Test
    public void testDeleteRuleById_invalidRule()
    {
        for (Exception exception : ruleValidationExceptions())
        {
            Mockito.reset(nodeValidatorMock);
            given(nodeValidatorMock.validateFolderNode(any(), anyBoolean())).willReturn(folderNodeRef);
            given(nodeValidatorMock.validateRuleSetNode(any(), any())).willReturn(ruleSetNodeRef);
            given(nodeValidatorMock.validateRuleNode(any(), any())).willThrow(exception);

            // when
            assertThatExceptionOfType(exception.getClass()).isThrownBy(
                () -> rules.deleteRuleById(FOLDER_NODE_ID, RULE_SET_ID, RULE_ID));

            then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, true);
            then(nodeValidatorMock).should().validateRuleSetNode(RULE_SET_ID, folderNodeRef);
            then(nodeValidatorMock).should().validateRuleNode(RULE_ID, ruleSetNodeRef);
            then(nodeValidatorMock).shouldHaveNoMoreInteractions();
            then(ruleServiceMock).shouldHaveNoInteractions();
        }
    }

    private static org.alfresco.service.cmr.rule.Rule createRule(final String id) {
        final NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);
        final org.alfresco.service.cmr.rule.Rule rule = new org.alfresco.service.cmr.rule.Rule();
        rule.setNodeRef(nodeRef);
        rule.setRuleType("ruleType");
        rule.setAction(action);

        return rule;
    }

    private static List<Exception> folderValidationExceptions()
    {
        return List.of(
            new EntityNotFoundException(FOLDER_NODE_ID),
            new InvalidArgumentException(),
            new PermissionDeniedException()
        );
    }

    private static List<Exception> ruleSetValidationExceptions()
    {
        return List.of(
            new EntityNotFoundException(RULE_SET_ID),
            new InvalidArgumentException(),
            new RelationshipResourceNotFoundException(RULE_SET_ID, "fake-relationship-id")
        );
    }

    private static List<Exception> ruleValidationExceptions()
    {
        return List.of(
            new EntityNotFoundException(RULE_ID),
            new InvalidArgumentException(),
            new RelationshipResourceNotFoundException(RULE_ID, "fake-relationship-id")
        );
    }
}
