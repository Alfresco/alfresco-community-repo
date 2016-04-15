package org.alfresco.repo.workflow;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.namespace.QName;

public class PropertyValueSizeIsMoreMaxLengthException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 5722742734237891185L;

    public PropertyValueSizeIsMoreMaxLengthException(QName name)
    {
        super("Property '" + name.getLocalName() + "' has size more than max value.");
    }
    
    public PropertyValueSizeIsMoreMaxLengthException(String name)
    {
        super("Property '" + name + "' has size more than max value.");
    }
}