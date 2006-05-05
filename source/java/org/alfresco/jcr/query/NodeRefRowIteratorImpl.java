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
package org.alfresco.jcr.query;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.query.Query;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.alfresco.jcr.session.SessionImpl;
import org.alfresco.jcr.util.AbstractRangeIterator;
import org.alfresco.jcr.util.JCRProxyFactory;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;


/**
 * Row Iterator based on a list of Node References
 * 
 * @author David Caruana
 */
public class NodeRefRowIteratorImpl extends AbstractRangeIterator implements RowIterator
{
    private SessionImpl session;
    private Map<QName, PropertyDefinition> columns;
    private List<NodeRef> nodeRefs;
    private RowIterator proxy = null;
    
    /**
     * Construct
     * 
     * @param session
     * @param columnNames
     * @param nodeRefs
     */
    public NodeRefRowIteratorImpl(SessionImpl session, Map<QName, PropertyDefinition> columns, List<NodeRef> nodeRefs)
    {
        this.session = session;
        this.columns = columns;
        this.nodeRefs = nodeRefs;
    }

    /**
     * Get proxied JCR Query
     * 
     * @return  proxy
     */
    public RowIterator getProxy()
    {
        if (proxy == null)
        {
            proxy = (RowIterator)JCRProxyFactory.create(this, RowIterator.class, session);
        }
        return proxy;
    }
    
    /* (non-Javadoc)
     * @see javax.jcr.query.RowIterator#nextRow()
     */
    public Row nextRow()
    {
        long position = skip();
        NodeRef nodeRef = nodeRefs.get((int)position);
        NodeService nodeService = session.getRepositoryImpl().getServiceRegistry().getNodeService();        
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        return new PropertyMapRowImpl(session, columns, nodeRef, properties);
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
        return nextRow();
    }
    
}
