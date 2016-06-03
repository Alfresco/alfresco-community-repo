package org.alfresco.repo.jscript;

import org.alfresco.repo.processor.BaseProcessorExtension;
import org.alfresco.service.ServiceRegistry;

/**
 * Support object for session level properties etc.
 * <p>
 * Provides access to the user's authentication ticket.
 * 
 * @author Andy Hind
 */
public class Session extends BaseProcessorExtension
{
    /** Service registry */
    private ServiceRegistry services;
    
    /**
     * Set the service registry
     * 
     * @param services  the service registry
     */
    public void setServiceRegistry(ServiceRegistry services)
    {
        this.services = services;
    }
    
    /**
     * Get the user's authentication ticket.
     * 
     * @return String
     */
    public String getTicket()
    {
        return services.getAuthenticationService().getCurrentTicket();
    }
}
