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

package org.alfresco.module.org_alfresco_module_rm.hold;

import static java.util.Arrays.asList;

import static org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock.generateQName;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Hold service implementation unit test
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public class HoldServiceImplUnitTest extends BaseUnitTest
{
    /** test values */
    private static final String HOLD_NAME = "holdname";
    private static final String HOLD_REASON = "holdreason";
    private static final String HOLD_DESCRIPTION = "holddescription";
    private static final String GENERIC_ERROR_MSG = "any error message text";

    protected NodeRef holdContainer;
    protected NodeRef hold;
    protected NodeRef hold2;
    protected NodeRef activeContent;

    @Mock
    private CapabilityService mockedCapabilityService;

    @Mock
    private ChildAssociationRef mockChildAssociationRef;

    @Spy @InjectMocks HoldServiceImpl holdService;

    @Before
    @Override
    public void before() throws Exception
    {
        super.before();

        // setup objects used in mock interactions
        holdContainer = generateNodeRef(TYPE_HOLD_CONTAINER);
        hold = generateNodeRef(TYPE_HOLD);
        hold2 = generateNodeRef(TYPE_HOLD);

        when(mockedCapabilityService.getCapabilityAccessState(hold, RMPermissionModel.ADD_TO_HOLD)).thenReturn(AccessStatus.ALLOWED);
        when(mockedCapabilityService.getCapabilityAccessState(hold2, RMPermissionModel.ADD_TO_HOLD)).thenReturn(AccessStatus.ALLOWED);
        when(mockedCapabilityService.getCapabilityAccessState(hold, RMPermissionModel.REMOVE_FROM_HOLD)).thenReturn(AccessStatus.ALLOWED);
        when(mockedCapabilityService.getCapabilityAccessState(hold2, RMPermissionModel.REMOVE_FROM_HOLD)).thenReturn(AccessStatus.ALLOWED);

        activeContent = generateNodeRef();
        QName contentSubtype = QName.createQName("contentSubtype", "contentSubtype");
        when(mockedNodeService.getType(activeContent)).thenReturn(contentSubtype);
        when(mockedNodeTypeUtility.instanceOf(contentSubtype, ContentModel.TYPE_CONTENT)).thenReturn(true);
        when(mockedNodeService.getPrimaryParent(activeContent)).thenReturn(mockChildAssociationRef);

        when(mockedNodeService.getPrimaryParent(recordFolder)).thenReturn(mockChildAssociationRef);
        // setup interactions
        doReturn(holdContainer).when(mockedFilePlanService).getHoldContainer(filePlan);
    }

    @Test
    public void isHold()
    {
        assertTrue(holdService.isHold(hold));
        assertFalse(holdService.isHold(recordFolder));
    }

    @Test
    public void heldByMultipleResults()
    {
        // setup record folder in multiple holds
        List<ChildAssociationRef> holds = new ArrayList<>(4);
        holds.add(new ChildAssociationRef(ASSOC_FROZEN_CONTENT, hold, ASSOC_FROZEN_CONTENT, recordFolder, true, 1));
        holds.add(new ChildAssociationRef(ASSOC_FROZEN_CONTENT, hold2, ASSOC_FROZEN_CONTENT, recordFolder, true, 2));
        holds.add(new ChildAssociationRef(ASSOC_FROZEN_CONTENT, hold, ASSOC_FROZEN_CONTENT, activeContent, true, 1));
        holds.add(new ChildAssociationRef(ASSOC_FROZEN_CONTENT, hold2, ASSOC_FROZEN_CONTENT, activeContent, true, 2));
        doReturn(holds).when(mockedNodeService).getParentAssocs(recordFolder, ASSOC_FROZEN_CONTENT, ASSOC_FROZEN_CONTENT);

        //setup active content in multiple holds
        doReturn(holds).when(mockedNodeService).getParentAssocs(activeContent, ASSOC_FROZEN_CONTENT, ASSOC_FROZEN_CONTENT);

        doReturn(Collections.singleton(filePlan)).when(mockedFilePlanService).getFilePlans();

        // check that both holds are found for record folder
        List<NodeRef> heldByHolds = holdService.heldBy(recordFolder, true);
        assertNotNull(heldByHolds);
        assertEquals(2, heldByHolds.size());
        assertTrue(holdService.heldBy(recordFolder, false).isEmpty());

        // check that both holds are found for record (even thou they are one level deep)
        heldByHolds = holdService.heldBy(record, true);
        assertNotNull(heldByHolds);
        assertEquals(2, heldByHolds.size());
        assertTrue(holdService.heldBy(record, false).isEmpty());

        // check that both holds are found for active content
        heldByHolds = holdService.heldBy(activeContent, true);
        assertNotNull(heldByHolds);
        assertEquals(2, heldByHolds.size());
        assertTrue(holdService.heldBy(activeContent, false).isEmpty());
    }

    @Test (expected=AlfrescoRuntimeException.class)
    public void getHold()
    {
        // setup node service interactions
        when(mockedNodeService.getChildByName(eq(holdContainer), eq(ContentModel.ASSOC_CONTAINS), anyString())).thenReturn(null)
                                                                                                               .thenReturn(hold)
                                                                                                               .thenReturn(recordFolder);

        // no hold
        NodeRef noHold = holdService.getHold(filePlan, "notAHold");
        assertNull(noHold);

        // found hold
        NodeRef someHold = holdService.getHold(filePlan, "someHold");
        assertNotNull(someHold);
        assertEquals(TYPE_HOLD, mockedNodeService.getType(someHold));

        // ensure runtime exception is thrown
        holdService.getHold(filePlan, "notHold");
    }

    @Test (expected=RuntimeException.class)
    public void getHeldNotAHold()
    {
        holdService.getHeld(recordFolder);
    }

    @Test
    public void getHeldNoResults()
    {
        assertTrue(holdService.getHeld(hold).isEmpty());
    }

    @Test
    public void getHeldWithResults()
    {
        // setup record folder in hold
        List<ChildAssociationRef> holds = new ArrayList<>(2);
        holds.add(new ChildAssociationRef(ASSOC_FROZEN_CONTENT, hold, ASSOC_FROZEN_CONTENT, recordFolder, true, 1));
        holds.add(new ChildAssociationRef(ASSOC_FROZEN_CONTENT, hold, ASSOC_FROZEN_CONTENT, activeContent, true, 1));
        doReturn(holds).when(mockedNodeService).getChildAssocs(hold, ASSOC_FROZEN_CONTENT, RegexQNamePattern.MATCH_ALL);

        List<NodeRef> list = holdService.getHeld(hold);
        assertEquals(2, list.size());
        assertEquals(recordFolder, list.get(0));
        assertEquals(activeContent, list.get(1));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void createHold()
    {
        // setup node service interactions
        when(mockedNodeService.createNode(eq(holdContainer), eq(ContentModel.ASSOC_CONTAINS), any(QName.class) , eq(TYPE_HOLD), any(Map.class)))
            .thenReturn(new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, holdContainer, generateQName(), hold));

        mockPoliciesForCreateHold();

        // create hold
        NodeRef newHold = holdService.createHold(filePlan, HOLD_NAME, HOLD_REASON, HOLD_DESCRIPTION);
        assertNotNull(newHold);
        assertEquals(TYPE_HOLD, mockedNodeService.getType(newHold));
        assertEquals(hold, newHold);

        // check the node service interactions
        ArgumentCaptor<Map> propertyMapCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<QName> assocNameCaptor = ArgumentCaptor.forClass(QName.class);
        verify(mockedNodeService).createNode(eq(holdContainer), eq(ContentModel.ASSOC_CONTAINS), assocNameCaptor.capture() , eq(TYPE_HOLD), propertyMapCaptor.capture());

        // check property map
        Map<QName, Serializable> propertyMap = (Map<QName, Serializable>)propertyMapCaptor.getValue();
        assertNotNull(propertyMap);
        assertEquals(3, propertyMap.size());
        assertTrue(propertyMap.containsKey(ContentModel.PROP_NAME));
        assertEquals(HOLD_NAME, propertyMap.get(ContentModel.PROP_NAME));
        assertTrue(propertyMap.containsKey(ContentModel.PROP_DESCRIPTION));
        assertEquals(HOLD_DESCRIPTION, propertyMap.get(ContentModel.PROP_DESCRIPTION));
        assertTrue(propertyMap.containsKey(PROP_HOLD_REASON));
        assertEquals(HOLD_REASON, propertyMap.get(PROP_HOLD_REASON));

        // check assoc name
        assertNotNull(assocNameCaptor.getValue());
        assertEquals(NamespaceService.CONTENT_MODEL_1_0_URI, assocNameCaptor.getValue().getNamespaceURI());
        assertEquals(HOLD_NAME, assocNameCaptor.getValue().getLocalName());
    }

    @Test
    public void getHoldReason()
    {
        // setup node service interactions
        when(mockedNodeService.exists(hold))
            .thenReturn(false)
            .thenReturn(true)
            .thenReturn(true)
            .thenReturn(true);
        when(mockedNodeService.getProperty(eq(hold), eq(PROP_HOLD_REASON)))
            .thenReturn(null)
            .thenReturn(HOLD_REASON);

        // node does not exist
        assertNull(holdService.getHoldReason(hold));

        // node isn't a hold
        assertNull(holdService.getHoldReason(recordFolder));

        // hold reason isn't set
        assertNull(holdService.getHoldReason(hold));

        // hold reason set
        assertEquals(HOLD_REASON, holdService.getHoldReason(hold));
    }

    @Test
    public void setHoldReason()
    {
        // setup node service interactions
        when(mockedNodeService.exists(hold))
            .thenReturn(false)
            .thenReturn(true)
            .thenReturn(true);

        // node does not exist
        holdService.setHoldReason(hold, HOLD_REASON);
        verify(mockedNodeService, never()).setProperty(hold, PROP_HOLD_REASON, HOLD_REASON);

        // node isn't a hold
        holdService.setHoldReason(recordFolder, HOLD_REASON);
        verify(mockedNodeService, never()).setProperty(hold, PROP_HOLD_REASON, HOLD_REASON);

        // set hold reason
        holdService.setHoldReason(hold, HOLD_REASON);
        verify(mockedNodeService).setProperty(hold, PROP_HOLD_REASON, HOLD_REASON);
    }

    @Test (expected=AlfrescoRuntimeException.class)
    public void deleteHoldNotAHold()
    {
        holdService.deleteHold(recordFolder);
        verify(mockedNodeService, never()).deleteNode(hold);
    }

    @Test
    public void deleteHold()
    {
        mockPoliciesForDeleteHold();

        // delete hold
        holdService.deleteHold(hold);
        verify(mockedNodeService).deleteNode(hold);

        // TODO check interactions with policy component!!!
    }

    @Test (expected = AccessDeniedException.class)
    public void deleteHoldNoPermissionsOnContent()
    {
        mockPoliciesForDeleteHold();

        ChildAssociationRef childAssociationRef = generateChildAssociationRef(hold, record);
        when(mockedNodeService.getChildAssocs(hold, ASSOC_FROZEN_CONTENT, RegexQNamePattern.MATCH_ALL))
            .thenReturn(Collections.singletonList(childAssociationRef));

        when(mockedPermissionService.hasPermission(record, RMPermissionModel.FILING)).thenReturn(AccessStatus.DENIED);
        when(mockedNodeService.getProperty(record, ContentModel.PROP_NAME)).thenThrow(new AccessDeniedException(GENERIC_ERROR_MSG));

        holdService.beforeDeleteNode(hold);
    }

    @Test (expected = IntegrityException.class)
    public void addToHoldNotAHold()
    {
        holdService.addToHold(recordFolder, recordFolder);
    }

    @Test (expected = IntegrityException.class)
    public void addToHoldNotARecordFolderOrRecordOrActiveContent()
    {
        NodeRef anotherThing = generateNodeRef(TYPE_RECORD_CATEGORY);
        holdService.addToHold(hold, anotherThing);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void addToHoldNotInHold()
    {
        mockPoliciesForAddToHold();
        when(mockedNodeService.getPrimaryParent(record)).thenReturn(mockChildAssociationRef);

        holdService.addToHold(hold, recordFolder);

        verify(mockedNodeService).addChild(hold, recordFolder, ASSOC_FROZEN_CONTENT, ASSOC_FROZEN_CONTENT);
        verify(mockedNodeService).addAspect(eq(recordFolder), eq(ASPECT_FROZEN), any(Map.class));
        verify(mockedNodeService).addAspect(eq(record), eq(ASPECT_FROZEN), any(Map.class));

        holdService.addToHold(hold, record);
        verify(mockedNodeService).addChild(hold, record, ASSOC_FROZEN_CONTENT, ASSOC_FROZEN_CONTENT);
        verify(mockedNodeService).addAspect(eq(recordFolder), eq(ASPECT_FROZEN), any(Map.class));
        verify(mockedNodeService, times(2)).addAspect(eq(record), eq(ASPECT_FROZEN), any(Map.class));

        holdService.addToHold(hold, activeContent);
        verify(mockedNodeService).addChild(hold, activeContent, ASSOC_FROZEN_CONTENT, ASSOC_FROZEN_CONTENT);
        verify(mockedNodeService).addAspect(eq(activeContent), eq(ASPECT_FROZEN), any(Map.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void addToHoldAlreadyInHold()
    {
        doReturn(asList(recordFolder, activeContent)).when(holdService).getHeld(hold);

        holdService.addToHold(hold, recordFolder);

        verify(mockedNodeService, never()).addChild(hold, recordFolder, ASSOC_FROZEN_CONTENT, ASSOC_FROZEN_CONTENT);
        verify(mockedNodeService, never()).addAspect(eq(recordFolder), eq(ASPECT_FROZEN), any(Map.class));
        verify(mockedNodeService, never()).addAspect(eq(record), eq(ASPECT_FROZEN), any(Map.class));

        holdService.addToHold(hold, activeContent);

        verify(mockedNodeService, never()).addChild(hold, activeContent, ASSOC_FROZEN_CONTENT, ASSOC_FROZEN_CONTENT);
        verify(mockedNodeService, never()).addAspect(eq(activeContent), eq(ASPECT_FROZEN), any(Map.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void addToHoldAlreadyFrozen()
    {
        doReturn(true).when(mockedNodeService).hasAspect(recordFolder, ASPECT_FROZEN);
        doReturn(true).when(mockedNodeService).hasAspect(record, ASPECT_FROZEN);
        doReturn(true).when(mockedNodeService).hasAspect(activeContent, ASPECT_FROZEN);

        mockPoliciesForAddToHold();

        holdService.addToHold(hold, recordFolder);

        verify(mockedNodeService).addChild(hold, recordFolder, ASSOC_FROZEN_CONTENT, ASSOC_FROZEN_CONTENT);
        verify(mockedNodeService, never()).addAspect(eq(recordFolder), eq(ASPECT_FROZEN), any(Map.class));
        verify(mockedNodeService, never()).addAspect(eq(record), eq(ASPECT_FROZEN), any(Map.class));

        holdService.addToHold(hold, activeContent);
        verify(mockedNodeService).addChild(hold, activeContent, ASSOC_FROZEN_CONTENT, ASSOC_FROZEN_CONTENT);
        verify(mockedNodeService, never()).addAspect(eq(activeContent), eq(ASPECT_FROZEN), any(Map.class));
    }

    @Test (expected = AccessDeniedException.class)
    public void addActiveContentToHoldsNoPermissionsOnHold()
    {
        when(mockedCapabilityService.getCapabilityAccessState(hold, RMPermissionModel.ADD_TO_HOLD)).thenReturn(AccessStatus.DENIED);
        // build a list of holds
        List<NodeRef> holds = new ArrayList<>(2);
        holds.add(hold);
        holds.add(hold2);
        holdService.addToHolds(holds, activeContent);
    }

    @Test (expected = AccessDeniedException.class)
    public void addActiveContentToHoldNoPermissionsOnContent()
    {
        when(mockedPermissionService.hasPermission(activeContent, PermissionService.WRITE)).thenReturn(AccessStatus.DENIED);
        holdService.addToHold(hold, activeContent);
    }

    @Test (expected = IntegrityException.class)
    public void addArchivedContentToHold()
    {
        when(mockedNodeService.hasAspect(activeContent, RecordsManagementModel.ASPECT_ARCHIVED)).thenReturn(true);
        holdService.addToHold(hold, activeContent);
    }

    @Test (expected = IntegrityException.class)
    public void addLockedContentToHold()
    {
        when(mockedNodeService.hasAspect(activeContent, ContentModel.ASPECT_LOCKABLE)).thenReturn(true);
        holdService.addToHold(hold, activeContent);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void addToHolds()
    {
        // ensure the interaction indicates that a node has the frozen aspect applied if it has
        doAnswer(new Answer<Void>()
        {
            public Void answer(InvocationOnMock invocation)
            {
                NodeRef nodeRef = (NodeRef)invocation.getArguments()[0];
                doReturn(true).when(mockedNodeService).hasAspect(nodeRef, ASPECT_FROZEN);
                return null;
            }

        }).when(mockedNodeService).addAspect(any(NodeRef.class), eq(ASPECT_FROZEN), any(Map.class));

        mockPoliciesForAddToHold();

        // build a list of holds
        List<NodeRef> holds = new ArrayList<>(2);
        holds.add(hold);
        holds.add(hold2);

        // add the record folder to both holds
        holdService.addToHolds(holds, recordFolder);

        // verify the interactions
        verify(mockedNodeService, times(1)).addChild(hold, recordFolder, ASSOC_FROZEN_CONTENT, ASSOC_FROZEN_CONTENT);
        verify(mockedNodeService, times(1)).addChild(hold2, recordFolder, ASSOC_FROZEN_CONTENT, ASSOC_FROZEN_CONTENT);
        verify(mockedNodeService, times(1)).addAspect(eq(recordFolder), eq(ASPECT_FROZEN), any(Map.class));
        verify(mockedNodeService, times(1)).addAspect(eq(record), eq(ASPECT_FROZEN), any(Map.class));
    }

    @Test (expected = IntegrityException.class)
    public void removeFromHoldNotAHold()
    {
        holdService.removeFromHold(recordFolder, recordFolder);
    }

    @Test
    public void removeFromHoldNotInHold()
    {
        mockPoliciesForRemoveFromHold();

        holdService.removeFromHold(hold, recordFolder);

        verify(mockedNodeService, never()).removeChild(hold, recordFolder);
        verify(mockedNodeService, never()).removeAspect(recordFolder, ASPECT_FROZEN);
        verify(mockedNodeService, never()).removeAspect(record, ASPECT_FROZEN);
    }

    @Test
    public void removeFromHold()
    {
        doReturn(Collections.singletonList(recordFolder)).when(holdService).getHeld(hold);
        doReturn(true).when(mockedNodeService).hasAspect(recordFolder, ASPECT_FROZEN);
        doReturn(true).when(mockedNodeService).hasAspect(record, ASPECT_FROZEN);

        mockPoliciesForRemoveFromHold();

        holdService.removeFromHold(hold, recordFolder);

        verify(mockedNodeService, times(1)).removeChild(hold, recordFolder);
        verify(mockedNodeService, times(1)).removeAspect(recordFolder, ASPECT_FROZEN);
        verify(mockedNodeService, times(1)).removeAspect(record, ASPECT_FROZEN);
    }

    @Test
    public void removeFromHolds()
    {
        doReturn(Collections.singletonList(recordFolder)).when(holdService).getHeld(hold);
        doReturn(Collections.singletonList(recordFolder)).when(holdService).getHeld(hold2);
        doReturn(true).when(mockedNodeService).hasAspect(recordFolder, ASPECT_FROZEN);
        doReturn(true).when(mockedNodeService).hasAspect(record, ASPECT_FROZEN);

        mockPoliciesForRemoveFromHold();

        // build a list of holds
        List<NodeRef> holds = new ArrayList<>(2);
        holds.add(hold);
        holds.add(hold2);

        holdService.removeFromHolds(holds, recordFolder);

        verify(mockedNodeService, times(1)).removeChild(hold, recordFolder);
        verify(mockedNodeService, times(1)).removeChild(hold2, recordFolder);
        verify(mockedNodeService, times(1)).removeAspect(recordFolder, ASPECT_FROZEN);
        verify(mockedNodeService, times(1)).removeAspect(record, ASPECT_FROZEN);
    }

    @Test
    public void removeFromAllHolds()
    {
        // build a list of holds
        List<NodeRef> holds = new ArrayList<>(2);
        holds.add(hold);
        holds.add(hold2);

        doAnswer(new Answer<Void>()
        {
            public Void answer(InvocationOnMock invocation)
            {
                doReturn(Collections.singletonList(hold2)).when(holdService).heldBy(recordFolder, true);
                return null;
            }

        }).when(mockedNodeService).removeChild(hold, recordFolder);

        mockPoliciesForRemoveFromHold();

        doAnswer(new Answer<Void>()
        {
            public Void answer(InvocationOnMock invocation)
            {
                doReturn(new ArrayList<NodeRef>()).when(holdService).heldBy(recordFolder, true);
                return null;
            }

        }).when(mockedNodeService).removeChild(hold2, recordFolder);

        // define interactions
        doReturn(holds).when(holdService).heldBy(recordFolder, true);
        doReturn(Collections.singletonList(recordFolder)).when(holdService).getHeld(hold);
        doReturn(Collections.singletonList(recordFolder)).when(holdService).getHeld(hold2);
        doReturn(true).when(mockedNodeService).hasAspect(recordFolder, ASPECT_FROZEN);
        doReturn(true).when(mockedNodeService).hasAspect(record, ASPECT_FROZEN);

        // remove record folder from all holds
        holdService.removeFromAllHolds(recordFolder);

        // verify interactions
        verify(mockedNodeService, times(1)).removeChild(hold, recordFolder);
        verify(mockedNodeService, times(1)).removeChild(hold2, recordFolder);
        verify(mockedNodeService, times(1)).removeAspect(recordFolder, ASPECT_FROZEN);
        verify(mockedNodeService, times(1)).removeAspect(record, ASPECT_FROZEN);
    }

     @Test (expected = AccessDeniedException.class)
    public void removeActiveContentFromHoldsNoPermissionsOnHold()
    {
        doReturn(Collections.singletonList(activeContent)).when(holdService).getHeld(hold);
        doReturn(Collections.singletonList(activeContent)).when(holdService).getHeld(hold2);
        doReturn(true).when(mockedNodeService).hasAspect(activeContent, ASPECT_FROZEN);
        when(mockedCapabilityService.getCapabilityAccessState(hold, RMPermissionModel.REMOVE_FROM_HOLD)).thenReturn(AccessStatus.DENIED);
        // build a list of holds
        List<NodeRef> holds = new ArrayList<>(2);
        holds.add(hold);
        holds.add(hold2);
        holdService.removeFromHolds(holds, activeContent);
    }

    /**
     * test before delete node throws exception for failed read permission check for content
     */
    @Test (expected = AccessDeniedException.class)
    public void testBeforeDeleteNodeThrowsExceptionForActiveContentWithoutReadPermission()
    {
        NodeRef heldContent = generateNodeRef(TYPE_CONTENT);
        mockPoliciesForExistingHoldWithHeldItems(hold, heldContent);

        // mocks for held content
        when(mockedRecordService.isRecord(heldContent)).thenReturn(false);
        when(mockedRecordFolderService.isRecordFolder(heldContent)).thenReturn(false);
        when(mockedPermissionService.hasPermission(heldContent, PermissionService.READ)).thenReturn(AccessStatus.DENIED);
        when(mockedNodeService.getProperty(heldContent, ContentModel.PROP_NAME)).thenReturn("foo");

        holdService.beforeDeleteNode(hold);
    }

    /**
     * test before delete node throws exception for failed read permission check for records
     */
    @Test (expected = AccessDeniedException.class)
    public void testBeforeDeleteNodeThrowsExceptionForARecordWithoutReadPermission()
    {
        NodeRef heldContent = generateNodeRef();
        mockPoliciesForExistingHoldWithHeldItems(hold, heldContent);

        when(mockedRecordService.isRecord(heldContent)).thenThrow(new AccessDeniedException(""));

        holdService.beforeDeleteNode(hold);
    }

    /**
     * test before delete node throws exception for failed file permission check for records
     */
    @Test (expected = AccessDeniedException.class)
    public void testBeforeDeleteNodeThrowsExceptionForARecordWithoutFilePermission()
    {
        NodeRef heldContent = generateNodeRef();

        mockPoliciesForExistingHoldWithHeldItems(hold, heldContent);

        // mocks for held record
        when(mockedRecordService.isRecord(heldContent)).thenReturn(true);
        when(mockedPermissionService.hasPermission(heldContent, RMPermissionModel.FILING)).thenReturn(AccessStatus.DENIED);
        when(mockedNodeService.getProperty(heldContent, ContentModel.PROP_NAME)).thenReturn("foo");

        holdService.beforeDeleteNode(hold);
    }

    /**
     * Test hold deleted for active content with read permission
     */
    @Test
    public void testDeleteHoldChecksReadPermissionForActiveContent()
    {
        NodeRef heldContent = generateNodeRef(TYPE_CONTENT);
        List<ChildAssociationRef> holds = createListOfHoldAssociations(heldContent);

        mockPoliciesForDeleteHold();
        when(mockedNodeService.getChildAssocs(hold, ASSOC_FROZEN_CONTENT, RegexQNamePattern.MATCH_ALL)).thenReturn(holds);
        when(mockedRecordService.isRecord(heldContent)).thenReturn(false);
        when(mockedRecordFolderService.isRecordFolder(heldContent)).thenReturn(false);
        when(mockedPermissionService.hasPermission(heldContent, PermissionService.READ)).thenReturn(AccessStatus.ALLOWED);
        when(mockedNodeService.getProperty(heldContent, ContentModel.PROP_NAME)).thenReturn("foo");

        holdService.deleteHold(hold);

        verify(mockedNodeService, times(1)).deleteNode(hold);
    }

    /**
     * Helper method to create hold and associations with given content
     */
    private List<ChildAssociationRef> createListOfHoldAssociations(NodeRef heldContent)
    {
        List<ChildAssociationRef> holds = new ArrayList<>(2);
        holds.add(new ChildAssociationRef(ASSOC_FROZEN_CONTENT, hold, ASSOC_FROZEN_CONTENT, heldContent, true, 1));
        return holds;
    }

    /**
     * mocks for existing hold with held items
     */
    private void mockPoliciesForExistingHoldWithHeldItems(NodeRef hold, NodeRef heldContent)
    {
        when(mockedNodeService.exists(hold)).thenReturn(true);
        when(holdService.isHold(hold)).thenReturn(true);

        List<ChildAssociationRef> holds = createListOfHoldAssociations(heldContent);
        when(mockedNodeService.getChildAssocs(hold, ASSOC_FROZEN_CONTENT, RegexQNamePattern.MATCH_ALL)).thenReturn(holds);
    }

    /**
     * mocks policies for create hold
     */
    private void mockPoliciesForCreateHold()
    {
        doNothing().when(holdService).invokeBeforeCreateHold(any(), anyString(), anyString());
        doNothing().when(holdService).invokeOnCreateHold(any());
    }

    /**
     * mocks policies for delete hold
     */
    private void mockPoliciesForDeleteHold()
    {
        doNothing().when(holdService).invokeBeforeDeleteHold(any());
        doNothing().when(holdService).invokeOnDeleteHold(any(), any());
    }

    /**
     * mocks policies for add to hold
     */
    private void mockPoliciesForAddToHold()
    {
        doNothing().when(holdService).invokeBeforeAddToHold(any(), any());
        doNothing().when(holdService).invokeOnAddToHold(any(), any());
    }

    /**
     * mocks policies for remove from hold
     */
    private void mockPoliciesForRemoveFromHold()
    {
        doNothing().when(holdService).invokeBeforeRemoveFromHold(any(), any());
        doNothing().when(holdService).invokeOnRemoveFromHold(any(), any());
    }
}
