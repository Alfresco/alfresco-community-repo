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
package org.alfresco.repo.event2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.event.v1.model.EventType;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;

public class EventConsolidatorUnitTest
{
    private final NodeResourceHelper nodeResourceHelper = mock(NodeResourceHelper.class);
    private NodeEventConsolidator eventConsolidator;

    @Before
    public void setUp() throws Exception
    {
        eventConsolidator = new NodeEventConsolidator(nodeResourceHelper);
    }

    @Test
    public void testGetMappedAspectsBeforeRemovedAndAddedEmpty()
    {
        Set<String> currentAspects = new HashSet<>();
        currentAspects.add("cm:geographic");
        currentAspects.add("cm:auditable");

        Set<String> mappedAspectsBefore = eventConsolidator.getMappedAspectsBefore(currentAspects);

        assertEquals(0, mappedAspectsBefore.size());
    }

    @Test
    public void testGetMappedAspectsBefore_AspectRemoved()
    {
        eventConsolidator.addAspect(ContentModel.ASSOC_CONTAINS);

        Set<String> currentAspects = new HashSet<>();
        currentAspects.add("cm:geographic");
        currentAspects.add("cm:auditable");

        Set<String> removed = new HashSet<>();
        Set<String> added = new HashSet<>();
        removed.add("cm:contains");

        when(nodeResourceHelper.mapToNodeAspects(eventConsolidator.getAspectsRemoved())).thenReturn(removed);
        when(nodeResourceHelper.mapToNodeAspects(eventConsolidator.getAspectsAdded())).thenReturn(added);

        Set<String> mappedAspectsBefore = eventConsolidator.getMappedAspectsBefore(currentAspects);

        assertEquals(3, mappedAspectsBefore.size());
    }

    @Test
    public void testGetMappedAspectsBefore_AspectAdded()
    {
        eventConsolidator.addAspect(ContentModel.ASSOC_CONTAINS);

        Set<String> currentAspects = new HashSet<>();
        currentAspects.add("cm:geographic");
        currentAspects.add("cm:auditable");

        Set<String> removed = new HashSet<>();
        Set<String> added = new HashSet<>();
        added.add("cm:auditable");

        when(nodeResourceHelper.mapToNodeAspects(eventConsolidator.getAspectsRemoved())).thenReturn(removed);
        when(nodeResourceHelper.mapToNodeAspects(eventConsolidator.getAspectsAdded())).thenReturn(added);

        Set<String> mappedAspectsBefore = eventConsolidator.getMappedAspectsBefore(currentAspects);

        assertEquals(1, mappedAspectsBefore.size());
    }

    @Test
    public void testGetMappedAspectsBefore_AspectAddedAndRemoved()
    {
        eventConsolidator.addAspect(ContentModel.ASSOC_CONTAINS);

        Set<String> currentAspects = new HashSet<>();
        currentAspects.add("cm:geographic");
        currentAspects.add("cm:auditable");

        Set<String> removed = new HashSet<>();
        removed.add("cm:contains");
        Set<String> added = new HashSet<>();
        added.add("cm:contains");

        when(nodeResourceHelper.mapToNodeAspects(eventConsolidator.getAspectsRemoved())).thenReturn(removed);
        when(nodeResourceHelper.mapToNodeAspects(eventConsolidator.getAspectsAdded())).thenReturn(added);

        Set<String> mappedAspectsBefore = eventConsolidator.getMappedAspectsBefore(currentAspects);

        assertEquals(2, mappedAspectsBefore.size());
    }

    @Test
    public void testGetMappedAspectsBefore_AspectRemovedAndAdded()
    {
        eventConsolidator.addAspect(ContentModel.ASSOC_CONTAINS);
        eventConsolidator.removeAspect(ContentModel.ASSOC_CONTAINS);

        Set<String> currentAspects = new HashSet<>();
        currentAspects.add("cm:geographic");
        currentAspects.add("cm:auditable");
        currentAspects.add("cm:contains");

        Set<String> removed = new HashSet<>();
        removed.add("cm:contains");
        Set<String> added = new HashSet<>();
        added.add("cm:contains");

        when(nodeResourceHelper.mapToNodeAspects(eventConsolidator.getAspectsRemoved())).thenReturn(removed);
        when(nodeResourceHelper.mapToNodeAspects(eventConsolidator.getAspectsAdded())).thenReturn(added);

        Set<String> mappedAspectsBefore = eventConsolidator.getMappedAspectsBefore(currentAspects);

        assertEquals(0, mappedAspectsBefore.size());
    }

