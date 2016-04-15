package org.alfresco.repo.management.subsystems;

import org.springframework.context.ApplicationContext;

/**
 * An interface providing access to a child application context corresonding to a particular subsystem. As with other
 * {@link PropertyBackedBean}s, can be stopped, reconfigured, started and tested.
 * 
 * @author dward
 */
public interface ApplicationContextFactory extends PropertyBackedBean
{
    /**
     * Gets the application context, configured according to the properties of the factory.
     * 
     * @return the application context
     */
    public ApplicationContext getApplicationContext();
}
