package org.alfresco.service.cmr.usage;

import org.alfresco.service.Auditable;
import org.alfresco.service.PublicService;

public interface ContentUsageService
{
    /**
     * Gets user usage
     * 
     * @return Return user's current calculated usage (in bytes)
     */
    @Auditable
    public long getUserUsage(String userName);
    
    /**
     * Gets user quota
     * 
     * Note: -1 means no quota limit set
     * 
     * @return Return user's quota (in bytes).
     */
    @Auditable
    public long getUserQuota(String userName);
    
    /**
     * Set user quota. 
     * 
     * Note: It is possible to set a quota that is below the current usage. At this point
     * the user will be over quota until their usage is decreased.
     * 
     * Note: -1 means no quota limit set
     * 
     * @param userName User name
     * @param newQuota User's new quota (in bytes)
     */
    @Auditable
    public void setUserQuota(String userName, long newQuota);
    
    /**
     * Are ContentUsages enabled (refer to 'system.usages.enabled' repository property) ?
     * 
     * @return true if ContentUsages are enabled, otherwise false
     */
    @Auditable
    public boolean getEnabled();
}
