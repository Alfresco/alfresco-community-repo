package org.alfresco.repo.lock.mem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link LockStoreFactory} implementation that creates new {@link LockStoreImpl} objects.
 * 
 * @author Matt Ward
 */
public class DefaultLockStoreFactory implements LockStoreFactory
{
    private static final Log log = LogFactory.getLog(DefaultLockStoreFactory.class);
    
    @Override
    public LockStore createLockStore()
    {
        if (log.isDebugEnabled())
        {
            log.debug("Creating LockStore.");
        }
        LockStore lockStore = new LockStoreImpl();
        return lockStore;
    }

}
