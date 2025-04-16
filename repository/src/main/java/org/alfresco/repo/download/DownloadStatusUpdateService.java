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
package org.alfresco.repo.download;

import org.alfresco.service.cmr.download.DownloadStatus;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Service for updating the status of a download.
 * 
 * @author Alex Miller
 */
public interface DownloadStatusUpdateService
{
    /**
     * Update and persist the status of the download.
     * 
     * Implementations should only do this if sequenceNumber is greater than the sequenceNumber of the previous update, to prevent out of order updates.
     * 
     * @param nodeRef
     *            The download node, whose status is to be updated.
     * @param status
     *            The new status
     * @param sequenceNumber
     *            int
     */
    void update(NodeRef nodeRef, DownloadStatus status, int sequenceNumber);
}
