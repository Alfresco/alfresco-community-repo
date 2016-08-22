/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
    private VirtualStore smartStore;

    public VirtualLockServiceExtension()
    {
        super(LockServiceTrait.class);
    }

    public void setSmartStore(VirtualStore smartStore)
    {
        this.smartStore = smartStore;
    }

    @Override
    public void lock(NodeRef nodeRef, LockType lockType) throws UnableToAquireLockException
    {
        getTrait().lock(smartStore.materializeIfPossible(nodeRef),
                        lockType);
    }

    @Override
    public void lock(NodeRef nodeRef, LockType lockType, int timeToExpire) throws UnableToAquireLockException
    {
        getTrait().lock(smartStore.materializeIfPossible(nodeRef),
                        lockType,
                        timeToExpire);
    }

    @Override
    public void lock(NodeRef nodeRef, LockType lockType, int timeToExpire, Lifetime lifetime)
                throws UnableToAquireLockException
    {
        getTrait().lock(smartStore.materializeIfPossible(nodeRef),
                        lockType,
                        timeToExpire,
                        lifetime);
    }

    @Override
    public void lock(NodeRef nodeRef, LockType lockType, int timeToExpire, Lifetime lifetime, String additionalInfo)
                throws UnableToAquireLockException
    {
        getTrait().lock(smartStore.materializeIfPossible(nodeRef),
                        lockType,
                        timeToExpire,
                        lifetime,
                        additionalInfo);
    }

    @Override
    public void lock(NodeRef nodeRef, LockType lockType, int timeToExpire, boolean lockChildren)
                throws UnableToAquireLockException
    {
        getTrait().lock(smartStore.materializeIfPossible(nodeRef),
                        lockType,
                        timeToExpire,
                        lockChildren);
    }

    @Override
    public void lock(NodeRef nodeRef, LockType lockType, int timeToExpire, Lifetime lifetime, boolean lockChildren) 
               throws UnableToAquireLockException
    {
        getTrait().lock(smartStore.materializeIfPossible(nodeRef),
                lockType,
                timeToExpire,
                lifetime,
                lockChildren);
    }

    @Override
    public void lock(Collection<NodeRef> nodeRefs, LockType lockType, int timeToExpire)
                throws UnableToAquireLockException
    {
        getTrait().lock(smartStore.materializeIfPossible(nodeRefs),
                        lockType,
                        timeToExpire);
    }

    @Override
    public void unlock(NodeRef nodeRef) throws UnableToReleaseLockException
    {
        getTrait().unlock(smartStore.materializeIfPossible(nodeRef));
    }

    @Override
    public void unlock(NodeRef nodeRef, boolean lockChildren) throws UnableToReleaseLockException
    {
        getTrait().unlock(smartStore.materializeIfPossible(nodeRef),
                          lockChildren);
    }

    @Override
    public void unlock(NodeRef nodeRef, boolean lockChildren, boolean allowCheckedOut)
                throws UnableToReleaseLockException
    {
        getTrait().unlock(smartStore.materializeIfPossible(nodeRef),
                          lockChildren,
                          allowCheckedOut);
    }

    @Override
    public void unlock(Collection<NodeRef> nodeRefs) throws UnableToReleaseLockException
    {
        getTrait().unlock(smartStore.materializeIfPossible(nodeRefs));
    }

    @Override
    public LockStatus getLockStatus(NodeRef nodeRef)
    {
        return getTrait().getLockStatus(smartStore.materializeIfPossible(nodeRef));
    }

    @Override
    public LockStatus getLockStatus(NodeRef nodeRef, String userName)
    {
        return getTrait().getLockStatus(smartStore.materializeIfPossible(nodeRef),
                                        userName);
    }

    @Override
    public LockType getLockType(NodeRef nodeRef)
    {
        return getTrait().getLockType(smartStore.materializeIfPossible(nodeRef));
    }

    @Override
    public void checkForLock(NodeRef nodeRef)
    {
        getTrait().checkForLock(smartStore.materializeIfPossible(nodeRef));
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
        return getTrait().getAdditionalInfo(smartStore.materializeIfPossible(nodeRef));
    }

    @Override
    public LockState getLockState(NodeRef nodeRef)
    {
        return getTrait().getLockState(smartStore.materializeIfPossible(nodeRef));
    }

    @Override
    public void setEphemeralExpiryThreshold(int threshSecs)
    {
        getTrait().setEphemeralExpiryThreshold(threshSecs);
    }

    @Override
    public boolean isLocked(NodeRef nodeRef)
    {
        return getTrait().isLocked(smartStore.materializeIfPossible(nodeRef));
    }

    @Override
    public boolean isLockedAndReadOnly(NodeRef nodeRef)
    {
        return getTrait().isLockedAndReadOnly(smartStore.materializeIfPossible(nodeRef));
    }
}
