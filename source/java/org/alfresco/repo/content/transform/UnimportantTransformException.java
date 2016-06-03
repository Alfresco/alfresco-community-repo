package org.alfresco.repo.content.transform;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * An exception that may be thrown by a transformer that indicates that the transform
 * could not be performed, but that a full stack trace is not required in logging as
 * the reason is expected some of the time (for example source file does not contain an
 * embedded image).
 * 
 * @author Alan Davis
 */
public class UnimportantTransformException extends AlfrescoRuntimeException
{
    public UnimportantTransformException(String msgId)
    {
        super(msgId);
    }
}
