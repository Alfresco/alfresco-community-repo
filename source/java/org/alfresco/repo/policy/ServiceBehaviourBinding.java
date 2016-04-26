package org.alfresco.repo.policy;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Behaviour binding to a Service.
 * 
 * @author David Caruana
 *
 */
@AlfrescoPublicApi
public class ServiceBehaviourBinding implements BehaviourBinding
{
    // The service
    private Object service;

    /**
     * Construct
     * 
     * @param service  the service
     */
    /*package*/ ServiceBehaviourBinding(Object service)
    {
        this.service = service;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.policy.BehaviourBinding#generaliseBinding()
     */
    public BehaviourBinding generaliseBinding()
    {
        return null;
    }
    
    /**
     * Gets the Service
     * 
     * @return  the service
     */
    public Object getService()
    {
        return service;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof ServiceBehaviourBinding))
        {
            return false;
        }
        return service.equals(((ServiceBehaviourBinding)obj).service);
    }

    @Override
    public int hashCode()
    {
        return service.hashCode();
    }

    @Override
    public String toString()
    {
        return "ServiceBinding[service=" + service + "]";
    }

}
