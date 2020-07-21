/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.event.v1.model.EventData;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.PeerAssociationResource;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;

/**
 * @author Chris Shields
 */
public class PeerAssociationRepoEventIT extends AbstractContextAwareRepoEvent
{

    @Test
    public void testAddPeerAssociation()
    {
        final NodeRef content1NodeRef = createNode(ContentModel.TYPE_CONTENT);
        final NodeRef content2NodeRef = createNode(ContentModel.TYPE_CONTENT);

        checkNumOfEvents(2);

        RepoEvent<EventData<NodeResource>> resultRepoEvent = getRepoEventWithoutWait(1);
        assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(), resultRepoEvent.getType());

        resultRepoEvent = getRepoEventWithoutWait(2);
        assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(), resultRepoEvent.getType());
        
        retryingTransactionHelper.doInTransaction(() ->
                nodeService.createAssociation(
                        content1NodeRef,
                        content2NodeRef,
                        ContentModel.ASSOC_ORIGINAL));

        List<AssociationRef> peerAssociationRefs = retryingTransactionHelper.doInTransaction(
                () ->
                        nodeService.getSourceAssocs(content2NodeRef, ContentModel.ASSOC_ORIGINAL));
        assertEquals(1, peerAssociationRefs.size());

        checkNumOfEvents(4);

        final RepoEvent<EventData<PeerAssociationResource>> peerAssocRepoEvent = getFilteredEvent(EventType.PEER_ASSOC_CREATED,0);

        assertEquals("Wrong repo event type.",
                EventType.PEER_ASSOC_CREATED.getType(),
                peerAssocRepoEvent.getType());
        assertNotNull("Repo event ID is not available. ", peerAssocRepoEvent.getId());
        assertNotNull("Source is not available", peerAssocRepoEvent.getSource());
        assertEquals("Repo event source is not available. ",
                "/" + descriptorService.getCurrentRepositoryDescriptor().getId(),
                peerAssocRepoEvent.getSource().toString());
        assertNotNull("Repo event creation time is not available. ", peerAssocRepoEvent.getTime());
        assertEquals("Repo event datacontenttype", "application/json",
                peerAssocRepoEvent.getDatacontenttype());
        assertNotNull(peerAssocRepoEvent.getDataschema());
        assertEquals(EventJSONSchema.PEER_ASSOC_CREATED_V1.getSchema(), peerAssocRepoEvent.getDataschema());

        final EventData<PeerAssociationResource> nodeResourceEventData = getEventData(peerAssocRepoEvent);
        // EventData attributes
        assertNotNull("Event data group ID is not available. ", nodeResourceEventData.getEventGroupId());
        assertNull("resourceBefore property is not available", nodeResourceEventData.getResourceBefore());

        final PeerAssociationResource peerAssociationResource = getPeerAssocResource(peerAssocRepoEvent);
        assertEquals("Wrong source", content1NodeRef.getId(), peerAssociationResource.getSource().getId());
        assertEquals("Wrong target", content2NodeRef.getId(), peerAssociationResource.getTarget().getId());
        assertEquals("Wrong assoc type", "cm:original", peerAssociationResource.getAssocType());
    }

    @Test
    public void testAddMultiplePeerAssociationSameTransaction()
    {
        final NodeRef content1NodeRef = createNode(ContentModel.TYPE_CONTENT);
        final NodeRef content2NodeRef = createNode(ContentModel.TYPE_CONTENT);
        final NodeRef content3NodeRef = createNode(ContentModel.TYPE_CONTENT);

        checkNumOfEvents(3);

        RepoEvent<EventData<NodeResource>> resultRepoEvent = getRepoEventWithoutWait(1);
        assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(), resultRepoEvent.getType());

        resultRepoEvent = getRepoEventWithoutWait(2);
        assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(), resultRepoEvent.getType());

        resultRepoEvent = getRepoEventWithoutWait(3);
        assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(), resultRepoEvent.getType());

        retryingTransactionHelper.doInTransaction(() -> {
                    nodeService.createAssociation(
                            content1NodeRef,
                            content2NodeRef,
                            ContentModel.ASSOC_ORIGINAL);

                    nodeService.createAssociation(
                            content3NodeRef,
                            content2NodeRef,
                            ContentModel.ASSOC_ORIGINAL);
                    return null;
                });


        List<AssociationRef> peerAssociationRefs = retryingTransactionHelper.doInTransaction(() ->
                nodeService.getSourceAssocs(content2NodeRef, ContentModel.ASSOC_ORIGINAL));

        assertEquals(2, peerAssociationRefs.size());

        checkNumOfEvents(7);

        List<RepoEvent<EventData<PeerAssociationResource>>> peerAssocRepoEvent = getFilteredEvents(EventType.PEER_ASSOC_CREATED);

        // we should have 2 assoc.peer.Created events
        assertEquals("Wrong association events number",2, peerAssocRepoEvent.size());
    }

    @Test
    public void testAddMultiplePeerAssociationDifferentTransaction()
    {
        final NodeRef content1NodeRef = createNode(ContentModel.TYPE_CONTENT);
        final NodeRef content2NodeRef = createNode(ContentModel.TYPE_CONTENT);
        final NodeRef content3NodeRef = createNode(ContentModel.TYPE_CONTENT);

        checkNumOfEvents(3);

        RepoEvent<EventData<NodeResource>> resultRepoEvent = getRepoEventWithoutWait(1);
        assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(), resultRepoEvent.getType());

        resultRepoEvent = getRepoEventWithoutWait(2);
        assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(), resultRepoEvent.getType());

        resultRepoEvent = getRepoEventWithoutWait(3);
        assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(), resultRepoEvent.getType());

        retryingTransactionHelper.doInTransaction(() -> {
            nodeService.createAssociation(
                    content1NodeRef,
                    content2NodeRef,
                    ContentModel.ASSOC_ORIGINAL);
            return null;
        });

        retryingTransactionHelper.doInTransaction(() -> {
            nodeService.createAssociation(
                    content3NodeRef,
                    content2NodeRef,
                    ContentModel.ASSOC_ORIGINAL);
            return null;
        });

        List<AssociationRef> peerAssociationRefs = retryingTransactionHelper.doInTransaction(() ->
                nodeService.getSourceAssocs(content2NodeRef, ContentModel.ASSOC_ORIGINAL));

        assertEquals(2, peerAssociationRefs.size());

        checkNumOfEvents(7);

        List<RepoEvent<EventData<PeerAssociationResource>>> peerAssocRepoEvent = getFilteredEvents(EventType.PEER_ASSOC_CREATED);

        // we should have 2 assoc.peer.Created events
        assertEquals("Wrong association events number",2, peerAssocRepoEvent.size());

        assertEquals("Wrong source",
                content1NodeRef.getId(),
                getPeerAssocResource(peerAssocRepoEvent.get(0)).getSource().getId());
        assertEquals("Wrong target",
                content2NodeRef.getId(),
                getPeerAssocResource(peerAssocRepoEvent.get(0)).getTarget().getId());
        assertEquals("Wrong assoc type",
                "cm:original",
                getPeerAssocResource(peerAssocRepoEvent.get(0)).getAssocType());

        assertEquals("Wrong source",
                content3NodeRef.getId(),
                getPeerAssocResource(peerAssocRepoEvent.get(1)).getSource().getId());
        assertEquals("Wrong target",
                content2NodeRef.getId(),
                getPeerAssocResource(peerAssocRepoEvent.get(1)).getTarget().getId());
        assertEquals("Wrong assoc type",
                "cm:original",
                getPeerAssocResource(peerAssocRepoEvent.get(1)).getAssocType());
    }


    @Test
    public void testRemovePeerAssociation()
    {
        final NodeRef content1NodeRef = createNode(ContentModel.TYPE_CONTENT);
        final NodeRef content2NodeRef = createNode(ContentModel.TYPE_CONTENT);

        checkNumOfEvents(2);

        RepoEvent<EventData<NodeResource>> resultRepoEvent = getRepoEventWithoutWait(1);
        assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(), resultRepoEvent.getType());

        resultRepoEvent = getRepoEventWithoutWait(2);
        assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(), resultRepoEvent.getType());

        // Create peer association
        retryingTransactionHelper.doInTransaction(() ->
                nodeService.createAssociation(
                        content1NodeRef,
                        content2NodeRef,
                        ContentModel.ASSOC_ORIGINAL));
        
        List<AssociationRef> peerAssociationRefs = retryingTransactionHelper.doInTransaction(
                () ->
                        nodeService.getSourceAssocs(content2NodeRef, ContentModel.ASSOC_ORIGINAL));
        assertEquals(1, peerAssociationRefs.size());
        
        checkNumOfEvents(4);

        // Remove peer association
        retryingTransactionHelper.doInTransaction(() -> 
        {
                nodeService.removeAssociation(
                        content1NodeRef,
                        content2NodeRef,
                        ContentModel.ASSOC_ORIGINAL);
                return null;
        });
        
        peerAssociationRefs = retryingTransactionHelper.doInTransaction(
                () ->
                        nodeService.getSourceAssocs(content2NodeRef, ContentModel.ASSOC_ORIGINAL));
        assertEquals(0, peerAssociationRefs.size());

        checkNumOfEvents(6);
        
        // Check the peer assoc created event
        final RepoEvent<EventData<PeerAssociationResource>> peerAssocRepoEvent = getFilteredEvent(EventType.PEER_ASSOC_CREATED, 0);

        assertEquals("Wrong repo event type.",
                EventType.PEER_ASSOC_CREATED.getType(),
                peerAssocRepoEvent.getType());
        assertNotNull("Repo event ID is not available. ", peerAssocRepoEvent.getId());
        assertNotNull("Source is not available", peerAssocRepoEvent.getSource());
        assertEquals("Repo event source is not available. ",
                "/" + descriptorService.getCurrentRepositoryDescriptor().getId(),
                peerAssocRepoEvent.getSource().toString());
        assertNotNull("Repo event creation time is not available. ", peerAssocRepoEvent.getTime());
        assertEquals("Repo event datacontenttype", "application/json",
                peerAssocRepoEvent.getDatacontenttype());
        assertNotNull(peerAssocRepoEvent.getDataschema());
        assertEquals(EventJSONSchema.PEER_ASSOC_CREATED_V1.getSchema(), peerAssocRepoEvent.getDataschema());

        final EventData<PeerAssociationResource> nodeResourceEventData = getEventData(peerAssocRepoEvent);
        // EventData attributes
        assertNotNull("Event data group ID is not available. ",
                nodeResourceEventData.getEventGroupId());
        assertNull("resourceBefore property is not available",
                nodeResourceEventData.getResourceBefore());

        final PeerAssociationResource peerAssociationResource = getPeerAssocResource(
                peerAssocRepoEvent);
        assertEquals("Wrong source", content1NodeRef.getId(),
                peerAssociationResource.getSource().getId());
        assertEquals("Wrong target", content2NodeRef.getId(),
                peerAssociationResource.getTarget().getId());
        assertEquals("Wrong assoc type", "cm:original",
                peerAssociationResource.getAssocType());

        // Check the peer assoc deleted event
        final RepoEvent<EventData<PeerAssociationResource>> peerAssocRepoEvent2 = getFilteredEvent(EventType.PEER_ASSOC_DELETED, 0);

        assertEquals("Wrong repo event type.",
                EventType.PEER_ASSOC_DELETED.getType(),
                peerAssocRepoEvent2.getType());
        assertNotNull("Repo event ID is not available. ", peerAssocRepoEvent2.getId());
        assertNotNull("Source is not available", peerAssocRepoEvent2.getSource());
        assertEquals("Repo event source is not available. ",
                "/" + descriptorService.getCurrentRepositoryDescriptor().getId(),
                peerAssocRepoEvent2.getSource().toString());
        assertNotNull("Repo event creation time is not available. ", peerAssocRepoEvent2.getTime());
        assertEquals("Repo event datacontenttype", "application/json",
                peerAssocRepoEvent2.getDatacontenttype());
        assertNotNull(peerAssocRepoEvent2.getDataschema());
        assertEquals(EventJSONSchema.PEER_ASSOC_DELETED_V1.getSchema(), peerAssocRepoEvent2.getDataschema());

        final EventData<PeerAssociationResource> nodeResourceEventData2 = getEventData(peerAssocRepoEvent2);
        // EventData attributes
        assertNotNull("Event data group ID is not available. ",
                nodeResourceEventData2.getEventGroupId());
        assertNull("resourceBefore property is not available",
                nodeResourceEventData2.getResourceBefore());

        final PeerAssociationResource peerAssociationResource2 = getPeerAssocResource(
                peerAssocRepoEvent2);
        assertEquals("Wrong source", content1NodeRef.getId(),
                peerAssociationResource2.getSource().getId());
        assertEquals("Wrong target", content2NodeRef.getId(),
                peerAssociationResource2.getTarget().getId());
        assertEquals("Wrong assoc type", "cm:original",
                peerAssociationResource2.getAssocType());
    }


    @Test
    public void testAddAndRemovePeerAssociationSameTransaction()
    {
        final NodeRef content1NodeRef = createNode(ContentModel.TYPE_CONTENT);
        final NodeRef content2NodeRef = createNode(ContentModel.TYPE_CONTENT);

        checkNumOfEvents(2);

        RepoEvent<EventData<NodeResource>> resultRepoEvent = getRepoEventWithoutWait(1);
        assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(),
                resultRepoEvent.getType());

        resultRepoEvent = getRepoEventWithoutWait(2);
        assertEquals("Wrong repo event type.", EventType.NODE_CREATED.getType(),
                resultRepoEvent.getType());

        // Create peer association
        retryingTransactionHelper.doInTransaction(() ->
                
                {
                    nodeService.createAssociation(
                            content1NodeRef,
                            content2NodeRef,
                            ContentModel.ASSOC_ORIGINAL);

                    nodeService.removeAssociation(
                            content1NodeRef,
                            content2NodeRef,
                            ContentModel.ASSOC_ORIGINAL);
                    return null;
                });
        
        List<AssociationRef> peerAssociationRefs = retryingTransactionHelper.doInTransaction(
                () ->
                        nodeService.getSourceAssocs(content2NodeRef, ContentModel.ASSOC_ORIGINAL));
        assertEquals(0, peerAssociationRefs.size());

        checkNumOfEvents(2);
    }
}
