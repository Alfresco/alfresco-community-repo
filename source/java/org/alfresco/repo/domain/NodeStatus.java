/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.domain;

/**
 * Interface for persistent <b>node status</b> objects.
 * <p>
 * The node status records the liveness and change times of a node.  It follows
 * that a <b>node</b> might not exist (have been deleted) when the 
 * <b>node status</b> still exists.
 * 
 * @author Derek Hulley
 */
public interface NodeStatus
{
    /**
     * @return Returns the unique key for this node status
     */
    public NodeKey getKey();

    /**
     * @param key the unique key
     */
    public void setKey(NodeKey key);
    
    public Node getNode();
    
    public void setNode(Node node);
    
    public Transaction getTransaction();
    
    public void setTransaction(Transaction transaction);
    
    public boolean isDeleted();
}
