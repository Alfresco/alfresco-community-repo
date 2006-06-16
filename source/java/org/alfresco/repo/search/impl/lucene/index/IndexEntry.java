/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
        return builder.toString();
    }
    
    
}