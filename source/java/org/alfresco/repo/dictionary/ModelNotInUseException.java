package org.alfresco.repo.dictionary;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * An exception thrown to indicate that a model is not in use.
 * 
 * @author sglover
 *
 */
public class ModelNotInUseException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 1447075542326143577L;

    public ModelNotInUseException(String message)
    {
        super(message);
    }
}
