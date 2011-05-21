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

import java.util.HashSet;
import java.util.Set;

/**
 * Class to represent a WebDAV lock info
 * 
 * @author Ivan Rybnikov
 *
 */
public class LockInfo
{
    // Exclusive lock token
    private String token = null;

    // Lock scope
    private String scope = null;

    // Lock depth
    private String depth = null;

    // If lock is shared
    private boolean shared = false;

    // Shared lock tokens
    private Set<String> sharedLockTokens = null;

    // Shared lock token separator
    private static final String SHARED_LOCK_TOKEN_SEPARATOR = ",";

    /**
     * Default constructor
     * 
     */
    public LockInfo()
    {
    }

    /**
     * Constructor
     * 
     * @param token Exclusive lock token
     * @param scope Lock scope (shared/exclusive)
     * @param depth Lock depth (0/infinity)
     */
    public LockInfo(String token, String scope, String depth)
    {
        this.token = token;
        this.scope = scope;
        this.depth = depth;
    }

    /**
     * Returns true if node has shared or exclusive locks
     * 
     * @return boolean
     */
    public boolean isLocked()
    {
        if (token != null || (sharedLockTokens != null && !sharedLockTokens.isEmpty()))
        {
            return true;
        }
        return false;
    }
    
    /**
     * Setter for exclusive lock token
     * 
     * @param token Lock token
     */
    public void setToken(String token)
    {
        this.token = token;
    }

    /**
     * Getter for exclusive lock token
     * @return
     */
    public String getToken()
    {
        return token;
    }

    /**
     * Setter for lock scope.
     * 
     * @param scope
     */
    public void setScope(String scope)
    {
        this.scope = scope;
    }

    /**
     * Returns lock scope
     * 
     * @return lock scope
     */
    public String getScope()
    {
        return scope == null ? WebDAV.XML_EXCLUSIVE : scope;
    }

    /**
     * Setter for lock depth
     * 
     * @param depth lock depth
     */
    public void setDepth(String depth)
    {
        this.depth = depth;
    }

    /**
     * Returns lock depth
     * 
     * @return lock depth
     */
    public String getDepth()
    {
        return depth;
    }

    /**
     * Transforms shared lock tokens string to list. 
     * 
     * @param sharedLockTokens String contains all node's shared lock tokens
     *                         divided with SHARED_LOCK_TOKEN_SEPARATOR value.
     * @return List of shared lock tokens
     */
    public static Set<String> parseSharedLockTokens(String sharedLockTokens)
    {
        if (sharedLockTokens == null)
        {
            return null;
        }
        
        String[] sl = sharedLockTokens.split(SHARED_LOCK_TOKEN_SEPARATOR);
        Set<String> result = new HashSet<String>(sl.length * 2);
        for (int i = 0; i < sl.length; i++)
        {
            result.add(sl[i]);
        }

        return result;
    }

    /**
     * Getter for sharedLockTokens list
     * 
     * @return LinkedList<String>
     */
    public Set<String> getSharedLockTokens()
    {
        return sharedLockTokens;
    }

    /**
     * Setter for sharedLockTokens list
     * 
     * @param sharedLockTokens
     */
    public void setSharedLockTokens(Set<String> sharedLockTokens)
    {
        this.sharedLockTokens = sharedLockTokens;
    }

    /**
     * Adds new shared lock token to sharedLockTokens list
     * 
     * @param token new token
     */
    public void addSharedLockToken(String token)
    {
        if (sharedLockTokens == null)
        {
            sharedLockTokens = new HashSet<String>(3);
        }
        sharedLockTokens.add(token);
    }

    /**
     * Transforms list of shared locks to string.
     * Lock tokens separated with SHARED_LOCK_TOKEN_SEPARATOR value.
     * 
     * @param lockTokens list of shared locks
     * @return String
     */
    public static String makeSharedLockTokensString(Set<String> lockTokens)
    {
        StringBuilder str = new StringBuilder();

        boolean first = true;
        for (String token : lockTokens)
        {
            if (!first)
            {
                str.append(SHARED_LOCK_TOKEN_SEPARATOR);
            }
            else
            {
                first = false;
            }
            str.append(token);
        }
        return str.toString();
    }
    
    /**
     * Setter for shared property
     * 
     * @param shared
     */
    public void setShared(boolean shared)
    {
        this.shared = shared;
    }

    /**
     * Returns true is lock is shared
     * 
     * @return boolean
     */
    public boolean isShared()
    {
        return shared;
    }
    
    /**
     * Return the lock info as a string
     * 
     * @return String
     */
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        
        str.append("[");
        
        str.append("token=");
        str.append(getToken());
        str.append(",scope=");
        str.append(getScope());
        str.append(",depth=");
        str.append(getDepth());
        str.append(",shared locks=");
        str.append(getSharedLockTokens());

        str.append("]");
        
        return str.toString();
    }

}
