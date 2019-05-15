package org.alfresco.rest.exception;

public class EmptyJsonResponseException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public EmptyJsonResponseException(String message)
    {
        super(String.format("Empty JSON Response returned. Possible API bug, please investigate this further. Message: %s", message));
    }
}
