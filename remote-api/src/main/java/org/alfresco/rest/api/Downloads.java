/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.api;

import org.alfresco.rest.api.model.Download;

/**
 * downloads API
 * 
 * @author cpopa
 *
 */
public interface Downloads
{
    /**
     * Creates a download:download node.
     * 
     * @param download
     * @return information about the newly created download:download node
     */
    Download createDownloadNode(Download download);

    /**
     * Get status info about a download node.
     * 
     * @param downloadNodeId
     * @return status info about a download:download node
     */
    Download getDownloadStatus(String downloadNodeId);
    
    /**
     * Stop the zip creation if still in progress
     * @param downloadNodeId
     */
    void cancel(String downloadNodeId);
}
