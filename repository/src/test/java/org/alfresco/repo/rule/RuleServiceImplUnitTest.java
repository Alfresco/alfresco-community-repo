/*
 * #%L
 * Alfresco Repository
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
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.repo.rule;

import static org.alfresco.model.ContentModel.ASSOC_CONTAINS;
import static org.alfresco.repo.rule.RuleModel.ASSOC_ACTION;
import static org.alfresco.repo.rule.RuleModel.ASSOC_RULE_FOLDER;
import static org.alfresco.repo.rule.RuleModel.TYPE_RULE;
import static org.alfresco.service.cmr.security.AccessStatus.ALLOWED;
import static org.alfresco.service.cmr.security.AccessStatus.DENIED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.alfresco.repo.action.RuntimeActionService;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionServiceException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleServiceException;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/** Unit tests for {@link RuleServiceImpl}. */
public class RuleServiceImplUnitTest
{
    private static final NodeRef FOLDER_NODE = new NodeRef("folder://node/");
    private static final NodeRef RULE_SET_NODE = new NodeRef("rule://set/node");
    private static final NodeRef RULE_NODE = new NodeRef("rule://node/");
    private static final NodeRef ACTION_NODE = new NodeRef("action://node/");
    @InjectMocks
    private RuleService ruleService = new RuleServiceImpl();
    @Mock
    private NodeService nodeService;
    @Mock
    private PermissionService permissionService;
    @Mock
    private SimpleCache nodeRulesCache;
    @Mock
    private NodeService runtimeNodeService;
    @Mock
    private RuntimeActionService runtimeActionService;
    @Mock
    private Rule mockRule;
    @Mock
    private Action mockAction;

    @Before
    public void setUp()
    {
        openMocks(this);
    }

    @Test
    public void saveRule()
    {
        when(permissionService.hasPermission(FOLDER_NODE, PermissionService.CHANGE_PERMISSIONS)).thenReturn(ALLOWED);
        when(nodeService.exists(FOLDER_NODE)).thenReturn(true);
        ChildAssociationRef ruleSet = mock(ChildAssociationRef.class);
        when(ruleSet.getChildRef()).thenReturn(RULE_SET_NODE);
        when(runtimeNodeService.getChildAssocs(FOLDER_NODE, ASSOC_RULE_FOLDER, ASSOC_RULE_FOLDER)).thenReturn(List.of(ruleSet));
        ChildAssociationRef ruleAssociation = mock(ChildAssociationRef.class);
        when(ruleAssociation.getChildRef()).thenReturn(RULE_NODE);
        when(nodeService.createNode(eq(RULE_SET_NODE), eq(ASSOC_CONTAINS), any(QName.class), eq(TYPE_RULE))).thenReturn(ruleAssociation);
        // Set the rule title and action.
        when(mockRule.getTitle()).thenReturn("Rule title");
        when(mockRule.getAction()).thenReturn(mockAction);
        when(runtimeActionService.createActionNodeRef(mockAction, RULE_NODE, ASSOC_ACTION, ASSOC_ACTION)).thenReturn(ACTION_NODE);

        // Call the method under test.
        ruleService.saveRule(FOLDER_NODE, mockRule);

        then(nodeService).should(times(2)).hasAspect(FOLDER_NODE, RuleModel.ASPECT_RULES);
        then(nodeService).should().exists(FOLDER_NODE);
        then(nodeService).should().addAspect(FOLDER_NODE, RuleModel.ASPECT_RULES, null);
        then(nodeService).should().createNode(eq(RULE_SET_NODE), eq(ASSOC_CONTAINS), any(QName.class), eq(TYPE_RULE));
        then(nodeService).should(atLeastOnce()).setProperty(eq(RULE_NODE), any(QName.class), nullable(Serializable.class));
        then(nodeService).should().getChildAssocs(RULE_NODE, RuleModel.ASSOC_ACTION, RuleModel.ASSOC_ACTION);
        verifyNoMoreInteractions(nodeService);
    }

