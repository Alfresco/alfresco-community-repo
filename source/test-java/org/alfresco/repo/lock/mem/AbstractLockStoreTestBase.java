/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.lock.mem;

import static org.junit.Assert.*;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.LockHelper.LockTryException;
import org.junit.Before;
import org.junit.Test;

/**
 * Abstract base class for testing {@link LockStore} implementations. Subclasses must
 * implement the createLockStore method and will inherit a set of suitable tests.
 * 
 * @author Matt Ward
 */
public abstract class AbstractLockStoreTestBase<T extends LockStore>
{    
    /**
     * Instance of the Class Under Test.
     */
    protected T lockStore;
    
    /**
     * Concrete subclasses must implement this method to provide the tests with a LockStore instance.
     * 
     * @return LockStore to test
     */
    protected abstract T createLockStore();
    
    @Before
    public void setUpLockStore()
    {
        lockStore = createLockStore();
    }
    
    @Test
    public void testSetAndGet()
    {
        NodeRef ephemeralNodeRef = new NodeRef("workspace://SpacesStore/12345");
        LockState ephemeralLock = LockState.createLock(
                    ephemeralNodeRef, LockType.NODE_LOCK, "owner", null, Lifetime.EPHEMERAL, null);
        
        NodeRef persistentNodeRef = new NodeRef("workspace://SpacesStore/5838743");
        LockState persistentLock = LockState.createLock(
                    persistentNodeRef, LockType.NODE_LOCK, "owner", null, Lifetime.PERSISTENT, null);
        
        lockStore.set(ephemeralNodeRef, ephemeralLock);
        lockStore.set(persistentNodeRef, persistentLock);
        
        LockState newLockState = lockStore.get(ephemeralNodeRef);
        assertEquals(ephemeralLock, newLockState);
        
        newLockState = lockStore.get(persistentNodeRef);
        assertEquals(persistentLock, newLockState);
    }

    @Test
    public void testContains()
    {
        NodeRef nodeRef1 = new NodeRef("workspace://SpacesStore/12345");
        LockState lock1 = LockState.createLock(nodeRef1, LockType.NODE_LOCK, "owner", null, Lifetime.EPHEMERAL, null);
        
        NodeRef nodeRef2 = new NodeRef("workspace://SpacesStore/5838743");
        LockState lock2 = LockState.createLock(nodeRef2, LockType.NODE_LOCK, "owner", null, Lifetime.PERSISTENT, null);
        
        NodeRef nodeRef3 = new NodeRef("workspace://SpacesStore/65752323");
        
        lockStore.set(nodeRef1, lock1);
        lockStore.set(nodeRef2, lock2);
        
        assertNotNull(lockStore.get(nodeRef1));
        assertNotNull(lockStore.get(nodeRef2));
        assertNull(lockStore.get(nodeRef3));
    }

    @Test
    public void testClear()
    {
        NodeRef nodeRef1 = new NodeRef("workspace://SpacesStore/12345");
        LockState lock1 = LockState.createLock(nodeRef1, LockType.NODE_LOCK, "owner", null, Lifetime.EPHEMERAL, null);
        
        NodeRef nodeRef2 = new NodeRef("workspace://SpacesStore/5838743");
        LockState lock2 = LockState.createLock(nodeRef2, LockType.NODE_LOCK, "owner", null, Lifetime.PERSISTENT, null);
        
        lockStore.set(nodeRef1, lock1);
        lockStore.set(nodeRef2, lock2);
        
        assertNotNull(lockStore.get(nodeRef1));
        assertNotNull(lockStore.get(nodeRef2));
        
        lockStore.clear();
        
        assertNull(lockStore.get(nodeRef1));
        assertNull(lockStore.get(nodeRef2));
    }

    @Test
    public void testGetNodes()
    {
        NodeRef nodeRef1 = new NodeRef("workspace://SpacesStore/1");
        LockState lock1 = LockState.createLock(nodeRef1, LockType.NODE_LOCK, "owner", null, Lifetime.EPHEMERAL, null);
        
        NodeRef nodeRef2 = new NodeRef("workspace://SpacesStore/2");
        LockState lock2 = LockState.createLock(nodeRef2, LockType.NODE_LOCK, "owner", null, Lifetime.PERSISTENT, null);
        
        NodeRef nodeRef3 = new NodeRef("workspace://SpacesStore/3");
        LockState lock3 = LockState.createLock(nodeRef3, LockType.NODE_LOCK, "owner", null, Lifetime.EPHEMERAL, null);
        
        NodeRef nodeRef4 = new NodeRef("workspace://SpacesStore/4");
        LockState lock4 = LockState.createLock(nodeRef4, LockType.NODE_LOCK, "owner", null, Lifetime.PERSISTENT, null);
        
        lockStore.set(nodeRef1, lock1);
        lockStore.set(nodeRef2, lock2);
        lockStore.set(nodeRef3, lock3);
        lockStore.set(nodeRef4, lock4);

        Set<NodeRef> unorderedNodes = lockStore.getNodes();
        Set<NodeRef> nodes = new TreeSet<NodeRef>(new Comparator<NodeRef>()
        {
            @Override
            public int compare(NodeRef o1, NodeRef o2)
            {
                return o1.toString().compareTo(o2.toString());
            }
        });
        nodes.addAll(unorderedNodes);
        Iterator<NodeRef> it = nodes.iterator();
        assertEquals(nodeRef1, it.next());
        assertEquals(nodeRef2, it.next());
        assertEquals(nodeRef3, it.next());
        assertEquals(nodeRef4, it.next());
    }
}
