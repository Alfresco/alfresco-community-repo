/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.rest.rm.community.model.audit;

/**
 * Enumerates the list of events audited
 *
 * @author  Rodica Sutu
 * @since 2.7
 *
 */
public enum AuditEvents
{
    CREATE_PERSON("Create Person", "Create User"),
    DELETE_PERSON("Delete Person", "Delete User"),
    CREATE_USER_GROUP("Create User Group", "Create User Group"),
    DELETE_USER_GROUP("Delete User Group", "Delete User Group"),
    ADD_TO_USER_GROUP("Add To User Group", "Add To User Group"),
    REMOVE_FROM_USER_GROUP("Remove From User Group", "Remove From User Group");

    /** event audited */
    public final String event;

    /** display name for the event audited */
    public final String eventDisplayName;

    AuditEvents(String event, String displayName)
    {
        this.event = event;
        this.eventDisplayName = displayName;
    }
}