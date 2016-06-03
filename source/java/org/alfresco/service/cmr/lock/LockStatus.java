package org.alfresco.service.cmr.lock;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Used to indicate lock status.
 * 
 * <ul>
 * <li>NO_LOCK - Indicates that there is no lock present</li>
 * <li>LOCKED - Indicates that the node is locked by somebody else</li>
 * <li>LOCK_OWNER - Indicates that the node is locked and the current user has lock ownership rights</li>
 * <li>LOCK_EXPIRED - Indicates that the lock has expired and the node can be considered to be unlocked</li>
 * </ul>
 * 
 * @author Roy Wetherall
 */
@AlfrescoPublicApi
public enum LockStatus 
{
    /**
     * Indicates that there is no lock present 
     */
    NO_LOCK, 
    /**
     * Indicates that the node is locked
     */
    LOCKED,
    /**
     * Indicates that the node is locked and you have lock ownership rights 
     */
    LOCK_OWNER, 
    /**
     * Indicates that the lock has expired and the node can be considered to be unlocked
     */
    LOCK_EXPIRED 
}