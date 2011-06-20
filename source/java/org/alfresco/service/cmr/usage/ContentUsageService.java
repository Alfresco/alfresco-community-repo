/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
     * @param User's new quota (in bytes)
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
