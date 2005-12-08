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


/**
 * Thrown when an operation cannot be performed because the <b>node</b> reference
 * no longer exists.
 * 
 * @author Derek Hulley
 */
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
        super(msg);
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
