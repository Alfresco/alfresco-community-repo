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
package org.alfresco.service.cmr.repository;

import java.text.MessageFormat;

import org.alfresco.service.namespace.QName;

/**
 * Used to indicate that an aspect is missing from a node.
 * 
 * @author Roy Wetherall
 */
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
