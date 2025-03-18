/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.node.getchildren;

import java.io.Serializable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.query.CannedQuerySortDetails;
import org.alfresco.query.CannedQuerySortDetails.SortOrder;
import org.alfresco.repo.domain.node.AuditablePropertiesEntity;
import org.alfresco.repo.domain.node.Node;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.NodeEntity;
import org.alfresco.repo.domain.node.NodePropertyEntity;
import org.alfresco.repo.domain.node.NodePropertyHelper;
import org.alfresco.repo.domain.node.NodePropertyKey;
import org.alfresco.repo.domain.node.NodePropertyValue;
import org.alfresco.repo.domain.node.ReferenceablePropertiesEntity;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.domain.query.CannedQueryDAO;
import org.alfresco.repo.node.getchildren.FilterPropString.FilterTypeString;
import org.alfresco.repo.security.permissions.PermissionCheckedValue.PermissionCheckedValueMixin;
import org.alfresco.repo.security.permissions.impl.acegi.AbstractCannedQueryPermissions;
import org.alfresco.repo.security.permissions.impl.acegi.MethodSecurityBean;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.AlfrescoCollator;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;

/**
 * GetChildren canned query
 * 
 * To get paged list of children of a parent node filtered by child type. Also optionally filtered and/or sorted by one or more properties (up to three).
 *
 * @author janv
 * @since 4.0
 */
public class GetChildrenCannedQuery extends AbstractCannedQueryPermissions<NodeRef>
{
    private Log logger = LogFactory.getLog(getClass());

    private static final String QUERY_NAMESPACE = "alfresco.node";
    private static final String QUERY_SELECT_GET_CHILDREN_WITH_PROPS = "select_GetChildrenCannedQueryWithProps";
    private static final String QUERY_SELECT_GET_CHILDREN_WITHOUT_PROPS = "select_GetChildrenCannedQueryWithoutProps";

    public static final int MAX_FILTER_SORT_PROPS = 3;

    // note: special qnames - originally from Share DocLib default config (however, we do not support arbitrary "fts-alfresco" special sortable fields)
    public static final QName SORT_QNAME_CONTENT_SIZE = QName.createQName("http://www.alfresco.org/model/content/1.0", "content.size");
    public static final QName SORT_QNAME_CONTENT_MIMETYPE = QName.createQName("http://www.alfresco.org/model/content/1.0", "content.mimetype");
    public static final QName SORT_QNAME_NODE_TYPE = QName.createQName("", "TYPE");
    public static final QName SORT_QNAME_NODE_IS_FOLDER = QName.createQName("", "IS_FOLDER"); // ALF-13968

    public static final QName FILTER_QNAME_NODE_IS_PRIMARY = QName.createQName("", "IS_PRIMARY");

    private NodeDAO nodeDAO;
    private QNameDAO qnameDAO;
    private CannedQueryDAO cannedQueryDAO;
    private NodePropertyHelper nodePropertyHelper;
    private TenantService tenantService;
    protected NodeService nodeService;

    private boolean applyPostQueryPermissions = false; // if true, the permissions will be applied post-query (else should be applied as part of the "queryAndFilter")

    public GetChildrenCannedQuery(
            NodeDAO nodeDAO,
            QNameDAO qnameDAO,
            CannedQueryDAO cannedQueryDAO,
            NodePropertyHelper nodePropertyHelper,
            TenantService tenantService,
            NodeService nodeService,
            MethodSecurityBean<NodeRef> methodSecurity,
            CannedQueryParameters params)
    {
        super(params, methodSecurity);

        this.nodeDAO = nodeDAO;
        this.qnameDAO = qnameDAO;
        this.cannedQueryDAO = cannedQueryDAO;
        this.nodePropertyHelper = nodePropertyHelper;
        this.tenantService = tenantService;
        this.nodeService = nodeService;

        if ((params.getSortDetails() != null) && (params.getSortDetails().getSortPairs().size() > 0))
        {
            applyPostQueryPermissions = true;
        }

        // TODO refactor (only apply post query if sorted - as above)
        GetChildrenCannedQueryParams paramBean = (GetChildrenCannedQueryParams) params.getParameterBean();
        if ((paramBean.getFilterProps() != null) && (paramBean.getFilterProps().size() > 0))
        {
            applyPostQueryPermissions = true;
        }
    }