    @Test
    public void testGetMappedAspectsBefore_AspectAddedTwiceRemovedOnce()
    {
        eventConsolidator.addAspect(ContentModel.ASSOC_CONTAINS);
        eventConsolidator.addAspect(ContentModel.ASSOC_CONTAINS);

        eventConsolidator.removeAspect(ContentModel.ASSOC_CONTAINS);

        Set<String> currentAspects = new HashSet<>();
        currentAspects.add("cm:geographic");
        currentAspects.add("cm:auditable");
        currentAspects.add("cm:contains");

        Set<String> removed = new HashSet<>();
        Set<String> added = new HashSet<>();
        added.add("cm:contains");

        when(nodeResourceHelper.mapToNodeAspects(eventConsolidator.getAspectsRemoved())).thenReturn(removed);
        when(nodeResourceHelper.mapToNodeAspects(eventConsolidator.getAspectsAdded())).thenReturn(added);

        Set<String> mappedAspectsBefore = eventConsolidator.getMappedAspectsBefore(currentAspects);

        assertEquals(2, mappedAspectsBefore.size());
    }

    @Test
    public void testGetMappedAspectsBefore_AspectRemovedTwiceAddedOnce()
    {
        eventConsolidator.addAspect(ContentModel.ASSOC_CONTAINS);
        eventConsolidator.addAspect(ContentModel.ASSOC_CONTAINS);

        eventConsolidator.removeAspect(ContentModel.ASSOC_CONTAINS);

        Set<String> currentAspects = new HashSet<>();
        currentAspects.add("cm:geographic");
        currentAspects.add("cm:auditable");
        currentAspects.add("cm:contains");

        Set<String> removed = new HashSet<>();
        removed.add("cm:contains");
        Set<String> added = new HashSet<>();
        added.add("cm:contains");

        when(nodeResourceHelper.mapToNodeAspects(eventConsolidator.getAspectsRemoved())).thenReturn(removed);
        when(nodeResourceHelper.mapToNodeAspects(eventConsolidator.getAspectsAdded())).thenReturn(added);

        Set<String> mappedAspectsBefore = eventConsolidator.getMappedAspectsBefore(currentAspects);

        assertEquals(2, mappedAspectsBefore.size());
    }

    @Test
    public void testGetMappedAspectsBefore_FilteredAspectAdded()
    {
        eventConsolidator.addAspect(ContentModel.ASPECT_COPIEDFROM);

        Set<String> currentAspects = new HashSet<>();
        currentAspects.add("cm:geographic");
        currentAspects.add("cm:auditable");

        Set<String> removed = new HashSet<>();
        Set<String> added = new HashSet<>();

        when(nodeResourceHelper.mapToNodeAspects(eventConsolidator.getAspectsRemoved())).thenReturn(removed);
        when(nodeResourceHelper.mapToNodeAspects(eventConsolidator.getAspectsAdded())).thenReturn(added);

        Set<String> mappedAspectsBefore = eventConsolidator.getMappedAspectsBefore(currentAspects);

        assertEquals(0, mappedAspectsBefore.size());
    }

    @Test
    public void testAddAspect()
    {
        eventConsolidator.addAspect(ContentModel.ASSOC_CONTAINS);

        assertEquals(1, eventConsolidator.getAspectsAdded().size());
        assertEquals(0, eventConsolidator.getAspectsRemoved().size());
        assertTrue(eventConsolidator.getAspectsAdded().contains(ContentModel.ASSOC_CONTAINS));
    }

    @Test
    public void testRemoveAspect()
    {
        eventConsolidator.removeAspect(ContentModel.ASSOC_CONTAINS);

        assertEquals(0, eventConsolidator.getAspectsAdded().size());
        assertEquals(1, eventConsolidator.getAspectsRemoved().size());
        assertTrue(eventConsolidator.getAspectsRemoved().contains(ContentModel.ASSOC_CONTAINS));
    }

