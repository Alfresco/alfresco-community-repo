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
package org.alfresco.repo.events;

import org.alfresco.events.types.Event;


/**
* Creates and prepares event information.
*
* The primary reason for this interface is to allow for deferred creation
* of the Event.  If a NoOpEventPublisher is being used then the prepareEvent()
* method will never get called.
*
* As of Java 8 a Lambda expression could be used as the implementation of 
* this FunctionalInterface
*
* @author Gethin James
* @since 5.0
**/

//@FunctionalInterface
public interface EventPreparator
{
    public Event prepareEvent(String user, String networkId, String transactionId);
}
