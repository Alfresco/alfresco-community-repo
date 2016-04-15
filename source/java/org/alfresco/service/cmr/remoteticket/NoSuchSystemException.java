package org.alfresco.service.cmr.remoteticket;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Exception thrown if a request is made, to work on 
 *  authentication for a Remote System, where the
 *  System is not known to the service.
 *  
 * @author Nick Burch
 * @since 4.0.2
 */
public class NoSuchSystemException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 282472917033620185L;
    private String system; 

    public NoSuchSystemException(String system) 
    {
        super("No Remote System defined with ID '" + system + "'");
        this.system = system;
    }
    
    public String getSystem() 
    {
        return system;
    }
}