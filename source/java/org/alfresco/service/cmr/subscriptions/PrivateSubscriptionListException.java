package org.alfresco.service.cmr.subscriptions;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * This exception is thrown if a subscription list is private and the accessing
 * user is not allowed to see it.
 * 
 * @author Florian Mueller
 * @since 4.0
 */
public class PrivateSubscriptionListException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 6971869799749343887L;

    public PrivateSubscriptionListException(String msg)
    {
        super(msg);
    }

    public PrivateSubscriptionListException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
}
