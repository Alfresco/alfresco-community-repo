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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

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
