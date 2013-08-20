package org.alfresco.rest.framework.resource.parameters;

import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;

/**
 * Thrown when an invalid select syntax is used.
 * 
 * @author Gethin James
 */
public class InvalidSelectException extends InvalidArgumentException
{
    private static final long serialVersionUID = -8631890798598434965L;
    
    public static String DEFAULT_MESSAGE_ID = "framework.exception.InvalidSelect";
    
    public InvalidSelectException(Object queryParam)
    {
        super(DEFAULT_MESSAGE_ID, new Object[] {queryParam});
    }
}
