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
package org.alfresco.repo.node.db.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.ChildAssoc;
import org.alfresco.repo.domain.Node;
import org.alfresco.repo.domain.NodeAssoc;
import org.alfresco.repo.domain.NodeKey;
import org.alfresco.repo.domain.NodeStatus;
import org.alfresco.repo.domain.Store;
import org.alfresco.repo.domain.StoreKey;
import org.alfresco.repo.domain.hibernate.ChildAssocImpl;
import org.alfresco.repo.domain.hibernate.NodeAssocImpl;
import org.alfresco.repo.domain.hibernate.NodeImpl;
import org.alfresco.repo.domain.hibernate.NodeStatusImpl;
import org.alfresco.repo.domain.hibernate.StoreImpl;
import org.alfresco.repo.node.db.NodeDaoService;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.hibernate.ObjectDeletedException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Hibernate-specific implementation of the persistence-independent <b>node</b> DAO interface
 * 
 * @author Derek Hulley
 */
public class HibernateNodeDaoServiceImpl extends HibernateDaoSupport implements NodeDaoService
{
    private static final String QUERY_GET_ALL_STORES = "store.GetAllStores";
    private static final String QUERY_GET_NODE_ASSOC = "node.GetNodeAssoc";
    private static final String QUERY_GET_NODE_ASSOC_TARGETS = "node.GetNodeAssocTargets";
    private static final String QUERY_GET_NODE_ASSOC_SOURCES = "node.GetNodeAssocSources";
    private static final String QUERY_GET_CONTENT_DATA_STRINGS = "node.GetContentDataStrings";
    
    /** a uuid identifying this unique instance */
    private String uuid;

    /**
     * 
     */
    public HibernateNodeDaoServiceImpl()
    {
        this.uuid = GUID.generate();
    }

