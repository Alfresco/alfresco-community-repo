/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.model.filefolder;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.query.CannedQuerySortDetails;
import org.alfresco.query.PagingResults;
import org.alfresco.query.CannedQuerySortDetails.SortOrder;
import org.alfresco.repo.domain.node.AuditablePropertiesEntity;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.NodeEntity;
import org.alfresco.repo.domain.node.NodePropertyEntity;
import org.alfresco.repo.domain.node.NodePropertyHelper;
import org.alfresco.repo.domain.node.NodePropertyKey;
import org.alfresco.repo.domain.node.NodePropertyValue;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.domain.query.CannedQueryDAO;
import org.alfresco.repo.security.permissions.impl.acegi.AbstractCannedQueryPermissions;
import org.alfresco.repo.security.permissions.impl.acegi.MethodSecurityInterceptor;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * GetChidren canned query - to get paged list of children of a parent node (sorted or unsorted)
 *
 * @author janv
 * @since 4.0
 */
public class GetChildrenCannedQuery extends AbstractCannedQueryPermissions<NodeRef>
{
    private Log logger = LogFactory.getLog(getClass());
    
    private static final String QUERY_NAMESPACE = "alfresco.node";
    private static final String QUERY_SELECT_GET_CHILDREN_SORTED = "select_GetChildrenSortedCannedQuery";
    private static final String QUERY_SELECT_GET_CHILDREN = "select_GetChildrenCannedQuery";
    
    public static final int MAX_SORT_PAIRS = 2;
    
    // note: speical qnames - originally from Share DocLib default config (however, we do not support arbitrary "fts-alfresco" special sortable fields)
    public static final QName SORT_QNAME_CONTENT_SIZE = QName.createQName("http://www.alfresco.org/model/content/1.0", "content.size");
    public static final QName SORT_QNAME_CONTENT_MIMETYPE = QName.createQName("http://www.alfresco.org/model/content/1.0", "content.mimetype");
    public static final QName SORT_QNAME_NODE_TYPE = QName.createQName("", "TYPE");
    
    
    private NodeDAO nodeDAO;
    private QNameDAO qnameDAO;
    private CannedQueryDAO cannedQueryDAO;
    private NodePropertyHelper nodePropertyHelper;
    private boolean sorted = true;
    
    public GetChildrenCannedQuery(
            NodeDAO nodeDAO,
            QNameDAO qnameDAO,
            CannedQueryDAO cannedQueryDAO,
            NodePropertyHelper nodePropertyHelper,
            MethodSecurityInterceptor methodSecurityInterceptor,
            Method method,
            CannedQueryParameters params,
            String queryExecutionId)
    {
        super(params, queryExecutionId, methodSecurityInterceptor, method);
        
        this.nodeDAO = nodeDAO;
        this.qnameDAO = qnameDAO;
        this.cannedQueryDAO = cannedQueryDAO;
        this.nodePropertyHelper = nodePropertyHelper;
        
        if ((params.getSortDetails() == null) || (params.getSortDetails().getSortPairs().size() == 0))
        {
            sorted = false;
        }
    }
    
