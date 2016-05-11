/*
 * Copyright (C) 2005-2016 Alfresco Software Limited.
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

package org.alfresco.rest.api.impl;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.Queries;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.resource.parameters.SortColumn;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author janv
 */
public class QueriesImpl implements Queries, InitializingBean
{
    private ServiceRegistry sr;
    private SearchService searchService;
    private NodeService nodeService;
    private NamespaceService namespaceService;

    private final static String QT_FIELD = "keywords";

    private final static String QUERY_LIVE_SEARCH_NODES = "live-search-nodes";

    private final static Map<String,QName> MAP_PARAM_SORT_QNAME;
    static
    {
        Map<String,QName> aMap = new HashMap<>(3);

        aMap.put(PARAM_NAME, ContentModel.PROP_NAME);
        aMap.put(PARAM_CREATEDAT, ContentModel.PROP_CREATED);
        aMap.put(PARAM_MODIFIEDAT, ContentModel.PROP_MODIFIED);

        MAP_PARAM_SORT_QNAME = Collections.unmodifiableMap(aMap);
    }


    private Nodes nodes;

    public void setServiceRegistry(ServiceRegistry sr)
    {
        this.sr = sr;
    }

    public void setNodes(Nodes nodes)
    {
        this.nodes = nodes;
    }

    @Override
    public void afterPropertiesSet()
    {
        ParameterCheck.mandatory("sr", this.sr);
        ParameterCheck.mandatory("nodes", this.nodes);

        this.searchService = sr.getSearchService();
        this.nodeService = sr.getNodeService();
        this.namespaceService = sr.getNamespaceService();
    }

    @Override
    public CollectionWithPagingInfo<Node> findNodes(String queryId, Parameters parameters)
    {
        if (! QUERY_LIVE_SEARCH_NODES.equals(queryId))
        {
            throw new NotFoundException(queryId);
        }

        StringBuilder sb = new StringBuilder();

        // TODO check min length, excluding quotes etc
        String term = parameters.getParameter(PARAM_TERM);
        if (term == null)
        {
            throw new InvalidArgumentException("Query 'term' not specified");
        }

        String rootNodeId = parameters.getParameter(PARAM_ROOT_NODE_ID);
        if (rootNodeId != null)
        {
            sb.append("PATH:\"").append(getQNamePath(rootNodeId)).append("//*\" AND (");
        }

        // this will be expanded via query template
        sb.append(QT_FIELD+":").append(term);

        if (rootNodeId != null)
        {
            sb.append(")");
        }

        String nodeType = parameters.getParameter(PARAM_NODE_TYPE);
        if (nodeType != null)
        {
            // TODO could/should check that this is a valid type ?
            sb.append(" AND (+TYPE:\"").append(nodeType).append(("\""));
        }
        else
        {
            sb.append(" AND (+TYPE:\"cm:content\" OR +TYPE:\"cm:folder\")");

            sb.append(" AND -TYPE:\"cm:thumbnail\" AND -TYPE:\"cm:failedThumbnail\" AND -TYPE:\"cm:rating\" AND -TYPE:\"fm:post\"")
                    .append(" AND -ASPECT:\"sys:hidden\" AND -cm:creator:system AND -TYPE:\"st:site\"")
                    .append(" AND -ASPECT:\"st:siteContainer\" AND -QNAME:comment\\-* ");
        }

        SearchParameters sp = new SearchParameters();

        sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        sp.setQuery(sb.toString());
        sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

        // query template / field
        sp.addQueryTemplate(QT_FIELD, "%(cm:name cm:title cm:description TEXT TAG)");

        Paging paging = parameters.getPaging();
        PagingRequest pagingRequest = Util.getPagingRequest(paging);

        sp.setSkipCount(pagingRequest.getSkipCount());
        sp.setMaxItems(pagingRequest.getMaxItems());

        List<SortColumn> sortCols = parameters.getSorting();
        if ((sortCols != null) && (sortCols.size() > 0))
        {
            for (SortColumn sortCol : sortCols)
            {
                QName sortPropQName = MAP_PARAM_SORT_QNAME.get(sortCol.column);
                if (sortPropQName == null)
                {
                    throw new InvalidArgumentException("Invalid sort field: "+sortCol.column);
                }
                sp.addSort("@" + sortPropQName,  sortCol.asc);
            }
        }
        else
        {
            // default sort order
            sp.addSort("@" + ContentModel.PROP_MODIFIED, false);
        }

        ResultSet results = searchService.query(sp);

        List<Node> nodeList = new ArrayList<>(results.length());

        for (ResultSetRow row : results)
        {
            NodeRef nodeRef = row.getNodeRef();
            nodeList.add(nodes.getFolderOrDocument(nodeRef.getId(), parameters));
        }

        results.close();

        return CollectionWithPagingInfo.asPaged(paging, nodeList, results.hasMore(), new Long(results.getNumberFound()).intValue());
    }

    private String getQNamePath(String nodeId)
    {
        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId);

        Map<String, String> cache = new HashMap<>();
        StringBuilder buf = new StringBuilder(128);
        Path path = nodeService.getPath(nodeRef);
        for (Path.Element e : path)
        {
            if (e instanceof Path.ChildAssocElement)
            {
                QName qname = ((Path.ChildAssocElement)e).getRef().getQName();
                if (qname != null)
                {
                    String prefix = cache.get(qname.getNamespaceURI());
                    if (prefix == null)
                    {
                        // first request for this namespace prefix, get and cache result
                        Collection<String> prefixes = namespaceService.getPrefixes(qname.getNamespaceURI());
                        prefix = prefixes.size() != 0 ? prefixes.iterator().next() : "";
                        cache.put(qname.getNamespaceURI(), prefix);
                    }
                    buf.append('/').append(prefix).append(':').append(ISO9075.encode(qname.getLocalName()));
                }
            }
            else
            {
                buf.append('/').append(e.toString());
            }
        }
        return buf.toString();
    }
}
