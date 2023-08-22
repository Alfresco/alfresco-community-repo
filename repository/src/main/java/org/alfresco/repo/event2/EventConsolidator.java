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

import java.util.ArrayDeque;
import java.util.Deque;

import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.EventData;
import org.alfresco.repo.event.v1.model.EventType;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.repo.event.v1.model.Resource;
import org.alfresco.service.cmr.repository.EntityRef;
import org.alfresco.service.namespace.QName;

/**
 * Encapsulates events occurred in a single transaction.
 *
 * @param <REF> entity (e.g. node, child association, peer association) reference type
 * @param <RES> entity resource type
 */
public abstract class EventConsolidator<REF extends EntityRef, RES extends Resource>
{
    protected final Deque<EventType> eventTypes;
    protected final NodeResourceHelper helper;
    protected REF entityReference;
    protected RES resource;

    public EventConsolidator(final REF entityReference, final NodeResourceHelper nodeResourceHelper)
    {
        this.eventTypes = new ArrayDeque<>();
        this.entityReference = entityReference;
        this.helper = nodeResourceHelper;
    }

    /**
     * Get entity (e.g. node, peer association, child association) type.
     *
     * @return QName the peer association type
     */
    public abstract QName getEntityType();

    /**
     * Whether the entity has been created and then deleted, e.g. a temporary node.
     *
     * @return {@code true} if the node has been created and then deleted, otherwise false
     */
    public abstract boolean isTemporaryEntity();

    /**
     * Get a derived event for a transaction.
     *
     * @return a derived event type
     */
    protected abstract EventType getDerivedEvent();

    /**
     * Get event types.
     *
     * @return Deque<EventType> queue of event types
     */
    public Deque<EventType> getEventTypes()
    {
        return eventTypes;
    }

    /**
     * Builds and returns the {@link RepoEvent} instance.
     *
     * @param eventInfo the object holding the event information
     * @return the {@link RepoEvent} instance
     */
    public RepoEvent<DataAttributes<RES>> getRepoEvent(EventInfo eventInfo)
    {
        EventType eventType = getDerivedEvent();

        DataAttributes<RES> eventData = buildEventData(eventInfo, resource, eventType);

        return RepoEvent.<DataAttributes<RES>>builder()
            .setId(eventInfo.getId())
            .setSource(eventInfo.getSource())
            .setTime(eventInfo.getTimestamp())
            .setType(eventType.getType())
            .setData(eventData)
            .setDataschema(EventJSONSchema.getSchemaV1(eventType))
            .build();
    }

    /**
     * Provides primary event data.
     */
    protected DataAttributes<RES> buildEventData(EventInfo eventInfo, RES resource, EventType eventType)
    {
        return EventData.<RES>builder()
            .setEventGroupId(eventInfo.getTxnId())
            .setResource(resource)
            .build();
    }
}
