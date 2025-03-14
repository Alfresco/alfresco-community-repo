package org.alfresco.util;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Enumeration for sort direction.
 *
 * @author Tuna Aksoy
 * @since 2.5
 */
@AlfrescoPublicApi
public enum SortDirection
{
    ASC("asc"), DESC("desc");

    /** A string representation of the sort direction. */
    private final String sortDirection;

    private SortDirection(String sortDirection)
    {
        this.sortDirection = sortDirection;
    }

    /**
     * Get the appropriate {@code SortDirection} from a string representation.
     *
     * @param sortDirectionString
     *            A string representation (case will be ignored).
     * @return The {@code SortDirection} value.
     */
    public static SortDirection getSortDirection(String sortDirectionString)
    {
        SortDirection sortDirection = null;

        for (SortDirection value : values())
        {
            if (value.sortDirection.equalsIgnoreCase(sortDirectionString))
            {
                sortDirection = value;
                break;
            }
        }

        if (sortDirection == null)
        {
            throw new AlfrescoRuntimeException("Sort direction unknown.");
        }

        return sortDirection;
    }
}