    @Override
    protected List<NodeRef> query(CannedQueryParameters parameters)
    {
        Long start = (logger.isDebugEnabled() ? System.currentTimeMillis() : null);
        
        // Get parameters
        GetChildrenCannedQueryParams paramBean = (GetChildrenCannedQueryParams)parameters.getParameterBean();
        NodeRef parentRef = paramBean.getParentRef();
        Set<QName> childNodeTypeQNames = paramBean.getSearchTypeQNames();
        
        CannedQuerySortDetails sortDetails = parameters.getSortDetails();
        List<Pair<? extends Object, SortOrder>> sortPairs = sortDetails.getSortPairs();
        
        ParameterCheck.mandatory("nodeRef", parentRef);
        
        Pair<Long, NodeRef> nodePair = nodeDAO.getNodePair(parentRef);
        if (nodePair == null)
        {
            throw new InvalidNodeRefException("Node does not exist: " + parentRef, parentRef);
        }
        
        Long parentNodeId = nodePair.getFirst();
        
        // Set query params - note: currently using SortableChildEntity to hold (supplemental-) query params
        SortableNodeEntity params = new SortableNodeEntity();
        
        // Set parent node id
        params.setParentNodeId(parentNodeId);
        
        // Set sort props
        int sortPairsCnt = setSortParams(sortPairs, params);
        
        // Set child node type qnames
        if (childNodeTypeQNames != null)
        {
            Set<Long> childNodeTypeQNameIds = qnameDAO.convertQNamesToIds(childNodeTypeQNames, false);
            if (childNodeTypeQNameIds.size() > 0)
            {
                params.setChildNodeTypeQNameIds(new ArrayList<Long>(childNodeTypeQNameIds));
            }
        }
        
        final List<NodeRef> result;
        
        if (sortPairsCnt > 0)
        {
            final List<SortableNode> children = new ArrayList<SortableNode>(100);
            
            SortedChildQueryCallback callback = new SortedChildQueryCallback()
            {
                public boolean handle(NodeRef nodeRef, Map<QName, Serializable> sortPropVals)
                {
                    children.add(new SortableNode(nodeRef, sortPropVals));
                    // More results
                    return true;
                }
            };
            
            SortedResultHandler resultHandler = new SortedResultHandler(callback);
            cannedQueryDAO.executeQuery(QUERY_NAMESPACE, QUERY_SELECT_GET_CHILDREN_SORTED, params, 0, Integer.MAX_VALUE, resultHandler);
            resultHandler.done();
            
            // sort
            Collections.sort(children, new PropComparatorAsc(sortPairs));
            
            result = new ArrayList<NodeRef>(children.size());
            for (SortableNode child : children)
            {
                result.add(child.getNodeRef());
            }
        }
        else
        {
            int requestedCount = parameters.getPageDetails().getResultsRequiredForPaging();
            if (requestedCount != Integer.MAX_VALUE)
            {
                requestedCount++; // add one for "hasMoreItems"
            }
            
            result = new ArrayList<NodeRef>(100);
            
            final int maxItems = requestedCount;
            
            UnsortedChildQueryCallback callback = new UnsortedChildQueryCallback()
            {
                public boolean handle(NodeRef nodeRef)
                {
                    result.add(nodeRef);
                    
                    // More results ?
                    return (result.size() < maxItems);
                }
            };
            
            UnsortedResultHandler resultHandler = new UnsortedResultHandler(callback, parameters.getAuthenticationToken());
            cannedQueryDAO.executeQuery(QUERY_NAMESPACE, QUERY_SELECT_GET_CHILDREN, params, 0, Integer.MAX_VALUE, resultHandler);
            resultHandler.done();
        }
        
        if (start != null)
        {
            logger.debug("Base query "+(sortPairsCnt > 0 ? "(sort=y, perms=n)" : "(sort=n, perms=y)")+": "+result.size()+" in "+(System.currentTimeMillis()-start)+" msecs");
        }
        
        return result;
    }

    // Set sort props (0, 1 or 2)
    private int setSortParams(List<Pair<? extends Object, SortOrder>> sortPairs, SortableNodeEntity params)
    {
        int sortPairsCnt = sortPairs.size();
        
        if (sortPairsCnt > MAX_SORT_PAIRS)
        {
            throw new AlfrescoRuntimeException("GetChildren: exceeded maximum number sort parameters: (max="+MAX_SORT_PAIRS+", actual="+sortPairsCnt);
        }
        
        int cnt = 0;
        
        for (int i = 0; i < sortPairsCnt; i++)
        {
            QName sortQName = (QName)sortPairs.get(i).getFirst();
            if (AuditablePropertiesEntity.getAuditablePropertyQNames().contains(sortQName))
            {
                params.setAuditableProps(true);
            }
            else if (sortQName.equals(SORT_QNAME_NODE_TYPE))
            {
                params.setNodeType(true);
            }
            else
            {
                Long sortQNameId = getQNameId(sortQName);
                if (sortQNameId != null)
                {
                    if (i == 0)
                    {
                        params.setProp1qnameId(sortQNameId);
                    }
                    else if (i == 1)
                    {
                        params.setProp2qnameId(sortQNameId);
                    }
                    else
                    {
                        // belts and braces
                        throw new AlfrescoRuntimeException("GetChildren: unexpected - cannot set sort parameter: "+i);
                    }
                }
                else
                {
                    logger.warn("Skipping sort param - cannot find: "+sortQName);
                    break;
                }
            }
            
            cnt++;
        }
        
        return cnt;
    }
    
    private Long getQNameId(QName sortPropQName)
    {
        if (sortPropQName.equals(SORT_QNAME_CONTENT_SIZE) || sortPropQName.equals(SORT_QNAME_CONTENT_MIMETYPE))
        {
            sortPropQName = ContentModel.PROP_CONTENT;
        }
        
        Pair<Long, QName> qnamePair = qnameDAO.getQName(sortPropQName);
        return (qnamePair == null ? null : qnamePair.getFirst());
    }
    
    @Override
    protected boolean isApplyPostQuerySorting()
    {
        // note: sorted as part of the query impl (using SortableNode results)
        return false;
    }
    