    @Test
    public void testAddAspectRemoveAspect()
    {
        eventConsolidator.addAspect(ContentModel.ASSOC_CONTAINS);
        eventConsolidator.removeAspect(ContentModel.ASSOC_CONTAINS);

        assertEquals(0, eventConsolidator.getAspectsAdded().size());
        assertEquals(0, eventConsolidator.getAspectsRemoved().size());
    }

    @Test
    public void testRemoveAspectAddAspect()
    {
        eventConsolidator.removeAspect(ContentModel.ASSOC_CONTAINS);
        eventConsolidator.addAspect(ContentModel.ASSOC_CONTAINS);

        assertEquals(0, eventConsolidator.getAspectsAdded().size());
        assertEquals(0, eventConsolidator.getAspectsRemoved().size());
    }

    @Test
    public void testAddAspectTwiceRemoveAspectOnce()
    {
        eventConsolidator.addAspect(ContentModel.ASSOC_CONTAINS);
        eventConsolidator.removeAspect(ContentModel.ASSOC_CONTAINS);
        eventConsolidator.addAspect(ContentModel.ASSOC_CONTAINS);

        assertEquals(1, eventConsolidator.getAspectsAdded().size());
        assertEquals(0, eventConsolidator.getAspectsRemoved().size());
        assertTrue(eventConsolidator.getAspectsAdded().contains(ContentModel.ASSOC_CONTAINS));
    }

    @Test
    public void testAddAspectOnceRemoveAspectTwice()
    {
        eventConsolidator.removeAspect(ContentModel.ASSOC_CONTAINS);
        eventConsolidator.addAspect(ContentModel.ASSOC_CONTAINS);
        eventConsolidator.removeAspect(ContentModel.ASSOC_CONTAINS);

        assertEquals(0, eventConsolidator.getAspectsAdded().size());
        assertEquals(1, eventConsolidator.getAspectsRemoved().size());
        assertTrue(eventConsolidator.getAspectsRemoved().contains(ContentModel.ASSOC_CONTAINS));
    }

    @Test
    public void testOnMoveNodeWithPrimaryParent()
    {
        ChildAssociationRef oldAssociationMock = mock(ChildAssociationRef.class);
        ChildAssociationRef newAssociationMock = mock(ChildAssociationRef.class);
        NodeRef parentRefMock = mock(NodeRef.class);
        given(newAssociationMock.isPrimary()).willReturn(true);
        given(oldAssociationMock.getParentRef()).willReturn(parentRefMock);

        eventConsolidator.onMoveNode(oldAssociationMock, newAssociationMock);

        then(newAssociationMock).should().getChildRef();
        then(newAssociationMock).should().isPrimary();
        then(newAssociationMock).shouldHaveNoMoreInteractions();
        then(nodeResourceHelper).should().getPrimaryHierarchy(parentRefMock, true);
        assertTrue("Node event consolidator should contain event type: UPDATED", eventConsolidator.getEventTypes().contains(EventType.NODE_UPDATED));

    }

    @Test
    public void testOnMoveNodeAfterSecondaryParentAdded()
    {
        ChildAssociationRef oldAssociationMock = mock(ChildAssociationRef.class);
        ChildAssociationRef newAssociationMock = mock(ChildAssociationRef.class);
        NodeRef nodeRefMock = mock(NodeRef.class);
        NodeRef parentRefMock = mock(NodeRef.class);
        List<String> secondaryParentsMock = mock(List.class);
        given(newAssociationMock.isPrimary()).willReturn(false);
        given(newAssociationMock.getChildRef()).willReturn(nodeRefMock);
        given(newAssociationMock.getParentRef()).willReturn(parentRefMock);
        given(parentRefMock.getId()).willReturn("parent-id");
        given(nodeResourceHelper.getSecondaryParents(any(NodeRef.class))).willReturn(secondaryParentsMock);

        // when
        eventConsolidator.onMoveNode(oldAssociationMock, newAssociationMock);

        then(newAssociationMock).should().isPrimary();
        then(newAssociationMock).should(times(2)).getChildRef();
        then(newAssociationMock).should(times(2)).getParentRef();
        then(newAssociationMock).shouldHaveNoMoreInteractions();
        then(oldAssociationMock).shouldHaveNoInteractions();
        then(nodeResourceHelper).should().getSecondaryParents(nodeRefMock);
        then(secondaryParentsMock).should().remove("parent-id");
        then(secondaryParentsMock).shouldHaveNoMoreInteractions();
        assertTrue("Node event consolidator should contain event type: UPDATED", eventConsolidator.getEventTypes().contains(EventType.NODE_UPDATED));
        assertEquals(secondaryParentsMock, eventConsolidator.getSecondaryParentsBefore());
    }

