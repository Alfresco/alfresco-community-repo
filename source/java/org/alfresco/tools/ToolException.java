package org.alfresco.tools;

/**
 * Tool Exception
 * 
 * @author David Caruana
 */
public class ToolException extends RuntimeException
{
    private static final long serialVersionUID = 3257008761007847733L;

    public ToolException(String msg)
    {
        super(msg);
    }

    public ToolException(String msg, Throwable cause)
    {
        super(msg, cause);
    }

}
