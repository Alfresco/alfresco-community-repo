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

public interface PublishingQueue
{
    MutablePublishingPackage createPublishingPackage();
    
    /**
     * Adds the supplied publishing package onto the queue.
     * @param publishingPackage The publishing package that is to be enqueued
     * @param schedule The time at which the new publishing event should be scheduled (optional - <code>null</code> indicates "as soon as possible")
     * @param comment A comment to be stored with this new event (optional - may be <code>null</code>)
     * @return A PublishingEvent object representing the newly scheduled event
     */
    PublishingEvent scheduleNewEvent(PublishingPackage publishingPackage, Calendar schedule, String comment);
    
    void cancelEvent(String eventId);
}