    @Test
    public void saveRule_missingAction()
    {
        when(permissionService.hasPermission(FOLDER_NODE, PermissionService.CHANGE_PERMISSIONS)).thenReturn(ALLOWED);
        when(nodeService.exists(FOLDER_NODE)).thenReturn(true);
        ChildAssociationRef ruleSet = mock(ChildAssociationRef.class);
        when(ruleSet.getChildRef()).thenReturn(RULE_SET_NODE);
        when(runtimeNodeService.getChildAssocs(FOLDER_NODE, ASSOC_RULE_FOLDER, ASSOC_RULE_FOLDER)).thenReturn(List.of(ruleSet));
        ChildAssociationRef ruleAssociation = mock(ChildAssociationRef.class);
        when(nodeService.createNode(eq(RULE_SET_NODE), eq(ASSOC_CONTAINS), any(QName.class), eq(TYPE_RULE))).thenReturn(ruleAssociation);
        // Set the title and no action for the rule.
        when(mockRule.getTitle()).thenReturn("Rule title");
        when(mockRule.getAction()).thenReturn(null);

        // Call the method under test.
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> ruleService.saveRule(FOLDER_NODE, mockRule));
    }

    @Test
    public void saveRule_missingTitle()
    {
        when(permissionService.hasPermission(FOLDER_NODE, PermissionService.CHANGE_PERMISSIONS)).thenReturn(ALLOWED);
        when(nodeService.exists(FOLDER_NODE)).thenReturn(true);
        ChildAssociationRef ruleSet = mock(ChildAssociationRef.class);
        when(ruleSet.getChildRef()).thenReturn(RULE_SET_NODE);
        when(runtimeNodeService.getChildAssocs(FOLDER_NODE, ASSOC_RULE_FOLDER, ASSOC_RULE_FOLDER)).thenReturn(List.of(ruleSet));
        ChildAssociationRef ruleAssociation = mock(ChildAssociationRef.class);
        when(nodeService.createNode(eq(RULE_SET_NODE), eq(ASSOC_CONTAINS), any(QName.class), eq(TYPE_RULE))).thenReturn(ruleAssociation);
        // The rule has an empty title.
        when(mockRule.getTitle()).thenReturn("");

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> ruleService.saveRule(FOLDER_NODE, mockRule));
    }

    @Test
    public void saveRule_errorIfFolderHasMultipleRuleSets()
    {
        when(permissionService.hasPermission(FOLDER_NODE, PermissionService.CHANGE_PERMISSIONS)).thenReturn(ALLOWED);
        when(nodeService.exists(FOLDER_NODE)).thenReturn(true);
        // Simulate a folder node with several rule sets.
        ChildAssociationRef childA = mock(ChildAssociationRef.class);
        ChildAssociationRef childB = mock(ChildAssociationRef.class);
        when(runtimeNodeService.getChildAssocs(
                FOLDER_NODE,
                ASSOC_RULE_FOLDER,
                ASSOC_RULE_FOLDER)).thenReturn(List.of(childA, childB));

        assertThatExceptionOfType(ActionServiceException.class).isThrownBy(() -> ruleService.saveRule(FOLDER_NODE, mockRule));
    }

    @Test
    public void saveRule_nodeDoesNotExist()
    {
        when(permissionService.hasPermission(FOLDER_NODE, PermissionService.CHANGE_PERMISSIONS)).thenReturn(ALLOWED);
        when(nodeService.exists(FOLDER_NODE)).thenReturn(false);

        assertThatExceptionOfType(RuleServiceException.class).isThrownBy(() -> ruleService.saveRule(FOLDER_NODE, mockRule));
    }

    @Test
    public void saveRule_accessDenied()
    {
        when(permissionService.hasPermission(FOLDER_NODE, PermissionService.CHANGE_PERMISSIONS)).thenReturn(DENIED);

        assertThatExceptionOfType(RuleServiceException.class).isThrownBy(() -> ruleService.saveRule(FOLDER_NODE, mockRule));
    }

    @Test
    public void testGetRuleSetNode()
    {
        given(runtimeNodeService.getChildAssocs(any(), any(), any())).willReturn(List.of(createAssociation(FOLDER_NODE, RULE_SET_NODE)));

        // when
        final NodeRef actualNode = ruleService.getRuleSetNode(FOLDER_NODE);

        then(runtimeNodeService).should().getChildAssocs(FOLDER_NODE, ASSOC_RULE_FOLDER, ASSOC_RULE_FOLDER);
        then(runtimeNodeService).shouldHaveNoMoreInteractions();
        then(nodeService).shouldHaveNoInteractions();
        assertThat(actualNode).isNotNull();
    }

    @Test
    public void testGetRuleSetNode_emptyAssociation()
    {
        given(runtimeNodeService.getChildAssocs(any(), any(), any())).willReturn(Collections.emptyList());

        // when
        final NodeRef actualNode = ruleService.getRuleSetNode(FOLDER_NODE);

        then(runtimeNodeService).should().getChildAssocs(FOLDER_NODE, ASSOC_RULE_FOLDER, ASSOC_RULE_FOLDER);
        then(runtimeNodeService).shouldHaveNoMoreInteractions();
        then(nodeService).shouldHaveNoInteractions();
        assertThat(actualNode).isNull();
    }

    @Test
    public void testGetRuleSetNode_notPrimaryAssociation()
    {
        given(runtimeNodeService.getChildAssocs(any(), any(), any())).willReturn(List.of(createAssociation(FOLDER_NODE, RULE_SET_NODE, false)));

        // when
        final NodeRef actualNode = ruleService.getRuleSetNode(FOLDER_NODE);

        then(runtimeNodeService).should().getChildAssocs(FOLDER_NODE, ASSOC_RULE_FOLDER, ASSOC_RULE_FOLDER);
        then(runtimeNodeService).shouldHaveNoMoreInteractions();
        then(nodeService).shouldHaveNoInteractions();
        assertThat(actualNode).isNotNull();
    }

    @Test
    public void testIsRuleSetAssociatedWithFolder()
    {
        given(runtimeNodeService.getParentAssocs(any(), any(), any())).willReturn(List.of(createAssociation(FOLDER_NODE, RULE_SET_NODE)));

        // when
        boolean associated = ruleService.isRuleSetAssociatedWithFolder(RULE_SET_NODE, FOLDER_NODE);

        then(runtimeNodeService).should().getParentAssocs(RULE_SET_NODE, ASSOC_RULE_FOLDER, ASSOC_RULE_FOLDER);
        then(runtimeNodeService).shouldHaveNoMoreInteractions();
        then(nodeService).shouldHaveNoInteractions();
        assertThat(associated).isTrue();
    }

    @Test
    public void testIsRuleSetAssociatedWithFolder_emptyAssociation()
    {
        given(runtimeNodeService.getParentAssocs(any(), any(), any())).willReturn(Collections.emptyList());

        // when
        boolean associated = ruleService.isRuleSetAssociatedWithFolder(RULE_SET_NODE, FOLDER_NODE);

        then(runtimeNodeService).should().getParentAssocs(RULE_SET_NODE, ASSOC_RULE_FOLDER, ASSOC_RULE_FOLDER);
        then(runtimeNodeService).shouldHaveNoMoreInteractions();
        then(nodeService).shouldHaveNoInteractions();
        assertThat(associated).isFalse();
    }

    @Test
    public void testIsRuleSetAssociatedWithFolder_improperAssociation()
    {
        final NodeRef fakeFolderNode = new NodeRef("folder://node/fake");
        given(runtimeNodeService.getParentAssocs(any(), any(), any())).willReturn(List.of(createAssociation(fakeFolderNode, RULE_SET_NODE)));

        // when
        boolean associated = ruleService.isRuleSetAssociatedWithFolder(RULE_SET_NODE, FOLDER_NODE);

        then(runtimeNodeService).should().getParentAssocs(RULE_SET_NODE, ASSOC_RULE_FOLDER, ASSOC_RULE_FOLDER);
        then(runtimeNodeService).shouldHaveNoMoreInteractions();
        then(nodeService).shouldHaveNoInteractions();
        assertThat(associated).isFalse();
    }

    @Test
    public void testIsRuleAssociatedWithRuleSet()
    {
        given(runtimeNodeService.getParentAssocs(any())).willReturn(List.of(createAssociation(RULE_SET_NODE, RULE_NODE)));

        // when
        boolean associated = ruleService.isRuleAssociatedWithRuleSet(RULE_NODE, RULE_SET_NODE);

        then(runtimeNodeService).should().getParentAssocs(RULE_NODE);
        then(runtimeNodeService).shouldHaveNoMoreInteractions();
        then(nodeService).shouldHaveNoInteractions();
        assertThat(associated).isTrue();
    }

    @Test
    public void testIsRuleAssociatedWithRuleSet_emptyAssociation()
    {
        given(runtimeNodeService.getParentAssocs(any())).willReturn(Collections.emptyList());

        // when
        boolean associated = ruleService.isRuleAssociatedWithRuleSet(RULE_NODE, RULE_SET_NODE);

        then(runtimeNodeService).should().getParentAssocs(RULE_NODE);
        then(runtimeNodeService).shouldHaveNoMoreInteractions();
        then(nodeService).shouldHaveNoInteractions();
        assertThat(associated).isFalse();
    }

    @Test
    public void testIsRuleAssociatedWithRuleSet_improperAssociation()
    {
        final NodeRef fakeRuleSetNode = new NodeRef("rule://set/node/fake");
        given(runtimeNodeService.getParentAssocs(any())).willReturn(List.of(createAssociation(fakeRuleSetNode, RULE_NODE)));

        // when
        boolean associated = ruleService.isRuleAssociatedWithRuleSet(RULE_NODE, RULE_SET_NODE);

        then(runtimeNodeService).should().getParentAssocs(RULE_NODE);
        then(runtimeNodeService).shouldHaveNoMoreInteractions();
        then(nodeService).shouldHaveNoInteractions();
        assertThat(associated).isFalse();
    }

    @Test
    public void testIsRuleSetShared()
    {
        given(runtimeNodeService.getParentAssocs(any())).willReturn(List.of(createAssociation(FOLDER_NODE, RULE_SET_NODE, false)));

        // when
        boolean shared = ruleService.isRuleSetShared(RULE_SET_NODE);

        then(runtimeNodeService).should().getParentAssocs(RULE_SET_NODE);
        then(runtimeNodeService).shouldHaveNoMoreInteractions();
        then(nodeService).shouldHaveNoInteractions();
        assertThat(shared).isTrue();
    }

    @Test
    public void testIsRuleSetShared_notShared()
    {
        given(runtimeNodeService.getParentAssocs(any())).willReturn(List.of(createAssociation(FOLDER_NODE, RULE_SET_NODE)));

        // when
        boolean shared = ruleService.isRuleSetShared(RULE_SET_NODE);

        then(runtimeNodeService).should().getParentAssocs(RULE_SET_NODE);
        then(runtimeNodeService).shouldHaveNoMoreInteractions();
        then(nodeService).shouldHaveNoInteractions();
        assertThat(shared).isFalse();
    }

    private static ChildAssociationRef createAssociation(final NodeRef parentRef, final NodeRef childRef)
    {
        return createAssociation(parentRef, childRef, true);
    }

    private static ChildAssociationRef createAssociation(final NodeRef parentRef, final NodeRef childRef, final boolean isPrimary)
    {
        return new ChildAssociationRef(null, parentRef, null, childRef, isPrimary, 1);
    }
}
