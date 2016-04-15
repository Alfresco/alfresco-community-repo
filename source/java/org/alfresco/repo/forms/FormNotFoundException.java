package org.alfresco.repo.forms;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Exception used by the Form service when a form can not be found for the given item
 *
 * @author Gavin Cornwell
 */
public class FormNotFoundException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 688834574410335422L;

    public FormNotFoundException(Item item)
    {
        // TODO: replace strings with msg ids
        super("A form could not be found for item: " + item);
    }
    
    public FormNotFoundException(Item item, Throwable cause)
    {
        // TODO: replace strings with msg ids
        super("A form could not be found for item: " + item, cause);
    }
}
