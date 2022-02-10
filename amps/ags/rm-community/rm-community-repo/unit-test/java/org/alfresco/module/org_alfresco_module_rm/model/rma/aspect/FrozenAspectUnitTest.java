/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.module.org_alfresco_module_rm.model.rma.aspect;

import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.ASPECT_HELD_CHILDREN;
import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.PROP_HELD_CHILDREN_COUNT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService;
import org.alfresco.module.org_alfresco_module_rm.util.NodeTypeUtility;
import org.alfresco.module.org_alfresco_module_rm.util.PropertyModificationAllowedCheck;
import org.alfresco.module.org_alfresco_module_rm.util.TransactionalResourceHelper;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Test class for frozen aspect
 * @author Ross Gale
 * @since 3.2
 */
@RunWith(MockitoJUnitRunner.class)
public class FrozenAspectUnitTest
{
    @Mock
    private NodeService mockNodeService;

    @Mock
    private ChildAssociationRef mockChildAssociationRef;

    @Mock
    private NodeTypeUtility mockedNodeTypeUtility;

    @Mock
    private FreezeService mockFreezeService;

    @Mock
    private TransactionalResourceHelper mockResourceHelper;

    @Mock
    private ChildAssociationRef mockChildRef;

    @Mock
    private ChildAssociationRef mockParentRef;

    @Mock
    private ChildAssociationRef mockOldRef;

    @Mock
    private PropertyModificationAllowedCheck mockPropertyModificationAllowedCheck;

    @InjectMocks
    private FrozenAspect frozenAspect;

    private final List<ChildAssociationRef> children = new ArrayList<>();

    private NodeRef record = new NodeRef("workspace://record/node");
    private NodeRef folder = new NodeRef("workspace://folder/node");
    private NodeRef content = new NodeRef("workspace://content/node");
    private NodeRef child = new NodeRef("workspace://content/child");
    private NodeRef parent = new NodeRef("workspace://content/parent");

    @Before
    public void setUp()
    {
        when(mockNodeService.exists(record)).thenReturn(true);
        when(mockNodeService.getType(record)).thenReturn(ContentModel.TYPE_CONTENT);
        when(mockedNodeTypeUtility.instanceOf(mockNodeService.getType(record), ContentModel.TYPE_CONTENT)).thenReturn(true);
        when(mockNodeService.exists(content)).thenReturn(true);
        when(mockNodeService.hasAspect(folder, ASPECT_HELD_CHILDREN)).thenReturn(true);
        when(mockNodeService.getProperty(folder, PROP_HELD_CHILDREN_COUNT)).thenReturn(1);
        when(mockFreezeService.isFrozen(content)).thenReturn(false);
        children.add(mockChildRef);
        when(mockNodeService.getChildAssocs(content)).thenReturn(children);
        when(mockChildRef.isPrimary()).thenReturn(true);
        frozenAspect.setNodeService(mockNodeService);
    }

    /**
     * Test that the held count is reduced on a record folder after the frozen aspect is removed from a record
     */
    @Test
    public void testRemoveAspectForRecords()
    {
        when(mockNodeService.getPrimaryParent(record)).thenReturn(mockChildAssociationRef);
        when(mockChildAssociationRef.getParentRef()).thenReturn(folder);
        frozenAspect.onRemoveAspect(record, null);
        verify(mockNodeService, times(1)).setProperty(folder, PROP_HELD_CHILDREN_COUNT, 0);
    }

    /**
     * Test that the held count is reduced on a folder after the frozen aspect is removed from a piece of content
     */
    @Test
    public void testRemoveAspectForContent()
    {
        when(mockNodeService.getType(content)).thenReturn(ContentModel.TYPE_CONTENT);
        when(mockedNodeTypeUtility.instanceOf(mockNodeService.getType(content), ContentModel.TYPE_CONTENT)).thenReturn(true);
        when(mockNodeService.getPrimaryParent(content)).thenReturn(mockChildAssociationRef);
        when(mockChildAssociationRef.getParentRef()).thenReturn(folder);
        frozenAspect.onRemoveAspect(content, null);
        verify(mockNodeService, times(1)).setProperty(folder, PROP_HELD_CHILDREN_COUNT, 0);
    }

