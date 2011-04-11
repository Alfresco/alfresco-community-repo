/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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
package org.alfresco.repo.transfer.manifest;

import java.util.Date;

import org.alfresco.service.cmr.transfer.TransferVersion;

/**
 * Data value object
 *
 * Part of the transfer manifest
 */

public class TransferManifestHeader
{
    private Date createdDate;
    private int nodeCount; 
    private String repositoryId;
    private boolean isSync;
    private boolean isReadOnly;
    private TransferVersion version;
    
    public void setCreatedDate(Date createDate)
    {
        this.createdDate = createDate;
    }

    public Date getCreatedDate()
    {
        return createdDate;
    }

    /**
     * @return the nodeCount
     */
    public int getNodeCount()
    {
        return nodeCount;
    }

    /**
     * @param nodeCount the nodeCount to set
     */
    public void setNodeCount(int nodeCount)
    {
        this.nodeCount = nodeCount;
    }

    /**
     * The repository ID of this, the sending system
     * @param repositoryId
     */
    public void setRepositoryId(String repositoryId)
    {
        this.repositoryId = repositoryId;
    }

    /**
     * Get the repository ID of this, the sending system
     * @return the repository Id
     */
    public String getRepositoryId()
    {
        return repositoryId;
    }

    public void setSync(boolean isSync)
    {
        this.isSync = isSync;
    }

    public boolean isSync()
    {
        return isSync;
    }

    public void setReadOnly(boolean isReadOnly)
    {
        this.isReadOnly = isReadOnly;
    }

    public boolean isReadOnly()
    {
        return isReadOnly;
    }
    
    public void setTransferVersion(TransferVersion version)
    {
        this.version = version;
    }
    
    public TransferVersion getTransferVersion()
    {
        return version;
    }
}
