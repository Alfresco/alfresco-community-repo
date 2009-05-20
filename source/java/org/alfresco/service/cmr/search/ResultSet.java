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
package org.alfresco.service.cmr.search;

import java.util.List;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;

/**
 * An iterable result set from a searcher query.<b/>
 * Implementations must implement the indexes as zero-based.<b/>
 * TODO: Expose meta data and XML
 * 
 * @author andyh
 * 
 */
public interface ResultSet extends Iterable<ResultSetRow> // Specfic iterator over ResultSetRows
{
    /**
     * Get the relative paths to all the elements contained in this result set
     */
    Path[] getPropertyPaths();

    /**
     * Get the size of the result set
     */
    int length();

    /**
     * Get the id of the node at the given index
     * 
     * @param n zero-based index
     */
    NodeRef getNodeRef(int n);

    /**
     * Get the score for the node at the given position
     * 
     * @param n zero-based index
     */
    float getScore(int n);

    /**
     * Close the result set.
     * This must be called to allow the release of underlying resources.
     */
    
    void close();
    
    /**
     * Get a row from the result set by row index, starting at 0.
     * 
     * @param i zero-based index
     */
    ResultSetRow getRow(int i);
    
    /**
     * Get a list of all the node refs in the result set
     */
    List<NodeRef> getNodeRefs();
    
    /**
     * Get a list of all the child associations in the results set.
     */
    List<ChildAssociationRef> getChildAssocRefs();
    
    /**
     * Get the child assoc ref for a particular row.
     * 
     * @param n zero-based index
     */
    ChildAssociationRef getChildAssocRef(int n);
    
    /**
     * Get the meta data for the results set.
     */
    ResultSetMetaData getResultSetMetaData();
}
