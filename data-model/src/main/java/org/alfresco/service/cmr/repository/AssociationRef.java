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
package org.alfresco.service.cmr.repository;

import java.io.Serializable;
import java.util.StringTokenizer;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;

/**
 * This class represents a regular, named node relationship between two nodes.
 * <p>
 * Note that the ID of the association might not be populated.
 * 
 * @author Derek Hulley
 */
@AlfrescoPublicApi
public class AssociationRef implements EntityRef, Serializable
{
    private static final long serialVersionUID = 3977867284482439475L;
    
    private static final String FILLER = "|";

    private Long id;
    private NodeRef sourceRef;
    private QName assocTypeQName;
    private NodeRef targetRef;

    /**
     * Construct a representation of a source --- name ----> target relationship.
     *
     * @param sourceRef
     *            the source reference - never null
     * @param assocTypeQName
     *            the qualified name of the association type - never null
     * @param targetRef
     *            the target node reference - never null.
     */
    public AssociationRef(NodeRef sourceRef, QName assocTypeQName, NodeRef targetRef)
    {
        this(null, sourceRef, assocTypeQName, targetRef);
    }
    
    /**
     * Construct a representation of a source --- name ----> target relationship.
     *
     * @param id
     *            unique identifier - may be null
     * @param sourceRef
     *            the source reference - never null
     * @param assocTypeQName
     *            the qualified name of the association type - never null
     * @param targetRef
     *            the target node reference - never null.
     */
    public AssociationRef(Long id, NodeRef sourceRef, QName assocTypeQName, NodeRef targetRef)
    {
        this.id = id;
        this.sourceRef = sourceRef;
        this.assocTypeQName = assocTypeQName;
        this.targetRef = targetRef;

        // check
        if (sourceRef == null)
        {
            throw new IllegalArgumentException("Source reference may not be null");
        }
        if (assocTypeQName == null)
        {
            throw new IllegalArgumentException("QName may not be null");
        }
        if (targetRef == null)
        {
            throw new IllegalArgumentException("Target reference may not be null");
        }
    }
    
    /**
     * @param assocRefStr a string of the form <b>id|sourceNodeRef|targetNodeRef|assocTypeQName</b>.
     */
    public AssociationRef(String assocRefStr)
    {
        StringTokenizer tokenizer = new StringTokenizer(assocRefStr, FILLER);
        if (tokenizer.countTokens() != 3 && tokenizer.countTokens() != 4)
        {
            throw new AlfrescoRuntimeException("Unable to parse association string: " + assocRefStr);
        }
        
        String idStr = "0";
        if (tokenizer.countTokens() == 4)
        {
            idStr = tokenizer.nextToken();
        }
        String sourceNodeRefStr = tokenizer.nextToken();
        String targetNodeRefStr = tokenizer.nextToken();
        String assocTypeQNameStr = tokenizer.nextToken();
        
        this.id = new Long(idStr);
        this.sourceRef = new NodeRef(sourceNodeRefStr);
        this.targetRef = new NodeRef(targetNodeRefStr);
        this.assocTypeQName = QName.createQName(assocTypeQNameStr);
    }

    /**
     * @return Returns a string of the form <b>sourceNodeRef|targetNodeRef|assocTypeQName|assocQName</b>
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder(180);
        sb.append(id == null ? Long.valueOf(0) : id).append(FILLER)
          .append(sourceRef).append(FILLER)
          .append(targetRef).append(FILLER)
          .append(assocTypeQName);
        return sb.toString();
    }

    /**
     * Compares:
     * <ul>
     * <li>{@link #sourceRef}</li>
     * <li>{@link #targetRef}</li>
     * <li>{@link #assocTypeQName}</li>
     * </ul>
     */
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof AssociationRef))
        {
            return false;
        }
        AssociationRef other = (AssociationRef) o;

        return (EqualsHelper.nullSafeEquals(this.sourceRef, other.sourceRef)
                && EqualsHelper.nullSafeEquals(this.assocTypeQName, other.assocTypeQName)
                && EqualsHelper.nullSafeEquals(this.targetRef, other.targetRef));
    }

    public int hashCode()
    {
        int hashCode = ((getSourceRef() == null) ? 0 : getSourceRef().hashCode());
        hashCode = 37 * hashCode + ((getTypeQName() == null) ? 0 : getTypeQName().hashCode());
        hashCode = 37 * hashCode + getTargetRef().hashCode();
        return hashCode;
    }

    /**
     * Gets the unique identifier for this association.
     * 
     * @return  the unique identifier for this association, or <tt>null</tt> if the ID was not
     *          given at the time of construction
     */
    public Long getId()
    {
        return this.id;
    }

    /**
     * Get the qualified name of the source-target association
     * 
     * @return Returns the qualified name of the source-target association.
     */
    public QName getTypeQName()
    {
        return assocTypeQName;
    }

    /**
     * @return Returns the child node reference - never null
     */
    public NodeRef getTargetRef()
    {
        return targetRef;
    }

    /**
     * @return Returns the parent node reference, which may be null if this
     *         represents the imaginary reference to the root node
     */
    public NodeRef getSourceRef()
    {
        return sourceRef;
    }
}
