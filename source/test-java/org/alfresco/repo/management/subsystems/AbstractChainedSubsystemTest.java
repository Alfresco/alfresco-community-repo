package org.alfresco.repo.management.subsystems;

import junit.framework.TestCase;

/**
 * Uses package level protection to allow us to sneak inside chained subsystems for test purposes.
 * 
 * @author dward
 */
public abstract class AbstractChainedSubsystemTest extends TestCase
{
    
    public ChildApplicationContextFactory getChildApplicationContextFactory(DefaultChildApplicationContextManager childApplicationContextManager, String id)
    {
        childApplicationContextManager.lock.readLock().lock();
        try
        {
            DefaultChildApplicationContextManager.ApplicationContextManagerState state = (DefaultChildApplicationContextManager.ApplicationContextManagerState) childApplicationContextManager
                    .getState(true);
            return state.getApplicationContextFactory(id);
        }
        finally
        {
            childApplicationContextManager.lock.readLock().unlock();
        }
    }

}
