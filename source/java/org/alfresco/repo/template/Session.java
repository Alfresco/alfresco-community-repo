package org.alfresco.repo.template;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.TemplateImageResolver;

/**
 * Support session information in free marker templates.
 * 
 * @author Andy Hind
 */
public class Session extends BaseTemplateProcessorExtension
{
    private ServiceRegistry services;

    /**
     * Sets the service registry
     * 
     * @param services  the service registry
     */
    public void setServiceRegistry(ServiceRegistry services)
    {
        this.services = services;
    }
    
    /**
     * Get the current authentication ticket.
     * 
     * @return String
     */
    public String getTicket()
    {
        return services.getAuthenticationService().getCurrentTicket();
    }
}