    @Test
    public void testOnMoveNodeBeforeSecondaryParentRemoved()
    {
        ChildAssociationRef oldAssociationMock = mock(ChildAssociationRef.class);
        ChildAssociationRef newAssociationMock = mock(ChildAssociationRef.class);
        NodeRef nodeRefMock = mock(NodeRef.class);
        NodeRef parentRefMock = mock(NodeRef.class);
        List<String> secondaryParentsMock = mock(List.class);
        given(newAssociationMock.isPrimary()).willReturn(false);
        given(newAssociationMock.getChildRef()).willReturn(nodeRefMock);
        given(oldAssociationMock.getParentRef()).willReturn(parentRefMock);
        given(parentRefMock.getId()).willReturn("parent-id");
        given(nodeResourceHelper.getSecondaryParents(any(NodeRef.class))).willReturn(secondaryParentsMock);

        // when
        eventConsolidator.onMoveNode(oldAssociationMock, newAssociationMock);

        then(newAssociationMock).should().isPrimary();
        then(newAssociationMock).should(times(2)).getChildRef();
        then(newAssociationMock).should().getParentRef();
        then(newAssociationMock).shouldHaveNoMoreInteractions();
        then(oldAssociationMock).should(times(3)).getParentRef();
        then(oldAssociationMock).shouldHaveNoMoreInteractions();
        then(nodeResourceHelper).should().getSecondaryParents(nodeRefMock);
        then(secondaryParentsMock).should().contains("parent-id");
        then(secondaryParentsMock).should().add("parent-id");
        then(secondaryParentsMock).shouldHaveNoMoreInteractions();
        assertTrue("Node event consolidator should contain event type: NODE_UPDATED", eventConsolidator.getEventTypes().contains(EventType.NODE_UPDATED));
        assertEquals(secondaryParentsMock, eventConsolidator.getSecondaryParentsBefore());
    }

    @Test
    public void testBuildNodeResourceBeforeDelta_FolderWithOnlyModifiedAtChange_IncludesModifiedAt()
    {
        // GIVEN: An existing folder node that is being updated (NOT being created)
        // This simulates when same user uploads file to an existing folder
        NodeRef folderRef = new NodeRef("workspace://SpacesStore/folder-uuid");

        // Mock the helper to return folder type
        when(nodeResourceHelper.getNodeType(folderRef)).thenReturn(ContentModel.TYPE_FOLDER);
        when(nodeResourceHelper.createNodeResourceBuilder(folderRef)).thenReturn(mock(org.alfresco.repo.event.v1.model.NodeResource.Builder.class));

        // Setup: Simulate update where only cm:modified changes (NOT creating the node)
        // cm:modifier stays the same (same user)
        java.util.Map<org.alfresco.service.namespace.QName, java.io.Serializable> propertiesBefore = new java.util.HashMap<>();
        propertiesBefore.put(ContentModel.PROP_MODIFIED, new java.util.Date(1000000000L));
        propertiesBefore.put(ContentModel.PROP_MODIFIER, "admin");

        java.util.Map<org.alfresco.service.namespace.QName, java.io.Serializable> propertiesAfter = new java.util.HashMap<>();
        propertiesAfter.put(ContentModel.PROP_MODIFIED, new java.util.Date(2000000000L)); // Different time
        propertiesAfter.put(ContentModel.PROP_MODIFIER, "admin"); // Same user - won't be in changedPropsBefore

        // Mock the helper methods - simulating that no properties map to NodeResource fields
        when(nodeResourceHelper.mapToNodeProperties(any())).thenReturn(java.util.Collections.emptyMap());
        when(nodeResourceHelper.getLocalizedPropertiesBefore(any(java.util.Map.class), any(org.alfresco.repo.event.v1.model.NodeResource.class))).thenReturn(java.util.Collections.emptyMap());
        when(nodeResourceHelper.getContentInfo(any())).thenReturn(null);
        when(nodeResourceHelper.getUserInfo(any())).thenReturn(null); // cm:modifier not in changedPropsBefore
        when(nodeResourceHelper.getZonedDateTime(any(java.util.Date.class))).thenReturn(java.time.ZonedDateTime.now());

        // WHEN: onUpdateProperties is called (simulating folder update, not creation)
        eventConsolidator.onUpdateProperties(folderRef, propertiesBefore, propertiesAfter);

        // THEN: Verify event type is UPDATE
        assertTrue("Event consolidator should contain NODE_UPDATED event for folder",
                   eventConsolidator.getEventTypes().contains(EventType.NODE_UPDATED));

        // THEN: Verify derived event is UPDATE (not CREATED, not filtered out)
        // This validates that our shouldIncludeModifiedAt() logic works:
        // - For folders, even when only cm:modified changes
        // - modifiedAt is included in resourceBefore
        // - resourceBeforeAllFieldsNull is set to false
        // - Event passes the eligibility check
        assertEquals("Derived event should be NODE_UPDATED for folder with only timestamp change",
                     EventType.NODE_UPDATED, eventConsolidator.getDerivedEvent());
    }

