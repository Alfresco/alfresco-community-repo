package org.alfresco.repo.lock.mem;



/**
 * Factory to create {@link LockStore} instances.
 * 
 * @author Matt Ward
 */
public interface LockStoreFactory
{
    LockStore createLockStore();
}
