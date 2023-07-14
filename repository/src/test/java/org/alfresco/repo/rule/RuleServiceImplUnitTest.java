/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import static org.alfresco.model.ContentModel.ASSOC_CONTAINS;
import static org.alfresco.model.ContentModel.ASSOC_MEMBER;
import static org.alfresco.model.ContentModel.TYPE_CONTENT;
import static org.alfresco.model.ContentModel.TYPE_FOLDER;
import static org.alfresco.repo.rule.RuleModel.ASPECT_IGNORE_INHERITED_RULES;
import static org.alfresco.repo.rule.RuleModel.ASSOC_ACTION;
import static org.alfresco.repo.rule.RuleModel.ASSOC_RULE_FOLDER;
import static org.alfresco.repo.rule.RuleModel.TYPE_RULE;
import static org.alfresco.service.cmr.security.AccessStatus.ALLOWED;
import static org.alfresco.service.cmr.security.AccessStatus.DENIED;
import static org.alfresco.service.namespace.RegexQNamePattern.MATCH_ALL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.alfresco.repo.action.RuntimeActionService;
import org.alfresco.repo.cache.DefaultSimpleCache;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionServiceException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleServiceException;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.collections.MapUtils;
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
    private SimpleCache<NodeRef, List<Rule>> nodeRulesCache;
    @Mock
    private NodeService runtimeNodeService;
    @Mock
    private RuntimeActionService runtimeActionService;
    @Mock
    private DictionaryService dictionaryService;
    @Mock
    private Rule mockRule;
    @Mock
    private Action mockAction;

    @Before
    public void setUp()
    {
        openMocks(this);

        when(dictionaryService.isSubClass(TYPE_FOLDER, TYPE_FOLDER)).thenReturn(true);
        when(dictionaryService.isSubClass(TYPE_CONTENT, TYPE_FOLDER)).thenReturn(false);
        when(permissionService.hasReadPermission(any())).thenReturn(ALLOWED);
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
        given(runtimeNodeService.getChildAssocs(any(), any(), any())).willReturn(emptyList());

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
        then(runtimeNodeService).should().hasAspect(FOLDER_NODE, ASPECT_IGNORE_INHERITED_RULES);
        then(runtimeNodeService).should().getParentAssocs(FOLDER_NODE);
        then(runtimeNodeService).shouldHaveNoMoreInteractions();
        then(nodeService).shouldHaveNoInteractions();
        assertThat(associated).isTrue();
    }

    @Test
    public void testIsRuleSetAssociatedWithFolder_emptyAssociation()
    {
        given(runtimeNodeService.getParentAssocs(any(), any(), any())).willReturn(emptyList());

        // when
        boolean associated = ruleService.isRuleSetAssociatedWithFolder(RULE_SET_NODE, FOLDER_NODE);

        then(runtimeNodeService).should().getParentAssocs(RULE_SET_NODE, ASSOC_RULE_FOLDER, ASSOC_RULE_FOLDER);
        then(runtimeNodeService).should().hasAspect(FOLDER_NODE, ASPECT_IGNORE_INHERITED_RULES);
        then(runtimeNodeService).should().getParentAssocs(FOLDER_NODE);
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
        then(runtimeNodeService).should().hasAspect(FOLDER_NODE, ASPECT_IGNORE_INHERITED_RULES);
        then(runtimeNodeService).should().getParentAssocs(FOLDER_NODE);
        then(runtimeNodeService).shouldHaveNoMoreInteractions();
        then(nodeService).shouldHaveNoInteractions();
        assertThat(associated).isFalse();
    }

    /**
     * Check that a rule set is associated with the folder in the following case:
     * <pre>
     *     parent --[link]-> rule set <-[owned]-- owningFolder
     *     +- child
     * </pre>
     */
    @Test
    public void testIsRuleSetAssociatedWithFolder_inheritedLinkedAssociation()
    {
        // The rule is owned by one node.
        NodeRef owningFolder = new NodeRef("owning://node/");
        // The rule is linked to by the parent node.
        NodeRef parent = new NodeRef("parent://node/");
        List<ChildAssociationRef> ruleAssociations = List.of(createAssociation(owningFolder, RULE_SET_NODE), createAssociation(parent, RULE_SET_NODE));
        given(runtimeNodeService.getParentAssocs(RULE_SET_NODE, ASSOC_RULE_FOLDER, ASSOC_RULE_FOLDER)).willReturn(ruleAssociations);
        // The parent and the child both supply rule sets.
        given(runtimeNodeService.getParentAssocs(FOLDER_NODE)).willReturn(List.of(createAssociation(parent, FOLDER_NODE)));

        // when
        boolean associated = ruleService.isRuleSetAssociatedWithFolder(RULE_SET_NODE, FOLDER_NODE);

        then(runtimeNodeService).should().getParentAssocs(RULE_SET_NODE, ASSOC_RULE_FOLDER, ASSOC_RULE_FOLDER);
        then(runtimeNodeService).should().hasAspect(FOLDER_NODE, ASPECT_IGNORE_INHERITED_RULES);
        then(runtimeNodeService).should().getParentAssocs(FOLDER_NODE);
        then(runtimeNodeService).should().hasAspect(parent, ASPECT_IGNORE_INHERITED_RULES);
        then(runtimeNodeService).should().getParentAssocs(parent);
        then(runtimeNodeService).shouldHaveNoMoreInteractions();
        then(nodeService).shouldHaveNoInteractions();
        assertThat(associated).isTrue();
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
        given(runtimeNodeService.getParentAssocs(any())).willReturn(emptyList());

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

    /** Check that a straight chain of nodes is traversed correctly. */
    @Test
    public void testGetNodesSupplyingRuleSets_chain()
    {
        Map<String, NodeRef> nodes = createParentChildHierarchy("A,B", "B,C", "C,D", "D,E");

        List<NodeRef> actual = ruleService.getNodesSupplyingRuleSets(nodes.get("E"));

        Map<NodeRef, String> invertedMap = MapUtils.invertMap(nodes);
        String nodeNames = actual.stream().map(invertedMap::get).collect(joining(","));

        assertEquals("A,B,C,D,E", nodeNames);
    }

    /** Check that ordered parents are returned in the correct order. */
    @Test
    public void testGetNodesSupplyingRuleSets_multipleParents()
    {
        Map<String, NodeRef> nodes = createParentChildHierarchy("A,E", "B,E", "C,E", "D,E");

        List<NodeRef> actual = ruleService.getNodesSupplyingRuleSets(nodes.get("E"));

        Map<NodeRef, String> invertedMap = MapUtils.invertMap(nodes);
        String nodeNames = actual.stream().map(invertedMap::get).collect(joining(","));

        assertEquals("A,B,C,D,E", nodeNames);
    }

    /** Check that the ASPECT_IGNORE_INHERITED_RULES aspect breaks the chain. */
    @Test
    public void testGetNodesSupplyingRuleSets_brokenChain()
    {
        Map<String, NodeRef> nodes = createParentChildHierarchy("A,B", "B,C", "C,D", "D,E");
        given(runtimeNodeService.hasAspect(nodes.get("C"), ASPECT_IGNORE_INHERITED_RULES)).willReturn(true);

        List<NodeRef> actual = ruleService.getNodesSupplyingRuleSets(nodes.get("E"));

        Map<NodeRef, String> invertedMap = MapUtils.invertMap(nodes);
        String nodeNames = actual.stream().map(invertedMap::get).collect(joining(","));

        assertEquals("C,D,E", nodeNames);
    }

    /** Check that the user group hierarchy is not traversed. */
    @Test
    public void testGetNodesSupplyingRuleSets_userGroupHierarchy()
    {
        Map<String, NodeRef> nodes = createParentChildHierarchy("A,B", "B,C", "C,D", "D,E");
        // Replace the B,C association with a user group membership association.
        ChildAssociationRef memberAssoc = new ChildAssociationRef(ASSOC_MEMBER, nodes.get("B"), TYPE_FOLDER, nodes.get("C"));
        given(runtimeNodeService.getParentAssocs(nodes.get("C"))).willReturn(List.of(memberAssoc));

        List<NodeRef> actual = ruleService.getNodesSupplyingRuleSets(nodes.get("E"));

        Map<NodeRef, String> invertedMap = MapUtils.invertMap(nodes);
        String nodeNames = actual.stream().map(invertedMap::get).collect(joining(","));

        assertEquals("C,D,E", nodeNames);
    }

    /** Check that a cycle doesn't cause a problem. */
    @Test
    public void testGetNodesSupplyingRuleSets_infiniteCycle()
    {
        Map<String, NodeRef> nodes = createParentChildHierarchy("A,B", "B,C", "C,A");

        List<NodeRef> actual = ruleService.getNodesSupplyingRuleSets(nodes.get("C"));

        Map<NodeRef, String> invertedMap = MapUtils.invertMap(nodes);
        String nodeNames = actual.stream().map(invertedMap::get).collect(joining(","));

        assertEquals("A,B,C", nodeNames);
    }

    /** Check that a diamond of nodes is traversed correctly. */
    @Test
    public void testGetNodesSupplyingRuleSets_diamond()
    {
        Map<String, NodeRef> nodes = createParentChildHierarchy("A,B", "A,C", "B,D", "C,D");

        List<NodeRef> actual = ruleService.getNodesSupplyingRuleSets(nodes.get("D"));

        Map<NodeRef, String> invertedMap = MapUtils.invertMap(nodes);
        String nodeNames = actual.stream().map(invertedMap::get).collect(joining(","));

        assertEquals("A,B,C,D", nodeNames);
    }

    /**
     * Check that hierarchy of nodes is traversed correctly. Parent-child associations are created in alphabetical order.
     * <pre>
     *     A
     *    /|\
     *   B C D
     *   | |\|
     *   E | F
     *    \|/
     *     G
     * </pre>
     */
    @Test
    public void testGetNodesSupplyingRuleSets_alphabetical()
    {
        Map<String, NodeRef> nodes = createParentChildHierarchy("A,B", "A,C", "A,D", "B,E", "C,F", "C,G", "D,F", "E,G", "F,G");

        List<NodeRef> actual = ruleService.getNodesSupplyingRuleSets(nodes.get("G"));

        Map<NodeRef, String> invertedMap = MapUtils.invertMap(nodes);
        String nodeNames = actual.stream().map(invertedMap::get).collect(joining(","));

        assertEquals("A,C,B,E,D,F,G", nodeNames);
    }

    /**
     * Check that hierarchy of nodes is traversed correctly. Parent-child associations are created in reverse alphabetical order.
     * <pre>
     *     A
     *    /|\
     *   B C D
     *   | |\|
     *   E | F
     *    \|/
     *     G
     * </pre>
     */
    @Test
    public void testGetNodesSupplyingRuleSets_reversedAssociationOrder()
    {
        Map<String, NodeRef> nodes = createParentChildHierarchy("F,G", "E,G", "D,F", "C,G", "C,F", "B,E", "A,D", "A,C", "A,B");

        List<NodeRef> actual = ruleService.getNodesSupplyingRuleSets(nodes.get("G"));

        Map<NodeRef, String> invertedMap = MapUtils.invertMap(nodes);
        String nodeNames = actual.stream().map(invertedMap::get).collect(joining(","));

        assertEquals("A,D,C,F,B,E,G", nodeNames);
    }

    /**
     * Create a mock hierarchy of nodes using the supplied parent child associations.
     *
     * @param parentChildAssociations A list of strings of the form "Parent,Child". Associations will be created in this order.
     * @return A map from the node name to the new NodeRef object.
     */
    private Map<String, NodeRef> createParentChildHierarchy(String... parentChildAssociations)
    {
        // Find all the node names mentioned.
        Set<String> nodeNames = new HashSet<>();
        List.of(parentChildAssociations).forEach(parentChildAssociation -> {
            String[] parentChildPair = parentChildAssociation.split(",");
            nodeNames.addAll(List.of(parentChildPair));
        });
        // Create the NodeRefs.
        Map<String, NodeRef> nodeRefMap = nodeNames.stream().collect(
                Collectors.toMap(nodeName -> nodeName, nodeName -> new NodeRef("node://" + nodeName + "/")));
        // Mock the associations.
        nodeNames.forEach(nodeName -> {
            NodeRef nodeRef = nodeRefMap.get(nodeName);
            List<ChildAssociationRef> parentAssocs = List.of(parentChildAssociations)
                                                         .stream()
                                                         .filter(assoc -> assoc.endsWith(nodeName))
                                                         .map(assoc -> assoc.split(",")[0])
                                                         .map(nodeRefMap::get)
                                                         .map(parentRef -> new ChildAssociationRef(ASSOC_CONTAINS, parentRef, TYPE_FOLDER, nodeRef))
                                                         .collect(toList());
            given(runtimeNodeService.getParentAssocs(nodeRef)).willReturn(parentAssocs);
        });
        return nodeRefMap;
    }

    /** Check that getFoldersInheritingRuleSet returns a child folder. */
    @Test
    public void testGetFoldersInheritingRuleSet()
    {
        NodeRef parent = new NodeRef("parent://node/");
        NodeRef ruleSetNode = new NodeRef("rule://set/");
        ChildAssociationRef ruleSetAssociation = mock(ChildAssociationRef.class);
        given(runtimeNodeService.getParentAssocs(ruleSetNode)).willReturn(List.of(ruleSetAssociation));
        given(ruleSetAssociation.getParentRef()).willReturn(parent);
        NodeRef child = new NodeRef("child://node/");
        ChildAssociationRef childAssocMock = mock(ChildAssociationRef.class);
        given(runtimeNodeService.getChildAssocs(parent, ASSOC_CONTAINS, MATCH_ALL)).willReturn(List.of(childAssocMock));
        given(childAssocMock.getChildRef()).willReturn(child);
        given(runtimeNodeService.getType(child)).willReturn(TYPE_FOLDER);

        List<NodeRef> actual = ruleService.getFoldersInheritingRuleSet(ruleSetNode, 100);

        assertEquals("Unexpected list of inheriting folders.", List.of(child), actual);
    }

    /** Check that getFoldersInheritingRuleSet omits a child folder if IGNORE_INHERITED_RULES is applied. */
    @Test
    public void testGetFoldersInheritingRuleSet_ignoreInheritedRules()
    {
        NodeRef parent = new NodeRef("parent://node/");
        NodeRef ruleSetNode = new NodeRef("rule://set/");
        ChildAssociationRef ruleSetAssociation = mock(ChildAssociationRef.class);
        given(runtimeNodeService.getParentAssocs(ruleSetNode)).willReturn(List.of(ruleSetAssociation));
        given(ruleSetAssociation.getParentRef()).willReturn(parent);
        NodeRef child = new NodeRef("child://node/");
        ChildAssociationRef childAssocMock = mock(ChildAssociationRef.class);
        given(runtimeNodeService.getChildAssocs(parent, ASSOC_CONTAINS, MATCH_ALL)).willReturn(List.of(childAssocMock));
        given(childAssocMock.getChildRef()).willReturn(child);
        given(runtimeNodeService.getType(child)).willReturn(TYPE_FOLDER);
        given(runtimeNodeService.hasAspect(child, ASPECT_IGNORE_INHERITED_RULES)).willReturn(true);

        List<NodeRef> actual = ruleService.getFoldersInheritingRuleSet(ruleSetNode, 100);

        assertEquals("Unexpected list of inheriting folders.", emptyList(), actual);
    }

    /** Check that getFoldersInheritingRuleSet only returns at most the requested number of folders. */
    @Test
    public void testGetFoldersExceedsLimit()
    {
        NodeRef root = new NodeRef("root://node/");
        NodeRef ruleSetNode = new NodeRef("rule://set/");
        ChildAssociationRef ruleSetAssociation = mock(ChildAssociationRef.class);
        given(runtimeNodeService.getParentAssocs(ruleSetNode)).willReturn(List.of(ruleSetAssociation));
        given(ruleSetAssociation.getParentRef()).willReturn(root);
        // Create a chain of ancestors starting from the root that is 10 folders deep.
        List<NodeRef> nodeChain = new ArrayList<>();
        nodeChain.add(root);
        IntStream.range(0, 10).forEach(index -> {
            NodeRef parent = nodeChain.get(nodeChain.size() - 1);
            NodeRef child = new NodeRef("chain://node/" + index);
            nodeChain.add(child);
            ChildAssociationRef childAssocMock = mock(ChildAssociationRef.class);
            given(runtimeNodeService.getChildAssocs(parent, ASSOC_CONTAINS, MATCH_ALL)).willReturn(List.of(childAssocMock));
            given(childAssocMock.getChildRef()).willReturn(child);
            given(runtimeNodeService.getType(child)).willReturn(TYPE_FOLDER);
        });

        // Request at most 9 folders inheriting the rule.
        List<NodeRef> actual = ruleService.getFoldersInheritingRuleSet(ruleSetNode, 9);

        // Check we don't get the root node or the final descendant folder.
        assertEquals("Unexpected list of inheriting folders.", nodeChain.subList(1, 10), actual);
    }

    /** Check that getFoldersInheritingRuleSet doesn't include documents. */
    @Test
    public void testGetFoldersInheritingRuleSet_ignoreFiles()
    {
        NodeRef parent = new NodeRef("parent://node/");
        NodeRef ruleSetNode = new NodeRef("rule://set/");
        ChildAssociationRef ruleSetAssociation = mock(ChildAssociationRef.class);
        given(runtimeNodeService.getParentAssocs(ruleSetNode)).willReturn(List.of(ruleSetAssociation));
        given(ruleSetAssociation.getParentRef()).willReturn(parent);
        NodeRef childDocument = new NodeRef("child://document/");
        ChildAssociationRef childAssocMock = mock(ChildAssociationRef.class);
        given(runtimeNodeService.getChildAssocs(parent, ASSOC_CONTAINS, MATCH_ALL)).willReturn(List.of(childAssocMock));
        given(childAssocMock.getChildRef()).willReturn(childDocument);
        given(runtimeNodeService.getType(childDocument)).willReturn(TYPE_CONTENT);

        List<NodeRef> actual = ruleService.getFoldersInheritingRuleSet(ruleSetNode, 100);

        assertEquals("Unexpected list of inheriting folders.", emptyList(), actual);
    }

    /**
     * Check that getFoldersInheritingRuleSet does not include folders that the user doesn't have access to.
     * <p>
     * This test uses a chain of three folders:
     * <pre>
     *     grandparent - owns the rule set
     *     parent - user does not have read access
     *     child - user _does_ have read access
     * </pre>
     */
    @Test
    public void testGetFoldersInheritingRuleSet_omitFoldersWithoutReadPermission()
    {
        NodeRef grandparent = new NodeRef("grandparent://node/");
        NodeRef ruleSetNode = new NodeRef("rule://set/");
        ChildAssociationRef ruleSetAssociation = mock(ChildAssociationRef.class);
        given(runtimeNodeService.getParentAssocs(ruleSetNode)).willReturn(List.of(ruleSetAssociation));
        given(ruleSetAssociation.getParentRef()).willReturn(grandparent);
        NodeRef parent = new NodeRef("parent://node/");
        ChildAssociationRef parentAssocMock = mock(ChildAssociationRef.class);
        given(runtimeNodeService.getChildAssocs(grandparent, ASSOC_CONTAINS, MATCH_ALL)).willReturn(List.of(parentAssocMock));
        given(parentAssocMock.getChildRef()).willReturn(parent);
        given(runtimeNodeService.getType(parent)).willReturn(TYPE_FOLDER);
        NodeRef child = new NodeRef("child://node/");
        ChildAssociationRef childAssocMock = mock(ChildAssociationRef.class);
        given(runtimeNodeService.getChildAssocs(grandparent, ASSOC_CONTAINS, MATCH_ALL)).willReturn(List.of(childAssocMock));
        given(childAssocMock.getChildRef()).willReturn(child);
        given(runtimeNodeService.getType(child)).willReturn(TYPE_FOLDER);

        // The current user doesn't have permission to view the parent node.
        given(permissionService.hasReadPermission(parent)).willReturn(DENIED);

        List<NodeRef> actual = ruleService.getFoldersInheritingRuleSet(ruleSetNode, 100);

        assertEquals("Unexpected list of inheriting folders.", List.of(child), actual);
    }

    /** Check that a linked folder can be retrieved from a rule set node. */
    @Test
    public void testGetFoldersLinkingToRuleSet()
    {
        NodeRef ruleSetNode = new NodeRef("rule://set/");
        NodeRef owningFolder = new NodeRef("owning://folder/");
        ChildAssociationRef owningAssocMock = mock(ChildAssociationRef.class);
        given(owningAssocMock.getParentRef()).willReturn(owningFolder);
        given(nodeService.getPrimaryParent(ruleSetNode)).willReturn(owningAssocMock);
        // Simulate a folder linking to the rule set.
        NodeRef linkingFolder = new NodeRef("linking://folder/");
        ChildAssociationRef linkingAssocMock = mock(ChildAssociationRef.class);
        given(linkingAssocMock.getParentRef()).willReturn(linkingFolder);
        given(nodeService.getParentAssocs(ruleSetNode)).willReturn(List.of(owningAssocMock, linkingAssocMock));

        List<NodeRef> linkingFolders = ruleService.getFoldersLinkingToRuleSet(ruleSetNode, 100);

        assertEquals("Unexpected list of linking folders.", List.of(linkingFolder), linkingFolders);
    }

    /** Check that permissions affect which linked folders are returned to the user. */
    @Test
    public void testGetFoldersLinkingToRuleSet_respectsPermissions()
    {
        NodeRef ruleSetNode = new NodeRef("rule://set/");
        NodeRef owningFolder = new NodeRef("owning://folder/");
        ChildAssociationRef owningAssocMock = mock(ChildAssociationRef.class);
        given(owningAssocMock.getParentRef()).willReturn(owningFolder);
        given(nodeService.getPrimaryParent(ruleSetNode)).willReturn(owningAssocMock);
        // Simulate a folder linking to the rule set.
        NodeRef linkingFolder = new NodeRef("linking://folder/");
        ChildAssociationRef linkingAssocMock = mock(ChildAssociationRef.class);
        given(linkingAssocMock.getParentRef()).willReturn(linkingFolder);
        given(nodeService.getParentAssocs(ruleSetNode)).willReturn(List.of(owningAssocMock, linkingAssocMock));
        // The currect user does not have permission to view the folder.
        given(permissionService.hasReadPermission(linkingFolder)).willReturn(DENIED);

        List<NodeRef> linkingFolders = ruleService.getFoldersLinkingToRuleSet(ruleSetNode, 100);

        assertEquals("Unexpected list of linking folders.", emptyList(), linkingFolders);
    }
}
