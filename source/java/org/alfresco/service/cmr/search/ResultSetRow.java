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
     * Get the values of all available node properties
     * 
     * @return
     */
    public Map<Path, Serializable> getValues();

    /**
     * Get a node property by path
     * 
     * @param path
     * @return
     */
    public Serializable getValue(Path path);

    /**
     * Get a node value by name
     * 
     * @param qname
     * @return
     */
    public Serializable getValue(QName qname);
    
    /**
     * The refernce to the node that equates to this row in the result set
     * 
     * @return
     */
    public NodeRef getNodeRef();

    /**
     * Get the score for this row in the result set
     * 
     * @return
     */
    public float getScore(); // Score is score + rank + potentially other
                                // stuff

    /**
     * Get the containing result set
     * 
     * @return
     */
    public ResultSet getResultSet();
    
    /**
     * Return the QName of the node in the context in which it was found.
     * @return
     */
    
    public QName getQName();

    /**
     * Get the position of this row in the containing set.
     * @return
     */
    public int getIndex();
    
    /**
     * Return the child assoc ref for this row
     * @return
     */
    public ChildAssociationRef getChildAssocRef();
    
}
