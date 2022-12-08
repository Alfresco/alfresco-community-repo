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

package org.alfresco.rm.rest.api.model;

import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventType;

/**
 * The EventTypeInfo model to be exposed through REST API.
 */
public class EventTypeInfo {
    private String id;
    private String name;
    private boolean isAutomatic;
    private String associationName;
    private String actionOnAssociatedNode;

    public static EventTypeInfo fromRecordsManagementEventType(RecordsManagementEventType eventType)
    {
        EventTypeInfo eventTypeInfo = new EventTypeInfo();
        if (eventType != null) {
            eventTypeInfo.setName(eventType.getDisplayLabel());
            eventTypeInfo.setId(eventType.getName());
            eventTypeInfo.setAutomatic(eventType.isAutomaticEvent());
        }
        return eventTypeInfo;
    }

    public EventTypeInfo() {}

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public boolean isAutomatic()
    {
        return isAutomatic;
    }

    public void setAutomatic(boolean automatic)
    {
        isAutomatic = automatic;
    }

    public String getAssociationName()
    {
        return associationName;
    }

    public void setAssociationName(String associationName)
    {
        this.associationName = associationName;
    }

    public String getActionOnAssociatedNode()
    {
        return actionOnAssociatedNode;
    }

    public void setActionOnAssociatedNode(String actionOnAssociatedNode)
    {
        this.actionOnAssociatedNode = actionOnAssociatedNode;
    }
}

