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

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.namespace.QName;

/**
 * Thrown when an operation could not be performed because a named association already
 * exists between two nodes
 * 
 * @author Derek Hulley
 */
@AlfrescoPublicApi
public class AssociationExistsException extends RuntimeException
{
    private static final long serialVersionUID = 3256440317824874800L;

    private Long sourceNodeId;
    private Long targetNodeId;
    private QName qname;
    
    /**
     * @see #AssociationExistsException(Long, Long, org.alfresco.service.namespace.QName, Throwable)
     */
    public AssociationExistsException(Long sourceNodeId, Long targetNodeId, QName qname)
    {
        super();
        this.sourceNodeId = sourceNodeId;
        this.targetNodeId = targetNodeId;
        this.qname = qname;
    }

    /**
     * @param sourceNodeId      the source of the association
     * @param targetNodeId      the target of the association
     * @param qname             the qualified name of the association
     * @param cause a causal exception
     */
    public AssociationExistsException(Long sourceNodeId, Long targetNodeId, QName qname, Throwable cause)
    {
        super(cause);
        this.sourceNodeId = sourceNodeId;
        this.targetNodeId = targetNodeId;
        this.qname = qname;
    }

    public Long getSourceNodeId()
    {
        return sourceNodeId;
    }

    public Long getTargetNodeId()
    {
        return targetNodeId;
    }
    
    public QName getQName()
    {
        return qname;
    }
}
