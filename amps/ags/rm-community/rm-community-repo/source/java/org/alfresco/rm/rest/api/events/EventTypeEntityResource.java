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

import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventService;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rm.rest.api.model.EventTypeInfo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Event Type entity resource
 */
@EntityResource(name = "event-types", title = "Event Types")
public class EventTypeEntityResource implements EntityResourceAction.Read<EventTypeInfo> {
    private RecordsManagementEventService recordsManagementEventService;

    /**
     * Set the records management event service
     * @param rmEventService
     */
    public void setRecordsManagementEventService(RecordsManagementEventService rmEventService)
    {
        this.recordsManagementEventService = rmEventService;
    }

    @Override
    @WebApiDescription(title = "Return a list of different event types")
    public CollectionWithPagingInfo<EventTypeInfo> readAll(Parameters params)
    {
        // TODO: AssociationName and ActionOnAssociatedNode properties still need to be assigned.
        Paging paging = params.getPaging();

        List<EventTypeInfo> eventTypeInfoList = recordsManagementEventService.getEventTypes().stream()
                .map(EventTypeInfo::fromRecordsManagementEventType)
                .collect(Collectors.toList());

        int totalCount = eventTypeInfoList.size();
        boolean hasMoreItems = paging.getSkipCount() + paging.getMaxItems() < totalCount;
        return CollectionWithPagingInfo.asPaged(paging, eventTypeInfoList.stream()
                .skip(paging.getSkipCount())
                .limit(paging.getMaxItems())
                .collect(Collectors.toList()), hasMoreItems, totalCount);
    }
}
