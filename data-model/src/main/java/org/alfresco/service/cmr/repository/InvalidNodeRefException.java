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

/**
 * Thrown when an operation cannot be performed because the <b>node</b> reference
 * no longer exists.
 * 
 * @author Derek Hulley
 */
@AlfrescoPublicApi
public class InvalidNodeRefException extends RuntimeException
{
    private static final long serialVersionUID = 3689345520586273336L;

    private NodeRef nodeRef;
    
    public InvalidNodeRefException(NodeRef nodeRef)
    {
        this(null, nodeRef);
    }

    public InvalidNodeRefException(String msg, NodeRef nodeRef)
    {
        this(msg, nodeRef, null);
    }

    public InvalidNodeRefException(String msg, NodeRef nodeRef, Throwable cause)
    {
        super(msg, cause);
        this.nodeRef = nodeRef;
    }

    /**
     * @return Returns the offending node reference
     */
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }
}
