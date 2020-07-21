/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.service.cmr.search;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * A row in a result set
 * 
 * TODO: Support for other non attribute features such as parents and path
 * 
 * @author andyh
 * 
 */
@AlfrescoPublicApi
public interface ResultSetRow
{
    /**
     * Get the values of all available node properties.  These are only properties
     * that were stored in the query results and can vary depending on the query
     * language that was used.
     * 
     * @return Returns all the available node properties
     */
    public Map<String, Serializable> getValues();

    /**
     * Get the data for a single column
     * @param columnName String
     * @return the value
     */
    public Serializable getValue(String columnName);

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
     * Gets the node refs
     * @return a map of selector name to node ref
     */
    public Map<String, NodeRef> getNodeRefs();
    

    /**
     * Gets the node ref related to the named selector
     * @param selectorName String
     * @return the node ref
     */
    public NodeRef getNodeRef(String selectorName);
    
    /**
     * Get the score for this row in the result set
     * 
     * @return Returns the score for this row in the resultset
     */
    public float getScore(); // Score is score + rank + potentially other
                                // stuff

    /**
     * Get the scores .
     * @return a map of selector name to score.
     */
    public Map<String, Float> getScores();
    
    /**
     * Get the score related to the named selector.
     * @param selectorName String
     * @return - the score.
     */
    public float getScore(String selectorName);
    
    
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
     * Get the index of this result set in the result set 
     * If you want the overall position in paged reults you have to add the skipCount fo the result set. 
     * @return int
     */
    public int getIndex();
    
    /**
     * @return Returns the child assoc ref for this row
     */
    public ChildAssociationRef getChildAssocRef();
    
}