    private class PropComparatorAsc implements Comparator<SortableNode>
    {
        private List<Pair<? extends Object, SortOrder>> sortProps;
        private Collator collator;
        
        public PropComparatorAsc(List<Pair<? extends Object, SortOrder>> sortProps)
        {
            this.sortProps = sortProps;
            this.collator = Collator.getInstance(); // note: currently default locale
        }
        
        public int compare(SortableNode n1, SortableNode n2)
        {
            return compareImpl(n1, n2, sortProps);
        }
        
        private int compareImpl(SortableNode node1In, SortableNode node2In, List<Pair<? extends Object, SortOrder>> sortProps)
        {
            Object pv1 = null;
            Object pv2 = null;
            
            QName sortPropQName = (QName)sortProps.get(0).getFirst();
            boolean sortAscending = (sortProps.get(0).getSecond() == SortOrder.ASCENDING);
            
            SortableNode node1 = node1In;
            SortableNode node2 = node2In; 
            
            if (sortAscending == false)
            {
                node1 = node2In;
                node2 = node1In;
            }
            
            int result = 0;
            
            pv1 = node1.getVal(sortPropQName);
            pv2 = node2.getVal(sortPropQName);
            
            if (pv1 == null)
            {
                return (pv2 == null ? 0 : -1);
            }
            else if (pv2 == null)
            {
                return 1;
            }
            
            if (pv1 instanceof String)
            {
                result = collator.compare((String)pv1, (String)pv2); // TODO use collation keys (re: performance)
            }
            else if (pv1 instanceof Date)
            {
                result = (((Date)pv1).compareTo((Date)pv2));
            }
            else if (pv1 instanceof Long)
            {
                result = (((Long)pv1).compareTo((Long)pv2));
            }
            else if (pv1 instanceof QName)
            {
                result = (((QName)pv1).compareTo((QName)pv2));
            }
            else
            {
                // TODO other comparisons
                throw new RuntimeException("Unsupported sort type: "+pv1.getClass().getName());
            }
            
            if ((result == 0) && (sortProps.size() > 1))
            {
                return compareImpl(node1In, node2In, sortProps.subList(1, sortProps.size()));
            }
            
            return result;
        }
    }
    
    @Override
    protected boolean isApplyPostQueryPermissions()
    {
        return sorted; // true if sorted (if unsorted then permissions are applied as part of the query impl)
    }
    
    @Override
    protected PagingResults<NodeRef> applyPostQueryPermissions(List<NodeRef> results, String authenticationToken, int requestedCount)
    {
        Long start = (logger.isDebugEnabled() ? System.currentTimeMillis() : null);
        
        int requestTotalCountMax = getParameters().requestTotalResultCountMax();
        int maxChecks = (((requestTotalCountMax > 0) && (requestTotalCountMax > requestedCount)) ? requestTotalCountMax : requestedCount);
        int cnt = results.size();
        
        int toIdx = (maxChecks > cnt ? cnt : maxChecks);
        
        // note: assume user has read access to most/majority of the items hence pre-load up to max checks
        preload(results.subList(0, toIdx));
        
        PagingResults<NodeRef> ret = super.applyPostQueryPermissions(results, authenticationToken, requestedCount);
        
        if (start != null)
        {
            logger.debug("Post-query perms: "+ret.getPage().size()+" in "+(System.currentTimeMillis()-start)+" msecs");
        }
        
        return ret;
    }
    
    private void preload(List<NodeRef> nodeRefs)
    {
        Long start = (logger.isTraceEnabled() ? System.currentTimeMillis() : null);
        
        // note: currently pre-loads aspects AND properties
        nodeDAO.cacheNodes(nodeRefs);
        
        if (start != null)
        {
            logger.trace("Pre-load: "+nodeRefs.size()+" in "+(System.currentTimeMillis()-start)+" msecs");
        }
    }
    
    private interface SortedChildQueryCallback
    {
        boolean handle(NodeRef nodeRef, Map<QName, Serializable> sortPropVals);
    }
    
    private interface UnsortedChildQueryCallback
    {
        boolean handle(NodeRef nodeRef);
    }
    
    private class SortedResultHandler implements CannedQueryDAO.ResultHandler<SortableNodeEntity>
    {
        private final SortedChildQueryCallback resultsCallback;
        private boolean more = true;
        
        private SortedResultHandler(SortedChildQueryCallback resultsCallback)
        {
            this.resultsCallback = resultsCallback;
        }
        
