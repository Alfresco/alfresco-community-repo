/*
 * #%L
 * Alfresco Data model classes
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
package org.alfresco.repo.index.shard;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

import org.alfresco.service.cmr.repository.StoreRef;

/**
 * Store shard state for auto discovery 
 * 
 * @author Andy
 *
 */
public class ShardState implements Serializable
{
    private static final long serialVersionUID = -5961621567026938963L;

    ShardInstance shardInstance;
        
    private boolean isMaster;
    
    private long lastUpdated;
    
    private long lastIndexedChangeSetId;

    private long lastIndexedTxCommitTime = 0;

    private long lastIndexedTxId = 0;

    private long lastIndexedChangeSetCommitTime = 0;
    
    
    private HashMap<String, String> propertyBag = new HashMap<String, String>();

    public ShardState()
    {
    }
    
    /**
     * @return the shardInstance
     */
    public ShardInstance getShardInstance()
    {
        return shardInstance;
    }

    /**
     * @param shardInstance the shardInstance to set
     */
    public void setShardInstance(ShardInstance shardInstance)
    {
        this.shardInstance = shardInstance;
    }

    /**
     * @return the isMaster
     */
    public boolean isMaster()
    {
        return isMaster;
    }

    /**
     * @param isMaster the isMaster to set
     */
    public void setMaster(boolean isMaster)
    {
        this.isMaster = isMaster;
    }

    /**
     * @return the propertyBag
     */
    public HashMap<String, String> getPropertyBag()
    {
        return propertyBag;
    }

    /**
     * @param propertyBag the propertyBag to set
     */
    public void setPropertyBag(HashMap<String, String> propertyBag)
    {
        this.propertyBag = propertyBag;
    }
    

    /**
     * @return the lastUpdated
     */
    public long getLastUpdated()
    {
        return lastUpdated;
    }

    /**
     * @param lastUpdated the lastUpdated to set
     */
    public void setLastUpdated(long lastUpdated)
    {
        this.lastUpdated = lastUpdated;
    }

    /**
     * @return the lastIndexedChangeSetId
     */
    public long getLastIndexedChangeSetId()
    {
        return lastIndexedChangeSetId;
    }

    /**
     * @param lastIndexedChangeSetId the lastIndexedChangeSetId to set
     */
    public void setLastIndexedChangeSetId(long lastIndexedChangeSetId)
    {
        this.lastIndexedChangeSetId = lastIndexedChangeSetId;
    }

    /**
     * @return the lastIndexedTxCommitTime
     */
    public long getLastIndexedTxCommitTime()
    {
        return lastIndexedTxCommitTime;
    }

    /**
     * @param lastIndexedTxCommitTime the lastIndexedTxCommitTime to set
     */
    public void setLastIndexedTxCommitTime(long lastIndexedTxCommitTime)
    {
        this.lastIndexedTxCommitTime = lastIndexedTxCommitTime;
    }

    /**
     * @return the lastIndexedTxId
     */
    public long getLastIndexedTxId()
    {
        return lastIndexedTxId;
    }

    /**
     * @param lastIndexedTxId the lastIndexedTxId to set
     */
    public void setLastIndexedTxId(long lastIndexedTxId)
    {
        this.lastIndexedTxId = lastIndexedTxId;
    }

    /**
     * @return the lastIndexedChangeSetCommitTime
     */
    public long getLastIndexedChangeSetCommitTime()
    {
        return lastIndexedChangeSetCommitTime;
    }

    /**
     * @param lastIndexedChangeSetCommitTime the lastIndexedChangeSetCommitTime to set
     */
    public void setLastIndexedChangeSetCommitTime(long lastIndexedChangeSetCommitTime)
    {
        this.lastIndexedChangeSetCommitTime = lastIndexedChangeSetCommitTime;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ShardState [shardInstance="
                + shardInstance + ", isMaster=" + isMaster + ", lastUpdated=" + lastUpdated + ", lastIndexedChangeSetId=" + lastIndexedChangeSetId + ", lastIndexedTxCommitTime="
                + lastIndexedTxCommitTime + ", lastIndexedTxId=" + lastIndexedTxId + ", lastIndexedChangeSetCommitTime=" + lastIndexedChangeSetCommitTime + ", propertyBag="
                + propertyBag + "]";
    }

  

    
}
