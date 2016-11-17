/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
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
    TRANSFER_CONTAINER_TYPE("rma:transferContainer"),
    UNFILED_CONTAINER_TYPE("rma:unfiledRecordContainer");

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
