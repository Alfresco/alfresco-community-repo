package org.alfresco.repo.lock.mem;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ParameterCheck;
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
        ParameterCheck.mandatory("nodeRef", nodeRef);
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
