/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.domain.solr;

import org.alfresco.repo.solr.Transaction;

/**
 * Bean to represent SOLR transaction data.
 * 
 * @since 4.0
 */
public class TransactionEntity implements Transaction
{
    private Long id;
    private Long commitTimeMs;
    private int updates;
    private int deletes;
    
    /**
     * Required default constructor
     */
    public TransactionEntity()
    {
    }
        
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("TransactionEntity")
          .append("[ ID=").append(id)
          .append(", updates=").append(updates)
          .append(", deletes=").append(deletes)
          .append(", commitTimeMs=").append(commitTimeMs)
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

    public int getUpdates()
    {
        return updates;
    }

    public void setUpdates(int updates)
    {
        this.updates = updates;
    }

    public int getDeletes()
    {
        return deletes;
    }

    public void setDeletes(int deletes)
    {
        this.deletes = deletes;
    }

    public Long getCommitTimeMs()
    {
        return commitTimeMs;
    }

    public void setCommitTimeMs(Long commitTimeMs)
    {
        this.commitTimeMs = commitTimeMs;
    }
}

