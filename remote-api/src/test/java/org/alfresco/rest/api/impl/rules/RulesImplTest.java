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
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import junit.framework.TestCase;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.action.CompositeActionImpl;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.rules.Rule;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.core.exceptions.RelationshipResourceNotFoundException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.NamespaceService;
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
    private static final NodeRef FOLDER_NODE_REF = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, FOLDER_NODE_ID);
    private static final NodeRef RULE_SET_NODE_REF = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, RULE_SET_ID);
    private static final NodeRef RULE_NODE_REF = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, RULE_ID);
    private static final Paging PAGING = Paging.DEFAULT;
    private static final List<String> INCLUDE = emptyList();
    private static final String ACTION_DEFINITION_NAME = "actionDefinitionName";
    private static final Map<String, Serializable> DUMMY_PARAMS = Map.of("dummy-key", "dummy-value");

    @Mock
    private Nodes nodesMock;
    @Mock
    private NamespaceService namespaceService;
    @Mock
    private NodeValidator nodeValidatorMock;
    @Mock
    private RuleService ruleServiceMock;
    @Mock
    private RuleLoader ruleLoaderMock;
    @Mock
    private ActionParameterConverter actionParameterConverterMock;
    @Mock
    private ActionPermissionValidator actionPermissionValidatorMock;
    @Mock
    private org.alfresco.service.cmr.rule.Rule serviceRuleMock;
    @Mock
    private Rule ruleMock;
    private org.alfresco.service.cmr.rule.Rule ruleModel = createRule(RULE_ID);
    private CompositeAction compositeAction = new CompositeActionImpl(RULE_NODE_REF, "compositeActionId");

    @InjectMocks
    private RulesImpl rules;

    @Before
    @Override
    public void setUp() throws Exception
    {
        given(nodeValidatorMock.validateFolderNode(any(), anyBoolean())).willReturn(FOLDER_NODE_REF);
        given(nodeValidatorMock.validateRuleSetNode(any(), any())).willReturn(RULE_SET_NODE_REF);
        given(nodeValidatorMock.validateRuleNode(any(), any())).willReturn(RULE_NODE_REF);

        given(ruleServiceMock.getRule(RULE_NODE_REF)).willReturn(ruleModel);
        given(ruleServiceMock.getRules(FOLDER_NODE_REF, false)).willReturn(List.of(ruleModel));
        given(ruleServiceMock.getOwningNodeRef(RULE_SET_NODE_REF)).willReturn(FOLDER_NODE_REF);

        given(ruleLoaderMock.loadRule(ruleModel, INCLUDE)).willReturn(ruleMock);

        compositeAction.addAction(new ActionImpl(FOLDER_NODE_REF, "actionId", ACTION_DEFINITION_NAME, DUMMY_PARAMS));
    }

    @Test
    public void testGetRules()
    {
        given(ruleLoaderMock.loadRule(ruleModel, emptyList())).willReturn(ruleMock);

        // when
        final CollectionWithPagingInfo<Rule> rulesPage = rules.getRules(FOLDER_NODE_ID, RULE_SET_ID, INCLUDE, PAGING);

        then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, false);
        then(nodeValidatorMock).should().validateRuleSetNode(RULE_SET_ID, FOLDER_NODE_REF);
        then(nodeValidatorMock).shouldHaveNoMoreInteractions();
        then(ruleServiceMock).should().getOwningNodeRef(RULE_SET_NODE_REF);
        then(ruleServiceMock).should().getRules(FOLDER_NODE_REF, false);
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
        then(ruleLoaderMock).should().loadRule(ruleModel, emptyList());
        then(ruleLoaderMock).shouldHaveNoMoreInteractions();
        assertThat(rulesPage)
                .isNotNull()
                .extracting(CollectionWithPagingInfo::getCollection)
                .isNotNull()
                .extracting(Collection::size)
                .isEqualTo(1);
        assertThat(rulesPage.getCollection().stream().findFirst().get()).isEqualTo(ruleMock);
    }

    @Test
    public void testGetRules_emptyResult()
    {
        given(ruleServiceMock.getRules(FOLDER_NODE_REF, false)).willReturn(emptyList());

        // when
        final CollectionWithPagingInfo<Rule> rulesPage = rules.getRules(FOLDER_NODE_ID, RULE_SET_ID, INCLUDE, PAGING);

        then(ruleServiceMock).should().getOwningNodeRef(RULE_SET_NODE_REF);
        then(ruleServiceMock).should().getRules(FOLDER_NODE_REF, false);
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
                    () -> rules.getRules(FOLDER_NODE_ID, RULE_SET_ID, INCLUDE, PAGING));

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
            given(nodeValidatorMock.validateFolderNode(any(), anyBoolean())).willReturn(FOLDER_NODE_REF);
            given(nodeValidatorMock.validateRuleSetNode(any(), any())).willThrow(exception);

            // when
            assertThatExceptionOfType(exception.getClass()).isThrownBy(
                    () -> rules.getRules(FOLDER_NODE_ID, RULE_SET_ID, INCLUDE, PAGING));

            then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, false);
            then(nodeValidatorMock).should().validateRuleSetNode(RULE_SET_ID, FOLDER_NODE_REF);
            then(nodeValidatorMock).shouldHaveNoMoreInteractions();
            then(ruleServiceMock).shouldHaveNoInteractions();
        }
    }

    @Test
    public void testGetRuleById()
    {
        // when
        final Rule rule = rules.getRuleById(FOLDER_NODE_ID, RULE_SET_ID, RULE_ID, INCLUDE);

        then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, false);
        then(nodeValidatorMock).should().validateRuleSetNode(RULE_SET_ID, FOLDER_NODE_REF);
        then(nodeValidatorMock).should().validateRuleNode(RULE_ID, RULE_SET_NODE_REF);
        then(nodeValidatorMock).shouldHaveNoMoreInteractions();
        then(nodesMock).shouldHaveNoInteractions();
        then(ruleServiceMock).should().getRule(RULE_NODE_REF);
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
        assertThat(rule).isEqualTo(ruleMock);
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
                    () -> rules.getRuleById(FOLDER_NODE_ID, RULE_SET_ID, RULE_ID, INCLUDE));

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
            given(nodeValidatorMock.validateFolderNode(any(), anyBoolean())).willReturn(FOLDER_NODE_REF);
            given(nodeValidatorMock.validateRuleSetNode(any(), any())).willThrow(exception);

            // when
            assertThatExceptionOfType(exception.getClass()).isThrownBy(
                    () -> rules.getRuleById(FOLDER_NODE_ID, RULE_SET_ID, RULE_ID, INCLUDE));

            then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, false);
            then(nodeValidatorMock).should().validateRuleSetNode(RULE_SET_ID, FOLDER_NODE_REF);
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
            given(nodeValidatorMock.validateFolderNode(any(), anyBoolean())).willReturn(FOLDER_NODE_REF);
            given(nodeValidatorMock.validateRuleSetNode(any(), any())).willReturn(RULE_SET_NODE_REF);
            given(nodeValidatorMock.validateRuleNode(any(), any())).willThrow(exception);

            // when
            assertThatExceptionOfType(exception.getClass()).isThrownBy(
                    () -> rules.getRuleById(FOLDER_NODE_ID, RULE_SET_ID, RULE_ID, INCLUDE));

            then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, false);
            then(nodeValidatorMock).should().validateRuleSetNode(RULE_SET_ID, FOLDER_NODE_REF);
            then(nodeValidatorMock).should().validateRuleNode(RULE_ID, RULE_SET_NODE_REF);
            then(nodeValidatorMock).shouldHaveNoMoreInteractions();
            then(ruleServiceMock).shouldHaveNoInteractions();
        }
    }

    /**
     * Create a single rule.
     */
    @Test
    public void testCreateRules()
    {
        List<Rule> ruleList = List.of(ruleMock);
        given(ruleMock.toServiceModel(nodesMock, namespaceService)).willReturn(serviceRuleMock);
        given(serviceRuleMock.getAction()).willReturn(compositeAction);
        given(ruleServiceMock.saveRule(FOLDER_NODE_REF, serviceRuleMock)).willAnswer(arg -> arg.getArguments()[1]);
        given(ruleLoaderMock.loadRule(serviceRuleMock, INCLUDE)).willReturn(ruleMock);
        given(actionPermissionValidatorMock.validateRulePermissions(any())).willAnswer(arg -> arg.getArguments()[0]);

        // when
        List<Rule> actual = rules.createRules(FOLDER_NODE_REF.getId(), RULE_SET_NODE_REF.getId(), ruleList, INCLUDE);

        then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, true);
        then(nodeValidatorMock).should().validateRuleSetNode(RULE_SET_ID, FOLDER_NODE_REF);
        then(nodeValidatorMock).shouldHaveNoMoreInteractions();
        then(actionParameterConverterMock).should().getConvertedParams(DUMMY_PARAMS, ACTION_DEFINITION_NAME);
        then(actionParameterConverterMock).shouldHaveNoMoreInteractions();
        then(actionPermissionValidatorMock).should().validateRulePermissions(serviceRuleMock);
        then(actionPermissionValidatorMock).shouldHaveNoMoreInteractions();
        then(ruleServiceMock).should().saveRule(FOLDER_NODE_REF, ruleMock.toServiceModel(nodesMock, namespaceService));
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
        List<Rule> expected = List.of(ruleMock);
        assertThat(actual).isEqualTo(expected);
    }

    /**
     * Check that when passing the default rule set then we don't perform any validation around the rule set node.
     */
    @Test
    public void testCreateRules_defaultRuleSet()
    {
        List<Rule> ruleList = List.of(ruleMock);
        given(ruleMock.toServiceModel(nodesMock, namespaceService)).willReturn(serviceRuleMock);
        given(ruleServiceMock.saveRule(FOLDER_NODE_REF, serviceRuleMock)).willAnswer(arg -> arg.getArguments()[1]);
        given(ruleLoaderMock.loadRule(serviceRuleMock, INCLUDE)).willReturn(ruleMock);
        given(serviceRuleMock.getAction()).willReturn(compositeAction);
        given(actionPermissionValidatorMock.validateRulePermissions(any())).willAnswer(arg -> arg.getArguments()[0]);

        // when
        List<Rule> actual = rules.createRules(FOLDER_NODE_REF.getId(), DEFAULT_ID, ruleList, INCLUDE);

        then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, true);
        then(nodeValidatorMock).shouldHaveNoMoreInteractions();
        then(actionParameterConverterMock).should().getConvertedParams(DUMMY_PARAMS, ACTION_DEFINITION_NAME);
        then(actionParameterConverterMock).shouldHaveNoMoreInteractions();
        then(actionPermissionValidatorMock).should().validateRulePermissions(serviceRuleMock);
        then(actionPermissionValidatorMock).shouldHaveNoMoreInteractions();
        then(ruleServiceMock).should().saveRule(FOLDER_NODE_REF, ruleMock.toServiceModel(nodesMock, namespaceService));
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
        List<Rule> expected = List.of(ruleMock);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testCreateRules_emptyRuleList()
    {
        List<Rule> ruleList = emptyList();

        // when
        List<Rule> actual = rules.createRules(FOLDER_NODE_REF.getId(), RULE_SET_NODE_REF.getId(), ruleList, INCLUDE);

        then(ruleServiceMock).shouldHaveNoInteractions();
        assertThat(actual).isEqualTo(emptyList());
    }

    /**
     * Create three rules in a single call and check they are all passed to the RuleService.
     */
    @Test
    public void testCreateRules_createMultipleRules()
    {
        List<Rule> ruleBodyList = new ArrayList<>();
        List<Rule> expected = new ArrayList<>();
        IntStream.range(0, 3).forEach(i -> {
            Rule ruleBodyMock = mock(Rule.class);
            ruleBodyList.add(ruleBodyMock);
            org.alfresco.service.cmr.rule.Rule serviceRuleMockInner = mock(org.alfresco.service.cmr.rule.Rule.class);
            given(ruleBodyMock.toServiceModel(nodesMock, namespaceService)).willReturn(serviceRuleMockInner);
            final CompositeAction compositeActionInner = new CompositeActionImpl(RULE_NODE_REF, "compositeActionInnerId");
            compositeActionInner.addAction(new ActionImpl(FOLDER_NODE_REF, "actionInnerId", ACTION_DEFINITION_NAME, DUMMY_PARAMS));
            given(serviceRuleMockInner.getAction()).willReturn(compositeActionInner);
            given(ruleServiceMock.saveRule(FOLDER_NODE_REF, serviceRuleMockInner)).willAnswer(arg -> arg.getArguments()[1]);
            Rule ruleMockInner = mock(Rule.class);
            given(ruleLoaderMock.loadRule(serviceRuleMockInner, INCLUDE)).willReturn(ruleMockInner);
            expected.add(ruleMockInner);
            given(actionPermissionValidatorMock.validateRulePermissions(any())).willAnswer(arg -> arg.getArguments()[0]);
        });

        // when
        List<Rule> actual = rules.createRules(FOLDER_NODE_REF.getId(), RULE_SET_NODE_REF.getId(), ruleBodyList, INCLUDE);

        then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, true);
        then(nodeValidatorMock).should().validateRuleSetNode(RULE_SET_ID, FOLDER_NODE_REF);
        then(nodeValidatorMock).shouldHaveNoMoreInteractions();
        for (Rule ruleBody : ruleBodyList)
        {
            then(actionPermissionValidatorMock).should().validateRulePermissions(ruleBody.toServiceModel(nodesMock, namespaceService));
            then(ruleServiceMock).should().saveRule(FOLDER_NODE_REF, ruleBody.toServiceModel(nodesMock, namespaceService));
        }
        then(actionParameterConverterMock).should(times(3)).getConvertedParams(DUMMY_PARAMS, ACTION_DEFINITION_NAME);
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
                    () -> rules.createRules(FOLDER_NODE_REF.getId(), RULE_SET_NODE_REF.getId(), emptyList(), INCLUDE));

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
            given(nodeValidatorMock.validateFolderNode(any(), anyBoolean())).willReturn(FOLDER_NODE_REF);
            given(nodeValidatorMock.validateRuleSetNode(any(), any())).willThrow(exception);

            // when
            assertThatExceptionOfType(exception.getClass()).isThrownBy(
                    () -> rules.createRules(FOLDER_NODE_REF.getId(), RULE_SET_NODE_REF.getId(), emptyList(), INCLUDE));

            then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, true);
            then(nodeValidatorMock).should().validateRuleSetNode(RULE_SET_ID, FOLDER_NODE_REF);
            then(nodeValidatorMock).shouldHaveNoMoreInteractions();
            then(ruleServiceMock).shouldHaveNoInteractions();
        }
    }

    /**
     * Check that we can update a rule.
     */
    @Test
    public void testUpdateRuleById()
    {
        given(ruleMock.toServiceModel(nodesMock, namespaceService)).willReturn(serviceRuleMock);
        given(ruleServiceMock.saveRule(FOLDER_NODE_REF, serviceRuleMock)).willAnswer(a -> a.getArguments()[1]);
        given(serviceRuleMock.getAction()).willReturn(compositeAction);
        given(ruleLoaderMock.loadRule(serviceRuleMock, INCLUDE)).willReturn(ruleMock);
        given(actionPermissionValidatorMock.validateRulePermissions(any())).willAnswer(arg -> arg.getArguments()[0]);

        // when
        Rule updatedRule = rules.updateRuleById(FOLDER_NODE_REF.getId(), RULE_SET_NODE_REF.getId(), RULE_ID, ruleMock, INCLUDE);

        then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, true);
        then(nodeValidatorMock).should().validateRuleSetNode(RULE_SET_ID, FOLDER_NODE_REF);
        then(nodeValidatorMock).should().validateRuleNode(RULE_ID, RULE_SET_NODE_REF);
        then(nodeValidatorMock).shouldHaveNoMoreInteractions();
        then(ruleServiceMock).should().saveRule(FOLDER_NODE_REF, serviceRuleMock);
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
        then(actionParameterConverterMock).should().getConvertedParams(DUMMY_PARAMS, ACTION_DEFINITION_NAME);
        then(actionParameterConverterMock).shouldHaveNoMoreInteractions();
        then(actionPermissionValidatorMock).should().validateRulePermissions(serviceRuleMock);
        then(actionPermissionValidatorMock).shouldHaveNoMoreInteractions();
        assertThat(updatedRule).isEqualTo(ruleMock);
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
                    () -> rules.updateRuleById(FOLDER_NODE_REF.getId(), RULE_SET_NODE_REF.getId(), RULE_ID, mock(Rule.class), INCLUDE));

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
            given(nodeValidatorMock.validateFolderNode(any(), anyBoolean())).willReturn(FOLDER_NODE_REF);
            given(nodeValidatorMock.validateRuleSetNode(any(), any())).willThrow(exception);

            // when
            assertThatExceptionOfType(exception.getClass()).isThrownBy(
                    () -> rules.updateRuleById(FOLDER_NODE_REF.getId(), RULE_SET_NODE_REF.getId(), RULE_ID, mock(Rule.class), INCLUDE));

            then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, true);
            then(nodeValidatorMock).should().validateRuleSetNode(RULE_SET_ID, FOLDER_NODE_REF);
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
            given(nodeValidatorMock.validateFolderNode(any(), anyBoolean())).willReturn(FOLDER_NODE_REF);
            given(nodeValidatorMock.validateRuleSetNode(any(), any())).willReturn(RULE_SET_NODE_REF);
            given(nodeValidatorMock.validateRuleNode(any(), any())).willThrow(exception);

            // when
            assertThatExceptionOfType(exception.getClass()).isThrownBy(
                    () -> rules.updateRuleById(FOLDER_NODE_REF.getId(), RULE_SET_NODE_REF.getId(), RULE_ID, mock(Rule.class), INCLUDE));

            then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, true);
            then(nodeValidatorMock).should().validateRuleSetNode(RULE_SET_ID, FOLDER_NODE_REF);
            then(nodeValidatorMock).should().validateRuleNode(RULE_ID, RULE_SET_NODE_REF);
            then(nodeValidatorMock).shouldHaveNoMoreInteractions();
            then(ruleServiceMock).shouldHaveNoInteractions();
        }
    }

    @Test
    public void testDeleteRuleById()
    {
        //when
        rules.deleteRuleById(FOLDER_NODE_ID, RULE_SET_ID, RULE_ID);

        then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, true);
        then(nodeValidatorMock).should().validateRuleSetNode(RULE_SET_ID, FOLDER_NODE_REF);
        then(nodeValidatorMock).should().validateRuleNode(RULE_ID, RULE_SET_NODE_REF);
        then(nodeValidatorMock).shouldHaveNoMoreInteractions();
        then(nodesMock).shouldHaveNoInteractions();
        then(ruleServiceMock).should().getRule(RULE_NODE_REF);
        then(ruleServiceMock).should().removeRule(FOLDER_NODE_REF, ruleModel);
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
            given(nodeValidatorMock.validateFolderNode(any(), anyBoolean())).willReturn(FOLDER_NODE_REF);
            given(nodeValidatorMock.validateRuleSetNode(any(), any())).willThrow(exception);

            // when
            assertThatExceptionOfType(exception.getClass()).isThrownBy(
                    () -> rules.deleteRuleById(FOLDER_NODE_ID, RULE_SET_ID, RULE_ID));

            then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, true);
            then(nodeValidatorMock).should().validateRuleSetNode(RULE_SET_ID, FOLDER_NODE_REF);
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
            given(nodeValidatorMock.validateFolderNode(any(), anyBoolean())).willReturn(FOLDER_NODE_REF);
            given(nodeValidatorMock.validateRuleSetNode(any(), any())).willReturn(RULE_SET_NODE_REF);
            given(nodeValidatorMock.validateRuleNode(any(), any())).willThrow(exception);

            // when
            assertThatExceptionOfType(exception.getClass()).isThrownBy(
                    () -> rules.deleteRuleById(FOLDER_NODE_ID, RULE_SET_ID, RULE_ID));

            then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, true);
            then(nodeValidatorMock).should().validateRuleSetNode(RULE_SET_ID, FOLDER_NODE_REF);
            then(nodeValidatorMock).should().validateRuleNode(RULE_ID, RULE_SET_NODE_REF);
            then(nodeValidatorMock).shouldHaveNoMoreInteractions();
            then(ruleServiceMock).shouldHaveNoInteractions();
        }
    }

    private static org.alfresco.service.cmr.rule.Rule createRule(final String id)
    {
        final NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);
        final org.alfresco.service.cmr.rule.Rule rule = new org.alfresco.service.cmr.rule.Rule();
        rule.setNodeRef(nodeRef);
        rule.setRuleType("ruleType");

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
