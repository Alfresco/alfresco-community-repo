/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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

import org.alfresco.repo.event.v1.model.EventType;
import org.alfresco.repo.event.v1.model.PeerAssociationResource;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.namespace.QName;

/**
 * Encapsulates peer association events occurred in a single transaction.
 *
 * @author Sara Aspery
 */
public class PeerAssociationEventConsolidator extends EventConsolidator<AssociationRef, PeerAssociationResource> implements PeerAssociationEventSupportedPolicies
{

    public PeerAssociationEventConsolidator(AssociationRef associationRef, NodeResourceHelper helper)
    {
        super(associationRef, helper);
    }

    /**
     * Add peer association created event on create of a peer association.
     *
     * @param associationRef AssociationRef
     */
    @Override
    public void onCreateAssociation(AssociationRef associationRef)
    {
        eventTypes.add(EventType.PEER_ASSOC_CREATED);
        resource = buildPeerAssociationResource(associationRef);
    }

    /**
     * Add peer association deleted event on delete of a peer association.
     *
     * @param associationRef AssociationRef
     */
    @Override
    public void beforeDeleteAssociation(AssociationRef associationRef)
    {
        eventTypes.add(EventType.PEER_ASSOC_DELETED);
        resource = buildPeerAssociationResource(associationRef);
    }

    private PeerAssociationResource buildPeerAssociationResource(AssociationRef associationRef)
    {
        String sourceId = associationRef.getSourceRef().getId();
        String targetId = associationRef.getTargetRef().getId();
        String assocType = helper.getQNamePrefixString(associationRef.getTypeQName());

        return new PeerAssociationResource(sourceId, targetId, assocType);
    }

    @Override
    protected EventType getDerivedEvent()
    {
        if (isTemporaryEntity())
        {
            // This event will be filtered out, but we set the correct
            // event type anyway for debugging purposes
            return EventType.PEER_ASSOC_DELETED;
        }
        else if (eventTypes.contains(EventType.PEER_ASSOC_CREATED))
        {
            return EventType.PEER_ASSOC_CREATED;
        }
        else if (eventTypes.getLast() == EventType.PEER_ASSOC_DELETED)
        {
            return EventType.PEER_ASSOC_DELETED;
        }
        else
        {
            // Default to first event
            return eventTypes.getFirst();
        }
    }

    @Override
    public boolean isTemporaryEntity()
    {
        return eventTypes.contains(EventType.PEER_ASSOC_CREATED) && eventTypes.getLast() == EventType.PEER_ASSOC_DELETED;
    }

    @Override
    public QName getEntityType()
    {
        return entityReference.getTypeQName();
    }
}

