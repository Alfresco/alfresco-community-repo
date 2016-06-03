package org.alfresco.repo.content.transform;

import org.alfresco.api.AlfrescoPublicApi;     
import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Exception indicates that a transformer is unable to transform a requested
 * transformation. Normally the transformer is a component of a complex (compound) transformer
 * and has been asked to transform a file that is too large (see transformation limits) as the
 * size of the intermediate file is unknown at the start.
 * 
 * @author Alan Davis
 */
@AlfrescoPublicApi
public class UnsupportedTransformationException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 9039331287661301086L;

    public UnsupportedTransformationException(String msgId)
    {
        super(msgId);
    }
}
