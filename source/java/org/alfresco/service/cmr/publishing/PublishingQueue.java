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
    /**
     * A factory method to create an empty publishing package that can be populated before being passed into
     * a call to the {@link PublishingQueue#scheduleNewEvent(PublishingPackage, String, Calendar, String)} operation.
     * @return A publishing package that can be populated before being placed on the publishing queue.
     */
    MutablePublishingPackage createPublishingPackage();
    
    /**
     * Adds the supplied publishing package onto the queue.
     * @param publishingPackage The publishing package that is to be enqueued
     * @param channelName The name of the channel that the package is to be published to
     * @param schedule The time at which the new publishing event should be scheduled (optional - <code>null</code> indicates "as soon as possible")
     * @param comment A comment to be stored with this new event (optional - may be <code>null</code>)
     * @return The identifier of the newly scheduled event
     */
    String scheduleNewEvent(PublishingPackage publishingPackage, String channelName, Calendar schedule, String comment);
}
