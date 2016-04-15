package org.alfresco.repo.policy.annotation;

import org.alfresco.repo.policy.Behaviour;

/**
 * Interface for a behaviour registry.
 *
 * @author Roy Wetherall
 * @since 5.0
 */
public interface BehaviourRegistry
{
    /**
     * Register a behaviour against a given name.
     *
     * @param behaviour behaviour
     */
    void registerBehaviour(String name, Behaviour behaviour);

    /**
     * Gets the behaviour for a given name.
     *
     * @param name                  behaviour name
     * @return {@link Behaviour}    behaviour, null otherwise
     */
    Behaviour getBehaviour(String name);
}
