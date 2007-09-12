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

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Interface for ACLs. ACLs express the capabilities granted to 
 * different agents (users, groups, or roles (one hopes that roles can go away as they are 
 * operationally just another name for a group)).  ACLs contain explicit entries made of
 * a capability and a list of agents plus an indication of whether the entry denies or allows
 * the capability. Entries that deny override any entries that allow. 
 * @author britt
 */
public interface ACL extends Serializable
{
    /**
     * Insert an allow entry for the agent, capabilities combination.
     * Removes a denials explicitly for the agent and capabilities given.
     * @param agent The agent, (user, group, role)
     * @param capabilities The capabilities being granted.
     */
    public void allow(String agent, String ... capabilities);
    
    /**
     * Insert a deny entry for the agent, capabilities combination.
     * Removes an allow explicitly for the agent and capabilities given.
     * @param agent The agent, (user, group, role)
     * @param capabilities
     */
    public void deny(String agent, String ... capabilities);
    
    /**
     * Does the given agent have the given capability
     * @param agent The agent (user)
     * @param capability The capability.
     * @return Whether the agent can.
     */
    public boolean can(String agent, String capability);
    
    /**
     * Get the capabilities for the given agent.
     * @param agent The agent.
     * @return A set of capabilities.
     */
    public Set<String> getCapabilities(String agent);
    
    /**
     * Get a string representation of this ACL, suitable for persistence.
     * @return The string representation.
     */
    public String getStringRepresentation();
    
    /**
     * Should this ACL be inherited.
     * @return Whether it should.
     */
    public boolean inherits();
}
