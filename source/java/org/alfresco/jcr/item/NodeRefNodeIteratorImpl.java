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
package org.alfresco.jcr.item;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.query.QueryResult;

import org.alfresco.jcr.session.SessionImpl;
import org.alfresco.jcr.util.AbstractRangeIterator;
import org.alfresco.jcr.util.JCRProxyFactory;
import org.alfresco.service.cmr.repository.NodeRef;


/**
 * Alfresco implementation of a Node Iterator
 * 
 * @author David Caruana
 */
public class NodeRefNodeIteratorImpl extends AbstractRangeIterator
    implements NodeIterator
{
    private SessionImpl sessionImpl;
    private List<NodeRef> nodeRefs;
    
    /**
     * Construct
     * 
     * @param context  session context
     * @param nodes  node list
     */
    public NodeRefNodeIteratorImpl(SessionImpl sessionImpl, List<NodeRef> nodeRefs)
    {
        this.sessionImpl = sessionImpl;
        this.nodeRefs = nodeRefs;
    }
    
    /* (non-Javadoc)
     * @see javax.jcr.NodeIterator#nextNode()
     */
    public Node nextNode()
    {
        long position = skip();
        NodeRef nodeRef = nodeRefs.get((int)position);
        NodeImpl nodeImpl = new NodeImpl(sessionImpl, nodeRef);
        return nodeImpl.getProxy();
    }

    /* (non-Javadoc)
     * @see javax.jcr.RangeIterator#getSize()
     */
    public long getSize()
    {
        return nodeRefs.size();
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    public Object next()
    {
        return nextNode();
    }

}
