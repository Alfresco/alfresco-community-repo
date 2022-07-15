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

package org.alfresco.rest.api.impl;

import static java.util.Collections.emptyList;

import static org.alfresco.rest.api.model.rules.RuleSet.DEFAULT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.rules.Rule;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
    private static final String RULE_NAME = "Rule name";

    @Mock
    private Nodes nodesMock;

    @Mock
    private PermissionService permissionServiceMock;

    @Mock
    private RuleService ruleServiceMock;

    @InjectMocks
    private RulesImpl rules;

    @Before
    @Override
    public void setUp() throws Exception
    {
        MockitoAnnotations.openMocks(this);

        given(nodesMock.validateOrLookupNode(eq(FOLDER_NODE_ID), any())).willReturn(folderNodeRef);
        given(nodesMock.validateNode(eq(RULE_SET_ID))).willReturn(ruleSetNodeRef);
        given(nodesMock.nodeMatches(any(), any(), any())).willReturn(true);
        given(permissionServiceMock.hasReadPermission(any())).willReturn(AccessStatus.ALLOWED);
    }

    @Test
    public void testGetRules()
    {
        given(ruleServiceMock.isRuleSetAssociatedWithFolder(any(), any())).willReturn(true);
        given(ruleServiceMock.getRules(any())).willReturn(List.of(createRule(RULE_ID)));

        // when
        final CollectionWithPagingInfo<Rule> rulesPage = rules.getRules(FOLDER_NODE_ID, RULE_SET_ID, paging);

        then(nodesMock).should().validateOrLookupNode(eq(FOLDER_NODE_ID), isNull());
        then(nodesMock).should().validateNode(eq(RULE_SET_ID));
        then(nodesMock).should().nodeMatches(eq(folderNodeRef), any(), isNull());
        then(nodesMock).should().nodeMatches(eq(ruleSetNodeRef), any(), isNull());
        then(nodesMock).shouldHaveNoMoreInteractions();
        then(permissionServiceMock).should().hasReadPermission(eq(folderNodeRef));
        then(permissionServiceMock).shouldHaveNoMoreInteractions();
        then(ruleServiceMock).should().isRuleSetAssociatedWithFolder(eq(ruleSetNodeRef), eq(folderNodeRef));
        then(ruleServiceMock).should().getRules(eq(folderNodeRef));
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
    public void testGetRulesForDefaultRuleSet()
    {
        given(ruleServiceMock.getRules(any())).willReturn(List.of(createRule(RULE_ID)));

        // when
        final CollectionWithPagingInfo<Rule> rulesPage = rules.getRules(FOLDER_NODE_ID, DEFAULT_ID, paging);

        then(nodesMock).should().validateOrLookupNode(eq(FOLDER_NODE_ID), isNull());
        then(nodesMock).should().nodeMatches(eq(folderNodeRef), any(), isNull());
        then(nodesMock).shouldHaveNoMoreInteractions();
        then(permissionServiceMock).should().hasReadPermission(eq(folderNodeRef));
        then(permissionServiceMock).shouldHaveNoMoreInteractions();
        then(ruleServiceMock).should().getRuleSetNode(eq(folderNodeRef));
        then(ruleServiceMock).should().getRules(eq(folderNodeRef));
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
    public void testGetRulesForNotExistingFolderNode()
    {
        given(nodesMock.nodeMatches(eq(folderNodeRef), any(), any())).willReturn(false);

        // when
        assertThatExceptionOfType(InvalidArgumentException.class).isThrownBy(
            () -> rules.getRules(FOLDER_NODE_ID, RULE_SET_ID, paging));

        then(ruleServiceMock).shouldHaveNoInteractions();
    }

    @Test
    public void testGetRulesForNotExistingRuleSetNode()
    {
        given(nodesMock.nodeMatches(eq(folderNodeRef), any(), any())).willReturn(true);
        given(nodesMock.nodeMatches(eq(ruleSetNodeRef), any(), any())).willReturn(false);

        // when
        assertThatExceptionOfType(InvalidArgumentException.class).isThrownBy(
            () -> rules.getRules(FOLDER_NODE_ID, RULE_SET_ID, paging));

        then(ruleServiceMock).shouldHaveNoInteractions();
    }

    @Test
    public void testGetRulesForNotAssociatedRuleSetToFolder()
    {
        given(ruleServiceMock.isRuleSetAssociatedWithFolder(any(), any())).willReturn(false);

        // when
        assertThatExceptionOfType(InvalidArgumentException.class).isThrownBy(
            () -> rules.getRules(FOLDER_NODE_ID, RULE_SET_ID, paging));

        then(ruleServiceMock).should().isRuleSetAssociatedWithFolder(eq(ruleSetNodeRef), eq(folderNodeRef));
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testGetRulesWithoutReadPermission()
    {
        given(permissionServiceMock.hasReadPermission(any())).willReturn(AccessStatus.DENIED);

        // when
        assertThatExceptionOfType(PermissionDeniedException.class).isThrownBy(
            () -> rules.getRules(FOLDER_NODE_ID, RULE_SET_ID, paging));

        then(ruleServiceMock).shouldHaveNoInteractions();
    }

    @Test
    public void testGetRuleById()
    {
        given(nodesMock.validateNode(eq(RULE_ID))).willReturn(ruleNodeRef);
        given(ruleServiceMock.isRuleSetAssociatedWithFolder(any(), any())).willReturn(true);
        given(ruleServiceMock.isRuleAssociatedWithRuleSet(any(), any())).willReturn(true);
        given(ruleServiceMock.getRule(any())).willReturn(createRule(RULE_ID));

        // when
        final Rule rule = rules.getRuleById(FOLDER_NODE_ID, RULE_SET_ID, RULE_ID);

        then(nodesMock).should().validateOrLookupNode(eq(FOLDER_NODE_ID), isNull());
        then(nodesMock).should().validateNode(eq(RULE_SET_ID));
        then(nodesMock).should().validateNode(eq(RULE_ID));
        then(nodesMock).should().nodeMatches(eq(folderNodeRef), any(), isNull());
        then(nodesMock).should().nodeMatches(eq(ruleSetNodeRef), any(), isNull());
        then(nodesMock).should().nodeMatches(eq(ruleNodeRef), any(), isNull());
        then(nodesMock).shouldHaveNoMoreInteractions();
        then(permissionServiceMock).should().hasReadPermission(eq(folderNodeRef));
        then(permissionServiceMock).shouldHaveNoMoreInteractions();
        then(ruleServiceMock).should().isRuleSetAssociatedWithFolder(eq(ruleSetNodeRef), eq(folderNodeRef));
        then(ruleServiceMock).should().isRuleAssociatedWithRuleSet(eq(ruleNodeRef), eq(ruleSetNodeRef));
        then(ruleServiceMock).should().getRule(eq(ruleNodeRef));
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
        assertThat(rule)
                .isNotNull()
            .extracting(Rule::getId)
                .isEqualTo(RULE_ID);
    }

    @Test
    public void testGetRuleByIdForDefaultRuleSet()
    {
        final String defaultRuleSetId = "-default-";
        given(nodesMock.validateNode(eq(RULE_ID))).willReturn(ruleNodeRef);
        given(ruleServiceMock.getRuleSetNode(any())).willReturn(ruleSetNodeRef);
        given(ruleServiceMock.isRuleAssociatedWithRuleSet(any(), any())).willReturn(true);
        given(ruleServiceMock.getRule(any())).willReturn(createRule(RULE_ID));

        // when
        final Rule rule = rules.getRuleById(FOLDER_NODE_ID, defaultRuleSetId, RULE_ID);

        then(nodesMock).should().validateOrLookupNode(eq(FOLDER_NODE_ID), isNull());
        then(nodesMock).should().validateNode(eq(RULE_ID));
        then(nodesMock).should().nodeMatches(eq(folderNodeRef), any(), isNull());
        then(nodesMock).should().nodeMatches(eq(ruleNodeRef), any(), isNull());
        then(nodesMock).shouldHaveNoMoreInteractions();
        then(permissionServiceMock).should().hasReadPermission(eq(folderNodeRef));
        then(permissionServiceMock).shouldHaveNoMoreInteractions();
        then(ruleServiceMock).should().getRuleSetNode(eq(folderNodeRef));
        then(ruleServiceMock).should().isRuleAssociatedWithRuleSet(eq(ruleNodeRef), eq(ruleSetNodeRef));
        then(ruleServiceMock).should().getRule(eq(ruleNodeRef));
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
        assertThat(rule)
                .isNotNull()
            .extracting(Rule::getId)
                .isEqualTo(RULE_ID);
    }

    @Test
    public void testGetRuleByIdForNotAssociatedRuleToRuleSet()
    {
        given(nodesMock.validateNode(eq(RULE_SET_ID))).willReturn(ruleSetNodeRef);
        given(nodesMock.validateNode(eq(RULE_ID))).willReturn(ruleNodeRef);
        given(ruleServiceMock.isRuleSetAssociatedWithFolder(any(), any())).willReturn(true);
        given(ruleServiceMock.isRuleAssociatedWithRuleSet(any(), any())).willReturn(false);

        // when
        assertThatExceptionOfType(InvalidArgumentException.class).isThrownBy(
            () -> rules.getRuleById(FOLDER_NODE_ID, RULE_SET_ID, RULE_ID));

        then(ruleServiceMock).should().isRuleSetAssociatedWithFolder(eq(ruleSetNodeRef), eq(folderNodeRef));
        then(ruleServiceMock).should().isRuleAssociatedWithRuleSet(eq(ruleNodeRef), eq(ruleSetNodeRef));
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
    }

    /** Create a single rule. */
    @Test
    public void testSaveRules()
    {
        Rule ruleBody = mock(Rule.class);
        List<Rule> ruleList = List.of(ruleBody);
        given(ruleServiceMock.isRuleSetAssociatedWithFolder(any(), any())).willReturn(true);
        org.alfresco.service.cmr.rule.Rule serviceRuleBody = mock(org.alfresco.service.cmr.rule.Rule.class);
        given(ruleBody.toServiceModel(nodesMock)).willReturn(serviceRuleBody);
        org.alfresco.service.cmr.rule.Rule serviceRule = mock(org.alfresco.service.cmr.rule.Rule.class);
        given(ruleServiceMock.saveRule(folderNodeRef, serviceRuleBody)).willReturn(serviceRule);
        given(serviceRule.getNodeRef()).willReturn(ruleNodeRef);

        // when
        List<Rule> actual = rules.createRules(folderNodeRef.getId(), ruleSetNodeRef.getId(), ruleList);

        then(ruleServiceMock).should().isRuleSetAssociatedWithFolder(ruleSetNodeRef, folderNodeRef);
        then(ruleServiceMock).should().saveRule(folderNodeRef, ruleBody.toServiceModel(nodesMock));
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
        List<Rule> expected = List.of(Rule.from(serviceRule));
        assertThat(actual).isEqualTo(expected);
    }

    /** Check that when passing the default rule set then we don't perform any validation around the rule set node. */
    @Test
    public void testSaveRules_defaultRuleSet()
    {
        NodeRef defaultRuleSetNodeRef = new NodeRef("default://rule/set");
        given(ruleServiceMock.getRuleSetNode(folderNodeRef)).willReturn(defaultRuleSetNodeRef);
        Rule ruleBody = mock(Rule.class);
        List<Rule> ruleList = List.of(ruleBody);
        org.alfresco.service.cmr.rule.Rule serviceRuleBody = mock(org.alfresco.service.cmr.rule.Rule.class);
        given(ruleBody.toServiceModel(nodesMock)).willReturn(serviceRuleBody);
        org.alfresco.service.cmr.rule.Rule serviceRule = mock(org.alfresco.service.cmr.rule.Rule.class);
        given(ruleServiceMock.saveRule(folderNodeRef, serviceRuleBody)).willReturn(serviceRule);
        given(serviceRule.getNodeRef()).willReturn(ruleNodeRef);

        // when
        List<Rule> actual = rules.createRules(folderNodeRef.getId(), DEFAULT_ID, ruleList);

        then(ruleServiceMock).should().getRuleSetNode(folderNodeRef);
        then(ruleServiceMock).should().saveRule(folderNodeRef, ruleBody.toServiceModel(nodesMock));
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
        List<Rule> expected = List.of(Rule.from(serviceRule));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testSaveRules_ruleSetNotAssociatedWithFolder()
    {
        Rule rule = Rule.builder().name(RULE_NAME).create();
        List<Rule> ruleList = List.of(rule);
        given(ruleServiceMock.isRuleSetAssociatedWithFolder(ruleSetNodeRef, folderNodeRef)).willReturn(false);

        // when
        assertThatExceptionOfType(InvalidArgumentException.class).isThrownBy(
                () -> rules.createRules(folderNodeRef.getId(), ruleSetNodeRef.getId(), ruleList));

        then(ruleServiceMock).should().isRuleSetAssociatedWithFolder(ruleSetNodeRef, folderNodeRef);
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testSaveRules_emptyRuleList()
    {
        given(ruleServiceMock.isRuleSetAssociatedWithFolder(any(), any())).willReturn(true);
        List<Rule> ruleList = emptyList();

        // when
        List<Rule> actual = this.rules.createRules(folderNodeRef.getId(), ruleSetNodeRef.getId(), ruleList);

        then(ruleServiceMock).should().isRuleSetAssociatedWithFolder(ruleSetNodeRef, folderNodeRef);
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
        assertThat(actual).isEqualTo(emptyList());
    }

    /** Create three rules in a single call and check they are all passed to the RuleService. */
    @Test
    public void testSaveRules_createMultipleRules()
    {
        given(ruleServiceMock.isRuleSetAssociatedWithFolder(any(), any())).willReturn(true);
        List<Rule> ruleBodyList = new ArrayList<>();
        List<Rule> expected = new ArrayList<>();
        for (String ruleId : List.of("A", "B", "C"))
        {
            Rule ruleBody = mock(Rule.class);
            ruleBodyList.add(ruleBody);
            org.alfresco.service.cmr.rule.Rule serviceRuleBody = mock(org.alfresco.service.cmr.rule.Rule.class);
            given(ruleBody.toServiceModel(nodesMock)).willReturn(serviceRuleBody);
            org.alfresco.service.cmr.rule.Rule serviceRule = mock(org.alfresco.service.cmr.rule.Rule.class);
            given(ruleServiceMock.saveRule(folderNodeRef, serviceRuleBody)).willReturn(serviceRule);
            NodeRef ruleNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, ruleId);
            given(serviceRule.getNodeRef()).willReturn(ruleNodeRef);
            expected.add(Rule.from(serviceRule));
        }

        // when
        List<Rule> actual = rules.createRules(folderNodeRef.getId(), ruleSetNodeRef.getId(), ruleBodyList);

        then(ruleServiceMock).should().isRuleSetAssociatedWithFolder(ruleSetNodeRef, folderNodeRef);
        for (Rule ruleBody : ruleBodyList)
        {
            then(ruleServiceMock).should().saveRule(folderNodeRef, ruleBody.toServiceModel(nodesMock));
        }
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
        assertThat(actual).isEqualTo(expected);
    }

    private static org.alfresco.service.cmr.rule.Rule createRule(final String id) {
        final org.alfresco.service.cmr.rule.Rule rule = new org.alfresco.service.cmr.rule.Rule();
        rule.setNodeRef(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id));

        return rule;
    }
}