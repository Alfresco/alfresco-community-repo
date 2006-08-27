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
package org.alfresco.repo.domain;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.namespace.QName;

/**
 * Represents a special type of association between nodes, that of the
 * parent-child relationship.
 * 
 * @author Derek Hulley
 */
public interface ChildAssoc extends Comparable<ChildAssoc>
{
    /**
     * Performs the necessary work on the provided nodes to ensure that a bidirectional
     * association is properly set up.
     * <p>
     * The association attributes still have to be set up.
     * 
     * @param parentNode
     * @param childNode
     * 
     * @see #setName(String)
     * @see #setIsPrimary(boolean)
     */
    public void buildAssociation(Node parentNode, Node childNode);
    
    /**
     * Performs the necessary work on the {@link #getParent() parent} and
     * {@link #getChild() child} nodes to maintain the inverse association sets
     */
    public void removeAssociation();
    
    public ChildAssociationRef getChildAssocRef();

    public Long getId();

    public Node getParent();

    public Node getChild();
    
    /**
     * @return Returns the qualified name of the association type
     */
    public QName getTypeQName();
    
    /**
     * @param assocTypeQName the qualified name of the association type as defined
     *      in the data dictionary
     */
    public void setTypeQName(QName assocTypeQName);

    /**
     * @return Returns the child node name.  This may be truncated, in which case it
     *      will end with <b>...</b>
     */
    public String getChildNodeName();
    
    /**
     * @param childNodeName the name of the child node, which may be truncated and
     *      terminated with <b>...</b> in order to not exceed 50 characters.
     */
    public void setChildNodeName(String childNodeName);
    
    /**
     * @return Returns the crc value for the child node name.
     */
    public long getChildNodeNameCrc();
    
    /**
     * @param crc the crc value
     */
    public void setChildNodeNameCrc(long crc);
    
    /**
     * @return Returns the qualified name of this association 
     */
    public QName getQname();

    /**
     * @param qname the qualified name of the association
     */
    public void setQname(QName qname);

    public boolean getIsPrimary();

    public void setIsPrimary(boolean isPrimary);
    
    /**
     * @return Returns the user-assigned index
     */
    public int getIndex();
    
    /**
     * Set the index of this association
     *  
     * @param index the association index
     */
    public void setIndex(int index);
}
