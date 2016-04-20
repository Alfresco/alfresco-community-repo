package org.alfresco.service.transaction;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Exception thrown when a read-only server attempts a write operation.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class ReadOnlyServerException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 4996411986062612198L;

    public static final String MSG_READ_ONLY = "permissions.err_read_only";

    public ReadOnlyServerException()
    {
        super(MSG_READ_ONLY);
    }
}
