/*
 * #%L
 * Alfresco Repository
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

import java.text.MessageFormat;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.namespace.QName;

/**
 * Used to indicate that an aspect is missing from a node.
 * 
 * @author Roy Wetherall
 */
@AlfrescoPublicApi
public class AspectMissingException extends RuntimeException
{
    private static final long serialVersionUID = 3257852099244210228L;
    
    private QName missingAspect;
    private NodeRef nodeRef;

    /**
     * Error message
     */
    private static final String ERROR_MESSAGE = "The {0} aspect is missing from this node (id: {1}).  " +
            "It is required for this operation.";
    
    /**
     * Constructor
     */
    public AspectMissingException(QName missingAspect, NodeRef nodeRef)
    {
        super(MessageFormat.format(ERROR_MESSAGE, new Object[]{missingAspect.toString(), nodeRef.getId()}));
        this.missingAspect = missingAspect;
        this.nodeRef = nodeRef;
    }

    public QName getMissingAspect()
    {
        return missingAspect;
    }
    
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }
}
