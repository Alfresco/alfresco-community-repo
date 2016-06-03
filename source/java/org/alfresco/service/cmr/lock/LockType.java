package org.alfresco.service.cmr.lock;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * The type of lock to be used by the lock service
 * <p>
 * The lock owner or the administrator can release the lock.
 */
@AlfrescoPublicApi
public enum LockType
{
    /**
     * No-one can update or delete the locked node. No one can add children to the locked node.
     *
     * No-one can update or delete the locked node. 
     * <p>
     * No one can add children to the locked node.
     *
    * @deprecated Deprecated in 4.1.6.  Will be replaced by a more descriptive name.
     */
    @Deprecated
    READ_ONLY_LOCK,
    /**
     * READ_ONLY_LOCK - no-one can update or delete the locked node. No one can add children to the locked node.
     * 
     * @deprecated Deprecated in 4.1.6.  Will be replaced by a more descriptive name.
     */
    @Deprecated
    WRITE_LOCK,

    /**


     * No-one can update or delete the locked node.    
     * <p>
     * There are no restrictions on adding children to the locked node.
     * 
     * @deprecated Deprecated in 4.1.6.  Will be replaced by a more descriptive name.
     */
    @Deprecated
    NODE_LOCK
}