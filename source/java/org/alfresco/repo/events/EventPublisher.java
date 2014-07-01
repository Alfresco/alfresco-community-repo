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

import org.alfresco.events.types.BrowserEvent;
import org.alfresco.events.types.Event;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * EventPublisher can be used to broadcast events.
 *
 * @author Gethin James
 * @since 5.0
 */
public interface EventPublisher
{
    //TODO AbstractEventsService should probably implement this interface
    
    /**
     * Publish the event
     * @param event Event
     */
    public void publishEvent(Event event);
    
    /**
     * A special type of Event that occurs in a web browser
     * @see BrowserEvent
     * @param req - WebScriptRequest
     * @param siteId - optional site id
     * @param component - page eg. "documentdetails"
     * @param action - eg. "view"
     * @param attributes - optional additional attributes as a json map eg. {"liked":"true"}
     */
    public void publishBrowserEvent(WebScriptRequest req, String siteId, String component, String action, String attributes);
    
    /**
     * Publish the event using an EventPreparator
     * @param prep EventPreparator
     */
    public void publishEvent(EventPreparator prep);
    
}
