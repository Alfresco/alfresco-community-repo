package org.alfresco.repo.lock.mem;


/**
 * Tests for the {@link LockStoreImpl} class.
 * 
 * @author Matt Ward
 */
public class LockStoreImplTest extends AbstractLockStoreTestBase<LockStoreImpl>
{
    @Override
    protected LockStoreImpl createLockStore()
    {
        return new LockStoreImpl(20);
    }
}
