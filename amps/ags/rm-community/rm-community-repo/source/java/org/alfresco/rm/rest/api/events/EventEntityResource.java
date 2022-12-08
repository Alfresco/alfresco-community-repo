/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rm.rest.api.events;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEvent;
import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventService;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.core.ResourceParameter;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.EventAlreadyExistsException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rm.rest.api.model.EventBody;
import org.alfresco.rm.rest.api.model.EventInfo;
import org.alfresco.util.GUID;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.alfresco.util.ParameterCheck.mandatory;

/**
 * Event entity resource
 */
@EntityResource(name = "events", title = "Events")
public class EventEntityResource implements EntityResourceAction.Read<EventInfo>,
                                            EntityResourceAction.ReadById<EventInfo>,
                                            EntityResourceAction.Update<EventBody>,
                                            EntityResourceAction.Create<EventBody> {

    private RecordsManagementEventService recordsManagementEventService;

    /**
     * Set the records management event service
     *
     * @param rmEventService
     */
    public void setRecordsManagementEventService(RecordsManagementEventService rmEventService)
    {
        this.recordsManagementEventService = rmEventService;
    }

    @Override
    @WebApiDescription(title = "Return a single event identified by 'eventId'")
    @WebApiParam(name = "eventId", title = "The event id", kind = ResourceParameter.KIND.URL_PATH)
    public EventInfo readById(String eventId, Parameters parameters) throws EntityNotFoundException
    {
        mandatory("eventId", eventId);

        RecordsManagementEvent event = null;
        if (eventExists(eventId))
        {
            // Get the event
            event = recordsManagementEventService.getEvent(eventId);
        }

        return EventInfo.fromRecordsManagementEvent(event);
    }

    @Override
    @WebApiDescription(title = "Return a list of events")
    public CollectionWithPagingInfo<EventInfo> readAll(Parameters params)
    {
        Paging paging = params.getPaging();

        List<EventInfo> eventInfoList = recordsManagementEventService.getEvents().stream()
                .map(EventInfo::fromRecordsManagementEvent)
                .collect(Collectors.toList());

        int totalCount = eventInfoList.size();
        boolean hasMoreItems = paging.getSkipCount() + paging.getMaxItems() < totalCount;
        return CollectionWithPagingInfo.asPaged(paging, eventInfoList.stream()
                .skip(paging.getSkipCount())
                .limit(paging.getMaxItems())
                .collect(Collectors.toList()), hasMoreItems, totalCount);
    }

    @Override
    @WebApiDescription(title = "Create a new event")
    public List<EventBody> create(List<EventBody> eventBodyList, Parameters parameters)
    {
        //TODO: 403 User not allowed to update event error still needs to be implemented
        mandatory("eventBodyList", eventBodyList);
        for (EventBody eventBody : eventBodyList) {
            mandatory("eventName", eventBody.getName());
            mandatory("eventType", eventBody.getType());
        }

        List<EventBody> responseEventBodyList = new ArrayList<>();
        for (EventBody eventBody : eventBodyList) {
            String eventId = GUID.generate();
            String eventName = eventBody.getName();
            String eventType = eventBody.getType();

            if(canCreateEvent(eventId, eventName)) {
                RecordsManagementEvent event = recordsManagementEventService.addEvent(eventType, eventId, eventName);
                responseEventBodyList.add(EventBody.fromRecordsManagementEvent(event));
            }
        }
        return responseEventBodyList;
    }

    @Override
    @WebApiDescription(title = "Update a single event identified by 'eventId'")
    @WebApiParam(name = "eventId", title = "The event id", kind = ResourceParameter.KIND.URL_PATH)
    public EventBody update(String eventId, EventBody eventBody, Parameters parameters)
    {
        //TODO: 403 User not allowed to update event error still needs to be implemented
        mandatory("eventId", eventId);
        mandatory("eventName", eventBody.getName());
        mandatory("eventType", eventBody.getType());

        RecordsManagementEvent event = null;
        if (canEditEvent(eventBody.getName(), eventId, eventBody.getType()))
        {
            // Create event
            event = recordsManagementEventService.addEvent(eventBody.getType(), eventId, eventBody.getName());
        }
        else
        {
            // Get event
            event = recordsManagementEventService.getEvent(eventId);
        }

        return EventBody.fromRecordsManagementEvent(event);
    }

    /**
     * Helper method for checking if an event exists or not. Throws an
     * error if the event does not exist.
     *
     * @param eventId The id of the event
     */
    private boolean eventExists(String eventId)
    {
        boolean eventExists = true;

        // Check the event exists
        if (!recordsManagementEventService.existsEvent(eventId))
        {
            throw new EntityNotFoundException(eventId);
        }

        return eventExists;
    }

    /**
     * Helper method for checking if an event can be created or not. Throws an
     * error if the event already exists.
     *
     * @param eventId The id of the event
     * @param eventName The name of the event
     */
    private boolean canCreateEvent(String eventId, String eventName)
    {
        boolean canCreateEvent = true;

        if (!recordsManagementEventService.canCreateEvent(eventName, eventId))
        {
            throw new EventAlreadyExistsException("framework.exception.CreateEventAlreadyExists", new Object[] {eventName});
        }

        return canCreateEvent;
    }

    /**
     * Helper method for checking if an event can be edited or not. Throws an
     * error if an event with the same display label already exists.
     *
     * @param eventName The name of the event
     * @param eventId The id of the event
     * @param eventType The type of the event
     * @return true if the event can be edited, false otherwise
     */
    private boolean canEditEvent(String eventName, String eventId, String eventType)
    {
        boolean canEditEvent = false;
        if (eventExists(eventId)) {
            try
            {
                canEditEvent = recordsManagementEventService.canEditEvent(eventName, eventId, eventType);
            }
            catch (AlfrescoRuntimeException are)
            {
                throw new EventAlreadyExistsException("framework.exception.UpdateEventAlreadyExists", new Object[] {eventName});
            }
        }
        return canEditEvent;
    }
}
