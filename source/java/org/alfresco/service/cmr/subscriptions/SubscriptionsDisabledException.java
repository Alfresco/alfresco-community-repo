package org.alfresco.service.cmr.subscriptions;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * This exception is thrown if subscriptions are disabled.
 * 
 * @author Florian Mueller
 * @since 4.0
 */
public class SubscriptionsDisabledException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 6971869799749343887L;

    public SubscriptionsDisabledException(String msg)
    {
        super(msg);
    }

    public SubscriptionsDisabledException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
}
