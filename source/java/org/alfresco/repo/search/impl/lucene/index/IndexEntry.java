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
    IndexType type;

    /**
     * The unique name of the index entry
     */
    String name;
    
    /**
     * The preceeding index name.
     * Allows deltas etc to apply to the index or an overlay for example. 
     */
    String parentName;

    /**
     * The status of the inedx entry
     */
    TransactionStatus status;
    
    /**
     * If merging, the id where the result is going
     */
    String mergeId;

    IndexEntry(IndexType type, String name,  String parentName, TransactionStatus status, String mergeId)
    {
        this.type = type;
        this.name = name;
        this.parentName = parentName;
        this.status = status;
        this.mergeId = mergeId;
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
    
    
}