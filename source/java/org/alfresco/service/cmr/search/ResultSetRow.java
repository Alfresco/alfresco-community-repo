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
package org.alfresco.service.cmr.search;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.QName;

/**
 * A row in a result set
 * 
 * TODO: Support for other non attribute features such as parents and path
 * 
 * @author andyh
 * 
 */
public interface ResultSetRow
{
    /**
     * Get the values of all available node properties.  These are only properties
     * that were stored in the query results and can vary depending on the query
     * language that was used.
     * 
     * @return Returns all the available node properties
     */
    public Map<Path, Serializable> getValues();

    /**
     * Get a node property by path
     * 
     * @param path the path to the value required
     * @return Returns the value of the property at the given path
     */
    public Serializable getValue(Path path);

    /**
     * Get a node property value by name
     * 
     * @param qname the property name
     * @return Returns the node property for the given name
     */
    public Serializable getValue(QName qname);
    
    /**
     * The reference to the node that equates to this row in the result set
     * 
     * @return Returns the reference to the node that makes this result 
     */
    public NodeRef getNodeRef();

    /**
     * Get the score for this row in the result set
     * 
     * @return Returns the score for this row in the resultset
     */
    public float getScore(); // Score is score + rank + potentially other
                                // stuff

    /**
     * Get the containing result set
     * 
     * @return Returns the containing resultset
     */
    public ResultSet getResultSet();
    
    /**
     * @return Returns the name of the child association leading down to the
     *      node represented by this row 
     */
    public QName getQName();

    /**
     * Get the position of this row in the containing set.
     * 
     * @return Returns the position of this row in the containing resultset
     */
    public int getIndex();
    
    /**
     * @return Returns the child assoc ref for this row
     */
    public ChildAssociationRef getChildAssocRef();
    
}
