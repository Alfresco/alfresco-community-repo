/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.security.permissions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.alfresco.repo.security.permissions.PermissionCheckCollection.PermissionCheckCollectionMixin;

/**
 * Tests {@link PermissionCheckCollection}
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class PermissionCheckCollectionTest extends TestCase
{
    @Override
    protected void setUp() throws Exception
    {
    }

    public void testBasicWrapping() throws Exception
    {
        List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(2);
        list.add(3);
        
        final int targetResultCount = 2000;
        final long cutOffAfterTimeMs = 10000L;
        final int cutOffAfterCount = 1000;
        
        Collection<Integer> proxiedList = PermissionCheckCollectionMixin.create(list, targetResultCount, cutOffAfterTimeMs, cutOffAfterCount);
        
        // Check
        assertTrue("Proxied object must still be a List", proxiedList instanceof List);
        assertEquals("List values incorrect", 3, proxiedList.size());
        assertTrue("Proxied object must also be a PermissionCheckCollection", proxiedList instanceof PermissionCheckCollection);
        @SuppressWarnings("unchecked")
        PermissionCheckCollection<String> proxiedPermissionCheckCollection = (PermissionCheckCollection<String>) proxiedList;
        assertEquals("targetResultCount value incorrect", targetResultCount, proxiedPermissionCheckCollection.getTargetResultCount());
        assertEquals("cutOffAfterTimeMs value incorrect", cutOffAfterTimeMs, proxiedPermissionCheckCollection.getCutOffAfterTimeMs());
        assertEquals("cutOffAfterCount value incorrect", cutOffAfterCount, proxiedPermissionCheckCollection.getCutOffAfterCount());
    }
    
    public void testVolumeWrapping() throws Exception
    {
        int count = 10000;
        long before = System.nanoTime();
        for (int i = 0; i < count; i++)
        {
            testBasicWrapping();
        }
        long after = System.nanoTime();
        double average = ((double) (after - before) / (double) count) / (double) 1.0E6;
        System.out.println("Average is " + average + "ms per wrap.");
    }
}
