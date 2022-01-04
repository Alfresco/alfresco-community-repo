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

package org.alfresco.module.org_alfresco_module_rm.event;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.util.ParameterCheck;

/**
 * Records management event
 *
 * @author Roy Wetherall
 * @since 1.0
 */
@AlfrescoPublicApi
public class RecordsManagementEvent
{
    /** Records management event type */
    private RecordsManagementEventType type;

    /** Records management event name */
    private String name;

    /** Records management display label */
    private String displayLabel;

    /**
     * Constructor
     *
     * @param type          event type
     * @param name          event name
     * @param displayLabel  event display label
     */
    public RecordsManagementEvent(RecordsManagementEventType type, String name, String displayLabel)
    {
        ParameterCheck.mandatory("type", type);
        ParameterCheck.mandatory("name", name);
        ParameterCheck.mandatory("displayLabel", displayLabel);

        this.type =  type;
        this.name = name;
        this.displayLabel = displayLabel;
    }

    /**
     * Get records management type name
     *
     * @return  String records management event type name
     */
    public String getType()
    {
        return type.getName();
    }

    /**
     * Get the records management event type.
     *
     * @return {@link RecordsManagementEventType}   records management event type
     *
     * @since 2.2
     */
    public RecordsManagementEventType getRecordsManagementEventType()
    {
        return type;
    }

    /**
     * Event name
     *
     * @return String   event name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     *
     * @return
     */
    public String getDisplayLabel()
    {
        return displayLabel;
    }
}
