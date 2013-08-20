package org.alfresco.rest.framework.core.exceptions;

/**
 * In this version of the API the specified resource has been deleted and is
 * therefore not support
 *
 * @author Gethin James
 */
public class DeletedResourceException extends UnsupportedResourceOperationException
{
    private static final long serialVersionUID = -6475070011048033402L;
    public static String DEFAULT_MESSAGE_ID = "framework.exception.DeletedResource";
    
    /**
     * Creates the exception with the default message and the name of the resource
     * @param resourceName
     */
    public DeletedResourceException(String resourceName)
    {
        super(DEFAULT_MESSAGE_ID, new String[] {resourceName});
    }


}
