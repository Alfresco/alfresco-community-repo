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
package org.alfresco.repo.domain.locks;

import org.alfresco.util.EqualsHelper;

/**
 * Entity bean for <b>alf_lock</b> table.
 * <p>
 * These are unique (see {@link #equals(Object) equals} and {@link #hashCode() hashCode}) based
 * on the shared and exclusive resource ID combination.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class LockEntity
{
    public static final Long CONST_LONG_ZERO = new Long(0L);
    
    private Long id;
    private Long version;
    private Long sharedResourceId;
    private Long exclusiveResourceId;
    private String lockToken;
    private Long startTime;
    private Long expiryTime = Long.MIN_VALUE;           // 'expired' unless set 
    
    @Override
    public int hashCode()
    {
        return (sharedResourceId == null ? 0 : sharedResourceId.hashCode()) +
                (exclusiveResourceId == null ? 0 : exclusiveResourceId.hashCode());
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj instanceof LockEntity)
        {
            LockEntity that = (LockEntity) obj;
            return EqualsHelper.nullSafeEquals(this.sharedResourceId, that.sharedResourceId) &&
                   EqualsHelper.nullSafeEquals(this.exclusiveResourceId, that.exclusiveResourceId);
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("LockEntity")
          .append("[ ID=").append(id)
          .append(", sharedResourceId=").append(sharedResourceId)
          .append(", exclusiveResourceId=").append(exclusiveResourceId)
          .append("]");
        return sb.toString();
    }

    /**
     * Determine if the lock is logically exclusive.  A lock is <b>exclusive</b> if the
     * shared lock resource matches the exclusive lock resource.
     * 
     * @return      Returns <tt>true</tt> if the lock is exclusive or <tt>false</tt> if it is not
     */
    public boolean isExclusive()
    {
        if (sharedResourceId == null || exclusiveResourceId == null)
        {
            throw new IllegalStateException("LockEntity has not been populated");
        }
        return sharedResourceId.equals(exclusiveResourceId);
    }
    
    public boolean hasExpired()
    {
        return System.currentTimeMillis() > expiryTime;
    }
    
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getVersion()
    {
        return version;
    }

    public void setVersion(Long version)
    {
        this.version = version;
    }
    
    /**
     * Increments the version number or resets it if it reaches a large number
     */
    public void incrementVersion()
    {
        long currentVersion = version.longValue();
        if (currentVersion >= 10E6)
        {
            this.version = CONST_LONG_ZERO;
        }
        else
        {
            this.version = new Long(version.longValue() + 1L);
        }
    }

    /**
     * @return                  Returns the ID of the shared lock resource
     */
    public Long getSharedResourceId()
    {
        return sharedResourceId;
    }

    /**
     * 
     * @param sharedResourceId  the ID of the shared lock resource
     */
    public void setSharedResourceId(Long sharedResourceId)
    {
        this.sharedResourceId = sharedResourceId;
    }

    public Long getExclusiveResourceId()
    {
        return exclusiveResourceId;
    }

    public void setExclusiveResourceId(Long exclusiveResourceId)
    {
        this.exclusiveResourceId = exclusiveResourceId;
    }

    /**
     * @return              Returns the token assigned when the lock was created
     */
    public String getLockToken()
    {
        return lockToken;
    }

    /**
     * @param lockToken     the token assigned when the lock was created
     */
    public void setLockToken(String lockToken)
    {
        this.lockToken = lockToken;
    }

    /**
     * 
     * @return              Returns the time when the lock was started
     */
    public Long getStartTime()
    {
        return startTime;
    }

    public void setStartTime(Long startTime)
    {
        this.startTime = startTime;
    }

    public Long getExpiryTime()
    {
        return expiryTime;
    }

    public void setExpiryTime(Long expiryTime)
    {
        this.expiryTime = expiryTime;
    }
}
