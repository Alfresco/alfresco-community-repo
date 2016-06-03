package org.alfresco.rest.framework.resource.parameters.where;

import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;

/**
 * Thrown when an invalid query syntax is used.
 * 
 * @author Gethin James
 */
public class InvalidQueryException extends InvalidArgumentException
{
    private static final long serialVersionUID = 8825573452685077660L;
    public static String DEFAULT_MESSAGE_ID = "framework.exception.InvalidQuery";
    private Object queryParam;

	public InvalidQueryException() {
		 super(DEFAULT_MESSAGE_ID);
	}

    public InvalidQueryException(Object queryParam)
    {
        super(DEFAULT_MESSAGE_ID, new Object[] {queryParam});
        this.queryParam = queryParam;
    }

    public Object getQueryParam()
    {
        return queryParam;
    }

}
