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
     */
    NodeRef getNodeRef(int n);

    /**
     * Get the score for the node at the given position
     */
    float getScore(int n);

    /**
     * Close the result set.
     * This must be called to allow the release of underlying resources.
     */
    
    void close();
    
    /**
     * Get a row from the result set by row index, starting at 0.
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
     */
    ChildAssociationRef getChildAssocRef(int n);
    
    /**
     * Get the meta data for the results set.
     */
    ResultSetMetaData getResultSetMetaData();
}
