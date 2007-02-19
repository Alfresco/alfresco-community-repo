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
