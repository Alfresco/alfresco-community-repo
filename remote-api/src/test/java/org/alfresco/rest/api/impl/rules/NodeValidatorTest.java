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

import static org.alfresco.model.ContentModel.TYPE_FOLDER;
import static org.alfresco.model.ContentModel.TYPE_SYSTEM_FOLDER;
import static org.alfresco.repo.rule.RuleModel.TYPE_RULE;
import static org.alfresco.rest.api.model.rules.RuleSet.DEFAULT_ID;
import static org.alfresco.service.cmr.security.AccessStatus.ALLOWED;
import static org.alfresco.service.cmr.security.AccessStatus.DENIED;
import static org.alfresco.service.cmr.security.PermissionService.CHANGE_PERMISSIONS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.reset;

import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.core.exceptions.RelationshipResourceNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.security.PermissionService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class NodeValidatorTest
{

    private static final String FOLDER_NODE_ID = "dummy-folder-node-id";
    private static final String LINK_TO_NODE_ID = "dummy-link-to-node-id";
    private static final String RULE_SET_ID = "dummy-rule-set-id";
    private static final String RULE_ID = "dummy-rule-id";
    private static final String PARENT_NODE_ID = "dummy-parent-node-id";
    private static final NodeRef folderNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, FOLDER_NODE_ID);
    private static final NodeRef ruleSetNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, RULE_SET_ID);
    private static final NodeRef ruleNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, RULE_ID);
    private static final NodeRef parentNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, PARENT_NODE_ID);

    @Mock
    private Nodes nodesMock;

    @Mock
    private Node ruleSetNodeMock;

    @Mock
    private PermissionService permissionServiceMock;

    @Mock
    private RuleService ruleServiceMock;

    @Mock
    private NodeService nodeServiceMock;

    @Mock
    private ChildAssociationRef primaryParentMock;

    @InjectMocks
    private NodeValidator nodeValidator;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.openMocks(this);
        given(nodesMock.validateOrLookupNode(FOLDER_NODE_ID)).willReturn(folderNodeRef);
        given(nodesMock.validateNode(RULE_SET_ID)).willReturn(ruleSetNodeRef);
        given(nodesMock.validateNode(RULE_ID)).willReturn(ruleNodeRef);
        given(nodesMock.nodeMatches(any(), any(), any())).willReturn(true);
        given(permissionServiceMock.hasReadPermission(any())).willReturn(ALLOWED);
        given(permissionServiceMock.hasPermission(any(), any())).willReturn(ALLOWED);
    }

    @Test
    public void testValidateFolderNode()
    {
        // when
        final NodeRef nodeRef = nodeValidator.validateFolderNode(FOLDER_NODE_ID, false);

        then(nodesMock).should().validateOrLookupNode(FOLDER_NODE_ID);
        then(nodesMock).should().nodeMatches(folderNodeRef, Set.of(TYPE_FOLDER), null);
        then(nodesMock).shouldHaveNoMoreInteractions();
        then(permissionServiceMock).should().hasReadPermission(folderNodeRef);
        then(permissionServiceMock).shouldHaveNoMoreInteractions();
        then(ruleServiceMock).shouldHaveNoInteractions();

        assertThat(nodeRef).isNotNull().isEqualTo(folderNodeRef);
    }

    @Test
    public void testValidateFolderNode_notExistingFolder()
    {
        given(nodesMock.validateOrLookupNode(FOLDER_NODE_ID)).willThrow(new EntityNotFoundException(FOLDER_NODE_ID));

        //when
        assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(
            () -> nodeValidator.validateFolderNode(FOLDER_NODE_ID, false));

        then(nodesMock).should().validateOrLookupNode(FOLDER_NODE_ID);
        then(nodesMock).shouldHaveNoMoreInteractions();
        then(ruleServiceMock).shouldHaveNoInteractions();
    }

    @Test
    public void testValidateFolderNode_notMatchingTypeFolder()
    {
        given(nodesMock.nodeMatches(any(), eq(Set.of(TYPE_FOLDER)), any())).willReturn(false);

        // when
        assertThatExceptionOfType(InvalidArgumentException.class).isThrownBy(
            () -> nodeValidator.validateFolderNode(FOLDER_NODE_ID, false));

        then(nodesMock).should().validateOrLookupNode(FOLDER_NODE_ID);
        then(nodesMock).should().nodeMatches(folderNodeRef, Set.of(TYPE_FOLDER), null);
        then(nodesMock).shouldHaveNoMoreInteractions();
        then(ruleServiceMock).shouldHaveNoInteractions();
    }

    @Test
    public void testValidateFolderNode_noReadPermission()
    {
        given(permissionServiceMock.hasReadPermission(any())).willReturn(DENIED);

        // when
        assertThatExceptionOfType(PermissionDeniedException.class).isThrownBy(
            () -> nodeValidator.validateFolderNode(FOLDER_NODE_ID, false));

        then(permissionServiceMock).should().hasReadPermission(folderNodeRef);
        then(permissionServiceMock).shouldHaveNoMoreInteractions();
        then(ruleServiceMock).shouldHaveNoInteractions();
    }

    @Test
    public void testValidateFolderNode_noChangePermission()
    {
        given(permissionServiceMock.hasPermission(any(), any())).willReturn(DENIED);

        // when
        assertThatExceptionOfType(PermissionDeniedException.class).isThrownBy(() ->
            nodeValidator.validateFolderNode(folderNodeRef.getId(), true));

        then(permissionServiceMock).should().hasPermission(folderNodeRef, CHANGE_PERMISSIONS);
        then(permissionServiceMock).shouldHaveNoMoreInteractions();
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
    }

    @Test
    public void validateRuleSetNode()
    {
        given(ruleServiceMock.isRuleSetAssociatedWithFolder(any(), any())).willReturn(true);

        // when
        final NodeRef nodeRef = nodeValidator.validateRuleSetNode(RULE_SET_ID, folderNodeRef);

        then(nodesMock).should().validateNode(RULE_SET_ID);
        then(nodesMock).should().nodeMatches(ruleSetNodeRef, Set.of(TYPE_SYSTEM_FOLDER), null);
        then(nodesMock).shouldHaveNoMoreInteractions();
        then(ruleServiceMock).should().isRuleSetAssociatedWithFolder(ruleSetNodeRef, folderNodeRef);
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
        then(permissionServiceMock).shouldHaveNoInteractions();

        assertThat(nodeRef).isNotNull().isEqualTo(ruleSetNodeRef);
    }

    @Test
    public void testValidateRuleSetNodeNoParentId()
    {
        given(nodesMock.getNode(any())).willReturn(ruleSetNodeMock);
        given(nodeServiceMock.getPrimaryParent(any())).willReturn(primaryParentMock);
        given(primaryParentMock.getParentRef()).willReturn(parentNodeRef);

        //when
        final NodeRef nodeRef = nodeValidator.validateRuleSetNode(LINK_TO_NODE_ID,true);

        assertThat(nodeRef).isNotNull().isEqualTo(parentNodeRef);

    }

    @Test
    public void validateRuleSetNode_defaultId()
    {
        given(ruleServiceMock.getRuleSetNode(any())).willReturn(ruleSetNodeRef);

        // when
        final NodeRef nodeRef = nodeValidator.validateRuleSetNode(DEFAULT_ID, folderNodeRef);

        then(ruleServiceMock).should().getRuleSetNode(folderNodeRef);
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
        then(nodesMock).shouldHaveNoInteractions();
        then(permissionServiceMock).shouldHaveNoInteractions();

        assertThat(nodeRef).isNotNull().isEqualTo(ruleSetNodeRef);
    }

    @Test
    public void testValidateRuleSetNode_notExistingRuleSet()
    {
        given(nodesMock.validateNode(RULE_SET_ID)).willThrow(new EntityNotFoundException(RULE_SET_ID));

        //when
        assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(
            () -> nodeValidator.validateRuleSetNode(RULE_SET_ID, folderNodeRef));

        then(nodesMock).should().validateNode(RULE_SET_ID);
        then(nodesMock).shouldHaveNoMoreInteractions();
        then(ruleServiceMock).shouldHaveNoInteractions();
    }

    @Test
    public void testValidateRuleSetNode_notMatchingTypeSystemFolder()
    {
        given(nodesMock.nodeMatches(any(), eq(Set.of(TYPE_SYSTEM_FOLDER)), any())).willReturn(false);

        // when
        assertThatExceptionOfType(InvalidArgumentException.class).isThrownBy(
            () -> nodeValidator.validateRuleSetNode(RULE_SET_ID, folderNodeRef));

        then(nodesMock).should().validateNode(RULE_SET_ID);
        then(nodesMock).should().nodeMatches(ruleSetNodeRef, Set.of(TYPE_SYSTEM_FOLDER), null);
        then(nodesMock).shouldHaveNoMoreInteractions();
        then(ruleServiceMock).shouldHaveNoInteractions();
    }

    @Test
    public void testValidateRuleSetNode_notExistingDefaultRuleSet()
    {
        given(ruleServiceMock.getRuleSetNode(folderNodeRef)).willReturn(null);

        // when
        assertThatExceptionOfType(RelationshipResourceNotFoundException.class).isThrownBy(
            () -> nodeValidator.validateRuleSetNode(DEFAULT_ID, folderNodeRef));

        then(ruleServiceMock).should().getRuleSetNode(folderNodeRef);
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
        then(nodesMock).shouldHaveNoInteractions();
    }

    @Test
    public void testValidateRuleSetNode_notAssociatedRuleSetToFolder()
    {
        given(ruleServiceMock.isRuleSetAssociatedWithFolder(any(), any())).willReturn(false);

        // when
        assertThatExceptionOfType(InvalidArgumentException.class).isThrownBy(
            () -> nodeValidator.validateRuleSetNode(RULE_SET_ID, folderNodeRef));

        then(ruleServiceMock).should().isRuleSetAssociatedWithFolder(ruleSetNodeRef, folderNodeRef);
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
    }

    @Test
    public void validateRuleNode()
    {
        given(ruleServiceMock.isRuleAssociatedWithRuleSet(any(), any())).willReturn(true);

        // when
        final NodeRef nodeRef = nodeValidator.validateRuleNode(RULE_ID, ruleSetNodeRef);

        then(nodesMock).should().validateNode(RULE_ID);
        then(nodesMock).should().nodeMatches(ruleNodeRef, Set.of(TYPE_RULE), null);
        then(nodesMock).shouldHaveNoMoreInteractions();
        then(ruleServiceMock).should().isRuleAssociatedWithRuleSet(ruleNodeRef, ruleSetNodeRef);
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
        then(permissionServiceMock).shouldHaveNoInteractions();

        assertThat(nodeRef).isNotNull().isEqualTo(ruleNodeRef);
    }

    @Test
    public void validateRuleNode_nullRuleSet()
    {
        // when
        final NodeRef nodeRef = nodeValidator.validateRuleNode(RULE_ID, null);

        then(nodesMock).should().validateNode(RULE_ID);
        then(nodesMock).should().nodeMatches(ruleNodeRef, Set.of(TYPE_RULE), null);
        then(nodesMock).shouldHaveNoMoreInteractions();
        then(ruleServiceMock).shouldHaveNoInteractions();
        then(permissionServiceMock).shouldHaveNoInteractions();

        assertThat(nodeRef).isNotNull().isEqualTo(ruleNodeRef);
    }

    @Test
    public void testValidateRuleNode_notExistingRule()
    {
        given(nodesMock.validateNode(RULE_ID)).willThrow(new EntityNotFoundException(RULE_ID));

        //when
        assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(
            () -> nodeValidator.validateRuleNode(RULE_ID, ruleSetNodeRef));

        then(nodesMock).should().validateNode(RULE_ID);
        then(nodesMock).shouldHaveNoMoreInteractions();
        then(ruleServiceMock).shouldHaveNoInteractions();
    }

    @Test
    public void testValidateRuleNode_notMatchingTypeRule()
    {
        given(nodesMock.nodeMatches(any(), eq(Set.of(TYPE_RULE)), any())).willReturn(false);

        // when
        assertThatExceptionOfType(InvalidArgumentException.class).isThrownBy(
            () -> nodeValidator.validateRuleNode(RULE_ID, ruleSetNodeRef));

        then(nodesMock).should().validateNode(RULE_ID);
        then(nodesMock).should().nodeMatches(ruleNodeRef, Set.of(TYPE_RULE), null);
        then(nodesMock).shouldHaveNoMoreInteractions();
        then(ruleServiceMock).shouldHaveNoInteractions();
    }

    @Test
    public void testValidateRuleNode_notAssociatedRuleToRuleSet()
    {
        given(ruleServiceMock.isRuleAssociatedWithRuleSet(any(), any())).willReturn(false);

        // when
        assertThatExceptionOfType(InvalidArgumentException.class).isThrownBy(
            () -> nodeValidator.validateRuleNode(RULE_ID, ruleSetNodeRef));

        then(ruleServiceMock).should().isRuleAssociatedWithRuleSet(ruleNodeRef, ruleSetNodeRef);
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testIsRuleSetNode()
    {
        //resetting mock to bypass setup method
        resetNodesMock();

        boolean actual = nodeValidator.isRuleSetNode(RULE_SET_ID);
        Assert.assertTrue(actual);
    }


    @Test
    public void testIsNotRuleSetNode()
    {
        //resetting mock to bypass setup method
        resetNodesMock();

        //using an id that doesn't belong to a ruleset node
        boolean actual = nodeValidator.isRuleSetNode(FOLDER_NODE_ID);
        Assert.assertFalse(actual);
    }

    @Test
    public void testIsRuleSetNotNullAndShared()
    {
        given(ruleServiceMock.isRuleSetShared(any())).willReturn(true);

        // when
        final boolean shared = nodeValidator.isRuleSetNotNullAndShared(ruleSetNodeRef);

        then(ruleServiceMock).should().isRuleSetShared(ruleSetNodeRef);
        then(ruleServiceMock).shouldHaveNoMoreInteractions();

        assertThat(shared).isTrue();
    }

    @Test
    public void testIsRuleSetNotNullAndShared_nullRuleSetNode()
    {
        // when
        final boolean shared = nodeValidator.isRuleSetNotNullAndShared(null);

        then(ruleServiceMock).shouldHaveNoInteractions();

        assertThat(shared).isFalse();
    }

    @Test
    public void testIsRuleSetNotNullAndShared_withoutRuleSetAndWithFolder()
    {
        given(ruleServiceMock.getRuleSetNode(any())).willReturn(ruleSetNodeRef);

        // when
        nodeValidator.isRuleSetNotNullAndShared(null, folderNodeRef);

        then(ruleServiceMock).should().getRuleSetNode(folderNodeRef);
        then(ruleServiceMock).should().isRuleSetShared(ruleSetNodeRef);
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testIsRuleSetNotNullAndShared_withRuleSetAndWithFolder()
    {
        // when
        nodeValidator.isRuleSetNotNullAndShared(ruleSetNodeRef, folderNodeRef);

        then(ruleServiceMock).should().isRuleSetShared(ruleSetNodeRef);
        then(ruleServiceMock).shouldHaveNoMoreInteractions();
    }

    private void resetNodesMock()
    {
        reset(nodesMock);
        given(nodesMock.validateOrLookupNode(FOLDER_NODE_ID)).willReturn(folderNodeRef);
        given(nodesMock.validateNode(RULE_SET_ID)).willReturn(ruleSetNodeRef);
        given(nodesMock.validateNode(RULE_ID)).willReturn(ruleNodeRef);
        given(nodesMock.nodeMatches(ruleSetNodeRef, Set.of(ContentModel.TYPE_SYSTEM_FOLDER), null)).willReturn(true);
    }
}
