package org.alfresco.repo.management.subsystems;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Custom AlfrescoRuntimeException for invalid property values.
 * 
 * @author abalmus
 */
public class InvalidPropertyValueException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = -6026809919380879547L;

    /**
     * Creates a new InvalidPropertyValueException with custom message ID.
     * @param msgId 
     */
    public InvalidPropertyValueException(String msgId)
    {
        super(msgId);
    }

    /**
     * Creates a new InvalidPropertyValueException with custom message ID and custom message parameters.
     * @param msgId
     * @param msgParams 
     */
    public InvalidPropertyValueException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }
}
