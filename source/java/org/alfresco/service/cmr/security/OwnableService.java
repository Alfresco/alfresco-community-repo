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
package org.alfresco.service.cmr.security;

import org.alfresco.service.Auditable;
import org.alfresco.service.PublicService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Service support around managing ownership.
 * 
 * @author Andy Hind
 */
@PublicService
public interface OwnableService
{
    /**
     * Get the username of the owner of the given object.
     *  
     * @param nodeRef
     * @return the username or null if the object has no owner
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef"})
    public String getOwner(NodeRef nodeRef);
    
    /**
     * Set the owner of the object.
     * 
     * @param nodeRef
     * @param userName
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef", "userName"})
    public void setOwner(NodeRef nodeRef, String userName);
    
    /**
     * Set the owner of the object to be the current user.
     * 
     * @param nodeRef
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef"})
    public void takeOwnership(NodeRef nodeRef);
    
    /**
     * Does the given node have an owner?
     * 
     * @param nodeRef
     * @return
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef"})
    public boolean hasOwner(NodeRef nodeRef);
}
