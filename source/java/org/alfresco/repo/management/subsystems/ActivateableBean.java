package org.alfresco.repo.management.subsystems;

/**
 * An interface to be implemented by beans that can be 'turned off' by some configuration setting. When such beans are
 * inactive, they will not perform any validation checks on initialization and will remain in a state where their
 * {@link #isActive()} method always returns <code>false</code>. {@link ChainingSubsystemProxyFactory} will ignore any
 * <code>ActivatableBean</code>s whose {@link #isActive()} method returns <code>false</code>. This allows certain
 * functions of a chained subsystem (e.g. CIFS authentication, SSO) to be targeted to specific members of the chain.
 * 
 * @author dward
 */
public interface ActivateableBean
{
    /**
     * Determines whether this bean is active.
     * 
     * @return <code>true</code> if this bean is active
     */
    public boolean isActive();
}
