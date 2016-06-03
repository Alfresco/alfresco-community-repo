package org.alfresco.repo.workflow.activiti;

import java.util.Map;

import org.activiti.engine.delegate.JavaDelegate;
import org.alfresco.service.ServiceRegistry;

/**
 * Base class for all {@link JavaDelegate} used in Alfresco-context.
 *
 * @author Frederik Heremans
 */
public abstract class BaseJavaDelegate implements JavaDelegate
{
    private ServiceRegistry serviceRegistry;
    
    /**
     * Get the service-registry from the current Activiti-context.
     * 
     * @return service registry
     */
    protected ServiceRegistry getServiceRegistry()
    {
        return serviceRegistry;
    }

    /**
     * @param serviceRegistry the serviceRegistry to set
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }
    
    public void setBeanRegistry(Map<Object, Object> beanRegistry)
    {
        beanRegistry.put(getName(), this);
    }
    
    /**
     * Defaults to the full {@link Class} Name.
     * @return String
     */
    protected String getName()
    {
        return getClass().getSimpleName();
    }
}