    protected FilterSortChildQueryCallback getFilterSortChildQuery(final List<FilterSortNode> children, final List<FilterProp> filterProps, GetChildrenCannedQueryParams paramBean)
    {
        Set<QName> inclusiveAspects = paramBean.getInclusiveAspects();
        Set<QName> exclusiveAspects = paramBean.getExclusiveAspects();

        return new DefaultFilterSortChildQueryCallback(children, filterProps, inclusiveAspects, exclusiveAspects);
    }

    protected UnsortedChildQueryCallback getUnsortedChildQueryCallback(final List<NodeRef> rawResult, final int requestedCount, GetChildrenCannedQueryParams paramBean)
    {
        Set<QName> inclusiveAspects = paramBean.getInclusiveAspects();
        Set<QName> exclusiveAspects = paramBean.getExclusiveAspects();
        return new DefaultUnsortedChildQueryCallback(rawResult, requestedCount, inclusiveAspects, exclusiveAspects);
    }

    @Override
    protected List<NodeRef> queryAndFilter(CannedQueryParameters parameters)
    {
        Long start = (logger.isDebugEnabled() ? System.currentTimeMillis() : null);

        // Get parameters
        GetChildrenCannedQueryParams paramBean = (GetChildrenCannedQueryParams) parameters.getParameterBean();

        // Get parent node
        NodeRef parentRef = paramBean.getParentRef();
        ParameterCheck.mandatory("nodeRef", parentRef);
        Pair<Long, NodeRef> nodePair = nodeDAO.getNodePair(parentRef);
        if (nodePair == null)
        {
            throw new InvalidNodeRefException("Parent node does not exist: " + parentRef, parentRef);
        }
        Long parentNodeId = nodePair.getFirst();

        // Set query params - note: currently using SortableChildEntity to hold (supplemental-) query params
        FilterSortNodeEntity params = new FilterSortNodeEntity();

        // Set parent node id
        params.setParentNodeId(parentNodeId);

        // Get filter details
        Set<QName> childNodeTypeQNames = paramBean.getChildTypeQNames();
        Set<QName> assocTypeQNames = paramBean.getAssocTypeQNames();

        final List<FilterProp> filterProps = new ArrayList<>(paramBean.getFilterProps().size());
        filterProps.addAll(paramBean.getFilterProps()); // clone (to allow special handling for isPrimary)

        String pattern = paramBean.getPattern();

        // Get sort details
        CannedQuerySortDetails sortDetails = parameters.getSortDetails();
        @SuppressWarnings({"unchecked", "rawtypes"})
        final List<Pair<QName, SortOrder>> sortPairs = (List) sortDetails.getSortPairs();

        if (filterProps.size() > 0)
        {
            // special handling of isPrimary filter (not counted as a filter/sort "property")
            Boolean isPrimary = null;
            int idx = 0;
            for (FilterProp filter : filterProps)
            {
                if ((filter instanceof FilterPropBoolean) &&
                        ((FilterPropBoolean) filter).getPropName().equals(FILTER_QNAME_NODE_IS_PRIMARY))
                {
                    isPrimary = ((FilterPropBoolean) filter).getPropVal();
                    break;
                }
                idx++;
            }
            if (isPrimary != null)
            {
                params.setIsPrimary(isPrimary);
                filterProps.remove(idx);
            }
        }

        // Set sort / filter params
        // Note - need to keep the sort properties in their requested order
        List<QName> sortFilterProps = new ArrayList<QName>(filterProps.size() + sortPairs.size());
        for (Pair<QName, SortOrder> sort : sortPairs)
        {
            QName sortQName = sort.getFirst();
            if (!sortFilterProps.contains(sortQName))
            {
                sortFilterProps.add(sortQName);
            }
        }
        for (FilterProp filter : filterProps)
        {
            QName filterQName = filter.getPropName();
            if (!sortFilterProps.contains(filterQName))
            {
                sortFilterProps.add(filterQName);
            }
        }

        int filterSortPropCnt = sortFilterProps.size();

        if (filterSortPropCnt > MAX_FILTER_SORT_PROPS)
        {
            throw new AlfrescoRuntimeException("GetChildren: exceeded maximum number filter/sort properties: (max=" + MAX_FILTER_SORT_PROPS + ", actual=" + filterSortPropCnt);
        }

        filterSortPropCnt = setFilterSortParams(sortFilterProps, params);

        List<NodeRef> result = new ArrayList<>(0);

        try
        {
            if ((childNodeTypeQNames != null) && (childNodeTypeQNames.size() > 0))
            {
                // Set child node type qnames (additional filter - performed by DB query)
                Set<Long> childNodeTypeQNameIds = qnameDAO.convertQNamesToIds(childNodeTypeQNames, false);
                if (childNodeTypeQNameIds.size() > 0)
                {
                    params.setChildNodeTypeQNameIds(new ArrayList<Long>(childNodeTypeQNameIds));
                }
                else
                {
                    // short-circuit - return no results - given node type qname(s) do not exist
                    return result;
                }
            }

            if ((assocTypeQNames != null) && (assocTypeQNames.size() > 0))
            {
                // Set assoc type qnames (additional filter - performed by DB query)
                Set<Long> assocTypeQNameIds = qnameDAO.convertQNamesToIds(assocTypeQNames, false);
                if (assocTypeQNameIds.size() > 0)
                {
                    params.setAssocTypeQNameIds(assocTypeQNameIds);
                }
                else
                {
                    // short-circuit - return no results - given assoc type qname(s) do not exist
                    return result;
                }
            }

            if (pattern != null)
            {
                // TODO, check that we should be tied to the content model in this way. Perhaps a configurable property
                // name against which compare the pattern?
                Pair<Long, QName> nameQName = qnameDAO.getQName(ContentModel.PROP_NAME);
                if (nameQName == null)
                {
                    throw new AlfrescoRuntimeException("Unable to determine qname id of name property");
                }
                params.setNamePropertyQNameId(nameQName.getFirst());
                params.setPattern(pattern);
            }

            if (filterSortPropCnt > 0)
            {
                // filtered and/or sorted - note: permissions will be applied post query
                final List<FilterSortNode> children = new ArrayList<FilterSortNode>(100);
                final FilterSortChildQueryCallback c = getFilterSortChildQuery(children, filterProps, paramBean);
                FilterSortResultHandler resultHandler = new FilterSortResultHandler(c);
                cannedQueryDAO.executeQuery(QUERY_NAMESPACE, QUERY_SELECT_GET_CHILDREN_WITH_PROPS, params, 0, Integer.MAX_VALUE, resultHandler);
                resultHandler.done();

                if (sortPairs.size() > 0)
                {
                    Long startSort = (logger.isDebugEnabled() ? System.currentTimeMillis() : null);

                    // sort
                    Collections.sort(children, new PropComparatorAsc(sortPairs));

                    if (startSort != null)
                    {
                        logger.debug("Post-query sort: " + children.size() + " in " + (System.currentTimeMillis() - startSort) + " msecs");
                    }
                }

                result = new ArrayList<NodeRef>(children.size());
                for (FilterSortNode child : children)
                {
                    result.add(tenantService.getBaseName(child.getNodeRef()));
                }
            }
            else
            {
                // unsorted (apart from any implicit order) - note: permissions are applied during result handling to allow early cutoff

                final int requestedCount = parameters.getResultsRequired();

                final List<NodeRef> rawResult = new ArrayList<NodeRef>(Math.min(1000, requestedCount));
                UnsortedChildQueryCallback callback = getUnsortedChildQueryCallback(rawResult, requestedCount, paramBean);
                UnsortedResultHandler resultHandler = new UnsortedResultHandler(callback);
                cannedQueryDAO.executeQuery(QUERY_NAMESPACE, QUERY_SELECT_GET_CHILDREN_WITHOUT_PROPS, params, 0, Integer.MAX_VALUE, resultHandler);
                resultHandler.done();

                // permissions have been applied
                result = PermissionCheckedValueMixin.create(rawResult);
            }
        }
        finally
        {
            if (start != null)
            {
                logger.debug("Base query " + (filterSortPropCnt > 0 ? "(sort=y, perms=n)" : "(sort=n, perms=y)") + ": " + result.size() + " in " + (System.currentTimeMillis() - start) + " msecs");
            }
        }

        return result;
    }

