/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.search.impl.lucene.index;

/**
 * Describes an entry in an index
 * 
 * @author Andy Hind
 */
class IndexEntry
{
    /**
     * The type of the index entry
     */
    private IndexType type;

    /**
     * The unique name of the index entry
     */
    private String name;
    
    /**
     * The preceeding index name.
     * Allows deltas etc to apply to the index or an overlay for example. 
     */
    private String parentName;

    /**
     * The status of the index entry
     */
    private TransactionStatus status;
    
    /**
     * If merging, the id where the result is going
     */
    private String mergeId;
    
    private long documentCount; 
    
    private long deletions;
    
    private boolean deletOnlyNodes;
    
    IndexEntry(IndexType type, String name,  String parentName, TransactionStatus status, String mergeId, long documentCount, long deletions, boolean deletOnlyNodes)
    {
        this.type = type;
        this.name = name;
        this.parentName = parentName;
        this.status = status;
        this.mergeId = mergeId;
        this.documentCount = documentCount;
        this.deletions = deletions;
        this.deletOnlyNodes = deletOnlyNodes;
    }

    public String getMergeId()
    {
        return mergeId;
    }

    public void setMergeId(String mergeId)
    {
        this.mergeId = mergeId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getParentName()
    {
        return parentName;
    }

    public void setParentName(String parentName)
    {
        this.parentName = parentName;
    }

    public TransactionStatus getStatus()
    {
        return status;
    }

    public void setStatus(TransactionStatus status)
    {
        this.status = status;
    }

    public IndexType getType()
    {
        return type;
    }

    public void setType(IndexType type)
    {
        this.type = type;
    }

    public long getDocumentCount()
    {
        return documentCount;
    }

    public void setDocumentCount(long documentCount)
    {
        this.documentCount = documentCount;
    }
    
    public long getDeletions()
    {
        return deletions;
    }

    public void setDeletions(long deletions)
    {
        this.deletions = deletions;
    }

    public boolean isDeletOnlyNodes()
    {
        return deletOnlyNodes;
    }

    public void setDeletOnlyNodes(boolean deletOnlyNodes)
    {
        this.deletOnlyNodes = deletOnlyNodes;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(" Name=").append(getName()).append(" ");
        builder.append("Type=").append(getType()).append(" ");
        builder.append("Status=").append(getStatus()).append(" ");
        builder.append("Docs=").append(getDocumentCount()).append(" ");
        builder.append("Deletions=").append(getDeletions()).append(" ");
        return builder.toString();
    }
    
    
}