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

import org.alfresco.jcr.session.SessionImpl;
import org.alfresco.jcr.util.AbstractRangeIterator;
import org.alfresco.service.cmr.repository.ChildAssociationRef;


/**
 * Alfresco implementation of a Node Iterator
 * 
 * @author David Caruana
 */
public class ChildAssocNodeIteratorImpl extends AbstractRangeIterator
    implements NodeIterator
{
    private SessionImpl sessionImpl;
    private List<ChildAssociationRef> childAssocs;
    
    
    /**
     * Construct
     * 
     * @param context  session context
     * @param nodes  node list
     */
    public ChildAssocNodeIteratorImpl(SessionImpl sessionImpl, List<ChildAssociationRef> childAssocs)
    {
        this.sessionImpl = sessionImpl;
        this.childAssocs = childAssocs;
    }
    
    /* (non-Javadoc)
     * @see javax.jcr.NodeIterator#nextNode()
     */
    public Node nextNode()
    {
        long position = skip();
        ChildAssociationRef childAssocRef = childAssocs.get((int)position);
        NodeImpl nodeImpl = new NodeImpl(sessionImpl, childAssocRef.getChildRef());
        return nodeImpl.getProxy();
    }

    /* (non-Javadoc)
     * @see javax.jcr.RangeIterator#getSize()
     */
    public long getSize()
    {
        return childAssocs.size();
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    public Object next()
    {
        return nextNode();
    }

}
