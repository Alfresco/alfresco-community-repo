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
     * Start the creation of a downloadable archive file containing the content
     * from the given nodeRefs.
     * 
     * Implementations are expected to do this asynchronously, with clients 
     * using the returned NodeRef to check on progress.

     * Initially, only zip files will be supported, however this could be 
     * extended in the future, to support additional archive types.
     * 
     * @param nodeRefs NodeRefs of content to be added to the archive file
     * @param recursive Recurse into container nodes
     * @return Reference to node which will eventually contain the archive file
     */
    NodeRef createDownload(NodeRef[] nodeRefs, boolean recursive);

    /**
     * Start the creation of a downloadable archive file containing the content
     * from the given nodeRefs.
     *
     * Implementations are expected to do this asynchronously, with clients
     * using the returned NodeRef to check on progress.

     * Initially, only zip files will be supported, however this could be
     * extended in the future, to support additional archive types.
     *
     * @param nodeRefs NodeRefs of content to be added to the archive file
     * @param recursive Recurse into container nodes
     * @param downloadNodeName Download node name
     * @return Reference to node which will eventually contain the archive file
     */
    NodeRef createDownload(NodeRef[] nodeRefs, boolean recursive, String downloadNodeName);
    
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
     * Delete downloads created before the specified date.
     *
     * It also limits the number of deleted files for this batch of work to
     * the specified batchSize;
     *
     * It can also look into deleting downloads files from all sys:Download folders
     * affected by MNT-20212
     */
    void deleteDownloads(Date before, int batchSize, boolean cleanAllSysDownloadFolders);

    /**
     * Cancel a download request
     * 
     * @param downloadNodeRef NodeRef of the download to cancel
     */
    public void cancelDownload(NodeRef downloadNodeRef);
}
