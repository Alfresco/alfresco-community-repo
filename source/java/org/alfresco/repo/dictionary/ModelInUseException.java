package org.alfresco.repo.dictionary;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * An exception thrown when an attempt is made to remove a model (or part
 * thereof) when the model is in use. That is, when nodes and node properties
 * reference types, aspects, namespaces, etc in the model.
 * 
 * @author sglover
 *
 */
public class ModelInUseException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 1447075542326143577L;

    public ModelInUseException(String message)
    {
        super(message);
    }
}
