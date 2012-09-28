/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.download;

import org.alfresco.service.cmr.download.DownloadStatus;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Implementation class responsible for update the status of a download node. 
 *
 * @author Alex Miller
 */
public class DownloadStatusUpdateServiceImpl implements DownloadStatusUpdateService
{

    // Dependencies
    private DownloadStorage storage;
    
    // Dependency setters
    public void setStorage(DownloadStorage storage)
    {
        this.storage = storage;
    }
    
    @Override
    public void update(NodeRef nodeRef, DownloadStatus status, int sequenceNumber)
    {
        
        // Update the status of the download node, if and only if sequenceNumber is
        // greater than the sequence number of the last update. 
        int currentSequenceNumber = storage.getSequenceNumber(nodeRef);
        
        if (currentSequenceNumber < sequenceNumber)
        {
            storage.updateStatus(nodeRef, status);
        }
    }

}
