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
package org.alfresco.com;

/**
 * File plan component type enumeration
 *
 * @author Tuna Aksoy
 * @since 1.0
 */
public enum FilePlanComponentType
{
    FILE_PLAN_TYPE("rma:filePlan"),
    RECORD_CATEGORY_TYPE("rma:recordCategory"),
    RECORD_FOLDER_TYPE("rma:recordFolder"),
    HOLD_TYPE("rma:hold"),
    UNFILED_RECORD_FOLDER_TYPE("rma:unfiledRecordFolder");

    private String type;

    private FilePlanComponentType(String type)
    {
        this.type = type;
    }

    public static final FilePlanComponentType getFilePlanComponentType(String type)
    {
        switch (type)
        {
            case "rma:filePlan":
                return FILE_PLAN_TYPE;
            case "rma:recordCategory":
                return RECORD_CATEGORY_TYPE;
            case "rma:recordFolder":
                return RECORD_FOLDER_TYPE;
            case "rma:hold":
                return HOLD_TYPE;
            case "rma:unfiledRecordFolder":
                return UNFILED_RECORD_FOLDER_TYPE;
        }

        throw new IllegalArgumentException("Invalid file plan component type enum value: '" + type + "'.");
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
