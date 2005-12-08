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
package org.alfresco.repo.domain.hibernate;

import org.alfresco.repo.domain.NodeKey;
import org.alfresco.repo.domain.NodeStatus;
import org.alfresco.util.EqualsHelper;

/**
 * Hibernate implementation of a <b>node status</b>
 * 
 * @author Derek Hulley
 */
public class NodeStatusImpl implements NodeStatus
{
    private NodeKey key;
    private String changeTxnId;
    private boolean deleted;

    public int hashCode()
    {
        return (key == null) ? 0 : key.hashCode();
    }
    
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        else if (obj == null)
            return false;
        else if (!(obj instanceof NodeStatusImpl))
            return false;
        NodeStatus that = (NodeStatus) obj;
        return (EqualsHelper.nullSafeEquals(this.key, that.getKey())) &&
               (EqualsHelper.nullSafeEquals(this.changeTxnId, that.getChangeTxnId())) &&
               (this.deleted == that.isDeleted());
                        
    }
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder(50);
        sb.append("NodeStatus")
          .append("[key=").append(key)
          .append(", txn=").append(changeTxnId)
          .append(", deleted=").append(deleted)
          .append("]");
        return sb.toString();
    }
    
    public NodeKey getKey()
    {
        return key;
    }

    public void setKey(NodeKey key)
    {
        this.key = key;
    }

    public String getChangeTxnId()
    {
        return changeTxnId;
    }

    public void setChangeTxnId(String txnId)
    {
        this.changeTxnId = txnId;
    }

    public boolean isDeleted()
    {
        return deleted;
    }

    public void setDeleted(boolean deleted)
    {
        this.deleted = deleted;
    }
}
