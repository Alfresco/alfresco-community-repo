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
 * http://www.alfresco.com/legal/licensing
 */

package org.alfresco.service.simple.permission;

import java.util.List;

/**
 * Interface for a simple permission mechanism.
 * Nothing but String valued capabilities, and ACLs.
 * @author britt
 */
public interface SimplePermissionService
{
    /**
     * Can the current user perform the action indicated by the capability.
     * @param capability The capability: marker for an ability to perform an action
     * governed by an ACL.
     * @param acl The ACL. If this is null then the permission is granted.
     * @param owner The owner. The owner can always has the "changepermission" capability.
     * @return Whether permission is granted.
     */
    boolean can(String capability, ACL acl, String owner);
    
    /**
     * Can the user (agent) specified perform the action indicated by the capability.
     * @param agent The agent (user) to check.
     * @param capability The capability to check.
     * @param acl The ACL. If this is null then the permission is granted.
     * @param owner The owner.
     * @return Whether permission is granted.
     */
    boolean can(String agent, String capability, ACL acl, String owner);
    
    /**
     * Get the capabilities that this acl grants the current user.
     * @param acl The ACL.
     * @param owner The owner of the controlled entity.
     * @return A list of capabilities.
     */
    List<String> getCapabilities(ACL acl, String owner);
    
    /**
     * Get the capabilities that this agent grants the specifiec agent.
     * @param agent The agent (user).
     * @param acl The ACL.
     * @param owner The owner of the controlled entity.
     * @return A list of capabilities.
     */
    List<String> getCapabilities(String agent, ACL acl, String owner);
}
