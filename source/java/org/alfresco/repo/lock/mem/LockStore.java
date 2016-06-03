package org.alfresco.repo.lock.mem;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Defines the in-memory lock storage interface.
 * <p>
 * Operations MUST be thread-safe.
 * 
 * @author Matt Ward
 */
public interface LockStore
{
    LockState get(NodeRef nodeRef);
    void set(NodeRef nodeRef, LockState lockState);
    public Set<NodeRef> getNodes();
    
    /**
     * WARNING: only use in test code - unsafe method for production use.
     * 
     * TODO: remove this method?
     */
    void clear();
}
