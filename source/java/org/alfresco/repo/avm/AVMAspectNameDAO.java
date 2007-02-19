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
 * http://www.alfresco.com/legal/licensing" */

package org.alfresco.repo.avm;

import java.util.List;

import org.alfresco.service.namespace.QName;

/**
 * DAO for AVMAspectNames.
 * @author britt
 */
public interface AVMAspectNameDAO
{
    /**
     * Persist an aspect name.
     * @param aspectName The item to persist.
     */
    public void save(AVMAspectName aspectName);
    
    /**
     * Delete an Aspect Name.
     * @param aspectName The item to delete.
     */
    public void delete(AVMAspectName aspectName);
    
    /**
     * Delete a single aspect name from a node.
     * @param node The node.
     * @param aspectName The aspect name.
     */
    public void delete(AVMNode node, QName aspectName);
    
    /**
     * Delete all Aspect Names on a given node.
     * @param node The given node.
     */
    public void delete(AVMNode node);
    
    /**
     * Get all Aspect Names for a given node.
     * @param node The AVM Node.
     * @return A List of AVMAspectNames.
     */
    public List<AVMAspectName> get(AVMNode node);
    
    /**
     * Does the given node have the given asset.
     * @param node The AVM node.
     * @param name The QName of the Aspect.
     * @return Whether the aspect is there.
     */
    public boolean exists(AVMNode node, QName name);
}
