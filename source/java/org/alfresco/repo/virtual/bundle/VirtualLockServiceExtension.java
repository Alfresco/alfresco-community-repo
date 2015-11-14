/* 
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.repo.virtual.bundle;

import java.util.Collection;

import org.alfresco.repo.lock.mem.Lifetime;
import org.alfresco.repo.lock.mem.LockState;
import org.alfresco.repo.lock.traitextender.LockServiceExtension;
import org.alfresco.repo.lock.traitextender.LockServiceTrait;
import org.alfresco.repo.virtual.store.VirtualStore;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.lock.UnableToAquireLockException;
import org.alfresco.service.cmr.lock.UnableToReleaseLockException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.traitextender.SpringBeanExtension;

public class VirtualLockServiceExtension extends SpringBeanExtension<LockServiceExtension, LockServiceTrait>
            implements LockServiceExtension
{
    private VirtualStore virtualStore;

    public VirtualLockServiceExtension()
    {
        super(LockServiceTrait.class);
    }

    public void setVirtualStore(VirtualStore virtualStore)
    {
        this.virtualStore = virtualStore;
    }

    @Override
    public void lock(NodeRef nodeRef, LockType lockType) throws UnableToAquireLockException
    {
        getTrait().lock(virtualStore.materializeIfPossible(nodeRef),
                        lockType);
    }

    @Override
    public void lock(NodeRef nodeRef, LockType lockType, int timeToExpire) throws UnableToAquireLockException
    {
        getTrait().lock(virtualStore.materializeIfPossible(nodeRef),
                        lockType,
                        timeToExpire);
    }

    @Override
    public void lock(NodeRef nodeRef, LockType lockType, int timeToExpire, Lifetime lifetime)
                throws UnableToAquireLockException
    {
        getTrait().lock(virtualStore.materializeIfPossible(nodeRef),
                        lockType,
                        timeToExpire,
                        lifetime);
    }

    @Override
    public void lock(NodeRef nodeRef, LockType lockType, int timeToExpire, Lifetime lifetime, String additionalInfo)
                throws UnableToAquireLockException
    {
        getTrait().lock(virtualStore.materializeIfPossible(nodeRef),
                        lockType,
                        timeToExpire,
                        lifetime,
                        additionalInfo);
    }

    @Override
    public void lock(NodeRef nodeRef, LockType lockType, int timeToExpire, boolean lockChildren)
                throws UnableToAquireLockException
    {
        getTrait().lock(virtualStore.materializeIfPossible(nodeRef),
                        lockType,
                        timeToExpire,
                        lockChildren);
    }

    @Override
    public void lock(Collection<NodeRef> nodeRefs, LockType lockType, int timeToExpire)
                throws UnableToAquireLockException
    {
        getTrait().lock(virtualStore.materializeIfPossible(nodeRefs),
                        lockType,
                        timeToExpire);
    }

    @Override
    public void unlock(NodeRef nodeRef) throws UnableToReleaseLockException
    {
        getTrait().unlock(virtualStore.materializeIfPossible(nodeRef));
    }

    @Override
    public void unlock(NodeRef nodeRef, boolean lockChildren) throws UnableToReleaseLockException
    {
        getTrait().unlock(virtualStore.materializeIfPossible(nodeRef),
                          lockChildren);
    }

    @Override
    public void unlock(NodeRef nodeRef, boolean lockChildren, boolean allowCheckedOut)
                throws UnableToReleaseLockException
    {
        getTrait().unlock(virtualStore.materializeIfPossible(nodeRef),
                          lockChildren,
                          allowCheckedOut);
    }

    @Override
    public void unlock(Collection<NodeRef> nodeRefs) throws UnableToReleaseLockException
    {
        getTrait().unlock(virtualStore.materializeIfPossible(nodeRefs));
    }

    @Override
    public LockStatus getLockStatus(NodeRef nodeRef)
    {
        return getTrait().getLockStatus(virtualStore.materializeIfPossible(nodeRef));
    }

    @Override
    public LockStatus getLockStatus(NodeRef nodeRef, String userName)
    {
        return getTrait().getLockStatus(virtualStore.materializeIfPossible(nodeRef),
                                        userName);
    }

    @Override
    public LockType getLockType(NodeRef nodeRef)
    {
        return getTrait().getLockType(virtualStore.materializeIfPossible(nodeRef));
    }

    @Override
    public void checkForLock(NodeRef nodeRef)
    {
        getTrait().checkForLock(virtualStore.materializeIfPossible(nodeRef));
    }

    @Override
    public void suspendLocks()
    {
        getTrait().suspendLocks();
    }

    @Override
    public void enableLocks()
    {
        getTrait().enableLocks();
    }

    @Override
    public String getAdditionalInfo(NodeRef nodeRef)
    {
        return getTrait().getAdditionalInfo(virtualStore.materializeIfPossible(nodeRef));
    }

    @Override
    public LockState getLockState(NodeRef nodeRef)
    {
        return getTrait().getLockState(virtualStore.materializeIfPossible(nodeRef));
    }

    @Override
    public void setEphemeralExpiryThreshold(int threshSecs)
    {
        getTrait().setEphemeralExpiryThreshold(threshSecs);
    }

}
