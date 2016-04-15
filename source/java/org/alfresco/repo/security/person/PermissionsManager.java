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
package org.alfresco.repo.security.person;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Utility bean to set/check permissions on a node
 * @author andyh
 *
 */
public interface PermissionsManager
{
    /**
     * Set the permission as defined on the given node
     * 
     * @param nodeRef - the nodeRef 
     * @param owner - which should be set as the owner of the node (if configured to be set)
     */
    public void setPermissions(NodeRef nodeRef, String owner, String user);
    
    /**
     * Validate that permissions are set on a node as defined.
     * 
     * @param nodeRef NodeRef
     * @param owner String
     * @param user String
     * @return - true if correct, false if they are not set as defined.
     */
    public boolean validatePermissions(NodeRef nodeRef, String owner, String user);
}
