package org.alfresco.tools;

/**
 * Tool Argument Exception
 * 
 * @author David Caruana
 */
/*package*/ class ToolArgumentException extends ToolException
{
	private static final long serialVersionUID = 3880274996297222647L;

	/*package*/ ToolArgumentException(String msg)
    {
        super(msg);
    }

    /*package*/ ToolArgumentException(String msg, Throwable cause)
    {
        super(msg, cause);
    }

}
