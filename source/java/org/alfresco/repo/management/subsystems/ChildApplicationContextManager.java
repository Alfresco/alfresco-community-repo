package org.alfresco.repo.management.subsystems;

import java.util.Collection;

import org.springframework.context.ApplicationContext;

/**
 * A <code>ChildApplicationContextManager</code> manages a 'chain' of child application contexts, perhaps corresponding
 * to the components of a chained subsystem such as authentication. A <code>ChildApplicationContextManager</code> may
 * also support the dynamic modification of its chain.
 * 
 * @author dward
 */
public interface ChildApplicationContextManager
{
    /**
     * Gets the ordered collection of identifiers, indicating the ordering of the chain.
     * 
     * @return an ordered collection of identifiers, indicating the ordering of the chain.
     */
    public Collection<String> getInstanceIds();

    /**
     * Gets the application context with the given identifier.
     * 
     * @param id
     *            the identifier of the application context to retrieve
     * @return the application context with the given identifier or null if it does not exist
     */
    public ApplicationContext getApplicationContext(String id);
}
