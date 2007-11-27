/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.service.cmr.usage;

import org.alfresco.service.Auditable;
import org.alfresco.service.PublicService;

@PublicService
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
