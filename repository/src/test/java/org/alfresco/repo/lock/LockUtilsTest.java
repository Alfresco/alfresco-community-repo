/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.repo.lock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Tests for the {@link LockUtils} class.
 * 
 * @author Matt Ward
 * @deprecated essentially obsolete (isLockedAndReadOnly is now part of LockService)
 */
@RunWith(MockitoJUnitRunner.class)
public class LockUtilsTest
{
    private @Mock LockService lockService;
    private final NodeRef nodeRef = new NodeRef("workspace://SpacesStore/node-id");

    @Test
    public void testIsLockedAndReadOnly_ForLockOwnerWithNullLockType()
    {
        when(lockService.isLockedAndReadOnly(nodeRef)).thenReturn(true);

        boolean returnedVal = LockUtils.isLockedAndReadOnly(nodeRef, lockService);
        assertEquals(true, returnedVal);
    }

    @Test
    public void testIsLockedAndReadOnly_ForLockOwnerWithWriteLockType()
    {
        when(lockService.isLockedAndReadOnly(nodeRef)).thenReturn(false);

        boolean returnedVal = LockUtils.isLockedAndReadOnly(nodeRef, lockService);
        assertEquals(false, returnedVal);
    }

    @Test
    public void testIsLockedAndReadOnly_ForLockOwnerWithNodeLockType()
    {
        when(lockService.isLockedAndReadOnly(nodeRef)).thenReturn(true);

        boolean returnedVal = LockUtils.isLockedAndReadOnly(nodeRef, lockService);
        assertEquals(true, returnedVal);
    }

    @Test
    public void testIsLockedAndReadOnly_ForLockOwnerWithReadOnlyLockType()
    {
        when(lockService.isLockedAndReadOnly(nodeRef)).thenReturn(true);

        boolean returnedVal = LockUtils.isLockedAndReadOnly(nodeRef, lockService);
        assertEquals(true, returnedVal);
    }

    @Test
    public void testIsLockedAndReadOnly_ForNoLock()
    {
        when(lockService.isLockedAndReadOnly(nodeRef)).thenReturn(false);

        boolean returnedVal = LockUtils.isLockedAndReadOnly(nodeRef, lockService);
        assertEquals(false, returnedVal);
    }

    @Test
    public void testIsLockedAndReadOnly_ForExpiredLock()
    {
        when(lockService.isLockedAndReadOnly(nodeRef)).thenReturn(false);

        boolean returnedVal = LockUtils.isLockedAndReadOnly(nodeRef, lockService);
        assertEquals(false, returnedVal);
    }

    @Test
    public void testIsLockedAndReadOnly_ForLock()
    {
        when(lockService.isLockedAndReadOnly(nodeRef)).thenReturn(true);

        boolean returnedVal = LockUtils.isLockedAndReadOnly(nodeRef, lockService);
        assertEquals(true, returnedVal);
    }
}
