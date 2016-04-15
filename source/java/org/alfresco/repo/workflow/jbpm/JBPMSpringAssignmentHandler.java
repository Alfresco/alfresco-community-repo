package org.alfresco.repo.workflow.jbpm;

import org.jbpm.taskmgmt.def.AssignmentHandler;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springmodules.workflow.jbpm31.JbpmFactoryLocator;


/**
 * Abstract base implementation of a Jbpm Assignment Handler with access to
 * Alfresco Spring beans.
 * 
 * @author davidc
 */
public abstract class JBPMSpringAssignmentHandler implements AssignmentHandler
{
    private static final long serialVersionUID = -2233750219905283562L;

    /**
     * Construct
     */
    protected JBPMSpringAssignmentHandler()
    {
        // The following implementation is derived from Spring Modules v0.4
        BeanFactoryLocator factoryLocator = new JbpmFactoryLocator();
        BeanFactoryReference factory = factoryLocator.useBeanFactory(null);
        initialiseHandler(factory.getFactory());
    }
    
    /**
     * Initialise Action Handler
     * 
     * @param factory  Spring bean factory for accessing Alfresco beans
     */
    protected abstract void initialiseHandler(BeanFactory factory);

    
}
