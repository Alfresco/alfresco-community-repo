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
package org.alfresco.service.cmr.security;

import org.alfresco.service.Auditable;
import org.alfresco.service.PublicService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Service support around managing ownership.
 * 
 * @author Andy Hind
 */
public interface OwnableService
{
    public static String NO_OWNER = "";

    /**
     * Get the username of the owner of the given object.
     *  
     * @param nodeRef
     * @return the username or null if the object has no owner
     */
    @Auditable(parameters = {"nodeRef"})
    public String getOwner(NodeRef nodeRef);
    
    /**
     * Set the owner of the object.
     * 
     * @param nodeRef
     * @param userName
     */
    @Auditable(parameters = {"nodeRef", "userName"})
    public void setOwner(NodeRef nodeRef, String userName);
    
    /**
     * Set the owner of the object to be the current user.
     * 
     * @param nodeRef
     */
    @Auditable(parameters = {"nodeRef"})
    public void takeOwnership(NodeRef nodeRef);
    
    /**
     * Does the given node have an owner?
     * 
     * @param nodeRef
     * @return
     */
    @Auditable(parameters = {"nodeRef"})
    public boolean hasOwner(NodeRef nodeRef);
}
