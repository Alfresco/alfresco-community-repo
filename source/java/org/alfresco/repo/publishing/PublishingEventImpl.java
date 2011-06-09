/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

package org.alfresco.repo.publishing;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import org.alfresco.service.cmr.publishing.MutablePublishingEvent;
import org.alfresco.service.cmr.publishing.PublishingEvent;
import org.alfresco.service.cmr.publishing.PublishingPackage;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class PublishingEventImpl implements PublishingEvent
{
    private final String id;
    private final Status status;
    private final PublishingPackage publishingPackage;
    private final Date createdTime;
    private final String creator;
    private final Date modifiedTime;
    private final String modifier;
    private final Set<PublishingEvent> dependingEvents;
    private final Set<PublishingEvent> eventsDependedOn;
    private final Set<NodeRef> nodesDependedOn;
    protected final Calendar scheduledTime;
    protected String comment;
    
    
    public PublishingEventImpl(String id,
            Status status, PublishingPackage publishingPackage,
            Date createdTime,String creator,
            Date modifiedTime, String modifier,
            Set<PublishingEvent> dependingEvents,
            Set<PublishingEvent> eventsDependedOn,
            Set<NodeRef> nodesDependedOn,
            Calendar scheduledTime, String comment)
    {
        this.id = id;
        this.status = status;
        this.publishingPackage = publishingPackage;
        this.createdTime = createdTime;
        this.creator = creator;
        this.modifiedTime = modifiedTime;
        this.modifier = modifier;
        this.dependingEvents = Collections.unmodifiableSet(dependingEvents);
        this.eventsDependedOn = Collections.unmodifiableSet(eventsDependedOn);
        this.nodesDependedOn = Collections.unmodifiableSet(nodesDependedOn);
        this.scheduledTime = scheduledTime;
        this.comment = comment;
    }
    
    public PublishingEventImpl(PublishingEvent event)
    {
        this(event.getId(),
                event.getStatus(), event.getPackage(),
                event.getCreatedTime(), event.getCreator(),
                event.getModifiedTime(), event.getModifier(),
                event.getDependingEvents(), event.getEventsDependedOn(),
                event.getNodesDependedOn(),
                event.getScheduledTime(), event.getComment());
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public String getId()
    {
        return id;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public Status getStatus()
    {
        return status;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public Calendar getScheduledTime()
    {
        return (Calendar) scheduledTime.clone();
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public PublishingPackage getPackage()
    {
        return publishingPackage;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public Date getCreatedTime()
    {
        return new Date(createdTime.getTime());
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public String getCreator()
    {
        return creator;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public Date getModifiedTime()
    {
        return new Date(modifiedTime.getTime());
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public String getModifier()
    {
        return modifier;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public String getComment()
    {
        return comment;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public Set<PublishingEvent> getDependingEvents()
    {
        return dependingEvents;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public Set<PublishingEvent> getEventsDependedOn()
    {
        return eventsDependedOn;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public Set<NodeRef> getNodesDependedOn()
    {
        return nodesDependedOn;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public MutablePublishingEvent edit()
    {
        return new MutablePublishingEventImpl(this);
    }

}
