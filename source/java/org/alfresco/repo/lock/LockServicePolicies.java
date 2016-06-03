package org.alfresco.repo.lock;

import org.alfresco.repo.policy.ClassPolicy;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Policy interfaces for the lock service
 * 
 * @author Ray Gauss II
 * @since 4.1.6
 */
public interface LockServicePolicies
{
    /**
     * Policy for behavior before a lock is made.
     */
    public interface BeforeLock extends ClassPolicy
    {
        static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "beforeLock");
          
        /**
         * Called before an attempt to lock the given node is made.
         * 
         * @param nodeRef NodeRef
         * @param lockType LockType
         */
        void beforeLock(
                NodeRef nodeRef, 
                LockType lockType);
    }

}
