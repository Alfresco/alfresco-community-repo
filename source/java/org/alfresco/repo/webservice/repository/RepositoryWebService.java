/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.webservice.repository;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.webservice.AbstractWebService;
import org.alfresco.repo.webservice.CMLUtil;
import org.alfresco.repo.webservice.ServerQuery;
import org.alfresco.repo.webservice.Utils;
import org.alfresco.repo.webservice.types.CML;
import org.alfresco.repo.webservice.types.ClassDefinition;
import org.alfresco.repo.webservice.types.NamedValue;
import org.alfresco.repo.webservice.types.Node;
import org.alfresco.repo.webservice.types.NodeDefinition;
import org.alfresco.repo.webservice.types.Predicate;
import org.alfresco.repo.webservice.types.Query;
import org.alfresco.repo.webservice.types.Reference;
import org.alfresco.repo.webservice.types.ResultSet;
import org.alfresco.repo.webservice.types.Store;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.apache.axis.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Web service implementation of the RepositoryService. The WSDL for this
 * service can be accessed from
 * http://localhost:8080/alfresco/wsdl/repository-service.wsdl
 * 
 * @author gavinc
 */
public class RepositoryWebService extends AbstractWebService implements
        RepositoryServiceSoapPort
{
    private static Log logger = LogFactory.getLog(RepositoryWebService.class);

    private CMLUtil cmlUtil;

    /**
     * Sets the CML Util
     * 
     * @param cmlUtil   CML util object
     */
    public void setCmlUtil(CMLUtil cmlUtil)
    {
        this.cmlUtil = cmlUtil;
    }

    /**
     * {@inheritDoc}
     */
    public Store createStore(String scheme, String address) throws RemoteException, RepositoryFault
    {
        StoreRef storeRef = this.nodeService.createStore(scheme, address);
        return Utils.convertToStore(storeRef);
    }

    /**
     * {@inheritDoc}
     */
    public Store[] getStores() throws RemoteException, RepositoryFault
    {
        try
        {
            RetryingTransactionCallback<Store[]> callback = new RetryingTransactionCallback<Store[]>()
            {
                public Store[] execute() throws Throwable
                {
                    List<StoreRef> stores = nodeService.getStores();
                    Store[] returnStores = new Store[stores.size()];
                    for (int x = 0; x < stores.size(); x++)
                    {
                        StoreRef storeRef = stores.get(x);
                        
                        if (logger.isDebugEnabled() == true)
                        {
                            logger.debug("Store protocol :" + storeRef.getProtocol());
                        }
                        
                        Store store = Utils.convertToStore(storeRef);
                        returnStores[x] = store;
                    }

                    return returnStores;
                }
            };
            return Utils.getRetryingTransactionHelper(MessageContext.getCurrentContext()).doInTransaction(callback);
        }
        catch (Throwable e)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", e);
            }
            throw new RepositoryFault(0, e.toString());
        }
    }

    /**
     * Executes the given query, caching the results as required.
     */
    private QueryResult executeQuery(final MessageContext msgContext, final ServerQuery<ResultSet> query) throws RepositoryFault
    {
        try
        {
            RetryingTransactionCallback<QueryResult> callback = new RetryingTransactionCallback<QueryResult>()
            {
                public QueryResult execute() throws Throwable
                {
                    // Construct a session to handle the iteration
                    long batchSize = Utils.getBatchSize(msgContext);
                    RepositoryQuerySession session = new RepositoryQuerySession(Long.MAX_VALUE, batchSize, query);
                    String sessionId = session.getId();

                    // Get the first batch of results
                    ResultSet batchedResults = session.getNextResults(serviceRegistry);
                    // Construct the result
                    // TODO: http://issues.alfresco.com/browse/AR-1689
                    boolean haveMoreResults = session.haveMoreResults();
                    QueryResult queryResult = new QueryResult(
                            haveMoreResults ? sessionId : null,
                            batchedResults);

                    // Cache the session
                    if (session.haveMoreResults())
                    {
                        querySessionCache.put(sessionId, session);
                    }

                    // Done
                    return queryResult;
                }
            };
            return Utils.getRetryingTransactionHelper(MessageContext.getCurrentContext()).doInTransaction(callback, true);
        }
        catch (Throwable e)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", e);
            }
            e.printStackTrace();
            throw new RepositoryFault(0, e.toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    public QueryResult query(final Store store, final Query query, final boolean includeMetaData) throws RemoteException, RepositoryFault
    {
        String language = query.getLanguage();
        if (language.equals(Utils.QUERY_LANG_LUCENE) == false)
        {
            throw new RepositoryFault(110, "Only '"
                    + Utils.QUERY_LANG_LUCENE
                    + "' queries are currently supported!");
        }

        final MessageContext msgContext = MessageContext.getCurrentContext();
        SearchQuery serverQuery = new SearchQuery(store, query);
        QueryResult queryResult = executeQuery(msgContext, serverQuery);
        // Done
        return queryResult;
    }

    /**
     * {@inheritDoc}
     */
    public QueryResult queryChildren(final Reference node) throws RemoteException, RepositoryFault
    {
        final MessageContext msgContext = MessageContext.getCurrentContext();
        ChildAssociationQuery query = new ChildAssociationQuery(node);
        QueryResult queryResult = executeQuery(msgContext, query);
        // Done
        return queryResult;
    }
    
    /**
     * {@inheritDoc}
     */
    public QueryResult queryParents(final Reference node) throws RemoteException, RepositoryFault
    {
        final MessageContext msgContext = MessageContext.getCurrentContext();
        ParentAssociationQuery query = new ParentAssociationQuery(node);
        QueryResult queryResult = executeQuery(msgContext, query);
        // Done
        return queryResult;
    }

    /**
     * {@inheritDoc}
     */
    public QueryResult queryAssociated(final Reference node, final Association association) throws RemoteException, RepositoryFault
    {
        final MessageContext msgContext = MessageContext.getCurrentContext();
        AssociationQuery query = new AssociationQuery(node, association);
        QueryResult queryResult = executeQuery(msgContext, query);
        // Done
        return queryResult;
    }

    /**
     * {@inheritDoc}
     */
    public QueryResult fetchMore(final String querySessionId) throws RemoteException, RepositoryFault
    {
        try
        {
            RetryingTransactionCallback<QueryResult> callback = new RetryingTransactionCallback<QueryResult>()
            {
                public QueryResult execute() throws Throwable
                {
                    RepositoryQuerySession session = null;
                    try
                    {
                        // try and get the QuerySession with the given id from the cache
                        session = (RepositoryQuerySession) querySessionCache.get(querySessionId);
                    }
                    catch (ClassCastException e)
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Query session was not generated by the RepositoryWebService: " + querySessionId);
                        }
                        throw new RepositoryFault(
                                4,
                                "querySession with id '" + querySessionId + "' is invalid");
                    }

                    if (session == null)
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Invalid querySession id requested: " + querySessionId);
                        }
                        throw new RepositoryFault(
                                4,
                                "querySession with id '" + querySessionId + "' is invalid");
                    }

                    ResultSet moreResults = session.getNextResults(serviceRegistry);
                    
                    // Drop the cache results if there are no more results expected
                    if (!session.haveMoreResults())
                    {
                        querySessionCache.remove(querySessionId);
                    }

                    // get the next batch of results
                    // TODO: http://issues.alfresco.com/browse/AR-1689
                    boolean haveMoreResults = session.haveMoreResults();
                    QueryResult queryResult = new QueryResult(
                            haveMoreResults ? querySessionId : null,
                            moreResults);

                    // Done
                    return queryResult;
                }
            };
            return Utils.getRetryingTransactionHelper(MessageContext.getCurrentContext()).doInTransaction(callback, true);
        }
        catch (Throwable e)
        {
            if (e instanceof RepositoryFault)
            {
                throw (RepositoryFault) e;
            }
            else
            {
                if (logger.isDebugEnabled())
                {
                    logger.error("Unexpected error occurred", e);
                }
                throw new RepositoryFault(0, e.toString());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public UpdateResult[] update(final CML statements) throws RemoteException, RepositoryFault
    {
        try
        {
            RetryingTransactionCallback<UpdateResult[]> callback = new RetryingTransactionCallback<UpdateResult[]>()
            {
                public UpdateResult[] execute() throws Throwable
                {
                    return cmlUtil.executeCML(statements);
                }
            };
            return Utils.getRetryingTransactionHelper(MessageContext.getCurrentContext()).doInTransaction(callback);
        } 
        catch (Throwable e)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", e);
            }
            throw new RepositoryFault(0, e.toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    public NodeDefinition[] describe(final Predicate items) throws RemoteException, RepositoryFault
    {
        try
        {
            RetryingTransactionCallback<NodeDefinition[]> callback = new RetryingTransactionCallback<NodeDefinition[]>()
            {
                public NodeDefinition[] execute() throws Throwable
                {
                    List<NodeRef> nodes = Utils.resolvePredicate(items, nodeService, searchService, namespaceService);
                    NodeDefinition[] nodeDefs = new NodeDefinition[nodes.size()];
        
                    for (int x = 0; x < nodes.size(); x++)
                    {
                        nodeDefs[x] = setupNodeDefObject(nodes.get(x));
                    }
        
                    return nodeDefs;
                }
            };
            return Utils.getRetryingTransactionHelper(MessageContext.getCurrentContext()).doInTransaction(callback);
        }
        catch (Throwable e)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", e);
            }
            throw new RepositoryFault(0, e.toString());
        }
    }

    /**
     * Creates a NodeDefinition web service type object for the given 
     * repository NodeRef instance
     * 
     * @param nodeRef The NodeRef to generate the NodeDefinition for
     * @return The NodeDefinition representation of nodeRef
     */
    private NodeDefinition setupNodeDefObject(NodeRef nodeRef)
    {
        if (logger.isDebugEnabled())
            logger.debug("Building NodeDefinition for node: " + nodeRef);

        TypeDefinition ddTypeDef = this.dictionaryService
                .getType(this.nodeService.getType(nodeRef));

        // create the web service ClassDefinition type from the data dictionary TypeDefinition
        ClassDefinition typeDef = Utils.setupClassDefObject(ddTypeDef);

        Set<QName> aspectsQNames = this.nodeService.getAspects(nodeRef);
        ClassDefinition[] aspectDefs = new ClassDefinition[aspectsQNames.size()];
        int pos = 0;
        for (QName aspectQName : aspectsQNames)
        {
            AspectDefinition aspectDef = this.dictionaryService.getAspect(aspectQName);
            aspectDefs[pos] = Utils.setupClassDefObject(aspectDef);
            pos++;
        }

        return new NodeDefinition(typeDef, aspectDefs);
    }

    /**
     * Gets the nodes associatiated with the predicate provided.  Usefull when the store and ids of the required
     * nodes are known.
     * 
     * {@inheritDoc}
     */
    public Node[] get(final Predicate where) throws RemoteException, RepositoryFault
    {
        try
        {
            RetryingTransactionCallback<Node[]> callback = new RetryingTransactionCallback<Node[]>()
            {
                public Node[] execute() throws Throwable
                {
                    // Resolve the predicate to a list of node references
                    List<NodeRef> nodeRefs = Utils.resolvePredicate(where, nodeService, searchService, namespaceService);
                    List<Node> nodeList = new ArrayList<Node>();
                    for (NodeRef nodeRef : nodeRefs)
                    {
                        // search can return nodes that no longer exist, so we need to  ignore these
                        if(nodeService.exists(nodeRef) == false) 
                        {
                            if(logger.isDebugEnabled())
                            {
                                logger.warn("Search returned node that doesn't exist: " + nodeRef);
                            }
                        }
                        
                        // Get the nodes reference
                        Reference reference = Utils.convertToReference(nodeService, namespaceService, nodeRef);
                        
                        // Get the nodes type
                        String type = nodeService.getType(nodeRef).toString();
                        
                        // Get the nodes aspects
                        Set<QName> aspectQNames = nodeService.getAspects(nodeRef);
                        String[] aspects = new String[aspectQNames.size()];
                        int aspectIndex = 0;
                        for (QName aspectQName : aspectQNames)
                        {
                            aspects[aspectIndex] = aspectQName.toString();
                            aspectIndex++;
                        }
                        
                        // Get the nodes properties
                        Map<QName, Serializable> propertyMap = nodeService.getProperties(nodeRef);
                        NamedValue[] properties = new NamedValue[propertyMap.size()];
                        int propertyIndex = 0;
                        for (Map.Entry<QName, Serializable> entry : propertyMap.entrySet())
                        { 
                            properties[propertyIndex] = Utils.createNamedValue(dictionaryService, entry.getKey(), entry.getValue());
                            propertyIndex++;
                        }
                        
                        // Create the node and add to the array
                        Node node = new Node(reference, type, aspects, properties);
                        nodeList.add(node);
                    }
                    
                    Node[] nodes = nodeList.toArray(new Node[nodeList.size()]);
                    
                    return nodes;
                }
            };
            return Utils.getRetryingTransactionHelper(MessageContext.getCurrentContext()).doInTransaction(callback);
        } 
        catch (Throwable e)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", e);
            }
            throw new RepositoryFault(0, e.toString());
        }
    }    
}
