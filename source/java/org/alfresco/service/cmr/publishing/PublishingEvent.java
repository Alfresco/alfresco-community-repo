/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

package org.alfresco.service.cmr.publishing;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Brian
 *
 */
public interface PublishingEvent
{
    enum Status {SCHEDULED, IN_PROGRESS, COMPLETE, FAILED}
    
    String getId();
    
    Status getStatus();
    
    Calendar getScheduledTime();
    
    PublishingPackage getPackage();
    
    Date getCreatedTime();
    
    String getCreator();
    
    Date getModifiedTime();
    
    String getModifier();
    
    String getComment();
    
    /**
     * Retrieve those publishing events that depend on this one
     * @return
     */
    Set<PublishingEvent> getDependingEvents();
    
    /**
     * Retrieve those publishing events on which this one depends
     * @return
     */
    Set<PublishingEvent> getEventsDependedOn();

    Set<NodeRef> getNodesDependedOn();
    
    MutablePublishingEvent edit();
}
