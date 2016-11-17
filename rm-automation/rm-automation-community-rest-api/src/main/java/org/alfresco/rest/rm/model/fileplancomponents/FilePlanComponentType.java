/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.rest.rm.model.fileplancomponents;

import static org.alfresco.rest.rm.util.ParameterCheck.mandatoryString;

/**
 * File plan component type enumeration
 *
 * @author Tuna Aksoy
 * @since 2.6
 */
public enum FilePlanComponentType
{
    FILE_PLAN_TYPE("rma:filePlan"),
    RECORD_CATEGORY_TYPE("rma:recordCategory"),
    RECORD_FOLDER_TYPE("rma:recordFolder"),
    HOLD_TYPE("rma:hold"),
    UNFILED_RECORD_FOLDER_TYPE("rma:unfiledRecordFolder"),
    HOLD_CONTAINER_TYPE("rma:holdContainer"),
    TRANSFER_TYPE("rma:transfer"),
    TRANSFER_CONTAINER_TYPE("rma:transferContainer"),
    UNFILED_CONTAINER_TYPE("rma:unfiledRecordContainer"),
    FOLDER_TYPE("cm:folder"),
    CONTENT_TYPE("cm:content");

    private String type;

    private FilePlanComponentType(String type)
    {
        this.type = type;
    }

    public static final FilePlanComponentType getFilePlanComponentType(String type)
    {
        mandatoryString("type", type);

        FilePlanComponentType result = null;
        FilePlanComponentType[] values = values();

        for (FilePlanComponentType filePlanComponentType : values)
        {
            if (filePlanComponentType.toString().equals(filePlanComponentType))
            {
                result = filePlanComponentType;
                break;
            }
        }

        if (result == null)
        {
            throw new IllegalArgumentException("Invalid file plan component type enum value: '" + type + "'.");
        }

        return result;
    }

    /**
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString()
    {
        return this.type;
    }
}