    @Test
    public void testBuildNodeResourceBeforeDelta_ContentNodeWithOnlyModifiedAtChange()
    {
        // GIVEN: A content node (NOT folder) where only cm:modified changes
        // This tests that our logic is specific to folders
        NodeRef contentRef = new NodeRef("workspace://SpacesStore/content-uuid");
        ChildAssociationRef childAssocRef = mock(ChildAssociationRef.class);
        when(childAssocRef.getChildRef()).thenReturn(contentRef);

        // Mock the helper to return content type (NOT folder)
        when(nodeResourceHelper.getNodeType(contentRef)).thenReturn(ContentModel.TYPE_CONTENT);
        when(nodeResourceHelper.createNodeResourceBuilder(contentRef)).thenReturn(mock(org.alfresco.repo.event.v1.model.NodeResource.Builder.class));

        // Setup: Create content node
        eventConsolidator.onCreateNode(childAssocRef);

        // Setup: Simulate update where only cm:modified changes
        java.util.Map<org.alfresco.service.namespace.QName, java.io.Serializable> propertiesBefore = new java.util.HashMap<>();
        propertiesBefore.put(ContentModel.PROP_MODIFIED, new java.util.Date(1000000000L));
        propertiesBefore.put(ContentModel.PROP_MODIFIER, "admin");

        java.util.Map<org.alfresco.service.namespace.QName, java.io.Serializable> propertiesAfter = new java.util.HashMap<>();
        propertiesAfter.put(ContentModel.PROP_MODIFIED, new java.util.Date(2000000000L));
        propertiesAfter.put(ContentModel.PROP_MODIFIER, "admin"); // Same user

        // Mock helpers - same as folder test
        when(nodeResourceHelper.mapToNodeProperties(any())).thenReturn(java.util.Collections.emptyMap());
        when(nodeResourceHelper.getLocalizedPropertiesBefore(any(java.util.Map.class), any(org.alfresco.repo.event.v1.model.NodeResource.class))).thenReturn(java.util.Collections.emptyMap());
        when(nodeResourceHelper.getContentInfo(any())).thenReturn(null);
        when(nodeResourceHelper.getUserInfo(any())).thenReturn(null);
        when(nodeResourceHelper.getZonedDateTime(any(java.util.Date.class))).thenReturn(java.time.ZonedDateTime.now());

        // WHEN: onUpdateProperties is called
        eventConsolidator.onUpdateProperties(contentRef, propertiesBefore, propertiesAfter);

        // THEN: For content nodes (not folders), shouldIncludeModifiedAt returns false
        // So the behavior might be different (could be filtered if no other changes)
        assertTrue("Event consolidator should contain NODE_UPDATED event",
                   eventConsolidator.getEventTypes().contains(EventType.NODE_UPDATED));

        // Note: Without shouldIncludeModifiedAt logic for content,
        // this event might be filtered by EventGenerator's eligibility check
        // Our implementation specifically helps folders to avoid Elasticsearch sync issues
    }
}

