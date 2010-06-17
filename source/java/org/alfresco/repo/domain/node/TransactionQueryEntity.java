/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.domain.node;

import java.util.List;

/**
 * Bean to represent carry query parameters for <tt>alf_transaction</tt>.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class TransactionQueryEntity
{
    private Long id;
    private Long minId;
    private Long maxId;
    private Long minCommitTime;
    private Long maxCommitTime;
    private List<Long> includeTxnIds;
    private List<Long> excludeTxnIds;
    private Long excludeServerId;
    private Boolean ascending;
    private Long storeId;
    
    /**
     * Required default constructor
     */
    public TransactionQueryEntity()
    {
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("TransactionQueryEntity")
          .append("[ id=").append(id)
          .append(", minId=").append(minId)
          .append(", maxId=").append(maxId)
          .append(", minCommitTime=").append(minCommitTime)
          .append(", maxCommitTime=").append(maxCommitTime)
          .append(", includeTxnIds=").append(includeTxnIds)
          .append(", excludeTxnIds=").append(excludeTxnIds)
          .append(", excludeServerId=").append(excludeServerId)
          .append(", ascending=").append(ascending)
          .append(", storeId=").append(storeId)
          .append("]");
        return sb.toString();
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getMinId()
    {
        return minId;
    }

    public void setMinId(Long minId)
    {
        this.minId = minId;
    }

    public Long getMaxId()
    {
        return maxId;
    }

    public void setMaxId(Long maxId)
    {
        this.maxId = maxId;
    }

    public Long getMinCommitTime()
    {
        return minCommitTime;
    }

    public void setMinCommitTime(Long minCommitTime)
    {
        this.minCommitTime = minCommitTime;
    }

    public Long getMaxCommitTime()
    {
        return maxCommitTime;
    }

    public void setMaxCommitTime(Long maxCommitTime)
    {
        this.maxCommitTime = maxCommitTime;
    }

    public List<Long> getIncludeTxnIds()
    {
        return includeTxnIds;
    }

    public void setIncludeTxnIds(List<Long> includeTxnIds)
    {
        this.includeTxnIds = includeTxnIds;
    }

    public List<Long> getExcludeTxnIds()
    {
        return excludeTxnIds;
    }

    public void setExcludeTxnIds(List<Long> excludeTxnIds)
    {
        this.excludeTxnIds = excludeTxnIds;
    }

    public Long getExcludeServerId()
    {
        return excludeServerId;
    }

    public void setExcludeServerId(Long excludeServerId)
    {
        this.excludeServerId = excludeServerId;
    }

    public Boolean getAscending()
    {
        return ascending;
    }

    public void setAscending(Boolean ascending)
    {
        this.ascending = ascending;
    }

    public Long getStoreId()
    {
        return storeId;
    }

    public void setStoreId(Long storeId)
    {
        this.storeId = storeId;
    }
}
