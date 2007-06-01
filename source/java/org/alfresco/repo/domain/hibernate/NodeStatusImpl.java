/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.domain.hibernate;

import java.io.Serializable;

import org.alfresco.repo.domain.Node;
import org.alfresco.repo.domain.NodeKey;
import org.alfresco.repo.domain.NodeStatus;
import org.alfresco.repo.domain.Transaction;
import org.alfresco.util.EqualsHelper;

/**
 * Hibernate implementation of a <b>node status</b>
 * 
 * @author Derek Hulley
 */
public class NodeStatusImpl implements NodeStatus, Serializable
{
    private static final long serialVersionUID = -802747893314715639L;

    private NodeKey key;
    private Long version;
    private Node node;
    private Transaction transaction;
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(50);
        sb.append("NodeStatus")
          .append("[key=").append(key)
          .append(", node=").append(node == null ? null : node.getNodeRef())
          .append(", txn=").append(transaction)
          .append("]");
        return sb.toString();
    }

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
        return (EqualsHelper.nullSafeEquals(this.key, that.getKey()));
                        
    }
    
    public NodeKey getKey()
    {
        return key;
    }

    public void setKey(NodeKey key)
    {
        this.key = key;
    }

    public Long getVersion()
    {
        return version;
    }

    /**
     * For Hibernate use
     */
    @SuppressWarnings("unused")
    private void setVersion(Long version)
    {
        this.version = version;
    }

    public Node getNode()
    {
        return node;
    }

    public void setNode(Node node)
    {
        this.node = node;
    }

    public Transaction getTransaction()
    {
        return transaction;
    }

    public void setTransaction(Transaction transaction)
    {
        this.transaction = transaction;
    }

    public boolean isDeleted()
    {
        return (node == null);
    }
}