    /**
     * Test that the remove code is only run for records or active content
     */
    @Test
    public void testRemoveAspectForContentDoesntUpdateForOtherTypes()
    {
        when(mockNodeService.getType(content)).thenReturn(ContentModel.TYPE_FOLDER);
        when(mockedNodeTypeUtility.instanceOf(mockNodeService.getType(content), ContentModel.TYPE_CONTENT)).thenReturn(false);
        frozenAspect.onRemoveAspect(content, null);
        verify(mockNodeService, times(0)).setProperty(folder, PROP_HELD_CHILDREN_COUNT, 0);
    }

    /**
     * Test before delete throws an error if a node is frozen
     */
    @Test(expected = PermissionDeniedException.class)
    public void testBeforeDeleteNodeThrowsExceptionIfNodeFrozen()
    {
        when(mockFreezeService.isFrozen(content)).thenReturn(true);
        frozenAspect.beforeDeleteNode(content);
    }

    /**
     * Test before delete is fine for non-frozen nodes
     */
    @Test
    public void testBeforeDeleteForNonFrozenNodes()
    {
        frozenAspect.beforeDeleteNode(content);
        verify(mockNodeService, times(1)).getChildAssocs(content);
        verify(mockChildRef, times(1)).getChildRef();
    }

    /**
     * Test before delete throws an error for a node with frozen children
     */
    @Test (expected = PermissionDeniedException.class)
    public void testBeforeDeleteThrowsExceptionForFrozenChild()
    {
        when(mockChildRef.getChildRef()).thenReturn(child);
        when(mockFreezeService.isFrozen(child)).thenReturn(true);
        frozenAspect.beforeDeleteNode(content);
    }

    /**
     * Test on add aspect for a record node
     */
    @Test
    public void testOnAddAspectForRecord()
    {
        when(mockNodeService.getPrimaryParent(record)).thenReturn(mockParentRef);
        when(mockParentRef.getParentRef()).thenReturn(parent);
        when(mockNodeService.hasAspect(parent, ASPECT_HELD_CHILDREN)).thenReturn(true);
        when(mockNodeService.getProperty(parent, PROP_HELD_CHILDREN_COUNT)).thenReturn(0);
        frozenAspect.onAddAspect(record,null);
        verify(mockNodeService, times(1)).setProperty(parent, PROP_HELD_CHILDREN_COUNT, 1);
    }

    /**
     * Test on add aspect for a content node
     */
    @Test
    public void testOnAddAspectForContent()
    {
        when(mockNodeService.getType(content)).thenReturn(ContentModel.TYPE_CONTENT);
        when(mockedNodeTypeUtility.instanceOf(mockNodeService.getType(content), ContentModel.TYPE_CONTENT)).thenReturn(true);
        when(mockNodeService.getPrimaryParent(content)).thenReturn(mockParentRef);
        when(mockParentRef.getParentRef()).thenReturn(parent);
        when(mockNodeService.hasAspect(parent, ASPECT_HELD_CHILDREN)).thenReturn(false);
        when(mockNodeService.getType(parent)).thenReturn(ContentModel.TYPE_FOLDER);
        when(mockedNodeTypeUtility.instanceOf(mockNodeService.getType(parent), ContentModel.TYPE_FOLDER)).thenReturn(true);
        frozenAspect.onAddAspect(content, null);
        verify(mockNodeService, times(1)).addAspect(any(NodeRef.class), any(QName.class), anyMap());
    }

    /**
     * Test before move throws an error for a frozen node
     */
    @Test(expected = PermissionDeniedException.class)
    public void testBeforeMoveThrowsExceptionForFrozenNode()
    {
        when(mockOldRef.getChildRef()).thenReturn(child);
        when(mockNodeService.exists(child)).thenReturn(true);
        when(mockFreezeService.isFrozen(child)).thenReturn(true);
        frozenAspect.beforeMoveNode(mockOldRef, null);
    }

    /**
     * Test update properties throws an error for frozen nodes
     */
    @Test(expected = PermissionDeniedException.class)
    public void testUpdatePropertiesThrowsExceptionForFrozenNode()
    {
        when(mockFreezeService.isFrozen(content)).thenReturn(true);
        when(mockPropertyModificationAllowedCheck.check(null, null)).thenReturn(false);
        frozenAspect.onUpdateProperties(content, null, null);
    }
}
