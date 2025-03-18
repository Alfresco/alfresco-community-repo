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
package org.alfresco.filesys.repo;

import org.alfresco.jlan.server.filesys.DiskInterface;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Extended {@link org.alfresco.jlan.server.filesys.DiskInterface disk interface} to allow access to some of the internal configuration properties.
 * 
 * @author Derek Hulley
 */
public interface ContentDiskInterface extends DiskInterface
{
    /**
     * Get the name of the shared path within the server. The share name is equivalent in browse path to the {@link #getContextRootNodeRef() context root}.
     * 
     * @return Returns the share name
     */
    public String getShareName();

    /**
     * Get a reference to the node that all CIFS paths are relative to
     * 
     * @return Returns a node acting as the CIFS root
     */
    public NodeRef getContextRootNodeRef();
}
