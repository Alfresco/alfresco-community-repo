
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
