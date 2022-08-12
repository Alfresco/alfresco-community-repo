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

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.rule.RuleModel;
import org.alfresco.repo.rule.RuntimeRuleService;
import org.alfresco.rest.api.model.rules.RuleSet;
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
    private static final String FOLDER_NODE_ID = "dummy-folder-node-id";
    private static final String LINK_TO_NODE_ID = "dummy-link-to-node-id";
    private static final String RULE_SET_ID = "dummy-rule-set-id";
    private static final NodeRef folderNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, FOLDER_NODE_ID);
    private static final NodeRef linkToNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, LINK_TO_NODE_ID);
    private static final NodeRef ruleSetNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, RULE_SET_ID);
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
        given(nodeValidatorMock.validateFolderNode(eq(LINK_TO_NODE_ID), anyBoolean())).willReturn(linkToNodeRef);
        given(nodeValidatorMock.validateFolderNode(eq(FOLDER_NODE_ID), anyBoolean())).willReturn(folderNodeRef);
        given(nodeValidatorMock.validateRuleSetNode(RULE_SET_ID, folderNodeRef)).willReturn(ruleSetNodeRef);

        given(ruleServiceMock.getRuleSetNode(folderNodeRef)).willReturn(ruleSetNodeRef);
        given(ruleSetLoaderMock.loadRuleSet(ruleSetNodeRef, folderNodeRef, INCLUDES)).willReturn(ruleSetMock);
    }

    @Test
    public void testGetRuleSets()
    {
        // Call the method under test.
        CollectionWithPagingInfo<RuleSet> actual = ruleSets.getRuleSets(FOLDER_NODE_ID, INCLUDES, PAGING);

        then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, false);
        then(nodeValidatorMock).shouldHaveNoMoreInteractions();

        then(ruleServiceMock).should().getRuleSetNode(folderNodeRef);
        then(ruleServiceMock).shouldHaveNoMoreInteractions();

        Collection<RuleSet> expected = List.of(ruleSetMock);
        assertEquals(expected, actual.getCollection());
        assertEquals(PAGING, actual.getPaging());
    }

    @Test
    public void testGetZeroRuleSets()
    {
        // Simulate no rule sets for the folder.
        given(ruleServiceMock.getRuleSetNode(folderNodeRef)).willReturn(null);

        // Call the method under test.
        CollectionWithPagingInfo<RuleSet> actual = ruleSets.getRuleSets(FOLDER_NODE_ID, INCLUDES, PAGING);

        then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, false);
        then(nodeValidatorMock).shouldHaveNoMoreInteractions();

        then(ruleServiceMock).should().getRuleSetNode(folderNodeRef);
        then(ruleServiceMock).shouldHaveNoMoreInteractions();

        assertEquals(emptyList(), actual.getCollection());
        assertEquals(PAGING, actual.getPaging());
    }

    @Test
    public void testGetRuleSetById()
    {
        // Call the method under test.
        RuleSet actual = ruleSets.getRuleSetById(FOLDER_NODE_ID, RULE_SET_ID, INCLUDES);

        then(nodeValidatorMock).should().validateFolderNode(FOLDER_NODE_ID, false);
        then(nodeValidatorMock).should().validateRuleSetNode(RULE_SET_ID, folderNodeRef);
        then(nodeValidatorMock).shouldHaveNoMoreInteractions();

        assertEquals(ruleSetMock, actual);
    }

    @Test
    public void testLinkingToRuleSet()
    {
        NodeRef childNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "dummy-child-id");

        given(ruleServiceMock.hasRules(any(NodeRef.class))).willReturn(true, false);
        given(runtimeRuleServiceMock.getSavedRuleFolderAssoc(any(NodeRef.class))).willReturn(assocRef);
        given(assocRef.getChildRef()).willReturn(childNodeRef);

        //when
        assertEquals(ruleSets.linkToRuleSet(FOLDER_NODE_ID,LINK_TO_NODE_ID).getLinkToNodeId(), childNodeRef.getId());

        then(ruleServiceMock).should().hasRules(linkToNodeRef);
        then(ruleServiceMock).should().hasRules(folderNodeRef);
        then(runtimeRuleServiceMock).should().getSavedRuleFolderAssoc(linkToNodeRef);
        then(runtimeRuleServiceMock).shouldHaveNoMoreInteractions();
        then(nodeServiceMock).should().addChild(folderNodeRef, childNodeRef, RuleModel.ASSOC_RULE_FOLDER, RuleModel.ASSOC_RULE_FOLDER);
        then(nodeServiceMock).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testLinkToRuleSet_targetFolderHasNoRules()
    {
        given(ruleServiceMock.hasRules(linkToNodeRef)).willReturn(false);

        //when
        assertThatExceptionOfType(AlfrescoRuntimeException.class).isThrownBy(
                () -> ruleSets.linkToRuleSet(FOLDER_NODE_ID, LINK_TO_NODE_ID)
        );

        then(nodeServiceMock).shouldHaveNoMoreInteractions();
        then(ruleServiceMock).should().hasRules(linkToNodeRef);
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
        then(runtimeRuleServiceMock).shouldHaveNoInteractions();
    }

    @Test
    public void testLinkToRuleSet_folderShouldntHavePreExistingRules()
    {
        given(ruleServiceMock.hasRules(any(NodeRef.class))).willReturn(true, true);

        //when
        assertThatExceptionOfType(AlfrescoRuntimeException.class).isThrownBy(
                () -> ruleSets.linkToRuleSet(FOLDER_NODE_ID, LINK_TO_NODE_ID));

        then(nodeServiceMock).shouldHaveNoMoreInteractions();
        then(ruleServiceMock).should().hasRules(linkToNodeRef);
        then(ruleServiceMock).should().hasRules(folderNodeRef);
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
        then(runtimeRuleServiceMock).shouldHaveNoInteractions();
    }
}
