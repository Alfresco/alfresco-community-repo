/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.webdav;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.repo.lock.mem.LockStore;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Class to represent a WebDAV lock info. Instances of this class are accessible
 * my multiple threads as they are kept in the {@link LockStore}. Clients of this
 * class are expected to synchronise externally using the provided
 * ReentrantReadWriteLock (use {@link #getRWLock()}).
 * 
 * @author Ivan Rybnikov
 *
 */
public class LockInfoImpl implements Serializable, LockInfo
{
    private static final long serialVersionUID = 1L;
    
    // Exclusive lock token
    private String exclusiveLockToken = null;

    // Lock scope
    private String scope = null;

    // Lock depth
    private String depth = null;

    // Shared lock tokens
    private final Set<String> sharedLockTokens = new HashSet<String>(3);

    // User name of the lock's owner
    private String owner;
    
    // When does the lock expire?
    private Date expires;
    
    /**
     * Default constructor
     * 
     */
    public LockInfoImpl()
    {
    }

    /**
     * Constructor
     * 
     * @param token Exclusive lock token
     * @param scope Lock scope (shared/exclusive)
     * @param depth Lock depth (0/infinity)
     */
    public LockInfoImpl(String token, String scope, String depth)
    {
        this.exclusiveLockToken = token;
        this.scope = scope;
        this.depth = depth;
    }
    
    /**
     * Returns true if node has shared or exclusive locks
     * 
     * @return boolean
     */
    @Override
    @JsonIgnore
    public boolean isLocked()
    {
        // TODO: return (owner != null || isExclusive() || isShared());
        return (isExclusive() || isShared());
    }
    
    /**
     * Setter for exclusive lock token
     * 
     * @param token Lock token
     */
    @Override
    public void setExclusiveLockToken(String token)
    {
        if (isShared())
        {
            throw new IllegalStateException("Cannot add exclusive lock token [" + token +
                        "] to shared lock [" + toString() + "]");
        }
        this.exclusiveLockToken = token;
    }

    /**
     * Getter for exclusive lock token.
     * 
     * @return String
     */
    @Override
    public String getExclusiveLockToken()
    {
        return exclusiveLockToken;
    }

    /**
     * Setter for lock scope.
     * 
     * @param scope
     */
    @Override
    public void setScope(String scope)
    {
        this.scope = scope;
    }

    /**
     * Returns lock scope
     * 
     * @return lock scope
     */
    @Override
    public String getScope()
    {
        return scope == null ? WebDAV.XML_EXCLUSIVE : scope;
    }

    /**
     * Setter for lock depth
     * 
     * @param depth lock depth
     */
    @Override
    public void setDepth(String depth)
    {
        this.depth = depth;
    }

    /**
     * Returns lock depth
     * 
     * @return lock depth
     */
    @Override
    public String getDepth()
    {
        return depth;
    }

    /**
     * Getter for sharedLockTokens list.
     * 
     * @return LinkedList<String>
     */
    @Override
    public Set<String> getSharedLockTokens()
    {
        return sharedLockTokens;
    }

    /**
     * Setter for sharedLockTokens list.
     * 
     * @param sharedLockTokens
     */
    @Override
    public void setSharedLockTokens(Set<String> sharedLockTokens)
    {
        if (!sharedLockTokens.isEmpty() && isExclusive())
        {
            throw new IllegalStateException("Cannot add shared lock tokens [" + sharedLockTokens +
                        "] to exclusive lock [" + toString() + "]");
        }
        this.sharedLockTokens.clear();
        this.sharedLockTokens.addAll(sharedLockTokens);
    }

    /**
     * Adds new shared lock token to sharedLockTokens list.
     * 
     * @param token The token to add.
     */
    @Override
    public void addSharedLockToken(String token)
    {
        if (isExclusive())
        {
            throw new IllegalStateException("Cannot add shared lock token [" + token +
                        "] to exclusive lock [" + toString() + "]");
        }
        sharedLockTokens.add(token);
    }

    /**
     * Is it a shared lock?
     * 
     * @return true if shared.
     */
    @Override
    @JsonIgnore
    public boolean isShared()
    {
        return (!sharedLockTokens.isEmpty());
    }
    
    
    /**
     * Return the lock info as a string
     * 
     * @return String
     */
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        
        str.append("LockInfo[");
        
        str.append("exclusiveLockToken=");
        str.append(getExclusiveLockToken());
        str.append(", scope=");
        str.append(getScope());
        str.append(", depth=");
        str.append(getDepth());
        str.append(", sharedLockTokens=");
        str.append(getSharedLockTokens());
        str.append(", owner=");
        str.append(owner);
        str.append(", expires=");
        str.append(expires);
        
        str.append("]");
        
        return str.toString();
    }

    
    @Override
    public String toJSON()
    {
        ObjectMapper objectMapper = new ObjectMapper();
        String json;
        try
        {
            json = objectMapper.writeValueAsString(this);
        }
        catch (Throwable e)
        {
            throw new RuntimeException("Unable to generate JSON for " + toString(), e);
        }
        return json;
    }

    public static LockInfo fromJSON(String json)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        try
        {
            LockInfo lockInfo = objectMapper.readValue(json, LockInfoImpl.class);
            return lockInfo;
        }
        catch (Throwable e)
        {
            throw new RuntimeException("Unable to parse JSON from [" + json + "]", e);
        }
    }
    
    /**
     * Whether this lock has expired. If no expiry is set (i.e. expires is null)
     * then false is always returned.
     * 
     * @return true if expired.
     */
    @Override
    @JsonIgnore
    public boolean isExpired()
    {
        if (expires == null)
        {
            return false;
        }
        Date now = dateNow();
        return now.after(expires);
    }

    /**
     * Is it an exclusive lock?
     * 
     * @return true if exclusive.
     */
    @Override
    @JsonIgnore
    public boolean isExclusive()
    {
        return (exclusiveLockToken != null && exclusiveLockToken.length() > 0);
    }

    /**
     * Who owns the lock?
     * 
     * @return the owner
     */
    @Override
    @JsonIgnore
    public String getOwner()
    {
        return owner;
    }

    /**
     * Set the username of who owns the lock.
     * 
     * @param owner Owner's username
     */
    @Override
    @JsonIgnore
    public void setOwner(String owner)
    {
        this.owner = owner;
    }

    /**
     * Set the expiry date/time for this lock. Set to null for never expires.
     * 
     * @param expires the expires to set
     */
    @Override
    @JsonIgnore
    public void setExpires(Date expires)
    {
        this.expires = expires;
    }

    /**
     * Retrieve the expiry date/time for this lock, or null if it never expires.
     * 
     * @return the expires
     */
    @Override
    @JsonIgnore
    public Date getExpires()
    {
        return expires;
    }
    
    /**
     * Remaining time before lock expires, in seconds.
     */
    @Override
    @JsonIgnore
    public long getRemainingTimeoutSeconds()
    {
        Date expires = getExpires();
        if (expires == null)
        {
            return WebDAV.TIMEOUT_INFINITY;
        }
        else
        {
            Date now = dateNow();
            long timeout = ((expires.getTime() - now.getTime()) / 1000);
            return timeout;
        }
    }

    /**
     * Sets the expiry date/time to lockTimeout seconds into the future. Provide
     * a lockTimeout of WebDAV.TIMEOUT_INFINITY for never expires.
     * 
     * @param lockTimeoutSecs
     */
    @Override
    @JsonIgnore
    public void setTimeoutSeconds(int lockTimeoutSecs)
    {
        if (lockTimeoutSecs == WebDAV.TIMEOUT_INFINITY)
        {
            setExpires(null);
        }
        else
        {
            int timeoutMillis = (lockTimeoutSecs * 1000);
            Date now = dateNow();
            Date nextExpiry = new Date(now.getTime() + timeoutMillis);
            setExpires(nextExpiry);
        }
    }
    
    /**
     * Sets the expiry date/time to lockTimeout minutes into the future. Provide
     * a lockTimeout of WebDAV.TIMEOUT_INFINITY for never expires.
     * 
     * @param lockTimeoutMins
     */
    @Override
    @JsonIgnore
    public void setTimeoutMinutes(int lockTimeoutMins)
    {
        if (lockTimeoutMins != WebDAV.TIMEOUT_INFINITY)
        {
            setTimeoutSeconds(lockTimeoutMins * 60); 
        }
        else
        {
            setTimeoutSeconds(WebDAV.TIMEOUT_INFINITY);
        }
    }
    
    /**
     * Hook to allow unit testing - gets the current date/time.
     * 
     * @return Date
     */
    protected Date dateNow()
    {
        return new Date();
    }
}
