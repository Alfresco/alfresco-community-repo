/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.module.org_alfresco_module_rm.hold;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
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
import org.alfresco.module.org_alfresco_module_rm.BaseUnitTest;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;


/**
 * Hold service implementation unit test
 *
 * @author Roy Wetherall
 */
public class HoldServiceImplUnitTest extends BaseUnitTest
{
    /** test values */
    private static final String HOLD_NAME = "holdname";
    private static final String HOLD_REASON = "holdreason";
    private static final String HOLD_DESCRIPTION = "holddescription";

    protected NodeRef holdContainer;
    protected NodeRef hold;
    protected NodeRef hold2;    

    @Spy @InjectMocks HoldServiceImpl holdService;

    @Before
    @Override
    public void before()
    {
        super.before();

        // setup objects used in mock interactions
        holdContainer = generateNodeRef(TYPE_HOLD_CONTAINER);
        hold = generateNodeRef(TYPE_HOLD);
        hold2 = generateNodeRef(TYPE_HOLD);

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
    public void getHolds()
    {
        // with no holds
        List<NodeRef> emptyHoldList = holdService.getHolds(filePlan);
        verify(mockedNodeService).getChildAssocs(holdContainer, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
        assertNotNull(emptyHoldList);
        assertTrue(emptyHoldList.isEmpty());

        // setup holds (hold1 and hold 2)
        setupHolds();

        // with 2 holds
        List<NodeRef> holdsList = holdService.getHolds(filePlan);
        verify(mockedNodeService, times(2)).getChildAssocs(holdContainer, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
        assertNotNull(holdsList);
        assertEquals(2, holdsList.size());

        // check one of the holds
        NodeRef holdFromList = holdsList.get(0);
        assertEquals(TYPE_HOLD, mockedNodeService.getType(holdFromList));
    }

    private void setupHolds()
    {
        // set up list of two holds
        List<ChildAssociationRef> list = new ArrayList<ChildAssociationRef>(2);
        list.add(new ChildAssociationRef(generateQName(), holdContainer, generateQName(), hold, true, 1));
        list.add(new ChildAssociationRef(generateQName(), holdContainer, generateQName(), hold2, true, 2));
        when(mockedNodeService.getChildAssocs(holdContainer, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL)).thenReturn(list);
    }

    @Test
    public void heldByNothing()
    {
        // setup interactions
        doReturn(new ArrayList<ChildAssociationRef>()).when(mockedNodeService).getParentAssocs(recordFolder, ASSOC_FROZEN_RECORDS, ASSOC_FROZEN_RECORDS);
        setupHolds();

        // check that the record folder isn't held by anything
        assertTrue(holdService.heldBy(recordFolder, true).isEmpty());
        List<NodeRef> holds = holdService.heldBy(recordFolder, false);
        assertEquals(2, holds.size());

        // check that the record isn't held by anything (record is a child of the record folder)
        assertTrue(holdService.heldBy(record, true).isEmpty());
        holds = holdService.heldBy(record, false);
        assertEquals(2, holds.size());
    }

    @Test
    public void heldByMultipleResults()
    {
        // setup record folder in multiple holds
        List<ChildAssociationRef> holds = new ArrayList<ChildAssociationRef>(2);
        holds.add(new ChildAssociationRef(ASSOC_FROZEN_RECORDS, hold, ASSOC_FROZEN_RECORDS, recordFolder, true, 1));
        holds.add(new ChildAssociationRef(ASSOC_FROZEN_RECORDS, hold2, ASSOC_FROZEN_RECORDS, recordFolder, true, 2));
        doReturn(holds).when(mockedNodeService).getParentAssocs(recordFolder, ASSOC_FROZEN_RECORDS, ASSOC_FROZEN_RECORDS);

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
        List<ChildAssociationRef> holds = new ArrayList<ChildAssociationRef>(1);
        holds.add(new ChildAssociationRef(ASSOC_FROZEN_RECORDS, hold, ASSOC_FROZEN_RECORDS, recordFolder, true, 1));
        doReturn(holds).when(mockedNodeService).getChildAssocs(hold, ASSOC_FROZEN_RECORDS, RegexQNamePattern.MATCH_ALL);

        List<NodeRef> list = holdService.getHeld(hold);
        assertEquals(1, list.size());
        assertEquals(recordFolder, list.get(0));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void createHold()
    {
        // setup node service interactions
        when(mockedNodeService.createNode(eq(holdContainer), eq(ContentModel.ASSOC_CONTAINS), any(QName.class) , eq(TYPE_HOLD), any(Map.class)))
            .thenReturn(new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, holdContainer, generateQName(), hold));

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
        // delete hold
        holdService.deleteHold(hold);
        verify(mockedNodeService).deleteNode(hold);

        // TODO check interactions with policy component!!!
    }

    @Test (expected=AlfrescoRuntimeException.class)
    public void addToHoldNotAHold()
    {
        holdService.addToHold(recordFolder, recordFolder);
    }

    @Test (expected=AlfrescoRuntimeException.class)
    public void addToHoldNotARecordFolderOrRecord()
    {
        holdService.addToHold(hold, filePlanComponent);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void addToHoldNotInHold()
    {
        holdService.addToHold(hold, recordFolder);

        verify(mockedNodeService).addChild(hold, recordFolder, ASSOC_FROZEN_RECORDS, ASSOC_FROZEN_RECORDS);
        verify(mockedNodeService).addAspect(eq(recordFolder), eq(ASPECT_FROZEN), any(Map.class));
        verify(mockedNodeService).addAspect(eq(record), eq(ASPECT_FROZEN), any(Map.class));

        holdService.addToHold(hold, record);
        verify(mockedNodeService).addChild(hold, record, ASSOC_FROZEN_RECORDS, ASSOC_FROZEN_RECORDS);
        verify(mockedNodeService).addAspect(eq(recordFolder), eq(ASPECT_FROZEN), any(Map.class));
        verify(mockedNodeService, times(2)).addAspect(eq(record), eq(ASPECT_FROZEN), any(Map.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void addToHoldAlreadyInHold()
    {
        doReturn(Collections.singletonList(recordFolder)).when(holdService).getHeld(hold);

        holdService.addToHold(hold, recordFolder);

        verify(mockedNodeService, never()).addChild(hold, recordFolder, ASSOC_FROZEN_RECORDS, ASSOC_FROZEN_RECORDS);
        verify(mockedNodeService, never()).addAspect(eq(recordFolder), eq(ASPECT_FROZEN), any(Map.class));
        verify(mockedNodeService, never()).addAspect(eq(record), eq(ASPECT_FROZEN), any(Map.class));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void addToHoldAldeadyFrozen()
    {
        doReturn(true).when(mockedNodeService).hasAspect(recordFolder, ASPECT_FROZEN);
        doReturn(true).when(mockedNodeService).hasAspect(record, ASPECT_FROZEN);

        holdService.addToHold(hold, recordFolder);

        verify(mockedNodeService, times(1)).addChild(hold, recordFolder, ASSOC_FROZEN_RECORDS, ASSOC_FROZEN_RECORDS);
        verify(mockedNodeService, never()).addAspect(eq(recordFolder), eq(ASPECT_FROZEN), any(Map.class));
        verify(mockedNodeService, never()).addAspect(eq(record), eq(ASPECT_FROZEN), any(Map.class));
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

        // build a list of holds
        List<NodeRef> holds = new ArrayList<NodeRef>(2);
        holds.add(hold);
        holds.add(hold2);

        // add the record folder to both holds
        holdService.addToHolds(holds, recordFolder);

        // verify the interactions
        verify(mockedNodeService, times(1)).addChild(hold, recordFolder, ASSOC_FROZEN_RECORDS, ASSOC_FROZEN_RECORDS);
        verify(mockedNodeService, times(1)).addChild(hold2, recordFolder, ASSOC_FROZEN_RECORDS, ASSOC_FROZEN_RECORDS);
        verify(mockedNodeService, times(1)).addAspect(eq(recordFolder), eq(ASPECT_FROZEN), any(Map.class));
        verify(mockedNodeService, times(1)).addAspect(eq(record), eq(ASPECT_FROZEN), any(Map.class));
    }

    @Test (expected=AlfrescoRuntimeException.class)
    public void removeFromHoldNotAHold()
    {
        holdService.removeFromHold(recordFolder, recordFolder);
    }

    @Test
    public void removeFromHoldNotInHold()
    {
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

        // build a list of holds
        List<NodeRef> holds = new ArrayList<NodeRef>(2);
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
        List<NodeRef> holds = new ArrayList<NodeRef>(2);
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
}
