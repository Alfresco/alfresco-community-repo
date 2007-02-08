/*
 * Copyright (C) 2006 Alfresco, Inc.
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

package org.alfresco.repo.avm;

import java.util.List;

import org.alfresco.service.namespace.QName;

/**
 * DAO for AVMNodeProperty.
 * @author britt
 */
public interface AVMNodePropertyDAO
{
    /**
     * Save the given AVMNodeProperty.
     * @param prop
     */
    public void save(AVMNodeProperty prop);
    
    /**
     * Get an AVMNodeProperty by owner and name.
     * @param owner An AVMNode.
     * @param name The QName.
     * @return The found AVMNodeProperty or null if not found.
     */
    public AVMNodeProperty get(AVMNode owner, QName name);
    
    /**
     * Get a List of all properties for an owning node.
     * @param node The owning node.
     * @return A List of properties belonging to the given node.
     */
    public List<AVMNodeProperty> get(AVMNode node);

    /**
     * Update a property entry.
     * @param prop The property.
     */
    public void update(AVMNodeProperty prop);
    
    /**
     * Delete all properties associated with a node.
     * @param node The AVMNode whose properties should be deleted.
     */
    public void deleteAll(AVMNode node);
    
    /**
     * Delete the given property from the given node.
     * @param node The node to delete the property to delete.
     * @param name The name of the property to delete.
     */
    public void delete(AVMNode node, QName name);
}
