/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.repo.security.permissions;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * The interface for a dynamic authority provider e.g. for the owner of a node
 * or any other authority that is determined by the context rather than just a
 * node.
 * 
 * @author Andy Hind
 */
public interface DynamicAuthority
{
    /**
     * Is this authority granted to the given user for this node ref?
     * 
     * @param nodeRef
     * @param userName
     * @return
     */
    public boolean hasAuthority(NodeRef nodeRef, String userName);

    /**
     * If this authority is granted this method provides the string
     * representation of the granted authority.
     * 
     * @return
     */
    public String getAuthority();
}