    // Set filter/sort props (between 0 and 3)
    private int setFilterSortParams(List<QName> filterSortProps, FilterSortNodeEntity params)
    {
        int cnt = 0;
        int propCnt = 0;

        for (QName filterSortProp : filterSortProps)
        {
            if (AuditablePropertiesEntity.getAuditablePropertyQNames().contains(filterSortProp))
            {
                params.setAuditableProps(true);
            }
            else if (filterSortProp.equals(SORT_QNAME_NODE_TYPE) || filterSortProp.equals(SORT_QNAME_NODE_IS_FOLDER))
            {
                params.setNodeType(true);
            }
            else
            {
                Long sortQNameId = getQNameId(filterSortProp);
                if (sortQNameId != null)
                {
                    if (propCnt == 0)
                    {
                        params.setProp1qnameId(sortQNameId);
                    }
                    else if (propCnt == 1)
                    {
                        params.setProp2qnameId(sortQNameId);
                    }
                    else if (propCnt == 2)
                    {
                        params.setProp3qnameId(sortQNameId);
                    }
                    else
                    {
                        // belts and braces
                        throw new AlfrescoRuntimeException("GetChildren: unexpected - cannot set sort parameter: " + cnt);
                    }

                    propCnt++;
                }
                else
                {
                    logger.warn("Skipping filter/sort param - cannot find: " + filterSortProp);
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

    private class PropComparatorAsc implements Comparator<FilterSortNode>
    {
        private List<Pair<QName, SortOrder>> sortProps;
        private Collator collator;

        public PropComparatorAsc(List<Pair<QName, SortOrder>> sortProps)
        {
            this.sortProps = sortProps;
            // try to overrider collator comparison rules
            this.collator = AlfrescoCollator.getInstance(I18NUtil.getContentLocale());
        }

        public int compare(FilterSortNode n1, FilterSortNode n2)
        {
            return compareImpl(n1, n2, sortProps);
        }

        private int compareImpl(FilterSortNode node1In, FilterSortNode node2In, List<Pair<QName, SortOrder>> sortProps)
        {
            Object pv1 = null;
            Object pv2 = null;

            QName sortPropQName = (QName) sortProps.get(0).getFirst();
            boolean sortAscending = (sortProps.get(0).getSecond() == SortOrder.ASCENDING);

            FilterSortNode node1 = node1In;
            FilterSortNode node2 = node2In;

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
                if (pv2 == null && sortProps.size() > 1)
                {
                    return compareImpl(node1In, node2In, sortProps.subList(1, sortProps.size()));
                }
                else
                {
                    return (pv2 == null ? 0 : -1);
                }
            }
            else if (pv2 == null)
            {
                return 1;
            }

            if (pv1 instanceof String)
            {
                result = collator.compare((String) pv1, (String) pv2); // TODO use collation keys (re: performance)
            }
            else if (pv1 instanceof Date)
            {
                result = (((Date) pv1).compareTo((Date) pv2));
            }
            else if (pv1 instanceof Long)
            {
                result = (((Long) pv1).compareTo((Long) pv2));
            }
            else if (pv1 instanceof Integer)
            {
                result = (((Integer) pv1).compareTo((Integer) pv2));
            }
            else if (pv1 instanceof QName)
            {
                result = (((QName) pv1).compareTo((QName) pv2));
            }
            else if (pv1 instanceof Boolean)
            {
                result = (((Boolean) pv1).compareTo((Boolean) pv2));
            }
            else
            {
                // TODO other comparisons
                throw new RuntimeException("Unsupported sort type: " + pv1.getClass().getName());
            }

            if ((result == 0) && (sortProps.size() > 1))
            {
                return compareImpl(node1In, node2In, sortProps.subList(1, sortProps.size()));
            }

            return result;
        }
    }

    private boolean includeAspects(NodeRef nodeRef, Set<QName> inclusiveAspects, Set<QName> exclusiveAspects)
    {
        if (inclusiveAspects == null && exclusiveAspects == null)
        {
            return true;
        }

        Set<QName> nodeAspects = nodeService.getAspects(nodeRef);
        if (inclusiveAspects != null)
        {
            Set<QName> includedIntersect = new HashSet<QName>(nodeAspects);
            includedIntersect.retainAll(inclusiveAspects);
            if (includedIntersect.isEmpty())
            {
                return false;
            }
        }
        if (exclusiveAspects != null)
        {
            Set<QName> excludedIntersect = new HashSet<QName>(nodeAspects);
            excludedIntersect.retainAll(exclusiveAspects);
            if (excludedIntersect.isEmpty() == false)
            {
                return false;
            }
        }
        return true;

    }

    // note: currently inclusive and OR-based
    private boolean includeFilter(Map<QName, Serializable> propVals, List<FilterProp> filterProps)
    {
        for (FilterProp filterProp : filterProps)
        {
            Serializable propVal = propVals.get(filterProp.getPropName());
            if (propVal != null)
            {
                if ((filterProp instanceof FilterPropString) && (propVal instanceof String))
                {
                    String val = (String) propVal;
                    String filter = (String) filterProp.getPropVal();

                    switch ((FilterTypeString) filterProp.getFilterType())
                    {
                    case STARTSWITH:
                        if (val.startsWith(filter))
                        {
                            return true;
                        }
                        break;
                    case STARTSWITH_IGNORECASE:
                        if (val.toLowerCase().startsWith(filter.toLowerCase()))
                        {
                            return true;
                        }
                        break;
                    case EQUALS:
                        if (val.equals(filter))
                        {
                            return true;
                        }
                        break;
                    case EQUALS_IGNORECASE:
                        if (val.equalsIgnoreCase(filter))
                        {
                            return true;
                        }
                        break;
                    case ENDSWITH:
                        if (val.endsWith(filter))
                        {
                            return true;
                        }
                        break;
                    case ENDSWITH_IGNORECASE:
                        if (val.toLowerCase().endsWith(filter.toLowerCase()))
                        {
                            return true;
                        }
                        break;
                    case MATCHES:
                        if (val.matches(filter))
                        {
                            return true;
                        }
                        break;
                    case MATCHES_IGNORECASE:
                        if (val.toLowerCase().matches(filter.toLowerCase()))
                        {
                            return true;
                        }
                        break;
                    default:
                    }
                }
            }

            if ((filterProp instanceof FilterPropBoolean) && (propVal instanceof Boolean))
            {
                Boolean val = (Boolean) propVal;
                Boolean filter = (Boolean) filterProp.getPropVal();

                return (val == filter);
            }
        }

        return false;
    }

    @Override
    protected boolean isApplyPostQueryPermissions()
    {
        return applyPostQueryPermissions; // true if sorted (if unsorted then permissions are applied as part of the query impl)
    }

    @Override
    protected List<NodeRef> applyPostQueryPermissions(List<NodeRef> results, int requestedCount)
    {
        Long start = (logger.isDebugEnabled() ? System.currentTimeMillis() : null);

        int requestTotalCountMax = getParameters().getTotalResultCountMax();
        int maxChecks = (((requestTotalCountMax > 0) && (requestTotalCountMax > requestedCount)) ? requestTotalCountMax : requestedCount);
        int cnt = results.size();

        int toIdx = (maxChecks > cnt ? cnt : maxChecks);

        // note: assume user has read access to most/majority of the items hence pre-load up to max checks
        preload(results.subList(0, toIdx));

        List<NodeRef> ret = super.applyPostQueryPermissions(results, requestedCount);

        if (start != null)
        {
            logger.debug("Post-query perms: " + ret.size() + " in " + (System.currentTimeMillis() - start) + " msecs");
        }

        return ret;
    }

    private void preload(List<NodeRef> nodeRefs)
    {
        Long start = (logger.isTraceEnabled() ? System.currentTimeMillis() : null);

        nodeDAO.cacheNodes(nodeRefs);

        if (start != null)
        {
            logger.trace("Pre-load: " + nodeRefs.size() + " in " + (System.currentTimeMillis() - start) + " msecs");
        }
    }

    protected interface FilterSortChildQueryCallback
    {
        boolean handle(FilterSortNode node);
    }

    protected class DefaultFilterSortChildQueryCallback implements FilterSortChildQueryCallback
    {
        private List<FilterSortNode> children;
        private List<FilterProp> filterProps;
        private boolean applyFilter;
        private Set<QName> inclusiveAspects;
        private Set<QName> exclusiveAspects;

        public DefaultFilterSortChildQueryCallback(final List<FilterSortNode> children, final List<FilterProp> filterProps)
        {
            this(children, filterProps, null, null);
        }

        public DefaultFilterSortChildQueryCallback(final List<FilterSortNode> children, final List<FilterProp> filterProps, Set<QName> inclusiveAspects, Set<QName> exclusiveAspects)
        {
            this.children = children;
            this.filterProps = filterProps;
            this.applyFilter = (filterProps.size() > 0);
            this.inclusiveAspects = inclusiveAspects;
            this.exclusiveAspects = exclusiveAspects;
        }

        @Override
        public boolean handle(FilterSortNode node)
        {
            if (include(node))
            {
                children.add(node);
            }

            // More results
            return true;
        }

        protected boolean include(FilterSortNode node)
        {
            // filter, if needed
            return (!applyFilter || includeFilter(node.getPropVals(), filterProps)) && includeAspects(node.getNodeRef(), inclusiveAspects, exclusiveAspects);
        }
    }

    protected class DefaultUnsortedChildQueryCallback implements UnsortedChildQueryCallback
    {
        private List<NodeRef> rawResult;
        private int requestedCount;
        private Set<QName> inclusiveAspects;
        private Set<QName> exclusiveAspects;

        public DefaultUnsortedChildQueryCallback(final List<NodeRef> rawResult, final int requestedCount, Set<QName> inclusiveAspects, Set<QName> exclusiveAspects)
        {
            this.rawResult = rawResult;
            this.requestedCount = requestedCount;
            this.inclusiveAspects = inclusiveAspects;
            this.exclusiveAspects = exclusiveAspects;
        }

        @Override
        public boolean handle(NodeRef nodeRef)
        {
            if (include(nodeRef))
            {
                rawResult.add(tenantService.getBaseName(nodeRef));
            }

            // More results ?
            return (rawResult.size() < requestedCount);
        }

        protected boolean include(NodeRef nodeRef)
        {
            return includeAspects(nodeRef, inclusiveAspects, exclusiveAspects);
        }
    }

    protected interface UnsortedChildQueryCallback
    {
        boolean handle(NodeRef nodeRef);
    }

    protected class FilterSortResultHandler implements CannedQueryDAO.ResultHandler<FilterSortNodeEntity>
    {
        private final FilterSortChildQueryCallback resultsCallback;
        private boolean more = true;

        private static final int BATCH_SIZE = 256 * 4;
        private final List<FilterSortNodeEntity> results;

        private FilterSortResultHandler(FilterSortChildQueryCallback resultsCallback)
        {
            this.resultsCallback = resultsCallback;

            results = new LinkedList<FilterSortNodeEntity>();
        }

        public boolean handleResult(FilterSortNodeEntity result)
        {
            // Do nothing if no further results are required
            if (!more)
            {
                return false;
            }

            if (results.size() >= BATCH_SIZE)
            {
                // batch
                preloadFilterSort();
            }

            results.add(result);

            return more;
        }

        public void done()
        {
            if (results.size() >= 0)
            {
                // finish batch
                preloadFilterSort();
            }
        }

        private void preloadFilterSort()
        {
            List<NodeRef> nodeRefs = new ArrayList<>(results.size());
            for (FilterSortNodeEntity result : results)
            {
                nodeRefs.add(result.getNode().getNodeRef());
            }

            preload(nodeRefs);

            for (FilterSortNodeEntity result : results)
            {
                Node node = result.getNode();
                NodeRef nodeRef = node.getNodeRef();

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

                NodePropertyEntity prop3 = result.getProp3();
                if (prop3 != null)
                {
                    propertyValues.put(prop3.getKey(), prop3.getValue());
                }

                Map<QName, Serializable> propVals = nodePropertyHelper.convertToPublicProperties(propertyValues);

                // Add referenceable / spoofed properties (including spoofed name if null)
                ReferenceablePropertiesEntity.addReferenceableProperties(node, propVals);

                // special cases

                // MLText (eg. cm:title, cm:description, ...)
                for (Map.Entry<QName, Serializable> entry : propVals.entrySet())
                {
                    if (entry.getValue() instanceof MLText)
                    {
                        propVals.put(entry.getKey(), DefaultTypeConverter.INSTANCE.convert(String.class, (MLText) entry.getValue()));
                    }
                }

                // ContentData (eg. cm:content.size, cm:content.mimetype)
                ContentData contentData = (ContentData) propVals.get(ContentModel.PROP_CONTENT);
                if (contentData != null)
                {
                    propVals.put(SORT_QNAME_CONTENT_SIZE, contentData.getSize());
                    propVals.put(SORT_QNAME_CONTENT_MIMETYPE, contentData.getMimetype());
                }

                // Auditable props (eg. cm:creator, cm:created, cm:modifier, cm:modified, ...)
                AuditablePropertiesEntity auditableProps = node.getAuditableProperties();
                if (auditableProps != null)
                {
                    for (Map.Entry<QName, Serializable> entry : auditableProps.getAuditableProperties().entrySet())
                    {
                        propVals.put(entry.getKey(), entry.getValue());
                    }
                }

                // Node type
                Long nodeTypeQNameId = node.getTypeQNameId();
                if (nodeTypeQNameId != null)
                {
                    Pair<Long, QName> pair = qnameDAO.getQName(nodeTypeQNameId);
                    if (pair != null)
                    {
                        propVals.put(SORT_QNAME_NODE_TYPE, pair.getSecond());
                    }
                }

                // Call back
                boolean more = resultsCallback.handle(new FilterSortNode(nodeRef, propVals));
                if (!more)
                {
                    this.more = false;
                    break;
                }
            }

            results.clear();
        }
    }

    protected class FilterSortNode
    {
        private NodeRef nodeRef;
        private Map<QName, Serializable> propVals; // subset of nodes properties - used for filtering and/or sorting

        public FilterSortNode(NodeRef nodeRef, Map<QName, Serializable> propVals)
        {
            this.nodeRef = nodeRef;
            this.propVals = propVals;
        }

        @Override
        public String toString()
        {
            return "FilterSortNode [nodeRef=" + nodeRef + ", propVals=" + propVals + "]";
        }

        public NodeRef getNodeRef()
        {
            return nodeRef;
        }

        public Serializable getVal(QName prop)
        {
            return propVals.get(prop);
        }

        public Map<QName, Serializable> getPropVals()
        {
            return propVals;
        }
    }

    private class UnsortedResultHandler implements CannedQueryDAO.ResultHandler<NodeEntity>
    {
        private final UnsortedChildQueryCallback resultsCallback;

        private boolean more = true;

        private static final int BATCH_SIZE = 256 * 4;
        private final List<NodeRef> nodeRefs;

        private UnsortedResultHandler(UnsortedChildQueryCallback resultsCallback)
        {
            this.resultsCallback = resultsCallback;

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

            // TODO track total time for incremental permission checks ... and cutoff (eg. based on some config)
            List<NodeRef> results = applyPostQueryPermissions(nodeRefs, nodeRefs.size());

            for (NodeRef nodeRef : results)
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
