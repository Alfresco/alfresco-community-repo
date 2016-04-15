package org.alfresco.repo.lock.mem;

import org.alfresco.test_category.OwnJVMTestsCategory;
import org.junit.experimental.categories.Category;

/**
 * Test transaction related functions of {@link LockStoreImpl}.
 * 
 * @author Matt Ward
 */
@Category(OwnJVMTestsCategory.class)
public class LockStoreImplTxTest extends AbstractLockStoreTxTest<LockStoreImpl>
{
    @Override
    protected LockStoreImpl createLockStore()
    {
        return new LockStoreImpl(20);
    }
}
