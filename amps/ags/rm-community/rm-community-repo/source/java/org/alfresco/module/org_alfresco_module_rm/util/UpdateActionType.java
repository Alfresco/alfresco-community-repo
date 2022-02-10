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
package org.alfresco.module.org_alfresco_module_rm.util;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * An enumeration for the methods of updating a collection of immutable objects.
 *
 * @author Tom Page
 * @since 2.5
 */
@AlfrescoPublicApi
public enum UpdateActionType
{
    ADD,
    REMOVE;

    public static UpdateActionType valueOfIgnoreCase(String name)
    {
        UpdateActionType actionType;
        try
        {
            actionType = UpdateActionType.valueOf(name.toUpperCase());
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Could not find enum with name '" + name + "'. Not one of the values accepted for Enum class: [ADD, REMOVE]");
        }

        return actionType;
    }
}
