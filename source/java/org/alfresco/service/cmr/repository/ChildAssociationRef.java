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
package org.alfresco.service.cmr.repository;

import java.io.Serializable;
import java.util.StringTokenizer;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;

/**
 * This class represents a child relationship between two nodes. This
 * relationship is named.
 * <p>
 * So it requires the parent node ref, the child node ref and the name of the
 * child within the particular parent.
 * <p>
 * This combination is not a unique identifier for the relationship with regard
 * to structure. In use this does not matter as we have no concept of order,
 * particularly in the index.
 * 
 * @author andyh
 * 
 */
public class ChildAssociationRef
        implements EntityRef, Comparable<ChildAssociationRef>, Serializable
{
    private static final long serialVersionUID = 4051322336257127729L;
    
    private static final String FILLER = "|";

    private QName assocTypeQName;
    private NodeRef parentRef;
    private QName childQName;
    private NodeRef childRef;
    private boolean isPrimary;
    private int nthSibling;
    

    /**
     * Construct a representation of a parent --- name ----> child relationship.
     * 
     * @param assocTypeQName
     *            the type of the association
     * @param parentRef
     *            the parent reference - may be null
     * @param childQName
     *            the qualified name of the association - may be null
     * @param childRef
     *            the child node reference. This must not be null.
     * @param isPrimary
     *            true if this represents the primary parent-child relationship
     * @param nthSibling
     *            the nth association with the same properties. Usually -1 to be
     *            ignored.
     */
    public ChildAssociationRef(
            QName assocTypeQName,
            NodeRef parentRef,
            QName childQName,
            NodeRef childRef,
            boolean isPrimary,
            int nthSibling)
    {
        this.assocTypeQName = assocTypeQName;
        this.parentRef = parentRef;
        this.childQName = childQName;
        this.childRef = childRef;
        this.isPrimary = isPrimary;
        this.nthSibling = nthSibling;

        // check
        if (childRef == null)
        {
            throw new IllegalArgumentException("Child reference may not be null");
        }
    }

    /**
     * Constructs a <b>non-primary</b>, -1th sibling parent-child association
     * reference.
     * 
     * @see ChildAssociationRef#ChildAssocRef(QName, NodeRef, QName, NodeRef, boolean, int)
     */
    public ChildAssociationRef(QName assocTypeQName, NodeRef parentRef, QName childQName, NodeRef childRef)
    {
        this(assocTypeQName, parentRef, childQName, childRef, false, -1);
    }
    
    /**
     * @param childAssocRefStr a string of the form <b>parentNodeRef|childNodeRef|assocTypeQName|assocQName|isPrimary|nthSibling</b>
     */
    public ChildAssociationRef(String childAssocRefStr)
    {
        StringTokenizer tokenizer = new StringTokenizer(childAssocRefStr, FILLER);
        if (tokenizer.countTokens() != 6)
        {
            throw new AlfrescoRuntimeException("Unable to parse child association string: " + childAssocRefStr);
        }
        String parentNodeRefStr = tokenizer.nextToken();
        String childNodeRefStr = tokenizer.nextToken();
        String assocTypeQNameStr = tokenizer.nextToken();
        String assocQNameStr = tokenizer.nextToken();
        String isPrimaryStr = tokenizer.nextToken();
        String nthSiblingStr = tokenizer.nextToken();
        
        this.parentRef = new NodeRef(parentNodeRefStr);
        this.childRef = new NodeRef(childNodeRefStr);
        this.assocTypeQName = QName.createQName(assocTypeQNameStr);
        this.childQName = QName.createQName(assocQNameStr);
        this.isPrimary = Boolean.parseBoolean(isPrimaryStr);
        this.nthSibling = Integer.parseInt(nthSiblingStr);
    }

    /**
     * @return Returns a string of the form <b>parentNodeRef|childNodeRef|assocTypeQName|assocQName|isPrimary|nthSibling</b>
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder(250);
        sb.append(parentRef).append(FILLER)
          .append(childRef).append(FILLER)
          .append(assocTypeQName).append(FILLER)
          .append(childQName).append(FILLER)
          .append(isPrimary).append(FILLER)
          .append(nthSibling);
        return sb.toString();
    }
    
    /**
     * Compares:
     * <ul>
     * <li>{@link #assocTypeQName}</li>
     * <li>{@link #parentRef}</li>
     * <li>{@link #childRef}</li>
     * <li>{@link #childQName}</li>
     * </ul>
     */
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof ChildAssociationRef))
        {
            return false;
        }
        ChildAssociationRef other = (ChildAssociationRef) o;

        return (EqualsHelper.nullSafeEquals(this.assocTypeQName, other.assocTypeQName)
                && EqualsHelper.nullSafeEquals(this.parentRef, other.parentRef)
                && EqualsHelper.nullSafeEquals(this.childQName, other.childQName)
                && EqualsHelper.nullSafeEquals(this.childRef, other.childRef));
    }

    public int hashCode()
    {
        int hashCode = ((getTypeQName() == null) ? 0 : getTypeQName().hashCode());
        hashCode = 37 * hashCode + ((getParentRef() == null) ? 0 : getParentRef().hashCode());
        hashCode = 37 * hashCode + ((getQName() == null) ? 0 : getQName().hashCode());
        hashCode = 37 * hashCode + getChildRef().hashCode();
        return hashCode;
    }

    /**
     * @see #setNthSibling(int)
     */
    public int compareTo(ChildAssociationRef another)
    {
        int thisVal = this.nthSibling;
        int anotherVal = another.nthSibling;
        return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
    }

    /**
     * Get the qualified name of the association type
     * 
     * @return Returns the qualified name of the parent-child association type
     *      as defined in the data dictionary.  It may be null if this is the
     *      imaginary association to the root node.
     */
    public QName getTypeQName()
    {
        return assocTypeQName;
    }

    /**
     * Get the qualified name of the parent-child association
     * 
     * @return Returns the qualified name of the parent-child association. It
     *         may be null if this is the imaginary association to a root node.
     */
    public QName getQName()
    {
        return childQName;
    }

    /**
     * @return Returns the child node reference - never null
     */
    public NodeRef getChildRef()
    {
        return childRef;
    }

    /**
     * @return Returns the parent node reference, which may be null if this
     *         represents the imaginary reference to the root node
     */
    public NodeRef getParentRef()
    {
        return parentRef;
    }

    /**
     * @return Returns true if this represents a primary association
     */
    public boolean isPrimary()
    {
        return isPrimary;
    }

    /**
     * @return Returns the nth sibling required
     */
    public int getNthSibling()
    {
        return nthSibling;
    }

    /**
     * Allows post-creation setting of the ordering index.  This is a helper
     * so that sorted sets and lists can be easily sorted.
     * <p>
     * This index is <b>in no way absolute</b> and should change depending on
     * the results that appear around this instance.  Therefore, the sibling
     * number cannot be used to construct, say, sibling number 5.  Sibling
     * number 5 will exist only in results where there are siblings 1 - 4.
     * 
     * @param nthSibling the sibling index
     */
    public void setNthSibling(int nthSibling)
    {
        this.nthSibling = nthSibling;
    }
}
