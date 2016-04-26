package org.alfresco.service.cmr.quickshare;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Exception thrown by the QUickShare service when QuickShare is disabled.
 *
 * @author Alex Miller
 * @since Cloud/4.2
 */
public class QuickShareDisabledException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = -640705116576240570L;

    public QuickShareDisabledException(String message)
    {
        super(message);
    }

}
