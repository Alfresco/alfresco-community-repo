package org.alfresco.rest.framework.core.exceptions;

/**
 * The media type is unsupported.
 *
 * @author janv
 */
public class UnsupportedMediaTypeException extends ApiException
{
    public static String DEFAULT_MESSAGE_ID = "framework.exception.UnsupportedMediaType";

    public UnsupportedMediaTypeException()
    {
        super(DEFAULT_MESSAGE_ID);
    }

    public UnsupportedMediaTypeException(String msgId)
    {
        super(msgId);
    }

    public UnsupportedMediaTypeException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }
}
