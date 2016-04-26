package org.alfresco.service.cmr.quickshare;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Exception thrown by the QuickShareService to indicate that content for the given quick share id 
 * could not be found.
 *
 * @author Alex Miller
 * @since Cloud/4.2
 */
public class InvalidSharedIdException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = -8652843674476371259L;

    public InvalidSharedIdException(String sharedId)
    {
        super("Unable to find: " + sharedId);
    }

}
