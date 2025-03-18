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
package org.alfresco.repo.security.permissions;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;

/**
 * A single permission entry defined against a node.
 * 
 * @author andyh
 */
public interface PermissionEntry
{
    /**
     * Get the permission definition.
     * 
     * This may be null. Null implies that the settings apply to all permissions
     * 
     * @return PermissionReference
     */
    public PermissionReference getPermissionReference();

    /**
     * Get the authority to which this entry applies This could be the string value of a username, group, role or any other authority assigned to the authorisation.
     * 
     * If null then this applies to all.
     * 
     * @return String
     */
    public String getAuthority();

    /**
     * Get the node ref for the node to which this permission applies.
     * 
     * This can only be null for a global permission
     * 
     * @return NodeRef
     */
    public NodeRef getNodeRef();

    /**
     * Is permissions denied?
     *
     */
    public boolean isDenied();

    /**
     * Is permission allowed?
     *
     */
    public boolean isAllowed();

    /**
     * Get the Access enum value
     * 
     * @return AccessStatus
     */
    public AccessStatus getAccessStatus();

    /**
     * Is this permission inherited?
     * 
     * @return boolean
     */
    public boolean isInherited();

    /**
     * Return the position in the inhertance chain (0 is not inherited and set on the object)
     * 
     * @return int
     */
    public int getPosition();
}
