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
package org.alfresco.service.cmr.download;

import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Zip download service.
 * 
 * Implementations are responsible for triggering the Zip creation process and
 * reporting on the status of the of this process.
 *
 * @author Alex Miller
 */
public interface DownloadService
{
    /**
     * Start the creation of a downlaodable archive file containing the content
     * from the given nodeRefs.
     * 
     * Implementations are expected to do this asynchronously, with clients 
     * using the returned NodeRef to check on progress.

     * Initially, only zip files will be supported, however this could be 
     * extended in the future, to support additional archive types.
     * 
     * @param nodeRefs NodeRefs of content to be added to the archive file
     * @param recusirsive Recurse into container nodes
     * @return Reference to node which will eventually contain the archive file
     */
    public NodeRef createDownload(NodeRef[] nodeRefs, boolean recusirsive);
    
    /**
     * Get the status of the of the download identified by downloadNode.
     */
    public DownloadStatus getDownloadStatus(NodeRef downloadNode);

    /**
     * Delete downloads created before before.
     * 
     * @param before Date
     */
    public void deleteDownloads(Date before);
    
    /**
     * Cancel a download request
     * 
     * @param downloadNodeRef NodeRef of the download to cancel
     */
    public void cancelDownload(NodeRef downloadNodeRef);
}
