/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
package org.alfresco.rest.rm.community.model.fileplancomponents;

import static org.alfresco.rest.rm.community.util.ParameterCheck.mandatoryString;

/**
 * File plan component alias enumeration
 *
 * @author Tuna Aksoy
 * @since 2.6
 */
public enum FilePlanComponentAlias
{
    FILE_PLAN_ALIAS("-filePlan-"),
    TRANSFERS_ALIAS("-transfers-"),
    UNFILED_RECORDS_CONTAINER_ALIAS("-unfiled-"),
    HOLDS_ALIAS("-holds-");

    private String alias;

    private FilePlanComponentAlias(String alias)
    {
        this.alias = alias;
    }

    public static final FilePlanComponentAlias getFilePlanComponentAlias(String alias)
    {
        mandatoryString("alias", alias);

        FilePlanComponentAlias result = null;
        FilePlanComponentAlias[] values = values();

        for (FilePlanComponentAlias filePlanComponentAlias : values)
        {
            if (filePlanComponentAlias.toString().equals(alias))
            {
                result = filePlanComponentAlias;
                break;
            }
        }

        if (result == null)
        {
            throw new IllegalArgumentException("Invalid file plan component alias enum value: '" + alias + "'.");
        }

        return result;
    }

    /**
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString()
    {
        return this.alias;
    }
}
