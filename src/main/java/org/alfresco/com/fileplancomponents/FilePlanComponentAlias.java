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
package org.alfresco.com.fileplancomponents;

/**
 * File plan component alias enumeration
 *
 * @author Tuna Aksoy
 * @since 1.0
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
        switch (alias)
        {
            case "-filePlan-":
                return FILE_PLAN_ALIAS;
            case "-transfers-":
                return TRANSFERS_ALIAS;
            case "-unfiled-":
                return UNFILED_RECORDS_CONTAINER_ALIAS;
            case "-holds-":
                return HOLDS_ALIAS;
        }

        throw new IllegalArgumentException("Invalid file plan component alias enum value: '" + alias + "'.");
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
