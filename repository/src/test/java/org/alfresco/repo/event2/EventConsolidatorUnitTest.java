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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.junit.Test;

public class EventConsolidatorUnitTest
{
    private NodeResourceHelper nodeResourceHelper = mock(NodeResourceHelper.class);
    
    @Test
    public void testGetMappedAspectsBeforeRemovedAndAddedEmpty()
    {
        NodeEventConsolidator eventConsolidator = new NodeEventConsolidator(nodeResourceHelper);
        
        Set<String> currentAspects = new HashSet<>();
        currentAspects.add("cm:geographic");
        currentAspects.add("cm:auditable");

        Set<String> mappedAspectsBefore = eventConsolidator.getMappedAspectsBefore(currentAspects);

        assertEquals(0, mappedAspectsBefore.size());
    }

    @Test
    public void testGetMappedAspectsBefore_AspectRemoved()
    {
        NodeEventConsolidator eventConsolidator = new NodeEventConsolidator(nodeResourceHelper);
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
        NodeEventConsolidator eventConsolidator = new NodeEventConsolidator(nodeResourceHelper);
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
        NodeEventConsolidator eventConsolidator = new NodeEventConsolidator(nodeResourceHelper);
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
        NodeEventConsolidator eventConsolidator = new NodeEventConsolidator(nodeResourceHelper);
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
        NodeEventConsolidator eventConsolidator = new NodeEventConsolidator(nodeResourceHelper);

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
        NodeEventConsolidator eventConsolidator = new NodeEventConsolidator(nodeResourceHelper);

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
        NodeEventConsolidator eventConsolidator = new NodeEventConsolidator(nodeResourceHelper);
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
        NodeEventConsolidator eventConsolidator = new NodeEventConsolidator(nodeResourceHelper);
        eventConsolidator.addAspect(ContentModel.ASSOC_CONTAINS);
        
        assertEquals(1, eventConsolidator.getAspectsAdded().size());
        assertEquals(0, eventConsolidator.getAspectsRemoved().size());
        assertTrue(eventConsolidator.getAspectsAdded().contains(ContentModel.ASSOC_CONTAINS));
    }

    @Test
    public void testRemoveAspect()
    {
        NodeEventConsolidator eventConsolidator = new NodeEventConsolidator(nodeResourceHelper);
        eventConsolidator.removeAspect(ContentModel.ASSOC_CONTAINS);

        assertEquals(0, eventConsolidator.getAspectsAdded().size());
        assertEquals(1, eventConsolidator.getAspectsRemoved().size());
        assertTrue(eventConsolidator.getAspectsRemoved().contains(ContentModel.ASSOC_CONTAINS));
    }

    @Test
    public void testAddAspectRemoveAspect()
    {
        NodeEventConsolidator eventConsolidator = new NodeEventConsolidator(nodeResourceHelper);
        eventConsolidator.addAspect(ContentModel.ASSOC_CONTAINS);
        eventConsolidator.removeAspect(ContentModel.ASSOC_CONTAINS);

        assertEquals(0, eventConsolidator.getAspectsAdded().size());
        assertEquals(0, eventConsolidator.getAspectsRemoved().size());
    }

    @Test
    public void testRemoveAspectAddAspect()
    {
        NodeEventConsolidator eventConsolidator = new NodeEventConsolidator(nodeResourceHelper);
        eventConsolidator.removeAspect(ContentModel.ASSOC_CONTAINS);
        eventConsolidator.addAspect(ContentModel.ASSOC_CONTAINS);

        assertEquals(0, eventConsolidator.getAspectsAdded().size());
        assertEquals(0, eventConsolidator.getAspectsRemoved().size());
    }

    @Test
    public void testAddAspectTwiceRemoveAspectOnce()
    {
        NodeEventConsolidator eventConsolidator = new NodeEventConsolidator(nodeResourceHelper);
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
        NodeEventConsolidator eventConsolidator = new NodeEventConsolidator(nodeResourceHelper);
        eventConsolidator.removeAspect(ContentModel.ASSOC_CONTAINS);
        eventConsolidator.addAspect(ContentModel.ASSOC_CONTAINS);
        eventConsolidator.removeAspect(ContentModel.ASSOC_CONTAINS);

        assertEquals(0, eventConsolidator.getAspectsAdded().size());
        assertEquals(1, eventConsolidator.getAspectsRemoved().size());
        assertTrue(eventConsolidator.getAspectsRemoved().contains(ContentModel.ASSOC_CONTAINS));
    }
}
