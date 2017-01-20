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

package org.alfresco.rest.api.model;

import java.util.List;

import org.alfresco.service.cmr.download.DownloadStatus;

/**
 * Represents a download entity
 *
 */
public class Download
{
    private String id;
    private List<String> nodeIds;
    private DownloadStatus.Status status;
    private long bytesAdded;
    private long totalBytes;
    private long filesAdded;
    private long totalFiles;
    
    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public List<String> getNodeIds()
    {
        return nodeIds;
    }

    public void setNodeIds(List<String> nodeIds)
    {
        this.nodeIds = nodeIds;
    }

    public DownloadStatus.Status getStatus()
    {
        return status;
    }

    public void setStatus(DownloadStatus.Status status)
    {
        this.status = status;
    }

    public long getBytesAdded()
    {
        return bytesAdded;
    }

    public void setBytesAdded(long bytesAdded)
    {
        this.bytesAdded = bytesAdded;
    }

    public long getTotalBytes()
    {
        return totalBytes;
    }

    public void setTotalBytes(long totalBytes)
    {
        this.totalBytes = totalBytes;
    }

    public long getFilesAdded()
    {
        return filesAdded;
    }

    public void setFilesAdded(long filesAdded)
    {
        this.filesAdded = filesAdded;
    }

    public long getTotalFiles()
    {
        return totalFiles;
    }

    public void setTotalFiles(long totalFiles)
    {
        this.totalFiles = totalFiles;
    }


    @Override
    public String toString()
    {        
        StringBuilder builder = new StringBuilder(150);
        builder.append("Download [id=").append(id)
               .append(", nodeIds=").append(nodeIds)
               .append(", status=").append(status)
               .append(", bytesAdded=").append(bytesAdded)
               .append(", totalBytes=").append(totalBytes)
               .append(", filesAdded=").append(filesAdded)
               .append(", totalFiles=").append(totalFiles)
               .append("]");
        return builder.toString();
    }
}
