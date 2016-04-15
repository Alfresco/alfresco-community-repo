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
package org.alfresco.service.cmr.webdav;

import org.alfresco.service.cmr.repository.NodeRef;

public interface WebDavService
{
    /**
     * Get the WebDavUrl for the specified nodeRef
     * @param nodeRef the node that the webdav URL (or null)
     * @return the URL of the node in webdav or "" if a URL cannot be built.
     */
    public String getWebdavUrl(NodeRef nodeRef);
    
    /**
     * Determines whether activity post generation is enabled for WebDAV. When enabled,
     * file creation, modification and deletion will create activities that can be viewed
     * in the Share web client.
     * 
     * @return true if activity generation is enabled.
     */
    public boolean activitiesEnabled();
    
    /**
     * Is the web dav service enabled?
     * @return true, is enabled
     */
    public boolean getEnabled();
}