    /**
     * Checks equality by type and uuid
     */
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        else if (!(obj instanceof HibernateNodeDaoServiceImpl))
        {
            return false;
        }
        HibernateNodeDaoServiceImpl that = (HibernateNodeDaoServiceImpl) obj;
        return this.uuid.equals(that.uuid);
    }
    
    /**
     * @see #uuid
     */
    public int hashCode()
    {
        return uuid.hashCode();
    }

    /**
     * Does this <tt>Session</tt> contain any changes which must be
     * synchronized with the store?
     * 
     * @return true => changes are pending
     */
    public boolean isDirty()
    {
        // create a callback for the task
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                return session.isDirty();
            }
        };
        // execute the callback
        return ((Boolean)getHibernateTemplate().execute(callback)).booleanValue();
    }

    /**
     * Just flushes the session
     */
    public void flush()
    {
        getSession().flush();
    }

    /**
     * @see #QUERY_GET_ALL_STORES
     */
    @SuppressWarnings("unchecked")
    public List<Store> getStores()
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_ALL_STORES);
                return query.list();
            }
        };
        List<Store> queryResults = (List) getHibernateTemplate().execute(callback);
        // done
        return queryResults;
    }
    
    /**
     * Ensures that the store protocol/identifier combination is unique
     */
    public Store createStore(String protocol, String identifier)
    {
        // ensure that the name isn't in use
        Store store = getStore(protocol, identifier);
        if (store != null)
        {
            throw new RuntimeException("A store already exists: \n" +
                    "   protocol: " + protocol + "\n" +
                    "   identifier: " + identifier + "\n" +
                    "   store: " + store);
        }
        
        store = new StoreImpl();
        // set key
        store.setKey(new StoreKey(protocol, identifier));
        // persist so that it is present in the hibernate cache
        getHibernateTemplate().save(store);
        // create and assign a root node
        Node rootNode = newNode(
                store,
                GUID.generate(),
                ContentModel.TYPE_STOREROOT);
        store.setRootNode(rootNode);
        // done
        return store;
    }

    public Store getStore(String protocol, String identifier)
    {
        StoreKey storeKey = new StoreKey(protocol, identifier);
        Store store = (Store) getHibernateTemplate().get(StoreImpl.class, storeKey);
        // done
        return store;
    }

    /**
     * Fetch the node status, if it exists
     */
    public NodeStatus getNodeStatus(NodeRef nodeRef, boolean create)
    {
        NodeKey nodeKey = new NodeKey(nodeRef);
        NodeStatus status = null;
        try
        {
            status = (NodeStatus) getHibernateTemplate().get(NodeStatusImpl.class, nodeKey);
        }
        catch (DataAccessException e)
        {
            if (e.contains(ObjectDeletedException.class))
            {
                // the object no longer exists
                return null;
            }
            throw e;
        }
        // create if necessary
        if (status == null && create)
        {
            status = new NodeStatusImpl();
            status.setKey(nodeKey);
            status.setChangeTxnId(AlfrescoTransactionSupport.getTransactionId());
            getHibernateTemplate().save(status);
        }
        // done
        return status;
    }

    public void recordChangeId(NodeRef nodeRef)
    {
        NodeKey key = new NodeKey(nodeRef);
        
        NodeStatus status = (NodeStatus) getHibernateTemplate().get(NodeStatusImpl.class, key);
        if (status == null)
        {
            // the node never existed or the status was deleted
            return;
        }
        else
        {
            status.setChangeTxnId(AlfrescoTransactionSupport.getTransactionId());
        }
    }

    public Node newNode(Store store, String uuid, QName nodeTypeQName) throws InvalidTypeException
    {
        NodeKey key = new NodeKey(store.getKey(), uuid);
        
        // create (or reuse) the mandatory node status
        NodeStatus status = (NodeStatus) getHibernateTemplate().get(NodeStatusImpl.class, key);
        if (status == null)
        {
            status = new NodeStatusImpl();
            status.setKey(key);
        }
        else
        {
            // The node existed at some point.
            // Although unlikely, it is possible that the node was deleted in this transaction.
            // If that is the case, then the session has to be flushed so that the database
            // constraints aren't violated as the node creation will write to the database to
            // get an ID
            if (status.getChangeTxnId().equals(AlfrescoTransactionSupport.getTransactionId()))
            {
                // flush
                getHibernateTemplate().flush();
            }
        }

        // build a concrete node based on a bootstrap type
        Node node = new NodeImpl();
        // set other required properties
        node.setStore(store);
        node.setUuid(uuid);
        node.setTypeQName(nodeTypeQName);
        // persist the node
        getHibernateTemplate().save(node);

        // set required status properties
        status.setNode(node);
        status.setChangeTxnId(AlfrescoTransactionSupport.getTransactionId());
        // persist the nodestatus
        getHibernateTemplate().save(status);

        // done
        return node;
    }

    public Node getNode(NodeRef nodeRef)
    {
        // get it via the node status
        NodeStatus status = getNodeStatus(nodeRef, false);
        if (status == null)
        {
            // no status implies no node
            return null;
        }
        else
        {
            // a status may have a node
            Node node = status.getNode();
            return node;
        }
    }
    
    /**
     * Manually ensures that all cascading of associations is taken care of
     */
    public void deleteNode(Node node, boolean cascade)
    {
        // delete all parent assocs
        Collection<ChildAssoc> parentAssocs = node.getParentAssocs();
        parentAssocs = new ArrayList<ChildAssoc>(parentAssocs);
        for (ChildAssoc assoc : parentAssocs)
        {
            deleteChildAssoc(assoc, false);  // we don't cascade upwards
        }
        // delete all child assocs
        Collection<ChildAssoc> childAssocs = node.getChildAssocs();
        childAssocs = new ArrayList<ChildAssoc>(childAssocs);
        for (ChildAssoc assoc : childAssocs)
        {
            deleteChildAssoc(assoc, cascade);   // potentially cascade downwards
        }
        // delete all target assocs
        Collection<NodeAssoc> targetAssocs = node.getTargetNodeAssocs();
        targetAssocs = new ArrayList<NodeAssoc>(targetAssocs);
        for (NodeAssoc assoc : targetAssocs)
        {
            deleteNodeAssoc(assoc);
        }
        // delete all source assocs
        Collection<NodeAssoc> sourceAssocs = node.getSourceNodeAssocs();
        sourceAssocs = new ArrayList<NodeAssoc>(sourceAssocs);
        for (NodeAssoc assoc : sourceAssocs)
        {
            deleteNodeAssoc(assoc);
        }
        // update the node status
        NodeRef nodeRef = node.getNodeRef();
        NodeStatus nodeStatus = getNodeStatus(nodeRef, true);
        nodeStatus.setNode(null);
        nodeStatus.setChangeTxnId(AlfrescoTransactionSupport.getTransactionId());
        // finally delete the node
        getHibernateTemplate().delete(node);
        // done
    }

    public ChildAssoc newChildAssoc(
            Node parentNode,
            Node childNode,
            boolean isPrimary,
            QName assocTypeQName,
            QName qname)
    {
        ChildAssoc assoc = new ChildAssocImpl();
        assoc.setTypeQName(assocTypeQName);
        assoc.setIsPrimary(isPrimary);
        assoc.setQname(qname);
        assoc.buildAssociation(parentNode, childNode);
        // persist
        getHibernateTemplate().save(assoc);
        // done
        return assoc;
    }
    
    public ChildAssoc getChildAssoc(
            Node parentNode,
            Node childNode,
            QName assocTypeQName,
            QName qname)
    {
        ChildAssociationRef childAssocRef = new ChildAssociationRef(
                assocTypeQName,
                parentNode.getNodeRef(),
                qname,
                childNode.getNodeRef());
        // get all the parent's child associations
        Collection<ChildAssoc> assocs = parentNode.getChildAssocs();
        // hunt down the desired assoc
        for (ChildAssoc assoc : assocs)
        {
            // is it a match?
            if (!assoc.getChildAssocRef().equals(childAssocRef))    // not a match
            {
                continue;
            }
            else
            {
                return assoc;
            }
        }
        // not found
        return null;
    }

    /**
     * Manually enforces cascade deletions down primary associations
     */
    public void deleteChildAssoc(ChildAssoc assoc, boolean cascade)
    {
        Node childNode = assoc.getChild();
        
        // maintain inverse association sets
        assoc.removeAssociation();
        // remove instance
        getHibernateTemplate().delete(assoc);
        
        if (cascade && assoc.getIsPrimary())   // the assoc is primary
        {
            // delete the child node
            deleteNode(childNode, cascade);
            /*
             * The child node deletion will cascade delete all assocs to
             * and from it, but we have safely removed this one, so no
             * duplicate call will be received to do this
             */
        }
    }

    public ChildAssoc getPrimaryParentAssoc(Node node)
    {
        // get the assocs pointing to the node
        Collection<ChildAssoc> parentAssocs = node.getParentAssocs();
        ChildAssoc primaryAssoc = null;
        for (ChildAssoc assoc : parentAssocs)
        {
            // ignore non-primary assocs
            if (!assoc.getIsPrimary())
            {
                continue;
            }
            else if (primaryAssoc != null)
            {
                // we have more than one somehow
                throw new DataIntegrityViolationException(
                        "Multiple primary associations: \n" +
                        "   child: " + node + "\n" +
                        "   first primary assoc: " + primaryAssoc + "\n" +
                        "   second primary assoc: " + assoc);
            }
            primaryAssoc = assoc;
            // we keep looping to hunt out data integrity issues
        }
        // did we find a primary assoc?
        if (primaryAssoc == null)
        {
            // the only condition where this is allowed is if the given node is a root node
            Store store = node.getStore();
            Node rootNode = store.getRootNode();
            if (rootNode == null)
            {
                // a store without a root node - the entire store is hosed
                throw new DataIntegrityViolationException("Store has no root node: \n" +
                        "   store: " + store);
            }
            if (!rootNode.equals(node))
            {
                // it wasn't the root node
                throw new DataIntegrityViolationException("Non-root node has no primary parent: \n" +
                        "   child: " + node);
            }
        }
        // done
        return primaryAssoc;
    }

    public NodeAssoc newNodeAssoc(Node sourceNode, Node targetNode, QName assocTypeQName)
    {
        NodeAssoc assoc = new NodeAssocImpl();
        assoc.setTypeQName(assocTypeQName);
        assoc.buildAssociation(sourceNode, targetNode);
        // persist
        getHibernateTemplate().save(assoc);
        // done
        return assoc;
    }

    public NodeAssoc getNodeAssoc(
            final Node sourceNode,
            final Node targetNode,
            final QName assocTypeQName)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_NODE_ASSOC);
                query.setEntity("sourceNode", sourceNode)
                     .setEntity("targetNode", targetNode)
                     .setString("assocTypeQName", assocTypeQName.toString())
                     .setMaxResults(1);
                return query.uniqueResult();
            }
        };
        Object queryResult = getHibernateTemplate().execute(callback);
        if (queryResult == null)
        {
            return null;
        }
        NodeAssoc assoc = (NodeAssoc) queryResult;
        // done
        return assoc;
    }

    @SuppressWarnings("unchecked")
    public Collection<Node> getNodeAssocTargets(final Node sourceNode, final QName assocTypeQName)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_NODE_ASSOC_TARGETS);
                query.setEntity("sourceNode", sourceNode)
                     .setString("assocTypeQName", assocTypeQName.toString());
                return query.list();
            }
        };
        List<Node> queryResults = (List) getHibernateTemplate().execute(callback);
        // done
        return queryResults;
    }

    @SuppressWarnings("unchecked")
    public Collection<Node> getNodeAssocSources(final Node targetNode, final QName assocTypeQName)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_NODE_ASSOC_SOURCES);
                query.setEntity("targetNode", targetNode)
                     .setString("assocTypeQName", assocTypeQName.toString());
                return query.list();
            }
        };
        List<Node> queryResults = (List) getHibernateTemplate().execute(callback);
        // done
        return queryResults;
    }

    public void deleteNodeAssoc(NodeAssoc assoc)
    {
        // maintain inverse association sets
        assoc.removeAssociation();
        // remove instance
        getHibernateTemplate().delete(assoc);
    }

    @SuppressWarnings("unchecked")
    public List<String> getContentDataStrings()
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(HibernateNodeDaoServiceImpl.QUERY_GET_CONTENT_DATA_STRINGS);
                return query.list();
            }
        };
        List<String> queryResults = (List) getHibernateTemplate().execute(callback);
        return queryResults;
    }
}




















