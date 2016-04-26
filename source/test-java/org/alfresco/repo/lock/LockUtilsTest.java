package org.alfresco.repo.lock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests for the {@link LockUtils} class.
 * 
 * @author Matt Ward
 */
@RunWith(MockitoJUnitRunner.class)
public class LockUtilsTest
{
    private @Mock LockService lockService;
    private final NodeRef nodeRef = new NodeRef("workspace://SpacesStore/node-id");
    
    @Test
    public void testIsLockedAndReadOnly_ForLockOwnerWithNullLockType()
    {
        when(lockService.getLockStatus(nodeRef)).thenReturn(LockStatus.LOCK_OWNER);
        when(lockService.getLockType(nodeRef)).thenReturn(null);
        
        boolean returnedVal = LockUtils.isLockedAndReadOnly(nodeRef, lockService);
        assertEquals(true, returnedVal);
    }
    
    @Test
    public void testIsLockedAndReadOnly_ForLockOwnerWithWriteLockType()
    {
        when(lockService.getLockStatus(nodeRef)).thenReturn(LockStatus.LOCK_OWNER);
        when(lockService.getLockType(nodeRef)).thenReturn(LockType.WRITE_LOCK);
        
        boolean returnedVal = LockUtils.isLockedAndReadOnly(nodeRef, lockService);
        assertEquals(false, returnedVal);
    }
    
    @Test
    public void testIsLockedAndReadOnly_ForLockOwnerWithNodeLockType()
    {
        when(lockService.getLockStatus(nodeRef)).thenReturn(LockStatus.LOCK_OWNER);
        when(lockService.getLockType(nodeRef)).thenReturn(LockType.NODE_LOCK);
        
        boolean returnedVal = LockUtils.isLockedAndReadOnly(nodeRef, lockService);
        assertEquals(true, returnedVal);
    }
    
    @Test
    public void testIsLockedAndReadOnly_ForLockOwnerWithReadOnlyLockType()
    {
        when(lockService.getLockStatus(nodeRef)).thenReturn(LockStatus.LOCK_OWNER);
        when(lockService.getLockType(nodeRef)).thenReturn(LockType.READ_ONLY_LOCK);
        
        boolean returnedVal = LockUtils.isLockedAndReadOnly(nodeRef, lockService);
        assertEquals(true, returnedVal);
    }

    @Test
    public void testIsLockedAndReadOnly_ForNoLock()
    {
        when(lockService.getLockStatus(nodeRef)).thenReturn(LockStatus.NO_LOCK);
        
        boolean returnedVal = LockUtils.isLockedAndReadOnly(nodeRef, lockService);
        assertEquals(false, returnedVal);
    }
    
    @Test
    public void testIsLockedAndReadOnly_ForExpiredLock()
    {
        when(lockService.getLockStatus(nodeRef)).thenReturn(LockStatus.LOCK_EXPIRED);
        
        boolean returnedVal = LockUtils.isLockedAndReadOnly(nodeRef, lockService);
        assertEquals(false, returnedVal);
    }
    
    @Test
    public void testIsLockedAndReadOnly_ForLock()
    {
        when(lockService.getLockStatus(nodeRef)).thenReturn(LockStatus.LOCKED);
        
        boolean returnedVal = LockUtils.isLockedAndReadOnly(nodeRef, lockService);
        assertEquals(true, returnedVal);
    }
}