        public boolean handleResult(SortableNodeEntity result)
        {
            // Do nothing if no further results are required
            if (!more)
            {
                return false;
            }
            
            NodeRef nodeRef = result.getNode().getNodeRef();
            
            Map<NodePropertyKey, NodePropertyValue> propertyValues = new HashMap<NodePropertyKey, NodePropertyValue>(3);
            
            NodePropertyEntity prop1 = result.getProp1();
            if (prop1 != null)
            {
                propertyValues.put(prop1.getKey(), prop1.getValue());
            }
            
            NodePropertyEntity prop2 = result.getProp2();
            if (prop2 != null)
            {
                propertyValues.put(prop2.getKey(), prop2.getValue());
            }
            
            Map<QName, Serializable> sortPropVals = nodePropertyHelper.convertToPublicProperties(propertyValues);
            
            // special cases
            
            // MLText (eg. cm:title, cm:description, ...)
            for (Map.Entry<QName, Serializable> entry : sortPropVals.entrySet())
            {
                if (entry.getValue() instanceof MLText)
                {
                    sortPropVals.put(entry.getKey(), DefaultTypeConverter.INSTANCE.convert(String.class, (MLText)entry.getValue()));
                }
            }
            
            // ContentData (eg. cm:content.size, cm:content.mimetype)
            ContentData contentData = (ContentData)sortPropVals.get(ContentModel.PROP_CONTENT);
            if (contentData != null)
            {
                sortPropVals.put(SORT_QNAME_CONTENT_SIZE, contentData.getSize());
                sortPropVals.put(SORT_QNAME_CONTENT_MIMETYPE, contentData.getMimetype());
            }
            
            // Auditable props (eg. cm:creator, cm:created, cm:modifier, cm:modified, ...)
            AuditablePropertiesEntity auditableProps = result.getNode().getAuditableProperties();
            if (auditableProps != null)
            {
                for (Map.Entry<QName, Serializable> entry : auditableProps.getAuditableProperties().entrySet())
                {
                    sortPropVals.put(entry.getKey(), entry.getValue());
                }
            }
            
            // Node type
            Long nodeTypeQNameId = result.getNode().getTypeQNameId();
            if (nodeTypeQNameId != null)
            {
                Pair<Long, QName> pair = qnameDAO.getQName(nodeTypeQNameId);
                if (pair != null)
                {
                    sortPropVals.put(SORT_QNAME_NODE_TYPE, pair.getSecond());
                }
            }
            
            // Call back
            boolean more = resultsCallback.handle(nodeRef, sortPropVals);
            if (!more)
            {
                this.more = false;
            }
            
            return more;
        }
        
        public void done()
        {
        }
    }
    
    private class SortableNode
    {
        private NodeRef nodeRef;
        private Map<QName, Serializable> sortPropVals;
        
        public SortableNode(NodeRef nodeRef, Map<QName, Serializable> sortPropVals)
        {
            this.nodeRef = nodeRef;
            this.sortPropVals = sortPropVals;
        }
        
        public NodeRef getNodeRef()
        {
            return nodeRef;
        }
        
        public Serializable getVal(QName sortProp)
        {
            return sortPropVals.get(sortProp);
        }
    }
    
    private class UnsortedResultHandler implements CannedQueryDAO.ResultHandler<NodeEntity>
    {
        private final UnsortedChildQueryCallback resultsCallback;
        private final String authenticationToken;
        
        private boolean more = true;
        
        private static final int BATCH_SIZE = 256 * 4;
        private final List<NodeRef> nodeRefs;
        
        private UnsortedResultHandler(UnsortedChildQueryCallback resultsCallback, String authenticationToken)
        {
            this.resultsCallback = resultsCallback;
            this.authenticationToken = authenticationToken;
            
            nodeRefs = new LinkedList<NodeRef>(); 
        }
        
        public boolean handleResult(NodeEntity result)
        {
            // Do nothing if no further results are required
            if (!more)
            {
                return false;
            }
            
            NodeRef nodeRef = result.getNodeRef();
            
            if (nodeRefs.size() >= BATCH_SIZE)
            {
                // batch
                preloadAndApplyPermissions();
            }
            
            nodeRefs.add(nodeRef);
            
            return more;
        }
        
        private void preloadAndApplyPermissions()
        {
            preload(nodeRefs);
            
            PagingResults<NodeRef> results = applyPermissions(nodeRefs, authenticationToken, nodeRefs.size());
            
            for (NodeRef nodeRef : results.getPage())
            {
                // Call back
                boolean more = resultsCallback.handle(nodeRef);
                if (!more)
                {
                    this.more = false;
                    break;
                }
            }
            
            nodeRefs.clear();
        }
        
        public void done()
        {
            if (nodeRefs.size() >= 0)
            {
                // finish batch
                preloadAndApplyPermissions();
            }
        }
    }
}
