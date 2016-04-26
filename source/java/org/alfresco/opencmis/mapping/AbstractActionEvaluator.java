package org.alfresco.opencmis.mapping;

import org.alfresco.opencmis.dictionary.CMISActionEvaluator;
import org.alfresco.service.ServiceRegistry;
import org.apache.chemistry.opencmis.commons.enums.Action;

/**
 * Base class for all action evaluators
 * 
 * @author davidc
 * 
 */
public abstract class AbstractActionEvaluator implements CMISActionEvaluator
{
    private ServiceRegistry serviceRegistry;
    private Action action;

    /**
     * Construct
     * 
     * @param serviceRegistry ServiceRegistry
     * @param action Action
     */
    protected AbstractActionEvaluator(ServiceRegistry serviceRegistry, Action action)
    {
        this.serviceRegistry = serviceRegistry;
        this.action = action;
    }

    /**
     * @return service registry
     */
    protected ServiceRegistry getServiceRegistry()
    {
        return serviceRegistry;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.opencmis.CMISActionEvaluator#getAction()
     */
    public Action getAction()
    {
        return action;
    }
}
