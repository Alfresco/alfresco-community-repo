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

import org.alfresco.service.namespace.QName;

/**
 * Thrown when an operation could not be performed because a named association already
 * exists between two nodes
 * 
 * @author Derek Hulley
 */
public class AssociationExistsException extends RuntimeException
{
    private static final long serialVersionUID = 3256440317824874800L;

    private NodeRef sourceRef;
    private NodeRef targetRef;
    private QName qname;
    
    /**
     * @param sourceRef the source of the association
     * @param targetRef the target of the association
     * @param qname the qualified name of the association
     */
    public AssociationExistsException(NodeRef sourceRef, NodeRef targetRef, QName qname)
    {
        super();
        this.sourceRef = sourceRef;
        this.targetRef = targetRef;
        this.qname = qname;
    }

    public NodeRef getSourceRef()
    {
        return sourceRef;
    }

    public NodeRef getTargetRef()
    {
        return targetRef;
    }
    
    public QName getQName()
    {
        return qname;
    }
}
