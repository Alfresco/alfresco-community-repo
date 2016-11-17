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
