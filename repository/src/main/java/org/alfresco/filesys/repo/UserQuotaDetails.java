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

package org.alfresco.filesys.repo;

import org.alfresco.jlan.util.MemorySize;

/**
 * User Quota Details Class
 * 
 * <p>Used to track the live usage of a user as files are being written.
 * 
 * @author gkspencer
 */
public class UserQuotaDetails {

    // User name and allowed quota, -1 indicates unlimited quota
    
    private String m_userName;
    private long m_quota;
    
    // Current live usage
    
    private long m_curUsage;
    
    // Timestamp of the last allocation/release
    
    private long m_lastUpdate;
    
    /**
     * Class constructor
     * 
     * @param userName String
     * @param quota long
     */
    public UserQuotaDetails(String userName, long quota) {
        m_userName = userName;
        m_quota    = quota;
    }
    
    /**
     * Return the user name
     * 
     * @return String
     */
    public final String getUserName() {
        return m_userName;
    }
    
    /**
     * Check if the user has a usage quota
     * 
     * @return boolean
     */
    public final boolean hasUserQuota() {
        return m_quota == -1L ? false : true;
    }
    
    /**
     * Return the user quota, in bytes
     * 
     * @return long
     */
    public final long getUserQuota() {
        return m_quota;
    }
    
    /**
     * Return the current live usage, in bytes
     * 
     * @return long
     */
    public final long getCurrentUsage() {
        return m_curUsage;
    }
    
    /**
     * Return the time the live usage value was last updated
     * 
     * @return long
     */
    public final long getLastUpdated() {
        return m_lastUpdate;
    }
    
    /**
     * Return the available space for this user, -1 is unlimited
     * 
     * @return long
     */
    public final long getAvailableSpace() {
        if (!hasUserQuota() || getUserQuota() == 0)
            return -1L;
        long availSpace = getUserQuota() - getCurrentUsage();
        if ( availSpace < 0L)
            availSpace = 0L;
        return availSpace;
    }
    
    /**
     * Set the user quota, in bytes
     * 
     * @param quota long
     */
    public final void setUserQuota(long quota) {
        m_quota = quota;
    }
    
    /**
     * Update the current live usage
     * 
     * @param usage long
     */
    public final void setCurrentUsage(long usage) {
        m_curUsage = usage;
        m_lastUpdate = System.currentTimeMillis();
    }
    
    /**
     * Add to the current live usage
     * 
     * @param usage long
     * @return long
     */
    public final long addToCurrentUsage(long usage) {
        m_curUsage += usage;
        m_lastUpdate = System.currentTimeMillis();
        
        return m_curUsage;
    }

    /**
     * Subtract from the current live usage
     * 
     * @param usage long
     * @return long 
     */
    public final long subtractFromCurrentUsage(long usage) {
        m_curUsage -= usage;
        m_lastUpdate = System.currentTimeMillis();
        
        return m_curUsage;
    }
    
    /**
     * Return the user quota details as a string
     * 
     * @return String
     */
    public String toString() {
        StringBuilder str = new StringBuilder();
        
        str.append("[");
        str.append(getUserName());
        str.append(",quota=");
        str.append(MemorySize.asScaledString(getUserQuota()));
        str.append(",current=");
        str.append(getCurrentUsage());
        str.append(",available=");
        str.append(getAvailableSpace());
        str.append("/");
        str.append(MemorySize.asScaledString(getAvailableSpace()));
        str.append("]");
        
        return str.toString();
    }
}
