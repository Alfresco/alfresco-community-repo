package org.alfresco.service.cmr.version;

import java.text.MessageFormat;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * @author Roy Wetherall
 */
@AlfrescoPublicApi
public class ReservedVersionNameException extends RuntimeException
{
    /**
     * Serial verison UID
     */
    private static final long serialVersionUID = 3690478030330015795L;

    /**
     * Error message
     */
    private static final String MESSAGE = "The version property name {0} clashes with a reserved verison property name.";
    
    /**
     * Constructor
     * 
     * @param propertyName  the name of the property that clashes with
     *                      a reserved property name
     */
    public ReservedVersionNameException(String propertyName)
    {
        super(MessageFormat.format(MESSAGE, new Object[]{propertyName}));
    }
}
