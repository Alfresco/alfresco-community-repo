/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

import java.io.Serializable;
import java.util.Date;

import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Value class describing the lock state of a node. Lock specific properties may
 * be added using the {@link #additionalInfo} field - <strong>objects</strong> assigned
 * to this field <strong>MUST</strong> implement hashCode and equals methods properly.
 * 
 * @author Matt Ward
 */
public final class LockState implements Serializable
{
    private static final long serialVersionUID = 1L;
    private final NodeRef nodeRef;
    private final LockType lockType;
    private final String owner;
    private final Date expires;
    private final Lifetime lifetime;
    private final String additionalInfo;

    /**
     * Constructor.
     * 
     * @param nodeRef
     * @param lockType
     * @param owner
     * @param secondsToExpire
     * @param additionalInfo
     */
    private LockState(NodeRef nodeRef, LockType lockType, String owner, Date expires,
                Lifetime lifetime, String additionalInfo)
    {
        this.nodeRef = nodeRef;
        this.lockType = lockType;
        this.owner = owner;
        this.expires = (expires == null ? null : new Date(expires.getTime()));
        this.lifetime = lifetime;
        this.additionalInfo = additionalInfo;
    }

    public static LockState createLock(NodeRef nodeRef, LockType lockType, String owner, Date expires,
                Lifetime lifetime, String additionalInfo)
    {
        return new LockState(nodeRef, lockType, owner, expires, lifetime, additionalInfo);
    }
    
    public static LockState createWithLockType(LockState lockState, LockType lockType)
    {
        return new LockState(lockState.getNodeRef(),
                             lockType,
                             lockState.getOwner(),
                             lockState.getExpires(),
                             lockState.getLifetime(),
                             lockState.getAdditionalInfo());
    }
    
    public static LockState createWithOwner(LockState lockState, String owner)
    {
        return new LockState(lockState.getNodeRef(),
                    lockState.getLockType(),
                    owner,
                    lockState.getExpires(),
                    lockState.getLifetime(),
                    lockState.getAdditionalInfo());
    }
    
    public static LockState createWithExpires(LockState lockState, Date expires)
    {
        return new LockState(lockState.getNodeRef(),
                    lockState.getLockType(),
                    lockState.getOwner(),
                    expires,
                    lockState.getLifetime(),
                    lockState.getAdditionalInfo());
    }
    
    public static LockState createWithLifetime(LockState lockState, Lifetime lifetime)
    {
        return new LockState(lockState.getNodeRef(),
                    lockState.getLockType(),
                    lockState.getOwner(),
                    lockState.getExpires(),
                    lifetime,
                    lockState.getAdditionalInfo());
    }
    
    public static LockState createWithAdditionalInfo(LockState lockState, String additionalInfo)
    {
        return new LockState(lockState.getNodeRef(),
                    lockState.getLockType(),
                    lockState.getOwner(),
                    lockState.getExpires(),
                    lockState.getLifetime(),
                    additionalInfo);
    }
    
    public static LockState createUnlocked(NodeRef nodeRef, String additionalInfo)
    {
        return new LockState(nodeRef, null, null, null, null, additionalInfo);
    }
    
    public static LockState createUnlocked(NodeRef nodeRef)
    {
        return new LockState(nodeRef, null, null, null, null, null);
    }
    
    /**
     * Returns whether this {@link LockState} is for a lock or whether there is no
     * lock defined for the node. If a lock is defined for a node, that does not mean that
     * the node <em>is</em> locked - the {@link LockService} must be used to determine that.
     * 
     * @return true if there is a lock defined for the node.
     */
    public boolean isLockInfo()
    {
        return (lockType != null);
    }
    
    public NodeRef getNodeRef()
    {
        return this.nodeRef;
    }

    public LockType getLockType()
    {
        return this.lockType;
    }

    public String getOwner()
    {
        return this.owner;
    }

    public Date getExpires()
    {
        return this.expires;
    }

    public Lifetime getLifetime()
    {
        return this.lifetime;
    }

    public String getAdditionalInfo()
    {
        return this.additionalInfo;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                    + ((this.additionalInfo == null) ? 0 : this.additionalInfo.hashCode());
        result = prime * result + ((this.expires == null) ? 0 : this.expires.hashCode());
        result = prime * result + ((this.lifetime == null) ? 0 : this.lifetime.hashCode());
        result = prime * result + ((this.lockType == null) ? 0 : this.lockType.hashCode());
        result = prime * result + ((this.nodeRef == null) ? 0 : this.nodeRef.hashCode());
        result = prime * result + ((this.owner == null) ? 0 : this.owner.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        LockState other = (LockState) obj;
        if (this.additionalInfo == null)
        {
            if (other.additionalInfo != null) return false;
        }
        else if (!this.additionalInfo.equals(other.additionalInfo)) return false;
        if (this.expires == null)
        {
            if (other.expires != null) return false;
        }
        else if (!this.expires.equals(other.expires)) return false;
        if (this.lifetime != other.lifetime) return false;
        if (this.lockType != other.lockType) return false;
        if (this.nodeRef == null)
        {
            if (other.nodeRef != null) return false;
        }
        else if (!this.nodeRef.equals(other.nodeRef)) return false;
        if (this.owner == null)
        {
            if (other.owner != null) return false;
        }
        else if (!this.owner.equals(other.owner)) return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "LockState [nodeRef=" + this.nodeRef + ", lockType=" + this.lockType + ", owner="
                    + this.owner + ", expires=" + this.expires + ", lifetime=" + this.lifetime
                    + ", additionalInfo=" + this.additionalInfo + "]";
    }
}
