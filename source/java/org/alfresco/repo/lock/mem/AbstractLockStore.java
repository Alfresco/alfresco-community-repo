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

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.alfresco.repo.lock.LockUtils;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.UnableToAquireLockException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Base class for LockStore implementations that use a ConcurrentMap as storage.
 * 
 * @author Matt Ward
 */
public abstract class AbstractLockStore<T extends ConcurrentMap<NodeRef, LockState>> implements LockStore
{
    protected T map;
    
    public AbstractLockStore(T map)
    {
        this.map = map;
    }

    @Override
    public LockState get(NodeRef nodeRef)
    {
        LockState lockState;
        Map<NodeRef, LockState> txMap = getTxMap();
        if (txMap != null && txMap.containsKey(nodeRef))
        {
            // The transactional map is able to provide the LockState
            lockState = txMap.get(nodeRef);
        }
        else
        {
            lockState = map.get(nodeRef);
            if (txMap != null)
            {
                // As the txMap doesn't have the LockState, cache it for later.
                txMap.put(nodeRef, lockState);
            }
        }
        return lockState;
    }

    @Override
    public void set(NodeRef nodeRef, LockState lockState)
    {
        Map<NodeRef, LockState> txMap = getTxMap();
        LockState previousLockState = null;
        if (txMap != null)
        {
            if (txMap.containsKey(nodeRef))
            {
                // There is known previous state.
                previousLockState = txMap.get(nodeRef);
            }
            else
            {
                // No previous known state - get the current state, this becomes
                // the previous known state.
                previousLockState = get(nodeRef);
            }
        }
        else
        {
            // No transaction, but we still need to know the previous state, before attempting
            // to set new state.
            previousLockState = get(nodeRef);
        }
        
        // Has the lock been succesfully placed into the lock store?
        boolean updated = false;
        
        if (previousLockState != null)
        {
            String userName = AuthenticationUtil.getFullyAuthenticatedUser();
            String owner = previousLockState.getOwner();
            Date expires = previousLockState.getExpires();
            if (LockUtils.lockStatus(userName, owner, expires) == LockStatus.LOCKED)
            {
                throw new UnableToAquireLockException(nodeRef);
            }            
            // Use ConcurrentMap.replace(key, old, new) so that we can ensure we don't encounter a
            // 'lost update' (i.e. someone else has locked a node while we were thinking about it).
            updated = map.replace(nodeRef, previousLockState, lockState);
        }
        else
        {
            if (map.putIfAbsent(nodeRef, lockState) == null)
            {
                updated = true;
            }
        }
        
        if (!updated)
        {
            String msg = String.format("Attempt to update lock state failed, old=%s, new=%s, noderef=%s",
                        previousLockState, lockState, nodeRef);
            throw new ConcurrencyFailureException(msg);
        }
        else
        {
            // Keep the new value for future reads within this TX.
            if (txMap != null)
            {
                txMap.put(nodeRef, lockState);
            }
        }
    }


    @Override
    public void clear()
    {
        map.clear();
        Map<NodeRef, LockState> txMap = getTxMap();
        if (txMap != null)
        {
            txMap.clear();
        }
    }

    /**
     * Returns a transactionally scoped Map that is used to provide repeatable lock store queries
     * for a given NodeRef. If no transaction is present, then null is returned.
     * 
     * @return Transactional Map or null if not available.
     */
    protected Map<NodeRef, LockState> getTxMap()
    {
        if (!TransactionSynchronizationManager.isSynchronizationActive())
        {
            return null;
        }
        Map<NodeRef, LockState> map = TransactionalResourceHelper.getMap(getClass().getName()+".repeatableReadMap");
        return map;
    }
    
    @Override
    public Set<NodeRef> getNodes()
    {
        return map.keySet();
    }
}
