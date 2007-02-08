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

/**
 * DAO for MergeLinks.
 * @author britt
 */
public interface MergeLinkDAO
{
    /**
     * Save an unsaved MergeLink.
     * @param link The link to save.
     */
    public void save(MergeLink link);
    
    /**
     * Get a link from the merged to node.
     * @param to The node merged to.
     * @return An AVMNode or null if not found.
     */
    public MergeLink getByTo(AVMNode to);
    
    /**
     * Get all the link that the given node was merged to.
     * @param from The node that was merged from
     * @return A List of MergeLinks.
     */
    public List<MergeLink> getByFrom(AVMNode from);
    
    /**
     * Delete a link.
     * @param link The link to delete.
     */
    public void delete(MergeLink link);
}
