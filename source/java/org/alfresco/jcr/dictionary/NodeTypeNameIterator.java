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
package org.alfresco.jcr.dictionary;

import java.util.List;

import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;

import org.alfresco.jcr.util.AbstractRangeIterator;
import org.alfresco.service.namespace.QName;


/**
 * Alfresco implementation of a Node Type Iterator
 * 
 * @author David Caruana
 */
public class NodeTypeNameIterator extends AbstractRangeIterator
    implements NodeTypeIterator
{
    private NodeTypeManagerImpl typeManager;
    private List<QName> nodeTypeNames;
    
    
    /**
     * Construct
     * 
     * @param context  session context
     * @param nodeTypes  node type list
     */
    public NodeTypeNameIterator(NodeTypeManagerImpl typeManager, List<QName> nodeTypeNames)
    {
        this.typeManager = typeManager;
        this.nodeTypeNames = nodeTypeNames;
    }
    
    /* (non-Javadoc)
     * @see javax.jcr.nodetype.NodeTypeIterator#nextNodeType()
     */
    public NodeType nextNodeType()
    {
        long position = skip();
        QName name = nodeTypeNames.get((int)position);
        return typeManager.getNodeTypeImpl(name);
    }

    /* (non-Javadoc)
     * @see javax.jcr.RangeIterator#getSize()
     */
    public long getSize()
    {
        return nodeTypeNames.size();
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    public Object next()
    {
        return nextNodeType();
    }

}
