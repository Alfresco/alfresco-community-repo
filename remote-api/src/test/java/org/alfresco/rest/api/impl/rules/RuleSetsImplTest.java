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

import static org.alfresco.rest.api.impl.rules.RuleSetLoader.RULE_IDS;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;
import org.alfresco.repo.rule.RuleModel;
import org.alfresco.repo.rule.RuntimeRuleService;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.rest.api.model.rules.RuleSet;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/** Unit tests for {@link RuleSetsImpl}. */
@Experimental
@RunWith (MockitoJUnitRunner.class)
public class RuleSetsImplTest extends TestCase
{
    private static final String FOLDER_ID = "dummy-folder-id";
    private static final String LINK_TO_NODE_ID = "dummy-link-to-node-id";
    private static final String RULE_SET_ID = "dummy-rule-set-id";
    private static final NodeRef FOLDER_NODE = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, FOLDER_ID);
    private static final NodeRef LINK_TO_NODE = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, LINK_TO_NODE_ID);
    private static final NodeRef RULE_SET_NODE = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, RULE_SET_ID);
    private static final Paging PAGING = Paging.DEFAULT;
    private static final List<String> INCLUDES = List.of("dummy-includes");

    @InjectMocks
    private RuleSetsImpl ruleSets;
    @Mock
    private RuleSetLoader ruleSetLoaderMock;
    @Mock
    private NodeValidator nodeValidatorMock;
    @Mock
    private NodeService nodeServiceMock;
    @Mock
    private RuleService ruleServiceMock;
    @Mock
    private RuntimeRuleService runtimeRuleServiceMock;
    @Mock
    private RuleSet ruleSetMock;
    @Mock
    private ChildAssociationRef assocRef;

    @Before
    @Override
    public void setUp()
    {
        MockitoAnnotations.openMocks(this);

        given(nodeValidatorMock.validateFolderNode(eq(LINK_TO_NODE_ID), anyBoolean())).willReturn(LINK_TO_NODE);
        given(nodeValidatorMock.validateRuleSetNode(LINK_TO_NODE_ID,false)).willReturn(LINK_TO_NODE);
        given(nodeValidatorMock.validateFolderNode(eq(FOLDER_ID), anyBoolean())).willReturn(FOLDER_NODE);
        given(nodeValidatorMock.validateRuleSetNode(RULE_SET_ID, FOLDER_NODE)).willReturn(RULE_SET_NODE);

        given(ruleServiceMock.getRuleSetNode(FOLDER_NODE)).willReturn(RULE_SET_NODE);
        given(ruleServiceMock.getNodesSupplyingRuleSets(FOLDER_NODE)).willReturn(List.of(FOLDER_NODE));

        given(ruleSetLoaderMock.loadRuleSet(RULE_SET_NODE, FOLDER_NODE, INCLUDES)).willReturn(ruleSetMock);
    }

    @Test
    public void testGetRuleSets()
    {
        // Call the method under test.
        CollectionWithPagingInfo<RuleSet> actual = ruleSets.getRuleSets(FOLDER_ID, INCLUDES, PAGING);

        then(nodeValidatorMock).should().validateFolderNode(FOLDER_ID, false);
        then(nodeValidatorMock).shouldHaveNoMoreInteractions();

        then(ruleServiceMock).should().getNodesSupplyingRuleSets(FOLDER_NODE);
        then(ruleServiceMock).should().getRuleSetNode(FOLDER_NODE);
        then(ruleServiceMock).shouldHaveNoMoreInteractions();

        Collection<RuleSet> expected = List.of(ruleSetMock);
        assertEquals(expected, actual.getCollection());
        assertEquals(PAGING, actual.getPaging());
    }

    @Test
    public void testGetZeroRuleSets()
    {
        // Simulate no rule sets for the folder.
        given(ruleServiceMock.getRuleSetNode(FOLDER_NODE)).willReturn(null);

        // Call the method under test.
        CollectionWithPagingInfo<RuleSet> actual = ruleSets.getRuleSets(FOLDER_ID, INCLUDES, PAGING);

        then(nodeValidatorMock).should().validateFolderNode(FOLDER_ID, false);
        then(nodeValidatorMock).shouldHaveNoMoreInteractions();

        then(ruleServiceMock).should().getNodesSupplyingRuleSets(FOLDER_NODE);
        then(ruleServiceMock).should().getRuleSetNode(FOLDER_NODE);
        then(ruleServiceMock).shouldHaveNoMoreInteractions();

        assertEquals(emptyList(), actual.getCollection());
        assertEquals(PAGING, actual.getPaging());
    }

    @Test
    public void testOnlyGetPermittedRuleSets()
    {
        // Simulate a private folder with a rule set that the current user can't access.
        NodeRef privateFolder = new NodeRef("private://folder/");
        NodeRef privateRuleSetNode = new NodeRef("private://rule/set/node/");
        given(ruleServiceMock.getRuleSetNode(privateFolder)).willReturn(privateRuleSetNode);
        given(ruleServiceMock.getNodesSupplyingRuleSets(FOLDER_NODE)).willReturn(List.of(FOLDER_NODE, privateFolder));
        given(ruleSetLoaderMock.loadRuleSet(eq(privateRuleSetNode), any(NodeRef.class), any(List.class)))
                .willThrow(new AccessDeniedException("Cannot access private rule set."));

        // Call the method under test.
        CollectionWithPagingInfo<RuleSet> actual = ruleSets.getRuleSets(FOLDER_ID, INCLUDES, PAGING);

        then(nodeValidatorMock).should().validateFolderNode(FOLDER_ID, false);
        then(nodeValidatorMock).shouldHaveNoMoreInteractions();

        then(ruleServiceMock).should().getNodesSupplyingRuleSets(FOLDER_NODE);
        then(ruleServiceMock).should().getRuleSetNode(FOLDER_NODE);
        then(ruleServiceMock).should().getRuleSetNode(privateFolder);
        then(ruleServiceMock).shouldHaveNoMoreInteractions();

        // Check we only get the accessible rule set back.
        Collection<RuleSet> expected = List.of(ruleSetMock);
        assertEquals(expected, actual.getCollection());
        assertEquals(PAGING, actual.getPaging());
    }

    /** Check that a folder with a parent and grandparent can inherit rule sets from the grandparent, even if the parent has no rules. */
    @Test
    public void testGetInheritedRuleSets()
    {
        // Simulate a parent node without a rule set.
        NodeRef parentNode = new NodeRef("parent://node/");
        // Simulate a grandparent node providing a rule set.
        NodeRef grandparentNode = new NodeRef("grandparent://node/");
        RuleSet grandparentRuleSet = mock(RuleSet.class);
        NodeRef grandparentRuleSetNode = new NodeRef("grandparent://rule-set/");
        given(ruleServiceMock.getRuleSetNode(grandparentNode)).willReturn(grandparentRuleSetNode);
        given(ruleSetLoaderMock.loadRuleSet(grandparentRuleSetNode, FOLDER_NODE, INCLUDES)).willReturn(grandparentRuleSet);
        // These should be returned with the highest in hierarchy first.
        given(ruleServiceMock.getNodesSupplyingRuleSets(FOLDER_NODE)).willReturn(List.of(grandparentNode, parentNode, FOLDER_NODE));

        // Call the method under test.
        CollectionWithPagingInfo<RuleSet> actual = ruleSets.getRuleSets(FOLDER_ID, INCLUDES, PAGING);

        then(nodeValidatorMock).should().validateFolderNode(FOLDER_ID, false);
        then(nodeValidatorMock).shouldHaveNoMoreInteractions();

        then(ruleServiceMock).should().getNodesSupplyingRuleSets(FOLDER_NODE);
        then(ruleServiceMock).should().getRuleSetNode(grandparentNode);
        then(ruleServiceMock).should().getRuleSetNode(parentNode);
        then(ruleServiceMock).should().getRuleSetNode(FOLDER_NODE);
        then(ruleServiceMock).shouldHaveNoMoreInteractions();

        Collection<RuleSet> expected = List.of(grandparentRuleSet, ruleSetMock);
        assertEquals(expected, actual.getCollection());
        assertEquals(PAGING, actual.getPaging());
    }

    /** When getting rule sets then only the first instance of each rule set should be included (ancestor first). */
    @Test
    public void testGetDuplicateRuleSets()
    {
        // Simulate a grandparent, parent and child with the grandparent linking to the child's rule set.
        NodeRef grandparentNode = new NodeRef("grandparent://node/");
        given(ruleServiceMock.getRuleSetNode(grandparentNode)).willReturn(RULE_SET_NODE);
        NodeRef parentNode = new NodeRef("parent://node/");
        RuleSet parentRuleSet = mock(RuleSet.class);
        NodeRef parentRuleSetNode = new NodeRef("parent://rule-set/");
        given(ruleServiceMock.getRuleSetNode(parentNode)).willReturn(parentRuleSetNode);
        given(ruleSetLoaderMock.loadRuleSet(parentRuleSetNode, FOLDER_NODE, INCLUDES)).willReturn(parentRuleSet);
        // These should be returned with the highest in hierarchy first.
        given(ruleServiceMock.getNodesSupplyingRuleSets(FOLDER_NODE)).willReturn(List.of(grandparentNode, parentNode, FOLDER_NODE));

        // Call the method under test.
        CollectionWithPagingInfo<RuleSet> actual = ruleSets.getRuleSets(FOLDER_ID, INCLUDES, PAGING);

        // The grandparent's linked rule set should be first and only appear once.
        Collection<RuleSet> expected = List.of(ruleSetMock, parentRuleSet);
        assertEquals(expected, actual.getCollection());
        assertEquals(PAGING, actual.getPaging());
    }

    @Test
    public void testGetRuleSetById()
    {
        // Call the method under test.
        RuleSet actual = ruleSets.getRuleSetById(FOLDER_ID, RULE_SET_ID, INCLUDES);

        then(nodeValidatorMock).should().validateFolderNode(FOLDER_ID, false);
        then(nodeValidatorMock).should().validateRuleSetNode(RULE_SET_ID, FOLDER_NODE);
        then(nodeValidatorMock).shouldHaveNoMoreInteractions();

        assertEquals(ruleSetMock, actual);
    }

    @Test
    public void testLinkToFolderRuleSet()
    {
        NodeRef childNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "dummy-child-id");

        given(ruleServiceMock.hasRules(any(NodeRef.class))).willReturn(true, false);
        given(runtimeRuleServiceMock.getSavedRuleFolderAssoc(any(NodeRef.class))).willReturn(assocRef);
        given(assocRef.getChildRef()).willReturn(childNodeRef);

        //when
        String actual = ruleSets.linkToRuleSet(FOLDER_ID,LINK_TO_NODE_ID).getId();

        then(ruleServiceMock).should().hasRules(LINK_TO_NODE);
        then(ruleServiceMock).should().hasRules(FOLDER_NODE);
        then(runtimeRuleServiceMock).should().getSavedRuleFolderAssoc(LINK_TO_NODE);
        then(runtimeRuleServiceMock).shouldHaveNoMoreInteractions();
        then(nodeServiceMock).should().addChild(FOLDER_NODE, childNodeRef, RuleModel.ASSOC_RULE_FOLDER, RuleModel.ASSOC_RULE_FOLDER);
        then(nodeServiceMock).shouldHaveNoMoreInteractions();

        assertEquals(childNodeRef.getId(),actual);
    }

    @Test
    public void testLinkToRuleSet()
    {
        NodeRef childNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "dummy-child-id");

        given(nodeValidatorMock.isRuleSetNode(any())).willReturn(true);
        given(ruleServiceMock.hasRules(any(NodeRef.class))).willReturn(true, false);
        given(runtimeRuleServiceMock.getSavedRuleFolderAssoc(any(NodeRef.class))).willReturn(assocRef);
        given(assocRef.getChildRef()).willReturn(childNodeRef);

        //when
        String actual = ruleSets.linkToRuleSet(FOLDER_ID,LINK_TO_NODE_ID).getId();

        then(nodeValidatorMock).should().validateFolderNode(FOLDER_ID,true);
        then(nodeValidatorMock).should().isRuleSetNode(LINK_TO_NODE_ID);
        then(nodeValidatorMock).should().validateRuleSetNode(LINK_TO_NODE_ID,false);
        then(nodeValidatorMock).shouldHaveNoMoreInteractions();
        then(ruleServiceMock).should().hasRules(LINK_TO_NODE);
        then(ruleServiceMock).should().hasRules(FOLDER_NODE);
        then(runtimeRuleServiceMock).should().getSavedRuleFolderAssoc(LINK_TO_NODE);
        then(runtimeRuleServiceMock).shouldHaveNoMoreInteractions();
        then(nodeServiceMock).should().addChild(FOLDER_NODE, childNodeRef, RuleModel.ASSOC_RULE_FOLDER, RuleModel.ASSOC_RULE_FOLDER);
        then(nodeServiceMock).shouldHaveNoMoreInteractions();

        assertEquals(childNodeRef.getId(),actual);
    }

    @Test
    public void testLinkToRuleSet_targetFolderHasNoRules()
    {
        given(ruleServiceMock.hasRules(LINK_TO_NODE)).willReturn(false);

        //when
        assertThatExceptionOfType(InvalidArgumentException.class).isThrownBy(
                () -> ruleSets.linkToRuleSet(FOLDER_ID, LINK_TO_NODE_ID)
        );

        then(nodeServiceMock).shouldHaveNoMoreInteractions();
        then(ruleServiceMock).should().hasRules(LINK_TO_NODE);
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
        then(runtimeRuleServiceMock).shouldHaveNoInteractions();
    }

    @Test
    public void testLinkToRuleSet_folderShouldntHavePreExistingRules()
    {
        given(ruleServiceMock.hasRules(any(NodeRef.class))).willReturn(true, true);

        //when
        assertThatExceptionOfType(InvalidArgumentException.class).isThrownBy(
                () -> ruleSets.linkToRuleSet(FOLDER_ID, LINK_TO_NODE_ID));

        then(nodeServiceMock).shouldHaveNoMoreInteractions();
        then(ruleServiceMock).should().hasRules(LINK_TO_NODE);
        then(ruleServiceMock).should().hasRules(FOLDER_NODE);
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
        then(runtimeRuleServiceMock).shouldHaveNoInteractions();
    }

    @Test
    public void testUnlinkRuleSet()
    {
        given(ruleServiceMock.isLinkedToRuleNode(FOLDER_NODE)).willReturn(true);

        //when
        ruleSets.unlinkRuleSet(FOLDER_ID,RULE_SET_ID);

        then(nodeValidatorMock).should().validateFolderNode(FOLDER_ID,true);
        then(nodeValidatorMock).should().validateRuleSetNode(RULE_SET_ID,FOLDER_NODE);
        then(nodeValidatorMock).shouldHaveNoMoreInteractions();
        then(ruleServiceMock).should().isLinkedToRuleNode(FOLDER_NODE);
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
        then(nodeServiceMock).should().removeAspect(FOLDER_NODE,RuleModel.ASPECT_RULES);
        then(nodeServiceMock).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testUnlinkRuleSet_folderIsNotLinkedToRuleSet()
    {
        given(ruleServiceMock.isLinkedToRuleNode(FOLDER_NODE)).willReturn(false);

        //when
        assertThatExceptionOfType(InvalidArgumentException.class).isThrownBy(
                () -> ruleSets.unlinkRuleSet(FOLDER_ID,RULE_SET_ID));

        then(nodeValidatorMock).should().validateFolderNode(FOLDER_ID,true);
        then(nodeValidatorMock).should().validateRuleSetNode(RULE_SET_ID,FOLDER_NODE);
        then(nodeValidatorMock).shouldHaveNoMoreInteractions();
        then(ruleServiceMock).should().isLinkedToRuleNode(FOLDER_NODE);
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
        then(nodeServiceMock).shouldHaveNoInteractions();
    }

    @Test
    public void testUpdateRuleSet()
    {
        given(ruleSetMock.getId()).willReturn(RULE_SET_ID);
        given(nodeValidatorMock.validateFolderNode(FOLDER_ID, false)).willReturn(FOLDER_NODE);
        given(nodeValidatorMock.validateRuleSetNode(RULE_SET_ID, FOLDER_NODE)).willReturn(RULE_SET_NODE);
        RuleSet ruleSetResponse = mock(RuleSet.class);
        given(ruleSetLoaderMock.loadRuleSet(RULE_SET_NODE, FOLDER_NODE, emptyList())).willReturn(ruleSetResponse);

        //when
        RuleSet ruleSet = ruleSets.updateRuleSet(FOLDER_ID, ruleSetMock, emptyList());

        assertEquals("Unexpected rule set returned.", ruleSetResponse, ruleSet);

        then(nodeValidatorMock).should().validateFolderNode(FOLDER_ID, false);
        then(nodeValidatorMock).should().validateRuleSetNode(RULE_SET_ID, FOLDER_NODE);
        then(ruleSetLoaderMock).should().loadRuleSet(RULE_SET_NODE, FOLDER_NODE, emptyList());
    }

    /** Simulate rules being reordered from [RuleA, RuleB] to [RuleB, RuleA]. */
    @Test
    public void testUpdateRuleSet_reorderRules()
    {
        List<String> dbOrder = List.of("RuleA", "RuleB");
        List<String> newOrder = List.of("RuleB", "RuleA");
        List<String> includes = List.of(RULE_IDS);

        RuleSet dbRuleSet = mock(RuleSet.class);
        RuleSet requestRuleSet = mock(RuleSet.class);
        given(requestRuleSet.getId()).willReturn(RULE_SET_ID);
        given(requestRuleSet.getRuleIds()).willReturn(newOrder);

        given(ruleSetLoaderMock.loadRuleSet(RULE_SET_NODE, FOLDER_NODE, includes)).willReturn(dbRuleSet);
        given(ruleSetLoaderMock.loadRuleIds(FOLDER_NODE)).willReturn(dbOrder);
        given(nodeValidatorMock.validateFolderNode(FOLDER_ID, false)).willReturn(FOLDER_NODE);
        given(nodeValidatorMock.validateRuleSetNode(RULE_SET_ID, FOLDER_NODE)).willReturn(RULE_SET_NODE);

        //when
        RuleSet ruleSet = ruleSets.updateRuleSet(FOLDER_ID, requestRuleSet, includes);

        assertEquals("Unexpected rule set returned.", dbRuleSet, ruleSet);

        then(nodeValidatorMock).should().validateFolderNode(FOLDER_ID, false);
        then(nodeValidatorMock).should().validateRuleSetNode(RULE_SET_ID, FOLDER_NODE);
        then(ruleSetLoaderMock).should().loadRuleSet(RULE_SET_NODE, FOLDER_NODE, includes);
        then(dbRuleSet).should().setRuleIds(newOrder);
    }

    /** Check that we can't remove a rule by updating the rule set. */
    @Test
    public void testUpdateRuleSet_tryToChangeSetOfRuleIds()
    {
        List<String> dbOrder = List.of("RuleA", "RuleB");
        List<String> newOrder = List.of("RuleA");
        List<String> includes = List.of(RULE_IDS);

        RuleSet dbRuleSet = mock(RuleSet.class);
        RuleSet requestRuleSet = mock(RuleSet.class);
        given(requestRuleSet.getId()).willReturn(RULE_SET_ID);
        given(requestRuleSet.getRuleIds()).willReturn(newOrder);

        given(ruleSetLoaderMock.loadRuleSet(RULE_SET_NODE, FOLDER_NODE, includes)).willReturn(dbRuleSet);
        given(ruleSetLoaderMock.loadRuleIds(FOLDER_NODE)).willReturn(dbOrder);
        given(nodeValidatorMock.validateFolderNode(FOLDER_ID, false)).willReturn(FOLDER_NODE);
        given(nodeValidatorMock.validateRuleSetNode(RULE_SET_ID, FOLDER_NODE)).willReturn(RULE_SET_NODE);

        //when
        assertThatExceptionOfType(InvalidArgumentException.class).isThrownBy(
                () -> ruleSets.updateRuleSet(FOLDER_ID, requestRuleSet, includes)
        );
    }

    /** Check that we can't include a rule twice in a rule set. */
    @Test
    public void testUpdateRuleSet_DuplicateRuleId()
    {
        List<String> dbOrder = List.of("RuleA", "RuleB");
        List<String> newOrder = List.of("RuleA", "RuleB", "RuleA");
        List<String> includes = List.of(RULE_IDS);

        RuleSet dbRuleSet = mock(RuleSet.class);
        RuleSet requestRuleSet = mock(RuleSet.class);
        given(requestRuleSet.getId()).willReturn(RULE_SET_ID);
        given(requestRuleSet.getRuleIds()).willReturn(newOrder);

        given(ruleSetLoaderMock.loadRuleSet(RULE_SET_NODE, FOLDER_NODE, includes)).willReturn(dbRuleSet);
        given(ruleSetLoaderMock.loadRuleIds(FOLDER_NODE)).willReturn(dbOrder);
        given(nodeValidatorMock.validateFolderNode(FOLDER_ID, false)).willReturn(FOLDER_NODE);
        given(nodeValidatorMock.validateRuleSetNode(RULE_SET_ID, FOLDER_NODE)).willReturn(RULE_SET_NODE);

        //when
        assertThatExceptionOfType(InvalidArgumentException.class).isThrownBy(
                () -> ruleSets.updateRuleSet(FOLDER_ID, requestRuleSet, includes)
                                                                            );
    }

    /** Check that we can update the rule ids without returning them. */
    @Test
    public void testUpdateRuleSet_dontIncludeRuleIds()
    {
        List<String> dbOrder = List.of("RuleA", "RuleB");
        List<String> newOrder = List.of("RuleB", "RuleA");
        List<String> includes = emptyList();

        RuleSet dbRuleSet = mock(RuleSet.class);
        RuleSet requestRuleSet = mock(RuleSet.class);
        given(requestRuleSet.getId()).willReturn(RULE_SET_ID);
        given(requestRuleSet.getRuleIds()).willReturn(newOrder);

        given(ruleSetLoaderMock.loadRuleSet(RULE_SET_NODE, FOLDER_NODE, includes)).willReturn(dbRuleSet);
        given(ruleSetLoaderMock.loadRuleIds(FOLDER_NODE)).willReturn(dbOrder);
        given(nodeValidatorMock.validateFolderNode(FOLDER_ID, false)).willReturn(FOLDER_NODE);
        given(nodeValidatorMock.validateRuleSetNode(RULE_SET_ID, FOLDER_NODE)).willReturn(RULE_SET_NODE);

        //when
        RuleSet ruleSet = ruleSets.updateRuleSet(FOLDER_ID, requestRuleSet, includes);

        // Expect the DB rule set to be returned, but no extra fields to be populated.
        assertEquals("Unexpected rule set returned.", dbRuleSet, ruleSet);
        then(dbRuleSet).shouldHaveNoInteractions();
    }
}
