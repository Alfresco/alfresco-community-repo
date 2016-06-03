package org.alfresco.repo.lock.mem;

/**
 * Specifies the lifetime of a node lock, e.g. ephemeral or persistent.
 * 
 * @author Matt Ward
 */
public enum Lifetime
{
    EPHEMERAL,    // Locks are stored in volatile memory only.
    PERSISTENT    // Locks are stored in memory and also persisted.
}
